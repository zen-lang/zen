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


(defonce schema-key-interpreters-atom (atom {}))


(defonce schema-post-process-hooks-atom (atom {}))


(defonce schema-pre-process-hooks-atom (atom {}))


(defn register-compile-key-interpreter! [[k interpreter] f]
  (swap! schema-key-interpreters-atom assoc-in [k interpreter] f))


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


(defn safe-compile-key [k ztx kfg]
  (try (merge (some-> (get @schema-key-interpreters-atom k)
                      (update-vals
                        (fn [interpreter-compile-key-fn]
                          (interpreter-compile-key-fn k ztx kfg))))
              (compile-key k ztx kfg))
       (catch Exception e
         {:rule (fn [vtx _data _opts]
                  (validation.utils/add-err vtx
                                            k
                                            {:type "compile-key-exception"
                                             :message (.getMessage e)}))})))


(defn compile-schema [ztx schema]
  (let [rulesets (->> schema
                      (keep (fn [[k v]]
                              (-> (safe-compile-key k ztx v)
                                  (assoc ::priority (rule-priority k)))))
                      (sort-by ::priority))

        compiled-schema-fn
        (fn compiled-schema-fn [vtx data opts]
          (loop [rs rulesets
                 vtx* (assoc vtx :type (:type schema))]
            (if (empty? rs)
              vtx*
              (let [{:as r, when-fn :when} (first rs)]
                (if (or (nil? when-fn) (when-fn data))
                  (recur (rest rs)
                         (->> (:interpreters opts)
                              (keep #(get r %))
                              (reduce #(%2 %1 data opts) vtx*)))
                  (recur (rest rs) vtx*))))))]

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

        compiled-schema-fn (get-cached ztx schema true)

        opts (update opts :interpreters #(into [:rule ::navigate] %))]

    (compiled-schema-fn vtx data opts)))


(defmethod compile-key :keys            [_ _ _] {:when map?})
(defmethod compile-key :key             [_ _ _] {:when map?})
(defmethod compile-key :exclusive-keys  [_ _ _] {:when map?})
(defmethod compile-key :validation-type [_ _ _] {:when map?})
(defmethod compile-key :require         [_ _ _] {:when map?})
(defmethod compile-key :values          [_ _ _] {:when map?})
(defmethod compile-key :schema-key      [_ _ _] {:when map?})
(defmethod compile-key :keyname-schemas [_ _ _] {:when map?})
(defmethod compile-key :key-schema      [_ _ _] {:when map?})

(defmethod compile-key :scale     [_ _ _] {:when number?})
(defmethod compile-key :precision [_ _ _] {:when number?})
(defmethod compile-key :min       [_ _ _] {:when number?})
(defmethod compile-key :max       [_ _ _] {:when number?})

(defmethod compile-key :minLength [_ _ _] {:when string?})
(defmethod compile-key :maxLength [_ _ _] {:when string?})
(defmethod compile-key :regex     [_ _ _] {:when string?})

(defmethod compile-key :every    [_ _ _] {:when #(or (sequential? %) (set? %))})
(defmethod compile-key :minItems [_ _ _] {:when #(or (sequential? %) (set? %))})
(defmethod compile-key :maxItems [_ _ _] {:when #(or (sequential? %) (set? %))})

(defmethod compile-key :nth          [_ _ _] {:when sequential?})
(defmethod compile-key :schema-index [_ _ _] {:when sequential?})
(defmethod compile-key :slicing      [_ _ _] {:when sequential?})

(defmethod compile-key :subset-of   [_ _ _] {:when set?})
(defmethod compile-key :superset-of [_ _ _] {:when set?})

(defmethod compile-key :tags [_ _ _] {:when #(or (symbol? %) (list? %) (string? %))})


(register-compile-key-interpreter!
  [:keys ::navigate]
  (fn [_ ztx ks]
    (let [key-rules (->> ks
                         (map (fn [[k sch]]
                                [k (get-cached ztx sch false)]))
                         (into {}))]
      (fn [vtx data opts]
        (loop [data (seq data)
               vtx* vtx]
          (if (empty? data)
            vtx*
            (let [[k v] (first data)]
              (if-let [key-rule (get key-rules k)]
                (recur (rest data)
                       (-> (validation.utils/node-vtx&log vtx* [k] [k] :keys)
                           (key-rule v opts)
                           (validation.utils/merge-vtx vtx*)))
                (recur (rest data) vtx*)))))))))


(register-compile-key-interpreter!
 [:values ::navigate]
 (fn [_ ztx sch]
   (let [v (get-cached ztx sch false)]
     (fn [vtx data opts]
       (reduce-kv (fn [vtx* key value]
                    (let [node-visited?
                          (when-let [pth (get (:visited vtx*) (validation.utils/cur-path vtx* [key]))]
                            (:keys (get (:visited-by vtx*) pth)))

                          strict?
                          (= (:valmode opts) :strict)]
                      (if (and (not strict?) node-visited?)
                        vtx*
                        (-> (validation.utils/node-vtx&log vtx* [:values] [key])
                            (v value opts)
                            (validation.utils/merge-vtx vtx*)))))
                  vtx
                  data)))))


(register-compile-key-interpreter!
 [:every ::navigate]
 (fn [_ ztx sch]
   (let [v (get-cached ztx sch false)]
     (fn [vtx data opts]
       (let [data*
             (cond
               (seq (:indices opts))
               (map vector (:indices opts) data)

               (set? data)
               (map (fn [set-el] [set-el set-el]) data)

               :else
               (map-indexed vector data))]
         (reduce (fn [vtx [idx item]]
                   (-> (validation.utils/node-vtx vtx [:every idx] [idx])
                       (v item (dissoc opts :indices))
                       (validation.utils/merge-vtx vtx)))
                 vtx
                 data*))))))


(register-compile-key-interpreter!
 [:confirms ::navigate]
  (fn [_ ztx ks]
    (let [compile-confirms
          (fn [sym]
            (if-let [sch (utils/get-symbol ztx sym)]
              [sym (:zen/name sch) (get-cached ztx sch false)]
              [sym]))

          comp-fns
          (->> ks
               (map compile-confirms)
               doall)]
    (fn confirms-sch [vtx data opts]
      (loop [comp-fns comp-fns
             vtx*     vtx]
        (if (empty? comp-fns)
          vtx*
          (let [[sym sch-nm v] (first comp-fns)]
            (cond
              (true? (get-in vtx* [:zen.v2-validation/confirmed (:path vtx*) sch-nm]))
              (recur (rest comp-fns) vtx*)

              (fn? v)
              (recur (rest comp-fns)
                     (-> (assoc-in vtx* [:zen.v2-validation/confirmed (:path vtx*) sch-nm] true)
                         (validation.utils/node-vtx [:confirms sch-nm])
                         (v data opts)
                         (validation.utils/merge-vtx vtx*)))

              :else
              (recur (rest comp-fns)
                     #_"NOTE: This errors mechanism comes from ::validate interpreter. Maybe we should untie it from here."
                     (validation.utils/add-err vtx* :confirms {:message (str "Could not resolve schema '" sym)}))))))))))


#_"NOTE: Errors mechanism used here comes from ::validate interpreter. Maybe we should untie it from here."
(register-compile-key-interpreter!
 [:schema-key ::navigate]
 (fn [_ ztx {sk :key sk-ns :ns sk-tags :tags}]
  (fn [vtx data opts]
    (if-let [sch-nm (get data sk)]
      (let [sch-symbol               (if sk-ns (symbol sk-ns (name sch-nm)) (symbol sch-nm))
            {tags :zen/tags :as sch} (utils/get-symbol ztx sch-symbol)]
         (cond
           (nil? sch)
           (validation.utils/add-err vtx :schema-key
                    {:message (str "Could not find schema " sch-symbol)
                     :type    "schema"})

           (not (contains? tags 'zen/schema))
           (validation.utils/add-err vtx :schema-key
                    {:message (str "'" sch-symbol " should be tagged with zen/schema, but " tags)
                     :type    "schema"})

           (and sk-tags (not (clojure.set/subset? sk-tags tags)))
           (validation.utils/add-err vtx :schema-key
                    {:message (str "'" sch-symbol " should be tagged with " sk-tags ", but " tags)
                     :type    "schema"})

           :else
           (let [v (get-cached ztx sch false)]
             (-> (validation.utils/node-vtx vtx [:schema-key sch-symbol])
                 (v data opts)
                 (validation.utils/merge-vtx vtx)))))
       vtx))))


#_"NOTE: Errors mechanism used here comes from ::validate interpreter. Maybe we should untie it from here."
(register-compile-key-interpreter!
 [:schema-index ::navigate]
 (fn [_ ztx {si :index si-ns :ns}]
   (fn [vtx data opts]
     (if-let [sch-nm (or (get data si) (nth data si))]
       (let [sch-symbol (if si-ns (symbol si-ns (name sch-nm)) sch-nm)
             sch        (utils/get-symbol ztx sch-symbol)]
         (cond
           (nil? sch)
           (validation.utils/add-err vtx
                    :schema-index
                    {:message (format "Could not find schema %s" sch-symbol)
                     :type    "schema"})

           :else
           (let [v (get-cached ztx sch false)]
             (-> (validation.utils/node-vtx vtx [:schema-index sch-symbol])
                 (v data opts)
                 (validation.utils/merge-vtx vtx)))))
       vtx))))


(register-compile-key-interpreter!
 [:nth ::navigate]
 (fn [_ ztx cfg]
   (let [schemas (doall
                  (map (fn [[index v]] [index (get-cached ztx v false)])
                       cfg))]
     (fn [vtx data opts]
       (reduce (fn [vtx* [index v]]
                 (if-let [nth-el (and (< index (count data))
                                      (nth data index))]
                   (-> (validation.utils/node-vtx vtx* [:nth index] [index])
                       (v nth-el opts)
                       (validation.utils/merge-vtx vtx*))
                   vtx*))
               vtx
               schemas)))))


(register-compile-key-interpreter!
 [:keyname-schemas ::navigate]
 (fn [_ ztx {:keys [tags]}]
   (fn [vtx data opts]
     (let [rule-fn
           (fn [vtx* [schema-key data*]]
             (if-let [sch (and (qualified-ident? schema-key) (utils/get-symbol ztx (symbol schema-key)))]
               ;; TODO add test on nil case
               (if (or (nil? tags)
                       (clojure.set/subset? tags (:zen/tags sch)))
                 (-> (validation.utils/node-vtx&log vtx* [:keyname-schemas schema-key] [schema-key])
                     ((get-cached ztx sch false) data* opts)
                     (validation.utils/merge-vtx vtx*))
                 vtx*)
               vtx*))]
       (reduce rule-fn vtx data)))))


(register-compile-key-interpreter!
 [:key ::navigate]
 (fn [_ ztx sch]
   (let [v (get-cached ztx sch false)]
     (fn [vtx data opts]
       (reduce (fn [vtx* [k _]]
                 (let [node-visited?
                       (when-let [pth (get (:visited vtx*)
                                           (validation.utils/cur-path vtx* [k]))]
                         (:keys (get (:visited-by vtx*) pth)))

                       strict?
                       (= (:valmode opts) :strict)]
                   (if (and (not strict?) node-visited?)
                     vtx*
                     (-> (validation.utils/node-vtx&log vtx* [:key] [k])
                         (v k opts)
                         (validation.utils/merge-vtx vtx*)))))
               vtx
               data)))))


(register-compile-key-interpreter!
 [:key-schema ::navigate]
 (fn [_ ztx {:keys [tags key]}]
   (let [keys-schemas
         (->> tags
              (mapcat #(utils/get-tag ztx %))
              (mapv (fn [sch-name]
                      (let [sch (utils/get-symbol ztx sch-name)] ;; TODO get rid of type coercion
                        {:sch-key (if (= "zen" (namespace sch-name))
                                    (keyword (name sch-name))
                                    (keyword sch-name))
                         :for?    (:for sch)
                         :v       (get-cached ztx sch false)}))))]
    (fn key-schema-fn [vtx data opts]
      (let [key-rules
            (into {}
                  (keep (fn [{:keys [sch-key for? v]}]
                          (when (or (nil? for?)
                                    (contains? for? (get data key)))
                            [sch-key v])))
                  keys-schemas)]
        (loop [data (seq data)
               vtx* vtx]
          (if (empty? data)
            vtx*
            (let [[k v] (first data)]
              (recur (rest data)
                     (if (contains? key-rules k)
                       (-> (validation.utils/node-vtx&log vtx* [k] [k])
                           ((get key-rules k) v opts)
                           (validation.utils/merge-vtx vtx*))
                       vtx*))))))))))
