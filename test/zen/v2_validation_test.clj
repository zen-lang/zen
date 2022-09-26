(ns zen.v2-validation-test
  (:require
   [clojure.java.io]
   [zen.utils]
   [zen.store]
   [zen.effect :as fx]
   [zen.test-runner :as r]
   [clojure.test :refer [deftest is testing]]
   [zen.v2-validation :as v]
   [zen.validation]
   [zen.core :as zen]
   [edamame.core]
   [matcho.core :as matcho]))

;; see slicing-test/zen-fx-engine-slicing-test
(defmethod fx/fx-evaluator 'zen.tests.slicing-test/slice-key-check
  [ztx {:keys [params path]} data]
  (if (= (get data (first params)) "fx-value")
    {:errors []}
    {:errors [{:message "wrong slice key value"
               :type "fx.apply"}]}))

(deftest implemented-validations

  (do
    (def ztx (zen/new-context {:unsafe true}))

    (r/zen-read-ns ztx 'zen.tests.require-test)

    (r/zen-read-ns ztx 'zen.tests.boolean-test)

    (r/zen-read-ns ztx 'zen.tests.case-test)

    (r/zen-read-ns ztx 'zen.tests.schema-key-test)

    (r/zen-read-ns ztx 'zen.tests.types-test)

    (def list-expr (zen.utils/get-symbol ztx 'zen.tests.types-test/expr))

    (is list-expr)

    (testing "lists are not expanded by default"
      (is (= '(print hello user zen.test/empty?) (get-in list-expr [:keys :list-value :const :value]))))

    (r/zen-read-ns ztx 'zen.tests.keyname-schemas-test)

    (r/zen-read-ns ztx 'zen.tests.map-test)

    (r/zen-read-ns ztx 'zen.tests.slicing-test)

    (r/zen-read-ns ztx 'zen.tests.confirms-test)

    (r/zen-read-ns ztx 'zen.tests.match-test)

    (r/zen-read-ns ztx 'zen.tests.key-schema-test)

    (r/zen-read-ns ztx 'zen.tests.core-validate-test)

    (r/zen-read-ns ztx 'zen.tests.effects-test)

    (r/zen-read-ns ztx 'zen.tests.fn-test)

    (r/run-tests ztx))

  (testing "valmode :extended"
    "keys, values and key impl dependent validation"
    (do
      (def ztx (zen/new-context {:unsafe true}))

      (r/zen-read-ns ztx 'zen.tests.routing-test)

      (is (empty? (zen/errors ztx))))))

(defn resolve-zen-ns [ztx]
  (->> (read-string (slurp (clojure.java.io/resource "zen.edn")))

       (map (fn [[k v]]
              [k (or (zen.utils/get-symbol ztx (zen.utils/mk-symbol 'zen k))
                     v)]))
       (into {})))

(deftest metadata-roundtrip

  (testing "zen ns is read and validated"
    (do
      (def ztx (zen/new-context {:unsafe true}))

      (zen/read-ns ztx 'zen)

      (:errors @ztx)

      (is (empty? (:errors @ztx)))))

  (testing "zen meta validates itself"

    (do
      (def ztx (zen/new-context {:unsafe true}))

      (zen.utils/get-symbol ztx 'zen/namespace)

      (def result (v/validate ztx #{'zen/namespace} (resolve-zen-ns ztx)))

      (is (empty? (:errors result))))))

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


(deftest values-validation-test
  (def myns
    '{ns myns

      my-simple-map-tag
      {:zen/tags #{zen/tag zen/schema}
       :type     zen/map
       :key      {:type zen/keyword}
       :values   {:type zen/string}}

      my-simple-map
      {:zen/tags #{my-simple-map-tag}

       :hello "world"}})

  (testing "current valiatdion"
    (def ztx (zen.core/new-context))
    (zen.core/load-ns ztx myns)
    (is (empty? (zen.core/errors ztx))))

  (testing "previous validation engine with previous zen.edn"
    (with-redefs [zen.v2-validation/validate zen.validation/validate]
      (def ztx (atom {}))
      (zen.core/load-ns ztx (->> (clojure.java.io/resource "v1/zen.edn") slurp edamame.core/parse-string))
      (zen.core/load-ns ztx myns)
      (is (empty? (zen.core/errors ztx))))))


(deftest set-validation-test
  (def myns
    '{ns myns

      my-set-schema
      {:zen/tags #{zen/schema}
       :type zen/map
       :keys {:strings
              {:type zen/set
               :every {:type zen/string
                       :minLength 2}}}}})

  (testing "current valiatdion"
    (def ztx (zen.core/new-context))
    (zen.core/load-ns ztx myns)

    (matcho/match (zen.core/validate ztx #{'myns/my-set-schema} {:strings #{"aa" "bb" "c"}})
                  {:errors [{:path [:strings "c" nil]}
                            nil]}))

  (testing "previous validation engine with previous zen.edn"
    (with-redefs [zen.v2-validation/validate zen.validation/validate]
      (def ztx (atom {}))
      (zen.core/load-ns ztx (->> (clojure.java.io/resource "v1/zen.edn") slurp edamame.core/parse-string))
      (zen.core/load-ns ztx myns)

      (matcho/match (zen.core/validate ztx #{'myns/my-set-schema} {:strings #{"aa" "bb" "c"}})
                    {:errors [{:path [:strings "c" nil]}
                              nil]}))))
