(ns zen.package
  (:require [clojure.string :as str]
            [clojure.java.shell :as shell]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            clojure.pprint
            [zen.store]))


(defn sh! [& args]
  (println "$" (str/join " " args))
  (let [result (apply shell/sh args)]

    (when-not (str/blank? (:out result))
      (print (:out result)))

    (when-not (str/blank? (:err result))
      (print "!" (:err result)))

    (flush)
    result))


(defn sh&&! [call & calls]
  (let [{:as result, :keys [exit]} (apply sh! call)]
    (if (and (= 0 exit) (seq calls))
      (apply sh&&! calls)
      result)))


(defn mkdir! [path name]
  (sh! "mkdir" "-p" name :dir path))


(defn read-deps [root]
  (try
    (let [package-file (->> (str root "/zen-package.edn")
                            slurp
                            edn/read-string)]
      (:deps package-file))
    (catch Exception e (println (.getMessage e)))))


(defn init-pre-commit-hook! [root]
  (let [precommit-hook-file (str root "/.git/hooks/pre-commit")]
    (spit precommit-hook-file "#!/bin/bash \n\necho \"hello world!\"")
    (sh! "chmod" "+x" precommit-hook-file)))


(defn append-gitignore-zen-packages [root]
  (let [gitignore-path (str root "/.gitignore")
        gitignore      (when (.exists (io/file gitignore-path))
                         (slurp gitignore-path))]
    (when-not (str/includes? (str gitignore) "/zen-packages\n")
      (spit (str root "/.gitignore") "\n/zen-packages" :append true))))


(defn pwd [& {:keys [silent]}]
  (let [sh-fn (if silent shell/sh sh!)]
    (str/trim-newline (:out (sh-fn "pwd")))))


(defn create-file!
  [name content & [root]]
  (let [name (if root (str root "/" name) name)
        file (io/file name)]
    (when-not (.exists file)
      (io/make-parents file))
    (spit name content)))


(defn make-template! [root & {:keys [package-name]}]
  (let [package (io/file (str root "/zen-package.edn"))]
    (if (.exists package)
      :done
      (do
        (mkdir! root "zrc")
        (create-file! "zen-package.edn" {:deps {}} root)
        (append-gitignore-zen-packages root)
        (when package-name
          (create-file! (format "zrc/%s.edn" package-name)
                        (format "{:ns %s \n :import #{}\n\n}" (symbol package-name))
                        root))))))


(defn zen-init! [root & {:keys [package-name]}]
  (sh! "git" "init" :dir root)
  (make-template! root {:package-name package-name})
  (init-pre-commit-hook! root))


(defn zen-init-deps-recur! [root deps]
  (loop [[[dep-name dep-url] & deps-to-init] deps
         initted-deps #{}]
    (cond
      (nil? dep-name)
      initted-deps

      (contains? initted-deps dep-name)
      (recur deps-to-init
             initted-deps)

      :else
      (let [dep-name-str (name dep-name)]
        (sh! "git" "clone" (str dep-url) dep-name-str
             :dir root)
        (recur
          (concat (read-deps (str root "/" dep-name-str))
                  deps-to-init)
          (conj initted-deps dep-name))))))


(defn zen-init-deps! [root]
  (mkdir! root "zen-packages")

  (zen-init-deps-recur!
    (str root "/zen-packages")
    (read-deps root)))


(comment
  (shell/sh "cp" "-r" "/tmp/zen.package-test/test-module/zen-packages/*/zrc/" "/tmp/zen.package-test/zen-build/zrc")
  nil
  )

(defn dir-list [dir] (-> dir clojure.java.io/file .list))


(defn zen-build! [root {:as _cfg :keys [build-path package-name]}]
  (zen-init-deps! root)

  (let [zrc-paths (zen.store/expand-package-path root)
        zrc-dest (str root "/" build-path "/zrc/")
        zip-dest (str root "/" build-path "/" package-name ".zip")]
    (sh! "mkdir" "-p" zrc-dest)
    (doseq [path zrc-paths]
      (sh! "cp" "-r" (str path "/") zrc-dest))
    (sh! "zip" "-r" zip-dest "." :dir zrc-dest)))

#_(defn copy! [& from-to] (apply sh! "cp" "-r" from-to))


#_(defn flat-dir! [dir to] (copy! dir to))





#_(defn clear-files! [dir & files]
  (apply sh! "rm" "-rf" (map #(str dir "/" %) files)))


#_(defn recur-flat! [dir]
  (flat-dir! (str dir "/zrc/.") dir)
  (flat-dir! (str dir "/zen_modules/.") dir)
  (doseq [mdir  (dir-list dir)
          :let  [mdir (str dir "/" mdir)]
          :when (not-empty (dir-list mdir))]
    (recur-flat! mdir))
  (clear-files! dir "package.edn" "zen_modules" ".git"))


#_(defn zen-build! [root]
  (let [build-dir (str root "/build")
        zrc (str root "/zrc")]
    (sh! "rm" "-rf" build-dir)
    (sh! "mkdir" build-dir)
    (copy! zrc (str root "/zen_modules") build-dir)
    (recur-flat! build-dir)
    (sh! "rm" "-rf" (str build-dir "/zen_modules") (str build-dir "/zrc"))
    (sh! "zip" "-r" "uberzen.zip" "." :dir build-dir)))
