(ns zen.cli.output
  (:require clojure.pprint))

(def ansi
  {:reset       "\u001B[0m"
   :color-green "\u001B[32m"
   :color-white "\u001B[37m"
   :color-red   "\u001B[31m"
   :bg-red      "\u001B[41m"
   :format-bold "\u001B[1m"})

(defn print-table
  ;; Custom clojure.pprint/print-table
  ([ks rows]
     (when (seq rows)
       (let [widths (map
                     (fn [k]
                       (apply max (count (str k)) (map #(count (str (get % k))) rows)))
                     ks)
             spacers (map #(apply str (repeat % "-")) widths)
             fmts (map #(str "%-" % "s") widths)
             fmt-row (fn [leader divider trailer row]
                       (apply str (interpose divider
                                             (for [[col fmt] (map vector (map #(get row %) ks) fmts)]
                                               (format fmt (str col))))))]
         (doseq [row rows]
           (println (fmt-row " " " " " " row))))))
  ([rows] (print-table (keys (first rows)) rows)))

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

(defmethod return :message
  [data]
  (if (= :ok (:zen.cli/status data))
    (println (str (:color-green ansi) (get-in data [:zen.cli/result :message])))
    (println (str (:color-red ansi) (get-in data [:zen.cli/result :message])))))

(defmethod return :command
  [{result :zen.cli/result}]
  (println (str (:format-bold ansi) "Description:" (:reset ansi)))
  (println "" (:description result))
  (println (str (:format-bold ansi) "Usage:" (:reset ansi)))
  (print-table
   (for [usage (:usage result)]
     {:command (->>
                [" "
                 (clojure.string/join " " (:path usage))
                 (some->>
                  (:params usage)
                  (seq)
                  (mapv #(str "[" % "]"))
                  (clojure.string/join " ")
                  (str " "))]
                (remove empty?)
                (apply str))
      :description 
      (when (:description usage)
        (str "- " (:description usage)))}))
  (when (seq (:examples result))
    (println (str (:format-bold ansi) "Examples:" (:reset ansi)))
    (print-table
     (for [[value desc] (:examples result)]
       {:1 value :2 desc}))))

(defmethod return :error
  [{result :zen.cli/result}]
  (if (seq result)
    (print-table
     (for [error result]
       {:1 (str (:format-bold ansi) (:color-red ansi) (clojure.string/upper-case (name (or (:type error) "error"))) (:reset ansi))
        :2 (str (:color-red ansi) (:zen.cli/file error)
                " "
                (some->> (remove nil? (into [(:resource error)] (:path error)))
                         (seq)
                         (vec))
                (:reset ansi))
        :3 (:message error)}))
    (println (str (:color-green ansi) "No errors found" (:reest ansi)))))
