(ns zen.walk)


(def nested-schema-keys #{:keys :every})


(defn get-nested-schemas-dispatch [{sch :node}]
  (first (filter sch nested-schema-keys)))


(defmulti get-nested-schemas #'get-nested-schemas-dispatch)


(defmethod get-nested-schemas :keys [{sch :node, :keys [path]}]
  (map (fn [[k v]]
         {:path (conj path :keys k)
          :node v})
       (:keys sch)))


(defmethod get-nested-schemas :every [{sch :node, :keys [path]}]
  [{:path (conj path :every)
    :node (:every sch)}])


(defmethod get-nested-schemas :default [{sch :node}]
  nil)


(defn nested-schema-entry? [[k v]]
  (contains? nested-schema-keys k))


(defn sch-seq [sch]
  (let [contains-nested-schemas?
        (fn [{:keys [node]}]
          (and (map? node)
               (some #(contains? node %) nested-schema-keys)))

        get-node-values
        (fn [node-map]
          (->> (:node node-map)
               (remove nested-schema-entry?)
               (map (fn [entry]
                      {:path (:path node-map)
                       :value entry}))))]

    (->> {:path [], :node sch}
         (tree-seq contains-nested-schemas? get-nested-schemas)
         (mapcat get-node-values))))


(defn zen-dsl-seq [ztx sym-def]
  (if (= #{'zen/schema}
         (:zen/tags sym-def))
    (sch-seq sym-def)
    :TODO))

