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

    (r/zen-read-ns ztx 'zen.tests.fn-test)

    (r/zen-read-ns ztx 'zen.tests.slicing-test)

    (r/run-tests ztx)))

(comment

  ;; cases

  ;; validation type:

  ;; closed validation on empty map
  ;; open validation on map with explicit :validation-type
  ;; multiple :keys on one level due to dynamic schemas
  ;; open validation on map due to :key or :vals
  ;; single keys :validation

  ;; all functions that work on keys should add to :visited set!
  ;; double check dynamic functions!

  (do

    (def ztx (zen/new-context {:unsafe true}))

    (r/zen-read-ns ztx 'zen.tests.slicing-test)

    (r/run-tests ztx)


    ))

