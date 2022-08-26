(ns zen.v2-validation-test
  (:require
   [clojure.java.io]
   [zen.utils]
   [zen.store]
   [zen.effect :as fx]
   [zen.test-runner :as r]
   [clojure.test :refer [deftest is testing]]
   [zen.v2-validation :as v]
   [zen.core :as zen]))

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
      (is (= '(print hello user zen.tests.expr-alias/argument) (get-in list-expr [:my/lisp :expr]))))

    (r/zen-read-ns ztx 'zen.tests.keyname-schemas-test)

    (r/zen-read-ns ztx 'zen.tests.map-test)

    (r/zen-read-ns ztx 'zen.tests.core-validate-test)

    (r/zen-read-ns ztx 'zen.tests.effects-test)

    (r/zen-read-ns ztx 'zen.tests.fn-test)

    (r/zen-read-ns ztx 'zen.tests.slicing-test)

    (r/zen-read-ns ztx 'zen.tests.confirms-test)

    (r/zen-read-ns ztx 'zen.tests.match-test)

    (r/zen-read-ns ztx 'zen.tests.key-schema-test)

    (r/run-tests ztx)))

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
