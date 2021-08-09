(ns zen.validation
  (:require [clojure.set]
            [zen.effect]
            [zen.match]
            [zen.test-utils :refer [vmatch match valid valid-schema! invalid-schema]]
            [clojure.string :as str]))

(defn get-symbol [ctx nm]
  (get-in @ctx [:symbols nm]))

(defn update-acc [ctx acc {dp :path sp :schema}]
  (cond-> acc
    dp (update :path into dp)
    sp (update :schema into sp)))

(defn add-error [ctx acc err & [upd-acc]]
  (update acc :errors conj (merge err (select-keys (update-acc ctx acc upd-acc) [:path :schema]))))

(defn new-validation-acc []
  {:errors []
   :warnings []
   :schema []
   :path []
   :effects []})

(defn pretty-type [x]
  (if-let [tp (type x)]
    (str/lower-case (last (str/split (str tp) #"\.")))
    "nil"))

(declare validate-node)

(defmulti validate-type (fn [tp & _] tp))

(defn restore-acc [acc old-acc]
  (merge acc (select-keys old-acc [:path :schema])))

(defn resolve-property [ctx k]
  (when (qualified-ident? k)
    (let [sym (symbol (namespace k) (name k))]
      (when (contains? (get-in @ctx [:tags 'zen/property]) sym)
        (get-symbol ctx sym)))))

(defn resolve-keyname-schema [ctx keyname-schemas schema-key]
  (when-let [prop-sch (and (seq keyname-schemas)
                           (qualified-ident? schema-key)
                           (get-symbol ctx (symbol schema-key)))]
    (when (or (nil? (:tags keyname-schemas))
              (clojure.set/subset? (:tags keyname-schemas) (:zen/tags prop-sch)))
      prop-sch)))

(defn is-exclusive? [group data]
  (->> group
       (filter #(->> (if (set? %) % #{%})
                     (select-keys data)
                     seq))
       (bounded-count 2)
       (> 2)))

(defmethod validate-type 'zen/map
  [_ ctx acc {ks :keys
              ky :key
              vls :values
              {sk :key sk-ns :ns sk-tags :tags} :schema-key
              reqs :require
              kns :keyname-schemas ;; TODO
              eks :exclusive-keys
              validation-type :validation-type
              :or {validation-type :closed}
              :as sch} data]
  (if (map? data)
    (let [ignore-unknown-keys (or ky vls (= :open validation-type))
          acc (if ignore-unknown-keys
                (update acc :keys
                        (fn [k] (reduce (fn [acc v] (assoc acc v #{(:schema acc)}))
                                        (or k {})
                                        (mapv #(conj (:path acc) %) (keys data)))))
                acc)
          acc (->> data
                   (reduce (fn [acc [k v]]
                             (if-let [fail (get-in ks [k :fail])]
                               (add-error ctx acc
                                          {:message fail}
                                          {:schema [:fail]})
                               (let [acc (if-let [sch (get ks k)]
                                           (let [acc' (validate-node ctx (update-acc ctx acc {:path [k] :schema [:keys k]}) sch v)
                                                 acc' (update-in acc' [:keys (conj (:path acc) k)] (fnil conj #{}) (:schema acc))]
                                             (restore-acc acc' acc))
                                           (if-let [prop-sch (or (resolve-property ctx k)
                                                                 (resolve-keyname-schema ctx kns k))]
                                             (-> (validate-node ctx (update-acc ctx acc {:path [k]
                                                                                         :schema (if kns [:keyname-schemas k] [:property k])}) prop-sch v)
                                                 (restore-acc acc)
                                                 (update-in [:keys (conj (:path acc) k)] (fnil conj #{}) (:schema acc)))
                                             (if ignore-unknown-keys
                                               acc
                                               (update-in acc [:keys (conj (:path acc) k)] #(or % #{})))))
                                     acc (if vls
                                           (-> (validate-node ctx (update-acc ctx acc {:schema [:values] :path [k]}) vls v)
                                               (restore-acc acc))
                                           acc)
                                     acc (if ky
                                           (-> (validate-node ctx (update-acc ctx acc {:schema [:key] :path [k]}) ky k)
                                               (restore-acc acc))
                                           acc)]
                                 acc)))
                           acc))
          acc (->> reqs
                   (reduce (fn [acc k]
                             (if (set? k)
                               (if (empty? (->> (select-keys data k) (remove nil?)))
                                 (add-error ctx acc
                                            {:message (format "one of keys %s is required" k) :type "require"}
                                            {:schema [:require]})
                                 acc)
                               (if (nil? (get data k))
                                 (add-error ctx acc
                                            {:message (format "%s is required" k) :type "require"}
                                            {:path [k] :schema [:require]})
                                 acc)))
                           acc))
          acc (if eks
                (-> (->> eks
                         (reduce (fn [acc group]
                                   (if (is-exclusive? group data)
                                     acc
                                     (add-error ctx acc {:message (format "Expected only one of keyset %s, but present %s" (str/join " or " group) (keys data))
                                                         :type "map.exclusive-keys"}
                                                {:schema [:exclusive-keys]})))
                                 acc))
                    (restore-acc acc))
                acc)

          acc (if-let [nm (and sk (get data sk))]
                (let [sch-nm (if sk-ns
                               (symbol sk-ns (name nm))
                               (cond (string? nm) (symbol nm)
                                     (symbol? nm) nm
                                     :else (assert false (str "Expected symbol or string " nm ", but got " (type nm)))))]
                  (if-let [{tags :zen/tags :as sch} (and sch-nm (get-symbol ctx sch-nm))]
                    (if (contains? tags 'zen/schema)
                      (if (and sk-tags (not (clojure.set/subset? sk-tags tags)))
                        (add-error ctx acc {:message (format "'%s should be tagged with %s, but %s" sch-nm sk-tags tags) :type "schema"})
                        (-> (validate-node ctx (update-acc ctx acc {:schema [:schema-key sch-nm]}) sch data)
                            (restore-acc acc)))
                      (add-error ctx acc {:message (format "'%s should be tagged with zen/schema, but %s" sch-nm tags) :type "schema"}))
                    (add-error ctx acc {:message (format "Could not find schema %s" sch-nm) :type "schema"})))
                acc)]
      acc)
    (add-error ctx acc {:message (format "Expected type of 'map, got %s" (pr-str data))  :type "type"})))

(defmulti slicing-filter (fn [ctx slicing-definition data] (:engine slicing-definition)))

(defmethod slicing-filter :zen [ctx {schema :zen} data]
  (empty? (:errors (validate-node ctx (new-validation-acc) schema data))))

(defmethod slicing-filter :zen-fx [ctx {schema :zen} data]
  (let [pure-validation (validate-node ctx (new-validation-acc) schema data)]
    (empty? (:errors (zen.effect/apply-fx ctx pure-validation data)))))

(defmethod slicing-filter :matcho [ctx {:keys [matcho]} data]) ;; TODO

(defn determine-slice [ctx slices data]
  (for [[slice-name slice-definition] slices
        :when (slicing-filter ctx (:filter slice-definition) data)]
    slice-name))

(defn slice [ctx slicing coll]
  (reduce (fn [coll-slices [idx coll-el]]
            (->> (or (not-empty (determine-slice ctx (:slices slicing) coll-el))
                     [:slicing/rest])
                 (into {} (map (fn [slice-name] [slice-name ^:slice{idx coll-el}])))
                 (merge-with merge coll-slices)))
          {}
          (if (:slice (meta coll))
            coll
            (map-indexed vector coll))))

(declare validate-collection)

(defn append-slice-name-to-error-path [old-path slice-name error]
  (let [error-sub-path (drop (count old-path) (:path error))
        path           (vec (concat old-path
                                    [(str "[" slice-name "]")]
                                    error-sub-path))]
    (assoc error :path path)))

(defn process-slicing-errors-paths
  "Appends slice names to errors paths
   We can't modify (:path acc) when traversing into slices because
   paths are shared between :every and :slices and
   keys validation relies on paths collisions"
  [path slice-name old-errors cur-errors]
  (->> cur-errors
       (drop (count old-errors))
       (mapv (partial append-slice-name-to-error-path path slice-name))
       (into old-errors)))

(defn validate-slice [ctx acc slice-name slice-schema slice-coll]
  (let [slice-validation-result (validate-node ctx (update-acc ctx acc {:schema [slice-name]}) slice-schema slice-coll)
        errors (process-slicing-errors-paths (:path acc) slice-name (:errors acc) (:errors slice-validation-result))]
    (restore-acc (assoc slice-validation-result :errors errors)
                 acc)))

(defn validate-slicing [ctx acc slicing coll]
  (let [slicing-acc (update-acc ctx acc {:schema [:slicing]})
        sliced-coll (slice ctx slicing coll)
        result-acc  (reduce (fn [acc' [slice-name {slice-schema :schema}]]
                              (let [slice (get sliced-coll slice-name [])]
                                (validate-slice ctx acc' slice-name slice-schema slice)))
                            (if (and (contains? slicing :rest) (contains? sliced-coll :slicing/rest))
                              (validate-slice ctx slicing-acc :slicing/rest (:rest slicing) (:slicing/rest sliced-coll))
                              slicing-acc)
                            (dissoc (:slices slicing) :slicing/rest))]
    (restore-acc result-acc acc)))

(defn validate-collection
  [type ctx acc {{si :index si-ns :ns} :schema-index mn :minItems mx :maxItems, :as schema} data]
  (let [need-to-traverse-collection? (or (:every schema) (:nth schema))
        acc (if need-to-traverse-collection?
              (->> (if (:slice (meta data))
                     data
                     (map-indexed vector data))
                   (reduce (fn [acc' [idx coll-el]]
                             (cond-> acc'
                               (:every schema)
                               (as-> $
                                 (update-acc ctx $ {:path [idx] :schema [:every]})
                                 (validate-node ctx $ (:every schema) coll-el)
                                 (restore-acc $ acc))

                               (and (:nth schema) (get (:nth schema) idx))
                               (as-> $
                                 (update-acc ctx $ {:path [idx] :schema [:nth idx]})
                                 (validate-node ctx $ (get (:nth schema) idx) coll-el)
                                 (restore-acc $ acc))))
                           acc))
              acc)

        acc (if (:slicing schema)
              (validate-slicing ctx acc (:slicing schema) data)
              acc)

        cnt (count data)
        acc (if (and mn (< cnt mn))
              (add-error ctx acc {:message (format "Expected >= %s, got %s" mn cnt) :type type}
                         {:schema [:minItems]})
              acc)
        acc (if (and mx (> cnt mx))
              (add-error ctx acc {:message (format "Expected <= %s, got %s" mx cnt) :type type}
                         {:schema [:maxItems]})
              acc)

        acc (if-let [nm (and si (or (get data si nil) (nth data si nil)))]
              (let [sch-nm (if si-ns (symbol si-ns (name nm)) nm)]
                (if-let [sch (and sch-nm (get-symbol ctx sch-nm))]
                  (-> (validate-node ctx (update-acc ctx acc {:schema [:schema-index sch-nm]}) sch data)
                      (restore-acc acc))
                  (add-error ctx acc
                             {:message (format "Could not find schema %s" sch-nm) :type "schema"}
                             {:schema [:schema-index]})))
              acc)]
    acc))

(defmethod validate-type 'zen/vector
  [_ ctx acc schema data]
  (if (or (sequential? data) (:slice (meta data)))
    (validate-collection "vector" ctx acc schema data)
    (add-error ctx acc {:message (format "Expected type of 'vector, got '%s" (pretty-type data))  :type "type"})))

(defmethod validate-type 'zen/list
  [_ ctx acc schema data]
  (if (list? data)
    (validate-collection "list" ctx acc schema data)
    (add-error ctx acc {:message (format "Expected type of 'list, got '%s" (pretty-type data))  :type "type"})))

(defmethod validate-type 'zen/set
  [_ ctx acc {:as schema, evr :every, mn :minItems, mx :maxItems} data]
  (if (or (set? data) (sequential? data))
    (let [acc (if evr
                (->
                 (loop [acc (update-acc ctx acc {:schema [:every]}), idx 0, [d & ds] data]
                   (if (and (nil? d) (empty? ds))
                     acc
                     (recur
                      (-> (validate-node ctx (update-acc ctx acc {:path [idx]}) evr d)
                          (restore-acc acc))
                      (inc idx) ds)))
                 (restore-acc acc))
                acc)
          cnt (count data)
          acc (if (and mn (< cnt mn))
                (add-error ctx acc {:message (format "Expected >= %s, got %s" mn cnt) :type "vector"}
                           {:schema [:minItems]})
                acc)
          acc (if (and mx (> cnt mx))
                (add-error ctx acc {:message (format "Expected <= %s, got %s" mx cnt) :type "vector"}
                           {:schema [:maxItems]})
                acc)
          acc (if (and (:subset-of schema)
                       (not (clojure.set/subset?
                              data
                              (:subset-of schema))))
                (add-error ctx acc {:message (format "Expected %s to be a subset of %s" data (apply sorted-set (:subset-of schema))) :type "set"}
                           {:schema [:subset-of]})
                acc)
          acc (if (and (:superset-of schema)
                       (not (clojure.set/superset?
                              data
                              (:superset-of schema))))
                (add-error ctx acc {:message (format "Expected %s to be a superset of %s" data (apply sorted-set (:superset-of schema))) :type "set"}
                           {:schema [:superset-of]})
                acc)]
      acc)
    (add-error ctx acc {:message (format "Expected type of 'set, got '%s" (pretty-type data))  :type "type"})))


(defmethod validate-type 'zen/any
  [_ _ctx acc _sch _data]
  acc)

(defmethod validate-type 'zen/case
  [_ ctx acc {case :case} data]
  (loop [[{wh :when th :then :as sch} & us] case
         idx 0]
    (if (nil? sch)
      (add-error ctx acc {:message (format "Expected one of %s, but none is conformant" (pr-str (map :when case)))  :type "case"} {:schema [:case]})
      (let [{errs :errors} (validate-node ctx (new-validation-acc) wh data)]
        (if (empty? errs)
          (if th
            (let [acc (validate-node ctx acc wh data)]
              (validate-node ctx (update-acc ctx acc {:schema [:case idx :then]}) th data))
            acc)
          (recur us (inc idx)))))))

(symbol "a/b")

(defmethod validate-type 'zen/string
  [_ ctx acc {ml :minLength mx :maxLength regex :regex tags :tags} data]
  (if (string? data)
    (let [ln (count data)
          acc (if (and ml (> ml ln))
                (add-error ctx acc {:message (format "Expected length >= %s, got %s" ml ln) :type "string.minLength"}
                           {:schema [:minLength]})
                acc)

          acc (if (and mx (< mx ln))
                (add-error ctx acc {:message (format "Expected length <= %s, got %s" mx ln) :type "string.maxLength"}
                           {:schema [:maxLength]})
                acc)

          acc (if (and regex (not (re-find (re-pattern regex) data)))
                (add-error ctx acc {:message (format "Expected match /%s/, got \"%s\"" regex data) :type "string.regex"}
                           {:schema [:regex]})
                acc)

          acc (if tags
                (let [sdata (symbol data)
                      sym (get-symbol ctx sdata)]
                  (if-not sym
                    (add-error ctx acc {:message (format "No symbol '%s found" sdata) :type "string"} {:schema [:tags]})

                    (let [sym-tags (:zen/tags sym)]
                      (if (not (clojure.set/superset? sym-tags tags))
                        (add-error ctx acc {:message (format "Expected symbol '%s tagged with '%s, but only %s"
                                                             (str data) (str tags)
                                                             (or sym-tags #{})) :type "string"} {:schema [:tags]})
                        acc))))
                acc)]
      acc)
    (add-error ctx acc {:message (format "Expected type of 'string, got '%s" (pretty-type data)) :type "string.type"})))

(defmethod validate-type 'zen/integer
  [_ ctx acc {ml :min mx :max} data]
  (if (integer? data)
    (let [acc (if (and ml (> ml data))
                (add-error ctx acc {:message (format "Expected  >= %s, got %s" ml data) :type "string"}
                           {:schema [:minLength]})
                acc)

          acc (if (and mx (< mx data))
                (add-error ctx acc {:message (format "Expected  <= %s, got %s" mx data) :type "string"}
                           {:schema [:maxLength]})
                acc)]
      acc)
    (add-error ctx acc {:message (format "Expected type of 'integer, got '%s" (pretty-type data)) :type "primitive-type"})))


(defmethod validate-type 'zen/number
  [_ ctx acc {ml :min mx :max} data]
  (if (number? data)
    (let [acc (if (and ml (> ml data))
                (add-error ctx acc {:message (format "Expected  >= %s, got %s" ml data) :type "string"}
                           {:schema [:minLength]})
                acc)

          acc (if (and mx (< mx data))
                (add-error ctx acc {:message (format "Expected  <= %s, got %s" mx data) :type "string"}
                           {:schema [:maxLength]})
                acc)]
      acc)
    (add-error ctx acc {:message (format "Expected type of 'number, got '%s" (pretty-type data)) :type "primitive-type"})))


(defmethod validate-type 'zen/symbol
  [_ ctx acc {tags :tags} data]
  (if (symbol? data)
    (if tags
      (let [sym (get-symbol ctx data)
            sym-tags (:zen/tags sym)]
        (if (not (clojure.set/superset? sym-tags tags))
          (add-error ctx acc {:message (format "Expected symbol '%s tagged with '%s, but only %s"
                                               (str data) (str tags)
                                               (or sym-tags #{})) :type "symbol"} {:schema [:tags]})
          acc))
      acc)
    (add-error ctx acc {:message (format "Expected type of 'symbol, got '%s" (pretty-type data)) :type "primitive-type"})))


(defmethod validate-type 'zen/apply
  [_ ctx acc {tags :tags} data]
  (if (list? data)
    (let [sym (first data)]
      (if (symbol? sym)
        (if-let [{fn-tags :zen/tags args-schema :args} (get-symbol ctx sym)]
          (if (contains? fn-tags 'zen/fn)
            (let [acc (if (and tags (not (clojure.set/subset? tags fn-tags)))
                        (add-error ctx acc {:message (format "fn definition '%s should be taged with %s, but '%s" sym tags fn-tags) :type "apply.tags"})
                        acc)]
              (validate-node ctx (update-acc ctx acc {:schema [sym :args]}) args-schema (rest data)))
            (add-error ctx acc {:message (format "fn definition '%s should be taged with 'zen/fn, but '%s" sym fn-tags) :type "apply.fn-tag"}))
          (add-error ctx acc {:message (format "Could not resolve fn '%s" sym) :type "apply.fn-name"}))
        (add-error ctx acc {:message (format "Expected symbol, got '%s" sym) :type "apply.fn-name"})))
    (add-error ctx acc {:message (format "Expected fn call '(fn-name args-1 arg-2), got '%s" (pretty-type data)) :type "apply.type"})))

(defmethod validate-type 'zen/boolean
  [_ ctx acc schema data]
  (if (boolean? data)
    acc
    (add-error ctx acc {:message (format "Expected type of 'boolean, got '%s" (pretty-type data)) :type "primitive-type"})))


(defmethod validate-type 'zen/keyword
  [_ ctx acc schema data]
  (if (keyword? data)
    acc
    (add-error ctx acc {:message (format "Expected type of 'keyword, got '%s" (pretty-type data)) :type "primitive-type"})))

(defn is-regex? [x]
  (instance? java.util.regex.Pattern x))

(defmethod validate-type 'zen/regex
  [_ ctx acc schema data]
  (if (and (string? data) (re-pattern data))
    acc
    (add-error ctx acc {:message (format "Expected type of 'regex, got '%s" (pretty-type data)) :type "primitive-type"})))

(defmethod validate-type 'zen/date
  [_ ctx acc schema data]
  (if (and (string? data) (re-matches #"\d{4}-(0[1-9]|1[012])-([012][1-9]|3[01])" data))
    acc
    (add-error ctx acc {:message (format "Expected type of 'date, got \"%s\"" data) :type "primitive-type"})))

(defmethod validate-type 'zen/datetime
  [_ ctx acc schema data]
  (if (and (string? data) (re-matches #"(?i)\d{4}-(0[1-9]|1[012])-([012][1-9]|3[01])T([01]\d|2[0-3]):([0-5]\d):([0-5]\d)[Z]?" data))
    acc
    (add-error ctx acc {:message (format "Expected type of 'datetime, got \"%s\"" data) :type "primitive-type"})))

(defmethod validate-type :default
  [t ctx acc schema data]
  (add-error ctx acc {:message (format "No validate-type multimethod for '%s" t) :type "primitive-type"}))

(defn register-unmatched-enum [acc enum data]
  (update-in acc [:enums (:path acc)]
             (fn [old]
               (-> old
                   (update :enum (fn [en] (into (or en #{}) (map :value enum))))
                   (assoc :data data)))))

(defn validate-node-rule-dispatch [ctx _acc rule _rule-val _data]
  (if (resolve-keyname-schema ctx {:tags #{'zen/schema-fx}} rule)
    ::fx
    rule))

(defmulti validate-node-rule #'validate-node-rule-dispatch)

(defmethod validate-node-rule :default [_ctx _acc _rule _rule-val _data])

(defn emit-fx
  "If an unknown key encountered it may be a top-level fx symbol (as defined in zen/schema :keyname-schemas)
  then the encountered symbol should be checked if it indeed is a zen/schema-fx
  and if it is a zen/schema-fx then an effect should be produced"
  [ctx schema-key schema-params data]
  (when-let [keyname-schema (resolve-keyname-schema ctx {:tags #{'zen/schema-fx}} schema-key)]
    {:name     (:zen/name keyname-schema)
     :params schema-params
     :data   data}))

(defmethod validate-node-rule ::fx [ctx acc rule rule-val data]
  (let [fx (-> (emit-fx ctx rule rule-val data)
               (assoc :path (:path acc)
                      :schema (:schema acc)))]
    (update acc :effects conj fx)))

(defmethod validate-node-rule :const [ctx acc _ const data]
  (when (and const (not= (:value const) data))
    (add-error ctx acc {:message (format "Expected '%s', got '%s'" (:value const) data) :type "schema"})))


(defmethod validate-node-rule :match [ctx acc _ pattern data]
  (let [errs (zen.match/match data pattern)]
    (when-not (empty? errs)
      (->> errs
           (reduce (fn [acc err]
                     (add-error ctx acc {:message (or (:message err) (str "Expected " (pr-str (:expected err)) ", got " (pr-str (:but err))))
                                         :type "match"}
                                {:path (:path err)}))
                   acc)))))

(defmethod validate-node-rule :filter [ctx acc _ filters data]
  (->> filters
       (reduce 
        (fn [acc [fk {m :match sch :schema}]]
          (let [subset (->> data (filterv (fn [x] (empty? (zen.match/match x m)))))]
            (-> (validate-node ctx (update-acc ctx acc {:schema [:filter fk]})
                               sch subset)
                (restore-acc acc))))
        acc)))

(defmethod validate-node-rule :confirms [ctx {pth :path :as acc} _ cfs data]
  (->> cfs
       (reduce (fn [acc sym]
                 (if (get-in acc [:confirms pth sym])
                   acc
                   (let [acc (assoc-in acc [:confirms pth sym] true)]
                     (if-let [sch (get-symbol ctx sym)]
                       (-> (validate-node ctx (update-acc ctx acc {:schema [:confirms sym]}) sch data)
                           (restore-acc acc))
                       (add-error ctx acc {:message (format "Could not resolve schema '%s" sym) :type "schema"})))))
               acc)))

(defmethod validate-node-rule :enum [_ctx {path :path :as acc} _ enum data]
  (when (and enum (not (get-in acc [:enums path :match])))
    (if (->> enum
             (filter (fn [v] (= (:value v) data)))
             first)
      (assoc-in acc [:enums path] {:match true})
      (register-unmatched-enum acc enum data))))


(defn validate-node-rule* [ctx acc rule rule-val data]
  (try (validate-node-rule ctx acc rule rule-val data)
       (catch Exception e
         (when (:unsafe @ctx) (throw e))
         (add-error ctx acc {:message (pr-str e) :type "schema"}))))

(defn validate-type* [ctx acc schema data]
  (try
    (if (:type schema)
      (let [{tags :zen/tags} (get-symbol ctx (:type schema))]
        (if (and tags (contains? tags 'zen/type))
          (validate-type (:type schema) ctx acc schema data)
          (add-error ctx acc {:message (format ":type '%s' should be tagged with 'zen/type, but %s " (:type schema) tags)
                              :type "schema.type"})))
      acc)
    (catch Exception e
      (when (:unsafe @ctx) (throw e))
      (add-error ctx acc {:message (pr-str e) :type "schema"}))))

(defn validate-node [ctx acc {tp :type :as schema} data]
  (let [acc (reduce-kv (fn [acc' rule rule-val]
                         (or (validate-node-rule* ctx acc' rule rule-val data)
                             acc'))
                       acc
                       schema)
        acc (validate-type* ctx acc schema data)]
    acc))

(defn unknown-keys-errors [acc]
  (->> (:keys acc)
       (filter (fn [[_ v]] (empty? v)))
       (map (fn [[k _]] {:type "unknown-key"
                        :message (format "unknown key %s" (last k))
                        :path k}))))

(defn enum-errors [acc]
  (->> (:enums acc)
       (remove (fn [[_ v]] (:match v)))
       (map (fn [[path {enum :enum data :data}]]
              {:type "enum"
               :message (format "Expected '%s' in %s" data enum)
               :path path}))))

(defn global-errors&warnings [acc]
  (let [errs (vec (concat (:errors acc)
                          (unknown-keys-errors acc)
                          (enum-errors acc)))]
    (merge acc {:warnings []
                :errors errs
                :effects (:effects acc)})))

(defn validate-schema [ctx schema data]
  (-> (validate-node ctx (new-validation-acc) schema data)
      (global-errors&warnings)))

(defn validate
  [ctx schemas data]
  (->> schemas
       (reduce (fn [acc sym]
                 (if-let [sch (get-symbol ctx sym)]
                   (validate-node ctx (assoc acc :schema [sym]) sch data)
                   (add-error ctx acc {:message (format "Could not resolve schema '%s" sym) :type "schema"})))
               (new-validation-acc))
       (global-errors&warnings)))
