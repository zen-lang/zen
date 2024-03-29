(ns zen.cli-test
  (:require
   [clojure.edn]
   [clojure.java.io :as io]
   [clojure.test :as t]
   [matcho.core :as matcho]
   [zen.cli :as sut]
   [zen.test-utils]))


(def zen-packages-fixtures
  {'my-dep {:deps '#{}
            :zrc '#{{:ns my-dep
                     :import #{}

                     tag {:zen/tags #{zen/schema zen/tag}
                          :type zen/map
                          :require #{:a}
                          :keys {:a {:type zen/string}}}}}}

   'my-dep-fork {:deps '#{}
                 :zrc '#{{:ns my-dep
                          :import #{}

                          new-sym {:i-am-forked :not-the-original-repo}

                          tag {:zen/tags #{zen/schema zen/tag}
                               :type zen/map
                               :require #{:a}
                               :keys {:a {:type zen/string}}}}}}})


(defn sut-cmd [cmd-name & args]
  (let [opts      (last args)
        cmd-args  (butlast args)
        test-opts {:format :identity}
        cli-opts  (merge test-opts opts)]
    (assert (and (map? opts)
                 (contains? opts :pwd))
            "Test cmd calls must specify PWD")

    (sut/cli-main (cons cmd-name cmd-args) cli-opts)))


(t/deftest cli-usecases-test
  (def test-dir-path (io/file "/tmp/zen-cli-test"))
  (def my-package-dir-path (io/file test-dir-path "my-package"))
  (def dependency-dir-path (io/file test-dir-path "my-dep"))
  (def dependency-fork-dir-path (io/file test-dir-path "my-dep-fork"))

  (zen.test-utils/rm-fixtures test-dir-path)
  (zen.test-utils/mk-fixtures test-dir-path zen-packages-fixtures)

  (t/testing "wrong command"
    (matcho/match (sut-cmd "AAAAAAAAAAAAAA" {:pwd test-dir-path})
      {::sut/status :error})

    (matcho/match (sut-cmd "init" "a" "b" "c" "d" "too many args" {:pwd test-dir-path})
      {::sut/status :error}))

  (t/testing "create template"
    (zen.test-utils/mkdir test-dir-path)

    (matcho/match (sut-cmd "init" "my-package" {:pwd test-dir-path})
      {::sut/code :initted-new, ::sut/status :ok})

    (matcho/match (zen.test-utils/fs-tree->tree-map my-package-dir-path)
      {"zen-package.edn" some?
       "zrc"             {"my-package.edn" some?}
       ".git"            some?
       ".gitignore"      some?}))


  (t/testing "try to create new template over existing directory, get error that repo already exists"
    (matcho/match (sut-cmd "init" "my-package" {:pwd test-dir-path})
      {::sut/status :ok, ::sut/code :already-exists}))


  (t/testing "check new template no errors"
    (matcho/match (sut-cmd "errors" {:pwd my-package-dir-path})
      {::sut/status :ok}))


  (zen.test-utils/git-init-commit my-package-dir-path)


  (t/testing "declare a symbol with tag and import ns from a dependency"
    (t/testing "no changes are made yet"
      (matcho/match (sut-cmd "changes" {:pwd my-package-dir-path})
        {::sut/status :ok
         ::sut/result {:status :unchanged}}))

    (t/testing "the symbol doesn't exist before update"
      (matcho/match (sut-cmd "get-symbol" "my-package/sym" {:pwd my-package-dir-path})
        {::sut/status :ok
         ::sut/result nil?})

      (matcho/match (sut-cmd "get-tag" "my-dep/tag" {:pwd my-package-dir-path})
        {::sut/status :ok
         ::sut/result empty?}))

    (zen.test-utils/update-zen-file (io/file my-package-dir-path "zrc" "my-package.edn")
                                    #(assoc %
                                            :import #{'my-dep}
                                            'sym {:zen/tags #{'my-dep/tag}
                                                  :a "a"}))

    (t/testing "get the symbol"
      (matcho/match (sut-cmd "get-symbol" "my-package/sym" {:pwd my-package-dir-path})
        {::sut/status :ok
         ::sut/result {:zen/tags #{'my-dep/tag}
                       :a "a"}}))

    (t/testing "get the symbol by the tag"
      (matcho/match (sut-cmd "get-tag" "my-dep/tag" {:pwd my-package-dir-path})
        {::sut/status :ok
         ::sut/result                     #{'my-package/sym}}))

    (t/testing "see changes"
      (matcho/match (sut-cmd "changes" {:pwd my-package-dir-path})
        {::sut/status :ok
         ::sut/result {:status :changed
                       :changes [{} nil]}})

      (zen.test-utils/git-commit my-package-dir-path "zrc" "Add my-dep/new-sym")

      (matcho/match (sut-cmd "changes" {:pwd my-package-dir-path})
        {::sut/status :ok
         ::sut/result {:status :unchanged}})))


  (t/testing "specify a dependency in zen-package.edn"
    (t/testing "check errors, see that namespace the dependency ns is missing"

      (matcho/match (sut-cmd "errors" {:pwd my-package-dir-path})
        {::sut/status :ok
         ::sut/result [{:missing-ns 'my-dep}
                       {:unresolved-symbol 'my-dep/tag}
                       nil]}))

    (t/testing "can safely pull deps without deps specified"
      (matcho/match (sut-cmd "pull-deps" {:pwd my-package-dir-path})
        {::sut/status :ok
         ::sut/result {:status :ok
                       :code :nothing-to-pull
                       :deps empty?}}))

    (zen.test-utils/update-zen-file (io/file my-package-dir-path "zen-package.edn")
                                    #(assoc % :deps {'my-dep (str dependency-dir-path)}))

    (t/testing "do pull-deps & check for errors, should be no errors"
      (matcho/match (sut-cmd "pull-deps" {:pwd my-package-dir-path})
        {::sut/status :ok
         ::sut/result {:status :ok
                       :code :pulled
                       :deps #{'my-dep}}})

      (matcho/match (sut-cmd "errors" {:pwd my-package-dir-path})
        {::sut/status :ok
         ::sut/result nil?}))

    (t/testing "do pull-deps again should be no errors and no changes"

      (matcho/match (sut-cmd "pull-deps" {:pwd my-package-dir-path})
        {::sut/status :ok})

      (matcho/match (sut-cmd "errors" {:pwd my-package-dir-path})
        {::sut/status :ok
         ::sut/result nil?}))


    (t/testing "change repo url, pull"
      (zen.test-utils/update-zen-file (io/file my-package-dir-path "zen-package.edn")
                                      #(assoc % :deps {'my-dep (str dependency-fork-dir-path)}))

      (matcho/match (sut-cmd "pull-deps" {:pwd my-package-dir-path})
        {::sut/status :error})
      (zen.test-utils/update-zen-file (io/file my-package-dir-path "zen-package.edn")
                                      #(assoc % :deps {'my-dep (str dependency-dir-path)}))
      (matcho/match (sut-cmd "errors" {:pwd my-package-dir-path})
        {::sut/status :ok
         ::sut/result nil?})

      (matcho/match (sut-cmd "get-symbol" "my-dep/new-sym" {:pwd my-package-dir-path})
        {::sut/status :ok})))


  (t/testing "commit update to the dependency"
    (zen.test-utils/update-zen-file (io/file dependency-dir-path "zrc" "my-dep.edn")
                                    #(assoc-in % ['new-sym :i-am-forked] :fork-updated))

    (zen.test-utils/git-commit dependency-dir-path "zrc" "Update my-dep/new-sym")

    (t/testing "do pull-deps and see the update"
      (matcho/match (sut-cmd "pull-deps" {:pwd my-package-dir-path})
        {::sut/status :ok
         ::sut/result {:status :ok
                       :code :pulled
                       :deps #{'my-dep}}})

      (matcho/match (sut-cmd "errors" {:pwd my-package-dir-path})
        {::sut/status :ok
         ::sut/result nil?})

      (matcho/match (sut-cmd "get-symbol" "my-dep/new-sym" {:pwd my-package-dir-path})
        {::sut/result {:i-am-forked :fork-updated}})))


  (t/testing "use validate command to validate some data"
    (matcho/match (sut-cmd "validate" "#{my-dep/tag}" "{}" {:pwd my-package-dir-path})
      {::sut/result {:errors [{} nil]}})))

(t/deftest help-command-test
  (matcho/match (sut-cmd "--help" {:pwd test-dir-path})
    {::sut/result
     {:description string?
      :usage
      [{:path ["zen" "build"]      :params vector?}
       {:path ["zen" "build"]      :params vector?}
       {:path ["zen" "changes"]    :params vector?}
       {:path ["zen" "errors"]     :params vector?}
       {:path ["zen" "exit"]       :params vector?}
       {:path ["zen" "get-symbol"] :params vector?}
       {:path ["zen" "get-tag"]    :params vector?}
       {:path ["zen" "init"]       :params vector?}
       {:path ["zen" "install"]    :params vector?}
       {:path ["zen" "pull-deps"]  :params vector?}
       {:path ["zen" "template"]   :params vector?}
       {:path ["zen" "validate"]   :params vector?}]}})

  (matcho/match (sut-cmd "install" "--help" {:pwd test-dir-path})
    {::sut/result
     {:description string?
      :usage       [{:path   ["zen" "install"]
                     :params ["package-identifier"]}]}}))


(defn sut-repl [opts]
  (assert (contains? opts :pwd) "Test repl must specify PWD")

  (let [timeout 5000

        return-atom-promise (atom (promise))
        input-atom-promise  (atom (promise))

        get&promise-new (fn [atom-promise timeout timeout-value]
                          (let [result (deref @atom-promise timeout timeout-value)]
                            (reset! atom-promise (promise))
                            result))

        eval-in-repl! (fn [s]
                        (deliver @input-atom-promise s)
                        (get&promise-new return-atom-promise
                                         timeout
                                         {::sut/status :error, ::sut/code :eval-timeout}))

        repl-opts (merge {:prompt-fn (fn [])
                          :read-fn   #(get&promise-new input-atom-promise timeout "exit")
                          :return-fn #(deliver @return-atom-promise %)}
                         opts)

        start-repl! (fn []
                      #_(future (sut/repl sut/commands repl-opts))
                      (future (sut/cli-main nil repl-opts)))]
    (start-repl!)
    eval-in-repl!))


(def repl-zen-packages-fixtures
  {'my-dep {:deps '#{}
            :zrc '#{{:ns my-dep
                     :import #{}

                     tag {:zen/tags #{zen/schema zen/tag}
                          :type zen/map
                          :require #{:a}
                          :keys {:a {:type zen/string}}}}}}})


(t/deftest repl-test
  (def test-dir-path (io/file "/tmp/zen-cli-repl-test"))
  (def my-package-dir-path (io/file test-dir-path "my-package"))
  (def dependency-dir-path (io/file test-dir-path "my-dep"))

  (zen.test-utils/rm-fixtures test-dir-path)
  (zen.test-utils/mk-fixtures test-dir-path repl-zen-packages-fixtures)

  (def stop-repl-atom (atom false))

  (def eval-in-repl! (sut-repl {:pwd my-package-dir-path
                                :stop-repl-atom stop-repl-atom}))
  (def eval-in-repl-init! (sut-repl {:pwd test-dir-path
                                     :stop-repl-atom stop-repl-atom}))

  (t/testing "init"
    (zen.test-utils/mkdir test-dir-path)

    (matcho/match (eval-in-repl-init! "init my-package")
      {::sut/status :ok, ::sut/code :initted-new})

    (zen.test-utils/git-init-commit my-package-dir-path))

  (zen.test-utils/update-zen-file (io/file my-package-dir-path "zrc" "my-package.edn")
                                  #(assoc %
                                          :import #{'my-dep}
                                          'sym {:zen/tags #{'my-dep/tag}
                                                :a "a"}))

  (t/testing "errors"
    (matcho/match (eval-in-repl! "errors")
      {::sut/result [{:missing-ns 'my-dep}
                     {:unresolved-symbol 'my-dep/tag}
                     nil]}))

  (zen.test-utils/update-zen-file (io/file my-package-dir-path "zen-package.edn")
                                  #(assoc % :deps {'my-dep (str dependency-dir-path)}))

  (t/testing "pull-deps"
    (matcho/match (eval-in-repl! "pull-deps")
      {::sut/status :ok
       ::sut/result {:code :pulled, :deps #{'my-dep}}})

    (matcho/match (eval-in-repl! "errors")
      {::sut/result empty?}))

  (t/testing "changes"
    (matcho/match (eval-in-repl! "changes")
      {::sut/result
       {:changes
        [{:type :namespace/new, :namespace 'my-dep}
         {:type :symbol/new, :symbol 'my-package/sym}
         nil]}}))

  (t/testing "validate"
    (matcho/match (eval-in-repl! "validate #{my-dep/tag} {:a :wrong-type}")
      {::sut/result {:errors [{} nil]}})

    (matcho/match (eval-in-repl! "validate #{my-dep/tag} {:a \"correct-type\"}")
      {:errors empty?}))

  (t/testing "get-symbol"
    (matcho/match (eval-in-repl! "get-symbol my-package/sym")
      {::sut/result {:zen/name 'my-package/sym}}))

  (t/testing "get-tag"
    (matcho/match (eval-in-repl! "get-tag my-dep/tag")
      {::sut/result #{'my-package/sym}}))

  (t/testing "exit"
    (matcho/match (eval-in-repl! "exit")
      {::sut/result {:status :ok, :code :exit}})

    (t/is (true? @stop-repl-atom)))

  #_"NOTE: this is safety stop if repl eval couldn't stop for some reason"
  (reset! stop-repl-atom true))


(t/deftest build-zen-project-into-zip
  (def test-dir-path (io/file "/tmp/zen-cli-build-cmd-test"))
  (def my-package-dir-path (io/file test-dir-path "my-package"))
  (def build-dir (io/file "build-dir"))
  (zen.test-utils/rm-fixtures test-dir-path)

  (t/testing "Initialize project"
    (zen.test-utils/mkdir my-package-dir-path)
    (matcho/match (sut-cmd "init" "my-package" {:pwd test-dir-path})
      {::sut/code :initted-new ::sut/status :ok})

    (matcho/match (zen.test-utils/fs-tree->tree-map my-package-dir-path)
      {"zen-package.edn" some?
       "zrc"             {"my-package.edn" some?}
       ".git"            {}
       ".gitignore"      some?}))

  (t/testing "Building project archive"
    (matcho/match (sut-cmd "build" build-dir "zen-project" {:pwd my-package-dir-path})
      {::sut/result {:status :ok :code :builded}
       ::sut/status :ok})

    (t/testing "Can see project-archive on fs-tree"
      (matcho/match (zen.test-utils/fs-tree->tree-map my-package-dir-path)
        {"build-dir" {"zen-project.zip" some?}
         "zen-package.edn" some?
         "zrc"             {"my-package.edn" some?}
         ".git"            {}
         ".gitignore"      some?}))

    (t/testing "Archive contains only zen-project files, .git is ommited"
      (matcho/match
       (zen.test-utils/zip-archive->fs-tree (io/file my-package-dir-path build-dir "zen-project.zip"))
        {"zrc" {"my-package.edn" {}}}))))
