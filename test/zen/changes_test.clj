(ns zen.changes-test
  (:require [zen.changes :as sut]
            [zen.core]
            [clojure.test :as t]
            [matcho.core :as matcho]))


(t/deftest changes-test
  (t/testing "ns remove"
    (def old-ztx (zen.core/new-context))
    (zen.core/load-ns old-ztx '{ns a})

    (def new-ztx (zen.core/new-context))
    (zen.core/load-ns new-ztx '{ns a})

    (matcho/match (sut/check-compatible old-ztx new-ztx)
                  {:status :ok
                   :changes empty?}))

  (t/testing "ns remove"
    (def old-ztx (zen.core/new-context))
    (zen.core/load-ns old-ztx '{ns b})
    (zen.core/load-ns old-ztx '{ns a import #{b}})

    (def new-ztx (zen.core/new-context))
    (zen.core/load-ns new-ztx '{ns a})
    (zen.core/load-ns new-ztx '{ns c})

    (matcho/match (sut/check-compatible old-ztx new-ztx)
                  {:status :changed,
                   :changes
                   [{:type :namespace/lost
                     :namespace 'b}
                    {:type :namespace/new
                     :namespace 'c}
                    nil]}))

  (t/testing "symbol remove"
    (def old-ztx (zen.core/new-context))
    (zen.core/load-ns old-ztx '{ns b
                                sym {:zen/tags #{zen/schema}
                                     :type zen/string}})
    (zen.core/load-ns old-ztx '{ns a
                                import #{b}
                                sym {:zen/tags #{zen/schema}
                                     :confirms #{b/sym}}})

    (def new-ztx (zen.core/new-context))
    (zen.core/load-ns new-ztx '{ns b
                                sym2 {:zen/tags #{zen/schema}
                                      :type zen/string}})
    (zen.core/load-ns new-ztx '{ns a
                                import #{b}
                                sym {:zen/tags #{zen/schema}
                                     :confirms #{b/sym}}})

    (matcho/match (sut/check-compatible old-ztx new-ztx)
                  {:status :changed,
                   :changes [{:type :symbol/lost,
                             :symbol 'b/sym}
                            nil]}))

  (t/testing "schema changes"
    (def old-ztx (zen.core/new-context))
    (zen.core/load-ns old-ztx '{ns b
                                sym {:zen/tags #{zen/schema}
                                     :type zen/map
                                     :keys {:foo {:type zen/number}
                                            :bar {:type zen/string}
                                            :baz {:type zen/any}}}})
    (zen.core/load-ns old-ztx '{ns a
                                import #{b}
                                sym {:zen/tags #{zen/schema}
                                     :confirms #{b/sym}}})

    (def new-ztx (zen.core/new-context))
    (zen.core/load-ns new-ztx '{ns b
                                sym {:zen/tags #{zen/schema}
                                     :type zen/map
                                     :keys {:foo  {:type zen/integer}
                                            :bar  {:type zen/string}
                                            :quux {:type zen/any}}}})
    (zen.core/load-ns new-ztx '{ns a
                                import #{b}
                                sym {:zen/tags #{zen/schema}
                                     :confirms #{b/sym}}})

    (matcho/match (sut/check-compatible old-ztx new-ztx)
                  {:status :changed
                   :changes [{:type   :schema/removed
                             :sym    'b/sym
                             :path   [:keys :baz]
                             :attr   :type
                             :before 'zen/any
                             :after  nil}
                            {:type   :schema/updated
                             :sym    'b/sym
                             :path   [:keys :foo]
                             :attr   :type
                             :before 'zen/number
                             :after  'zen/integer}
                            {:type   :schema/added
                             :sym    'b/sym
                             :path   [:keys :quux]
                             :attr   :type
                             :before nil
                             :after  'zen/any}
                            nil]})))
