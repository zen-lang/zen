(ns zen.env-test
  (:require [matcho.core :as matcho]
            [clojure.test :refer [deftest is testing]]
            [zen.core]))

(deftest test-envs

  (def ztx (zen.core/new-context {:paths ["test"]
                                  :env {:ESTR "extr" :EINT "99" :ESYM "schema"
                                        :ENUM "0.02" :EKEY "some/key"}}))

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
     :sym 'test-env/schema}))
