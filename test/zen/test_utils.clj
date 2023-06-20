(ns zen.test-utils
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.string :as str]
            [clojure.test :refer [is]]
            [matcho.core :as matcho]
            [zen.package]
            [zen.utils :as utils])
  (:import java.io.File))

(defmacro vmatch [tctx schemas subj res]
  `(let [res# (zen.core/validate ~tctx ~schemas ~subj)]
     (matcho/match res# ~res)
     res#))

(defmacro match [tctx schema subj res]
  `(let [res# (zen.core/validate ~tctx #{~schema} ~subj)]
     (matcho/match (:errors res#) ~res)
     (:errors res#)))

(defmacro valid [tctx schema subj]
  `(let [res# (zen.core/validate ~tctx #{~schema} ~subj)]
     (is (empty? (:errors res#)))
     (:errors res#)))

(defmacro valid-schema! [tctx subj]
  `(let [res# (zen.core/validate ~tctx #{'zen/schema} ~subj)]
     (is (empty? (:errors res#)))
     res#))

(defmacro invalid-schema [tctx subj res]
  `(let [res# (zen.core/validate ~tctx #{'zen/schema} ~subj)]
     (matcho/match (:errors res#) ~res)
     (:errors res#)))


(defn zip-entries [^String filename]
  (let [zf (java.util.zip.ZipFile. filename)]
    (try (->> (.entries zf)
              enumeration-seq
              (mapv (fn [^java.util.zip.ZipEntry e] (.getName e))))
         (finally (.close zf)))))


(defn mkdir [dir-name] (.mkdirs dir-name))

(defmacro ^:private predicate [s path]
  `(if ~path
     (. ~path ~s)
     false))

#_{:clj-kondo/ignore [:docstring-no-summary]}
(defn ^File file
  "If `path` is a period, replaces it with cwd and creates a new File object
   out of it and `paths`. Or, if the resulting File object does not constitute
   an absolute path, makes it absolutely by creating a new File object out of
   the `paths` and cwd."
  [path & paths]
  (when-let [path (apply
                   io/file (if (= path ".")
                             utils/*cwd*
                             path)
                   paths)]
    (if (.isAbsolute ^File path)
      path
      (io/file  utils/*cwd* path))))

(defn delete
  "Delete `path`."
  [path]
  (predicate delete (file path)))

(defn directory?
  "Return true if `path` is a directory."
  [path]
  (.isDirectory (file path)))

(defn delete-dir
  "Delete a directory tree."
  [root]
  (when (directory? root)
    (doseq [path (.listFiles (file root))]
      (delete-dir path)))
  (delete root))


(defn rm [root] (delete-dir root))

(defn git-commit [dir to-add message]
  (let [to-add-seq (if (sequential? to-add) to-add [to-add])]
    (apply shell/sh (concat ["git" "add"] to-add-seq [:dir dir]))
    (shell/sh "git" "commit" "-m" message :dir dir)))


(defn git-init-commit [dir]
  (shell/sh "git" "add" "." :dir dir)
  (shell/sh "git" "commit" "-m" "\"Initial commit\"" :dir dir))


(defn mk-module-dir-path [root-dir-path module-name]
  (io/file root-dir-path module-name))


(defn zen-ns->file-name [zen-ns]
  (-> (name zen-ns)
      (str/replace \. \/)
      (str ".edn")))


(defn spit-zrc [module-dir-path zen-namespaces]
  (mkdir (io/file module-dir-path "zrc"))

  (doseq [zen-ns zen-namespaces]
    (let [file-name (zen-ns->file-name (or (get zen-ns :ns) (get zen-ns 'ns)))]
      (spit (io/file module-dir-path "zrc" file-name) zen-ns))))


(defn spit-deps [root-dir-path module-dir-path deps]
  (spit (io/file module-dir-path "zen-package.edn")
        {:deps (into {}
                     (map (fn [dep-name]
                            [dep-name (str (mk-module-dir-path root-dir-path (str dep-name)))]))
                     deps)}))


(defn mk-module-fixture [root-dir-path module-name module-params]
  (let [module-dir-path (mk-module-dir-path root-dir-path (str module-name))]

    (mkdir module-dir-path)

    (zen.package/zen-init! module-dir-path)

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

(comment
  (delete-dir  (io/file "/tmp/zen-cli-test")))

(defn fs-tree->tree-map [init-path]
  (let [path (str init-path)
        splitted-path (drop 1 (str/split path #"/"))
        _ (println :path splitted-path)
        tree-map (reduce
                  (fn [store path] (assoc-in store path {}))
                  {}
                  (map (fn [f] (drop 1 (str/split (str f) #"/"))) (file-seq (io/file path))))]
    (get-in tree-map splitted-path)))


(defn update-zen-file [file-path update-fn]
  (spit file-path (update-fn (read-string (slurp file-path)))))


(defn zip-archive->fs-tree [path-to-zip]
  (with-open [zip-stream
              ^java.util.zip.ZipInputStream
              (-> path-to-zip
                  (io/input-stream)
                  (java.util.zip.ZipInputStream.))]
    (loop [entry
           ^java.util.zip.ZipEntry
           (.getNextEntry zip-stream)

           fs-tree {}]
      (if entry
        (let [entry-name (.getName entry)
              splitted-entry-name (str/split entry-name (java.util.regex.Pattern/compile (str (File/separatorChar))))]
          (recur (.getNextEntry zip-stream)
                 (assoc-in fs-tree splitted-entry-name {})))
        fs-tree))))
