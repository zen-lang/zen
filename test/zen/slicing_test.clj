(ns zen.slicing-test
  (:require [matcho.core :as matcho]
            [zen.core :as sut]
            [clojure.test :refer [deftest is testing]]))

(deftest slicing-tests
  (testing "Validate slicing definition"
    (def tctx (zen.core/new-context {:unsafe true}))

    (zen.core/load-ns!
     tctx '{ns myapp
            slice-definition {:zen/tags #{zen/schema}
                              :type zen/vector
                              :every {:type zen/map :keys {:kind {:type zen/string}}}
                              :slicing {:slices {"string"
                                                 {:filter {:engine :zen
                                                           :zen    {:type zen/map :keys {:kind {:const {:value "string"}}}}}
                                                  :schema {:type zen/vector
                                                           :every {:type zen/map :keys {:value {:type zen/string}}}}}
                                                 "number"
                                                 {:filter {:engine :zen
                                                           :zen    {:type zen/map :keys {:kind {:const {:value "number"}}}}}
                                                  :schema {:type zen/vector
                                                           :every {:type zen/map :keys {:value {:type zen/number}}}}}}
                                        :other {:type zen/vector
                                                :every {:type zen/map
                                                        :keys {:value {:type zen/case
                                                                       :case [{:when {:type zen/string} :then {:fail "String kind is already defined"}}
                                                                              {:when {:type zen/number} :then {:fail "Number kind is already defined"}}
                                                                              {:when {:type zen/any}}]}}}}}}})

    (matcho/match @tctx {:errors nil?})

    (matcho/match
     (zen.core/validate
      tctx
      #{'slice-definition}
      [{:kind "string" :value "Hello"}
       {:kind "string" :value "World"}
       {:kind "number" :value 1}
       {:kind "foo"    :value :bar}])
     {:error nil?})

    (matcho/match
     (zen.core/validate tctx #{'slice-definition} [{:kind "string" :value 1}])
     {:errors
      [{:message "Slice validation error ", ;; TODO Use rigth validation error
        :type "slice",
        :slice-name "string"
        :path []}]})

    (matcho/match
     (zen.core/validate tctx #{'slice-definition} [{:kind "number" :value "1"}])
     {:errors
      [{:message "Slice validation error ", ;; TODO Use rigth validation error
        :type "slice",
        :slice-name "number"
        :path []}]})

    (matcho/match
     (zen.core/validate tctx #{'slice-definition} [{:kind "foo" :value "1"}])
     {:errors
      [{:message "Slice validation error ", ;; TODO Use rigth validation error
        :type "slice",
        :slice-name "@default"
        :path []}]})))

#_(clojure.test/run-tests)
