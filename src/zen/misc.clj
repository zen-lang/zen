(ns zen.misc
  (:require
   [zen.utils :as utils]
   [clojure.string :as str]
   [zen.v2-validation]))

(defmethod zen.v2-validation/compile-key :zen.fhir/reference
  [_ ztx {:keys [refers]}]
  ;; TODO add test somewhere?
  {:rule
   (fn [vtx data opts]
     (if-not (and (seq refers) (some data [:resourceType :type]))
       vtx
       (let [tp            (some data [:resourceType :type])
             refer-schemas (map (partial utils/get-symbol ztx) refers)
             refer-types   (into #{} (map :zen.fhir/type) refer-schemas)]
         (if (contains? refer-types tp)
           (let [schemas-stack (->> (:schema vtx) (partition-by #{:confirms}) (take-nth 2) (map first))]
             (update-in vtx [:fx :zen.fhir/reference :deferreds]
                        (fnil conj [])
                        {:schemas schemas-stack
                         :path     (:path vtx)
                         :profiles (into #{}
                                         (keep (fn [sch]
                                                 (when (contains? (:zen/tags sch) 'zen.fhir/profile-schema)
                                                   (:zen.fhir/profileUri sch))))
                                         refer-schemas)
                         :value    data
                         :deferred {:reference true}}))
           (update vtx
                   :errors
                   (fnil conj [])
                   {:path    (:path vtx)
                    :message (str "expected one of " (str/join "," refer-types))})))))})

(defmethod zen.v2-validation/compile-key :zen.fhir/value-set
  [_ ztx value-set]
  {:rule
   (fn [vtx data opts]
     (if (and (= "enabled" (get-in @ztx
                                   [:aidbox/config :features :validation :value-set :mode]
                                   "enabled"))
              (= :required (:strength value-set)))
       (let [schemas-stack (->> (:schema vtx) (partition-by #{:confirms}) (take-nth 2) (map first))]
         (update-in vtx [:fx :zen.fhir/value-set :value-sets] conj
                    {:schemas   schemas-stack
                     :path      (:path vtx)
                     :data      data
                     :value-set (:symbol value-set)
                     :strength  (:strength value-set)}))
       vtx))})
