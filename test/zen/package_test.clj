(ns zen.package-test
  (:require [zen.package :as sut]
            [zen.core :as zen]
            [clojure.string :as string]
            [clojure.test :as t]))


(defn get-git-hash [path]
  (as-> (slurp (str path "/.git/HEAD")) v
    (string/trim-newline v)
    (subs v 5)
    (str path "/.git/" v)
    (slurp v)
    (string/trim-newline v)))


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
  (sut/sh! "git" "init" :dir dir)
  (sut/sh! "git" "add" "." :dir dir)
  (sut/sh! "git" "commit" "-m" "\"Initial commit\"" :dir dir))


(defn mk-module-dir [root-dir module-name]
  (str root-dir "/" module-name))


(defn zen-ns->file-name [zen-ns]
  (-> (name zen-ns)
      (string/replace \. \/)
      (str ".edn")))


(defn spit-zrc [module-dir zen-namespaces]
  (mkdir (str module-dir "/zrc"))

  (doseq [zen-ns zen-namespaces]
    (let [file-name (zen-ns->file-name (get zen-ns 'ns))]
      (spit (str module-dir "/zrc/" file-name) zen-ns))))


(defn spit-deps [root-dir module-dir deps]
  (spit (str module-dir "/zen-package.edn")
        {:deps (into {}
                     (map (fn [dep-name]
                            [dep-name (mk-module-dir root-dir dep-name)]))
                     deps)}))


(defn mk-module-fixture [root-dir module-name module-params]
  (let [module-dir (mk-module-dir root-dir module-name)]

    (spit-zrc module-dir (:zrc module-params))

    (spit-deps root-dir module-dir (:deps module-params))

    (git-init-commit module-dir)

    :done))


(defn mk-fixtures [test-dir deps]
  (mkdir test-dir)

  (doseq [module-name (keys deps)]
    (mk-module-fixture test-dir module-name (get deps module-name))))


(defn rm-fixtures [test-dir]
  (rm test-dir))


(t/deftest init-test
  (def test-dir "/tmp/zen.package-test")

  (rm-fixtures test-dir)

  (mk-fixtures test-dir
               {'test-module {:deps '#{a-lib}
                              :zrc '#{{ns main
                                       import #{a}
                                       sym {:zen/tags #{a/tag}
                                            :a "a"}}}}

                'a-lib       {:deps '#{a-lib-dep}
                              :zrc '#{{ns a
                                       import #{a-dep}
                                       tag
                                       {:zen/tags #{zen/schema zen/tag}
                                        :confirms #{a-dep/tag-sch}}}}}

                'a-lib-dep   {:deps '#{}
                              :zrc '#{{ns a-dep
                                       tag-sch
                                       {:zen/tags #{zen/schema}
                                        :type zen/map
                                        :require #{:a}
                                        :keys {:a {:type zen/string}}}}}}})

  (def module-dir (str test-dir "/test-module"))

  (sut/zen-init-deps! module-dir)

  (def ztx (zen.core/new-context {:package-paths [module-dir]}))

  (zen.core/read-ns ztx 'main)

  (t/is (empty? (zen.core/errors ztx)))

  (t/is (= #{'main/sym}
           (zen.core/get-tag ztx 'a/tag)))

  (t/is (empty? (:errors (zen.core/validate ztx
                                            #{'a/tag}
                                            (zen.core/get-symbol ztx 'main/sym))))))


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
