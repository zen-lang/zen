(ns zen.slicing-test
  (:require
   [clojure.test :as t]
   [matcho.core :as matcho]
   [zen.core]
   [zen.test-utils :as utils]
   [zen.validation]))

(t/deftest slicing-tests
  (t/testing "slicing grouping"
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

  (t/testing "Validate slicing definition"
    (def tctx (zen.core/new-context {:unsafe true}))

    (zen.core/load-ns!
     tctx '{ns myapp
            required-slice {:zen/tags #{zen/schema}
                            :type zen/vector
                            :every {:type zen/map
                                    :require #{:kind}
                                    :keys {:kind {:type zen/string}}}
                            :slicing {:slices {"one" {:filter {:engine :zen
                                                               :zen {:type zen/map
                                                                     :keys {:kind {:const {:value "one"}}}}}
                                                      :schema {:type zen/vector :minItems 1 :maxItems 1}}}}}
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

    (utils/valid tctx 'myapp/slice-definition
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

    (utils/vmatch tctx #{'myapp/slice-definition}
                  [{:kind "keyword" :value 1}]
                  {:errors [{:path [0 :value nil?]} nil?]})

    (utils/vmatch tctx #{'myapp/slice-definition}
                  [{:kind "number" :value "1"}]
                  {:errors [{:message "Expected type of 'number, got 'string"
                             :path [0 :value nil?]} nil?]})

    (utils/vmatch tctx #{'myapp/slice-definition}
                  [{:kind "foo" :value 1}]
                  {:errors [{:message "Expected type of 'string, got 'long"
                             :path [0 :value nil?]} nil?]})

    (utils/vmatch tctx #{'myapp/slice-definition}
                  [{:kind "map", :value {:nested [{:kind "keyword" :value "not keyword"}]}}]
                  {:errors [{:message "Expected type of 'keyword, got 'string"
                             :path [0 :value :nested 0 :value nil?]} nil?]})

    (utils/vmatch tctx #{'myapp/slice-definition}
                  [{:kind "nested" :value [{:kind "keyword" :value "not keyword"}]}]
                  {:errors [{:message "Expected type of 'keyword, got 'string"
                             :path [0 :value 0 :value nil?]}
                            nil?]})

    (utils/match tctx 'myapp/required-slice
                 [{:kind "two"}]
                 [{:type "vector.minItems"
                   :schema ['myapp/required-slice :slicing "one" :minItems]
                   :path []}]))

  (t/testing "slicing path collision unknown key bug"
    (def tctx (zen.core/new-context {:unsafe true}))

    (zen.core/load-ns!
     tctx '{ns myapp

            subj
            {:zen/tags #{zen/schema}
             :type zen/vector
             :every {:type zen/map, :keys {:kind {:type zen/string}}}
             :slicing {:rest {:type  zen/vector
                              :every {:type zen/map,
                                      :keys {:rest-key {:type zen/any}}}}
                       :slices {"slice"
                                {:filter {:engine :zen
                                          :zen    {:type zen/map
                                                   :keys {:kind {:const {:value "slice"}}}}}
                                 :schema {:type  zen/vector
                                          :every {:type zen/map
                                                  :keys {:slice-key {:type zen/string}}}}}}}}})

    (matcho/match @tctx {:errors nil?})

    (utils/valid tctx 'myapp/subj
                 [{:kind "slice", :slice-key "kw-key"}
                  {:kind "rest", :rest-key "rest-key"}])

    (utils/vmatch tctx #{'myapp/subj}
                  [{:kind "rest", :rest-key "rest-key"}
                   {:kind "slice", :rest-key "kw-key"}
                   {:kind "slice", :slice-key :kw-key}]
                  {:errors [{:path [2 :slice-key nil?] :type "string.type"}
                            {:path [1 :rest-key nil?]}
                       ;; zen can't know where unknown key came from, thus can't write slice in this path
                            nil?]})))
