(ns zen.cli
  (:gen-class)
  (:require [cli-matic.core]
            [cli-matic.utils]
            [zen.package]
            [zen.changes]
            [zen.core]
            [clojure.pprint]
            [clojure.java.io :as io]
            [clojure.string]
            [clojure.edn]
            [clojure.stacktrace]
            [clojure.java.shell]))


(defn get-pwd [{:keys [pwd] :as _args}]
  (or (some-> pwd (clojure.string/replace #"/+$" ""))
      (zen.package/pwd :silent true)))


(defn load-ztx [args]
  (zen.core/new-context {:package-paths [(get-pwd args)]}))


(defn collect-all-project-namespaces [args]
  (let [pwd (get-pwd args)
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
  ([ztx args]
   (load-used-namespaces ztx (collect-all-project-namespaces args) args))

  ([ztx symbols _args]
   (doseq [s symbols]
     (let [sym (symbol s)
           zen-ns (or (some-> sym namespace symbol)
                      sym)]
       (zen.core/read-ns ztx zen-ns)))
   ztx))


(defn init
  ([args] (init nil args))

  ([_ztx {:keys [name] :as args}]
   (let [dest (get-pwd args)
         not-empty-zen-dir? (seq (filter #(.isFile %)
                                         (file-seq (io/file dest))))]
     (if not-empty-zen-dir?
       {:status :ok, :code :already-exists}
       (do (zen.package/zen-init! dest {:package-name name})
           {:status :ok, :code :initted-new})))))


(defn pull-deps
  ([args] (pull-deps nil args))

  ([_ztx args] (zen.package/zen-init-deps! (get-pwd args))))


(defn errors
  ([args] (errors (load-ztx args) args))

  ([ztx _args]
   (load-used-namespaces ztx)
   (zen.core/errors ztx)))


(defn validate
  ([args] (validate (load-ztx args) args))

  ([ztx {[symbols-str data-str] :_arguments}]
   (let [symbols (clojure.edn/read-string symbols-str)
         data (clojure.edn/read-string data-str)]
     (load-used-namespaces ztx symbols)
     (clojure.pprint/pprint (zen.core/validate ztx symbols data)))))


(defn get-symbol
  ([sym args]
   (get-symbol (load-ztx args) sym args))

  ([ztx sym {:keys [pwd] :as args}]
   (let [_ (zen.core/read-ns ztx sym)]
     (load-used-namespaces ztx [sym] args)
     (zen.core/get-symbol ztx sym))))


(defn get-tag
  ([tag args] (get-tag (load-ztx args) tag args))

  ([ztx tag args]
   (load-used-namespaces ztx args)
   (zen.core/get-tag ztx (symbol tag))))


(defn exit
  ([args] (exit nil args))

  ([_ _] (System/exit 0)))


(defn changes
  ([args] (changes (load-ztx args) args))

  ([new-ztx args]
   (let [new-ztx (load-used-namespaces new-ztx)
         _stash! (clojure.java.shell/sh "git" "stash")
         old-ztx (load-used-namespaces (load-ztx args))
         _pop!   (clojure.java.shell/sh "git" "stash" "pop")]
     (clojure.pprint/pprint (zen.changes/check-changes old-ztx new-ztx)))))


(def cfg
  {:command     "zen"
   :description "Zen-lang cli. Provides zen validation, package managment and build tools"
   :version     "0.0.1"
   :opts        []
   :subcommands [{:description "Builds zen project from provided IG"
                  :command     "init"
                  :runs        init}
                 {:description "Recursively pulls all deps specified in package.edn to zen-packages/"
                  :command     "pull-deps"
                  :runs        pull-deps}
                 {:description "Validates zen, returns errors"
                  :command     "errors"
                  :runs        errors}
                 {:description "Compare new changes with committed"
                  :command     "changes"
                  :runs        changes}
                 {:description "Validates data with specified symbol. Use: `zen validate #{symbol} data`"
                  :command     "validate"
                  :runs        validate}
                 {:description "Gets symbol definition"
                  :command     "get-symbol"
                  :runs        get-symbol}
                 {:description "Gets symbols by tag"
                  :command     "get-tag"
                  :runs        get-tag}
                 {:description "Exits zen REPL"
                  :command     "exit"
                  :runs        exit}]})


(defn repl [cfg]
  (let [commands (into {}
                       (map (juxt :command :runs))
                       (:subcommands cfg))
        prompt "zen> "]

    (while true
      (try
        (print prompt)
        (flush)
        (let [line         (read-line)
              [cmd rest-s] (clojure.string/split line #" " 2)
              args         (map pr-str (clojure.edn/read-string (str \[ rest-s \])))]
          (if-let [f (get commands cmd)]
            (do
              (f {:_arguments args})
              (prn))
            (println "Command not found. Available commands: " (clojure.string/join ", " (keys commands)))))
        (catch Exception e
          (clojure.stacktrace/print-stack-trace e))))))


(defn -main
  [& args]
  (if (seq args)
    (cli-matic.core/run-cmd args cfg)
    (repl cfg)))


(comment

  (require 'clojure.java.shell)

  (do
    (clojure.java.shell/sh "rm" "-rf" "/tmp/zen")
    (clojure.java.shell/sh "mkdir" "-p" "/tmp/zen"))

  (clojure.java.shell/sh "make" "-B" "build")
  ;;
  )
