(ns zen.walk
  (:require [zen.core]
            [zen.v2-validation]))


(defn compare-path-lexicographical [v1 v2]
  (or (first (drop-while zero? (map #(compare (str %1) (str %2)) v1 v2)))
      (compare (count v1) (count v2))))


(defn iterate-dsl
  "Returns seq of paths in a provided dsl-expr.
   Paths are calculated in the `zen.v2-validation/validate-schema"
  [ztx tags dsl-expr]
  (->> (zen.v2-validation/validate ztx tags dsl-expr {:vtx-visited true})
       :visited))


(defn remove-nested-paths
  "Removes nested paths by sorting in lexicographical order.
   Then checking if a path is included in the next path"
  [paths]
  (let [sorted-paths (sort compare-path-lexicographical paths)]
    (->> (map (fn [cur-path next-path]
                (when (not= cur-path (take (count cur-path) next-path))
                  cur-path))
              sorted-paths
              (concat (rest sorted-paths) [nil]))
         (remove nil?))))


(defn iterate-dsl-leafs
  "Returns seq of paths to leafs in a provided dsl-expr."
  [ztx tags dsl-expr]
  (->> (iterate-dsl ztx tags dsl-expr)
       remove-nested-paths))


(defn zen-dsl-seq [ztx sym-def]
  (let [schema-tags (->> (:zen/tags sym-def)
                         (filter #(-> (zen.core/get-symbol ztx %)
                                      :zen/tags
                                      (contains? 'zen/schema))))]
    (for [path (iterate-dsl ztx schema-tags sym-def)]
      {:path  path
       :value (get-in sym-def path)})))


(defn zen-dsl-leafs-seq [ztx sym-def]
  (let [schema-tags (->> (:zen/tags sym-def)
                         (filter #(-> (zen.core/get-symbol ztx %)
                                      :zen/tags
                                      (contains? 'zen/schema))))]
    (for [path (iterate-dsl-leafs ztx schema-tags sym-def)]
      {:path  path
       :value (get-in sym-def path)})))


#_:confirms
#_:every
:key
:key-schema
:keyname-schemas
#_:keys
#_:nth
:schema-index
:schema-key
#_:values


(def key-walk-seq-fns
  {:confirms (fn [ztx symbols]
               (mapv (fn [sym]
                       {:schema-path [sym]
                        :schema (zen.core/get-symbol ztx sym)})
                     symbols))
   :every (fn [_ztx every-sch] [{:path   [:#]
                                 :schema every-sch}])
   :keys  (fn [_ztx ks] (mapv (fn [[k v]]
                                {:path   [k]
                                 :schema v})
                              ks))
   :nth   (fn [_ztx idxs] (mapv (fn [[idx sch]]
                                  {:path   [idx]
                                   :schema sch})
                                idxs))
   :values   (fn [_ztx val-sch] [{:path   [:*]
                                  :schema val-sch}])})


(defn add-lvl-ctx [cur-lvl k w]
  (let [path
        (if (:schema-path w)
          (or (:path w) [])
          (into (:path cur-lvl) (:path w)))

        full-path
        (into (:full-path cur-lvl) (:path w))

        schemas
        (-> (or (:schema-path w) (:schemas cur-lvl))
            (conj k))

        full-schemas
        (cond-> (:full-schemas cur-lvl)
          (some? (:schema-path w)) (into (:schema-path w))
          :always (conj k))


        schema-path
        (into (-> (or (:schema-path w)
                      (:schema-path cur-lvl))
                  (conj k))
              (:path w))

        full-schema-path
        (into
          (cond-> (:full-schema-path cur-lvl)
            (some? (:schema-path w)) (into (:schema-path w))
            :always (conj k))
          (:path w))]

    (assoc w
           :path path
           :schemas schemas
           :schema-path schema-path
           :full-path full-path
           :full-schemas full-schemas
           :full-schema-path full-schema-path)))


(defn update-queue [ztx queue cur-lvl]
  (reduce-kv
    (fn [acc k v]
      (if-let [walk-key-fn (get key-walk-seq-fns k)]
        (into acc
              (map #(add-lvl-ctx cur-lvl k %))
              (walk-key-fn ztx v))
        acc))
    queue
    (:schema cur-lvl)))


(defn compile [acc ztx wtx compile-fn]
  (reduce-kv (fn [acc k v] (compile-fn acc ztx wtx k v))
             acc
             (:schema wtx)))


(defn compile-schema [ztx compile-fn acc sym-def]
  (loop [[cur-lvl & queue] [{:schema      sym-def
                             :path        []
                             :schemas     [(:zen/name sym-def)]
                             :schema-path [(:zen/name sym-def)]
                             :full-path        []
                             :full-schemas     [(:zen/name sym-def)]
                             :full-schema-path [(:zen/name sym-def)]}]
         acc acc]
    (if (some? cur-lvl)
      (recur (update-queue ztx queue cur-lvl)
             (compile acc ztx cur-lvl compile-fn))
      acc)))
