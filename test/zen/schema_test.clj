(ns zen.schema-test
  (:require
   [zen.core :as zen]
   [matcho.core :as matcho]
   [clojure.test :refer [deftest is testing]]))

(deftest test-schema

  (def ctx (zen/new-context {:unsafe true}))

  (zen/read-ns ctx 'zen.tests.schema)

  (is (= 1 1))

  (doseq [case-nm (zen/get-tag ctx 'zen.tests.schema/test-case)]
    (let [{title :title schema :schema cs :cases} (zen/get-symbol ctx case-nm)]
      (testing title
        (println "***" title)
        (doseq [[k {v? :valid exmpl :example exp :result :as c}] cs]
          (let [{errs :errors} (zen/validate-schema ctx schema exmpl)]
            (when (and v? (not (empty? errs)))
              (println "ERR:" errs)
              (println k "Expected valid :"  schema "\n=>" (pr-str exmpl) "\n=>" (pr-str errs) "\n\n")
              (is (empty? errs)))
            (when exp
              (let [merrs (matcho/match* errs exp)]
                (when-not (empty? merrs)
                  (println k "Expected :"  schema "\n=>" (pr-str exmpl) "\nexpeceted: " (pr-str exp) "\ngot:" (pr-str errs))
                  (println merrs "\n")
                  (matcho/match errs exp)))))))))

  )
