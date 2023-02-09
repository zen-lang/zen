(ns zen.index-test
  (:require
   [matcho.core :as matcho]
   [zen.core :as zen]
   [clojure.test :refer [deftest is]]))


(deftest index-test
  (def ztx (zen/new-context))

  (def lib-ns
    '{:ns mylib

      mytag
      {:zen/tags #{zen/tag zen/index}
       :index-key :attr}

      mod1
      {:zen/tags #{mytag}
       :attr #{"a" "c"}}

      mod2
      {:zen/tags #{mytag}
       :attr "b"}})

  (zen/load-ns ztx lib-ns)
  (is (empty? (zen/errors ztx)))

  (matcho/match
   (zen/get-index ztx 'mylib/mytag "a")
   #{'mylib/mod1})

  (matcho/match
   (zen/get-index ztx 'mylib/mytag "c")
   #{'mylib/mod1})

  (matcho/match
   (zen/get-index ztx 'mylib/mytag "b")
   #{'mylib/mod2})


  )
