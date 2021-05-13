(ns zen.utils)


(defn deep-merge
  "efficient deep merge"
  [a b]
  (loop [[[k v :as i] & ks] b, acc a]
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


(defn assoc-when-key
  ([pred m k v]
   (cond-> m (pred k) (assoc k v)))
  ([pred m k v & kvs]
   {:pre [(even? (count kvs))]}
   (reduce (partial apply assoc-when-key pred)
           (assoc-when-key pred m k v)
           (partition 2 kvs))))


(defn assoc-when
  ([pred m k v]
   (cond-> m (pred v) (assoc k v)))
  ([pred m k v & kvs]
   {:pre [(even? (count kvs))]}
   (reduce (partial apply assoc-when pred)
           (assoc-when pred m k v)
           (partition 2 kvs))))


(defn assoc-some [m k v & kvs]
  (apply assoc-when some? m k v kvs))


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


(defn dissoc-when-key
  ([pred m k]
   (cond-> m
     (and (contains? m k)
          (pred k))
     (dissoc k)))
  ([pred m k & ks]
   (reduce (partial dissoc-when-key pred)
           (dissoc-when-key pred m k)
           ks)))


(defn dissoc-when
  ([pred m k]
   (cond-> m
     (and (contains? m k)
          (pred (get m k)))
     (dissoc k)))
  ([pred m k & ks]
   (reduce (partial dissoc-when pred)
           (dissoc-when pred m k)
           ks)))


(defn dissoc-nil [m k & ks]
  (apply dissoc-when nil? m k ks))


(defn strip-when-key [pred m]
  (if-let [ks (seq (keys m))]
    (apply dissoc-when-key pred m ks)
    m))


(defn strip-when-kv [pred m]
  (if-let [ks (seq (keys m))]
    (apply dissoc-when-kv pred m ks)
    m))


(defn strip-when [pred m]
  (if-let [ks (seq (keys m))]
    (apply dissoc-when pred m ks)
    m))


(defn strip-nils [m]
  (strip-when nil? m))
