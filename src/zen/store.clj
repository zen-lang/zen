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
  ;; NOTE disables symbol expansions in lists (expr types)
  (if (list? resource)
    resource
    (walk/walk (partial walk-resource walk-fn) walk-fn resource)))

(defn eval-resource [ctx ns-str ns-name nmsps k resource]
  (let [walk-fn
        (fn [x]
          (if (symbol? x)
            (if (namespace x)
              (do (when-not (get-symbol ctx x)
                    (swap! ctx update :errors
                           (fnil conj [])
                           {:message (format "Could not resolve symbol '%s in %s/%s" x ns-name k)
                            :ns ns-name}))
                  x)
              (do (when-not (get-symbol ctx (zen.utils/mk-symbol ns-name x))
                    (swap! ctx update :errors
                           (fnil conj [])
                           {:message (format "Could not resolve local symbol '%s in %s/%s" x ns-name k)
                            :ns ns-name}))
                  (zen.utils/mk-symbol ns-str x)))
            x))]

    (-> (walk-resource walk-fn resource)
        (assoc :zen/name (zen.utils/mk-symbol ns-name k)))))

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

(defn load-symbol [ctx nmsps k v]
  (let [ns-name (or (get nmsps 'ns) (get nmsps :ns))
        ns-str (name ns-name)
        sym (zen.utils/mk-symbol ns-name k)
        res (eval-resource ctx ns-str ns-name nmsps k v)]
    (swap! ctx (fn [ctx] (update-in ctx [:symbols sym] (fn [x]
                                                         #_(when x (println "WARN: reload" (:zen/name res)))
                                                         res))))
    (doseq [tg (:zen/tags res)]
      (swap! ctx update-in [:tags tg] (fn [x] (conj (or x #{}) sym))))
    res))

(defn load-alias [ctx alias-dest alias]
  (swap! ctx update :aliases zen.utils/disj-set-union-push alias-dest alias))

(defn symbol-definition? [[k v]]
  (and (symbol? k) (map? v)))

(defn symbol-alias? [[k v]]
  (and (symbol? k) (qualified-symbol? v)))

(defn pre-load-ns!
  "Loads symbols from nmsps to ctx without any processing
  so they can be referenced before they're processed"
  [ctx nmsps]
  (let [ns-name (or (get nmsps 'ns) (get nmsps :ns))
        this-ns-symbols
        (into {}
              (keep (fn [[sym schema :as kv]]
                      (when (symbol-definition? kv)
                        [(zen.utils/mk-symbol ns-name sym)
                         (select-keys schema #{:zen/tags})]))) ;; TODO: maybe not only tags must be saved?
              nmsps)]
    (swap! ctx update :symbols (partial merge this-ns-symbols))))

(defn load-ns [ctx nmsps & [opts]]
  (let [ns-name (or (get nmsps 'ns) (get nmsps :ns))
        aliased-ns (or (get nmsps 'alias) (get nmsps :alias))]
    (when (not (get-in @ctx [:ns ns-name]))
      (swap! ctx (fn [ctx] (assoc-in ctx [:ns ns-name] (assoc nmsps :zen/file (:zen/file opts)))))

      (pre-load-ns! ctx nmsps)

      (doseq [imp (cond->> (or (get nmsps 'import)
                               (get nmsps :import))
                    (symbol? aliased-ns) (cons aliased-ns))]
        (cond
          (get-in @ctx [:ns imp])
          :already-imported

          (contains? (:memory-store @ctx) imp)
          (load-ns ctx (get-in @ctx [:memory-store imp]) opts)

          :else
          (read-ns ctx imp {:ns ns-name})))

      (when (symbol? aliased-ns)
        (doseq [[aliased-sym _ :as kv] (get-in @ctx [:ns aliased-ns])]
          (when (symbol-definition? kv)
            (let [shadowed-here? (contains? nmsps aliased-sym)]
              (when (not shadowed-here?)
                (load-alias ctx
                            (zen.utils/mk-symbol aliased-ns aliased-sym)
                            (zen.utils/mk-symbol ns-name aliased-sym)))))))

      (->> (dissoc nmsps ['ns 'import 'alias :ns :import :alias])
           (mapv (fn [[k v :as kv]]
                   (cond (symbol-definition? kv) (load-symbol ctx nmsps k (merge v opts))
                         (symbol-alias? kv)      (load-alias ctx v (zen.utils/mk-symbol ns-name k))
                         :else                   nil)))
           (mapv (fn [res] (validate-resource ctx res)))))))

(defn load-ns! [ctx nmsps]
  (assert (map? nmsps) "Expected map")
  (load-ns ctx nmsps)
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

(defn expand-zen-modules [path]
  (let [modules (io/file path)]
    (when (and (.exists modules) (.isDirectory modules))
      (->> (.listFiles modules)
           (map (fn [x] (str x "/zrc")))
           (filter #(.isDirectory (io/file %)))))))

(defn expand-package-path [package-path]
  (let [zrc-path         (str package-path "/zrc")
        zen-modules-path (str package-path "/zen-modules")]
    (cons zrc-path (expand-zen-modules zen-modules-path))))

(defn find-in-package-paths [package-paths pth-to-find]
  (find-in-paths (mapcat expand-package-path package-paths)
                 pth-to-find))

;; TODO: cache find file
(defn find-file [ctx pth]
  (or (io/resource pth)
      (find-in-package-paths (:package-paths @ctx) pth)
      (find-in-paths (:paths @ctx) pth)))


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

(defn read-ns [ctx nm & [opts]]
  (let [pth (str (str/replace (str nm) #"\." "/") ".edn")]
    (if-let [file (find-file ctx pth)]
      (try
        (let [content (slurp file)
              env (:env @ctx)
              nmsps (edamame.core/parse-string content {:readers {'env         (fn [v] (env-string  env v))
                                                                  'env-string  (fn [v] (env-string  env v))
                                                                  'env-integer (fn [v] (env-integer env v))
                                                                  'env-symbol  (fn [v] (env-symbol  env v))
                                                                  'env-number  (fn [v] (env-number  env v))
                                                                  'env-keyword (fn [v] (env-keyword  env v))}})
              ns-name (or (get nmsps 'ns) (get nmsps :ns))]
          (if (= nm ns-name)
            (do (load-ns ctx nmsps {:zen/file pth})
                :zen/loaded)
            (do (prn :file-doesnt-match-namespace (.getPath file) nm ns-name)
                (swap! ctx update :errors
                       (fnil conj [])
                       {:message (str "Filename should match contained namespace. Expected "
                                      nm " got " ns-name)
                        :file (.getPath file)
                        :ns nm})
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

(defn read-ns! [ctx nmsps]
  (assert (symbol? nmsps) "Expected symbol")
  (read-ns ctx nmsps)
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
