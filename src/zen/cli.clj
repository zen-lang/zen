(ns zen.cli
  (:gen-class)
  (:require [cli-matic.core]
            [cli-matic.utils]
            [zen.package]
            [zen.changes]
            [zen.core]
            [clojure.pprint]
            [clojure.java.io]
            [clojure.string]
            [clojure.edn]
            [clojure.stacktrace]
            [clojure.java.shell]))


(defn load-ztx []
  (let [pwd (zen.package/pwd :silent true)
        ztx (zen.core/new-context {:package-paths [pwd]})]
    ztx))


(defn collect-all-project-namespaces []
  (let [pwd (zen.package/pwd :silent true)
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
  ([ztx]
   (load-used-namespaces ztx (collect-all-project-namespaces)))

  ([ztx sym & symbols]
   (run! #(let [sym (symbol %)
                zen-ns (or (some-> sym namespace symbol)
                           sym)]
            (zen.core/read-ns ztx zen-ns))
         (flatten (vector (cons sym symbols))))
   ztx))


(defn init
  ([args] (init nil args))

  ([_ztx {[name] :_arguments}]
   (let [to (zen.package/pwd)]
     (zen.package/make-template! to name)
     (zen.package/zen-init! to))))


(defn pull-deps
  ([args] (pull-deps nil args))

  ([_ztx _args]
   (let [to (zen.package/pwd)]
     (zen.package/zen-init-deps! to))))


(defn errors
  ([args] (errors (load-ztx) args))

  ([ztx _args]
   (load-used-namespaces ztx)
   (clojure.pprint/pprint (zen.core/errors ztx))))


(defn validate
  ([args] (validate (load-ztx) args))

  ([ztx {[symbols-str data-str] :_arguments}]
   (let [symbols (clojure.edn/read-string symbols-str)
         data (clojure.edn/read-string data-str)]
     (load-used-namespaces ztx symbols)
     (clojure.pprint/pprint (zen.core/validate ztx symbols data)))))


(defn get-symbol
  ([args] (get-symbol (load-ztx) args))

  ([ztx {[symbol-str] :_arguments}]
   (let [sym (clojure.edn/read-string symbol-str)]
     (load-used-namespaces ztx sym)
     (clojure.pprint/pprint (zen.core/get-symbol ztx sym)))))


(defn get-tag
  ([args] (get-tag (load-ztx) args))

  ([ztx {[tag-str] :_arguments}]
   (let [sym (clojure.edn/read-string tag-str)]
     (load-used-namespaces ztx sym)
     (clojure.pprint/pprint (zen.core/get-tag ztx sym)))))


(defn exit
  ([args] (exit nil args))

  ([_ _] (System/exit 0)))


(defn changes
  ([args] (changes (load-ztx) args))

  ([new-ztx _args]
   (let [new-ztx (load-used-namespaces new-ztx)
         _stash! (clojure.java.shell/sh "git" "stash")
         old-ztx (load-used-namespaces (load-ztx))
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
                 {:description "Recursively pulls all deps specified in package.edn to zen-modules/"
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
