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


(defn split-args-by-space [args-str]
  (map pr-str (clojure.edn/read-string (str \[ args-str \]))))


(defn apply-with-opts [f args opts]
  (apply f (conj (vec args) opts)))


(defn get-pwd [& [{:keys [pwd] :as opts}]]
  (or (some-> pwd (clojure.string/replace #"/+$" ""))
      (zen.package/pwd :silent true)))


(defn get-return-fn [& [opts]]
  (or (:return-fn opts) clojure.pprint/pprint))


(defn get-read-fn [& [opts]]
  (or (:read-fn opts) read-line))


(defn get-prompt-fn [& [opts]]
  (or (:prompt-fn opts)
      #(do (print "zen> ")
           (flush))))


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
  ([package-name opts] (init nil package-name opts))

  ([_ztx package-name opts]
   (if (zen.package/zen-init! (get-pwd opts) {:package-name (str->edn package-name)})
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

  ([_ opts]
   (when-let [stop-atom (:stop-repl-atom opts)]
     (reset! stop-atom true))
   {:status :ok, :code :exit, :message "Bye!"}))


(defn changes
  ([opts] (changes (load-ztx opts) opts))

  ([new-ztx opts]
   (let [pwd     (get-pwd opts)
         new-ztx (load-used-namespaces new-ztx opts)
         _stash! (clojure.java.shell/sh "git" "stash" :dir pwd) #_"NOTE: should this logic be moved to zen.package?"
         old-ztx (load-used-namespaces (load-ztx opts) opts)
         _pop!   (clojure.java.shell/sh "git" "stash" "pop" :dir pwd)]
     (zen.changes/check-changes old-ztx new-ztx))))


(defn command-not-found-err-message [cmd-name available-commands]
  {:status :error
   :code :command-not-found
   :message (str "Command " cmd-name " not found. Available commands: " (clojure.string/join ", " available-commands))})


(defmacro exception->error-result [& body]
  `(try
     ~@body
     (catch Exception e#
       {:status    :error
        :code      :exception
        :message   (.getMessage e#)
        :exception (Throwable->map e#)})))


(defn repl [commands & [opts]]
  (let [prompt-fn (get-prompt-fn opts)
        read-fn   (get-read-fn opts)
        return-fn (get-return-fn opts)

        opts (update opts :stop-repl-atom #(or % (atom false)))]
    (while (not @(:stop-repl-atom opts))
      (return-fn
        (exception->error-result
          (prompt-fn)
          (let [line              (read-fn)
                [cmd-name rest-s] (clojure.string/split line #" " 2)
                args              (split-args-by-space rest-s)]
            (if-let [cmd-fn (get commands cmd-name)]
              (apply-with-opts cmd-fn args opts)
              (command-not-found-err-message cmd-name (keys commands)))))))))


(defn cmd-unsafe [commands cmd-name args & [opts]]
  (if-let [cmd-fn (get commands cmd-name)]
    (apply-with-opts cmd-fn args opts)
    (command-not-found-err-message cmd-name (keys commands))))


(defn cmd [& args]
  (exception->error-result
    (apply cmd-unsafe args)))


(def commands
  {"init"       init
   "pull-deps"  pull-deps
   "errors"     errors
   "changes"    changes
   "validate"   validate
   "get-symbol" get-symbol
   "get-tag"    get-tag
   "exit"       exit})


(defn -main [& [cmd-name & args]]
  (if (some? cmd-name)
    ((get-return-fn) (cmd commands cmd-name args))
    (repl commands))
  (System/exit 0))
