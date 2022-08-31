(ns zen.system-test
  (:require
   [zen.core :as zen]
   [matcho.core :as matcho]
   [clojure.test :as t]))

(defmethod zen/start
  'mysystem/custom-comp
  [ztx config]
  (zen/pub ztx 'mysystem.custom-comp/start {})
  (atom {}))

(defmethod zen/stop
  'mysystem/custom-comp
  [ztx config state]
  (zen/pub ztx 'mysystem.custom-comp/stop (keys @state)))

(defmethod zen/op
  'mysystem/save
  [ztx config req & [session]]
  (if-let [db (zen/get-state ztx :custom-comp)]
    (do
      (zen/pub ztx 'save req)
      (swap! db assoc-in [(:resourceType req) (:id req)] req))
    (zen/error ztx 'mysystem/no-db {:op 'mysystem/save})))

(defmethod zen/op
  'mysystem/search
  [ztx config req & [session]]
  (if-let [db (zen/get-state ztx :custom-comp)]
    (do
      (zen/pub ztx 'search req)
      (get @db (:resourceType req)))
    (zen/error ztx 'mysystem/no-db {:op 'mysystem/search})))

(t/deftest test-zen-system

  (def ztx (zen/new-context {}))

  (zen/load-ns!
   ztx '{ns mysystem

         save
         {:zen/tags #{zen/op}
          :params {}}

         search
         {:zen/tags #{zen/op}
          :params {}}

         comp-sch
         {:zen/tags #{zen/schema zen/tag}
          :type zen/map
          :values {:type zen/any}}

         custom-comp
         {:zen/tags #{zen/start comp-sch}
          :zen/state-key :custom-comp
          :config {:attr "value"}}

         system
         {:zen/tags #{zen/system}
          :start [custom-comp]}})

  (zen/start-system ztx 'mysystem/system)

  (t/is (empty? (zen/errors ztx)))

  (t/is (zen/model-state ztx (zen/get-symbol ztx 'mysystem/custom-comp)))
  (t/is (zen/get-state ztx :custom-comp))

  (:zen/state @ztx)

  (matcho/match
   (zen/op-call ztx 'mysystem/save   {:resourceType "Patient" :id "pt1"})
   {"Patient" {"pt1" {:resourceType "Patient", :id "pt1"}}})

  (matcho/match
   (zen/op-call ztx 'mysystem/search {:resourceType "Patient"})
   {"pt1" {:resourceType "Patient", :id "pt1"}})

  (zen/stop-system ztx))
