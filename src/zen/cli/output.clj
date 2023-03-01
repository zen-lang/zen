(ns zen.cli.output
  (:require clojure.pprint))

(defn get-format
  [arguments]
  (some->>
   (filter #(clojure.string/starts-with? % "--format=") arguments)
   (first)
   (re-find #"--format=(.*)")
   (last)
   (keyword)))

(defmulti return
  (fn [data]
    (:format data))
  :default :pprint)

(defmethod return :identity
  [data]
  data)

(defmethod return :pprint
  [data]
  (clojure.pprint/pprint data))

(defmethod return :table
  [{result :zen.cli/result}]
  (-> (map #(update-keys % (comp clojure.string/capitalize name)) result)
      (clojure.pprint/print-table)))

(defmethod return :command
  [{result :zen.cli/result}]
  (println "Description:")
  (println (:description result))
  (println)
  (println "Usage:")
  (doseq [usage (:usage result)]
    (println (clojure.string/join " " (:path usage))
             (->> (:params usage)
                  (mapv #(str "[" % "]"))
                  (clojure.string/join " ")))))
