(ns zen.test-utils
  (:require [matcho.core :as matcho]
            [zen.package]
            [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.string :as str]
            [clojure.test :refer [is]])
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


(defn zip-entries [filename]
  (let [zf (java.util.zip.ZipFile. filename)]
    (try (->> (.entries zf)
              enumeration-seq
              (mapv #(.getName %)))
         (finally (.close zf)))))


(defn mkdir [name] (shell/sh "mkdir" "-p" name))


(defn rm [& names] (apply shell/sh "rm" "-rf" names))


(defn git-commit [dir to-add message]
  (let [to-add-seq (if (sequential? to-add) to-add [to-add])]
    (apply shell/sh (concat ["git" "add"] to-add-seq [:dir dir]))
    (shell/sh "git" "commit" "-m" message :dir dir)))


(defn git-init-commit [dir]
  (shell/sh "git" "add" "." :dir dir)
  (shell/sh "git" "commit" "-m" "\"Initial commit\"" :dir dir))


(defn mk-module-dir-path [root-dir-path module-name]
  (str root-dir-path "/" module-name))


(defn zen-ns->file-name [zen-ns]
  (-> (name zen-ns)
      (str/replace \. \/)
      (str ".edn")))


(defn spit-zrc [module-dir-path zen-namespaces]
  (mkdir (str module-dir-path "/zrc"))

  (doseq [zen-ns zen-namespaces]
    (let [file-name (zen-ns->file-name (or (get zen-ns :ns) (get zen-ns 'ns)))]
      (spit (str module-dir-path "/zrc/" file-name) zen-ns))))


(defn spit-deps [root-dir-path module-dir-path deps]
  (spit (str module-dir-path "/zen-package.edn")
        {:deps (into {}
                     (map (fn [dep-name]
                            [dep-name (mk-module-dir-path root-dir-path dep-name)]))
                     deps)}))


(defn mk-module-fixture [root-dir-path module-name module-params]
  (let [module-dir-path (mk-module-dir-path root-dir-path module-name)]

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


(defn fs-tree->tree-map [path]
  (let [splitted-path (drop 1 (str/split path #"/"))
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
