(ns zen.walk
  (:require [zen.core]
            [zen.v2-validation]))


(defn compare-path-lexicographical [v1 v2]
  (or (first (drop-while zero? (map #(compare (str %1) (str %2)) v1 v2)))
      (compare (count v1) (count v2))))


(defn remove-nested-paths
  "Removes nested paths by sorting in lexicographical order
   and then checking if a path is included in the next path"
  [paths]
  (let [sorted-paths (sort compare-path-lexicographical paths)]
    (->> (map (fn [cur-path next-path]
                (when (not= cur-path (take (count cur-path) next-path))
                  cur-path))
              sorted-paths
              (concat (rest sorted-paths) [nil]))
         (remove nil?))))


(defn iterate-dsl
  "Returns seq of paths in a provided dsl-expr.
   Paths are calculated in the `zen.v2-validation/validate-schema"
  [ztx dsl-schema dsl-expr]
  (->> (zen.v2-validation/validate-schema ztx dsl-schema dsl-expr)
       :visited
       remove-nested-paths))


(defn zen-dsl-seq [ztx sym-def]
  (for [tag   (:zen/tags sym-def)
        :let  [tag-sym (zen.core/get-symbol ztx tag)]
        :when (contains? (:zen/tags tag-sym) 'zen/schema)
        path  (iterate-dsl ztx tag-sym sym-def)]
    {:tag   tag
     :path  path
     :value (get-in sym-def path)}))

