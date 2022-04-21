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

(deftest dynamic-paths
  (def wctx (sut/new-context))
  (sut/read-ns wctx 'dyns)
  (is (nil? (sut/get-symbol wctx 'dyns/model)))

  (def dctx (sut/new-context {:paths ["/unexisting"
                                      (str (System/getProperty "user.dir") "/test/dynamic")]}))

  (sut/read-ns dctx 'dyns)
  (is (not (nil? (sut/get-symbol dctx 'dyns/model)))))

(deftest node-modules
  (def zctx* (sut/new-context {:paths ["test/fixtures/tmp-proj/"]}))
  (sut/read-ns zctx* 'project)

  (is (empty? (:errors @zctx*)))

  (is (contains? (:ns @zctx*) 'fhir.r4))
  (is (contains? (:ns @zctx*) 'us-core.patient)))

(deftest recursive-import
  (def memory-store
    '{foo {ns     foo
           import #{bar}
           tag    {:zen/tags #{zen/tag}}
           schema {:zen/tags #{zen/schema tag bar/tag}
                   :confirms #{bar/schema}}}

      bar {ns bar
           import #{foo}
           tag    {:zen/tags #{zen/tag}}
           schema {:zen/tags #{zen/schema foo/tag tag}
                   :confirms #{foo/schema}}}})

  (def ctx (sut/new-context {:memory-store memory-store}))

  (sut/load-ns ctx (memory-store 'foo))

  (is (empty? (:errors @ctx))))

(comment
  @ctx)
