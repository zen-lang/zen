(ns zen.schema
  (:require [zen.utils :as utils]
            [zen.validation.utils :as validation.utils]
            [clojure.set]))


(defn rule-priority [k]
  (cond
    (= k :keys) 0
    (= k :key) 10
    (= k :values) 1
    :else 100))


(defn valtype-rule [vtx data open-world?] #_"NOTE: maybe refactor name to 'set-unknown-keys ?"
  (let [filter-allowed
        (fn [unknown]
          (->> unknown
               (remove #(= (vec (butlast %)) (:path vtx)))
               set))

        set-unknown
        (fn [unknown]
          (let [empty-unknown? (empty? unknown)
                empty-visited? (empty? (:visited vtx))]
            (cond (and empty-unknown? (not empty-visited?))
                  (clojure.set/difference (validation.utils/cur-keyset vtx data)
                                          (:visited vtx))

                  (and empty-unknown? empty-visited?)
                  (set (validation.utils/cur-keyset vtx data))

                  (not empty-unknown?)
                  (clojure.set/difference unknown (:visited vtx)))))]

    (if open-world?
      (-> vtx
          (update :unknown-keys filter-allowed)
          (update :visited into (validation.utils/cur-keyset vtx data)))
      (update vtx :unknown-keys set-unknown))))


(defmulti compile-key (fn [k ztx kfg] k))


(defn safe-compile-key [k ztx kfg]
  (try (compile-key k ztx kfg)
       (catch Exception e
         {:rule (fn [vtx _data _opts]
                  (validation.utils/add-err vtx
                                            k
                                            {:type "compile-key-exception"
                                             :message (.getMessage e)}))})))


(defn navigate-props [vtx data props opts]
  (reduce (fn [vtx* prop]
            (if-let [prop-value (get data prop)]
              (-> (validation.utils/node-vtx&log vtx* [:property prop] [prop])
                  ((get props prop) prop-value opts)
                  (validation.utils/merge-vtx vtx*))
              vtx*))
          vtx
          (keys props)))


(defn compile-schema [ztx schema props]
  (let [rulesets (->> (dissoc schema :validation-type)
                      (remove (fn [[k _]] (contains? props k)))
                      (map (fn [[k kfg]]
                             (assoc (safe-compile-key k ztx kfg) ::priority (rule-priority k))))
                      (sort-by ::priority)
                      doall)
        open-world? (or (:key schema)
                        (:values schema)
                        (= (:validation-type schema) :open)
                        (= (:type schema) 'zen/any))]
    (fn compiled-sch [vtx data opts]
      (loop [rs rulesets
             vtx* (navigate-props (assoc vtx :type (:type schema)) data props opts)]
        (cond
          (and (empty? rs) (map? data) (:type schema)) #_"NOTE: why not (= 'zen/map (:type schema)) ?"
          (valtype-rule vtx* data open-world?)

          (empty? rs) vtx*

          :else
          (let [{when-fn :when rule-fn :rule} (first rs)
                when-fn (or when-fn (constantly true))]
            (if (when-fn data)
              (recur (rest rs) (rule-fn vtx* data opts))
              (recur (rest rs) vtx*))))))))


(declare resolve-props)


(defn get-cached
  [ztx schema init?]
  (let [hash* (hash schema)
        v-promise (get-in @ztx [:zen.v2-validation/compiled-schemas hash*])]
    (if (some? v-promise) #_"NOTE: race condition will result in double compilation, but this shouldn't crash anything"
      (fn [vtx data opts]
        ;; TODO add to vtx :warning
        (let [v (deref v-promise
                       (:compile-schema-timeout opts 60000)
                       ::timeout)]
          (if (= ::timeout v) ;; can't wait this long for the compilation to end, going to compile ourselves
            (do (swap! ztx update :zen.v2-validation/compiled-schemas dissoc hash*)
                ((get-cached ztx schema init?)
                 vtx data opts))
            (v vtx data opts))))

      (let [v-promise (promise)
            _ (swap! ztx assoc-in [:zen.v2-validation/compiled-schemas hash*] v-promise)

            props
            (if init?
              (resolve-props ztx)
              (:zen.v2-validation/prop-schemas @ztx))

            v (compile-schema ztx schema props)]

        (deliver v-promise v)
        v))))


(defn resolve-props [ztx]
  (->> (utils/get-tag ztx 'zen/property)
       (map (fn [prop]
              (zen.utils/get-symbol ztx prop)))
       (map (fn [sch]
              [sch (get-cached ztx sch false)]))
       (reduce (fn [acc [sch v]]
                 (assoc acc (keyword (:zen/name sch)) v))
               {})
       (swap! ztx assoc :zen.v2-validation/prop-schemas)
       :zen.v2-validation/prop-schemas))


(defn apply-schema
  "gets schema from cache and appiles on data with chosen interpreter. ex 'zen.v2-validation/*validate-schema"
  [ztx vtx schema data {:keys [sch-symbol] :as opts}]
  (let [vtx (-> vtx
                (assoc :schema [(or sch-symbol (:zen/name schema))])
                (assoc :path [])
                (assoc-in [:zen.v2-validation/confirmed [] (:zen/name schema)] true))

        compiled-schema-fn (get-cached ztx schema true)]

    (compiled-schema-fn vtx data opts)))
