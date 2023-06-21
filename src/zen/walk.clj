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
