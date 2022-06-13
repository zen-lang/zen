(ns zen.bench
  (:require
   ;; start cider with :test alias to get the criterium dependency
   ;; and with :profile alias to launch async profiler's java agent
   [clojure.pprint]
   [clojure.java.io :as io]
   [clj-async-profiler.core :as prof]
   [criterium.core :as c]
   [zen.core :as zen]
   [zen.v2-validation :as v]))

(defn bench [pth]
  (doall
   (for [[k {:keys [schema-sym data]}] (read-string (slurp (io/resource pth)))]
     (let [schema-sym* (if (vector? schema-sym)
                         schema-sym
                         [schema-sym])

           schema-names (set (map #(symbol (name %) "schema")
                                  schema-sym*))

           ztx (zen/new-context {:unsafe true})]

       (doseq [sch schema-sym*]
         (assert (= :zen/loaded (zen.core/read-ns ztx (symbol sch)))))

       (println)
       (println)
       (println (str k " OLD VERSION ERRORS"))
       (clojure.pprint/pprint (zen.core/validate ztx schema-names data))

       (println)
       (println)
       (println (str k " NEW VERSION ERRORS"))
       (clojure.pprint/pprint (v/validate ztx schema-names data))

       (println)
       (println)
       (println (str k " OLD VERSION BENCH"))
       (c/with-progress-reporting
         (c/bench (zen.core/validate ztx schema-names data) #_:verbose))

       (println)
       (println)
       (println (str k " NEW VERSION BENCH"))
       (c/with-progress-reporting
         (c/bench (v/validate ztx schema-names data) #_:verbose))

       [(zen.core/validate ztx schema-names data) (v/validate ztx schema-names data)]))))

(comment

  ;; in order for bench to work extract zen fhir definitions to /pkg

  (do

    (def plannet (:data (:plannet-org-1 (read-string (slurp (io/resource "zen/bench_data.edn"))))))

    (def ztx (zen/new-context {:unsafe true}))

    (zen.core/read-ns ztx 'hl7-fhir-us-davinci-pdex-plan-net.plannet-Organization)

    (zen.core/validate ztx #{'hl7-fhir-us-davinci-pdex-plan-net.plannet-Organization/schema} plannet)

    (v/validate ztx #{'hl7-fhir-us-davinci-pdex-plan-net.plannet-Organization/schema} plannet))

  (do

    (def data (:data (:plannet-org-1 (read-string (slurp (io/resource "zen/bench_data.edn"))))))

    (def ztx (zen/new-context {:unsafe true}))

    (zen.core/read-ns ztx 'hl7-fhir-us-davinci-pdex-plan-net.plannet-Organization)

    (zen.core/read-ns ztx 'hl7-fhir-r4-core.Organization)

    (v/validate ztx #{'hl7-fhir-r4-core.Organization/schema
                      'hl7-fhir-us-davinci-pdex-plan-net.plannet-Organization/schema}
                data)

    (prof/profile {:event :alloc}
     (doseq [_ (range 5000)]
       (v/validate ztx #{'hl7-fhir-r4-core.Organization/schema
                         'hl7-fhir-us-davinci-pdex-plan-net.plannet-Organization/schema}
                   data)))

    #_(prof/profile #_{:event :alloc}
       (doseq [_ (range 5000)]
         (zen.core/validate ztx #{'hl7-fhir-r4-core.Organization/schema
                                  'hl7-fhir-us-davinci-pdex-plan-net.plannet-Organization/schema}
                            data))))

  (prof/list-event-types)

  (def srv (prof/serve-files 8080))

  (def res (bench "zen/bench_data.edn"))

  (def pt-sch (zen.core/get-symbol ztx 'hl7-fhir-r4-core.Patient/schema))

  (zen.core/validate ztx #{'zen/schema} pt-sch)

  (v/validate ztx #{'hl7-fhir-r4-core.Patient/schema} pt))
