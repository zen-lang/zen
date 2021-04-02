(ns zen.validation
  (:require
   [clojure.set]
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
   :warings []
   :schema []
   :path []})

(defn pretty-type [x]
  (str/lower-case (last (str/split (str (type x)) #"\."))))

(declare validate-node)

(defmulti validate-type (fn [tp & _] tp))

(defn restore-acc [acc {pth :path sch :schema}]
  (assoc acc :schema sch :path pth))

(defn resolve-property [ctx k]
  (let [sym (symbol (namespace k) (name k))]
    (when (contains? (get-in @ctx [:tags 'zen/property]) sym)
      (get-symbol ctx sym))))

(defn is-exclusive? [group data]
  (loop [other-keys nil [k & ks] group]
    (if (and (nil? k) (empty? ks))
      true
      (let [has-keys  (not (empty? (select-keys data (if (set? k) k #{k}))))]
        (if (and other-keys has-keys)
          false
          (if (empty? ks)
            true
            (recur has-keys ks)))))))

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
                        (fn [k] (reduce (fn [acc v] (assoc acc v true))
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
                                           (let [acc' (validate-node ctx (update-acc ctx acc {:path [k] :schema [k]}) sch v)
                                                 acc' (assoc-in acc' [:keys (conj (:path acc) k)] true)]
                                             (restore-acc acc' acc))
                                           (if-let [prop-sch  (and (keyword? k) (namespace k) (or (resolve-property ctx k)
                                                                                                  (and kns
                                                                                                       (when-let [prop-sch (get-symbol ctx (symbol k))]
                                                                                                         (when (or (nil? (:tags kns))
                                                                                                                   (clojure.set/subset? (:tags kns) (:zen/tags prop-sch)))
                                                                                                           prop-sch)))))]
                                             (-> (validate-node ctx (update-acc ctx acc {:path [k] :schema (if kns [:keyname-schemas k] [:property k])}) prop-sch v)
                                                 (restore-acc acc))
                                             (if ignore-unknown-keys
                                               acc
                                               (update-in acc [:keys (conj (:path acc) k)] #(or % false)))))
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
                                            {:message (format "one of keys %s is required" k) :type "map.require"}
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
                               nm)]
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

(defn validate-collection
  [type ctx acc {evr :every {si :index si-ns :ns} :schema-index mn :minItems mx :maxItems nt :nth} data]
  (let [acc (if (or evr nt)
              (->
               (loop [acc acc, idx 0, [d & ds] data]
                 (if (and (nil? d) (empty? ds))
                   acc
                   (recur
                    (let [acc (if evr
                                (-> (validate-node ctx (update-acc ctx acc {:path [idx] :schema [:every]}) evr d)
                                    (restore-acc acc))
                                acc)
                          acc (if-let [sch (and nt (get nt idx))]
                                (-> (validate-node ctx (update-acc ctx acc {:path [idx] :schema [:nth idx]}) sch d)
                                    (restore-acc acc))
                                acc)]
                      acc)
                    (inc idx) ds)))
               (restore-acc acc))
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

        acc (if-let [nm (and si (nth data si nil))]
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
  (if (sequential? data)
    (validate-collection "vector" ctx acc schema data)
    (add-error ctx acc {:message (format "Expected type of 'vector, got %s" (pretty-type data))  :type "type"})))

(defmethod validate-type 'zen/list
  [_ ctx acc schema data]
  (if (list? data)
    (validate-collection "list" ctx acc schema data)
    (add-error ctx acc {:message (format "Expected type of 'vector, got %s" (pretty-type data))  :type "type"})))

(defmethod validate-type 'zen/set
  [_ ctx acc {evr :every mn :minItems mx :maxItems} data]
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
                acc)]
      acc)
    (add-error ctx acc {:message (format "Expected type of 'set, got %s" (pretty-type data))  :type "type"})))


(defmethod validate-type 'zen/any
  [_ ctx acc sch data]
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

(defmethod validate-type 'zen/string
  [_ ctx acc {ml :minLength mx :maxLength regex :regex} data]
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
  (if (or (float? data) (integer? data))
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
    (add-error ctx acc {:message (format "Expected type of 'symbol, got '%s" (pretty-type data)) :type "primitive-type"})))

(defn is-regex? [x]
  (instance? java.util.regex.Pattern x))

(defmethod validate-type 'zen/regex
  [_ ctx acc schema data]
  (if (and (string? data) (re-pattern data))
    acc
    (add-error ctx acc {:message (format "Expected type of 'regex, got '%s" (pretty-type data)) :type "primitive-type"})))

(defmethod validate-type 'zen/date
  [_ ctx acc schema data]
  (if (and (string? data) #_(re-matches #"\d{4}-\d{2}-\d{2}" data))
    acc
    (add-error ctx acc {:message (format "Expected type of 'date, got \"%s\"" data) :type "primitive-type"})))

(defmethod validate-type 'zen/datetime
  [_ ctx acc schema data]
  (if (and (string? data) #_(re-matches #"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}.*" data))
    acc
    (add-error ctx acc {:message (format "Expected type of 'date, got \"'%s\"" data) :type "primitive-type"})))

(defmethod validate-type :default
  [t ctx acc schema data]
  (add-error ctx acc {:message (format "No validate-type multimethod for '%s" t) :type "primitive-type"}))

(defn validate-const [ctx acc const data]
  (if const
    (if (= (:value const) data)
      acc
      (add-error ctx acc {:message (format "Expected '%s', got '%s'" (:value const) data) :type "schema"}))
    acc))

(defn validate-confirms [ctx {pth :path :as acc} cfs data]
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

(defn register-unmatched-enum [acc enum data]
  (update-in acc [:enums (:path acc)]
             (fn [old]
               (-> old
                   (update :enum (fn [en] (into (or en #{}) (map :value enum))))
                   (assoc :data data)))))

(defn validate-enum [ctx {path :path :as acc} enum data]
  (if enum
    (if (get-in acc [:enums path :match])
      acc
      (if (->> enum
               (filter (fn [v] (= (:value v) data)))
               first)
        (assoc-in acc [:enums path] {:match true})
        (register-unmatched-enum acc enum data)))
    acc))

(defmulti valueset-find (fn [tp ctx vs data] tp))

(defn register-unmatched-valueset [acc nm data]
  (update-in acc [:valuesets (:path acc)]
             (fn [{vs :valuesets :as node}]
               (-> node
                   (assoc :valuesets (conj (or vs #{}) nm))
                   (assoc :data data)))))

(defn validate-valuesets [ctx {path :path :as acc} valuesets data]
  (if valuesets
    ;; short circuit
    (if (get-in acc [:valuesets path :match])
      acc
      (loop [[{nm :name k :key :as vs} & vss] valuesets acc acc]
        (if (and (nil? vs) (empty? vss))
          acc
          (if-let [vs (get-symbol ctx nm)]
            (if-let [values (:values vs)]
              (if (->> values (filter (fn [v] (= (get v k) data))) (first))
                (assoc-in acc [:valuesets path] {:match nm})
                (recur vss (register-unmatched-valueset acc nm data)))
              (if-let [prov (:provider vs)]
                (if (valueset-find prov ctx vs data)
                  (assoc-in acc [:valuesets path] {:match nm})
                  (recur vss (register-unmatched-valueset acc nm data)))
                (recur vss acc)))
            (recur vss acc)))))
    acc))

(defn validate-node [ctx acc {tp :type :as schema} data]
  (try
    (let [acc (validate-const ctx acc (:const schema) data)
          acc (validate-confirms ctx acc (:confirms schema) data)
          acc (validate-enum ctx acc (:enum schema) data)
          acc (validate-valuesets ctx acc (:valuesets schema) data)]
      (if tp
        (let [{tags :zen/tags} (get-symbol ctx tp)]
          (if (and tags (contains? tags 'zen/type))
            (validate-type tp ctx acc schema data)
            (add-error ctx acc {:message (format ":type '%s' should be tagged with 'zen/type, but %s " tp tags) :type "schema.type"})))
        acc))
    (catch Exception e
      (add-error ctx acc {:message (pr-str e) :type "schema"})
      (when (:unsafe @ctx) (throw e)))))


(defn unknown-keys-errors [acc]
  (->> (:keys acc)
       (filter (fn [[_ v]] (false? v)))
       (map (fn [[k _]] {:type "unknown-key"
                         :message (format "unknown key %s" (last k))
                         :path k}))))

(defn valueset-errors [acc]
  (->> (:valuesets acc)
       (remove (fn [[_ v]] (:match v)))
       (map (fn [[path {vs :valuesets data :data}]]
              {:type "valuesets"
               :message (format "None of valuests %s is matched for '%s'" vs data)
               :path path}))))

(defn enum-errors [acc]
  (->> (:enums acc)
       (remove (fn [[_ v]] (:match v)))
       (map (fn [[path {enum :enum data :data}]]
              {:type "enum"
               :message (format "Expected '%s' in %s" data enum)
               :path path}))))

(defn global-errors [acc]
  (update acc :errors
          (fn [errs]
            (-> errs
                (into (unknown-keys-errors acc))
                (into (valueset-errors acc))
                (into (enum-errors acc))))))


(defn validate-schema [ctx schema data]
  (-> (validate-node
       ctx (new-validation-acc)   schema data)
      (global-errors)))

(defn validate
  [ctx schemas data]
  (->> schemas
       (reduce (fn [acc sym]
                 (if-let [sch (get-symbol ctx sym)]
                   (validate-node ctx (update acc :schema conj sym) sch data)
                   (add-error ctx acc {:message (format "Could not resolve schema '%s" sym) :type "schema"})))
               (new-validation-acc))
       (global-errors)))
