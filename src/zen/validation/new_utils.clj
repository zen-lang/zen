(ns zen.validation.new-utils
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

(defn- node-vtx&log_transient
  [vtx sch-path new-paths]
  (-> (transient vtx)
      (assoc! :errors [])
      (assoc! :path new-paths)
      (assoc! :schema (into (:schema vtx) sch-path))
      (assoc! :visited (conj (:visited vtx) new-paths))))

(defn node-vtx&log
  ([vtx sch-path path]
   (persistent! (node-vtx&log_transient vtx sch-path (into (:path vtx) path))))
  ([vtx
    sch-path
    path
    rule-name]
   (let [new-paths (into (:path vtx) path)
         vtx*      (persistent! (node-vtx&log_transient vtx sch-path new-paths))]
     (update-in vtx* [:visited-by new-paths] (fnil conj #{}) rule-name))))

(defn cur-path [vtx path]
  (into (:path vtx) path))

(defn merge-vtx [*node-vtx global-vtx]
  (let [node-vtx-errors (:errors *node-vtx)
        node-vtx-iter   (.iterator ^Iterable (dissoc *node-vtx :path :schema :errors))]
    (loop [merged-vtx ^clojure.lang.Map (update global-vtx :errors into node-vtx-errors)]
      (if (.hasNext node-vtx-iter)
        (let [node-vtx-entry ^clojure.lang.MapEntry (.next node-vtx-iter)]
          (recur (assoc merged-vtx
                        (nth node-vtx-entry 0)
                        (nth node-vtx-entry 1))))
        merged-vtx))))
