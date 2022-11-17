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

(defn update-types-recur [ctx tp-sym sym]
  (swap! ctx update-in [:tps tp-sym] (fn [x] (conj (or x #{}) sym)))
  (doseq [tp-sym' (:isa (get-symbol ctx tp-sym))]
    (update-types-recur ctx tp-sym' sym)))

(declare read-ns)

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
  (let [resolved (if (namespace sym)
                   sym
                   (zen.utils/mk-symbol zen-ns sym))
        res (get-symbol ctx resolved)]

    (when (contains? (:zen/tags res) 'zen/binding)
      (swap! ctx update-in [:bindings sym]
             (fn [cfg]
               (update (or cfg res)
                       :diref
                       (fnil conj #{})
                       ns-key))))

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

(defn eval-resource
  "walks resource, expands symbols and validates refs"
  [ctx zen-ns ns-sym resource]
  (let [walk-fn
        (fn [v]
          ;; do not validate zen/quote'd symbols
          (let [zen-sym? (and (symbol? v) (not (:zen/quote (meta v))))]
            (when zen-sym?
              (validate-symbol ctx zen-ns ns-sym v))

            ;; resolve local sym
            (if (and zen-sym? (not (namespace v)))
              (zen.utils/mk-symbol zen-ns v)
              v)))]

    (-> (walk-resource walk-fn resource)
        (assoc :zen/name (zen.utils/mk-symbol zen-ns ns-sym)))))

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

(defn load-symbol [ctx zen-ns ns-sym v]
  (let [sym (zen.utils/mk-symbol zen-ns ns-sym)

        {:keys [zen/tags zen/late-bind] :as resource}
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
                   (do
                     (when late-bind
                       (assoc-in ctx* [:bindings late-bind :backref] sym))
                     ctx*))))
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
                        [(zen.utils/mk-symbol zen-ns sym)
                         (select-keys resource #{:zen/tags})]))) ;; TODO: maybe not only tags must be saved?
              ns-map)]
    (swap! ctx update :symbols merge symbols)))

(defn load-ns [ctx ns-map & [opts]]
  (let [zen-ns (or (get ns-map 'ns) (get ns-map :ns))
        aliased-ns (or (get ns-map 'alias) (get ns-map :alias))]
    (swap! ctx assoc-in [:ns zen-ns] (assoc ns-map :zen/file (:zen/file opts)))

    (pre-load-ns! ctx zen-ns ns-map)

    ;; do imports
    (doseq [imp (cond->> (or (get ns-map 'import)
                             (get ns-map :import))
                  (symbol? aliased-ns) (cons aliased-ns))]
      (cond
        (get-in @ctx [:ns imp])
        :already-imported

        (contains? (:memory-store @ctx) imp)
        (load-ns ctx (get-in @ctx [:memory-store imp]) opts)

        :else (read-ns ctx imp opts)))

    ;; process aliases
    (when (symbol? aliased-ns)
      (doseq [[aliased-sym _ :as kv] (get-in @ctx [:ns aliased-ns])]
        (when (symbol-definition? kv)
          (let [shadowed-here? (contains? ns-map aliased-sym)]
            (when (not shadowed-here?)
              (load-alias ctx
                          (zen.utils/mk-symbol aliased-ns aliased-sym)
                          (zen.utils/mk-symbol zen-ns aliased-sym)))))))

    ;; eval symbols
    (->> (dissoc ns-map ['ns 'import 'alias :ns :import :alias])
         (mapv (fn [[k v :as kv]]
                 (cond (symbol-definition? kv) (load-symbol ctx zen-ns k (merge v opts))
                       (symbol-alias? kv)      (load-alias ctx v (zen.utils/mk-symbol zen-ns k))
                       :else                   nil)))
         (mapv (fn [res] (validate-resource ctx res))))))

(defn load-ns! [ctx ns-map]
  (assert (map? ns-map) "Expected map")
  (load-ns ctx ns-map)
  (when-let [errs (:errors @ctx)]
    (throw (Exception. (str/join "\n" errs)))))

(defn expand-node-modules [path]
  (let [modules (io/file (str path "/node_modules"))]
    (when (and (.exists modules) (.isDirectory modules))
      (->> (.listFiles modules)
           (mapcat (fn [x]
                     (if (and (.isDirectory x) (str/starts-with? (.getName x) "@"))
                       (.listFiles x)
                       [x])))
           (filter #(.isDirectory %))))))

(defn find-in-paths [paths pth]
  (loop [[p & ps] paths]
    (when p
      (let [fpth (str p "/" pth)
            file (io/file fpth)]
        (if (.exists file)
          file
          (recur (concat (expand-node-modules p) ps)))))))

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
      unzip-dest
      (zen.utils/unzip! zip-path unzip-dest))))

;; TODO: cache find file
(defn find-file [ctx pth]
  (or (io/resource pth)
      (find-in-paths (concat (mapcat expand-package-path (:package-paths @ctx))
                             (:paths @ctx)
                             (map unzip-to-cache-dir (:zip-paths @ctx)))
                     pth)))

(defn get-env [env env-name]
  (or (get env (keyword env-name))
      (System/getenv (str env-name))))

(defn env-string [env env-name]
  (when-let [v (get-env env env-name)]
    v))

(defn env-integer [env env-name]
  (when-let [v (get-env env env-name)]
    (Integer/parseInt v)))

(defn env-symbol [env env-name]
  (when-let [v (get-env env env-name)]
    (symbol v)))

(defn env-keyword [env env-name]
  (when-let [v (get-env env env-name)]
    (keyword v)))

(defn env-number [env env-name]
  (when-let [v (get-env env env-name)]
    (Double/parseDouble v)))

(defn zen-quote [d]
  (with-meta d {:zen/quote true}))

(defn read-ns [ctx nm & [opts]]
  (let [pth (str (str/replace (str nm) #"\." "/") ".edn")]
    (if-let [file (find-file ctx pth)]
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
                                                           'zen/quote   (fn [d] (zen-quote d))}})
              zen-ns (or (get ns-map 'ns) (get ns-map :ns))]
          (if (= nm zen-ns)
            (do (load-ns ctx ns-map {:zen/file (.getPath file)})
                :zen/loaded)
            (do (prn :file-doesnt-match-namespace (.getPath file) nm zen-ns)
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
