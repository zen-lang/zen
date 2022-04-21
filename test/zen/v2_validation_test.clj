(ns zen.v2-validation-test
  (:require
   [clojure.walk]
   [matcho.core :as matcho]
   [clojure.test :refer [deftest is]]
   [zen.core :as zen]))

(def replacements
  {'zen.test/empty? empty?
   'zen.test/nil? empty?})

(defn translate-to-matcho [match]
  (clojure.walk/postwalk (fn [x] (get replacements x x)) match))

(defmulti do-step (fn [_ztx step] (get-in step [:do :type])))

(defmethod do-step :default [_ztx _step]
  (matcho/match :step-type-is-not-implemented true))

(defmethod do-step 'zen.test/validate [ztx step]
  (zen.core/validate ztx #{(get-in step [:do :schema])} (get-in step [:do :data])))

(defmethod do-step 'zen.test/validate-schema [ztx step]
  (let [sch (zen.core/get-symbol ztx (get-in step [:do :schema]))]
    (zen.core/validate ztx '#{zen/schema} sch)))

(defmulti report-step (fn [_ztx step _result _test-case] (get-in step [:do :type])))

(defmethod report-step :default [_ztx _step _result _test-case])

(defmethod report-step 'zen.test/validate [_ztx step result test-case]
  (println "## Test: " (or (:title test-case) (:id test-case)))
  (println "  step: " (:desc step) " \n  "  (get-in step [:do :schema]) "\n  " (get-in step [:do :data]))
  (println (:message (last (get-in result [:results 'zen.validation-test 'test-validation])))))

(defmethod report-step 'zen.test/validate-schema [ztx step result test-case]
  (let [sch (zen.core/get-symbol ztx (get-in step [:do :schema]))]
    (println "## Test: " (or (:title test-case) (:id test-case)))
    (println "  step: " (:desc step) " \n  " (get-in step [:do :schema]) "\n  " sch)
    (println (:message (last (get-in result [:results 'zen.validation-test 'test-validation]))))))

(defn run-tests [ztx]
  (doall
   (filter
    identity
    (for [test-name (zen.core/get-tag ztx 'zen.test/case)
          step      (:steps (zen/get-symbol ztx test-name))]
      (let [test-def (zen/get-symbol ztx test-name)
            step-res  (do-step ztx step)
            match-res (matcho/match step-res (translate-to-matcho (:match step)))]
        (when-not (true? match-res)
          (report-step ztx step match-res test-def)
          {:expected (:match step) :got step-res}))))))

(deftest test-validation
  (def ztx (zen.core/new-context {:unsafe true}))

  (zen.core/read-ns ztx 'zen.tests.require-test)

  (run-tests ztx)

  (comment
    (zen.core/read-ns ztx 'zen.all-tests)
    (zen.core/read-ns ztx 'v2.ztest.keys-test)
    (zen.core/read-ns ztx 'zen.schema-key-test)
    (zen.core/read-ns ztx 'zen.case-test)
    (zen.core/read-ns ztx 'zen.map-test)
    (zen.core/read-ns ztx 'zen.keyname-schemas-test)
    (zen.core/read-ns ztx 'zen.fn-test)
    (zen.core/read-ns ztx 'zen.effects-test)
    (zen.core/read-ns ztx 'zen.slicing-test)

    (zen.core/read-ns ztx 'zen.core-validate-test)))
