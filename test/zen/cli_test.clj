(ns zen.cli-test
  (:require [zen.cli :as sut]
            [zen.core :as zen]
            [zen.store]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.test :as t]
            [matcho.core :as matcho]))


(t/deftest ^:kaocha/pending cli-usecases-test

  (def test-dir-path "/tmp/zen-cli")

  (zen/init "zrc/my-package")
  (zen/init "zrc/my-package")
  (spit "zrc/my-package" {:ns 'my-package
                          :import #{'a}
                          'sym {:zen/tags #{'a/tag}
                                :a "a"}})

  (zen/errors)

  (zen/get-sybmol 'my-package/sym)

  (zen/get-tag 'a/tag)

  (spit "zrc/my-package" {:deps "a"})

  (zen/pull-deps)

  (zen/errors)

  (zen/pull-deps)

  (zen/errors)

  (spit "a" {:ns 'a})

  (zen/pull-deps)

  (zen/get-sybmol 'a/tag)

  (zen/changes)

  (zen/validate)

  )
