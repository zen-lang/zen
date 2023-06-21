(ns zen.git
  (:require [clj-jgit.porcelain :as git]
            [clojure.java.io :as io])
  (:import java.io.File
           (org.eclipse.jgit.transport
            RemoteConfig)
           (org.eclipse.jgit.api Git)))


(defn init-repo
  ^Git [^File dir & {:keys [branch] :or {branch "main"}}]
  (println (format "Init repository in %s with branch %s" (str dir) branch))
  (git/git-init :branch branch :dir dir))

(defn pull
  ^Git [^File dir]
  (println (format "Pull repository changes %s" dir))
  (-> dir
      git/load-repo
      git/git-pull))

(defn init-template
  ^Git [^File root repository-url]
  (println (format "Cloning [%s] into [%s]" repository-url root))
  (if (.exists root)
    (git/load-repo root)
    (git/git-clone repository-url :dir root :branch nil :clone-all? false)))


(defn get-remote-url
  ^String [^File dir ^String remote-name]
  (->> (git/load-repo dir)
       .remoteList
       .call
       (filter (fn [^RemoteConfig r]
                 (= remote-name (.getName r))))
       (map (fn [^RemoteConfig r]
              (.getURIs r)))
       first
       first
       .toString))

(defn stash
  [^File dir]
  (git/git-stash-create (git/load-repo dir)))

(defn stash-pop
  [^File dir]
  (let [repo (git/load-repo dir)]
    (when (seq (git/git-stash-list repo))
      (git/git-stash-pop (git/load-repo dir)))))

(defn clone
  ^Git [^File dir ^String uri & {:keys [sub-dir]}]
  (println (format "Cloning [%s] into [%s]" uri (str (if sub-dir (io/file dir sub-dir) dir))))
  (git/git-clone  uri
                  :dir  (if sub-dir (io/file dir sub-dir) dir)
                  :depth 1
                  :branch nil))

(defn add
  [^File dir files]
  (println (format "Add files in repository %s" dir))
  (-> (git/load-repo dir)
      (git/git-add files)))

(defn commit
  [^File dir message]
  (println (format "Commit in repository %s" dir))
  (-> (git/load-repo dir)
      (git/git-commit message)))