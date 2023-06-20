(ns zen.valpending-test
  (:require
   [clojure.java.io]
   [clojure.test :as t]
   [zen.core :as zen]
   [zen.store]
   [zen.utils]
   [zen.v2-validation :as v]
   [zen.validation]))

(t/deftest ^:kaocha/pending is-key-extension
  (def ztx (zen/new-context {:unsafe true}))

  (def ex-ns
    '{ns extension-ns

      ext-1
      {:zen/tags #{zen/is-key zen/schema}
       :type zen/vector
       :every {:type zen/integer}}

      myns/ext-2
      {:zen/tags #{zen/is-key zen/schema}
       :type zen/vector
       :every {:type zen/string}}})

  (zen/load-ns ztx ex-ns)

  (t/is (empty? (zen/errors ztx)))

  (def sch
    '{:zen/tags #{zen/schema}
      :type zen/map
      :ext-1 [1 2 3]
      :myns/ext-2 ["a" "string" "vector"]})

  (t/is (empty? (:errors (zen/validate ztx #{'zen/schema} sch)))))

(t/deftest ^:kaocha/pending validation-compatibility-test
  (def rest-ns
    '{ns rest

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
       :values {:confirms #{api}}
       :keys {:apis   {:type zen/set :every {:type zen/symbol :tags #{api}}}
              :GET    {:confirms #{.api-op}}
              :POST   {:confirms #{.api-op}}
              :PUT    {:confirms #{.api-op}}
              :DELETE {:confirms #{.api-op}}
              :PATCH  {:confirms #{.api-op}}}
       :key {:type zen/case
             :case [{:when {:type zen/string}
                     :then {:type zen/string}}
                    {:when {:type zen/keyword}
                     :then {:type zen/keyword}}
                    {:when {:type zen/vector}
                     :then {:type zen/vector
                            :every {:type zen/keyword}
                            :minItems 1
                            :maxItems 1}}]}}})

  (def myns
    '{ns myns
      import #{rest}

      engine
      {:zen/tags #{rest/op-engine zen/schema}}

      op
      {:zen/tags #{rest/op}
       :engine engine}


      other-api
      {:zen/tags #{rest/api}
       :DELETE op}

      myapi
      {:zen/tags #{rest/api}
       :apis     #{other-api}
       :GET      op
       "$export" {:POST op}
       [:id]     {:GET op}}})

  (t/testing "composing map validations"

    (t/testing "previous validation engine with current zen.edn"
      (with-redefs [v/validate zen.validation/validate
                    zen/validate zen.validation/validate
                    v/validate-schema zen.validation/validate-schema
                    v/validate-schema zen.validation/validate-schema]

        (def ztx (zen/new-context))

        (t/testing "zen ns load"
          (t/is (empty? (zen/errors ztx))))

        (swap! ztx assoc :errors [])

        (t/testing "loading rest ns"
          (zen/load-ns ztx rest-ns)
          (t/is (empty? (zen/errors ztx))))

        (swap! ztx assoc :errors [])

        (t/testing "loading myns"
          (zen/load-ns ztx myns)
          (t/is (empty? (zen/errors ztx))))))))
