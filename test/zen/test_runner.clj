(ns zen.test-runner
  (:require
   [clojure.test :refer [is]]
   [clojure.walk]
   [matcho.core :as matcho]
   [zen.core :as zen]
   [zen.v2-validation :as v2]))

(def versions
  {:v1 #'zen/validate
   :v2 #'v2/validate})

(def replacements
  {'zen.test/empty? empty?
   'zen.test/nil? empty?})

(defn comp-fn [a b]
  (compare (str a) (str b)))

(defn translate-to-matcho [match]
  (clojure.walk/postwalk (fn [x] (get replacements x x)) match))

(defmulti do-step (fn [_ztx step version] (get-in step [:do :type])))

(defmethod do-step :default [_ztx _step version]
  (assert false "step type not found"))

(defmethod do-step 'zen.test/validate [ztx step version]
  (let [f (get versions version)
        sch (zen.core/get-symbol ztx (get-in step [:do :schema]))

        validated-sch (f ztx #{'zen/schema} (dissoc sch :zen/name :zen/file :zen/desc :zen/tags))]

    (is (empty? (:errors validated-sch)))

    (f ztx #{(get-in step [:do :schema])} (get-in step [:do :data]))))

(defmethod do-step 'zen.test/validate-schema [ztx step version]
  (let [f (get versions version)
        sch (zen.core/get-symbol ztx (get-in step [:do :schema]))]
    ;; TODO revisit this later
    ;; why this attrs are not in zen.edn?
    (f ztx #{'zen/schema} (dissoc sch :zen/name :zen/file :zen/tags :zen/desc))))

(defmulti report-step (fn [_ztx step _result _test-case] (get-in step [:do :type])))

(defmethod report-step :default [_ztx _step _result _test-case])

;; TODO fix report step so that it prints beautifully
(defmethod report-step 'zen.test/validate [_ztx step result test-case]
  (println "## Test: " (or (:title test-case) (:id test-case)))
  (println "  step: " (:desc step) " \n  "  (get-in step [:do :schema]) "\n  " (get-in step [:do :data]))
  (println (:message (last (get-in result [:results 'zen.validation-test 'test-validation])))))

(defmethod report-step 'zen.test/validate-schema [ztx step result test-case]
  (let [sch (zen.core/get-symbol ztx (get-in step [:do :schema]))]
    (println "## Test: " (or (:title test-case) (:id test-case)))
    (println "  step: " (:desc step) " \n  " (get-in step [:do :schema]) "\n  " sch)
    (println (:message (last (get-in result [:results 'zen.validation-test 'test-validation]))))))

(defn run-tests [ztx & versions]
  (doall
   (filter
    identity
    (for [test-name (zen.core/get-tag ztx 'zen.test/case)
          step      (:steps (zen/get-symbol ztx test-name))
          version (or (not-empty versions) [:v2 :v1])]
      (let [test-def (zen/get-symbol ztx test-name)
            scope-for (:only-for test-def)]
        (when (or (nil? scope-for) (contains? scope-for version))
          (let [step-res (-> (do-step ztx step version)
                             (update :errors (fn [errs] (sort-by :path comp-fn errs))))

                match-res (matcho/match step-res (translate-to-matcho (:match step)))]
            (when-not (true? match-res)
              #_(report-step ztx step match-res test-def)
              {:test test-name :desc (:desc step) :version version :expected (:match step) :got step-res}))))))))

;; TODO add id to steps in zen test
(defn run-step [ztx test-name step]
  (let [test-def (zen/get-symbol ztx test-name)
        _ (assert test-def (str "test not found " test-name))
        step (get (:steps test-def) step)
        step-res  (-> (do-step ztx step :v2)
                      (update :errors (fn [errs] (sort-by :path comp-fn errs))))
        match-res (matcho/match step-res (translate-to-matcho (:match step)))]
    (if-not (true? match-res)
      {:desc (:desc step) :expected (:match step) :got step-res}
      'passed)))

(defn run-test [ztx test-name & versions]
  (let [test-def (zen/get-symbol ztx test-name)]
    (assert test-def (str "test not found " test-name))
    (doall
     (filter
      identity
      (for [step (:steps test-def)
            version (or (not-empty versions) [:v1 :v2])]
        (when (or (nil? (:only-for test-def)) (contains? (:only-for test-def) version))
          (let [step-res  (-> (do-step ztx step version)
                              (update :errors (fn [errs] (sort-by :path comp-fn errs))))
                match-res (matcho/match step-res (translate-to-matcho (:match step)))]
            (when-not (true? match-res)
              #_(report-step ztx step match-res test-def)
              {:desc (:desc step) :version version :expected (:match step) :got step-res}))))))))

(defn zen-read-ns [ztx s]
  (is (= :zen/loaded (zen.core/read-ns ztx s))))
