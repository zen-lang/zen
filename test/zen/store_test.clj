(ns zen.store-test
  (:require [zen.core :as sut]
            [clojure.test :refer [deftest is]]))


(deftest core-schema-test
  (def ctx (sut/new-context))

  (is (empty? (:errors @ctx)))

  (is (not (empty? (get-in @ctx [:symbols 'zen/schema]))))

  (get-in @ctx [:symbols 'zen/schema])

  (is (not (empty? (get-in @ctx [:ns 'zen]))))
  (is (not (nil? ('zen/property (:tags @ctx)))))
  (keys @ctx)
  (second (:symbols @ctx)))

(deftest memory-store-schema-test
  (def data '{ns data
             import #{check}
             foo {:foo "bar"}})

  (def check '{ns check
               check {:zen/tags #{zen/schema}
                     :keys {:foo {:type zen/string}}}})

  (def memory-store {'check check 'data data})

  (def opts {:memory-store memory-store})

  (def ctx (sut/new-context opts))

  (sut/load-ns ctx data)

  (is (= memory-store (:memory-store @ctx)))

  (is (empty? (:errors (sut/validate ctx ['check/check] (sut/get-symbol ctx 'data/foo))))))

(comment
 @ctx)
