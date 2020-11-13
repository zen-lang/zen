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
  (second (:symbols @ctx))



  )

