(ns zen.store-test
  (:require [zen.core :as sut]
            [clojure.test :refer [deftest is]]
            [edamame.core]
            [clojure.java.io :as io]))


(deftest core-schema-test

  (def ctx (sut/new-context))

  (is (empty? (:errors @ctx)))

  (is (not (empty? (get-in @ctx [:symbols 'zen/schema]))))

  (get-in @ctx [:symbols 'zen/schema])

  (is (not (empty? (get-in @ctx [:ns 'zen]))))
  (is (not (nil? ('zen/property (:tags @ctx)))))
  

  (keys @ctx)

  (comment
    (doseq [[k v] (edamame.core/parse-string
                   (slurp (.getPath (io/resource "zen.edn"))))]
      (println k (meta k) "\n " (meta v) v)))


  )

