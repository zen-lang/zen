(ns zen.valpending-test
  (:require
   [clojure.java.io]
   [zen.utils]
   [zen.store]
   [zen.test-runner :as r]
   [clojure.test :refer [deftest is testing]]
   [zen.v2-validation :as v]
   [zen.validation]
   [zen.core :as zen]
   [matcho.core :as matcho]))

(deftest ^:kaocha/pending is-key-extension
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

  (is (empty? (zen/errors ztx)))

  (def sch
    '{:zen/tags #{zen/schema}
      :type zen/map
      :ext-1 [1 2 3]
      :myns/ext-2 ["a" "string" "vector"]})

  (is (empty? (:errors (zen/validate ztx #{'zen/schema} sch)))))

(deftest ^:kaocha/pending validation-compatibility-test
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

  (testing "composing map validations"

    (testing "previous validation engine with current zen.edn"
      (with-redefs [zen.v2-validation/validate zen.validation/validate
                    zen.core/validate zen.validation/validate
                    zen.v2-validation/validate-schema zen.validation/validate-schema
                    zen.core/validate-schema zen.validation/validate-schema]

        (def ztx (zen.core/new-context))

        (testing "zen ns load"
          (is (empty? (zen.core/errors ztx))))

        (swap! ztx assoc :errors [])

        (testing "loading rest ns"
          (zen.core/load-ns ztx rest-ns)
          (is (empty? (zen.core/errors ztx))))

        (swap! ztx assoc :errors [])

        (testing "loading myns"
          (zen.core/load-ns ztx myns)
          (is (empty? (zen.core/errors ztx))))))))
