(ns zen.validation.utils
  (:require
   [clojure.string :as str]))

(defn pretty-type [x]
  (if-let [tp (type x)]
    (str/lower-case (last (str/split (str tp) #"\.")))
    "nil"))

(defn unknown-errs [vtx]
  (update vtx :errors
          (fn [errs]
            (->> (:unknown-keys vtx)
                 (map (fn [path]
                        {:path path
                         :type "unknown-key"
                         :message (str "unknown key " (peek path))}))
                 (into errs)
                 vec))))

(defn add-err* [types-cfg vtx sch-key err & data-path]
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

(defn empty-vtx []
  {:errors []
   :warnings []
   :visited #{}
   :unknown-keys #{}
   :effects []})

(defn node-vtx
  ([vtx sch-path]
   (-> (transient vtx)
       (assoc! :errors [])
       (assoc! :schema (into (:schema vtx) sch-path))
       (persistent!)))
  ([vtx sch-path path]
   (-> (transient vtx)
       (assoc! :errors [])
       (assoc! :path (into (:path vtx) path))
       (assoc! :schema (into (:schema vtx) sch-path))
       (persistent!))))

(defn node-vtx&log [vtx sch-path path & [rule-name]]
  (let [vtx*
        (-> (transient vtx)
            (assoc! :errors [])
            (assoc! :path (into (:path vtx) path))
            (assoc! :unknown-keys (:unknown-keys vtx))
            (assoc! :schema (into (:schema vtx) sch-path))
            (assoc! :visited (conj (:visited vtx) (into (:path vtx) path)))
            (persistent!))]
    (cond-> vtx*
      rule-name
      (update-in [:visited-by (into (:path vtx) path)] (fnil conj #{}) rule-name))))

(defn cur-path [vtx path]
  (into (:path vtx) path))

(defn merge-vtx [*node-vtx global-vtx]
  (-> global-vtx
      (update :errors into (:errors *node-vtx))
      (merge (dissoc *node-vtx :path :schema :errors))))


(def fhir-date-regex
  (re-pattern
   "([0-9]([0-9]([0-9][1-9]|[1-9]0)|[1-9]00)|[1-9]000)(-(0[1-9]|1[0-2])(-(0[1-9]|[1-2][0-9]|3[0-1]))?)?"))


(def fhir-datetime-regex
  (re-pattern
   "([0-9]([0-9]([0-9][1-9]|[1-9]0)|[1-9]00)|[1-9]000)(-(0[1-9]|1[0-2])(-(0[1-9]|[1-2][0-9]|3[0-1])(T([01][0-9]|2[0-3]):[0-5][0-9]:([0-5][0-9]|60)(\\.[0-9]+)?(Z|(\\+|-)((0[0-9]|1[0-3]):[0-5][0-9]|14:00)))?)?)?"))


(def types-cfg
  {'zen/string {:fn string?
                :to-str "string"}

   'zen/date
   {:fn #(and (string? %) (re-matches fhir-date-regex %))
    :to-str "date"}

   'zen/datetime
   {:fn #(and (string? %) (re-matches fhir-datetime-regex %))
    :to-str "datetime"}

   'zen/number {:fn number?
                :to-str "number"}

   ;; TODO discuss sequential? predicate
   'zen/set {:fn #(or (set? %) (sequential? %))
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

   'zen/qsymbol {:fn (fn [sym]
                       (and
                         (symbol? sym)
                         (:zen/quote (meta sym))))
                 :to-str "quoted-symbol"}

   'zen/any (constantly true)
   'zen/case (constantly true)

   ;; fn is implemented as a separate multimethod
   'zen/apply {:to-str "apply"}

   'zen/regex
   {:fn #(and (string? %) (re-pattern %))
    :to-str "regex"}})


(def add-err (partial add-err* types-cfg))


(defn cur-keyset [vtx data]
  (->> (keys data)
       (map #(conj (:path vtx) %))
       set))
