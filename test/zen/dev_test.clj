(ns zen.dev-test
  (:require [clojure.test :as t]
            [clojure.java.io :as io]
            [zen.core :as zen]
            [matcho.core :as matcho]
            [zen.dev :as dev]))


(System/getProperty "java.io.tmpdir")

(defmacro wait-for [expr times]
  `(loop [t# ~times]
     (if (neg? t#)
       (throw (Exception. (str "Timeout: " ~times " x10ms")))
       (when-not ~expr
         (Thread/sleep 100)
         (print ".")
         (flush)
         (recur (dec t#))))))


(defn delete-directory-recursive
  [^java.io.File file]
  (when (.isDirectory file)
    (doseq [file-in-dir (.listFiles file)]
      (delete-directory-recursive file-in-dir)))
  (io/delete-file file true))


(defn init-project [project]
  (delete-directory-recursive (io/file project))
  (doto (io/file project) (.mkdirs))
  (doto (io/file (str project "/lib")) (.mkdirs)))


(t/deftest test-zen-dev)

(t/deftest test-zen-dev
  (def project (str "/tmp/zentest/" (str (gensym "zen"))))
  (init-project project)
  (def ztx (zen/new-context {:paths [project]}))

  (spit (str project "/dev-test-app.edn") "{ns dev-test-app import #{lib.dev-test-lib} Model1 {:zen/tags #{lib.dev-test-lib/model}}}")
  (spit (str project "/lib/dev-test-lib.edn") "{ns lib.dev-test-lib}")
  (spit (str project "/dev-test-broken.edn") "{ns dev-test-broken")

  (zen/read-ns ztx 'dev-test-app)

  (zen/read-ns ztx 'lib.dev-test-lib)
  (zen/read-ns ztx 'dev-test-broken)

  (t/is (nil? (zen/get-symbol ztx 'dev-test-app/Model2)))
  (t/is (nil? (zen/get-symbol ztx 'lib.dev-test-lib/model)))

  (matcho/match
   (zen/errors ztx)
   [{:message "Could not resolve symbol 'lib.dev-test-lib/model in dev-test-app/Model1",
     :ns 'dev-test-app}
    {:message "EOF while reading, expected } to match { at [1,1]",
     :file #"dev-test-broken.edn",
     :ns 'dev-test-broken}])

  (try
    (dev/watch ztx)
    (spit (str project "/lib/dev-test-lib.edn") "{ns lib.dev-test-lib model {:zen/tags #{zen/tag}}}")
    (spit (str project "/dev-test-app.edn") "{ns dev-test-app import #{lib.dev-test-lib dev-test-broken} Model1 {:zen/tags #{lib.dev-test-lib/model}} Model2 {:zen/tags #{lib.dev-test-lib/model}}}")
    (spit (str project "/dev-test-broken.edn") "{ns dev-test-broken Model {}}")

    (wait-for (zen/get-symbol ztx 'lib.dev-test-lib/model) 100)

    (t/is (not (nil? (zen/get-symbol ztx 'dev-test-app/Model2))))
    (t/is (not (nil? (zen/get-symbol ztx 'dev-test-app/Model1))))
    (t/is (not (nil? (zen/get-symbol ztx 'lib.dev-test-lib/model))))
    (t/is (not (nil? (zen/get-symbol ztx 'dev-test-broken/Model))))

    (matcho/match
     (zen/errors ztx)
     empty?)

    (finally
      (println ::stop)
      (dev/stop ztx))))


(t/deftest not-imported-but-created
  (def project (str "/tmp/zentest/" (str (gensym "zen"))))
  (init-project project)
  (def ztx (zen/new-context {:paths [project]}))

  (try
    (dev/watch ztx)

    (t/testing "not imported ns doesn't loaded after watcher found changes in it"
      (spit (str project "/dev-test-app2.edn") "{ns dev-test-app2 import #{}}")

      (Thread/sleep 200)

      (t/is (not (contains? (:ns @ztx) 'dev-test-app2)))

      (zen/read-ns ztx 'dev-test-app2)

      (t/is (contains? (:ns @ztx) 'dev-test-app2))

      (spit (str project "/not-imported-yet.edn") "{ns not-imported-yet, foo {}}")

      (Thread/sleep 200)

      (t/is (not (contains? (:ns @ztx) 'not-imported-yet)))

      (spit (str project "/dev-test-app2.edn") "{ns dev-test-app2 import #{not-imported-yet}}")

      (wait-for (contains? (:ns @ztx) 'not-imported-yet) 100)
      (t/is (contains? (:ns @ztx) 'not-imported-yet))

      (matcho/match
        (zen/errors ztx)
        empty?))

    (finally
      (println ::stop)
      (dev/stop ztx))))




(t/deftest not-created-but-imported
  (def project (str "/tmp/zentest/" (str (gensym "zen"))))
  (init-project project)
  (def ztx (zen/new-context {:paths [project]}))

  (try
    (dev/watch ztx)

    (t/testing "imported before created, then created and should be loaded"
      (spit (str project "/dev-test-app3.edn") "{ns dev-test-app3 import #{not-created-yet}}")

      (Thread/sleep 1000)
      (t/is (not (contains? (:ns @ztx) 'dev-test-app3)))

      (zen/read-ns ztx 'dev-test-app3)

      (t/is (contains? (:ns @ztx) 'dev-test-app3))

      (spit (str project "/not-created-yet.edn") "{ns not-created-yet, foo {}}")

      (wait-for (contains? (:ns @ztx) 'not-created-yet) 100)

      (t/is (contains? (into #{} (keys (:ns @ztx))) 'not-created-yet))

      (matcho/match
        (zen/errors ztx)
        empty?))

    (finally
      (println ::stop)
      (dev/stop ztx))))
