(ns zen.generative-bench)

(defn rand-str [len]
  (apply str (take len (repeatedly #(char (+ (rand 26) 65))))))

(defn scale-factor [depth branch]
  (* depth branch branch))

(def leaves-cfg
  {:string
   (fn [_]
     [{:type 'zen/string} (rand-str 6)])

   :regex-string
   (fn [scale]
     [{:type 'zen/string
       :minLength 3
       :maxLength (* 3 scale)
       :regex "[0-9].+"}
      (->> (repeat "420")
           (take (int (/ scale 2)))
           (apply str))])

   :set
   (fn [scale]
     [{:type 'zen/set
       :minItems 1
       :maxItems scale
       :every {:type 'zen/integer}}
      (->> (repeatedly #(rand-int 100))
           (take (int (/ scale 2)))
           set)])

   :vector
   (fn [scale]
     [{:type 'zen/vector
       :minItems 2
       :maxItems scale
       :every {:type 'zen/string
               :regex "hello"}}
      (vec (take (int (/ scale 2)) (repeat "hello")))])

   :enum
   (fn [scale]
     (let [enums (repeatedly scale #(rand-str 4))]
       [{:type 'zen/string
         :enum
         (->> (shuffle enums)
              (mapv (fn [e] {:value e})))}
        (first enums)]))})

(defn gen-leaves [scale-factor]
  (let [index (vals leaves-cfg)]
    (doall (repeatedly scale-factor #(apply (rand-nth index) [scale-factor])))))

(def node-cfg
  {:keys
   {:fn
    (fn [schemas depth branch]
      (let [pick [(keyword (rand-str 6)) (first schemas)]
            other
            (->> (repeatedly (- branch 1) #(rand-str 6))
                 (map keyword)
                 (map (fn [k] [k (rand-nth schemas)]))
                 doall)]

        [{:type 'zen/map
          :keys
          (->> (conj other pick)
               (mapcat (fn [[k [sch _]]] [k sch]))
               (apply hash-map))}

         (->> (conj other pick)
              (mapcat (fn [[k [_ data]]] [k data]))
              (apply hash-map))]))}

   :require
   {:depends-on :keys
    :fn
    (fn [schemas depth branch]
      (let [scale (int (/ branch 2))
            [sch data] (first schemas)]
        [(assoc sch :require
                (->> (:keys sch)
                     keys
                     (take scale)
                     set))
         data]))}

   :when
   {:fn
    (fn [schemas depth branch]
      (let [[sch data] (first schemas)
            oth (->> (repeatedly (- branch 1) #(rand-nth schemas))
                     (map (fn [[sch* _]]
                            {:when sch*})))]
        [{:type 'zen/case
          :case (conj (vec oth) {:when sch :then sch})}
         data]))}})

(defn gen-node [schemas depth branch]
  (let [node-fn
        (->> (vals node-cfg)
             (remove :depends-on)
             (map :fn)
             (rand-nth))
        node (apply node-fn [schemas depth branch])
        dependants
        (->> (vals node-cfg)
             (filter :depends-on)
             (filter (fn [{:keys [depends-on]}]
                       (contains? (first node) depends-on))))]
    (->> (random-sample 0.5 dependants)
         (reduce (fn [schemas* {:keys [fn]}]
                   (let [node* (apply fn [schemas* depth branch])]
                     (conj (next schemas*) node*)))
                 (conj schemas node))
         first)))

(defn gen-schema* [depth branch]
  (loop [schemas (gen-leaves (scale-factor depth branch))
         cur-depth 0]
    (cond

      (= cur-depth depth) (first schemas)

      :else
      (let [sch (gen-node schemas depth branch)]
        (recur (conj schemas sch) (+ cur-depth 1))))))

(defn leaves-count [{:keys [depth branch]}]
  (Math/pow branch (- depth 1)))

(defn gen-schema [{:keys [depth branch]}]
  (let [[sch data] (gen-schema* depth branch)]
    [(assoc sch :zen/tags #{'zen/schema}) data]))

(comment

  (gen-schema* 3 3)

  )
