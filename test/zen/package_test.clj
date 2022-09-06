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
(defn git-init [dir]
      (sut/sh! "git" "init" :dir dir)
      (sut/sh! "git" "add" "." :dir dir)
      (sut/sh! "git" "commit" "-m" "\"Initial commit\"" :dir dir))

(def root "/tmp/zen/veschin")
(def deps ["a" "b" "c"
           "additional-dep1"
           "additional-dep2"
           "additional-dep3"])

(defn fixture []
  (rm root "/tmp/zen")
  (mkdir root)
  (mkdir (str root "/zrc"))
  (mkdir (str root "/zen-modules"))
  (doseq [dir- deps]
    (let [dir (str "/tmp/" dir-)
          main dir-]
      (rm dir)
      (mkdir dir)
      (mkdir (str dir "/zrc"))
      (spit (str dir "/zen-package.edn")
            (cond-> {:artifact (keyword (string/replace dir- #"/" ".") main)}
              (#{"a" "b" "c"} dir-)
              (assoc :deps (rand-nth [{:author1 "/tmp/additional-dep1/"}
                                      {:author2 "/tmp/additional-dep2/"}
                                      {:author3 "/tmp/additional-dep3/"}]))))
      (spit (str dir "/" main ".edn") {'ns (symbol (str dir- "." main))})
      (git-init dir))))

(t/deftest init
  (fixture)
  (git-init root)

  (spit (str root "/zrc/veschin.edn")
        {'ns 'veschin
         ;; 'import #{'a}
         })

  (spit (str root "/zen-package.edn")
        {:artifact :veschin
         :deps {:a "/tmp/a"
                :b "/tmp/b"
                :c "/tmp/c"}})

  (sut/zen-init-deps! root)

  (def ztx (zen.core/new-context {:paths [(str root "/zrc")
                                          (str root "/zen-modules")]}))

  (zen.core/read-ns ztx 'veschin)

  (zen.core/read-ns ztx 'a)

  (t/is (empty? (zen.core/errors ztx)))

  ;; (t/is (empty? (:errors (zen.core/validate ztx #{'my-module/my-sch-from-a-which-depends-on-b}))))
  )


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
