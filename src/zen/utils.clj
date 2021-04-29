(ns zen.utils)


(defn deep-merge
  "efficient deep merge"
  [a b]
  (loop [[[k v :as i] & ks] b
         acc a]
    (if (nil? i)
      acc
      (let [av (get a k)]
        (if (= v av)
          (recur ks acc)
          (recur ks (if (and (map? v) (map? av))
                      (assoc acc k (deep-merge av v))
                      (assoc acc k v))))))))


(defn assoc-when-kv
  ([pred m k v]
   (cond-> m (pred [k v]) (assoc k v)))
  ([pred m k v & kvs]
   {:pre [(even? (count kvs))]}
   (reduce (partial apply assoc-when-kv pred)
           (assoc-when-kv pred m k v)
           (partition 2 kvs))))


(defn assoc-when-key [pred & args]
  (apply assoc-when-kv (comp pred first) args))


(defn assoc-when [pred & args]
  (apply assoc-when-kv (comp pred second) args))


(defn assoc-some [& args]
  (apply assoc-when some? args))


(defn dissoc-when-kv
  ([pred m k]
   (cond-> m
     (and (contains? m k)
          (pred [k (get m k)]))
     (dissoc k)))
  ([pred m k & ks]
   (reduce (partial dissoc-when-kv pred)
           (dissoc-when-kv pred m k)
           ks)))


(defn dissoc-when-key [pred & args]
  (apply dissoc-when-kv (comp pred first) args))


(defn dissoc-when [pred & args]
  (apply dissoc-when-kv (comp pred second) args))


(defn dissoc-nil [& args]
  (apply dissoc-when nil? args))


(defn strip-when-key [pred m]
  (apply dissoc-when-key pred m (keys m)))


(defn strip-when-kv [pred m]
  (apply dissoc-when-kv pred m (keys m)))


(defn strip-when [pred m]
  (apply dissoc-when pred m (keys m)))


(defn strip-nils [m]
  (strip-when nil? m))
