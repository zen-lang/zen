(ns zen.v2-validation
  (:require
   [clojure.set]
   [clojure.string :as str]
   [zen.utils :as utils]))

(defn pretty-type [x]
  (if-let [tp (type x)]
    (str/lower-case (last (str/split (str tp) #"\.")))
    "nil"))

(def types-cfg
  {'zen/string {:fn string?
                :to-str "string"}

   'zen/number {:fn number?
                :to-str "number"}

   'zen/set {:fn set?
             :to-str "set"}

   'zen/map map?

   'zen/vector {:fn vector?
                :to-str "vector"}

   'zen/boolean {:fn boolean?
                 :to-str "boolean"}

   'zen/keyword {:fn keyword?
                 :to-str "keyword"}

   'zen/list {:fn list?
              :to-str "list"}

   'zen/integer {:fn integer?
                 :to-str "integer"}

   'zen/symbol {:fn symbol?
                :to-str "symbol"}

   'zen/any (constantly true)
   'zen/case (constantly true)

   'zen/regex
   {:fn #(and (string? %) (re-pattern %))
    :to-str "regex"}})

(defmulti compile-key (fn [k ztx kfg] k))

(defmethod compile-key :default [k ztx kfg])

(defmulti compile-type-check (fn [tp ztx] tp))

(defn *compile-schema [ztx schema]
  (let [rulesets (->> (dissoc schema :zen/tags :zen/desc :zen/file :zen/name)
                      (map (fn [[k kfg]] (compile-key k ztx kfg)))
                      (reduce (fn [acc {w :when rs :rules}]
                                (update acc w into rs)) {}))]
    (fn [vtx data opts]
      (->> rulesets
           (reduce (fn [vtx [pred rules]]
                     (if (or (nil? pred) (pred data))
                       (->> rules
                            (reduce (fn [vtx r] (r (assoc vtx ::type (:type schema)) data opts))
                                    vtx))
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

(defn add-error [vtx err & rule-fn]
  (let [path* (or (:path err) (:path vtx))

        schema*
        (or (:schema err)
            (-> (:schema vtx)
                (into (:path vtx))
                (into rule-fn)))

        type*
        (if (not (contains? err :type))
          (if-let [type-str (get-in types-cfg [(::type vtx) :to-str])]
            (str  type-str "." (name (first rule-fn)))
            "primitive-type")
          (:type err))

        err*
        (-> err
            (assoc :path path*)
            (assoc :schema schema*)
            (assoc :type type*))]

    (update vtx :errors conj err*)))

(defn type-fn [sym]
  (let [type-cfg (get types-cfg sym)
        type-pred (if (fn? type-cfg) type-cfg (:fn type-cfg))]
    (fn [vtx data _]
      (if (type-pred data)
        vtx
        (let [error-msg
              {:message (str "Expected type of '" (or (:to-str type-cfg) sym)
                             ", got '" (pretty-type data))
               :schema (into (:schema vtx) (:path vtx))}]
          (add-error vtx error-msg :type))))))

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
(defmethod compile-type-check 'zen/regex [_ _] (type-fn 'zen/regex))
(defmethod compile-type-check 'zen/case [_ _] (type-fn 'zen/case))

(defmethod compile-key :type
  [_ ztx tp]
  {:rules [(compile-type-check tp ztx)]})

(defmethod compile-key :case
  [_ ztx cases]
  (let [vs (map (fn [{:keys [when then]}]
                  (cond-> {:when (get-cached ztx when)}
                    (not-empty then) (assoc :then (get-cached ztx then))))
                cases)]
    {:rules
     [(fn [vtx data opts]
        (loop [[{wh :when th :then :as v} & rest] vs
               item-idx 0]
          (if (nil? v)
            (add-error vtx
                       {:message (format "Expected one of the cases to be true")
                        :schema (conj (:schema vtx) :case)
                        :type "case"})
            (let [vtx*
                  {:errors []
                   :path (:path vtx)
                   :schema (into (:schema vtx) [:case item-idx :when])}
                  {errs :errors} (wh vtx* data opts)]
              (cond
                (and (empty? errs) th)
                (let [vtx*
                      {:errors []
                       :path (:path vtx)
                       :schema (into (:schema vtx) [:case item-idx :then])}]
                  (th vtx* data opts))

                (empty? errs) vtx

                :else (recur rest (inc item-idx)))))))]}))

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

(defmethod compile-key :min
  [_ ztx min]
  {:when number?
   :rules
   [(fn [vtx data opts]
      (if (< data min)
        (add-error vtx {:message (str "Expected >= " min ", got " data)} :min)
        vtx))]})

(defmethod compile-key :max
  [_ ztx max]
  {:when number?
   :rules
   [(fn [vtx data opts]
      (if (> data max)
        (add-error vtx {:message (str "Expected <= " max ", got " data)} :max)
        vtx))]})

(defmethod compile-key :minLength
  [_ ztx min-len]
  {:when string?
   :rules
   [(fn [vtx data opts]
      (if (< (count data) min-len)
        (add-error vtx {:message (str "Expected length >= " min-len ", got " (count data))}
                   :minLength)
        vtx))]})

(defmethod compile-key :maxLength
  [_ ztx max-len]
  {:when string?
   :rules
   [(fn [vtx data opts]
      (if (> (count data) max-len)
        (add-error vtx {:message (str "Expected length <= " max-len ", got " (count data))}
                   :maxLength)
        vtx))]})

(defmethod compile-key :minItems
  [_ ztx items-count]
  {:when #(or (sequential? %) (set? %))
   :rules
   [(fn [vtx data opts]
      (if (< (count data) items-count)
        (add-error vtx {:message (str "Expected >= " items-count ", got " (count data))}
                   :minItems)
        vtx))]})

(defmethod compile-key :maxItems
  [_ ztx items-count]
  {:when #(or (sequential? %) (set? %))
   :rules
   [(fn [vtx data opts]
      (if (> (count data) items-count)
        (add-error vtx {:message (str "Expected <= " items-count ", got " (count data))}
                   :maxItems)
        vtx))]})

;; TODO auto generate supported terms based on multi methods?
;; useful for introspection and documentation

(defmethod compile-key :const
  [_ ztx cfg]
  {:when map?
   :rules
   [(fn [vtx data opts]
      ;; TODO refactor on select-keys and set intersection for perf
      (->> (:value cfg)
           (reduce (fn [vtx* [k v]]
                     (if-not (and (contains? data k) (= (get data k) v))
                       (let [got-msg (if (nil? (get data k))
                                       {}
                                       {k (get data k)})
                             msg
                             {:message (str "Expected '" {k v} "', got '" got-msg "'")
                              :type "schema"
                              :path (:path vtx)
                              :schema (:schema vtx)}]
                         (add-error vtx* msg))
                       vtx*))
                   vtx)))]})

(defmethod compile-key :keys
  [_ ztx ks]
  {:when map?
   :rules
   (->> ks
        (mapv (fn [[k sch]]
                (let [v (get-cached ztx sch)]
                  (fn [vtx data opts]
                    (if-let [d (contains? data k)]
                      (-> (v (update vtx :path conj k)
                             (get data k)
                             opts)
                          (assoc :path (:path vtx)))
                      vtx))))))})

(defmethod compile-key :values
  [_ ztx sch]
  (let [v (get-cached ztx sch)]
    {:when map?
     :rules
     [(fn [vtx data opts]
        (reduce (fn [vtx* [key value]]
                  (let [{:keys [errors]}
                        (-> {:errors []
                             :path (conj (:path vtx) key)
                             :schema (conj (:schema vtx) :values)}
                            (v value opts))]
                    (update vtx* :errors into errors)))
                vtx data))]}))

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
                     :schema (-> (:schema vtx) (conj :every))}
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
        (add-error vtx {:path (:path vtx) :type "set"} :subset-of)
        vtx))]})

(defmethod compile-key :superset-of
  [_ ztx subset]
  {:when set?
   :rules
   [(fn [vtx data opts]
      (if-not (clojure.set/subset? subset data)
        (add-error vtx {:path (:path vtx) :type "set"} :superset-of)
        vtx))]})

(defmethod compile-key :regex
  [_ ztx regex]
  {:when string?
   :rules
   [(fn [vtx data opts]
      (if (not (re-find (re-pattern regex) data))
        (add-error vtx {:message (str "Expected match /" (str regex) "/, got \"" data "\"")}
                   :regex)
        vtx))]})

(defmethod compile-key :confirms
  [_ ztx ks]
  ;; TODO think about refactoring on function composition instead of reduce
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
  (let [one-of-fn
        (fn [vtx s]
          (let [reqs (->> (select-keys (::data vtx) s) (remove nil?))]
            (if (empty? reqs)
              ;; TODO add message
              (add-error vtx {:type "map.require"} :require)
              vtx)))

        all-keys-fn
        (fn [vtx mk]
          (add-error vtx
                     {:type "require"
                      :path (conj (:path vtx) mk)
                      :schema (conj (:schema vtx) :require)
                      :message (str mk " is required")}))]

    {:when map?
     :rules
     [(fn [vtx data opts]
        (->> (filter set? ks)
             (reduce one-of-fn (assoc vtx ::data data))))

      (fn [vtx data opts]
        (->> (-> (remove set? ks)
                 set
                 (clojure.set/difference (into #{} (keys data))))
             (reduce all-keys-fn vtx)))]}))

(defmethod compile-key :schema-key
  [_ ztx {sk :key sk-ns :ns sk-tags :tags}]
  {:when map?
   :rules
   [(fn [vtx data opts]
      (if-let [sch-nm (get data sk)]
        (let [sch-symbol (if sk-ns (symbol sk-ns (name sch-nm)) (symbol sch-nm))
              {tags :zen/tags :as sch} (utils/get-symbol ztx sch-symbol)]
          (cond
            (nil? sch)
            (add-error vtx {:message (str "Could not find schema " sch-symbol) :type "schema"})

            (not (contains? tags 'zen/schema))
            (add-error vtx {:message (str "'" sch-symbol " should be tagged with zen/schema, but " tags)
                            :type "schema"})

            (and sk-tags (not (clojure.set/subset? sk-tags tags)))
            (add-error vtx {:message (str "'" sch-symbol " should be tagged with " sk-tags ", but " tags)
                            :type "schema"})

            :else
            (apply (get-cached ztx sch) [(update vtx :schema into [:schema-key sch-symbol]) data opts])))
        vtx))]})

(defmethod compile-key :schema-index
  [_ ztx {si :index si-ns :ns}]
  {:when sequential?
   :rules
   [(fn [vtx data opts]
      (if-let [sch-nm (or (get data si) (nth data si))]
        (let [sch-symbol (if si-ns (symbol si-ns (name sch-nm)) sch-nm)
              sch (utils/get-symbol ztx sch-symbol)]
          (cond
            (nil? sch)
            (add-error vtx
                       {:message (format "Could not find schema %s" sch-symbol)
                        :type "schema"}
                       :schema-index)

            :else
            (apply (get-cached ztx sch)
                   [(update vtx :schema into [:schema-index sch-symbol]) data opts]))) vtx))]})

(defmethod compile-key :nth
  [_ ztx cfg]
  (let [schemas (map (fn [[index v]] [index (get-cached ztx v)]) cfg)]
    {:when sequential?
     :rules
     [(fn [vtx data opts]
        (reduce (fn [vtx* [index v]]
                  (if-let [nth-el (get data index)]
                    (let [{:keys [errors]}
                          (-> {:errors []
                               :path (conj (:path vtx) index)
                               :schema (conj (:schema vtx) :nth)}
                              (v nth-el opts))]
                      (update vtx* :errors into errors))
                    ;; TODO DISC do I need to raise an error here?
                    (update vtx* :errors conj {:message "nth element not found in provided collection"
                                               :path (conj (:path vtx*) index)
                                               :schema (conj (:schema vtx*) :nth)
                                               :type "schema"})))
                vtx schemas))]}))
