(ns build.zen
  (:require [clojure.tools.build.api :as b]))

(def lib 'my/lib1)
(def version (format "1.2.%s" (b/git-count-revs nil)))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def uber-file "target/zen.jar")

(defn clean [_]
  (b/delete {:path "target"}))

(defn uber [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src" "resources" "pkg"]
               :target-dir class-dir})
  (b/copy-file {:src "javascript-vendor/build/lib/index.js" :target (str class-dir "/index.js")})
  (b/copy-file {:src "javascript-vendor/build/lib/index.d.ts" :target (str class-dir "/index.d.ts")})
  (b/copy-file {:src "javascript-vendor/build/package.json" :target (str class-dir "/package.json")})
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main 'zen.cli}))
