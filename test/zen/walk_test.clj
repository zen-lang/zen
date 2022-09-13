(ns zen.walk-test
  (:require [zen.walk :as sut]
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


(t/deftest sch-seq-test
  (t/testing "schema traversing"
    (def ztx (zen.core/new-context))
    (zen.core/load-ns ztx '{:ns myns
                            foo {:zen/tags #{zen/schema}
                                 :type zen/map
                                 :require #{:a}}

                            mysch
                            {:zen/tags #{zen/schema}
                             :type zen/map
                             :confirms #{myns/foo}
                             :zen/desc "2"
                             :keys {:a {:type zen/string
                                        :zen/desc "3"}
                                    :b {:type zen/vector
                                        :zen/desc "4"
                                        :every {:type zen/map
                                                :zen/desc "5"
                                                :keys {:c {:zen/desc "6"
                                                           :type zen/any}}}}}}})

    (t/is (empty? (zen.core/errors ztx)))

    (matcho/match
      (sut/zen-dsl-seq ztx (zen.core/get-symbol ztx 'myns/mysch))
      [{:path [:confirms]                          :value #{'myns/foo}}
       {:path [:keys :a :type]                     :value 'zen/string}
       {:path [:keys :a :zen/desc]                 :value "3"}
       {:path [:keys :b :every :keys :c :type]     :value 'zen/any}
       {:path [:keys :b :every :keys :c :zen/desc] :value "6"}
       {:path [:keys :b :every :type]              :value 'zen/map}
       {:path [:keys :b :every :zen/desc]          :value "5"}
       {:path [:keys :b :type]                     :value 'zen/vector}
       {:path [:keys :b :zen/desc]                 :value "4"}
       #_{:path [:require]                           :value #{:a}}
       {:path [:type]                              :value 'zen/map}
       {:path [:zen/desc]                          :value "2"}
       {:path [:zen/name]                          :value 'myns/mysch}
       {:path [:zen/tags]                          :value #{'zen/schema}} #_"NOTE: zen itself, not schema. move? Same for :zen/desc"
       nil]))

  (t/testing "rpc method dsl"
    (zen.core/load-ns ztx rest-ns)
    (zen.core/load-ns ztx '{:ns myns3
                            :import #{rest}

                            myrpc
                            {:zen/tags #{rest/rpc}
                             :params {:type zen/map
                                      :require #{:foo}
                                      :keys {:foo {:type zen/string}}}
                             :result {:type zen/map
                                      :require #{:bar}
                                      :keys {:bar {:type zen/string}}}}})

    (t/is (empty? (zen.core/errors ztx)))

    (matcho/match
      (sut/zen-dsl-seq ztx (zen.core/get-symbol ztx 'myns3/myrpc))
      [{:path [:params :keys :foo :type nil] :value 'zen/string}
       {:path [:params :require nil]         :value #{:foo}}
       {:path [:params :type nil]            :value 'zen/map}
       {:path [:result :keys :bar :type]     :value 'zen/string}
       {:path [:result :require nil]         :value #{:bar}}
       {:path [:result :type nil]            :value 'zen/map}
       {:path [:zen/name nil]                :value 'myns3/myrpc}
       {:path [:zen/tags nil]                :value #{'rest/rpc}} #_"NOTE: zen itself, not schema. move? Same for :zen/desc"
       nil]))

  (t/testing "rest api dsl"
    (zen.core/load-ns ztx rest-ns)
    (zen.core/load-ns ztx '{:ns myns2
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
                                       :methods {:GET op}
                                       :routes  {"$export" {:methods {:POST op}}
                                                 [:id]     {:methods {:GET op}}}}}})

    (t/is (empty? (zen.core/errors ztx)))

    (matcho/match
      (sut/zen-dsl-seq ztx (zen.core/get-symbol ztx 'myns2/myapi))
      [#_{:path [:DELETE nil] :value 'myns2/delete}
       {:path [:routing :apis nil]                            :value #{'myns2/other-api}}
       {:path [:routing :methods :GET nil]                    :value 'myns2/op}
       {:path [:routing :routes "$export" :methods :POST nil] :value 'myns2/op}
       {:path [:routing :routes [:id] :methods :GET nil]      :value 'myns2/op}
       {:path [:zen/name]                                     :value 'myns2/myapi}
       {:path [:zen/tags]                                     :value #{'rest/api}} #_"NOTE: zen itself, not schema. move? Same for :zen/name"
       nil])))
