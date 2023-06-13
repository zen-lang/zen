(ns zen.store
  (:require [zen.v2-validation :as v2]
            [zen.utils]
            [clojure.edn]
            [clojure.java.io :as io]
            [clojure.walk :as walk]
            [edamame.core]
            [clojure.string :as str]))

(def get-symbol zen.utils/get-symbol)

(def get-tag zen.utils/get-tag)

(def mk-symbol zen.utils/mk-symbol)

(defn update-types-recur [ctx tp-sym sym]
  (swap! ctx update-in [:tps tp-sym] (fn [x] (conj (or x #{}) sym)))
  (doseq [tp-sym' (:isa (get-symbol ctx tp-sym))]
    (update-types-recur ctx tp-sym' sym)))

(declare read-ns)
(declare load-ns)

(defn pretty-path [pth]
  (->> pth
       (mapv (fn [x] (if (keyword? x) (subs (str x) 1) (str x))))
       (str/join "->" )))

(defn walk-resource [walk-fn resource]
  ;; disables sym expansion in zen/list (expr types)
  (if (list? resource)
    resource
    (walk/walk (partial walk-resource walk-fn) walk-fn resource)))

(defn validate-symbol
  "try to resolve symbol, add error, add direct late binding reference"
  [ctx zen-ns ns-key sym]
  (let [resolved-sym (if (namespace sym)
                       sym
                       (mk-symbol zen-ns sym))
        res (get-symbol ctx resolved-sym)]

    (when (contains? (:zen/tags res) 'zen/binding)
      (swap! ctx update-in [:bindings resolved-sym]
             (fn [cfg]
               (update (or cfg res)
                       :diref
                       (fnil conj #{})
                       (mk-symbol zen-ns ns-key)))))

    (when (nil? res)
      (swap! ctx update :errors
             (fnil conj [])
             (if (namespace sym)
               {:message (format "Could not resolve symbol '%s in %s/%s" sym zen-ns ns-key)
                :type :unresolved-qualified-symbol
                :unresolved-symbol sym
                :ns zen-ns}
               {:message (format "Could not resolve local symbol '%s in %s/%s" sym zen-ns ns-key)
                :type :unresolved-local-symbol
                :unresolved-symbol sym
                :ns zen-ns})))))

(defn zen-sym? [sym]
  (and (symbol? sym) (not (:zen/quote (meta sym)))))

(defn local-zen-sym? [sym]
  (and (zen-sym? sym) (not (namespace sym))))

(defn ensure-qualified [zen-ns sym]
  (if (local-zen-sym? sym)
    (mk-symbol zen-ns sym)
    sym))

(defn eval-resource
  "walk resource, expand symbols and validate refs"
  [ctx zen-ns ns-sym resource]
  (let [walk-fn
        (fn [v]
          ;; do not validate zen/quote'd symbols
          (if (zen-sym? v)
            (let [qual-sym (ensure-qualified zen-ns v)
                  maybe-resolved (zen.utils/resolve-aliased-sym ctx qual-sym)]

              #_"NOTE: because validate makes different messages for qual and unqual syms, we use v insead of qual-sysm here"
              (validate-symbol ctx zen-ns ns-sym (or maybe-resolved v))

              (or maybe-resolved qual-sym))
            v))]

    (-> (walk-resource walk-fn resource)
        (assoc :zen/name (mk-symbol zen-ns ns-sym)))))

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
  "resolve refs in resource, update indices"
  [ctx zen-ns ns-sym v]
  (let [sym (mk-symbol zen-ns ns-sym)

        {:keys [zen/tags zen/bind] :as resource}
        (eval-resource ctx zen-ns ns-sym v)]

    (swap! ctx (fn [ctx]
                 ;; save resource
                 (as-> (assoc-in ctx [:symbols sym] resource) ctx*
                   ;; upd tags registry
                   (reduce (fn [acc tag]
                             (update-in acc [:tags tag] (fnil conj #{}) sym))
                           ctx*
                           tags)
                   ;; resolve late binding
                   (cond-> ctx*
                     bind (assoc-in [:bindings bind :backref] sym)))))
    resource))

(defn load-alias [ctx alias-dest alias]
  (swap! ctx update :aliases zen.utils/disj-set-union-push alias-dest alias))

(defn symbol-definition? [[k v]]
  (and (symbol? k) (map? v)))

(defn symbol-alias? [[k v]]
  (and (symbol? k) (qualified-symbol? v)))

(defn pre-load-ns!
  "loads symbols from namespace to ztx before processing"
  [ctx zen-ns ns-map]
  (let [symbols
        (into {}
              (keep (fn [[sym resource :as kv]]
                      (when (symbol-definition? kv)
                        [(mk-symbol zen-ns sym)
                         (select-keys resource #{:zen/tags})]))) ;; TODO: maybe not only tags must be saved?
              ns-map)]
    (swap! ctx update :symbols merge symbols)))

(defn add-zen-ns! [ctx zen-ns ns-map opts]
  (swap! ctx assoc-in [:ns zen-ns] (assoc ns-map :zen/file (:zen/file opts))))

(defn get-ns [ns-map]
  (or (get ns-map 'ns) (get ns-map :ns)))

(defn get-ns-alias [ns-map]
  (or (get ns-map 'alias) (get ns-map :alias)))

(defn get-ns-imports [ns-map]
  (or (get ns-map 'import)
      (get ns-map :import)))

(defn ns-already-imported? [ctx zen-ns]
  (get-in @ctx [:ns zen-ns]))

(defn ns-in-memory-store? [ctx zen-ns]
  (contains? (:memory-store @ctx) zen-ns))

(defn get-from-memry-store [ctx zen-ns]
  (get-in @ctx [:memory-store zen-ns]))

(defn import-nss! [ctx zen-ns imports opts]
  (doseq [imp imports]
    (cond
      (ns-already-imported? ctx imp) :already-imported

      (ns-in-memory-store? ctx imp)
      (load-ns ctx (get-from-memry-store ctx imp) opts)

      :else (read-ns* ctx imp (assoc opts :ns zen-ns)))))

(defn process-ns-alias! [ctx this-ns-sym aliased-ns this-ns-map]
  (when (symbol? aliased-ns)
    (doseq [[aliased-sym _ :as kv] (get-in @ctx [:ns aliased-ns])]
      (when (symbol-definition? kv)
        (let [shadowed-here? (contains? this-ns-map aliased-sym)]
          (when (not shadowed-here?)
            (load-alias ctx
                        (mk-symbol aliased-ns aliased-sym)
                        (mk-symbol this-ns-sym aliased-sym))))))))

(defn load-ns-content! [ctx zen-ns ns-map opts]
  (let [ns-content (apply dissoc ns-map ['ns 'import 'alias :ns :import :alias])]
    (->> ns-content
         (sort-by (juxt symbol-definition? symbol-alias?)) #_"NOTE: load aliases first, symbols after"
         (mapv (fn [[k v :as kv]]
                 (cond (symbol-definition? kv) (load-symbol ctx zen-ns k (merge v opts))
                       (symbol-alias? kv)      (load-alias ctx v (mk-symbol zen-ns k))
                       :else                   nil)))
         (mapv (fn [res] (validate-resource ctx res))))))

(defn load-ns [ctx ns-map & [opts]]
  (let [zen-ns (get-ns ns-map)
        aliased-ns (get-ns-alias ns-map)]

    (add-zen-ns! ctx zen-ns ns-map opts)

    (pre-load-ns! ctx zen-ns ns-map)

    (import-nss! ctx
                 zen-ns
                 (cond->> (get-ns-imports ns-map)
                   (some? aliased-ns) (cons aliased-ns))
                 opts)

    (process-ns-alias! ctx zen-ns aliased-ns ns-map)

    (let [load-result (load-ns-content! ctx zen-ns ns-map opts)]
      [:resources-loaded (count load-result)])))

(defn load-ns! [ctx ns-map]
  (assert (map? ns-map) "Expected map")
  (load-ns ctx ns-map)
  (when-let [errs (:errors @ctx)]
    (throw (Exception. (str/join "\n" errs)))))

(defn expand-node-modules [path]
  (let [modules (io/file (str path "/node_modules"))]
    (when (and (.exists modules) (.isDirectory modules))
      (->> (.listFiles modules)
           (mapcat (fn [^java.io.File x]
                     (if (and (.isDirectory x) (str/starts-with? (.getName x) "@"))
                       (.listFiles x)
                       [x])))
           (filter (fn [^java.io.File x] (.isDirectory x)))))))

(defn mk-full-path [zen-path file-path]
  (str zen-path "/" file-path))

(defn get-file [zen-path file-path]
  (let [^java.io.File file (io/file (mk-full-path zen-path file-path))]
    (when (.exists file)
      file)))

(defn find-path
  "Returns a zen path containing pth"
  [paths pth]
  (loop [[p & ps] paths]
    (when p
      (if (some? (get-file p pth))
        (.getPath (io/file p))
        (recur (concat (expand-node-modules p) ps))))))

(defn find-in-paths
  "Returns file found in zen paths"
  [paths pth]
  (some-> (find-path paths pth)
          (get-file pth)))

(defn expand-zen-packages [path]
  (let [modules (io/file path)]
    (when (and (.exists modules) (.isDirectory modules))
      (->> (.listFiles modules)
           (map (fn [x] (str x "/zrc")))
           (filter #(.isDirectory (io/file %)))))))

(defn expand-package-path [package-path]
  (let [zrc-path         (str package-path "/zrc")
        zen-packages-path (str package-path "/zen-packages")]
    (cons zrc-path (expand-zen-packages zen-packages-path))))

(def ^:const unzip-cache-dir "/tmp/zen-unzip/")

(defn unzip-to-cache-dir [zip-path]
  (let [path-hash (zen.utils/string->md5 zip-path)
        unzip-dest (str unzip-cache-dir "/" path-hash)]
    (if (.exists (io/file unzip-dest))
      (str unzip-dest \/ "zrc")
      (str (zen.utils/unzip! zip-path unzip-dest) \/ "zrc"))))

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

(defn read-ns [ctx nm & [opts]]
  (let [pth (str (str/replace (str nm) #"\." "/") ".edn")]
    (if-let [{:keys [file zen-path]} (find-file&path ctx pth)]
      (try
        (let [content (slurp file)
              env (:env @ctx)
              ns-map (edamame.core/parse-string content
                                                {:readers {'env         (fn [v] (env-string  env v))
                                                           'env-string  (fn [v] (env-string  env v))
                                                           'env-integer (fn [v] (env-integer env v))
                                                           'env-symbol  (fn [v] (env-symbol  env v))
                                                           'env-number  (fn [v] (env-number  env v))
                                                           'env-keyword (fn [v] (env-keyword  env v))
                                                           'env-boolean (fn [v] (env-boolean env v))
                                                           'zen/quote   (fn [d] (zen-quote d))}})
              zen-ns (or (get ns-map 'ns) (get ns-map :ns))]
          (if (= nm zen-ns)
            (do (load-ns ctx ns-map (cond-> {:zen/file (.getPath file)}
                                      zen-path (assoc :zen/zen-path zen-path)))
                :zen/loaded)
            (do (println :file-doesnt-match-namespace (.getPath file) nm zen-ns)
                (swap! ctx update :errors
                       (fnil conj [])
                       {:message (str "Filename should match contained namespace. Expected "
                                      nm " got " zen-ns)
                        :file (.getPath file)
                        :ns nm
                        :got-ns zen-ns})
                :zen/load-failed)))
        (catch Exception e
          (println :error-while-reading (.getPath file) e)
          (swap! ctx update :errors
                 (fnil conj [])
                 {:message (.getMessage e)
                  :file (.getPath file)
                  :ns nm})
          :zen/load-failed))
      (do (swap! ctx update :errors
                 (fnil conj [])
                 {:message (format "No file for ns '%s" nm)
                  :missing-ns nm
                  :ns (or (:ns opts) nm)})
          :zen/load-failed))))

(defn read-ns! [ctx ns-sym]
  (assert (symbol? ns-sym) "Expected symbol")
  (read-ns ctx ns-sym)
  (when-let [errs (:errors @ctx)]
    (throw (Exception. (str/join "\n" errs)))))

(defn new-context [& [opts]]
  (let [ctx (atom (or opts {}))]
    (read-ns ctx 'zen)
    ctx))

(defn instance-of? [tp res]
  (let [tps (get res 'types)]
    (or (and (set? tps) (contains? tps 'primitive))
        (= tps 'primitive))))
