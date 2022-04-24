(ns zen.v2-validation
  (:require [clojure.set]
            [zen.utils :as utils]))

(defmulti compile-key (fn [k ztx kfg] k))

(defmethod compile-key :default [k ztx kfg])

(defmulti compile-type-check (fn [tp ztx] tp))

(defmulti effective-key (fn [k ztx kfg] k))
(defmulti merge-key (fn [k ztx kfg] k))

(defn validate-identity [vtx & _] vtx)

(defn add-error [vtx err]
  (update vtx :errors (fn [x] (conj (or x []) (assoc err :path (:path vtx))))))

(defn *compile-schema [ztx schema]
  (let [rulesets (->> (dissoc schema :zen/tags :zen/desc :zen/file :zen/name)
                      (mapv (fn [[k kfg]] (compile-key k ztx kfg)))
                      (reduce (fn [acc {w :when rs :rules}]
                                (update acc w into rs)) {}))]
    (fn [vtx data opts]
      (->> rulesets
           (reduce (fn [vtx [pred rules]]
                     (if (or (nil? pred) (pred data))
                       (->> rules
                            (reduce (fn [vtx r] (r vtx data opts)) vtx))
                       vtx))
                   vtx)))))

;; TODO precompile schemas tagged with specific tag on ns load
(defn get-cached [ztx schema]
  (let [sh (hash schema)]
    (or (get-in @ztx [:compiled-schemas sh])
        (let [v (*compile-schema ztx schema)]
          (swap! ztx assoc-in [:compiled-schemas sh] v)
          v))))

(defn validate-schema [ztx schema data & [opts]]
  (let [v  (get-cached ztx schema)]
    (-> (v {:errors [] :path [] :schema [(:zen/name schema)]} data opts)
        (select-keys [:errors :effects]))))

;; TODO support schemas seq as arg
;; TODO convert errors to vector here?
(defn validate [ztx schemas data & [opts]]
  (let [sch (utils/get-symbol ztx (first schemas))]
    (-> (validate-schema ztx sch data)
        (update :errors #(sort-by :path %)))))

(defmethod compile-type-check 'zen/string
  [_ _]
  (fn [vtx data _]
    (if (string? data)
      vtx
      (add-error vtx {:message (str "Expected type 'zen/string got " (type data))}))))

(defmethod compile-type-check 'zen/number
  [_ _]
  (fn [vtx data _]
    (if (number? data)
      vtx
      (add-error vtx {:message "Expected number" :type :type}))))

(defmethod compile-type-check 'zen/set
  [_ _]
  (fn [vtx data _]
    (if (set? data)
      vtx
      (add-error vtx {:message (str "Expected type 'zen/set got " (type data))}))))

(defn check-map? [vtx data _]
  (if (map? data) vtx
      (add-error vtx {:message "Expected map" :type :type})))

(defmethod compile-type-check 'zen/map [_ _] check-map?)

(defn check-vector? [vtx data _]
  (if (vector? data)
    vtx (add-error vtx {:message "Expected vector" :type :type})))

(defmethod compile-type-check 'zen/vector [_ _] check-vector?)

(defn check-boolean? [vtx data opts]
  (if-not (boolean? data)
    (add-error vtx {:type "primitive-type"
                    :schema (into (:schema vtx) (:path vtx))
                    :message (str "Expected zen/boolean, got " (type data))})
    vtx))

(defmethod compile-type-check 'zen/boolean [_ _] check-boolean?)

(defmethod compile-key :type
  [_ ztx tp]
  {:rules [(compile-type-check tp ztx)]})

(defmethod compile-key :minLength
  [_ ztx tp]
  {:when string? :rules [(fn minLength [vtx data opts] vtx)]})

(defmethod compile-key :maxLength
  [_ ztx ml]
  {:when string?
   :rules [(fn maxLength [vtx data opts]
             (if (> (count data) ml)
               (add-error vtx {:message "Longer than"})
               vtx))]})

(defmethod compile-key :keys
  [_ ztx ks]
  {:when map?
   :rules (->> ks
               (mapv (fn [[k sch]]
                       ;; TODO take from cache instead of compile
                       (let [v (*compile-schema ztx sch)]
                         (fn [vtx data opts]
                           (if-let [d (get data k)]
                             (-> (v (update vtx :path conj k) d opts)
                                 (assoc :path (:path vtx)))
                             vtx))))))})

(defmethod compile-key :confirms
  [_ ztx ks]
  (let [schemas
        (->> ks
             (map #(utils/get-symbol ztx %))
             (map (fn [sch] [(:zen/name sch) (get-cached ztx sch)])))]
    {:when map?
     :rules
     [(fn [vtx data opts]
        (reduce (fn [vtx* [schema-name schema]]
                  (update vtx* :errors into
                          (-> {:errors [] :path (:path vtx)
                               :schema (into (:schema vtx) [:confirms schema-name])}
                              (schema data opts)
                              :errors)))
                vtx schemas))]}))

(defmethod compile-key :require
  [_ ztx ks]
  (let [set-rule-fn
        (fn [vtx s]
          (let [reqs (->> (select-keys (::data vtx) s) (remove nil?))
                pth
                (-> (:schema vtx)
                    (into (:path vtx))
                    (conj :require))]
            (if (empty? reqs)
              (add-error vtx {:type "map.require" :schema pth})
              vtx)))

        key-rule-fn
        (fn [vtx mk]
          ;; TODO fix path for nested req
          (-> (assoc vtx :path [mk])
              (add-error {:type "require"
                          :schema (conj (:schema vtx) :require)})))]

    {:when map?
     :rules
     [(fn [vtx data opts]
        (->> (filter set? ks)
             (reduce set-rule-fn (assoc vtx ::data data))))

      (fn [vtx data opts]
        (->> (-> (remove set? ks)
                 set
                 (clojure.set/difference (into #{} (keys data))))
             (reduce key-rule-fn vtx)))]}))

