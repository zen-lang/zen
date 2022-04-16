(ns zen.v2-validation-test
  (:require [matcho.core :as matcho]
            [clojure.test :refer [deftest]]
            [clojure.walk]
            [clojure.string :as str]
            [zen.core]))

;; (defmacro match-schema [sch data errs]
;;   `(let [res# (sub/validate-schema ztx ~sch ~data)]
;;      (matcho/match (:errors res#) ~errs)
;;      (:errors res#)))

;; (defmacro valid-schema [sch data]
;;   `(let [res# (sub/validate-schema ztx ~sch ~data)]
;;      (is (empty? (:errors res#)))
;;      (:errors res#)))

(def replacements
  {'zen.test/empty? empty?
   'zen.test/nil? empty?})

(defn translate-to-matcho [match]
  (clojure.walk/postwalk (fn [x] (get replacements x x)) match))

;; (translate-to-matcho {:key 'zen.test/empty?})

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
  (println "## Case: " (or (:title test-case) (:id test-case)))
  (println "  validate: " (:desc step) " \n  "  (get-in step [:do :schema]) "\n  " (get-in step [:do :data]))
  (println (:message (last (get-in result [:results 'zen.validation-test 'test-validation])))))

(defmethod report-step 'zen.test/validate-schema [ztx step result test-case]
  (let [sch (zen.core/get-symbol ztx (get-in step [:do :schema]))]
    (println "## Case: " (or (:title test-case) (:id test-case)))
    (println "  validate: " (:desc step) " \n  " (get-in step [:do :schema])"\n  " sch)
    (println (:message (last (get-in result [:results 'zen.validation-test 'test-validation]))))))

#_(deftest test-validation
  (def ztx (zen.core/new-context {:unsafe true}))
  ;; (zen.core/read-ns ztx 'zen.all-tests)
  (zen.core/read-ns ztx 'zen.require-test)
  (zen.core/read-ns ztx 'zen.keys-test)
  (zen.core/read-ns ztx 'zen.schema-key-test)
  (zen.core/read-ns ztx 'zen.case-test)
  (zen.core/read-ns ztx 'zen.map-test)
  (zen.core/read-ns ztx 'zen.keyname-schemas-test)
  (zen.core/read-ns ztx 'zen.fn-test)
  (zen.core/read-ns ztx 'zen.effects-test)
  (zen.core/read-ns ztx 'zen.slicing-test)

  (zen.core/read-ns ztx 'zen.core-validate-test)

  #_(matcho/match @ztx {:errors empty?})

  (doseq [test-case (zen.core/get-tags ztx 'zen.test/case)
          step      (:steps test-case)
          :let      [step-res  (do-step ztx step)
                     match-res (matcho/match step-res (translate-to-matcho (:match step)))]
          :when     (not= true match-res)]
    (report-step ztx step match-res test-case))

  (comment
    (time
      (dotimes [_ 1000]
        (doseq [test-case (zen.core/get-tags ztx 'zen.test/case)
                step      (:steps test-case)]
          (do-step ztx step))))))
