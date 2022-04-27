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
        #_(update :errors #(sort-by :path %)))))

;; TODO redesign type errors after v2 release, they are not consistent in v1
(def types-cfg
  {'zen/string {:fn string?
                :type-error "string.type"}
   'zen/number number?
   'zen/set {:fn set?
             :type-error "type"}
   'zen/map map?
   'zen/vector vector?
   'zen/boolean boolean?
   'zen/keyword keyword?
   'zen/list list?
   'zen/integer integer?
   'zen/symbol symbol?
   'zen/any (constantly true)})

(defn type-fn [sym]
  (let [type-cfg (get types-cfg sym)
        type-pred (if (fn? type-cfg) type-cfg (:fn type-cfg))]
    (fn [vtx data _]
      (if (type-pred data)
        vtx
        (add-error vtx {:message (str "Expected type " sym " got " (type data))
                        :type (or (:type-error type-cfg) "primitive-type")
                        :schema (into (:schema vtx) (:path vtx))})))))

(defmethod compile-type-check 'zen/string [_ _] (type-fn 'zen/string))
(defmethod compile-type-check 'zen/number [_ _] (type-fn 'zen/number))
(defmethod compile-type-check 'zen/set [_ _] (type-fn 'zen/set))
(defmethod compile-type-check 'zen/map [_ _] (type-fn 'zen/map))
(defmethod compile-type-check 'zen/vector [_ _] (type-fn 'zen/vector))
(defmethod compile-type-check 'zen/boolean [_ _] (type-fn 'zen/boolean))
(defmethod compile-type-check 'zen/list [_ _] (type-fn 'zen/list))
(defmethod compile-type-check 'zen/keyword [_ _] (type-fn 'zen/keyword))
(defmethod compile-type-check 'zen/any [_ _] (type-fn 'zen/any))
(defmethod compile-type-check 'zen/integer [_ _] (type-fn 'zen/integer))
(defmethod compile-type-check 'zen/symbol [_ _] (type-fn 'zen/symbol))

(defmethod compile-key :type
  [_ ztx tp]
  {:rules [(compile-type-check tp ztx)]})

(defmethod compile-key :enum
  [_ ztx values]
  (let [values* (set (map :value values))]
    {:rules
     [(fn [vtx data opts]
        (if-not (contains? values* data)
          (add-error vtx {:message (str "Expected '" data "' in " values*)
                          :type "enum"
                          :schema (-> (:schema vtx) (into (:path vtx)))})
          vtx))]}))

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

(defmethod compile-key :minItems
  [_ ztx items-count]
  {:when #(or (sequential? %) (set? %))
   :rules
   [(fn [vtx data opts]
      (if (< (count data) items-count)
        (add-error vtx {:message (str "Expected >= " items-count ", got " (count data))
                        ;; TODO fix the types here
                        :type "list"
                        :schema (-> (:schema vtx)
                                    (into (:path vtx))
                                    (conj :minItems))})
        vtx))]})

(defmethod compile-key :maxItems
  [_ ztx items-count]
  {:when #(or (sequential? %) (set? %))
   :rules
   [(fn [vtx data opts]
      (if (> (count data) items-count)
        (add-error vtx {:message (str "Expected <= " items-count ", got " (count data))
                        :type "list"
                        ;; TODO maybe move schema to add error fn?
                        :schema (-> (:schema vtx)
                                    (into (:path vtx))
                                    (conj :maxItems))})
        vtx))]})

;; TODO think about better message generation
;; maybe add regex match on message to tests

(defmethod compile-key :const
  [_ ztx cfg]
  {:when map?
   :rules
   [(fn [vtx data opts]
      ;; TODO refactor on select-keys and set intersection for perf
      (->> (:value cfg)
           (reduce (fn [vtx* [k v]]
                     (if-not (and (contains? data k) (= (get data k) v))
                       (add-error vtx*
                                  {:message (str "Expected '" {k v} "', got '" {k (get data k)} "'")
                                   :type "schema"
                                   :path (:path vtx)
                                   :schema (:schema vtx)})
                       vtx*))
                   vtx)))]})

(defmethod compile-key :keys
  [_ ztx ks]
  {:when map?
   :rules
   (->> ks
        (mapv (fn [[k sch]]
                ;; TODO take from cache instead of compile
                (let [v (*compile-schema ztx sch)]
                  (fn [vtx data opts]
                    (if-let [d (contains? data k)]
                      (-> (v (update vtx :path conj k) (get data k) opts)
                          (assoc :path (:path vtx)))
                      vtx))))))})

(defmethod compile-key :every
  [_ ztx sch]
  (let [v (get-cached ztx sch)]
    {:when #(or (sequential? %) (set? %))
     :rules
     [(fn [vtx data opts]
        (let [err-fn
              (fn [idx item]
                (-> {:errors []
                     :path (conj (:path vtx) idx)
                     :schema (-> (:schema vtx) (into (:path vtx)) (conj :every))}
                    (v item opts)))]
          (->> (map-indexed err-fn data)
               (reduce (fn [vtx* item]
                         (update vtx* :errors into (:errors item)))
                       vtx))))]}))

(defmethod compile-key :subset-of
  [_ ztx superset]
  {:when set?
   :rules
   [(fn [vtx data opts]
      (if-not (clojure.set/subset? data superset)
        (add-error vtx {:path (:path vtx)
                        :type "set"
                        :schema
                        (-> (into (:schema vtx) (:path vtx))
                            (conj :subset-of))})
        vtx))]})

(defmethod compile-key :superset-of
  [_ ztx subset]
  {:when set?
   :rules
   [(fn [vtx data opts]
      (if-not (clojure.set/subset? subset data)
        (add-error vtx {:path (:path vtx)
                        :type "set"
                        :schema (-> (into (:schema vtx) (:path vtx))
                                    (conj :superset-of))})
        vtx))]})

(defmethod compile-key :regex
  [_ ztx regex]
  {:when string?
   :rules
   [(fn [vtx data opts]
      (if (not (re-find (re-pattern regex) data))
        (add-error vtx {:type "string.regex"
                        :path (:path vtx)
                        :schema (conj (:schema vtx) :regex)})
        vtx))]})

(defmethod compile-key :confirms
  [_ ztx ks]
  (let [vs
        (->> ks
             (map #(utils/get-symbol ztx %))
             (map (fn [sch] [(:zen/name sch) (get-cached ztx sch)])))]
    {:when map?
     :rules
     [(fn [vtx data opts]
        (reduce (fn [vtx* [schema-name v]]
                  (let [result
                        (-> {:errors [] :path (:path vtx)
                             :schema (into (:schema vtx) [:confirms schema-name])}
                            (v data opts))]
                    (update vtx* :errors into (:errors result))))
                vtx vs))]}))

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

