(ns zen.package-test
  (:require [zen.package :as sut]
            [zen.core :as zen]
            [zen.store]
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


(defn zip-entries [filename]
  (let [zf (java.util.zip.ZipFile. filename)]
    (try (->> (.entries zf)
              enumeration-seq
              (mapv #(.getName %)))
         (finally (.close zf)))))


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

  (t/testing "init"
    (mk-fixtures test-dir-path test-zen-repos)

    (t/is (and (.exists (io/file (str module-dir-path "/zen-package.edn")))
               (matcho/match
                 (read-string (slurp (io/file (str module-dir-path "/zen-package.edn"))))
                 {:deps {'a-lib string?}})))

    (t/is (and (.exists (io/file (str module-dir-path "/.gitignore")))
               (= (slurp (io/file (str module-dir-path "/.gitignore")))
                  "\n/zen-packages"))))

  (t/testing "read initted"
    (sut/zen-init-deps! module-dir-path)

    (t/is (.exists (io/file (str module-dir-path "/zen-packages/"))))

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
  (rm zen.store/unzip-cache-dir)
  (mk-fixtures test-dir-path test-zen-repos)

  (t/testing "Zen can build zrc folder with module files on top-level"
    (def user-cfg-fixture {:package-name "package"
                           :build-path "zen-build"
                           :with-latest true})

    (sut/zen-build! module-dir-path user-cfg-fixture)

    (def all-test-ns
      (->> (vals test-zen-repos)
           (mapcat :zrc)
           (map :ns)))

    (t/testing "all namespaces from the fixture are present in the build zrc path"
      (def build-zrc-path (str module-dir-path
                               "/" (:build-path user-cfg-fixture)
                               "/zrc"))

      (t/is (= (into #{}
                     (map #(str build-zrc-path "/" (name %) ".edn"))
                     all-test-ns)
               (into #{}
                     (map str)
                     (rest (file-seq (io/file build-zrc-path)))))))

    (t/testing "zip archive present"
      (def build-zip-path
        (str module-dir-path
             "/" (:build-path user-cfg-fixture)
             "/" (:package-name user-cfg-fixture) ".zip"))

      (t/is (.exists (io/file build-zip-path)))

      (t/testing "all namespaces from the fixture are present in zip"
        (t/is (= (into #{}
                       (map #(str (name %) ".edn"))
                       all-test-ns)
                 (into #{}
                       (zip-entries build-zip-path))))))

    (t/testing "zip archive read-ns"
      (def ztx (zen.core/new-context {:zip-paths [build-zip-path]}))

      (t/testing "no errors on read"
        (zen.core/read-ns ztx 'main)
        (t/is (empty? (zen.core/errors ztx))))

      (t/testing "symbols loaded"
        (t/is (= #{'main/sym}
                 (zen.core/get-tag ztx 'a/tag))))

      (t/testing "recursive deps schemas work"
        (t/is (empty? (:errors (zen.core/validate ztx
                                                  #{'a/recur-sch}
                                                  {:a {:b {:a {:b {}}}}}))))))))
