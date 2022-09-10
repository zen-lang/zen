(ns zen.changes-test
  (:require [zen.changes :as sut]
            [zen.core]
            [clojure.test :as t]
            [matcho.core :as matcho]))


(t/deftest changes-test
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
                       :namespace 'b}
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
                       :confirms #{b/sym}}}
               b {ns b
                  sym2 {:zen/tags #{zen/schema}
                        :type zen/string}}}})

      (matcho/match (sut/check-compatible old-ztx new-ztx)
                    {:status :error,
                     :errors [{:type :symbol/lost,
                               :symbol 'b/sym}
                              nil]}))

  (t/testing "schema breaking"
    (def old-ztx
      {:ns '{a {ns a
                import #{b}
                sym {:zen/tags #{zen/schema}
                     :confirms #{b/sym}}}
             b {ns b
                sym {:zen/tags #{zen/schema}
                     :type zen/map
                     :keys {:foo {:type zen/number}
                            :bar {:type zen/string}
                            :baz {:type zen/any}}}}}})

    (def new-ztx
      {:ns '{a {ns a
                import #{b}
                sym {:zen/tags #{zen/schema}
                     :confirms #{b/sym}}}
             b {ns b
                sym {:zen/tags #{zen/schema}
                     :type zen/map
                     :keys {:foo  {:type zen/integer}
                            :bar  {:type zen/string}
                            :quux {:type zen/any}}}}}})

    (matcho/match (sut/check-compatible old-ztx new-ztx)
                  {:status :error
                   :errors [{:type   :schema/changed
                             :sym    'b/sym
                             :path   [:keys :foo]
                             :attr   :type
                             :before 'zen/number
                             :after  'zen/integer}
                            {:type   :schema/removed
                             :sym    'b/sym
                             :path   [:keys :baz]
                             :attr   :type
                             :before 'zen/any
                             :after  nil}
                            {:type   :schema/added
                             :sym    'b/sym
                             :path   [:keys :quux]
                             :attr   :type
                             :before nil
                             :after  'zen/any}
                            nil]})))
