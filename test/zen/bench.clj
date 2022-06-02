(ns zen.bench
  (:require
   ;; start cider with :test alias to get the criterium dependency
   [clojure.java.io :as io]
   [criterium.core :as c]
   [zen.core :as zen]
   [zen.v2-validation :as v]))

(defn bench [pth]
  (doall
   (for [[k {:keys [schema-sym data]}] (read-string (slurp (io/resource pth)))]
     (let [schema-name (symbol (name schema-sym) "schema")
           ztx (zen/new-context {:unsafe true})]

       (assert (= :zen/loaded (zen.core/read-ns ztx (symbol schema-sym))))

       (println)
       (println)
       (println (str k " OLD VERSION"))
       (c/with-progress-reporting
         (c/bench (zen.core/validate ztx #{schema-name} data)
                  :verbose))

       (println)
       (println)
       (println (str k " NEW VERSION"))
       (c/with-progress-reporting
         (c/bench (v/validate ztx #{schema-name} data)
                  :verbose))

       [(zen.core/validate ztx #{schema-name} data)
        (v/validate ztx #{schema-name} data)]))))

(comment

  (def res (bench "zen/bench_data.edn"))

  (def pt-sch (zen.core/get-symbol ztx 'hl7-fhir-r4-core.Patient/schema))

  (zen.core/validate ztx #{'zen/schema} pt-sch)

  (v/validate ztx #{'hl7-fhir-r4-core.Patient/schema} pt))
