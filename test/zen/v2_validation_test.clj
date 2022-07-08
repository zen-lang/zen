(ns zen.v2-validation-test
  (:require
   [zen.utils]
   [zen.effect :as fx]
   [zen.test-runner :as r]
   [clojure.test :refer [deftest is]]
   [zen.v2-validation :as v]
   [zen.core :as zen]
   [zen.v2-validation :as v2]))

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

    (r/zen-read-ns ztx 'zen.tests.keyname-schemas-test)

    (r/zen-read-ns ztx 'zen.tests.map-test)

    (r/zen-read-ns ztx 'zen.tests.core-validate-test)

    (r/zen-read-ns ztx 'zen.tests.effects-test)

    (r/zen-read-ns ztx 'zen.tests.fn-test)

    (r/zen-read-ns ztx 'zen.tests.slicing-test)

    (r/zen-read-ns ztx 'zen.tests.confirms-test)

    (r/run-tests ztx)))

(deftest resolve-confirms-test

  (def ztx (zen/new-context {:unsafe true}))

  (r/zen-read-ns ztx 'zen.tests.confirms-test)

  (is
   (= (dissoc (zen.utils/get-symbol ztx 'zen.tests.confirms-test/confirms-resolved)
              :zen/file
              :zen/name)
      (v/resolve-confirms ztx (zen.utils/get-symbol ztx 'zen.tests.confirms-test/to-test))))

  (def data (dissoc (zen.utils/get-symbol ztx 'zen.tests.confirms-test/data-example)
                    :zen/file
                    :zen/name))

  (def resolved-results
    (v/validate-schema ztx
                       (v/resolve-confirms ztx (zen.utils/get-symbol ztx 'zen.tests.confirms-test/to-test))
                       data))

  (def confirms-results
    (v/validate ztx #{'zen.tests.confirms-test/to-test} data))

  (is (empty? (:errors resolved-results)))
  (is (empty? (:errors confirms-results))))

