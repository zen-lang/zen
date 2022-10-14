(ns zen.cli-test
  (:require [zen.cli :as sut]
            [clojure.string :as str]
            [clojure.test :as t]
            [zen.test-utils]
            [matcho.core :as matcho]))


(def zen-packages-fixtures
  {'my-dep {:deps '#{}
            :zrc '#{{:ns my-dep
                     :import #{}

                     tag {:zen/tags #{zen/schema zen/tag}
                          :type zen/map
                          :require #{:a}
                          :keys {:a {:type zen/string}}}}}}})


(t/deftest ^:kaocha/pending cli-usecases-test

  (def test-dir-path "/tmp/zen-cli-test/")
  (def my-package-dir-path (str test-dir-path "/my-package/"))
  (def dependency-dir-path (str test-dir-path "/my-dep/"))

  (zen.test-utils/rm-fixtures test-dir-path)
  (zen.test-utils/mk-fixtures test-dir-path zen-packages-fixtures)

  (t/testing "create template"
    (zen.test-utils/mkdir my-package-dir-path)

    (matcho/match (sut/init 'my-package {:pwd my-package-dir-path})
                  {:status :ok, :code :initted-new})

    (matcho/match (zen.test-utils/fs-tree->tree-map my-package-dir-path)
                  {"zen-package.edn" some?
                   "zrc"             {"my-package.edn" some?}
                   ".git"            {}
                   ".gitignore"      some?}))

  (t/testing "try to create new template over existing directory, get error that repo already exists"
    (matcho/match (sut/init 'my-package {:pwd my-package-dir-path})
                  {:status :ok, :code :already-exists}))

  (t/testing "declare a symbol with tag and import ns from a dependency"

    (t/testing "the symbol doesn't exist before update"
      (t/is (nil? (sut/get-symbol 'my-package/sym {:pwd my-package-dir-path})))
      (t/is (empty? (sut/get-tag 'my-dep/tag {:pwd my-package-dir-path}))))

    (zen.test-utils/update-edn-file (str my-package-dir-path "/zrc/my-package.edn")
                     #(assoc %
                             :import #{'my-dep}
                             'sym {:zen/tags #{'my-dep/tag}
                                   :a "a"}))

    (t/testing "get the symbol"
      (matcho/match (sut/get-symbol 'my-package/sym {:pwd my-package-dir-path})
                    {:zen/tags #{'my-dep/tag}
                     :a "a"}))

    (t/testing "get the symbol by the tag"
      (matcho/match (sut/get-tag 'my-dep/tag {:pwd my-package-dir-path})
                    #{'my-package/sym})))

  (t/testing "specify a dependency in zen-package.edn"
    (t/testing "check errors, see that namespace the dependency ns is missing"

      (matcho/match (sut/errors {:pwd my-package-dir-path})
                    [{}
                     {}
                     {}
                     nil]))

    (zen.test-utils/update-edn-file (str my-package-dir-path "/zen-package.edn")
                                    #(assoc % :deps {'my-dep dependency-dir-path}))

    (t/testing "do pull-deps & check for errors, should be no errors"

      (matcho/match (sut/pull-deps {:pwd my-package-dir-path})
                    {:status :ok, :code :pulled, :data ['my-dep nil]})

      (matcho/match (sut/errors {:pwd my-package-dir-path})
                    empty?))

    (t/testing "do pull-deps again should be no errors and no changes"

      (matcho/match (sut/pull-deps {:pwd my-package-dir-path})
                    {:status :ok, :code :nothing-to-update, :data empty?})

      (matcho/match (sut/errors {:pwd my-package-dir-path})
                    empty?)))

  #_#_#_#_(t/testing "commit update to the dependency"

    (spit "a" {:ns 'a})
    ('commit "a"))

  (t/testing "do pull-deps and see the update"

    (sut/pull-deps)
    (sut/errors)

    (sut/get-sybmol 'a/tag))

  (t/testing "do changes command, see your changes"

    (sut/changes))

  (t/testing "use validate command to validate on some data "

    (sut/validate)))
