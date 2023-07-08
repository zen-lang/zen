(ns zen.walk
  (:require [zen.core]
            [zen.utils]
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
                       #:new{:schema      (zen.core/get-symbol ztx sym)
                             :schema-sym  sym
                             :data-path   [] #_"NOTE: to flush local data-path"
                             :schema-path [sym]})
                     symbols))

   :every (fn [_ztx every-sch]
            [#:new{:schema    every-sch
                   :data-path [:#]}])

   :keys (fn [_ztx ks]
           (mapv (fn [[k v]]
                   #:new{:schema      v
                         :data-path   [k]
                         :schema-path [k]})
                 ks))

   :nth (fn [_ztx idxs]
          (mapv (fn [[idx sch]]
                  #:new{:schema      sch
                        :data-path   [idx]
                        :schema-path [idx]})
                idxs))

   :values (fn [_ztx val-sch]
             [#:new{:schema      val-sch
                    :data-path   [:*]}])})


(def branch-keys (set (keys key-walk-seq-fns)))


(defn- init-lvl [top-lvl-sch]
  {:schema           top-lvl-sch
   :schema-sym       (:zen/name top-lvl-sch)
   :schema-stack     [(:zen/name top-lvl-sch)]
   :data-path        []
   :full-data-path   []
   :schema-path      [(:zen/name top-lvl-sch)]
   :full-schema-path [(:zen/name top-lvl-sch)]})


(defn- update-when-some [m k f v & args]
  (if (some? v)
    (apply update m k f v args)
    m))


(defn- add-lvl-ctx [outer-lvl k this-lvl]
  (-> outer-lvl
      (update-when-some :schema (fn [_outer new] new) (:new/schema this-lvl))
      (update-when-some :schema-sym  (fn [_outer new] new) (:new/schema-sym this-lvl))
      (update-when-some :schema-stack conj (:new/schema-sym this-lvl))
      (update-when-some :full-data-path into (:new/data-path this-lvl))

      (update-when-some :data-path
                        (fn [outer new]
                          (if (:new/schema-sym this-lvl)
                            new
                            (into outer new)))
                        (:new/data-path this-lvl))

      (update-when-some :schema-path
                        (fn [outer new]
                          (if (:new/schema-sym this-lvl)
                            new
                            (-> outer (conj k) (into new))))
                        (:new/schema-path this-lvl))

      (update-when-some :full-schema-path
                        (fn [outer new] (-> outer (conj k) (into new)))
                        (:new/schema-path this-lvl))))


(defn- children-schemas [ztx root]
  (mapcat (fn [[k v]]
            (when-let [walk-key-fn (get key-walk-seq-fns k)]
              (map #(add-lvl-ctx root k %)
                   (walk-key-fn ztx v))))
          (:schema root)))


#_"TODO: handle infinite recursion"

(defn contains-nested-schemas? [schema]
  (some #(contains? schema %) branch-keys))


(defn schema-seq [ztx sym-def]
  (tree-seq
    #(contains-nested-schemas? (:schema %))
    #(children-schemas ztx %)
    (init-lvl sym-def)))


(defn schema-bf-seq [ztx sym-def]
  (zen.utils/bf-tree-seq
    #(contains-nested-schemas? (:schema %))
    #(children-schemas ztx %)
    (init-lvl sym-def)))


(defn reduce-schema-seq-kv [f acc schema-seq]
  (reduce (fn [acc node]
            (reduce-kv (fn [acc k v]
                         (f acc (assoc node :k k :v v)))
                       acc
                       (:schema node)))
          acc
          schema-seq))
