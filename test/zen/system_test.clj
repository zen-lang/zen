(ns zen.system-test
  (:require
   [clojure.test :as t]
   [matcho.core :as matcho]
   [zen.core :as zen]))

(defmethod zen/start
  'mysystem/custom-comp
  [ztx _config]
  (zen/pub ztx 'mysystem.custom-comp/start {})
  (atom {}))

(defmethod zen/stop
  'mysystem/custom-comp
  [ztx _config state]
  (zen/pub ztx 'mysystem.custom-comp/stop (keys @state)))

(defmethod zen/op
  'mysystem/save
  [ztx _config req & [_session]]
  (if-let [db (zen/get-state ztx :custom-comp)]
    (do
      (zen/pub ztx 'save req)
      (swap! db assoc-in [(:resourceType req) (:id req)] req))
    (zen/error ztx 'mysystem/no-db {:op 'mysystem/save})))

(defmethod zen/op
  'mysystem/search
  [ztx _config req & [_session]]
  (if-let [db (zen/get-state ztx :custom-comp)]
    (do
      (zen/pub ztx 'search req)
      (get @db (:resourceType req)))
    (zen/error ztx 'mysystem/no-db {:op 'mysystem/search})))

(defmethod zen/op
  'mysystem/just-op
  [ztx _config _req & [_session]]
  (swap! ztx assoc :just-op true)
  {:status :ok})

(defmethod zen/op
  'mysystem/generic-op
  [ztx _config {{v :value :as params} :params} & [_session]]
  (swap! ztx assoc :generic-op v)
  {:status :ok :params params})

(t/deftest test-zen-system

  (def ztx (zen/new-context {}))

  (zen/load-ns
   ztx '{ns mysystem

         save
         {:zen/tags #{zen/op}}

         search
         {:zen/tags #{zen/op}}

         comp-sch
         {:zen/tags #{zen/schema zen/tag}
          :type zen/map
          :values {:type zen/any}}

         just-op
         {:zen/tags #{zen/op}}

         generic-op
         {:zen/tags #{zen/op}}

         partial-op
         {:op generic-op
          :params {:value 2}}

         custom-comp
         {:zen/tags #{zen/start comp-sch}
          :zen/state-key :custom-comp
          :config {:attr "value"}}

         system
         {:zen/tags #{zen/system}
          :start [custom-comp just-op partial-op]}})

  (zen/start-system ztx 'mysystem/system)

  (t/is (empty? (zen/errors ztx)))

  (t/is (zen/model-state ztx (zen/get-symbol ztx 'mysystem/custom-comp)))
  (t/is (zen/get-state ztx :custom-comp))

  (t/is (:just-op @ztx))
  (t/is (= 2 (:generic-op @ztx)))

  (zen/op-call ztx 'mysystem/generic-op {:params {:value 3}})

  (zen/op-call ztx 'mysystem/partial-op {:params {:value 3 :extra 4}})

  (zen/op-call ztx 'mysystem/just-op {})

  (:zen/state @ztx)

  (matcho/match
   (zen/op-call ztx 'mysystem/save   {:resourceType "Patient" :id "pt1"})
    {"Patient" {"pt1" {:resourceType "Patient", :id "pt1"}}})

  (matcho/match
   (zen/op-call ztx 'mysystem/search {:resourceType "Patient"})
    {"pt1" {:resourceType "Patient", :id "pt1"}})

  (zen/stop-system ztx))
