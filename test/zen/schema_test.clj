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
      (println "***" (or title case-nm))
      (doseq [[k {v? :valid exmpl :example exp :result}] cs]
        (let [{errs :errors} (zen/validate-schema ctx schema exmpl)]
          (when (and v? (not (empty? errs)))
            (println "CASE " k ": Expected valid :"  schema "\n=>" (pr-str exmpl) "\n=>" (pr-str errs) "\n\n")
            (is (empty? errs)))
          (when exp
            (let [merrs (matcho/match* errs exp)]
              (when-not (empty? merrs)
                (println "CASE " k ": Expected :"  schema "\n=>" (pr-str exmpl) "\nexpeceted: " (pr-str exp) "\ngot:" (pr-str errs))
                (println "match errors: " merrs "\n")
                (matcho/match errs exp))))))))

  )


(deftest alias-test
  #"TODO: add alias remove test"
  (def test-namespaces
    '{ns1 {ns   ns1
           sym1 {:foo :bar}

           tag1 {:zen/tags #{zen/tag}}}

      ns2 {ns    ns2
           sym21 {:foo1 :bar2}
           sym22 {:foo2 :bar2}

           tag21 {:zen/tags #{zen/tag}}
           tag22 {:zen/tags #{zen/tag}}

           tagged-sym1 {:zen/tags #{tag21}}
           tagged-sym2 {:zen/tags #{tag22}}}

      myns {ns     myns
            import #{ns1}
            alias  ns2

            sym1  ns1/sym1
            sym22 {:baz :quux}

            tag1 ns1/tag1
            tagged-sym1 {:zen/tags #{tag1}}
            tagged-sym2 {:zen/tags #{ns1/tag1}}

            tag22 {:zen/tags #{zen/tag}}
            tagged-sym21  {:zen/tags #{tag21}}
            tagged-sym221 {:zen/tags #{tag22}}
            tagged-sym222 {:zen/tags #{ns2/tag22}}}})

  (def ztx (zen/new-context {:unsafe true
                             :memory-store test-namespaces}))

  (zen/load-ns ztx (get test-namespaces 'myns))

  (is (empty? (zen/errors ztx)))

  (testing "symbol alias"
    (matcho/match
     (zen/get-symbol ztx 'myns/sym1)
     '{:zen/name ns1/sym1}))

  (testing "ns alias"
    (matcho/match
      (zen/get-symbol ztx 'myns/sym21)
      '{:zen/name ns2/sym21})

    (testing "monkey patch"
      (matcho/match
        (zen/get-symbol ztx 'myns/sym22)
        '{:zen/name myns/sym22})))

  (testing "tags alias"
    (testing "symbol alias"
      (is (= #{'myns/tagged-sym1 'myns/tagged-sym2}
             (zen/get-tag ztx 'myns/tag1)))

      (is (= #{'myns/tagged-sym1 'myns/tagged-sym2}
             (zen/get-tag ztx 'ns1/tag1))))

    (testing "ns alias"
      (is (= #{'myns/tagged-sym21 'ns2/tagged-sym1}
             (zen/get-tag ztx 'myns/tag21)))

      (is (= #{'myns/tagged-sym21 'ns2/tagged-sym1}
             (zen/get-tag ztx 'ns2/tag21)))

      (testing "monkey patch"
        (is (= #{'myns/tagged-sym221}
               (zen/get-tag ztx 'myns/tag22)))

        (is (= #{'myns/tagged-sym222 'ns2/tagged-sym2}
               (zen/get-tag ztx 'ns2/tag22)))))))
