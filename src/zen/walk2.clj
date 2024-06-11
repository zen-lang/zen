(ns zen.walk2)

(defprotocol ^{:added "1.6"} Walkable
  (^{:added "1.6"} walkt [coll f]
    "If coll is a collection, applies f to each element of the collection
  and returns a collection of the results, of the same type and order
  as coll. If coll is not a collection, returns it unchanged. \"Same
  type\" means a type with the same behavior. For example, a hash-map
  may be returned as an array-map, but a a sorted-map will be returned
  as a sorted-map with the same comparator."))

;; From http://clojure.org/protocols :
;;
;; "You can implement a protocol on an interface ... if one interface
;; is derived from the other, the more derived is used, else which one
;; is used is unspecified."

(extend-protocol Walkable
  nil
  (walkt [coll f] nil)
  java.lang.Object  ; default: not a collection
  (walkt [x f] x)
  clojure.lang.IMapEntry
  (walkt [coll f]
    (clojure.lang.MapEntry. (f (.key coll)) (f (.val coll))))
  clojure.lang.ISeq  ; generic sequence fallback
  (walkt [coll f]
    (map f coll))
  clojure.lang.PersistentList  ; special case to preserve type
  (walkt [coll f]
    (apply list (map f coll)))
  clojure.lang.PersistentList$EmptyList  ; special case to preserve type
  (walkt [coll f] '())
  clojure.lang.IRecord  ; any defrecord
  (walkt [coll f]
    (reduce (fn [r x] (conj r (f x))) coll coll)))

(defn- walkt-transient [coll f]
  ;; Note: `transient` discards metadata as of Clojure 1.6.0
  (persistent!
   (reduce (fn [r x] (conj! r (f x))) (transient (empty coll)) coll)))

;; Persistent collections that support transients
(doseq [type [clojure.lang.PersistentArrayMap
              clojure.lang.PersistentHashMap
              clojure.lang.PersistentHashSet
              clojure.lang.PersistentVector]]
  (extend type Walkable {:walkt walkt-transient}))

(defn- walkt-default [coll f]
  (reduce (fn [r x] (conj r (f x))) (empty coll) coll))

;; Persistent collections that don't support transients
(doseq [type [clojure.lang.PersistentQueue
              clojure.lang.PersistentStructMap
              clojure.lang.PersistentTreeMap
              clojure.lang.PersistentTreeSet]]
  (extend type Walkable {:walkt walkt-default}))

(defn walk
  "Traverses form, an arbitrary data structure.  inner and outer are
  functions.  Applies inner to each element of form, building up a
  data structure of the same type, then applies outer to the result.
  Recognizes all Clojure data structures. Consumes seqs as with doall."
  {:added "1.1"}
  [inner outer form]
  (outer (walkt form inner)))

(defn postwalk
  "Performs a depth-first, post-order traversal of form.  Calls f on
  each sub-form, uses f's return value in place of the original.
  Recognizes all Clojure data structures. Consumes seqs as with doall."
  {:added "1.1"}
  [f form]
  (walk (partial postwalk f) f form))

(defn prewalk
  "Like postwalk, but does pre-order traversal."
  {:added "1.1"}
  [f form]
  (walk (partial prewalk f) identity (f form)))


(defn postwalk-demo
  "Demonstrates the behavior of postwalk by printing each form as it is
  walked.  Returns form."
  {:added "1.1"}
  [form]
  (postwalk (fn [x] (print "Walked: ") (prn x) x) form))

(defn prewalk-demo
  "Demonstrates the behavior of prewalk by printing each form as it is
  walked.  Returns form."
  {:added "1.1"}
  [form]
  (prewalk (fn [x] (print "Walked: ") (prn x) x) form))

(defn keywordize-keys
  "Recursively transforms all map keys from strings to keywords."
  {:added "1.1"}
  [m]
  (let [f (fn [[k v]] (if (string? k) [(keyword k) v] [k v]))]
    ;; only apply to maps
    (postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))

(defn stringify-keys
  "Recursively transforms all map keys from keywords to strings."
  {:added "1.1"}
  [m]
  (let [f (fn [[k v]] (if (keyword? k) [(name k) v] [k v]))]
    ;; only apply to maps
    (postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))

(defn prewalk-replace
  "Recursively transforms form by replacing keys in smap with their
  values.  Like clojure/replace but works on any data structure.  Does
  replacement at the root of the tree first."
  {:added "1.1"}
  [smap form]
  (prewalk (fn [x] (if (contains? smap x) (smap x) x)) form))

(defn postwalk-replace
  "Recursively transforms form by replacing keys in smap with their
  values.  Like clojure/replace but works on any data structure.  Does
  replacement at the leaves of the tree first."
  {:added "1.1"}
  [smap form]
  (postwalk (fn [x] (if (contains? smap x) (smap x) x)) form))

(defn macroexpand-all
  "Recursively performs all possible macroexpansions in form. For
  development use only: This function does not perfectly emulate
  macroexpansion by the Clojure compiler because it doesn't understand
  special forms like 'let'."
  {:added "1.1"}
  [form]
  (prewalk (fn [x] (if (seq? x) (macroexpand x) x)) form))
