(ns zen.validation.utils
  (:require
   [clojure.string :as str]))

(defn pretty-type [x]
  (if-let [tp (type x)]
    (str/lower-case (last (str/split (str tp) #"\.")))
    "nil"))

(defn setmap? [a]
  (or (set? a) (map? a)))

(defn deep-merge
  "efficient deep merge"
  [a b]
  (loop [[[k v :as i] & ks] b,
         acc (transient a)]
    (if (nil? i)
      (persistent! acc)
      (let [av (get a k)]
        (if (= v av)
          (recur ks acc)
          (recur ks (cond
                      (and (map? v) (map? av)) (assoc! acc k (deep-merge av v))
                      (and (setmap? v) (nil? av)) (assoc! acc k v)
                      (and (setmap? v) (setmap? av)) (assoc! acc k (into av v))
                      :else
                      (assoc! acc k v))))))))

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

(defn node-vtx&log [vtx sch-path path]
  (-> (transient vtx)
      (assoc! :errors [])
      (assoc! :path (into (:path vtx) path))
      (assoc! :unknown-keys (:unknown-keys vtx))
      (assoc! :schema (into (:schema vtx) sch-path))
      (assoc! :visited (conj (:visited vtx) (into (:path vtx) path)))
      (persistent!)))

(defn merge-vtx [*node-vtx global-vtx]
  (-> global-vtx
      (update :errors into (:errors *node-vtx))
      (merge (dissoc *node-vtx :path :schema :errors))))
