(ns zen.validation-test
  (:require [matcho.core :as matcho]
            [zen.validation :as sub]
            [clojure.test :refer [deftest is testing]]
            [zen.core]))


;; (defmacro match-schema [sch data errs]
;;   `(let [res# (sub/validate-schema ztx ~sch ~data)]
;;      (matcho/match (:errors res#) ~errs)
;;      (:errors res#)))

;; (defmacro valid-schema [sch data]
;;   `(let [res# (sub/validate-schema ztx ~sch ~data)]
;;      (is (empty? (:errors res#)))
;;      (:errors res#)))

(deftest test-validation
  (def ztx (zen.core/new-context {:unsafe true}))

  (zen.core/read-ns ztx 'zen.all-tests)
  (doseq [case (zen.core/get-tags ztx 'zen.test/case)]
    (println "Case:" (or (:title case) (:id case)))
    (doseq [{do :do match :match} (:steps case)]
      (let [res (sub/validate ztx (:schema do) (:data do))]
        (matcho/match res match)))))
