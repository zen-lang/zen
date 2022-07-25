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

(comment

  (do

    (def ztx (zen/new-context {:unsafe true}))

    (r/zen-read-ns ztx 'zen.tests.slicing-test)

    (r/run-step ztx 'zen.tests.slicing-test/slicing-path-collision-unknown-key-bug-test 0)

    #_(get @ztx :zen.v2-validation/super)
    #_@ztx))

(deftest resolve-confirms-test

  (def ztx (zen/new-context {:unsafe true}))

  (r/zen-read-ns ztx 'zen.tests.confirms-test)

  (def data (dissoc (zen.utils/get-symbol ztx 'zen.tests.confirms-test/data-example)
                    :zen/file
                    :zen/name))

  (get @ztx :zen.v2-validation/super)

  (def result (v/validate ztx #{'zen.tests.confirms-test/to-test} data))

  (is (= [{:message "Could not resolve schema 'zen.tests.confirms-test/not-found-1",
           :path [:key-c 0 :key-d],
           :type "primitive-type",
           :schema
           ['zen.tests.confirms-test/to-test
            :confirms
            'zen.tests.confirms-test/b
            :key-c
            :every
            0
            :key-d
            :confirms]}
          {:message "Could not resolve schema 'zen.tests.confirms-test/not-found",
           :path [],
           :type "primitive-type",
           :schema ['zen.tests.confirms-test/to-test :confirms]}]
         (:errors result))))

;; TODOS
;; 1. make sure that supercompilation result is cached
;; 3. get rid of extra ops with state
;; 6. remove all extra data from state to reduce memory usage
;; 4. pass tests
;; 5. make sure that sch-path is always correct
;; 7. how should props validation work with sch-path?

;; WTF is _id extensions in plannet?

;; true supercompilation
;; 1. pass schema-path to compile-schema
;; 2. if rule has returned :super field cache it
;; 3. drop ruleset if cache already contains :super fn for this path
;; 4. when compilation is done compile all the cached :super rules into fns ??
;; 5. execute each supercompiled rule once in its closure

;; true supercompilation tests:
;; 1. rules that are on the same level of the same schema should be merged into :arg
;; 2. rules that are on the same level of different schemas that are linked w :confirms should also be merged

;; true supercompilation guarantees:
;; supercompiled function on each schema level is executed exactly once
;; each unique child schema validates data exactly once
;; shallow child schemas are deep merged in compile-time
