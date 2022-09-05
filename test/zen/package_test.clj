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


(defn init-stub-dependencies! []
  (doseq [repo [{:name "/a"
                 :deps [['c "/tmp/c"]]}
                {:name "/b"
                 :deps [['c "/tmp/c"]]}
                {:name "/c"
                 :deps []}]]
    (let [dir (str "/tmp" (:name repo))
          ]

      (def dir "/tmp/a")
      (def deps [['c "/tmp/c"]])

      (sut/sh "rm" "-rf" dir)
      (sut/sh "mkdir" "-p" dir)
      (sut/git-init! dir)
      (spit (str dir "/main.edn") (str {'ns 'main (symbol (str (random-uuid))) {}}))
      (spit (str dir "/package.edn") (str deps))
      (sut/sh "git" "add" "." :dir dir)
      (sut/sh "git" "commit" "-m" "\"Initial commit\"" :dir dir))))


(t/deftest init
  (sut/mkdir! "/tmp" "zen.package-test/my-init-zen-module")

  (def path "/tmp/zen.package-test/my-init-zen-module")

  (sut/zen-init! path)

  (spit (str path "/package.edn")
        {:deps {:a "/tmp/a"
                :b "/tmp/b"}})

  ;;spit zrc/my-module.edn

  ;; init a & b


  (sut/zen-init-deps! path)
  ;; zen-module/
  ;;  a/
  ;;   zrc/
  ;;  b/
  ;;   zrc/

  (def ztx (zen.core/new-context {:paths [path]}))

  (zen.core/read-ns ztx 'my-module)

  (t/is (empty? (zen.core/errors ztx)))

  (t/is (empty? (:errors (zen.core/validate ztx #{'my-module/my-sch-from-a-which-depends-on-b})))))


(t/deftest zen-pm
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
