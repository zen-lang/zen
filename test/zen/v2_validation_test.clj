(ns zen.v2-validation-test
  (:require
   [zen.test-runner :as r]
   [clojure.test :refer [deftest is]]
   [zen.v2-validation :as v]
   [zen.core :as zen]))

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

    (r/run-tests ztx)))

(comment
  (do
    (def ztx (zen/new-context {:unsafe true}))

    (r/zen-read-ns ztx 'zen.tests.effects-test)

    (r/run-test ztx 'zen.tests.effects-test/fx-schema-test :v2)

    #_(r/run-tests ztx)))

;; edn
;; validator_test
;; optimize, and benchmark, super compilation
;; to implement - exclusive-keys
;; https://github.com/HealthSamurai/sansara/blob/master/box/zrc/aidbox/rest/acl.edn#L101

