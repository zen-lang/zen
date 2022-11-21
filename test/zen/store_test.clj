(ns zen.store-test
  (:require [zen.core :as sut]
            [clojure.test :refer [deftest is]]
            [clojure.test :as t]))

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

  ;; TODO remove dissoc sometimes - prop is validated incorrectly
  (def errs (:errors (sut/validate ctx ['check/check] (dissoc (sut/get-symbol ctx 'data/foo)
                                                              :zen/name))))

  (is (empty? errs)))

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

(deftest keywords-syntax
  (def ztx (zen.core/new-context {:unsafe true}))

  (zen.core/load-ns!
   ztx {:ns 'mytest

        :import #{'zen.test}

        'just-schema
        {:zen/tags #{'zen/schema}
         :type 'zen/map}})

  (def errs (:errors @ztx))

  (is (empty? errs)))

(t/deftest zen-quote-reader-tag-test
  (t/testing "Store preparation"
    (def ztx (sut/new-context {:paths ["test/fixtures/qsyms"]})))

  (t/testing "No errors on ns load"
    (sut/read-ns ztx 'main)

    (is (empty? (:errors @ztx)))

    (is (= 'name.spaced/quoted-symbol (:a (zen.core/get-symbol ztx 'main/qsch))))
    (is (= 'unqualified-quoted-symbol (:b (zen.core/get-symbol ztx 'main/qsch))))))

(t/deftest zen-loading-errors-test
  (t/testing "Store preparation"
    (def ztx (sut/new-context {:paths ["test/fixtures/loading"]})))

  (t/testing "No errors on ns load"
    (sut/read-ns ztx 'missing-things)

    (is (= ['{:message    "No file for ns 'non-existent-ns"
              :missing-ns non-existent-ns
              :ns         missing-things}]
           (:errors @ztx)))))
