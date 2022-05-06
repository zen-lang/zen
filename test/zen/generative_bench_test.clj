(ns zen.generative-bench-test
  (:require
   #_[criterium.core :as c]
   [zen.core :as zen]
   [zen.generative-bench :refer :all]
   [zen.validation :as val-old]
   [zen.v2-validation :as valid]
   [clojure.test :refer :all]))

(defn validate-old [ztx sch data]
  (val-old/validate-node ztx
                         (assoc (val-old/new-validation-acc) :schema [])
                         sch
                         data))

;; TODO add :values to bench

(deftest depth-benchmark

  (def cfg {:depth 6 :branch 6})

  (def cfg {:depth 3 :branch 4})

  (def result (gen-schema cfg))

  (def sch (first result))

  (def data (second result))

  (def ztx-old (zen.core/new-context {:unsafe true}))
  (def ztx-new (zen.core/new-context {:unsafe true}))

  (is (empty? (:errors (zen/validate ztx-old '#{zen/schema} sch))))
  (is (empty? (:errors (valid/validate ztx-new '#{zen/schema} sch))))

  (is (empty? (:errors (validate-old ztx-old sch data))))
  (is (empty? (:errors (valid/validate-schema ztx-new sch data))))

  (println " ")
  (println "leaves count: " (leaves-count cfg))
  (println "bench results:")

  (println "old:")
  (time (doall (repeatedly 10 #(validate-old ztx-old sch data))))

  (println "new:")
  (time (doall (repeatedly 10 #(valid/validate-schema ztx-new sch data)))))

(comment

  "criterium bench"

  "2x faster on single schema"

  (c/with-progress-reporting (c/bench (validate-old ztx-old sch data) :verbose))

  (c/with-progress-reporting (c/bench (valid/validate-schema ztx-new sch data) :verbose))

  "2x faster on 10 validations of single schema"

  (c/with-progress-reporting (c/bench
                              (doseq [_ (range 10)]
                                (validate-old ztx-old sch data))
                              :verbose))

  (c/with-progress-reporting (c/bench
                              (doseq [_ (range 10)]
                                (valid/validate-schema ztx-new sch data))
                              :verbose))

  "2x faster on 10 validations of different schemas"

  (def schemas (map (fn [_] (gen-schema cfg)) (range 10)))

  (c/with-progress-reporting (c/bench
                              (doseq [[sch data] schemas]
                                (validate-old ztx-old sch data))
                              :verbose))

  (c/with-progress-reporting (c/bench
                              (doseq [[sch data] schemas]
                                (valid/validate-schema ztx-new sch data))
                              :verbose))

  )
