(ns zen.cli
  (:gen-class)
  (:require [zen.package]
            [zen.changes]
            [zen.core]
            [zen.cli.output]
            [clojure.pprint]
            [clojure.java.io :as io]
            [clojure.string]
            [clojure.edn]
            [clojure.stacktrace]
            [clojure.java.shell]
            [zen.types-generation]
            [clojure.string :as str]))

(defn format-command-usage
  [schema-nth]
  (->> (sort-by first schema-nth)
       (map last)
       (mapv #(if (contains? % :enum)
                (clojure.string/join "|" (map :value (:enum %)))
                (:zen/desc %)))))

(defn help-command
  [schema args]
  (cond
    (= (get-in schema [:args :type]) 'zen/case)
    (mapv
     (fn [case-schema]
       {:description (:zen/desc case-schema)
        :path   (into ["zen"] args)
        :params (->> (get-in case-schema [:then :nth])
                     (format-command-usage))})
     (get-in schema [:args :case]))
    (= (get-in schema [:args :type]) 'zen/vector)
    [{:description (:zen/desc schema)
      :path   (into ["zen"] args)
      :params (->> (get-in schema [:args :nth])
                   (format-command-usage))}]))

(defn help
  [ztx command-symbol & [args]]
  (let [schema (zen.core/get-symbol ztx command-symbol)]
    (cond
      (contains? schema :commands)
      {:format  :command
       ::result {:description (:zen/desc schema)
                 :examples    (:examples schema)
                 :usage       (vec
                               (mapcat
                                (fn [[command-name command-schema]]
                                  (let [command-definition (zen.core/get-symbol ztx (or (:command command-schema) (:config command-schema)))]
                                    (help-command command-definition (conj args (name command-name)))))
                                (sort-by first (:commands schema))))}}
      (contains? (:zen/tags schema) 'zen.cli/command)
      {:format  :command
       ::result {:description (:zen/desc schema)
                 :examples    (:examples schema)
                 :usage       (help-command schema args)}})))

(defn str->edn [x]
  (clojure.edn/read-string (str x)))

(defn split-args-by-space [args-str]
  (map pr-str (clojure.edn/read-string (str \[ args-str \]))))

(defn apply-with-opts [f args opts]
  (apply f (conj (vec args) opts)))

(defn get-pwd [& [{:keys [pwd] :as opts}]]
  (or (some-> pwd (clojure.string/replace #"/+$" ""))
      (System/getProperty "user.dir")))

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
                      (map (fn [^java.io.File f] (relativize (.getAbsolutePath f))))
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
  ([opts] (init nil nil opts))

  ([package-name opts] (init nil package-name opts))

  ([_ztx package-name opts]
   (if (zen.package/zen-init! (get-pwd opts) (when package-name
                                               {:package-name (str->edn package-name)}))
     {::status :ok ::code :initted-new    :format :message ::result {:message "Project was successfully initted"}}
     {::status :ok ::code :already-exists :format :message ::result {:message "The current directory is not empty"}})))


(defn pull-deps
  ([opts] (pull-deps nil opts))

  ([_ztx opts]
   (if-let [initted-deps (zen.package/zen-init-deps! (get-pwd opts))]
     {::status :ok :format :message ::result {:message "Dependencies have been successfully updated"
                                              :status  :ok
                                              :code :pulled :deps initted-deps}}
     {::status :ok :format :message ::result {:status :ok :message "No dependencies found" :code :nothing-to-pull}})))


(defn errors
  ([opts] (errors (load-ztx opts) opts))

  ([ztx opts]
   (load-used-namespaces ztx opts)
   {:format  :error
    ::status :ok
    ::result (seq (map #(assoc % ::file
                               (some->
                                (or (get-in @ztx [:ns (:ns %) :zen/file])
                                    (:file %)
                                    (and (:resource %)
                                         (:zen/file (zen.core/get-symbol ztx (:resource %)))))
                                (subs (inc (count (get-pwd opts))))))
                       (zen.core/errors ztx :order :as-is)))}))

(defn get-sdk [args]
  (zen.types-generation/get-sdk (get-pwd) (last (clojure.string/split (str (first args)) #"="))))

(defn get-ts-types [args]
  (zen.types-generation/get-ts-types (get-pwd) (last (clojure.string/split (str (first args)) #"="))))

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

(defn cmd-unsafe [commands cmd-name args & [opts]]
  (if-let [cmd-fn (get commands cmd-name)]
    (apply-with-opts cmd-fn args opts)
    (command-not-found-err-message cmd-name (keys commands))))


(defn cmd [& args]
  (exception->error-result
   (apply cmd-unsafe args)))


(defn build
  ([opts] (build "target" opts))
  ([path opts] (build path "zen-package" opts))
  ([path package-name opts] (build nil path package-name opts))
  ([_ztx path package-name opts] #_"NOTE: currently this fn doesn't need ztx"
   (zen.package/zen-build! (get-pwd opts) {:build-path path :package-name package-name})
                                 {::status :ok :format :message ::result {:message "Project was successfully builded" :code :builded :status :ok}}))

(defn command-dispatch [command-name _command-args _opts]
  command-name)

(defmulti command #'command-dispatch :default ::not-found)

(defmethod command 'zen.cli/init [_ [package-name] opts]
  (apply init (remove nil? [package-name opts])))

(defmethod command 'zen.cli/pull-deps [_ _ opts]
  (pull-deps opts))

(defmethod command 'zen.cli/get-sdk [_ args _]
  (get-sdk args))

(defmethod command 'zen.cli/get-ts-types [_ args _]
  (get-ts-types args))

(defmethod command 'zen.cli/build [_ [path package-name] opts]
  (let [path-str (str path)]
    (if-not package-name
      (build path-str opts)
      (build path-str package-name opts))))

(defmethod command 'zen.cli/errors [_ _args opts]
  (errors opts))

(def templates
  {"aidbox"           {:url "https://github.com/Aidbox/fhir-r4-configuration-project"}
   "audit-log-viewer" {:url "https://github.com/Aidbox/audit-log-viewer"}})

(defmethod command 'zen.cli/template [_ [template-name] opts]
  (let [root-dir (get-pwd opts)
        ztx      (load-ztx opts)
        _        (zen.core/read-ns ztx 'zen.cli)
        template (zen.core/get-symbol ztx template-name)]
    (if template
      (when (zen.package/init-template root-dir (:url template))
        {:format :message ::status :ok ::result {:message (format "Template %s was successfully created" template-name)}})
      {:format :message ::status :ok ::result {:message (format "Template %s was not found" template-name)}})))

(defmethod command 'zen.cli/changes [_ _ opts]
  (changes opts))

(defmethod command 'zen.cli/validate [_ [symbols-str data-str] opts]
  (validate symbols-str data-str opts))

(defmethod command 'zen.cli/get-symbol [_ [sym-str] opts]
  (get-symbol sym-str opts))

(defmethod command 'zen.cli/get-tag [_ [tag-str] opts]
  (get-tag tag-str opts))

(defmethod command 'zen.cli/exit [_ _ opts]
  (exit opts))

(defmethod command 'zen.cli/install
  [_ [dependency-id] opts]
  (let [root-dir (get-pwd opts)
        zen-dep  (zen.package/format-dependency (str dependency-id))]
    (zen.package/add-package (get-pwd opts) zen-dep)
    (zen.package/zen-init-deps! root-dir)
    {:format :message ::status :ok :code :installed ::result {:message (format "Dependency %s was successfully installed" dependency-id)}}))

(defmethod command ::not-found [command-name _command-args _opts] #_"TODO: return help"
  {::status :error
   ::code ::implementation-missing
   ::result {:message (str "Command '" command-name " implementation is missing")}})


(defn coerce-args-style-dispatch [command-args-def _command-args]
  (:args-style command-args-def :positional))


(defmulti coerce-args-style #'coerce-args-style-dispatch)


(defmethod coerce-args-style :named [_command-args-def command-args]
  (clojure.edn/read-string (str "{" (clojure.string/join " " command-args) "}")))


(defmethod coerce-args-style :positional [_command-args-def command-args]
  (clojure.edn/read-string (str "[" (clojure.string/join " " command-args) "]")))

(defn handle-command
  [ztx command-sym command-args & [opts]]
  (if-let [command-def (zen.core/get-symbol ztx command-sym)]
    (let [coerced-args      (coerce-args-style command-def command-args)
          args-validate-res (zen.v2-validation/validate-schema ztx
                                                               (:args command-def)
                                                               coerced-args
                                                               {:sch-symbol command-sym})]
      (if (empty? (:errors args-validate-res))
        (let [command-res (try (command command-sym coerced-args (or opts {}))
                               (catch Exception e
                                 #::{:result {:exception e}
                                     :status :error
                                     :code   ::exception}))]
          (if (::status command-res)
            command-res
            #::{:result command-res
                :status :ok}))
        {:format  :error
         ::status :error
         ::code   ::invalid-args
         ::result (do (println "Use --help for more information")
                      (map #(assoc % :type "invalid arguments")
                           (:errors args-validate-res)))}))
    {:format  :message
     ::status :error
     ::code   ::undefined-command
     ::result {:message "undefined command"}}))

(defn extract-commands-params [args]
  (let [[[command-name & command-args] subcommands]
        (->> (group-by #(clojure.string/starts-with? % "--") args)
             (merge {false [] true []})
             (sort-by first)
             (map last))]
    {:command-name command-name
     :command-args command-args
     :subcommands  subcommands}))

(defn cli-exec [ztx config-sym args & [opts]]
  (let [config (zen.core/get-symbol ztx config-sym)
        commands (:commands config)

        {:keys [command-name command-args subcommands]} (extract-commands-params args)

        command-entry (get commands (keyword command-name))

        command-sym        (:command command-entry)
        nested-config-sym  (:config command-entry)]

    (cond
      (some #(= "--help" %) subcommands)
      (help ztx (or command-sym config-sym) (butlast args))

      (some? nested-config-sym)
      (cli-exec ztx nested-config-sym command-args opts)

      (some? command-sym)
      (handle-command ztx command-sym command-args opts)

      :else
      {:format  :message
       ::status :error
       ::code ::unknown-command
       ::result {:message "unknown command"}})))

(defn repl [ztx config-sym & [opts]]
  (let [prompt-fn (get-prompt-fn opts)
        read-fn   (get-read-fn opts)
        return-fn (get-return-fn opts)
        config    (zen.core/get-symbol ztx config-sym)
        commands  (:commands config)

        opts (update opts :stop-repl-atom #(or % (atom false)))]
    (while (not @(:stop-repl-atom opts))
      (return-fn
       (exception->error-result
        (prompt-fn)
        (let [line (read-fn)
              args (split-args-by-space line)]
          (cli-exec ztx config-sym args opts)))))))

(defn cli-main* [ztx config-sym [cmd-name :as args] opts]
  (if (seq cmd-name)
    (zen.cli.output/return
     (update (cli-exec ztx config-sym args opts)
             :format #(or (:format opts) (zen.cli.output/get-format args) %)))
    (repl ztx config-sym opts)))

(defn cli [ztx config-sym args & [opts]]
  (let [main-ns (symbol (namespace config-sym))]
    (if (= :zen/loaded (zen.core/read-ns ztx main-ns))
      (cli-main* ztx config-sym args opts)
      {::code   ::load-failed
       ::status :error
       ::result {:message "Couldn't load main CLI namespace"
                 :ns      main-ns
                 :errors  (zen.core/errors ztx)}})))


(defn cli-main [args & [opts]]
  (cli (zen.core/new-context) 'zen.cli/zen-config args opts))

(defn -main [& args]
  (cli-main args)
  (System/exit 0))
