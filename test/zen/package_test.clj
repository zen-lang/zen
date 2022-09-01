(ns zen.package-test
  (:require [zen.package :as sut]
            [clojure.string :as string]
            [clojure.test :as t]))

(defn get-git-hash [path]
  (def path "/tmp/a")
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

(def root "/tmp/zen")

(t/use-fixtures :once (fn [f]
                        (sut/init-stub-dependencies!)
                        (f)))

(t/deftest zen-pm
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
    (t/is (= ["main.edn"
              "a/"
              "a/main.edn"
              "a/c/"
              "a/c/main.edn"
              "b/"
              "b/dir/"
              "b/dir/main.edn"
              "b/dir/c/"
              "b/dir/c/main.edn"]
             (build-zip-flat-tree "/tmp/zen/build/uberzen.zip")))))

(comment
  (sut/zen-build! root)
  (walkzip "/tmp/zen/build/uberzen.zip")

  (let [zf (java.util.zip.ZipFile. "/tmp/zen/build/uberzen.zip")]
    (try (->> zf zip-entries (map #(.getName %)) vec)
         (finally (.close zf))))

  nil)
