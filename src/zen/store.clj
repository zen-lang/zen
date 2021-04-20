(ns zen.store
  (:require [zen.validation]
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
         (if (symbol? x)
           (if (namespace x)
             (do (when-not (get-in @ctx [:symbols x])
                   (swap! ctx update :errors conj {:message (format "Could not resolve symbol '%s in %s/%s" x ns-name k)}))
                 x)
             (do (when-not (get nmsps x)
                   (swap! ctx update :errors conj {:message (format "Could not resolve local symbol '%s in %s/%s" x ns-name k)}))
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

(defn load-ns [ctx nmsps & [opts]]
  (let [ns-name (get nmsps 'ns)]
    (when-not (get-in ctx [:ns ns-name])
      (swap! ctx (fn [ctx] (assoc-in ctx [:ns ns-name] (assoc nmsps :zen/file (:zen/file opts)))))
      (doseq [imp (get nmsps 'import)]
        (if-let [ns (get-in @ctx [:memory-store imp])]
          (load-ns ctx ns opts)
          (read-ns ctx imp)))
      (->>
       (dissoc nmsps ['ns 'import])
       (mapv (fn [[k v]]
               (cond (and (symbol? k) (map? v)) (load-symbol ctx nmsps k (merge v opts))
                     :else nil)))
       (mapv (fn [res] (validate-resource ctx res)))))))

(defn load-ns! [ctx nmsps]
  (assert (map? nmsps) "Expected map")
  (load-ns ctx nmsps)
  (when-let [errs (:errors @ctx)]
    (throw (Exception. (str/join "\n" errs)))))

(defn read-ns [ctx nm]
  (let [pth (str (str/replace (str nm) #"\." "/") ".edn")]
    (if-let [fpth (or (when-let [res (io/resource pth)]
                       (.getPath res))
                     (loop [[p & ps] (:paths @ctx)]
                       (when p
                         (let [fpth (str p "/" pth)]
                           (if (when-let [f (io/file fpth)]
                                 (.exists f))
                             fpth
                             (recur ps))))))]
      (try
        (let [nmsps (edamame.core/parse-string (slurp fpth))]
          (load-ns ctx nmsps {:zen/file fpth}))
        (catch Exception e
          (println (str "ERROR while reading " fpth))
          (println e)
          (throw e)))
      (swap! ctx update :errors conj {:message (format "No file for ns '%s" nm)}))))

(defn read-ns! [ctx nmsps]
  (assert (symbol? nmsps) "Expected symbol")
  (read-ns ctx nmsps)
  (when-let [errs (:errors @ctx)]
    (throw (Exception. (str/join "\n" errs)))))


(defn get-symbol [ctx nm]
  (get-in @ctx [:symbols nm]))

(defn get-tag [ctx tag]
  (get-in @ctx [:tags tag]))

(defn new-context [& [opts]]
  (let [ctx  (atom (or opts {}))]
    (read-ns ctx 'zen)
    ctx))

(defn instance-of? [tp res]
  (let [tps (get res 'types)]
    (or (and (set? tps) (contains? tps 'primitive))
        (= tps 'primitive))))

