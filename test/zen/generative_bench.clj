(ns zen.generative-bench)

(defn rand-str [len]
  (apply str (take len (repeatedly #(char (+ (rand 26) 65))))))

(def leaves-cfg
  {:string
   (fn []
     [{:type 'zen/string} (rand-str 6)])

   :regex-string
   (fn []
     [{:type 'zen/string
       :minLength 3
       :maxLength 7
       :regex "[0-9].+"}
      "420"])

   :set
   (fn []
     [{:type 'zen/set
       :minItems 1
       :maxItems 10
       :every {:type 'zen/integer}}
      (->> (repeatedly #(rand-int 100))
           (take (+ 1 (rand-int 10)))
           set)])

   :vector
   (fn []
     [{:type 'zen/vector
       :minItems 2
       :maxItems 4
       :every {:type 'zen/string
               :regex "hello"}}
      ["hello" "hello" "hello"]])

   :enum
   (fn []
     (let [enums (repeatedly 4 #(rand-str 4))]
       [{:type 'zen/string
         :enum
         (->> (shuffle enums)
              (map (fn [e] {:value e})))}
        (first enums)]))})

(defn gen-leaves [n]
  (let [index (vals leaves-cfg)]
    (doall (repeatedly n #(apply (rand-nth index) [])))))

(def node-cfg
  {:keys
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
             (apply hash-map))]))

   :when
   (fn [schemas depth branch]
     (let [[sch data] (first schemas)
           oth (->> (repeatedly (- branch 1) #(rand-nth schemas))
                    (map (fn [[sch* _]]
                           {:when sch*})))]
       [{:type 'zen/case
         :case (conj (vec oth) {:when sch :then sch})}
        data]))})

(defn gen-node [schemas depth branch]
  (apply (rand-nth (vals node-cfg)) [schemas depth branch]))

(defn gen-schema* [depth branch]
  (loop [schemas (gen-leaves (* depth branch branch))
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
