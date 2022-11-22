(ns zen.store-test
  (:require
   [matcho.core :as matcho]
   [zen.core :as zen]
   [clojure.test :refer [deftest is testing]]
   [clojure.test :as t]))

(deftest core-schema
  (def ctx (zen/new-context))

  (is (empty? (:errors @ctx)))

  (is (not (empty? (get-in @ctx [:symbols 'zen/schema]))))

  (is (not (empty? (get-in @ctx [:ns 'zen]))))

  (is (not (nil? ('zen/property (:tags @ctx))))))

(deftest memory-store-schema
  (def data '{ns data
              import #{check}
              foo {:foo "bar"}})

  (def check '{ns check
               check {:zen/tags #{zen/schema}
                      :keys {:foo {:type zen/string}}}})

  (def memory-store {'check check 'data data})

  (def opts {:memory-store memory-store})

  (def ctx (zen/new-context opts))

  (zen/load-ns ctx data)

  (is (= memory-store (:memory-store @ctx)))

  ;; TODO remove dissoc sometimes - prop is validated incorrectly
  (def errs (:errors (zen/validate ctx ['check/check] (dissoc (zen/get-symbol ctx 'data/foo)
                                                              :zen/name))))

  (is (empty? errs)))

(deftest dynamic-paths
  (def wctx (zen/new-context))
  (zen/read-ns wctx 'dyns)
  (is (nil? (zen/get-symbol wctx 'dyns/model)))

  (def dctx (zen/new-context {:paths ["/unexisting"
                                      (str (System/getProperty "user.dir") "/test/dynamic")]}))

  (zen/read-ns dctx 'dyns)
  (is (not (nil? (zen/get-symbol dctx 'dyns/model)))))

(deftest node-modules
  (def zctx* (zen/new-context {:paths ["test/fixtures/tmp-proj/"]}))
  (zen/read-ns zctx* 'project)

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

  (def ctx (zen/new-context {:memory-store memory-store}))

  (zen/load-ns ctx (memory-store 'foo))

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

(deftest zen-quote-reader-tag
  (testing "Store preparation"
    (def ztx (zen/new-context {:paths ["test/fixtures/qsyms"]})))

  (testing "No errors on ns load"
    (zen/read-ns ztx 'main)

    (is (empty? (:errors @ztx)))

    (is (= 'name.spaced/quoted-symbol (:a (zen.core/get-symbol ztx 'main/qsch))))
    (is (= 'unqualified-quoted-symbol (:b (zen.core/get-symbol ztx 'main/qsch))))))

(deftest zen-loading-errors
  (testing "Store preparation"
    (def ztx (zen/new-context {:paths ["test/fixtures/loading"]})))

  (testing "No errors on ns load"
    (zen/read-ns ztx 'missing-things)

    (is (= ['{:message    "No file for ns 'non-existent-ns"
              :missing-ns non-existent-ns
              :ns         missing-things}]
           (:errors @ztx)))))

(deftest late-binding
  (def ztx (zen/new-context))

  (def lib-ns
    '{:ns mylib

      config
      {:zen/tags #{zen/tag zen/schema}
       :type zen/map
       :keys {:token {:type zen/string}
              :zen/bind {:type zen/symbol}}}

      config-binding
      {:zen/tags #{zen/binding}}

      operation
      {:tags #{zen/op}
       :config config-binding}})

  (zen/load-ns ztx lib-ns)

  (matcho/assert
   '{:zen/tags #{zen/binding}
     :zen/name mylib/config-binding}
   (zen/get-symbol ztx 'mylib/config-binding))

  (is (empty? (zen/errors ztx)))

  (testing "late binding"

    (def app-ns
      '{:ns myapp
        :import #{mylib}

        config
        {:zen/tags #{mylib/config}
         :zen/bind mylib/config-binding
         :token "mytoken"}})

    (zen/load-ns ztx app-ns)

    (is (empty? (zen/errors ztx)))

    (matcho/assert
     '{:zen/tags #{zen/binding}
       :zen/bind mylib/config-binding
       :token "mytoken"
       :zen/name mylib/config-binding}
     (zen/get-symbol ztx 'mylib/config-binding))))
