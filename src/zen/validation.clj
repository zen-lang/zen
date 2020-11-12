(ns zen.validation
  (:require [clojure.string :as str]))

(defn get-symbol [ctx nm]
  (when-let [res (get-in @ctx [:symbols nm])]
    (assoc res 'name nm)))

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

(declare validate-schema)

(defmulti validate-type (fn [tp & _] tp))

(defn restore-acc [acc {pth :path sch :schema}]
  (assoc acc :schema sch :path pth))

(defn resolve-property [ctx k]
  (let [sym (symbol (namespace k) (name k))]
    (when (contains? (get-in @ctx [:tags 'zen/property]) sym)
      (get-symbol ctx sym))))

(defmethod validate-type 'zen/map
  [_ ctx acc {cfs :confirms ks :keys vls :values reqs :require} data]
  (if (map? data)
    (let [acc (->> cfs
                   (reduce (fn [acc sym]
                             (if-let [sch (get-symbol ctx sym)]
                               (-> (validate-schema ctx (update-acc ctx acc {:schema [sym]}) sch data)
                                   (restore-acc acc))
                               (add-error ctx acc {:message (format "Could not resolve schema '%s" sym) :type "schema"})))
                           acc))
          acc (->> data
                   (reduce (fn [acc [k v]]
                             (if-let [sch (get ks k)]
                               (let [acc' (validate-schema ctx (update-acc ctx acc {:path [k] :schema [k]}) sch v)
                                     acc' (if vls
                                            (validate-schema ctx (update-acc ctx (restore-acc acc' acc) {:schema [:values] :path [k]}) vls v)
                                            (assoc-in acc' [:keys (conj (:path acc) k)] true))]
                                 (restore-acc acc' acc))
                               (if vls
                                 (-> (validate-schema ctx (update-acc ctx acc {:schema [:values] :path [k]}) vls v)
                                     (restore-acc acc))
                                 (if-let [sch  (and (namespace k) (resolve-property ctx k))]
                                   (-> (validate-schema ctx (update-acc ctx acc {:path [k] :schema [k]}) sch v)
                                       (restore-acc acc))
                                   (update-in acc [:keys (conj (:path acc) k)] #(or % false))))))
                           acc))
          acc (->> reqs
                   (reduce (fn [acc k]
                             (if (nil? (get data k))
                               (add-error ctx (update-acc ctx acc {:path [k] :schema [:require]})
                                          {:message (format "%s is required" k) :type "require"})
                               acc))
                           acc))]
      acc)
    (add-error ctx acc {:message (format "Expected type of 'map, got %s" (pr-str data))  :type "type"})))

(defmethod validate-type 'zen/vector
  [_ ctx acc {evr :every mn :minItems mx :maxItems} data]
  (if (sequential? data)
    (let [acc (if evr
                (-> 
                 (loop [acc (update-acc ctx acc {:schema [:every]}), idx 0, [d & ds] data]
                   (if (and (nil? d) (empty? ds))
                     acc
                     (recur
                      (-> (validate-schema ctx (update-acc ctx acc {:path [idx]}) evr d)
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
                      (-> (validate-schema ctx (update-acc ctx acc {:path [idx]}) evr d)
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

(defmethod validate-type 'zen/string
  [_ ctx acc {ml :minLength mx :maxLength regex :regex} data]
  (if (string? data)
    (let [ln (count data)
          acc (if (and ml (> ml ln))
                (add-error ctx acc {:message (format "Expected length >= %s, got %s" ml ln) :type "string"}
                           {:schema [:minLength]})
                acc)

          acc (if (and mx (< mx ln))
                (add-error ctx acc {:message (format "Expected length <= %s, got %s" mx ln) :type "string"}
                           {:schema [:maxLength]})
                acc)
          acc (if (and regex (not (re-matches regex data)))
                (add-error ctx acc {:message (format "Expected '%s' matches /%s/" data regex) :type "string"}
                           {:schema [:regex]})
                acc)]
      acc)
    (add-error ctx acc {:message (format "Expected type of 'string, got '%s" (pretty-type data)) :type "primitive-type"})))

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

(defmethod validate-type 'zen/symbol
  [_ ctx acc schema data]
  (if (symbol? data)
    acc
    (add-error ctx acc {:message (format "Expected type of 'symbol, got '%s" (pretty-type data)) :type "primitive-type"})))


(defmethod validate-type 'zen/keyword
  [_ ctx acc schema data]
  (if (keyword? data)
    acc
    (add-error ctx acc {:message (format "Expected type of 'symbol, got '%s" (pretty-type data)) :type "primitive-type"})))

(defn is-regex? [x]
  (instance? java.util.regex.Pattern x))

(defmethod validate-type 'zen/regex
  [_ ctx acc schema data]
  (if (or (is-regex? data) (string? data))
    acc
    (add-error ctx acc {:message (format "Expected type of 'regex, got '%s" (pretty-type data)) :type "primitive-type"})))

(defn validate-schema [ctx acc {tp :type {sk :key} :schema-key  ks :keys cfs :confirms :as schema} data]
  (try
    (let [acc (if-let [sch-nm (and sk (get data sk))]
                (if-let [sch (and sch-nm (get-symbol ctx sch-nm))]
                  (-> (validate-schema ctx (update-acc ctx acc {:schema [:schema-key sch-nm]}) sch data)
                      (restore-acc acc))
                  (add-error ctx acc {:message (format "Could not find schema %s" sch-nm) :type "schema"}))
                acc)]
      (cond
        tp (validate-type tp ctx acc schema data)

        (or ks cfs) (validate-type 'zen/map ctx acc schema data)

        :else (add-error ctx acc {:message (format "I don't know how to eval %s" (pr-str schema)) :type "schema"})))
    (catch Exception e
      (add-error ctx acc {:message (.getMessage e)
                          :type "schema"}))))


(defn validate
  [ctx schemas data]
  (let [acc  (->> schemas
                  (reduce (fn [acc sym]
                            (if-let [sch (get-symbol ctx sym)]
                              (validate-schema ctx (assoc acc :schema [sym])  sch data)
                              (add-error ctx acc {:message (format "Could not resolve schema '%s" sym) :type "schema"})))
                          (new-validation-acc)))]
    (-> acc
        (select-keys [:errors])
        (update :errors into (->> (:keys acc)
                                  (filter (fn [[_ v]] (false? v)))
                                  (map (fn [[k _]] {:type "unknown-key"
                                                   :message (format "unknown key %s" (last k))
                                                   :path k})))))))
