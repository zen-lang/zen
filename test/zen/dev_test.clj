(ns zen.dev-test
  (:require [clojure.test :as t]
            [clojure.java.io :as io]
            [zen.core :as zen]
            [matcho.core :as matcho]
            [zen.dev :as dev]))


(System/getProperty "java.io.tmpdir")


(defonce project (str "/tmp/zentest/" (str (gensym "zen"))))


(defn init-project []
  (doto (io/file project) (.mkdirs))
  (doto (io/file (str project "/lib")) (.mkdirs))
  (spit (str project "/dev-test-app.edn") "{ns dev-test-app imports #{lib.dev-test-lib} Model1 {:zen/tags #{lib.dev-test-lib/model}}}")
  (spit (str project "/lib/dev-test-lib.edn") "{ns lib.dev-test-lib}")
  (spit (str project "/dev-test-broken.edn") "{ns dev-test-broken"))


(t/deftest test-zen-dev
  (init-project)

  (def ztx (zen/new-context {:paths [project]}))

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
    (spit (str project "/dev-test-app.edn") "{ns dev-test-app imports #{lib.dev-test-lib} Model1 {:zen/tags #{lib.dev-test-lib/model}} Model2 {:zen/tags #{lib.dev-test-lib/model}}}")
    (spit (str project "/dev-test-broken.edn") "{ns dev-test-broken Model {}}")


    (Thread/sleep 200)

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
