(ns zen.changes-test
  (:require [zen.changes :as sut]
            [zen.core]
            [clojure.test :as t]
            [matcho.core :as matcho]))


(def rest-ns
  '{:ns rest

    rpc
    {:zen/tags #{zen/tag zen/schema}
     :type zen/map
     :keys {:params {:confirms #{zen/schema}}
            :result {:confirms #{zen/schema}}}}

    op-engine
    {:zen/tags #{zen/tag}
     :zen/desc "tag for op engines"}

    op
    {:zen/tags   #{zen/tag zen/schema}
     :type       zen/map
     :require    #{:engine}
     :schema-key {:key :engine}
     :keys       {:engine {:type zen/symbol :tags #{op-engine}}}}

    api-op
    {:zen/tags #{zen/schema}
     :type zen/case
     :case [{:when {:type zen/symbol} :then {:type zen/symbol :tags #{op}}}
            {:when {:type zen/map} :then {:type zen/map
                                          :confirms #{op}}}]}

    route-node
    {:zen/tags #{zen/schema}
     :type zen/map
     :keys {:apis    {:type zen/set
                      :every {:type zen/symbol :tags #{api}}}
            :routes  {:type zen/map
                      :key {:type zen/case
                            :case [{:when {:type zen/string}
                                    :then {}}
                                   {:when {:type zen/vector}
                                    :then {:type zen/vector
                                           :every {:type zen/keyword}
                                           :minItems 1
                                           :maxItems 1}}]}
                      :values {:confirms #{route-node}}}
            :methods {:type zen/map
                      :keys {:GET    {:confirms #{api-op}}
                             :POST   {:confirms #{api-op}}
                             :PUT    {:confirms #{api-op}}
                             :DELETE {:confirms #{api-op}}
                             :PATCH  {:confirms #{api-op}}}}}}

    api
    {:zen/tags #{zen/tag zen/schema}
     :type zen/map
     :keys {:routing {:confirms #{route-node}}}}})


(t/deftest changes-test
  (t/testing "ns remove"
    (def old-ztx (zen.core/new-context))
    (zen.core/load-ns old-ztx '{ns a})

    (def new-ztx (zen.core/new-context))
    (zen.core/load-ns new-ztx '{ns a})

    (matcho/match (sut/check-changes old-ztx new-ztx)
                  {:status :unchanged
                   :changes empty?}))

  (t/testing "ns remove"
    (def old-ztx (zen.core/new-context))
    (zen.core/load-ns old-ztx '{ns b})
    (zen.core/load-ns old-ztx '{ns a import #{b}})

    (def new-ztx (zen.core/new-context))
    (zen.core/load-ns new-ztx '{ns a})
    (zen.core/load-ns new-ztx '{ns c})

    (matcho/match (sut/check-changes old-ztx new-ztx)
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

    (matcho/match (sut/check-changes old-ztx new-ztx)
                  {:status :changed,
                   :changes [{:type :symbol/lost,
                              :symbol 'b/sym}
                             {:type :symbol/new,
                              :symbol 'b/sym2}
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

    (matcho/match (sut/check-changes old-ztx new-ztx)
                  {:status :changed
                   :changes [{:type   :schema/removed
                              :sym    'b/sym
                              :path   [:keys :baz :type nil]
                             :before 'zen/any
                             :after  nil}
                            {:type   :schema/updated
                             :sym    'b/sym
                             :path   [:keys :foo :type nil]
                             :before 'zen/number
                             :after  'zen/integer}
                            {:type   :schema/added
                             :sym    'b/sym
                             :path   [:keys :quux :type nil]
                             :before nil
                             :after  'zen/any}
                            nil]}))

  (t/testing "dsl changes"
    (t/testing "rpc"
      (def old-ztx (zen.core/new-context))

      (zen.core/load-ns old-ztx rest-ns)
      (zen.core/load-ns old-ztx
                        '{:ns myns
                          :import #{rest}

                          myrpc
                          {:zen/tags #{rest/rpc}
                           :params {:type zen/map
                                    :require #{:foo}
                                    :keys {:foo {:type zen/string}}}
                           :result {:type zen/map
                                    :require #{:bar}
                                    :keys {:bar {:type zen/string}}}}})

      (def new-ztx (zen.core/new-context))

      (zen.core/load-ns new-ztx rest-ns)
      (zen.core/load-ns new-ztx
                        '{:ns myns
                          :import #{rest}

                          myrpc
                          {:zen/tags #{rest/rpc}
                           :params {:type zen/map
                                    :keys {:foo {:type zen/string}}}
                           :result {:type zen/map
                                    :keys {:bar {:type zen/string}}}}})

      (matcho/match (sut/check-changes old-ztx new-ztx)
                    {:status :changed
                     :changes [{:type   :schema/removed
                                :sym    'myns/myrpc
                                :path   [:params :require nil]
                                :before #{:foo}
                                :after  nil}
                               {:type   :schema/removed
                                :sym    'myns/myrpc
                                :path   [:result :require nil]
                                :before #{:bar}
                                :after  nil}
                               nil]}))

    (t/testing "routing"
      (def old-ztx (zen.core/new-context))

      (zen.core/load-ns old-ztx rest-ns)
      (zen.core/load-ns old-ztx
                        '{:ns myns
                          :import #{rest}

                          engine
                          {:zen/tags #{rest/op-engine zen/schema}}

                          op
                          {:zen/tags #{rest/op}
                           :engine engine}

                          other-api
                          {:zen/tags #{rest/api}
                           :routing {:methods {:DELETE op}}}

                          myapi
                          {:zen/tags #{rest/api}
                           :routing {:apis    #{other-api}
                                     :methods {:GET op
                                               :POST op}
                                     :routes  {"$export" {:methods {:POST op}}
                                               [:id]     {:methods {:GET op}}}}}})

      (def new-ztx (zen.core/new-context))

      (zen.core/load-ns new-ztx rest-ns)
      (zen.core/load-ns new-ztx
                        '{:ns myns
                          :import #{rest}

                          engine
                          {:zen/tags #{rest/op-engine zen/schema}}

                          op
                          {:zen/tags #{rest/op}
                           :engine engine}

                          other-api
                          {:zen/tags #{rest/api}
                           :routing {:methods {:DELETE op
                                               :GET op}}}

                          myapi
                          {:zen/tags #{rest/api}
                           :routing {:apis    #{other-api}
                                     :routes  {"$export" {:methods {:POST op}}
                                               [:id]     {:methods {:GET op}}}}}})

      (matcho/match (sut/check-changes old-ztx new-ztx)
                    {:status :changed
                     :changes [{:type   :schema/added
                                :sym    'myns/other-api
                                :path   [:routing :methods :GET nil]
                                :before nil
                                :after  'myns/op}
                               {:type   :schema/removed
                                :sym    'myns/myapi
                                :path   [:routing :methods :GET nil]
                                :before 'myns/op
                                :after  nil}
                               {:type   :schema/removed
                                :sym    'myns/myapi
                                :path   [:routing :methods :POST nil]
                                :before 'myns/op
                                :after  nil}
                               nil]}))))
