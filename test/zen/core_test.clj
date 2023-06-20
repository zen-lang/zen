(ns zen.core-test
  (:require
   [clojure.test :as t]
   [matcho.core :as matcho]
   [zen.core :as sut]))


(t/deftest errors-test
  (def ztx (sut/new-context))

  (sut/load-ns ztx '{:ns my-ns
                     :import #{doesnt-exist}

                     sym {:zen/tags #{zen/schema}
                          :unknown :key}})

  (t/testing "errors are sorted by default"
    (matcho/match (sut/errors ztx)
      [{:path [:unknown]}
       {:missing-ns 'doesnt-exist}
       nil]))

  (t/testing "errors can be returned in the order of creation"
    (matcho/match (sut/errors ztx :order :as-is)
      [{:missing-ns 'doesnt-exist}
       {:path [:unknown]}
       nil]))

  (t/testing "unknown order param throws error"
    (t/is (instance? Exception
                     (try (sut/errors ztx :order :foobar)
                          (catch java.lang.IllegalArgumentException e
                            e))))))
