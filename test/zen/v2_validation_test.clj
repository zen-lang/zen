(ns zen.v2-validation-test
  (:require
   [clojure.java.io]
   [zen.utils]
   [zen.store]
   [zen.effect :as fx]
   [zen.test-runner :as r]
   [clojure.test :refer [deftest is]]
   [zen.v2-validation :as v]
   [zen.core :as zen]))

(defn mv [from to]
  (let [source-filename (str (System/getProperty "user.dir") from)
        target-filename (str (System/getProperty "user.dir") to)
        source-file (java.nio.file.Paths/get (java.net.URI/create (str "file://" source-filename)))
        target-file (java.nio.file.Paths/get (java.net.URI/create (str "file://"  target-filename)))]
    (java.nio.file.Files/move source-file target-file
                              (into-array java.nio.file.CopyOption
                                          [(java.nio.file.StandardCopyOption/ATOMIC_MOVE)
                                           (java.nio.file.StandardCopyOption/REPLACE_EXISTING)]))))

(defonce flipstate (atom false))

(defn flip! []
  "sorry :)"
  (if @flipstate
    (do
      (reset! flipstate false)
      (mv "/pkg/zen.edn" "/pkg/v2/zen.edn")
      (mv "/pkg/v1/zen.edn" "/pkg/zen.edn"))
    (do
      (reset! flipstate true)
      (mv "/pkg/zen.edn" "/pkg/v1/zen.edn")
      (mv "/pkg/v2/zen.edn" "/pkg/zen.edn"))))

;; see slicing-test/zen-fx-engine-slicing-test
(defmethod fx/fx-evaluator 'zen.tests.slicing-test/slice-key-check
  [ztx {:keys [params path]} data]
  (if (= (get data (first params)) "fx-value")
    {:errors []}
    {:errors [{:message "wrong slice key value"
               :type "fx.apply"}]}))

(deftest implemented-validations

  (do
    (flip!)

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

    (r/zen-read-ns ztx 'zen.tests.match-test)

    (r/zen-read-ns ztx 'zen.tests.key-schema-test)

    (r/run-tests ztx)

    (flip!)))

(defn resolve-zen-ns [ztx]
  (->> (read-string (slurp (clojure.java.io/resource "zen.edn")))
       (map (fn [[k v]]
              [k (or (zen.utils/get-symbol ztx (zen.utils/mk-symbol 'zen k))
                     v)]))
       (into {})))

(deftest metadata-roundtrip
  (do
    (flip!)

    (def ztx (zen/new-context {:unsafe true}))

    (zen.utils/get-symbol ztx 'zen/namespace)

    (def result (v/validate ztx #{'zen/namespace} (resolve-zen-ns ztx)))

    (is (empty? (:errors result)))

    (flip!)))
