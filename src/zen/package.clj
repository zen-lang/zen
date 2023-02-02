(ns zen.package
  (:require [clojure.string :as str]
            [clojure.java.shell :as shell]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [zen.store]
            [zen.utils])
  (:import java.io.File))

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
  (let [package-file (io/file (str root "/zen-package.edn"))]
    (if (.exists package-file)
      (let [package-file-content
            (->> package-file
                 slurp
                 edn/read-string)]
        (not-empty (:deps package-file-content))))))


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
  (let [not-empty-zen-dir? (->> (file-seq (io/file root))
                                (filter (fn [^java.io.File f] (.isFile f)))
                                seq)]
    (if not-empty-zen-dir?
      nil
      (do
        (sh! "git" "init" :dir root)
        (make-template! root {:package-name package-name})
        (init-pre-commit-hook! root)))))


(defn zen-pull-deps-recur! [root deps & [ssh-cmd]]
  (loop [[[dep-name dep-url] & deps-to-init] deps
         pulled-deps #{}
         ssh-cmd ssh-cmd]
    (if (nil? dep-name)
      pulled-deps
      (let [dep-name-str (name dep-name)
            dep-dir-path (str root "/" dep-name-str)
            dep-dir (io/file dep-dir-path)
            shell-env (and ssh-cmd {"GIT_SSH_COMMAND" ssh-cmd})
            sh-with-env! (fn [& args]
                           (apply sh! (concat args [:env shell-env])))]
        (cond
          (contains? pulled-deps dep-name)
          (recur deps-to-init
                 pulled-deps
                 ssh-cmd)

          (and (.exists dep-dir)
               (= dep-url
                  (->> (sh-with-env! "git" "remote" "get-url" "origin" :dir dep-dir-path)
                       :out
                       str/trim-newline)))
          (do
            (sh-with-env! "git" "pull" :dir (str root "/" dep-name-str))
            (recur
              (concat (read-deps (str root "/" dep-name-str))
                      deps-to-init)
              (conj pulled-deps dep-name)
              ssh-cmd))

          :else
          (do
            (when (.exists dep-dir)
              (throw (Exception. (format "Directory %s already exists" dep-dir-path))))
            (sh-with-env! "git" "clone" "--depth=1" (str dep-url) dep-name-str
                          :dir root)
            (recur
              (concat (read-deps dep-dir-path)
                      deps-to-init)
              (conj pulled-deps dep-name)
              ssh-cmd)))))))


(defn zen-init-deps! [root & [ssh-cmd]]
  (when-let [deps (read-deps root)]
    (mkdir! root "zen-packages")
    (zen-pull-deps-recur! (str root "/zen-packages")
                          deps
                          ssh-cmd)))


(comment
  (shell/sh "cp" "-r" "/tmp/zen.package-test/test-module/zen-packages/*/zrc/" "/tmp/zen.package-test/zen-build/zrc")
  nil
  )


(defn dir-list [dir] (-> dir clojure.java.io/file .list))


(defn zen-build! [root {:as _cfg,
                        :keys [build-path package-name]}]
  (zen-init-deps! root)

  (let [sep-char (File/separatorChar)
        build-dir-name (.getName (io/file build-path))
        build-dir (if build-path (str root sep-char build-path) (str root sep-char build-dir-name))
        zip-name (str (or package-name "zen-package") ".zip")
        zip-write-path (str build-dir sep-char zip-name)
        zip-write-file (io/file zip-write-path)
        _ (when-not (.exists zip-write-file) (.mkdir zip-write-file))
        expanded-package-paths (zen.store/expand-package-path root)
        _ (doseq [p expanded-package-paths]
            (zen.utils/copy-directory p (str build-dir sep-char "zrc")))
        _ (when (.exists (io/file (str root sep-char "ftr")))
            (zen.utils/copy-directory (str root sep-char "ftr") (str build-dir sep-char "ftr")))
        sep-regex (java.util.regex.Pattern/compile (str (File/separatorChar)))]

    (with-open [zip-stream
                ^java.util.zip.ZipOutputStream
                (->
                  zip-write-path
                  (io/output-stream)
                  (java.util.zip.ZipOutputStream.))]

      (doseq [^java.io.File f (file-seq (io/file build-dir))
              :when (and (not (.isDirectory f))
                         (not (str/includes? (.getPath f) ".git"))
                         (not= (.getName f) zip-name))
              :let [splitted-file-path (str/split (.getPath f) sep-regex)
                    relative-to-build-directory-path (str/join sep-char (rest (drop-while (complement #{build-dir-name}) splitted-file-path)))
                    zip-entry
                    ^java.util.zip.ZipEntry
                    (java.util.zip.ZipEntry. relative-to-build-directory-path)]]
        (.putNextEntry zip-stream zip-entry)
        (io/copy f zip-stream)
        (.closeEntry zip-stream)))))

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
