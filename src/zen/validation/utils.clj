(ns zen.validation.utils
  (:require
   [clojure.string :as str]
   [zen.utils :as utils]))

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
                 (utils/iter-into errs)
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
            (assoc :path (utils/iter-into (:path vtx) data-path))
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

(defn node-vtx-transient
  ([vtx sch-path]
   (-> (transient vtx)
       (assoc! :errors [])
       (assoc! :schema (utils/iter-into (:schema vtx) sch-path))))
  ([vtx sch-path vtx-path]
   (-> vtx
       (node-vtx-transient sch-path)
       (assoc! :path vtx-path))))

(defn node-vtx
  ([vtx sch-path]
   (-> vtx
       (node-vtx-transient sch-path)
       (persistent!)))
  ([vtx sch-path path]
   (-> vtx
       (node-vtx-transient sch-path (utils/iter-into (:path vtx) path))
       (persistent!))))

(defn node-vtx&log-transient
  [vtx sch-path vtx-path]
  (-> vtx
      (node-vtx-transient sch-path vtx-path)
      (assoc! :visited (conj (:visited vtx) vtx-path))))

(defn node-vtx&log
  ([vtx sch-path path]
   (-> vtx
       (node-vtx&log-transient sch-path (utils/iter-into (:path vtx) path))
       (persistent!)))

  ([vtx sch-path path rule-name]
   (let [new-vtx-path (utils/iter-into (:path vtx) path)]
     (-> vtx
         (node-vtx&log-transient sch-path new-vtx-path)
         (persistent!)
         (update-in [:visited-by new-vtx-path] (fnil conj #{}) rule-name)))))

(defn cur-path [vtx path]
  (utils/iter-into (:path vtx) path))

(defn merge-vtx [node-vtx global-vtx]
  (utils/iter-reduce (fn [merged-vtx node-vtx-entry]
                       (assoc merged-vtx
                              (nth node-vtx-entry 0)
                              (nth node-vtx-entry 1)))
                     (update global-vtx :errors utils/iter-into (:errors node-vtx))
                     (dissoc node-vtx :path :schema :errors)))
