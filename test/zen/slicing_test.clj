(ns zen.slicing-test
  (:require [matcho.core :as matcho]
            [zen.core]
            [zen.validation]
            [zen.test-utils :refer [vmatch match valid valid-schema! invalid-schema]]
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
             :every {:type zen/map :keys {:kind {:type zen/string}
                                          :value {:type zen/any}}}
             :slicing {:rest {:type  zen/vector
                              :every {:type zen/map
                                      :keys {:value {:type zen/string}}}}
                       :slices {"kw"
                                {:filter {:engine :zen
                                          :zen    {:type zen/map
                                                   :keys {:kind {:const {:value "keyword"}}}}}
                                 :schema {:type  zen/vector
                                          :every {:type zen/map
                                                  :keys {:value {:type zen/keyword}}}}}

                                "number"
                                {:filter {:engine :zen
                                          :zen    {:type zen/map
                                                   :keys {:kind {:const {:value "number"}}}}}
                                 :schema {:type  zen/vector
                                          :every {:type zen/map
                                                  :keys {:value {:type zen/number}}}}}

                                "nested"
                                {:filter {:engine :zen
                                          :zen    {:type zen/map
                                                   :keys {:kind {:const {:value "nested"}}}}}
                                 :schema {:type  zen/vector
                                          :every {:type zen/map
                                                  :keys {:value {:type zen/vector
                                                                 :every {:type zen/map
                                                                         :keys {:kind  {:type zen/string}
                                                                                :value {:type zen/any}}}
                                                                 :slicing
                                                                 {:slices
                                                                  {"nest-kw"
                                                                   {:filter {:engine :zen
                                                                             :zen    {:type zen/map
                                                                                      :keys {:kind {:const {:value "keyword"}}}}}
                                                                    :schema {:type  zen/vector
                                                                             :every {:type zen/map
                                                                                     :keys {:value {:type zen/keyword}}}}}}}}}}}}

                                "map"
                                {:filter {:engine :zen
                                          :zen    {:type zen/map
                                                   :keys {:kind {:const {:value "map"}}}}}
                                 :schema {:type  zen/vector
                                          :every {:type zen/map
                                                  :keys {:value {:type zen/map
                                                                 :keys {:nested {:type zen/vector
                                                                                 :every {:type zen/map
                                                                                         :keys {:kind  {:type zen/string}
                                                                                                :value {:type zen/any}}}
                                                                                 :slicing
                                                                                 {:slices
                                                                                  {"nest-kw"
                                                                                   {:filter {:engine :zen
                                                                                             :zen    {:type zen/map
                                                                                                      :keys {:kind {:const {:value "keyword"}}}}}
                                                                                    :schema {:type  zen/vector
                                                                                             :every {:type zen/map
                                                                                                     :keys {:value {:type zen/keyword}}}}}}}}}}}}}}}}}})

    (matcho/match @tctx {:errors nil?})

    (valid tctx 'myapp/slice-definition
     [{:kind "keyword" :value :hello}
      {:kind "keyword" :value :world}
      {:kind "number" :value 1}
      {:kind "foo"    :value "string"}
      {:kind "map"
       :value {:nested [{:kind "keyword" :value :world}
                        {:kind "number" :value 1}]}}
      {:kind "nested"
       :value [{:kind "keyword" :value :world}
               {:kind "number" :value 1}]}])

    (vmatch tctx #{'myapp/slice-definition}
            [{:kind "keyword" :value 1}]
            {:errors [{:path ["[kw]" 0 :value nil?]} nil?]})

    (vmatch tctx #{'myapp/slice-definition}
            [{:kind "number" :value "1"}]
            {:errors [{:path ["[number]" 0 :value nil?]} nil?]})

    (vmatch tctx #{'myapp/slice-definition}
            [{:kind "foo" :value 1}]
            {:errors [{:path ["[:slicing/rest]" 0 :value nil?]} nil?]})

    (vmatch tctx #{'myapp/slice-definition}
            [{:kind "map", :value {:nested [{:kind "keyword" :value "not keyword"}]}}]
            {:errors [{:path ["[map]" 0 :value :nested "[nest-kw]" 0 :value nil?]} nil?]})

    (vmatch tctx #{'myapp/slice-definition}
            [{:kind "nested" :value [{:kind "keyword" :value "not keyword"}]}]
            {:errors [{:path ["[nested]" 0 :value "[nest-kw]" 0 :value nil?]} nil?]})))
