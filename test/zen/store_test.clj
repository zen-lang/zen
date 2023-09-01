(ns zen.store-test
  (:require
   [clojure.test :as t]
   [matcho.core :as matcho]
   [zen.core :as zen]))

(t/deftest core-schema
  (def ctx (zen/new-context))

  (t/is (empty? (:errors @ctx)))

  (t/is (not (empty? (get-in @ctx [:symbols 'zen/schema]))))

  (t/is (not (empty? (get-in @ctx [:ns 'zen]))))

  (t/is (not (nil? ('zen/property (:tags @ctx))))))

(t/deftest memory-store-schema
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

  (t/is (= memory-store (:memory-store @ctx)))

  ;; TODO remove dissoc sometimes - prop is validated incorrectly
  (def errs (:errors (zen/validate ctx ['check/check] (dissoc (zen/get-symbol ctx 'data/foo)
                                                              :zen/name))))

  (t/is (empty? errs)))

(t/deftest dynamic-paths
  (def wctx (zen/new-context))
  (zen/read-ns wctx 'dyns)
  (t/is (nil? (zen/get-symbol wctx 'dyns/model)))

  (def dctx (zen/new-context {:paths ["/unexisting"
                                      (str (System/getProperty "user.dir") "/test/dynamic")]}))

  (zen/read-ns dctx 'dyns)
  (t/is (not (nil? (zen/get-symbol dctx 'dyns/model)))))

(t/deftest node-modules
  (def path "test/fixtures/tmp-proj")
  (def zctx* (zen/new-context {:paths [path]}))
  (zen/read-ns zctx* 'project)

  (t/is (empty? (:errors @zctx*)))

  (t/is (contains? (:ns @zctx*) 'fhir.r4))
  (t/is (contains? (:ns @zctx*) 'us-core.patient))

  (t/testing ":zen/file and :zen/zen-path point to file and path containing the file"
    (def node-modules-us-core-path
      "node_modules/@zen-lang/us-core")

    (matcho/match (zen/get-symbol zctx* 'us-core.patient/patient)
      {:zen/file (str path
                      "/" node-modules-us-core-path
                      "/us-core/patient.edn")
       :zen/zen-path (str path "/" node-modules-us-core-path)})))

(t/deftest recursive-import
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

  (t/is (empty? (:errors @ctx))))

(t/deftest keywords-syntax
  (def ztx (zen/new-context {:unsafe true}))

  (zen/load-ns!
   ztx {:ns 'mytest

        :import #{'zen.test}

        'just-schema
        {:zen/tags #{'zen/schema}
         :type 'zen/map}})

  (def errs (:errors @ztx))

  (t/is (empty? errs)))

(t/deftest zen-quote-reader-tag
  (t/testing "Store preparation"
    (def ztx (zen/new-context {:paths ["test/fixtures/qsyms"]})))

  (t/testing "No errors on ns load"
    (zen/read-ns ztx 'main)

    (t/is (empty? (:errors @ztx)))

    (t/is (= 'name.spaced/quoted-symbol (:a (zen/get-symbol ztx 'main/qsch))))
    (t/is (= 'unqualified-quoted-symbol (:b (zen/get-symbol ztx 'main/qsch))))))

(t/deftest zen-loading-errors
  (t/testing "Store preparation"
    (def ztx (zen/new-context {:paths ["test/fixtures/loading"]})))

  (t/testing "No errors on ns load"
    (zen/read-ns ztx 'missing-things)

    (t/is (= ['{:message    "No file for ns 'non-existent-ns"
                :missing-ns non-existent-ns
                :ns         missing-things}]
             (:errors @ztx)))))

(t/deftest late-binding
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

  (matcho/assert
   [{:message "No binding for 'mylib/config-binding"
     :type :unbound-binding
     :ns 'zen.store
     :diref #{'mylib/operation}}]
   (zen/errors ztx))

  (t/testing "late binding"

    (def app-ns
      '{:ns myapp
        :import #{mylib}

        config
        {:zen/tags #{mylib/config}
         :zen/bind mylib/config-binding
         :token "mytoken"}})

    (zen/load-ns ztx app-ns)

    (t/is (empty? (zen/errors ztx)))

    (matcho/assert
     '{:zen/tags #{zen/binding}
       :zen/bind mylib/config-binding
       :token "mytoken"
       :zen/name mylib/config-binding}
     (zen/get-symbol ztx 'mylib/config-binding))))


(t/deftest load-ns-resources-loaded-test
  (def ztx (zen/new-context {}))

  (matcho/match
   (zen/load-ns ztx
                '{:ns my-ns
                  a {:zen/tags #{zen/schema}
                     :type zen/map}})
    [:resources-loaded 1 nil]))


(t/deftest cyclic-import-validation-test
  (def memory-store
    {'b '{:ns b
          :import #{a}
          s {:zen/tags #{a/t}}}

     'a '{:ns a
          :import #{b}
          t {:zen/tags #{zen/tag zen/schema}
             :type zen/map
             :require #{:foo}}}})

  (def z (zen/new-context {:memory-store memory-store}))

  (t/is (= [:resources-loaded 2]
           (zen/load-ns z (get memory-store 'a))))

  (t/is (seq (zen/errors z))))


(t/deftest cyclic-import-validation-on-read-ns-test
  (def z (zen/new-context {:paths ["test/fixtures/cyclic-import-validation-on-read-ns-test"]}))

  (t/is (= :zen/loaded (zen/read-ns z 'a)))

  (t/is (seq (zen/errors z))))


(t/deftest symbol-validation-test
  (def memory-store
    {'b '{:ns b
          :import #{a}
          s {:zen/tags #{a/t b}}}

     'a '{:ns a
          :import #{b}
          s {:zen/tags #{b/t a}}}})

  (def z (zen/new-context {:memory-store memory-store}))

  (t/is (= [:resources-loaded 2]
           (zen/load-ns z (get memory-store 'a))))

  (t/testing "unresolved symbols errors"
    (matcho/match (zen/errors z)
      [{} {} {} {} nil])))

(t/deftest parse-error-test

  (def z (zen/new-context {:paths ["test/fixtures/parse-error"]}))

  (t/is (= :zen/load-failed (zen/read-ns z 'error)))

  (t/is (= [{:message "Unmatched delimiter: }",
           :file "test/fixtures/parse-error/error.edn",
           :ns 'error}]
          (zen/errors z))))
