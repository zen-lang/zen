(ns zen.v2-validation-test
  (:require
   [zen.test-runner :as r]
   [clojure.test :refer [deftest is]]
   [zen.v2-validation :as v]
   [zen.core :as zen]))

  ;; 1. fix tests in edn
  ;; !!! 2. start with :schema-key
  ;; 2. analyze old validator tests, analyze aidbox usecases, write new tests
  ;; 2. optimize defmethod -> defrecord/deftype | ?
  ;; 3. try transients/transducers

;; TODO think about zen/types in error messages; format
;; TODO use pretty-type from v1

;; v2 validator use case - swap v1 for v2
;; check zen.fhir extension use case

(deftest implemented-validations
  (do

    (def ztx (zen/new-context {:unsafe true}))

    (r/zen-read-ns ztx 'zen.tests.require-test)

    (r/zen-read-ns ztx 'zen.tests.boolean-test)

    (r/zen-read-ns ztx 'zen.tests.types-test)

    (r/zen-read-ns ztx 'zen.tests.case-test)

    (r/run-tests ztx))

  )

;; to implement - exclusive-keys
;; https://github.com/HealthSamurai/sansara/blob/master/box/zrc/aidbox/rest/acl.edn#L101

;; also do not forget to remove :only-for as much as possible so that implementations are similar
