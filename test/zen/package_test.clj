(ns zen.package-test
  (:require [zen.package :as sut]
            [zen.core :as zen]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.test :as t]
            [matcho.core :as matcho]))


(defn get-git-hash [path]
  (as-> (slurp (str path "/.git/HEAD")) v
    (str/trim-newline v)
    (subs v 5)
    (str path "/.git/" v)
    (slurp v)
    (str/trim-newline v)))


(defn zip-entries [zipfile]
  (enumeration-seq (.entries zipfile)))


(defn build-zip-flat-tree [filename]
  (let [zf (java.util.zip.ZipFile. filename)]
    (try (->> zf zip-entries (map #(.getName %)) vec)
         (finally (.close zf)))))


;; (defn init-stub-dependencies! []
;;   (doseq [repo [{:name "/a"
;;                  :deps [['c "/tmp/c"]]}
;;                 {:name "/b"
;;                  :deps [['c "/tmp/c"]]}
;;                 {:name "/c"
;;                  :deps []}]]
;;     (let [dir (str "/tmp" (:name repo))
;;           ]

;;       (def dir "/tmp/a")
;;       (def deps [['c "/tmp/c"]])

;;       (sut/sh! "rm" "-rf" dir)
;;       (sut/sh! "mkdir" "-p" dir)
;;       (sut/git-init! dir)
;;       (spit (str dir "/main.edn") (str {'ns 'main (symbol (str (random-uuid))) {}}))
;;       (spit (str dir "/package.edn") (str deps))
;;       (sut/sh! "git" "add" "." :dir dir)
;;       (sut/sh! "git" "commit" "-m" "\"Initial commit\"" :dir dir))))

(defn mkdir [name] (sut/sh! "mkdir" "-p" name))
(defn rm [& names] (apply sut/sh! "rm" "-rf" names))

(defn git-init-commit [dir]
  (sut/sh! "git" "add" "." :dir dir)
  (sut/sh! "git" "commit" "-m" "\"Initial commit\"" :dir dir))


(defn mk-module-dir-path [root-dir-path module-name]
  (str root-dir-path "/" module-name))


(defn zen-ns->file-name [zen-ns]
  (-> (name zen-ns)
      (str/replace \. \/)
      (str ".edn")))


(defn spit-zrc [module-dir-path zen-namespaces]
  (mkdir (str module-dir-path "/zrc"))

  (doseq [zen-ns zen-namespaces]
    (let [file-name (zen-ns->file-name (or (get zen-ns :ns) (get zen-ns 'ns)))]
      (spit (str module-dir-path "/zrc/" file-name) zen-ns))))


(defn spit-deps [root-dir-path module-dir-path deps]
  (spit (str module-dir-path "/zen-package.edn")
        {:deps (into {}
                     (map (fn [dep-name]
                            [dep-name (mk-module-dir-path root-dir-path dep-name)]))
                     deps)}))


(defn mk-module-fixture [root-dir-path module-name module-params]
  (let [module-dir-path (mk-module-dir-path root-dir-path module-name)]

    (mkdir module-dir-path)

    (sut/zen-init! module-dir-path)

    (spit-zrc module-dir-path (:zrc module-params))

    (spit-deps root-dir-path module-dir-path (:deps module-params))

    (git-init-commit module-dir-path)

    :done))


(defn mk-fixtures [test-dir-path deps]
  (mkdir test-dir-path)

  (doseq [module-name (keys deps)]
    (mk-module-fixture test-dir-path module-name (get deps module-name))))


(defn rm-fixtures [test-dir-path]
  (rm test-dir-path))


(def test-zen-repos
  {'test-module {:deps '#{a-lib}
                 :zrc '#{{:ns main
                          :import #{a}
                          sym {:zen/tags #{a/tag}
                               :a "a"}}}}

   'a-lib       {:deps '#{a-lib-dep b-lib}
                 :zrc '#{{:ns a
                          :import #{a-dep b}
                          tag {:zen/tags #{zen/schema zen/tag}
                               :confirms #{a-dep/tag-sch}}
                          recur-sch {:zen/tags #{zen/schema}
                                     :type zen/map
                                     :keys {:a {:confirms #{b/recur-sch}}}}}}}

   'b-lib        {:deps '#{a-lib}
                  :zrc '#{{:ns b
                           :import #{a}
                           recur-sch {:zen/tags #{zen/schema}
                                      :type zen/map
                                      :keys {:b {:confirms #{a/recur-sch}}}}}}}

   'a-lib-dep   {:deps '#{}
                 :zrc '#{{:ns a-dep
                          tag-sch
                          {:zen/tags #{zen/schema}
                           :type zen/map
                           :require #{:a}
                           :keys {:a {:type zen/string}}}}}}})


(t/deftest init-test
  (def test-dir-path "/tmp/zen.package-test")
  (def module-dir-path (str test-dir-path "/test-module"))

  (rm-fixtures test-dir-path)
  (mk-fixtures test-dir-path test-zen-repos)

  (t/testing "init & read initted"
    (sut/zen-init-deps! module-dir-path)

    (def ztx (zen.core/new-context {:package-paths [module-dir-path]}))

    (t/testing "no errors on read"
      (zen.core/read-ns ztx 'main)
      (t/is (empty? (zen.core/errors ztx))))

    (t/testing "symbols loaded"
      (t/is (= #{'main/sym}
               (zen.core/get-tag ztx 'a/tag))))

    (t/testing "recursive deps schemas work"
      (t/is (empty? (:errors (zen.core/validate ztx
                                                #{'a/recur-sch}
                                                {:a {:b {:a {:b {}}}}})))))))


(t/deftest build-test
  (def test-dir-path "/tmp/zen.package-test")
  (def module-dir-path (str test-dir-path "/test-module"))

  (rm-fixtures test-dir-path)
  (mk-fixtures test-dir-path test-zen-repos)

  (t/testing "Zen can build zrc folder with module files on top-level"
    (def user-cfg-fixture {:package-name "package"
                           :build-path "zen-build"
                           :with-latest true})

    (sut/zen-build! module-dir-path user-cfg-fixture)

    (t/testing "all namespaces from the fixture are present in the build zrc path"
      (def all-test-ns
        (->> (vals test-zen-repos)
             (mapcat :zrc)
             (map :ns)))

      (def build-zrc-path (str module-dir-path "/" (:build-path user-cfg-fixture) "/zrc"))

      (t/is (= (into #{}
                     (map #(str build-zrc-path "/" (name %) ".edn"))
                     all-test-ns)
               (into #{}
                     (map str)
                     (rest (file-seq (io/file build-zrc-path)))))))))


#_(t/deftest zen-pm
  (init-stub-dependencies!)

  (def root "/tmp/zen")

  (t/testing "Zen can recursively load dependencies"
    (sut/zen-init! root)

    (t/is (= (get-git-hash "/tmp/a")
             (get-git-hash "/tmp/zen/zen_modules/a")))
    (t/is (= (get-git-hash "/tmp/c")
             (get-git-hash "/tmp/zen/zen_modules/a/zen_modules/c")))

    (t/is (= (get-git-hash "/tmp/b")
             (get-git-hash "/tmp/zen/zen_modules/b/dir")))
    (t/is (= (get-git-hash "/tmp/c")
             (get-git-hash "/tmp/zen/zen_modules/b/dir/zen_modules/c"))))

  (t/testing "Zen can build uberzen"
    (sut/zen-build! root)

    (t/is (= #{"main.edn"
               "a/"
               "a/main.edn"
               "a/c/"
               "a/c/main.edn"
               "b/"
               "b/dir/"
               "b/dir/main.edn"
               "b/dir/c/"
               "b/dir/c/main.edn"}
             (set (build-zip-flat-tree "/tmp/zen/build/uberzen.zip"))))))
