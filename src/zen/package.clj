(ns zen.package
  (:require
   clojure.edn
   [clojure.java.shell :as sh]
   ;; [clojure.java.io :as io]
   [clojure.string :as string]
   [clojure.test :as t]))

(def root "/tmp/zen")

(defn git-init [path] (sh/sh "git" "init" :dir path))

(defn zen-clone [package-file]
  (->> package-file
       slurp
       clojure.edn/read-string
       (map (comp #(->> (string/replace (first %) #"\." "/")
                        (str root "/zen_modules/")
                        (sh/sh "git" "clone" (second %)))
                  #(update % 0 str)))))

(comment

  (doseq [repo ["/a" "/b" "/c"]
          :let [dir (str "/tmp" repo)]]
    (sh/sh "mkdir" "-p" dir)
    (git-init dir)
    (spit (str dir "/main.edn") (str {'ns 'main (symbol (str (random-uuid))) {}}))
    (sh/sh "git" "add" "." :dir dir)
    (sh/sh "git" "commit" "-m" "\"Initial commit\"" :dir dir))

  (do
    (def zrc (str root "/zrc"))
    (sh/sh "rm" "-rf" root)
    (.mkdir (java.io.File. root))
    (.mkdir (java.io.File. zrc))
    (git-init root)
    (spit (str root "/.gitignore") "/zen_modules")

    (def precommit-hook-file (str root "/.git/hooks/pre-commit"))
    (spit precommit-hook-file "#!/bin/bash \n\necho \"hello world!\"")
    (sh/sh "chmod" "+x" precommit-hook-file)

    (spit (str zrc "/module1.edn") (str '{ns module1}))

    (def package-file (str zrc "/../package.edn"))
    (spit package-file (str '[[a "/tmp/a"]
                              [b.dir "/tmp/b"]
                              [c "/tmp/c"]]))
    (zen-clone package-file))
  )

(defn get-hash [path]
  (->> "/.git/refs/heads/master"
       (str path)
       slurp
       string/trim-newline))

(t/deftest zen-pm

    (t/is (= (get-hash "/tmp/a")
             (get-hash "/tmp/zen/zen_modules/a")))

    (t/is (= (get-hash "/tmp/b")
             (get-hash "/tmp/zen/zen_modules/b/dir")))

    (t/is (= (get-hash "/tmp/c")
             (get-hash "/tmp/zen/zen_modules/c"))))
