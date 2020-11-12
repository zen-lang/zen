(ns zen.store
  (:require
   [zen.validation]
   [clojure.edn]
   [clojure.java.io :as io]
   [clojure.walk]
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
                   (swap! ctx update :errors conj (format "Could not resolve symbol '%s in %s/%s" x ns-name k)))
                 x)
             (do (when-not (get nmsps x)
                   (swap! ctx update :errors conj (format "Could not resolve local symbol '%s in %s/%s" x ns-name k)))
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
      (println "validate with" schemas)
      (let [{errs :errors} (zen.validation/validate ctx schemas res)]
        (when-not (empty? errs)
          (doseq [err errs]
            (swap! ctx update :errors
                   conj (format "Validation: %s '%s' in %s by %s"
                                (get res :zen/name)
                                (:message err)
                                (pretty-path (:path err))
                                (pretty-path (:schema err))))))))))

(defn load-symbol [ctx nmsps k v]
  (let [ns-name (get nmsps 'ns)
        ns-str (name ns-name)
        sym (symbol ns-str (name k))
        res (eval-resource ctx ns-str ns-name nmsps k v)]
    (swap! ctx (fn [ctx] (update-in ctx [:symbols sym] (fn [x] (when x (println "WARN: reload" (:zen/name res))) res))))
    (doseq [tg (:zen/tags res)]
      (swap! ctx update-in [:tags tg] (fn [x] (conj (or x #{}) sym))))
    res))

(defn load-ns [ctx nmsps]
  (let [ns-name (get nmsps 'ns)]
    (when-not (get-in ctx [:ns ns-name])
      (swap! ctx (fn [ctx] (assoc-in ctx [:ns ns-name] nmsps)))
      (doseq [imp (get nmsps 'import)]
        (read-ns ctx imp))
      (->>
       (dissoc nmsps ['ns 'import])
       (mapv (fn [[k v]]
               (cond (and (symbol? k) (map? v)) (load-symbol ctx nmsps k v)
                     :else nil)))
       (mapv (fn [res] (validate-resource ctx res)))))))

(defn read-ns [ctx nm]
  (let [pth (str (str/replace (str nm) #"\." "/") ".edn")]
    (if-let [res (io/resource pth)]
      (let [nmsps (clojure.edn/read-string (slurp (.getPath res)))]
        (load-ns ctx nmsps))
      (swap! ctx update :errors conj (format "Could not load ns '%s" nm)))))

(defn get-symbol [ctx nm]
  (when-let [res (get-in @ctx [:symbols nm])]
    (assoc res 'name nm)))

(defn new-context [& [opts]]
  (let [ctx  (atom {})]
    (read-ns ctx 'zen)
    ctx))

(defn instance-of? [tp res]
  (let [tps (get res 'types)]
    (or (and (set? tps) (contains? tps 'primitive))
        (= tps 'primitive))))

