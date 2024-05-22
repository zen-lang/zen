(ns zen.package
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.java.shell :as shell]
   [clojure.string :as str]
   [zen.store]
   [zen.utils :as utils])
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


(defn mkdir! [path dir-name]
  (.mkdirs (io/file path dir-name)))

(defn format-dependency
  [dependency-id]
  (cond
    (clojure.string/ends-with? dependency-id ".git")
    (let [[_ repository] (re-find #".*/(.*?)\.git$" dependency-id)]
      [(symbol repository) dependency-id])))

(defn add-package
  [root & deps]
  (utils/update-file
   (io/file root "zen-package.edn")
   (fn [zen-package]
     (update zen-package :deps (fnil into {}) deps))))

(defn read-deps [^File root]
  (let [package-file (io/file root "zen-package.edn")]
    (if (.exists package-file)
      (let [package-file-content
            (->> package-file
                 slurp
                 edn/read-string)]
        (not-empty (:deps package-file-content))))))


(defn init-pre-commit-hook! [root]
  (let [precommit-hook-file (io/file root ".git" "hooks" "pre-commit")]
    (spit precommit-hook-file "#!/bin/bash \n\necho \"hello world!\"")
    (sh! "chmod" "+x" precommit-hook-file)))


(defn append-gitignore-zen-packages [root]
  (let [gitignore-file (io/file root ".gitignore")
        gitignore      (when (.exists gitignore-file)
                         (slurp gitignore-file))]
    (when-not (str/includes? (str gitignore) "/zen-packages\n")
      (spit gitignore-file "\n/zen-packages" :append true))))


(defn pwd [& {:keys [silent]}]
  (let [sh-fn (if silent shell/sh sh!)]
    (str/trim-newline (:out (sh-fn "pwd")))))


(defn create-file!
  [file-name content & [root]]
  (let [file (if root (io/file root file-name) (io/file file-name))]
    (when-not (.exists file)
      (io/make-parents file))
    (spit file content)))

(defn make-template! [root & {:keys [package-name]}]
  (let [package (io/file root "zen-package.edn")]
    (if (.exists package)
      :done
      (do
        (mkdir! root "zrc")
        (create-file! "zen-package.edn" {:deps {}} root)
        (append-gitignore-zen-packages root)
        (when package-name
          (create-file! (format "zrc%s%s.edn" (File/separator) package-name)
                        (format "{:ns %s \n :import #{}\n\n}" (symbol package-name))
                        root))
        :ok))))


(defn zen-init! [root & {:keys [package-name]}]
  (let [package-dir (if package-name (io/file root (str package-name)) (io/file root))
        _ (.mkdirs package-dir)
        not-empty-zen-dir? (->> (file-seq package-dir)
                                (filter (fn [^java.io.File f] (.isFile f)))
                                seq)]
    (if not-empty-zen-dir?
      nil
      (do
        (sh! "git" "init" :dir package-dir)
        (make-template! package-dir {:package-name package-name})
        #_(init-pre-commit-hook! package-dir)))))


(defn zen-pull-deps-recur! [root deps & [ssh-cmd]]
  (loop [[[dep-name dep-url] & deps-to-init] deps
         pulled-deps #{}
         ssh-cmd ssh-cmd]
    (if (nil? dep-name)
      pulled-deps
      (let [dep-name-str (name dep-name)
            dep-dir (io/file root dep-name-str)
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
                  (->> (sh-with-env! "git" "remote" "get-url" "origin" :dir dep-dir)
                       :out
                       str/trim-newline)))
          (do
            (sh-with-env! "git" "pull" :dir (io/file root dep-name-str))
            (recur
             (concat (read-deps (io/file root dep-name-str))
                     deps-to-init)
             (conj pulled-deps dep-name)
             ssh-cmd))

          :else
          (do
            (when (.exists dep-dir)
              (throw (Exception. (format "Directory %s already exists" dep-dir))))
            (sh-with-env! "git" "clone" "--depth=1" (str dep-url) dep-name-str
                          :dir root)
            (recur
             (concat (read-deps dep-dir)
                     deps-to-init)
             (conj pulled-deps dep-name)
             ssh-cmd)))))))


(defn zen-init-deps! [root & [ssh-cmd]]
  (when-let [deps (read-deps root)]
    (mkdir! root "zen-packages")
    (zen-pull-deps-recur! (io/file root "zen-packages")
                          deps
                          ssh-cmd)))

(defn zen-build! [root {:as _cfg,
                        :keys [build-path package-name]}]
  (zen-init-deps! root)
  (let [sep-char (File/separatorChar)
        build-dir-name (.getName (io/file build-path))
        build-dir (if build-path
                    (io/file root build-path)
                    (io/file root build-dir-name))
        zip-name (str (or package-name "zen-package") ".zip")
        zip-write-file (io/file build-dir zip-name)
        _ (when-not (.exists zip-write-file)
            (io/make-parents zip-write-file))
        expanded-package-paths (zen.store/expand-package-path root)
        _ (doseq [p expanded-package-paths]
            (utils/copy-directory p (io/file build-dir "zrc")))
        expanded-nippy-paths (zen.store/expand-nippy-path root)
        _ (doseq [p expanded-nippy-paths]
            (utils/copy-file p (io/file (io/file build-dir "zrc") (last (str/split (str p) #"/zen-packages/")))))
        _ (when (.exists (io/file root "ftr"))
            (utils/copy-directory (io/file root "ftr") (io/file build-dir "ftr")))
        sep-regex (java.util.regex.Pattern/compile (str (File/separatorChar)))]
    (with-open [zip-stream
                ^java.util.zip.ZipOutputStream
                (->
                  zip-write-file
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

(defn init-template
  [root repository-url]
  (sh! "git" "clone" "--depth=1" repository-url  "." :dir root))
