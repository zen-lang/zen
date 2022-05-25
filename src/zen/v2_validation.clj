(ns zen.v2-validation
  (:require
   [clojure.set :as cljset]
   [clojure.string :as str]
   [zen.utils :as utils]))

(defn pretty-type [x]
  (if-let [tp (type x)]
    (str/lower-case (last (str/split (str tp) #"\.")))
    "nil"))

(def fhir-date-regex
  "([0-9]([0-9]([0-9][1-9]|[1-9]0)|[1-9]00)|[1-9]000)(-(0[1-9]|1[0-2])(-(0[1-9]|[1-2][0-9]|3[0-1]))?)?")

(def fhir-datetime-regex
"([0-9]([0-9]([0-9][1-9]|[1-9]0)|[1-9]00)|[1-9]000)(-(0[1-9]|1[0-2])(-(0[1-9]|[1-2][0-9]|3[0-1])(T([01][0-9]|2[0-3]):[0-5][0-9]:([0-5][0-9]|60)(\\.[0-9]+)?(Z|(\\+|-)((0[0-9]|1[0-3]):[0-5][0-9]|14:00)))?)?)?")

(def types-cfg
  {'zen/string {:fn string?
                :to-str "string"}

   'zen/date
   {:fn #(and (string? %) (re-matches (re-pattern fhir-date-regex) %))
    :to-str "date"}

   'zen/datetime
   {:fn #(and (string? %) (re-matches (re-pattern fhir-datetime-regex) %))
    :to-str "datetime"}

   'zen/number {:fn number?
                :to-str "number"}

   'zen/set {:fn set?
             :to-str "set"}

   'zen/map {:fn map?
             :to-str "map"}

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

   ;; fn is implemented as a separate multimethod
   'zen/apply {:to-str "apply"}

   'zen/regex
   {:fn #(and (string? %) (re-pattern %))
    :to-str "regex"}})

(defmulti compile-key (fn [k ztx kfg] k))

(defmulti compile-type-check (fn [tp ztx] tp))

(defn *compile-schema [ztx schema]
  (let [node-type (:type schema)
        rulesets (->> (dissoc schema :zen/tags :zen/desc :zen/file :zen/name :validation-type)
                      (map (fn [[k kfg]] (compile-key k ztx kfg)))
                      (reduce (fn [acc {w :when rs :rules}]
                                (update acc w into rs)) {}))
        open-world? (or (:key schema) (:values schema) (= (:validation-type schema) :open))
        {valtype-pred :when valtype-rule :rule} (compile-key :validation-type ztx open-world?)]
    (fn [vtx data opts]
      (let [vtx*
            (reduce (fn [vtx [pred rules]]
                      (if (or (nil? pred) (pred data))
                        (->> rules
                             (reduce (fn [vtx r] (r (assoc vtx :type node-type)
                                                    data
                                                    opts))
                                     vtx))
                        vtx))
                    vtx
                    rulesets)]
        (if (valtype-pred data)
          (valtype-rule vtx* data opts)
          vtx*)))))

(defn get-cached [ztx schema]
  ;; TODO how performant is this call? maybe change to .hashCode
  (let [hash* (hash schema)
        v (get-in @ztx [:compiled-schemas hash*])]
    (cond
      (fn? v)
      v

      (true? (get-in @ztx [:visited-schemas hash*]))
      (fn [vtx data opts] vtx)

      :else
      (do
        (swap! ztx assoc-in [:visited-schemas hash*] true)
        (let [v (*compile-schema ztx schema)]
          (swap! ztx assoc-in [:compiled-schemas hash*] v)
          v)))))

(defn validate-schema [ztx schema data & [opts]]
  (let [v  (get-cached ztx schema)
        vtx {:errors []
             :path []
             :schema [(:zen/name schema)]
             :visited #{}
             :unknown-keys #{}}
        vtx* (v vtx data opts)
        unknown-errs
        (map (fn [path]
               {:path path
                :type "unknown-key"
                :message (str "unknown key " (last path))})
             (:unknown-keys vtx*))]

    (-> vtx*
        (update :errors #(vec (into % unknown-errs)))
        (select-keys [:errors :effects]))))

;; TODO support schemas seq as arg
(defn validate [ztx schemas data & [opts]]
  (validate-schema ztx (utils/get-symbol ztx (first schemas)) data))

(defn add-err [vtx sch-key err & data-path]
  (let [err-type
        (if (not (contains? err :type))
          (if-let [type-str (get-in types-cfg [(:type vtx) :to-str])]
            (str  type-str "." (name sch-key))
            "primitive-type")
          (:type err))

        err*
        (-> err
            (assoc :path (into (:path vtx) data-path))
            (assoc :type err-type)
            (assoc :schema (conj (:schema vtx) sch-key)))]
    (update vtx :errors conj err*)))

(defn add-fx [vtx sch-key fx & data-path]
  (let [fx*
        (-> fx
            (assoc :path (conj (:path vtx) sch-key)))]
    (update vtx :effects conj fx*)))

(defn node-vtx
  ([vtx sch-path]
   (node-vtx vtx sch-path []))
  ([vtx sch-path path]
   {:errors []
    :path (into (:path vtx) path)
    :unknown-keys (:unknown-keys vtx)
    :visited (:visited vtx)
    :schema (into (:schema vtx) sch-path)}))

(defn node-vtx&log [vtx sch-path path opts]
  {:errors []
   :path (into (:path vtx) path)
   :unknown-keys (:unknown-keys vtx)
   :schema (into (:schema vtx) sch-path)
   :visited (conj (:visited vtx) (into (:path vtx) path))})

(defn merge-vtx [*node-vtx global-vtx]
  (-> global-vtx
      (update :errors into (:errors *node-vtx))
      (assoc :visited (:visited *node-vtx))
      (assoc :unknown-keys (:unknown-keys *node-vtx))))

(defn update-vtx [vtx & keyvals]
  (merge vtx (apply hash-map keyvals)))

(defn type-fn [sym]
  (let [type-cfg (get types-cfg sym)
        type-pred (if (fn? type-cfg) type-cfg (:fn type-cfg))]
    (fn [vtx data _]
      (if (type-pred data)
        vtx
        (let [error-msg
              {:message (str "Expected type of '" (or (:to-str type-cfg) sym)
                             ", got '" (pretty-type data))}]
          (add-err vtx :type error-msg))))))

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
(defmethod compile-type-check 'zen/date [_ _] (type-fn 'zen/date))
(defmethod compile-type-check 'zen/datetime [_ _] (type-fn 'zen/datetime))

(defmethod compile-type-check 'zen/apply
  [tp ztx]
  (fn [vtx data opts]
    (cond
      (not (list? data))
      (add-err vtx :type {:message (str "Expected fn call '(fn-name args-1 arg-2), got '"
                                        (pretty-type data))})

      (not (symbol? (first data)))
      (add-err vtx :apply {:message (str "Expected symbol, got '" (first data))
                           :type "apply.fn-name"})

      :else
      (let [sch-sym (first data)
            {:keys [zen/tags args] :as sch} (utils/get-symbol ztx sch-sym)]
        (cond
          (nil? sch)
          (add-err vtx :apply {:message (str "Could not resolve fn '" sch-sym)
                               :type "apply.fn-name"})

          (not (contains? tags 'zen/fn))
          (add-err vtx :apply {:message (format "fn definition '%s should be tagged with 'zen/fn, but '%s" sch-sym tags)
                               :type "apply.fn-tag"})

          :else
          (let [v (get-cached ztx args)]
            (-> (node-vtx vtx [sch-sym :args])
                (v (rest data) opts)
                (merge-vtx vtx))))))))

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
            (add-err vtx
                     :case
                     {:message (format "Expected one of the cases to be true") :type "case"})
            (let [vtx* (wh (node-vtx vtx [:case item-idx :when]) data opts)]
              (cond
                (and (empty? (:errors vtx*)) th)
                (-> (merge-vtx vtx* vtx)
                    (node-vtx [:case item-idx :then])
                    (th  data opts)
                    (merge-vtx vtx))

                (empty? (:errors vtx*)) (merge vtx vtx*)

                :else (recur rest (inc item-idx)))))))]}))

(defmethod compile-key :enum
  [_ ztx values]
  (let [values* (set (map :value values))]
    {:rules
     [(fn [vtx data opts]
        (if-not (contains? values* data)
          (add-err vtx :enum {:message (str "Expected '" data "' in " values*) :type "enum"})
          vtx))]}))

(defmethod compile-key :min
  [_ ztx min]
  {:when number?
   :rules
   [(fn [vtx data opts]
      (if (< data min)
        (add-err vtx :min {:message (str "Expected >= " min ", got " data)})
        vtx))]})

(defmethod compile-key :max
  [_ ztx max]
  {:when number?
   :rules
   [(fn [vtx data opts]
      (if (> data max)
        (add-err vtx :max {:message (str "Expected <= " max ", got " data)})
        vtx))]})

(defmethod compile-key :minLength
  [_ ztx min-len]
  {:when string?
   :rules
   [(fn [vtx data opts]
      (if (< (count data) min-len)
        (add-err vtx
                 :minLength
                 {:message (str "Expected length >= " min-len ", got " (count data))})
        vtx))]})

(defmethod compile-key :maxLength
  [_ ztx max-len]
  {:when string?
   :rules
   [(fn [vtx data opts]
      (if (> (count data) max-len)
        (add-err vtx
                 :maxLength
                 {:message (str "Expected length <= " max-len ", got " (count data))})
        vtx))]})

(defmethod compile-key :minItems
  [_ ztx items-count]
  {:when #(or (sequential? %) (set? %))
   :rules
   [(fn [vtx data opts]
      (if (< (count data) items-count)
        (add-err vtx
                 :minItems
                 {:message (str "Expected >= " items-count ", got " (count data))})
        vtx))]})

(defmethod compile-key :maxItems
  [_ ztx items-count]
  {:when #(or (sequential? %) (set? %))
   :rules
   [(fn [vtx data opts]
      (if (> (count data) items-count)
        (add-err vtx
                 :maxItems
                 {:message (str "Expected <= " items-count ", got " (count data))})
        vtx))]})

(defmethod compile-key :const
  [_ ztx {:keys [value]}]
  {:rules
   [(fn [vtx data opts]
      (if (not= value data)
        (add-err vtx :const
                 {:message (str "Expected '" value "', got '" data "'")
                  :type "schema"})
        vtx))]})

(defmethod compile-key :keys
  [_ ztx ks]
  (let [key-rules
        (->> ks
             (map (fn [[k sch]]
                    [k (get-cached ztx sch)]))
             (into {}))]
    {:when map?
     :rules
     [(fn [vtx data opts]
        (reduce (fn [vtx* [k v]]
                  (cond
                    (not (contains? key-rules k))
                    (update vtx* :unknown-keys conj (conj (:path vtx) k))

                    :else
                    (-> (node-vtx&log vtx* [k] [k] opts)
                        ((get key-rules k) v opts)
                        (merge-vtx vtx*))))
                vtx
                data))]}))

(defmethod compile-key :values
  [_ ztx sch]
  (let [v (get-cached ztx sch)]
    {:when map?
     :rules
     [(fn [vtx data opts]
        (reduce (fn [vtx* [key value]]
                  (-> (node-vtx&log vtx* [:values] [key] opts)
                      (v value opts)
                      (merge-vtx vtx*)))
                vtx
                data))]}))

(defmethod compile-key :every
  [_ ztx sch]
  (let [v (get-cached ztx sch)]
    {:when #(or (sequential? %) (set? %))
     :rules
     [(fn [vtx data opts]
        (let [err-fn
              (fn [vtx [idx item]]
                (-> (node-vtx vtx [:every idx] [idx])
                    (v item (dissoc opts :indices))
                    (merge-vtx vtx)))

              data*
              (if-let [indices (not-empty (:indices opts))]
                (map vector indices data)
                (map-indexed vector data))]

          (reduce err-fn vtx data*)))]}))

(defmethod compile-key :subset-of
  [_ ztx superset]
  {:when set?
   :rules
   [(fn [vtx data opts]
      (if-not (clojure.set/subset? data superset)
        (add-err vtx :subset-of {:type "set"})
        vtx))]})

(defmethod compile-key :superset-of
  [_ ztx subset]
  {:when set?
   :rules
   [(fn [vtx data opts]
      (if-not (clojure.set/subset? subset data)
        (add-err vtx :superset-of {:type "set"})
        vtx))]})

(defmethod compile-key :regex
  [_ ztx regex]
  {:when string?
   :rules
   [(fn [vtx data opts]
      (if (not (re-find (re-pattern regex) data))
        (add-err vtx :regex
                 {:message (str "Expected match /" (str regex) "/, got \"" data "\"")})
        vtx))]})

(defmethod compile-key :confirms
  [_ ztx ks]
  (let [apply-fn
        (fn [[schema-name v] [vtx data opts]]
          (if (get-in vtx [:confirms (:path vtx) schema-name])
            (list vtx data opts)
            ;; to prevent infinite cycle of :confirms
            (-> (assoc-in vtx [:confirms (:path vtx) schema-name] true)
                (node-vtx [:confirms schema-name])
                (v data opts)
                (merge-vtx vtx)
                (list data opts))))

        comp-fn
        (->> ks
             (map #(utils/get-symbol ztx %))
             (map (fn [sch] (list (:zen/name sch) (get-cached ztx sch))))
             (map #(partial apply-fn %))
             (apply comp))]
    {:rules
     [(fn [vtx data opts]
        (-> (list vtx data opts)
            comp-fn
            first))]}))

(defmethod compile-key :require
  [_ ztx ks]
  (let [one-of-fn
        (fn [data vtx s]
          (let [reqs (->> (select-keys data s) (remove nil?))]
            (if (empty? reqs)
              (add-err vtx :require {:type "map.require"
                                     :message (str "one of keys " s " is required")})
              vtx)))

        all-keys-fn
        (fn [vtx mk]
          (add-err vtx
                   :require
                   {:type "require"
                    :message (str mk " is required")}
                   mk))]

    {:when map?
     :rules
     [(fn [vtx data opts]
        (->> (filter set? ks)
             (reduce (partial one-of-fn data) vtx)))

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
            (add-err vtx :schema-key
                     {:message (str "Could not find schema " sch-symbol)
                      :type "schema"})

            (not (contains? tags 'zen/schema))
            (add-err vtx :schema-key
                     {:message (str "'" sch-symbol " should be tagged with zen/schema, but " tags)
                      :type "schema"})

            (and sk-tags (not (clojure.set/subset? sk-tags tags)))
            (add-err vtx :schema-key
                     {:message (str "'" sch-symbol " should be tagged with " sk-tags ", but " tags)
                      :type "schema"})

            :else
            (let [v (get-cached ztx sch)]
              (-> (node-vtx vtx [:schema-key sch-symbol])
                  (v data opts)
                  (merge-vtx vtx)))))
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
            (add-err vtx
                     :schema-index
                     {:message (format "Could not find schema %s" sch-symbol)
                      :type "schema"})

            :else
            (let [v (get-cached ztx sch)]
              (-> (node-vtx vtx [:schema-index sch-symbol])
                  (v data opts)
                  (merge-vtx vtx)))))
        vtx))]})

(defmethod compile-key :nth
  [_ ztx cfg]
  (let [schemas (map (fn [[index v]] [index (get-cached ztx v)]) cfg)]
    {:when sequential?
     :rules
     [(fn [vtx data opts]
        (reduce (fn [vtx* [index v]]
                  (if-let [nth-el (get data index)]
                    (-> (node-vtx vtx* [:nth index] [index])
                        (v nth-el opts)
                        (merge-vtx vtx*))
                    vtx*))
                vtx
                schemas))]}))

(defmethod compile-key :keyname-schemas
  [_ ztx {:keys [tags]}]
  {:rules
   [(fn [vtx data opts]
      (let [rule-fn
            (fn [vtx* [schema-key data*]]
              (if-let [sch (and (qualified-ident? schema-key) (utils/get-symbol ztx (symbol schema-key)))]
                ;; TODO add test on nil case
                (if (or (nil? tags)
                        (clojure.set/subset? tags (:zen/tags sch)))
                  (-> (node-vtx&log vtx* [:keyname-schemas schema-key] [schema-key] opts)
                      ((get-cached ztx sch) data* opts)
                      (merge-vtx vtx*))
                  vtx*)
                vtx*))]
        (reduce rule-fn vtx data)))]})

(defmethod compile-key :default [schema-key ztx sch-params]
  ;; it is assumed that if no compile key impl found then effect is emitted
  (if (qualified-ident? schema-key)
    (let [{:keys [zen/tags] :as sch} (utils/get-symbol ztx (symbol schema-key))]
      {:rules
       [(fn [vtx data opts]
          (if (contains? tags 'zen/schema-fx)
            (add-fx vtx (:zen/name sch)
                    {:name (:zen/name sch)
                     :params sch-params
                     :data data})
            vtx))]})
    {:rules [(fn [vtx data opts] vtx)]}))

(defn is-exclusive? [group data]
  (->> group
       (filter #(->> (if (set? %) % #{%})
                     (select-keys data)
                     seq))
       (bounded-count 2)
       (> 2)))

(defmethod compile-key :exclusive-keys
  [_ ztx groups]
  (let [err-fn
        (fn [group [vtx data]]
          (if (is-exclusive? group data)
            (list vtx data)
            (let [err-msg
                  (format "Expected only one of keyset %s, but present %s"
                          (str/join " or " group)
                          (keys data))
                  vtx*
                  (add-err vtx :exclusive-keys
                           {:message err-msg
                            :type "map.exclusive-keys"})]
              (list vtx* data))))

        comp-fn
        (->> groups
             (map #(partial err-fn %))
             (apply comp))]

    {:rules
     [(fn [vtx data opts]
        (-> (list vtx data)
            comp-fn
            first))]}))

(defmethod compile-key :key
  [_ ztx sch]
  (let [v (get-cached ztx sch)]
    {:rules
     [(fn [vtx data opts]
        (reduce (fn [vtx* [k _]]
                  (-> (node-vtx&log vtx* [:key] [k] opts)
                      (v k opts)
                      (merge-vtx vtx*)))
                vtx
                data))]}))

(defmethod compile-key :tags
  [_ ztx sch-tags]
  {:when #(or (symbol? %) (list? %))
   :rules
   [(fn [vtx data opts]
      (let [sym (if (list? data) (first data) data)
            {:keys [zen/tags] :as sch} (utils/get-symbol ztx sym)]
        (if (not (clojure.set/superset? tags sch-tags))
          (add-err vtx :tags
                   {:message (format "Expected symbol '%s tagged with '%s, but only %s"
                                     (str sym) (str sch-tags) (or tags #{}))
                    ;; currently :tags implements two different usecases:
                    ;; schema apply and schema tags check
                    :type (if (list? data) "apply.fn-tag" "symbol")})
          vtx)))]})

(defmethod compile-key :slicing
  [_ ztx {slices :slices rest-schema :rest}]
  (let [schemas
        (->> slices
             (map (fn [[slice-name {:keys [schema]}]]
                    [slice-name (get-cached ztx schema)]))
             (into {}))

        rest-fn
        (when (not-empty rest-schema)
          (get-cached ztx rest-schema))

        slice-fns
        (map (fn [[slice-name slice-schema]]
               ;; TODO support other slicing engines
               (if-let [v (get-cached ztx (get-in slice-schema [:filter :zen]))]
                 (fn [vtx [idx el] opts]
                   (let [vtx* (v (node-vtx vtx [:slicing] [idx]) el opts)]
                     (when (empty? (:errors vtx*))
                       slice-name)))
                 (constantly nil)))
             slices)

        slices-templ
        (->> slices
             (map (fn [[slice-name _]]
                    [slice-name []]))
             (into {}))

        err-fn
        (fn [opts vtx [slice-name slice]]
          (cond
            (and (= slice-name :slicing/rest) (empty? rest-schema))
            vtx

            :else
            (let [v (if (= slice-name :slicing/rest)
                      rest-fn
                      (get schemas slice-name))

                  append-slice-path
                  (fn [p]
                    (let [prev-path (:path vtx)]
                      (-> (conj prev-path (str "[" slice-name "]"))
                          (concat (drop (count prev-path) p))
                          vec)))]
              (-> (node-vtx vtx [:slicing slice-name])
                  (v (mapv second slice)
                     (assoc opts :indices (map first slice)))
                  (update :errors (fn [errs] (map #(update % :path append-slice-path) errs)))
                  (merge-vtx vtx)))))]

    {:when sequential?
     :rules
     [(fn [vtx data opts]
        (->> data
             (map-indexed vector)
             (group-by (fn [indexed-el]
                         (or (some #(apply % [vtx indexed-el opts]) slice-fns)
                             :slicing/rest)))
             (merge slices-templ)
             (reduce (partial err-fn opts) vtx)))]}))

(defn cur-keyset [vtx data opts]
  (->> (keys data)
       (map #(conj (:path vtx) %))
       set))

(defmethod compile-key :validation-type
  [_ ztx open-world?]
  {:when map?
   :rule
   (fn [vtx data opts]
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
                     (cljset/difference (cur-keyset vtx data opts)
                                        (:visited vtx))

                     (and empty-unknown? empty-visited?)
                     (set (cur-keyset vtx data opts))

                     (not empty-unknown?)
                     (cljset/difference unknown (:visited vtx)))))]

       (if open-world?
         (update vtx :unknown-keys filter-allowed)
         (update vtx :unknown-keys set-unknown))))})
