(ns zen.v2-validation-test
  (:require
   [clojure.walk]
   [matcho.core :as matcho]
   [clojure.test :refer [deftest is]]
   [zen.validation :as val-old]
   [zen.v2-validation :as valid]
   [zen.core :as zen]))

(def versions
  {:v1 zen.core/validate
   :v2 valid/validate})

(def replacements
  {'zen.test/empty? empty?
   'zen.test/nil? empty?})

(defn translate-to-matcho [match]
  (clojure.walk/postwalk (fn [x] (get replacements x x)) match))

(defmulti do-step (fn [_ztx step version] (get-in step [:do :type])))

(defmethod do-step :default [_ztx _step version]
  (matcho/match :step-type-is-not-implemented true))

(defmethod do-step 'zen.test/validate [ztx step version]
  (apply (get versions version) [ztx #{(get-in step [:do :schema])} (get-in step [:do :data])]))

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

(defn run-tests [ztx & versions]
  (doall
   (filter
    identity
    (for [test-name (zen.core/get-tag ztx 'zen.test/case)
          step      (:steps (zen/get-symbol ztx test-name))
          version (or (not-empty versions) [:v1 :v2])]
      (let [test-def (zen/get-symbol ztx test-name)
            scope-for (:only-for test-def)]
        (when (or (nil? scope-for) (contains? scope-for version))
          (let [step-res (-> (do-step ztx step version)
                             (update :errors #(sort-by :path %)))
                match-res (matcho/match step-res (translate-to-matcho (:match step)))]
            (when-not (true? match-res)
              (report-step ztx step match-res test-def)
              {:desc (:desc step) :version version :expected (:match step) :got step-res}))))))))

;; TODO add id to steps in zen test
(defn run-step [ztx test-name step]
  (let [test-def (zen/get-symbol ztx test-name)
        step (get (:steps test-def) step)
        step-res  (do-step ztx step :v2)
        match-res (matcho/match step-res (translate-to-matcho (:match step)))]
    (if-not (true? match-res)
      {:desc (:desc step) :expected (:match step) :got step-res}
      'passed)))

(defn run-test [ztx test-name & versions]
  (let [test-def (zen/get-symbol ztx test-name)]
    (assert test-def "test not found")
    (doall
     (filter
      identity
      (for [step (:steps test-def)
            version (or (not-empty versions) [:v1 :v2])]
        (do (prn step)
            (when (or (nil? (:only-for test-def)) (contains? (:only-for test-def) version))
              (let [step-res  (-> (do-step ztx step version)
                                  (update :errors #(sort-by :path %)))
                    match-res (matcho/match step-res (translate-to-matcho (:match step)))]
                (when-not (true? match-res)
                  (report-step ztx step match-res test-def)
                  {:desc (:desc step) :version version :expected (:match step) :got step-res})))))))))

(defn zen-read-ns [ztx s]
  (is (= :zen/loaded (zen.core/read-ns ztx s))))

(deftest implemented-validations
  (do

    (def ztx (zen.core/new-context {:unsafe true}))

    (zen-read-ns ztx 'zen.tests.require-test)

    (zen-read-ns ztx 'zen.tests.boolean-test)

    (zen-read-ns ztx 'zen.tests.types-test)

    (zen-read-ns ztx 'zen.tests.case-test)

    (run-tests ztx)))

;; simple bench results
;;  100 cycles: v1 - 1.895s, v2 - 1.869s
;;  1000 cycles: v1 - 18.937s, v2 - 18.770

;; to implement - exclusive-keys
;; https://github.com/HealthSamurai/sansara/blob/master/box/zrc/aidbox/rest/acl.edn#L101
;; case statement

;; also do not forget to remove :only-for as much as possible so that implementations are similar

(deftest in-progress-validations

  (do
    (def ztx (zen.core/new-context {:unsafe true}))

    (zen-read-ns ztx 'zen.tests.case-test))

  (comment
    "tests that do not pass for v1 impl"

    (zen.core/read-ns ztx 'zen.tests.map-test)))

(defn rand-str [len]
  (apply str (take len (repeatedly #(char (+ (rand 26) 65))))))

(defn gen-schema* [depth branch]
  (cond
    (= depth 0)
    (case (rand-int 2)
      0 [{:type 'zen/string} (rand-str 6)]
      1
      (let [enums (repeatedly branch #(rand-str 4))]
        [{:type 'zen/string
          :enum
          (->> (shuffle enums)
               (map (fn [e] {:value e})))}
         (first enums)]))

    :else
    (case (rand-int 1)
      0
      (let [kws*
            (->> (repeatedly branch #(rand-str 6))
                 (map keyword)
                 (map (fn [k] [k (gen-schema* (- depth 1) branch)])))]
        [{:type 'zen/map
          :keys
          (->> kws*
               (map (fn [[k [sch _]]] [k sch]))
               flatten
               (apply hash-map))}
         (->> kws*
              (map (fn [[k [_ data]]]
                     [k data]))
              flatten
              (apply hash-map))])
      1
      (let [[sch data] (gen-schema* 2 branch)
            oth (->> (repeatedly branch #(gen-schema* 0 branch))
                     (map (fn [[sch* _]]
                            {:when sch*})))]
        [{:type 'zen/case
          :case (conj (vec oth) {:when sch :then sch})}
         data]))))

(defn gen-schema [{:keys [depth branch]}]
  (let [[sch data] (gen-schema* depth branch)]
    [(assoc sch :zen/tags #{'zen/schema})
     data]))

(defn validate-old [ztx sch data]
  (val-old/validate-node ztx
                         (assoc (val-old/new-validation-acc) :schema [])
                         sch
                         data))

(defn leaves-count [{:keys [depth branch]}]
  (Math/pow branch depth))

(deftest depth-benchmark
  (def ztx (zen.core/new-context {:unsafe true}))

  (def cfg {:depth 4 :branch 2})

  (def result (gen-schema cfg))

  (leaves-count cfg)

  (def sch (first result))

  (def data (second result))

  (is (empty? (:errors (zen.core/validate ztx '#{zen/schema} sch))))
  (is (empty? (:errors (valid/validate ztx '#{zen/schema} sch))))

  (is (empty? (:errors (validate-old ztx sch data))))

  (is (empty? (:errors (valid/validate-schema ztx sch data))))

  (println " ")
  (println "bench results:")

  (time (doall (repeatedly 10 #(validate-old ztx sch data))))

  (time (doall (repeatedly 10 #(valid/validate-schema ztx sch data)))))

