(ns zen.schema-test
  (:require
   [zen.core :as zen]
   [clojure.test :refer :all]))

(deftest test-schema

  (def ctx (zen/new-context {:unsafe true}))

  (zen/read-ns ctx 'zen.tests.schema)

  (is (= 1 1))
  (doseq [case-nm (zen/get-tag ctx 'zen.tests.schema/test-case)]
    (let [{title :title schema :schema cs :cases} (zen/get-symbol ctx case-nm)]
      (println "===" title)
      (println schema)
      (doseq [[k c] cs]
        (println "----" k c)
        (println (zen/validate-schema ctx schema (:example c))))))


  )
