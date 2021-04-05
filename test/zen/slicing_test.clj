(ns zen.slicing-test
  (:require [matcho.core :as matcho]
            [zen.core :as sut]
            [clojure.test :refer [deftest is testing]]))

(deftest slicing-tests
  (testing "Validate slicing definition"
    (def tctx (zen.core/new-context {:unsafe true}))

    (zen.core/load-ns
     tctx '{ns myapp
            slice-definition {:zen/tags #{zen/schema}
                              :type zen/vector
                              :every {:keys {:kind {:type zen/string}}}
                              :slices {"string"
                                       {:filter {:engine :zen
                                                 :zen    {:keys {:kind {:const {:value "string"}}}}}
                                        :schema {:type zen/vector
                                                 :every {:keys {:value {:type zen/string}}}}}
                                       "number"
                                       {:filter {:engine :zen
                                                 :zen    {:keys {:kind {:const {:value "number"}}}}}
                                        :schema {:type zen/vector
                                                 :every {:keys {:value {:type zen/number}}}}}
                                       "@default" {:schema {:type zen/vector
                                                            :every {:keys {:value {:type zen/case
                                                                                   :case [{:when {:type zen/string} :then {:fail "String kind is already defined"}}
                                                                                          {:when {:type zen/number} :then {:fail "Number kind is already defined"}}
                                                                                          {:when {:type zen/any}}
                                                                                          ]}}}}}}}})


    (matcho/match @tctx {:errors nil?})


    (def data-valid [{:kind "string"
                      :value "Hello"
                      }
                     {:kind "string"
                      :value "World"
                      }
                     {:kind "number"
                      :value 1
                      }
                     {:kind "foo"
                      :value :bar
                      }])
    (matcho/match
     (zen.core/validate tctx #{'slice-definition} data-valid)
     {:error nil?})


    (def data-invalid-1
      [{:kind "string"
        :value 1
        }])
    (matcho/match
     (zen.core/validate tctx #{'slice-definition} data-invalid-1)
     {:errors
      [{:message "Slice validation error ", ;; TODO Use rigth validation error
        :type "slice",
        :slice-name "string"
        :path [],
        }]})


    (def data-invalid-2
      [{:kind "number"
        :value "1"
        }])
    (matcho/match
     (zen.core/validate tctx #{'slice-definition} data-valid)
     {:errors
      [{:message "Slice validation error ", ;; TODO Use rigth validation error
        :type "slice",
        :slice-name "number"
        :path [],
        }]})


    (def data-invalid-3
      [
       {:kind "foo"
        :value "1"
        }])
    (matcho/match
     (zen.core/validate tctx #{'slice-definition} data-valid)
     {:errors
      [{:message "Slice validation error ", ;; TODO Use rigth validation error
        :type "slice",
        :slice-name "@default"
        :path [],
        }]})))
