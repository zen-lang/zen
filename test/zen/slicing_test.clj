(ns zen.slicing-test
  (:require [zen.core :as sut]
            [clojure.test :refer [deftest is]]))

(deftest slicing-tests
  (testing "Validate slicing definition"


    (def tctx (zen.core/new-context {:unsafe true}))

    (zen.core/load-ns!
     tctx '{ns myapp
            str {:zen/tags #{zen/schema}
                 :type zen/vector
                 :every {:keys {:kind {:type zen/string}}}
                 :slices {"string"
                          {:filter {:engine :zen
                                    :zen    {:keys {:kind {:const {:value "string"}}}}}
                           :schema {:keys {:value {:type zen/string}}}}
                          "number"
                          {:filter {:engine :zen
                                    :zen    {:keys {:kind {:const {:value "number"}}}}}
                           :schema {:keys {:value {:type zen/number}}}}
                          "@default" {:schema {:keys {:value {:type zen/any}}}}}}})

    (def data-1 [{:kind "string"
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
                  }])))
