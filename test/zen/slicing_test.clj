(ns zen.slicing-test
  (:require [matcho.core :as matcho]
            [zen.core]
            [zen.validation]
            [clojure.test :refer [deftest is testing]]))

(deftest slicing-tests
  (testing "slicing grouping"
    (def tctx (zen.core/new-context {:unsafe true}))

    (def slicing '{:slices {"string" {:filter {:engine :zen, :zen {:type zen/map :keys {:kind {:const {:value "string"}}}}}}
                            "number" {:filter {:engine :zen, :zen {:type zen/map :keys {:kind {:const {:value "number"}}}}}}}
                   :rest {:type zen/vector}})

    (def data [{:kind "string" :value "Hello"}
               {:kind "foo"    :value :bar}
               {:kind "string" :value "World"}
               {:kind "number" :value 1}])

    (matcho/match (zen.validation/slice tctx slicing data)
                  {"string"      {0 {:kind "string", :value "Hello"}
                                  2 {:kind "string", :value "World"}}
                   "number"      {3 {:kind "number", :value 1}}
                   :slicing/rest {1 {:kind "foo", :value :bar}}}))

  (testing "Validate slicing definition"
    (def tctx (zen.core/new-context {:unsafe true}))

    (zen.core/load-ns!
     tctx '{ns myapp
            slice-definition
            {:zen/tags #{zen/schema}
             :type zen/vector
             :every {:type zen/map :keys {:kind {:type zen/string}, :value {:type zen/any}}}
             :slicing {:slices {"kw"     {:filter {:engine :zen, :zen {:type zen/map :keys {:kind {:const {:value "keyword"}}}}}
                                          :schema {:type zen/vector, :every {:type zen/map :keys {:value {:type zen/keyword}}}}}
                                "number" {:filter {:engine :zen, :zen {:type zen/map :keys {:kind {:const {:value "number"}}}}}
                                          :schema {:type zen/vector, :every {:type zen/map :keys {:value {:type zen/number}}}}}}
                       :rest   {:type  zen/vector
                                :every {:type zen/map
                                        :keys {:value {:type zen/string}}}}}}})

    (matcho/match @tctx {:errors nil?})

    (matcho/match
     (zen.core/validate
      tctx
      #{'myapp/slice-definition}
      [{:kind "keyword" :value :hello}
       {:kind "keyword" :value :world}
       {:kind "number" :value 1}
       {:kind "foo"    :value "string"}])
     {:errors empty?})

    (matcho/match
     (zen.core/validate tctx #{'myapp/slice-definition} [{:kind "keyword" :value 1}])
     {:errors [{:path [0 ":kw" :value]}]})

    (matcho/match
     (zen.core/validate tctx #{'myapp/slice-definition} [{:kind "number" :value "1"}])
     {:errors [{:path [0 ":number" :value]}]})

    (matcho/match
     (zen.core/validate tctx #{'myapp/slice-definition} [{:kind "foo" :value 1}])
     {:errors [{:path [0 ":slicing/rest" :value]}]})))
