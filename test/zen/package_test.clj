(ns zen.package-test
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.test :as t]
   [matcho.core :as matcho]
   [zen.core :as zen]
   [zen.package :as sut]
   [zen.store]
   [zen.test-utils]))


(defn get-git-hash [path]
  (as-> (slurp (str path "/.git/HEAD")) v
    (str/trim-newline v)
    (subs v 5)
    (str path "/.git/" v)
    (slurp v)
    (str/trim-newline v)))


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
  (def test-dir-path (io/file "/tmp/zen.package-test"))
  (def module-dir-path (io/file test-dir-path "test-module"))

  (zen.test-utils/rm-fixtures test-dir-path)

  (t/testing "init"
    (zen.test-utils/mk-fixtures test-dir-path test-zen-repos)

    (t/is (and (.exists (io/file module-dir-path "zen-package.edn"))
               (matcho/match
                (read-string (slurp (io/file module-dir-path "zen-package.edn")))
                 {:deps {'a-lib string?}})))

    (t/is (and (.exists (io/file module-dir-path ".gitignore"))
               (= (slurp (io/file module-dir-path ".gitignore"))
                  "\n/zen-packages"))))

  (t/testing "read initted"
    (println :here module-dir-path)
    (sut/zen-init-deps! module-dir-path)

    (t/is (.exists (io/file module-dir-path "zen-packages")))

    (def ztx (zen/new-context {:package-paths [module-dir-path]}))

    (t/testing "no errors on read"
      (zen/read-ns ztx 'main)
      (t/is (empty? (zen/errors ztx))))

    (t/testing "symbols loaded"
      (t/is (= #{'main/sym}
               (zen/get-tag ztx 'a/tag))))

    (t/testing "recursive deps schemas work"
      (t/is (empty? (:errors (zen/validate ztx
                                           #{'a/recur-sch}
                                           {:a {:b {:a {:b {}}}}})))))))


(t/deftest build-test
  (def test-dir-path (io/file "/tmp/zen.package-test"))
  (def module-dir-path (io/file test-dir-path "test-module"))

  (zen.test-utils/rm-fixtures test-dir-path)
  (zen.test-utils/rm (zen.store/unzip-cache-dir))
  (zen.test-utils/mk-fixtures test-dir-path test-zen-repos)

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
      (def build-zrc-path (io/file module-dir-path
                                   (:build-path user-cfg-fixture)
                                   "zrc"))

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
                       (map #(str "zrc" \/ (name %) ".edn"))
                       all-test-ns)
                 (into #{}
                       (zen.test-utils/zip-entries build-zip-path))))))

    (t/testing "zip archive read-ns"
      (def ztx (zen/new-context {:zip-paths [build-zip-path]}))

      (t/testing "no errors on read"
        (zen/read-ns ztx 'main)
        (t/is (empty? (zen/errors ztx))))

      (t/testing "symbols loaded"
        (t/is (= #{'main/sym}
                 (zen/get-tag ztx 'a/tag))))

      (t/testing "recursive deps schemas work"
        (t/is (empty? (:errors (zen/validate ztx
                                             #{'a/recur-sch}
                                             {:a {:b {:a {:b {}}}}}))))))))
