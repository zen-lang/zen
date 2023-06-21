(ns zen.env-test
  (:require
   [clojure.test :as t]
   [matcho.core :as matcho]
   [zen.core]))

(t/deftest test-envs

  (def ztx (zen.core/new-context {:paths ["test"]
                                  :env {:ESTR "extr" :EINT "99" :ESYM "schema"
                                        :ENUM "0.02" :EKEY "some/key"
                                        :BOOL_TRUE "true"
                                        :BOOL_FALSE "false"}}))

  (zen.core/read-ns ztx 'test-env)

;; TODO check why error is returned
  (zen.core/errors ztx)

  (matcho/match
   (zen.core/get-symbol ztx 'test-env/model)
    {:zen/tags #{'test-env/schema}
     :home string?
     :string "extr"
     :int 99
     :key :some/key
     :num 0.02
     :bool-true true
     :bool-false false
     :sym 'test-env/schema

     :dstring "DS"
     :dint 4
     :dkey :key
     :dbool-false false
     :dbool-true true
     :dnum 4.0
     :dsym 'test-env/sym
     :dhome "DH"}))
