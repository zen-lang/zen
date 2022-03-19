(ns zen.store
  (:require [zen.validation]
            [zen.utils]
            [clojure.edn]
            [clojure.java.io :as io]
            [clojure.walk]
            [edamame.core]
            [clojure.string :as str]))


(defn update-types-recur [ctx tp-sym sym]
  (swap! ctx update-in [:tps tp-sym] (fn [x] (conj (or x #{}) sym)))
  (doseq [tp-sym' (get-in @ctx [:symbols tp-sym :isa])]
    (update-types-recur ctx tp-sym' sym)))

(declare read-ns)

(defn pretty-path [pth]
  (->> pth
       (mapv (fn [x] (if (keyword? x) (subs (str x) 1) (str x))))
       (str/join "->" )))

(defn eval-resource [ctx ns-str ns-name nmsps k resource]
  (-> (clojure.walk/postwalk
        (fn [x]
          (if (symbol? x) ;; TODO: allow symbols namespaced with current namespace
            (if (namespace x)
              (do (when-not (get-in @ctx [:symbols x])
                    (swap! ctx update :errors
                           (fnil conj [])
                           {:message (format "Could not resolve symbol '%s in %s/%s" x ns-name k)
                            :ns ns-name}))
                  x)
              (do (when-not (get nmsps x)
                    (swap! ctx update :errors
                           (fnil conj [])
                           {:message (format "Could not resolve local symbol '%s in %s/%s" x ns-name k)
                            :ns ns-name}))
                  (symbol ns-str (name x))))
            x))
        resource)
      (assoc ;;TODO :zen/ns ns-name
        :zen/name (symbol (name ns-name) (name k)))))


(defn validate-resource [ctx res]
  (let [tags (get res :zen/tags)
        schemas (->> tags
                     (mapv (fn [tag]
                             (when-let [sch (get-in @ctx [:symbols tag])]
                               (when (contains? (:zen/tags sch) 'zen/schema)
                                 tag))))
                     (filter identity)
                     (into #{}))]
    (when-not (empty? schemas)
      (let [{errs :errors} (zen.validation/validate ctx schemas res)]
        (when-not (empty? errs)
          (swap! ctx update :errors (fn [x] (into (or x []) (mapv #(assoc % :resource (:zen/name res)) errs)))))))))

(defn load-symbol [ctx nmsps k v]
  (let [ns-name (get nmsps 'ns)
        ns-str (name ns-name)
        sym (symbol ns-str (name k))
        res (eval-resource ctx ns-str ns-name nmsps k v)]
    (swap! ctx (fn [ctx] (update-in ctx [:symbols sym] (fn [x]
                                                        #_(when x (println "WARN: reload" (:zen/name res)))
                                                        res))))
    (doseq [tg (:zen/tags res)]
      (swap! ctx update-in [:tags tg] (fn [x] (conj (or x #{}) sym))))
    res))

(defn load-alias [ctx nmsps k v]
  (let [ns-name (get nmsps 'ns)
        ns-str  (name ns-name)
        sym     (symbol ns-str (name k))]
    (swap! ctx update :aliases zen.utils/disj-set-union-push sym v)))

(defn pre-load-ns!
  "Loads symbols from nmsps to ctx without any processing
  so they can be referenced before they're processed"
  [ctx nmsps]
  (let [ns-name (get nmsps 'ns)
        this-ns-symbols
        (into {}
              (keep (fn [[sym schema]]
                      (when (map? schema) ;; TODO: maybe not only maps?
                        [(symbol (name ns-name) (name sym))
                         (select-keys schema #{:zen/tags})]))) ;; TODO: maybe not only tags must be saved?
              nmsps)]
    (swap! ctx update :symbols (partial merge this-ns-symbols))))

(defn load-ns [ctx nmsps & [opts]]
  (let [ns-name (get nmsps 'ns)]
    (when (or true (not (get-in @ctx [:ns ns-name])))
      (swap! ctx (fn [ctx] (assoc-in ctx [:ns ns-name] (assoc nmsps :zen/file (:zen/file opts)))))

      (pre-load-ns! ctx nmsps)

      (doseq [imp (cond->> (get nmsps 'import)
                    (contains? nmsps 'alias)
                    (cons (get nmsps 'alias)))]
        (cond
          (get-in @ctx [:ns imp])
          :already-imported

          (contains? (:memory-store @ctx) imp)
          (load-ns ctx (get-in @ctx [:memory-store imp]) opts)

          :else
          (read-ns ctx imp {:ns ns-name})))

      (->> (dissoc nmsps ['ns 'import 'alias])
           (mapv (fn [[k v]]
                   (cond (and (symbol? k) (map? v))    (load-symbol ctx nmsps k (merge v opts))
                         (and (symbol? k) (qualified-symbol? v)) (load-alias ctx nmsps k v)
                         :else                         nil)))
           (mapv (fn [res] (validate-resource ctx res)))))))



(defn load-ns! [ctx nmsps]
  (assert (map? nmsps) "Expected map")
  (load-ns ctx nmsps)
  (when-let [errs (:errors @ctx)]
    (throw (Exception. (str/join "\n" errs)))))


(defn get-file [ctx pth]
  (or (when-let [resource (io/resource pth)]
        {:type     :resource
         :resource resource
         :path     (.getPath resource)})
      (some-> (get (:paths/cache @ctx) pth)
              (assoc :path (str "paths/cache:" pth)))))


;; TODO: cache find file
(defn find-file [ctx paths pth]
  (or (get-file ctx pth)
      (loop [[p & ps] paths]
        (when p
          (let [fpth (str p "/" pth)
                file (io/file fpth)]
            (if (.exists file)
              {:type :file
               :file file
               :path (.getPath file)}
              (let [modules (io/file (str p "/node_modules"))]
                (if (and (.exists modules) (.isDirectory modules))
                  (or (->> (.listFiles modules)
                           (mapcat (fn [x]
                                     (if (and (.isDirectory x) (str/starts-with? (.getName x) "@"))
                                       (.listFiles x)
                                       [x])))
                           (filter #(.isDirectory %))
                           (some (fn [x] (find-file ctx [x] pth))))
                      (recur ps))
                  (recur ps)))))))))


(defmulti read-file (fn [file-map] (:type file-map)))


(defmethod read-file :string [{s :string, :keys [return-input-stream]}]
  (if return-input-stream
    (java.io.ByteArrayInputStream. (.getBytes s))
    s))


(defmethod read-file :file [{:keys [file return-input-stream]}]
  (if return-input-stream
    (java.io.FileInputStream. file)
    (slurp file)))


(defmethod read-file :resource [{:keys [resource return-input-stream]}]
  (if return-input-stream
    (.openStream resource)
    (slurp resource)))

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
    (if-let [file (find-file ctx (:paths @ctx) pth)]
      (try
        (let [content (read-file file)
              env (:env @ctx)
              nmsps (edamame.core/parse-string content {:readers {'env         (fn [v] (env-string  env v))
                                                                  'env-string  (fn [v] (env-string  env v))
                                                                  'env-integer (fn [v] (env-integer env v))
                                                                  'env-symbol  (fn [v] (env-symbol  env v))
                                                                  'env-number  (fn [v] (env-number  env v))
                                                                  'env-keyword (fn [v] (env-keyword  env v))
                                                                  }})]
          (load-ns ctx nmsps {:zen/file pth})
          :zen/loaded)
        (catch Exception e
          (println :error-while-reading file e)
          (swap! ctx update :errors
                 (fnil conj [])
                 {:message (.getMessage e)
                  :file (:path file)
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

(def get-symbol zen.utils/get-symbol)

(defn get-tag [ctx tag]
  (when-let [aliases (conj (or (get-in @ctx [:aliases tag])
                               #{})
                           tag)]
    (reduce (fn [acc alias] (into acc (get-in @ctx [:tags alias])))
            #{}
            aliases)))

(defn new-context [& [opts]]
  (let [ctx (atom (or opts {}))]
    (read-ns ctx 'zen)
    ctx))

(defn instance-of? [tp res]
  (let [tps (get res 'types)]
    (or (and (set? tps) (contains? tps 'primitive))
        (= tps 'primitive))))

