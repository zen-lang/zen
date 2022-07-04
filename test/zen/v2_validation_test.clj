(ns zen.v2-validation-test
  (:require
   #_[com.rpl.specter :as s]
   [zen.utils]
   [zen.effect :as fx]
   [zen.test-runner :as r]
   [clojure.test :refer [deftest is]]
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
   (= (dissoc (zen.utils/get-symbol ztx 'zen.tests.confirms-test/confirms-resolved) :zen/file :zen/name)
      (v/resolve-confirms ztx (zen.utils/get-symbol ztx 'zen.tests.confirms-test/to-test)))))

(comment
  (do
    (def ztx (zen/new-context {:unsafe true}))

    (r/zen-read-ns ztx 'zen.tests.confirms-test)

    (defn confirms-defined? [sch]
      (and (get sch :confirms) (set? (:confirms sch))))

    ;; tests from old version
    ;; zen.fhir -> optimize
    ;; load testing -> memory consumption (plannet in total), hetzner (test)

    (def confirms-walker
      (s/comp-paths
       (s/recursive-path [] p
                         (s/if-path #(confirms-defined? %)
                                    #_(s/stay-then-continue [p])
                                    (s/stay-then-continue [s/MAP-VALS map? p])
                                    [s/MAP-VALS map? p]))))

    (s/select confirms-walker
              (zen.utils/get-symbol ztx 'zen.tests.confirms-test/to-test))

    (defn strip-meta [sch]
      (let [sch* (transient sch)]
        (-> (if (confirms-defined? sch*) (dissoc! sch* :confirms) sch*)
            (dissoc sch* :zen/file :zen/name :zen/tags :zen/desc)
            (persistent!))))

    (s/compiled-transform confirms-walker

                          (fn [sch]
                            (loop [cs (seq (:confirms sch))
                                   merged #{}
                                   sch* (strip-meta sch)]
                              (cond
                                (and (empty? cs) (confirms-defined? sch*))
                                (strip-meta sch*)
                                #_(recur (:confirms sch*) merged (strip-meta sch*))

                                (empty? cs) (strip-meta sch*)

                                (contains? merged (first cs)) (recur (rest cs) merged sch*)

                                :else
                                (let [right (zen.utils/get-symbol ztx (first cs))]
                                  (recur (rest cs)
                                         (conj merged (first cs))
                                         (zen.v2-validation/deep-merge sch* right))))))

                          (zen.utils/get-symbol ztx 'hl7-fhir-us-davinci-pdex-plan-net.plannet-Organization/schema)

                          #_(zen.utils/get-symbol ztx 'zen.tests.confirms-test/to-test))

    (zen.core/read-ns ztx 'hl7-fhir-us-davinci-pdex-plan-net.plannet-Organization)

    (s/select (map-key-walker :confirms)
              {:key {:a 1} :confirms 2})))
