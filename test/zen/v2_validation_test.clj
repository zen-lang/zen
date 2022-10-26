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

      (zen.v2-validation/validate ztx
                                  #{'zen/schema
                                    'zen.tests.routing-test/Configuration
                                    'zen.tests.routing-test/nested-schema}
                                  (zen/get-symbol ztx 'zen.tests.routing-test/config))

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

(deftest concurerncy-compilation-npe-test
  (testing "validate by schema which currently is compiling"
    (dotimes [_ 100]
      (def ztx (zen.core/new-context))

      (def lock (promise))

      (zen.core/load-ns ztx '{:ns myns
                              sym {:zen/tags #{zen/schema}
                                   :type zen/map
                                   :keys {:a {:type zen/string}}}})

      (def futures
        (mapv (fn [n]
                (future
                  @lock
                  (zen.core/validate ztx
                                     #{'myns/sym}
                                     {:a "1"})))
              [1 2]))

      (deliver lock :unlock)

      (matcho/match (mapv (fn [req-fut] @req-fut) futures)
                    [{:errors empty?}
                     {:errors empty?}]))))

(comment

  (do
    (def ztx (zen/new-context))

    (zen/read-ns ztx 'lisp)
    (zen/read-ns ztx 'lisp-test)

    (zen/validate-schema ztx
                         (zen/get-symbol ztx 'lisp/expr)
                         (zen/get-symbol ztx 'lisp-test/vals_test))

    (zen/validate-schema ztx
                         {:zen/tags #{'zen/schema}
                          :zen/name 'example-schema
                          :type 'zen/case
                          :case [{:when {:type 'zen/apply :tags #{'zen/fn}}}]}
                         '(lisp/vals {:foo 12}))

    (zen/errors ztx)))

