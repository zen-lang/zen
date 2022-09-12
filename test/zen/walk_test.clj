(ns zen.walk-test
  (:require [zen.walk :as sut]
            [zen.core]
            [clojure.test :as t]
            [matcho.core :as matcho]))


(def rest-ns
  '{ns rest

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

    .api-op
    {:zen/tags #{zen/schema}
     :type zen/case
     :case [{:when {:type zen/symbol} :then {:type zen/symbol :tags #{op}}}
            {:when {:type zen/map} :then {:type zen/map
                                          :confirms #{op}}}]}

    api
    {:zen/tags #{zen/tag zen/schema}
     :type zen/map
     :keys {:apis   {:type zen/set :every {:type zen/symbol :tags #{api}}}
            :GET    {:confirms #{.api-op}}
            :POST   {:confirms #{.api-op}}
            :PUT    {:confirms #{.api-op}}
            :DELETE {:confirms #{.api-op}}
            :PATCH  {:confirms #{.api-op}}}
     :key {:type zen/case
           :case [{:when {:type zen/string}
                   :then {:type zen/string}}
                  {:when {:type zen/vector}
                   :then {:type zen/vector
                          :every {:type zen/keyword}
                          :minItems 1
                          :maxItems 1}}]}
     :values {:type zen/map
              :confirms #{api}}}})


(t/deftest sch-seq-test
  (t/testing "any dsl traversing"
    (def ztx (zen.core/new-context))

    (matcho/match
      (sut/zen-dsl-seq
        ztx
        '{:zen/tags #{zen/schema}
          :type zen/map
          :confirms #{foo}
          :zen/desc "2"
          :keys {:a {:type zen/string
                     :zen/desc "3"}
                 :b {:type zen/vector
                     :zen/desc "4"
                     :every {:type zen/map
                             :zen/desc "5"
                             :keys {:c {:zen/desc "6"
                                        :type zen/any}}}}}})

      [{:path [:confirms]                          :value #{'foo}}
       {:path [:keys :a :type]                     :value 'zen/string}
       {:path [:keys :a :zen/desc]                 :value "3"}
       {:path [:keys :b :every :keys :c :type]     :value 'zen/any}
       {:path [:keys :b :every :keys :c :zen/desc] :value "6"}
       {:path [:keys :b :every :type]              :value 'zen/map}
       {:path [:keys :b :every :zen/desc]          :value "5"}
       {:path [:keys :b :type]                     :value 'zen/vector}
       {:path [:keys :b :zen/desc]                 :value "4"}
       {:path [:type]                              :value 'zen/map}
       {:path [:zen/desc]                          :value "2"}
       {:path [:zen/tags]                          :value #{'zen/schema}} #_"NOTE: zen itself, not schema. move? Same for :zen/desc"
       nil])

    (zen.core/load-ns ztx rest-ns)

    (matcho/match
      (sut/zen-dsl-seq
        ztx
        '{:zen/tags #{rest/api}

          :GET      search
          "$export" {:POST export}
          [:id]     {:GET read}})

      [{:path ["$export" :POST nil] :value 'export}
       {:path [:GET nil]            :value 'search}
       {:path [:zen/tags]           :value #{'rest/api}} #_"NOTE: zen itself, not schema. move? Same for :zen/desc"
       {:path [[:id] :GET nil]      :value 'read}
       nil])

    (matcho/match
      (sut/zen-dsl-seq
        ztx
        '{:zen/tags #{rest/rpc}
          :params {:type zen/map
                   :require #{:foo}
                   :keys {:foo {:type zen/string}}}
          :result {:type zen/map
                   :require #{:bar}
                   :keys {:bar {:type zen/string}}}})

      [{:path [:params :keys :foo :type nil] :value 'zen/string}
       {:path [:params :require nil]         :value #{:foo}}
       {:path [:params :type nil]            :value 'zen/map}
       {:path [:result :keys :bar :type]     :value 'zen/string}
       {:path [:result :require nil]         :value #{:bar}}
       {:path [:result :type nil]            :value 'zen/map}
       {:path [:zen/tags nil]                :value #{'rest/rpc}} #_"NOTE: zen itself, not schema. move? Same for :zen/desc"
       nil])))
