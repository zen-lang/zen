(ns zen.utils
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]))

(defn deep-merge
  "Efficient deep merge."
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

(defn disj-set-get-group [disj-set value]
  (get-in disj-set [value :group]))

(defn disj-set-get-root [disj-set value]
  (get-in disj-set [value :root]))

(defn disj-set-union-push
  ([disj-set value]
   (if (contains? disj-set value)
     disj-set
     (assoc disj-set
            value
            {:root value
             :group #{value}})))
  ([disj-set base-value & joining-values]
   (let [values    (cons base-value joining-values)
         root      (or (some #(disj-set-get-root disj-set %)
                             values)
                       base-value)
         groups    (keep #(disj-set-get-group disj-set %) values)
         new-group (reduce conj
                           (or (not-empty (reduce into groups))
                               #{})
                           values)]
     (reduce (fn [acc k] (assoc acc k {:root  root
                                       :group new-group}))
             (or disj-set {})
             new-group))))


#_"NOTE: this fn can be used in get-symbol. Can be refactored if it doesn't affect 'get-symbol performance"
(defn resolve-aliased-sym [ctx sym]
  (let [symbols (:symbols @ctx)]
    (or (when (contains? symbols sym)
          sym)
        (when-let [alias-root (disj-set-get-root (:aliases @ctx) sym)]
          (when (contains? symbols alias-root)
            alias-root)))))


(defn get-symbol [ctx sym]
  (when (symbol? sym)
    (let [{:keys [zen/tags] :as resource}
          (or (get-in @ctx [:symbols sym])
              (when-let [alias-root (disj-set-get-root (:aliases @ctx) sym)]
                (get-in @ctx [:symbols alias-root])))]
      (if (contains? tags 'zen/binding)
        (let [{:keys [backref]} (get-in @ctx [:bindings sym])]
          (if backref
            (merge (get-symbol ctx backref) resource)
            resource))
        resource))))


#_"TODO: profile performance with alisases"
(defn get-tag [ctx tag]
  (let [tags (:tags @ctx)]
    (or (get tags tag)
        (get tags (resolve-aliased-sym ctx tag)))))

(defn mk-symbol [ns-part name-part]
  (with-meta
    (symbol
     (if (qualified-ident? ns-part)
       (namespace ns-part)
       (name ns-part))
     (name name-part))
    (merge (meta ns-part) (meta name-part))))

(defmacro iter-reduce
  [fn val iterable]
  (let [[params & fn-body] (if (list? fn)
                             (drop-while symbol? fn)
                             [['acc 'val] (list fn 'acc 'val)])
        [acc-arg el-arg] params
        tagged-iter (vary-meta iterable assoc :tag `Iterable)]
    `(let [iter# (.iterator ~tagged-iter)]
       (loop [~acc-arg ~val]
         (if (.hasNext iter#)
           (let [~el-arg (.next iter#)]
             (recur (do ~@fn-body)))
           ~acc-arg)))))

;; NOTE: `clojure.core/into` uses transients when possible. Here they
;; are not used because `bench.clj` performance benchmarking showed
;; noticeable improvements (≈10%) when they were omitted. That
;; performance was done on FHIR resources schemas and data. For other
;; usecases this implementation may not be as efficient.
(defn iter-into
  "Efficient implementation of monadic and dyadic arities of clojure.core/into."
  ([to] to)
  ([to from]
   (if from
     (iter-reduce conj to from)
     to)))

(defn set-diff [set1 set2]
  (let [set1-transient
        (transient set1)

        set1-diffed-transient
        (if (< (count set2) (count set1))
          (iter-reduce (fn [set1* set2-el]
                         (if (get set1* set2-el)
                           (disj! set1* set2-el)
                           set1*))
                       set1-transient
                       set2)
          ;; We iterate over persistent set1 because transients are not
          ;; ^java.lang.Iterable
          (iter-reduce (fn [set1* set1-el]
                         (if (get set2 set1-el)
                           (disj! set1* set1-el)
                           set1*))
                       set1-transient
                       set1))]
    (persistent! set1-diffed-transient)))

(defn string->md5 [^String s]
  (let [md5-digest (doto (java.security.MessageDigest/getInstance "MD5")
                     (.update (.getBytes s)))]
    (format "%032x" (BigInteger. 1 (.digest md5-digest)))))

(defn input-stream->file [^java.io.InputStream input-stream
                          ^java.io.File file
                          & {:keys [create-parents?]}]
  (when create-parents?
    (io/make-parents file))
  (io/copy input-stream file))

(defn unzip! [url dest-dir]
  (with-open [zip-input-stream (java.util.zip.ZipInputStream. (io/input-stream url))]
    (doseq [^java.util.zip.ZipEntry etr (iteration (fn [_] (.getNextEntry zip-input-stream)))]
      (let [entry-name (.getName etr)
            file? (not (str/ends-with? entry-name "/"))]
        (when (and file? (not (str/blank? entry-name)))
          (let [file (io/file dest-dir entry-name)]
            (input-stream->file zip-input-stream file :create-parents? true)))))
    dest-dir))


;; GZIP files don't have entries. They have single payloads. Thus, we
;; don’t iterate over entries here.
(defn gunzip! [^String input ^String output]
  (with-open [rdr (-> input
                      (java.io.FileInputStream.)
                      (java.util.zip.GZIPInputStream.))]
    (spit output (slurp rdr))))


(defn copy-file [src dest]
  (java.nio.file.Files/copy (.toPath (io/file src))
                            (.toPath (io/file dest))
                            ^"[Ljava.nio.file.CopyOption;"
                            (into-array java.nio.file.CopyOption
                                        [(java.nio.file.StandardCopyOption/REPLACE_EXISTING)])))


(defn copy-directory [from to]
  (when (not (.exists to)) (.mkdirs to))
  (doseq [^java.io.File file (.listFiles from)]
    (if (.isDirectory file)
      (copy-directory (.getPath file) (io/file  to (.getName file)))
      (copy-file (.getPath file) (io/file to (.getName file))))))


(defn rmrf [path]
  (let [file (io/file path)]
    (when (.exists file)
      (run! io/delete-file (reverse (file-seq file))))))

(defn update-file
  [file update-fn]
  (let [old-content (when (.exists file)
                      (let [content (slurp file)]
                        (when (not-empty content)
                          (read-string (slurp file)))))
        new-content (update-fn old-content)]
    (spit file new-content)
    new-content))


(def ^{:doc "Current working directory."
       :dynamic true}
  *cwd* (.getCanonicalFile (io/file ".")))