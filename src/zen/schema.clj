(ns zen.schema
  (:require [zen.utils :as utils]
            [zen.validation.utils :as validation.utils]
            [clojure.set]))


(defn rule-priority [k]
  (cond
    (= k :keys) 0
    (= k :key) 10
    (= k :values) 1
    (= k :validation-type) 1000
    :else 100))


(defmulti compile-key (fn [k ztx kfg] k))


(defn safe-compile-key [k ztx kfg]
  (try (compile-key k ztx kfg)
       (catch Exception e
         {:rule (fn [vtx _data _opts]
                  (validation.utils/add-err vtx
                                            k
                                            {:type "compile-key-exception"
                                             :message (.getMessage e)}))})))


#_"TODO: maybe move to ztx?"
(defonce schema-post-process-hooks-atom
  (atom {}))

(defonce schema-pre-process-hooks-atom
  (atom {}))


#_"TODO: maybe support multiple hooks per interpreter?"
(defn register-schema-post-process-hook! [interpreter hook-fn]
  (swap! schema-post-process-hooks-atom assoc interpreter hook-fn))


(defn register-schema-pre-process-hook! [interpreter hook-fn]
  (swap! schema-pre-process-hooks-atom assoc interpreter hook-fn))


(defn get-schema-hooks [ztx hooks-map schema]
  (into {}
        (keep (fn [[interpreter schema-hook-fn]]
                (when-let [hook-fn (schema-hook-fn ztx schema)]
                  [interpreter hook-fn])))
        hooks-map))


(defn get-interpreter-hook-fn [hooks-map data opts interpreter]
  (when-let [hook-fn (get hooks-map interpreter)]
    (fn [vtx] (or (hook-fn vtx data opts)
                  vtx))))


(defn get-hooks [hooks-map data opts]
  (not-empty (keep #(get-interpreter-hook-fn hooks-map data opts %)
                   (:interpreters opts))))


(defn wrap-with-hooks [compiled-schema-fn {:keys [pre post]}]
  (if (and (empty? pre) (empty? post))
    compiled-schema-fn
    (fn compiled-with-hooks-fn [vtx data opts]
      (let [pre-hooks  (get-hooks pre data opts)
            post-hooks (get-hooks post data opts)]
        (cond-> vtx
          pre-hooks  (as-> $ (reduce #(%2 %1) $ pre-hooks))
          :always    (compiled-schema-fn data opts)
          post-hooks (as-> $ (reduce #(%2 %1) $ post-hooks)))))))


(defn compile-schema [ztx schema]
  (let [rulesets (->> schema
                      (keep (fn [[k v]]
                              (-> (safe-compile-key k ztx v)
                                  (assoc ::priority (rule-priority k)))))
                      (sort-by ::priority))

        compiled-schema-fn
        (fn compiled-schema-fn [vtx data opts]
          (let [vtx* (assoc vtx :type (:type schema))]
            (loop [rs rulesets, vtx* vtx*]
              (if (empty? rs)
                vtx*
                (let [{when-fn :when rule-fn :rule} (first rs)]
                  (if (or (nil? when-fn) (when-fn data))
                    (recur (rest rs) (rule-fn vtx* data opts))
                    (recur (rest rs) vtx*)))))))]

    (wrap-with-hooks
      compiled-schema-fn
      {:pre (get-schema-hooks ztx @schema-pre-process-hooks-atom schema)
       :post (get-schema-hooks ztx @schema-post-process-hooks-atom schema)})))


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
            v (compile-schema ztx schema)]

        (deliver v-promise v)
        v))))


(defn apply-schema
  "gets schema from cache and appiles on data with chosen interpreter. ex 'zen.v2-validation/*validate-schema"
  [ztx vtx schema data {:keys [sch-symbol] :as opts}]
  (let [vtx (-> vtx
                (assoc :schema [(or sch-symbol (:zen/name schema))])
                (assoc :path [])
                (assoc-in [:zen.v2-validation/confirmed [] (:zen/name schema)] true))

        compiled-schema-fn (get-cached ztx schema true)]

    (compiled-schema-fn vtx data opts)))
