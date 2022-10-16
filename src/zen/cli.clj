(ns zen.cli
  (:gen-class)
  (:require [zen.package]
            [zen.changes]
            [zen.core]
            [clojure.pprint]
            [clojure.java.io :as io]
            [clojure.string]
            [clojure.edn]
            [clojure.stacktrace]
            [clojure.java.shell]))


(defn str->edn [x]
  (clojure.edn/read-string (str x)))


(defn get-pwd [{:keys [pwd] :as _args}]
  (or (some-> pwd (clojure.string/replace #"/+$" ""))
      (zen.package/pwd :silent true)))


(defn load-ztx [opts]
  (zen.core/new-context {:package-paths [(get-pwd opts)]}))


(defn collect-all-project-namespaces [opts]
  (let [pwd (get-pwd opts)
        zrc (str pwd "/zrc")
        relativize #(subs % (count zrc))
        zrc-edns (->> zrc
                      clojure.java.io/file
                      file-seq
                      (filter #(clojure.string/ends-with? % ".edn"))
                      (map #(relativize (.getAbsolutePath %)))
                      (remove clojure.string/blank?)
                      (map #(subs % 1)))
        namespaces (map #(-> %
                             (clojure.string/replace ".edn" "")
                             (clojure.string/replace \/ \.)
                             symbol)
                        zrc-edns)]
    namespaces))


(defn load-used-namespaces
  ([ztx opts]
   (load-used-namespaces ztx (collect-all-project-namespaces opts) opts))

  ([ztx symbols _args]
   (doseq [s symbols]
     (let [sym (symbol s)
           zen-ns (or (some-> sym namespace symbol)
                      sym)]
       (zen.core/read-ns ztx zen-ns)))
   ztx))


(defn init
  ([opts] (init nil opts))

  ([_ztx {:keys [name] :as opts}]
   (if (zen.package/zen-init! (get-pwd opts) {:package-name (str->edn name)})
     {:status :ok, :code :initted-new}
     {:status :ok, :code :already-exists})))


(defn pull-deps
  ([opts] (pull-deps nil opts))

  ([_ztx opts]
   (if-let [initted-deps (zen.package/zen-init-deps! (get-pwd opts))]
     {:status :ok, :code :pulled, :deps initted-deps}
     {:status :ok, :code :nothing-to-pull})))


(defn errors
  ([opts] (errors (load-ztx opts) opts))

  ([ztx opts]
   (load-used-namespaces ztx opts)
   (zen.core/errors ztx :order :as-is)))


(defn validate
  ([symbols-str data-str opts] (validate (load-ztx opts) symbols-str data-str opts))

  ([ztx symbols-str data-str opts]
   (let [symbols (str->edn symbols-str)
         data (str->edn data-str)]
     (load-used-namespaces ztx symbols opts)
     (zen.core/validate ztx symbols data))))


(defn get-symbol
  ([sym-str opts]
   (get-symbol (load-ztx opts) sym-str opts))

  ([ztx sym-str opts]
   (let [sym (str->edn sym-str)]
     (zen.core/read-ns ztx sym)
     (load-used-namespaces ztx [sym] opts)
     (zen.core/get-symbol ztx sym))))


(defn get-tag
  ([tag-str opts] (get-tag (load-ztx opts) tag-str opts))

  ([ztx tag-str opts]
   (load-used-namespaces ztx opts)
   (zen.core/get-tag ztx (str->edn tag-str))))


(defn exit
  ([opts] (exit nil opts))

  ([_ _] (System/exit 0)))


(defn changes
  ([opts] (changes (load-ztx opts) opts))

  ([new-ztx opts]
   (let [pwd     (get-pwd opts)
         new-ztx (load-used-namespaces new-ztx opts)
         _stash! (clojure.java.shell/sh "git" "stash" :dir pwd) #_"NOTE: should this logic be moved to zen.package?"
         old-ztx (load-used-namespaces (load-ztx opts) opts)
         _pop!   (clojure.java.shell/sh "git" "stash" "pop" :dir pwd)]
     (zen.changes/check-changes old-ztx new-ztx))))


(def commands
  {"init"       init
   "pull-deps"  pull-deps
   "errors"     errors
   "changes"    changes
   "validate"   validate
   "get-symbol" get-symbol
   "get-tag"    get-tag
   "exit"       exit})


(defn command-not-found-err-message [cmd available-commands]
  (str "Command " cmd " not found. Available commands: " (clojure.string/join ", " available-commands)))


(defn repl [commands]
  (let [prompt "zen> "]
    (while true
      (try
        (print prompt)
        (flush)
        (let [line         (read-line)
              [cmd rest-s] (clojure.string/split line #" " 2)
              opts         (map pr-str (clojure.edn/read-string (str \[ rest-s \])))]
          (if-let [f (get commands cmd)]
            (do
              (f opts)
              (prn))
            (println (command-not-found-err-message cmd (keys commands)))))
        (catch Exception e
          (clojure.stacktrace/print-stack-trace e))))))


(defn -main [& [cmd & args]]
  (if (some? cmd)
    (if-let [cmd-fn (get commands cmd)]
      (apply cmd-fn (conj (vec args) {:pwd (zen.package/pwd :silent true)}))
      (println (command-not-found-err-message cmd (keys commands))))
    (repl commands)))


(comment

  (require 'clojure.java.shell)

  (do
    (clojure.java.shell/sh "rm" "-rf" "/tmp/zen")
    (clojure.java.shell/sh "mkdir" "-p" "/tmp/zen"))

  (clojure.java.shell/sh "make" "-B" "build")
  ;;
  )
