(ns zen.changes-test
  (:require [zen.changes :as sut]
            [clojure.test :as t]
            [matcho.core :as matcho]))


(t/deftest ^:kaocha/pending changes-test
  (t/testing "ns remove"
      (def old-ztx
        {:ns '{a {ns a}}})

      (def new-ztx
        {:ns '{a {ns a}}})

      (matcho/match (sut/check-compatible old-ztx new-ztx)
                    {:status :ok
                     :errors empty?}))

  (t/testing "ns remove"
      (def old-ztx
        {:ns '{a {ns a
                  import #{b}}
               b {ns b}}})

      (def new-ztx
        {:ns '{a {ns a}}})

      (matcho/match (sut/check-compatible old-ztx new-ztx)
                    {:status :error,
                     :errors
                     [{:type :namespace/lost,
                       :namespaces '#{b}}
                      nil]}))

  (t/testing "symbol remove"
      (def old-ztx
        {:ns '{a {ns a
                  import #{b}
                  sym {:zen/tags #{zen/schema}
                       :confirms #{b/sym}}}
               b {ns b
                  sym {:zen/tags #{zen/schema}
                       :type zen/string}}}})

      (def new-ztx
        {:ns '{a {ns a
                  import #{b}
                  sym {:zen/tags #{zen/schema}
                       :confirms #{b/sym2}}}
               b {ns b
                  sym2 {:zen/tags #{zen/schema}
                        :type zen/string}}}})

      (matcho/match (sut/check-compatible old-ztx new-ztx)
                    {:status :error,
                     :errors
                     [{:type :symbol/lost,
                       :ns-syms '{b #{sym}}}]}))

  #_(t/testing "schema breaking"
      (def old-ztx
        {:ns '{a {ns a
                  import #{b}
                  sym {:zen/tags #{zen/schema}
                       :confirms #{b/sym}}}
               b {ns b
                  sym {:zen/tags #{zen/schema}
                       :type zen/number}}}})

      (def new-ztx
        {:ns '{a {ns a
                  import #{b}
                  sym {:zen/tags #{zen/schema}
                       :confirms #{b/sym}}}
               b {ns b
                  sym {:zen/tags #{zen/schema}
                       :type zen/integer}}}})

      (matcho/match (sut/check-compatible old-ztx new-ztx)
                    {:status :error
                     :errors [{}
                              nil]})))
