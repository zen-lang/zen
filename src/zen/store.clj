(ns zen.store
  (:require
   [zen.validation]
   [clojure.edn]
   [clojure.java.io :as io]
   [clojure.walk]
   [clojure.string :as str]))

(defn update-types-recur [ctx tp-sym sym]
  (swap! ctx update-in [:tps tp-sym] (fn [x] (conj (or x #{}) sym)))
  (doseq [tp-sym' (get-in @ctx [:syms tp-sym :isa])]
    (update-types-recur ctx tp-sym' sym)))

(declare read-ns)

(defn pretty-path [pth]
  (->> pth
       (mapv (fn [x] (if (keyword? x) (subs (str x) 1) (str x))))
       (str/join "." )))

(defn load-ns [ctx nmsps]
  (let [ns-name (get nmsps 'ns)
        ns-str (name ns-name)]
    (when-not (get-in ctx [:ns ns-name])
      (swap! ctx (fn [ctx] (assoc-in ctx [:ns ns-name] nmsps)))
      (doseq [imp (get nmsps 'import)]
        (read-ns ctx imp))
      (->>
       (dissoc nmsps ['ns 'import])
       (mapv (fn [[k v]]
              (when (and (symbol? k) (map? v))
                (let [sym (symbol ns-str (name k))
                      res (-> (clojure.walk/postwalk
                               (fn [x]
                                 (if (and (symbol? x) (not (contains? #{'types} x)))
                                   (if (namespace x)
                                     (do (when-not (get-in @ctx [:syms x])
                                           (swap! ctx update :errors conj (format "Could not resolve symbol '%s in %s/%s" x ns-name k)))
                                         x)
                                     (do (when-not (get nmsps x)
                                           (swap! ctx update :errors conj (format "Could not resolve local symbol '%s in %s/%s" x ns-name k)))
                                         (symbol ns-str (name x))))
                                   x))
                               (dissoc v 'tags 'types))
                              (assoc 'ns ns-name
                                     'name (symbol (name ns-name) (name k))
                                     'types (get 'types v)))]
                  (swap! ctx (fn [ctx] (assoc-in ctx [:syms sym] res)))
                  (when-let [tps (get v 'types)]
                    (assert (or (set? tps) (symbol? tps)) (format "types should be a set of symbols or symbol in %s/%s" ns-name k))
                    (doseq [tp-sym (if (symbol? tps) [tps] tps)]
                      (update-types-recur ctx tp-sym sym)))
                  res))))
       (mapv (fn [res]
               (when-let [tps (and res (get res 'types))]
                 (let [tps (if (symbol? tps) #{tps} tps)]
                   (let [{errs :errors} (zen.validation/validate ctx tps (dissoc res 'types 'ns 'name 'tags))]
                     (when-not (empty? errs)
                       (doseq [err errs]
                         (swap! ctx update :errors
                                conj (format "Validation: %s '%s' in %s by %s"
                                             (get res 'name)
                                             (:message err)
                                             (pretty-path (:path err))
                                             (pretty-path (:schema err)))))))))))))))

(defn read-ns [ctx nm]
  (let [pth (str (str/replace (str nm) #"\." "/") ".edn")]
    (if-let [res (io/resource pth)]
      (let [nmsps (clojure.edn/read-string (slurp (.getPath res)))]
        (load-ns ctx nmsps))
      (swap! ctx update :errors conj (format "Could not load ns '%s" nm)))))

(defn get-symbol [ctx nm]
  (when-let [res (get-in @ctx [:syms nm])]
    (assoc res 'name nm)))

(defn new-context [& [opts]]
  (let [ctx  (atom {})]
    (read-ns ctx 'zen)
    ctx))

(defn instance-of? [tp res]
  (let [tps (get res 'types)]
    (or (and (set? tps) (contains? tps 'primitive))
        (= tps 'primitive))))

