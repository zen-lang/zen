(ns zen.store
  (:require
   [clojure.edn]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.walk :as walk]
   [edamame.core]
   [zen.utils]
   [zen.v2-validation :as v2]))


#_"NOTE: this namespace extensively uses the following var names:
- zen-ns-map -- namespace map
- zen-ns-sym -- symbol name of a namespace
- sym        -- symbol from a namespace, may be qualified or not
- qsym       -- namespace qualified symbol
- resource   -- definition of a symbol from a namespace"


(def get-symbol zen.utils/get-symbol)


(def get-tag zen.utils/get-tag)


(def mk-symbol zen.utils/mk-symbol)


(defn update-types-recur [ctx tp-sym sym]
  (swap! ctx update-in [:tps tp-sym] (fn [x] (conj (or x #{}) sym)))
  (doseq [tp-sym' (:isa (get-symbol ctx tp-sym))]
    (update-types-recur ctx tp-sym' sym)))


(declare read-ns*)


(declare load-ns*)


(defn pretty-path [pth]
  (->> pth
       (mapv (fn [x] (if (keyword? x) (subs (str x) 1) (str x))))
       (str/join "->")))


(defn walk-resource [walk-fn resource]
  ;; disables sym expansion in zen/list (expr types)
  (if (list? resource)
    resource
    (walk/walk (partial walk-resource walk-fn) walk-fn resource)))


(defn validate-symbol
  "Try to resolve symbol, add error, add direct late binding reference."
  [ctx zen-ns-sym ns-key sym]
  (let [resolved-sym (if (namespace sym)
                       sym
                       (mk-symbol zen-ns-sym sym))
        res (get-symbol ctx resolved-sym)]

    (when (contains? (:zen/tags res) 'zen/binding)
      (swap! ctx update-in [:bindings resolved-sym]
             (fn [cfg]
               (update (or cfg res)
                       :diref
                       (fnil conj #{})
                       (mk-symbol zen-ns-sym ns-key)))))

    (when (nil? res)
      (swap! ctx update :errors
             (fnil conj [])
             (if (namespace sym)
               {:message (format "Could not resolve symbol '%s in %s/%s" sym zen-ns-sym ns-key)
                :type :unresolved-qualified-symbol
                :unresolved-symbol sym
                :ns zen-ns-sym}
               {:message (format "Could not resolve local symbol '%s in %s/%s" sym zen-ns-sym ns-key)
                :type :unresolved-local-symbol
                :unresolved-symbol sym
                :ns zen-ns-sym})))))


(defn zen-sym? [sym]
  (and (symbol? sym) (not (:zen/quote (meta sym)))))


(defn local-zen-sym? [sym]
  (and (zen-sym? sym) (not (namespace sym))))


(defn ensure-qualified [zen-ns-sym sym]
  (if (local-zen-sym? sym)
    (mk-symbol zen-ns-sym sym)
    sym))


(defn eval-resource
  "Walk resource, expand symbols and validate refs."
  [ctx zen-ns-sym sym resource]
  (let [walk-fn
        (fn [v]
          ;; do not validate zen/quote'd symbols
          (if (zen-sym? v)
            (let [qual-sym (ensure-qualified zen-ns-sym v)
                  maybe-resolved (zen.utils/resolve-aliased-sym ctx qual-sym)]

              #_"NOTE: because validate makes different messages for qual and unqual syms, we use v insead of qual-sysm here"
              (validate-symbol ctx zen-ns-sym sym (or maybe-resolved v))

              (or maybe-resolved qual-sym))
            v))]

    (-> (walk-resource walk-fn resource)
        (assoc :zen/name (mk-symbol zen-ns-sym sym)))))


(defn validate-resource [ctx res]
  (let [tags (get res :zen/tags)
        schemas (->> tags
                     (mapv (fn [tag]
                             (when-let [sch (get-symbol ctx tag)]
                               (when (contains? (:zen/tags sch) 'zen/schema)
                                 tag))))
                     (filter identity)
                     (into #{}))]
    (when-not (empty? schemas)
      (let [{errs :errors} (v2/validate ctx schemas res)]
        (when-not (empty? errs)
          (swap! ctx update :errors (fn [x] (into (or x []) (mapv #(assoc % :resource (:zen/name res)) errs)))))))))


(defn load-symbol
  "Resolve refs in resource, update indices."
  [ctx zen-ns-sym sym v]
  (let [qsym (mk-symbol zen-ns-sym sym)

        {:keys [zen/tags zen/bind] :as resource}
        (eval-resource ctx zen-ns-sym sym v)]

    (swap! ctx (fn [ctx]
                 ;; save resource
                 (as-> (assoc-in ctx [:symbols qsym] resource) ctx*
                   ;; upd tags registry
                   (reduce (fn [acc tag]
                             (update-in acc [:tags tag] (fnil conj #{}) qsym))
                           ctx*
                           tags)
                   ;; resolve late binding
                   (cond-> ctx*
                     bind (assoc-in [:bindings bind :backref] qsym)))))
    resource))


(defn load-alias [ctx alias-dest alias]
  (swap! ctx update :aliases zen.utils/disj-set-union-push alias-dest alias))


(defn symbol-definition? [[k v]]
  (and (symbol? k) (map? v)))


(defn symbol-alias? [[k v]]
  (and (symbol? k) (qualified-symbol? v)))


(defn pre-load-ns!
  "Loads symbols from namespace to ztx before processing."
  [ctx zen-ns-sym zen-ns-map]
  (let [symbols
        (into {}
              (keep (fn [[sym resource :as kv]]
                      (when (symbol-definition? kv)
                        [(mk-symbol zen-ns-sym sym)
                         (select-keys resource #{:zen/tags})]))) ;; TODO: maybe not only tags must be saved?
              zen-ns-map)]
    (swap! ctx update :symbols merge symbols)))


(defn add-zen-ns! [ctx zen-ns-sym zen-ns-map opts]
  (swap! ctx assoc-in [:ns zen-ns-sym] (assoc zen-ns-map :zen/file (:zen/file opts))))


(defn get-ns [zen-ns-map]
  (or (get zen-ns-map 'ns) (get zen-ns-map :ns)))


(defn get-ns-alias [zen-ns-map]
  (or (get zen-ns-map 'alias) (get zen-ns-map :alias)))


(defn get-ns-imports [zen-ns-map]
  (or (get zen-ns-map 'import)
      (get zen-ns-map :import)))


(defn ns-already-loaded? [ctx zen-ns-sym]
  (contains? (:ns @ctx) zen-ns-sym))


(defn get-loaded-ns [ctx zen-ns-sym]
  (get-in @ctx [:ns zen-ns-sym]))


(defn ns-in-memory-store? [ctx zen-ns-sym]
  (contains? (:memory-store @ctx) zen-ns-sym))


(defn get-from-memry-store [ctx zen-ns-sym]
  (get-in @ctx [:memory-store zen-ns-sym]))


(defn import-nss! [ctx zen-ns-sym imports opts]
  (doall
   (for [imp imports]
     (cond
       (ns-already-loaded? ctx imp) nil

       (ns-in-memory-store? ctx imp)
       (load-ns* ctx (get-from-memry-store ctx imp) opts)

       :else (read-ns* ctx imp (assoc opts :ns zen-ns-sym))))))


(defn process-ns-alias! [ctx this-ns-sym aliased-ns this-ns-map]
  (when (symbol? aliased-ns)
    (doseq [[aliased-sym _ :as kv] (get-loaded-ns ctx aliased-ns)]
      (when (symbol-definition? kv)
        (let [shadowed-here? (contains? this-ns-map aliased-sym)]
          (when (not shadowed-here?)
            (load-alias ctx
                        (mk-symbol aliased-ns aliased-sym)
                        (mk-symbol this-ns-sym aliased-sym))))))))


(defn load-ns-content! [ctx zen-ns-sym zen-ns-map opts]
  #_"TODO do group-by instead of sort, validate only symbol-definitions, return validated resources"
  (let [ns-content (apply dissoc zen-ns-map ['ns 'import 'alias :ns :import :alias])]
    (->> ns-content
         (sort-by (juxt symbol-definition? symbol-alias?)) #_"NOTE: load aliases first, symbols after"
         (mapv (fn [[k v :as kv]]
                 (cond (symbol-definition? kv) (load-symbol ctx zen-ns-sym k (merge v opts))
                       (symbol-alias? kv)      (load-alias ctx v (mk-symbol zen-ns-sym k))
                       :else                   nil))))))


(defn load-ns* [ctx zen-ns-map & [opts]]
  (let [zen-ns-sym (get-ns zen-ns-map)
        aliased-ns (get-ns-alias zen-ns-map)

        _ (add-zen-ns! ctx zen-ns-sym zen-ns-map opts)
        _ (pre-load-ns! ctx zen-ns-sym zen-ns-map)

        imports (cond->> (get-ns-imports zen-ns-map)
                  (some? aliased-ns) (cons aliased-ns))

        imports-validate-queue (import-nss! ctx zen-ns-sym imports opts)

        _ (process-ns-alias! ctx zen-ns-sym aliased-ns zen-ns-map)

        this-ns-validate-queue (load-ns-content! ctx zen-ns-sym zen-ns-map opts)]

    (apply concat
           this-ns-validate-queue
           imports-validate-queue)))


(defn validate-queue-resources! [ctx queue]
  (mapv (fn [res] (validate-resource ctx res))
        queue))


(defn load-ns [ctx zen-ns-map & [opts]]
  (let [validate-queue (load-ns* ctx zen-ns-map opts)]
    [:resources-loaded (count (validate-queue-resources! ctx validate-queue))]))


(defn load-ns! [ctx zen-ns-map]
  (assert (map? zen-ns-map) "Expected map")
  (load-ns ctx zen-ns-map)
  (when-let [errs (:errors @ctx)]
    (throw (Exception. (str/join "\n" errs)))))


(defn expand-node-modules [path]
  (let [modules (io/file path "node_modules")]
    (when (and (.exists modules) (.isDirectory modules))
      (->> (.listFiles modules)
           (mapcat (fn [^java.io.File x]
                     (if (and (.isDirectory x) (str/starts-with? (.getName x) "@"))
                       (.listFiles x)
                       [x])))
           (filter (fn [^java.io.File x] (.isDirectory x)))))))


(defn mk-full-path [zen-path file-path]
  (io/file zen-path file-path))


(defn get-file [zen-path file-path]
  (let [^java.io.File file (mk-full-path zen-path file-path)]
    (when (.exists file)
      file)))


(defn find-path
  "Returns a zen path containing pth."
  [paths pth]
  (loop [[p & ps] paths]
    (when p
      (if (some? (get-file p pth))
        (.getPath (io/file p))
        (recur (concat (expand-node-modules p) ps))))))


(defn find-in-paths
  "Returns file found in zen paths."
  [paths pth]
  (some-> (find-path paths pth)
          (get-file pth)))


(defn expand-zen-packages [modules]
  (when (and (.exists modules) (.isDirectory modules))
    (->> (.listFiles modules)
         (map (fn [x] (io/file x "zrc")))
         (filter #(.isDirectory %)))))

(defn expand-nippy-indexes [modules]
  (when (and (.exists modules) (.isDirectory modules))
    (->> (.listFiles modules) 
         (filter (fn [x] (.exists (io/file x "index.nippy"))))
         (map (fn [x] (io/file x "index.nippy"))))))

(defn expand-package-path [package-path]
  (cons (io/file package-path "zrc") (expand-zen-packages (io/file package-path "zen-packages"))))

(defn expand-nippy-path [package-path]
  (expand-nippy-indexes (io/file package-path "zen-packages")))


(defn unzip-cache-dir
  []
  (io/file "/tmp/zen-unzip"))


(defn unzip-to-cache-dir [zip-path]
  (let [path-hash (zen.utils/string->md5 zip-path)
        unzip-dest (io/file (unzip-cache-dir) path-hash)]
    (if (.exists unzip-dest)
      (io/file unzip-dest "zrc")
      (io/file (zen.utils/unzip! zip-path unzip-dest) "zrc"))))


(defn find-file&path [ctx pth]
  (or (when-let [resource-file (io/resource pth)]
        {:file resource-file})
      (when-let [zen-path (find-path (concat (mapcat expand-package-path (:package-paths @ctx))
                                             (:paths @ctx)
                                             (map unzip-to-cache-dir (:zip-paths @ctx)))
                                     pth)]
        {:file     (get-file zen-path pth)
         :zen-path zen-path})))


;; TODO: cache find file
(defn find-file [ctx pth]
  (:file (find-file&path ctx pth)))


(defn get-env [env env-name]
  (let [[env-name default] (if (vector? env-name)
                             env-name
                             [env-name nil])]
    (or (get env (keyword env-name))
        (System/getenv (str env-name))
        default)))


(defn env-string [env env-name]
  (when-let [v (get-env env env-name)]
    v))


(defn env-integer [env env-name]
  (when-let [v (get-env env env-name)]
    (if (number? v)
      v
      (Integer/parseInt v))))


(defn env-symbol [env env-name]
  (when-let [v (get-env env env-name)]
    (if (symbol? v)
      v
      (symbol v))))


(defn env-keyword [env env-name]
  (when-let [v (get-env env env-name)]
    (if (keyword? v)
      v
      (keyword v))))


(defn env-number [env env-name]
  (when-let [v (get-env env env-name)]
    (if (number? v)
      v
      (Double/parseDouble v))))


(defn env-boolean [env env-name]
  (let [v (get-env env env-name)]
    (when (not (nil? v))
      (if (boolean? v)
        v
        (cond
          (= "true" v) true
          (= "false" v) false
          :else (throw (ex-info (str "Expected true or false in " env-name ", got " v) {})))))))


(defn zen-quote [d]
  (with-meta d {:zen/quote true}))


(defn read-ns* [ctx nm & [opts]]
  (let [pth (str (str/replace (str nm) #"\." "/") ".edn")]
    (if-let [{:keys [file zen-path]} (find-file&path ctx pth)]
      (try
        (let [content (slurp file)
              env (:env @ctx)
              zen-ns-map (first (edamame.core/parse-string-all content
                                                    {:readers {'env         (fn [v] (env-string  env v))
                                                               'env-string  (fn [v] (env-string  env v))
                                                               'env-integer (fn [v] (env-integer env v))
                                                               'env-symbol  (fn [v] (env-symbol  env v))
                                                               'env-number  (fn [v] (env-number  env v))
                                                               'env-keyword (fn [v] (env-keyword  env v))
                                                               'env-boolean (fn [v] (env-boolean env v))
                                                               'zen/quote   zen-quote}}))
              zen-ns-sym (get-ns zen-ns-map)]
          (if (= nm zen-ns-sym)
            (load-ns* ctx zen-ns-map (cond-> {:zen/file (.getPath file)}
                                       zen-path (assoc :zen/zen-path zen-path)))
            (do (println :file-doesnt-match-namespace (.getPath file) nm zen-ns-sym)
                (swap! ctx update :errors
                       (fnil conj [])
                       {:message (str "Filename should match contained namespace. Expected "
                                      nm " got " zen-ns-sym)
                        :file (.getPath file)
                        :ns nm
                        :got-ns zen-ns-sym})
                nil)))
        (catch Exception e
          #_(println :error-while-reading (.getPath file) e)
          (swap! ctx update :errors
                 (fnil conj [])
                 {:message (.getMessage e)
                  :file (.getPath file)
                  :ns nm})
          nil))
      (do (swap! ctx update :errors
                 (fnil conj [])
                 {:message (format "No file for ns '%s" nm)
                  :missing-ns nm
                  :ns (or (:ns opts) nm)})
          nil))))


(defn read-ns [ctx zen-ns-sym & [opts]]
  (if-let [validate-queue (read-ns* ctx zen-ns-sym opts)]
    (do
      (when (not (:disable-schema-validation opts))
        (validate-queue-resources! ctx validate-queue))
      :zen/loaded)
    :zen/load-failed))


(defn read-ns! [ctx zen-ns-sym]
  (assert (symbol? zen-ns-sym) "Expected symbol")
  (read-ns ctx zen-ns-sym)
  (when-let [errs (:errors @ctx)]
    (throw (Exception. (str/join "\n" errs)))))


(defn new-context [& [opts]]
  (let [ctx (atom (or opts {}))]
    (read-ns ctx 'zen)
    ctx))


(defn instance-of? [_tp res]
  (let [tps (get res 'types)]
    (or (and (set? tps) (contains? tps 'primitive))
        (= tps 'primitive))))
