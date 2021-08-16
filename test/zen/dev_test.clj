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
  (spit (str project "/myapp.edn") "{ns myapp imports #{lib.mylib} Model1 {:zen/tags #{lib.mylib/model}}}")
  (spit (str project "/lib/mylib.edn") "{ns lib.mylib}")
  (spit (str project "/broken.edn") "{ns broken"))

(t/deftest test-zen-dev
  (init-project)

  (def ztx (zen/new-context {:paths [project]}))

  (zen/read-ns ztx 'myapp)


  (zen/read-ns ztx 'lib.mylib)
  (zen/read-ns ztx 'broken)

  (t/is (nil? (zen/get-symbol ztx 'myapp/Model2)))
  (t/is (nil? (zen/get-symbol ztx 'lib.mylib/model)))

  (matcho/match
   (zen/errors ztx)
   [{:message "EOF while reading, expected } to match { at [1,1]",
     :file #"broken.edn",
     :ns 'broken}
    {:message "Could not resolve symbol 'lib.mylib/model in myapp/Model1",
     :ns 'myapp}])

  (try
    (dev/watch ztx)
    (spit (str project "/lib/mylib.edn") "{ns lib.mylib model {:zen/tags #{zen/tag}}}")
    (spit (str project "/myapp.edn") "{ns myapp imports #{lib.mylib} Model1 {:zen/tags #{lib.mylib/model}} Model2 {:zen/tags #{lib.mylib/model}}}")
    (spit (str project "/broken.edn") "{ns broken Model {}}")


    (Thread/sleep 200)

    (t/is (not (nil? (zen/get-symbol ztx 'myapp/Model2))))
    (t/is (not (nil? (zen/get-symbol ztx 'myapp/Model1))))
    (t/is (not (nil? (zen/get-symbol ztx 'lib.mylib/model))))
    (t/is (not (nil? (zen/get-symbol ztx 'broken/Model))))

    (matcho/match
     (zen/errors ztx)
     empty?)

    (finally
      (println ::stop)
      (dev/stop ztx)))




  )
