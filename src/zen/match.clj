(ns zen.match
  (:refer-clojure :exclude [assert])
  (:require
   [clojure.pprint]
   [clojure.string :as str]))

(defn smart-explain-data [p x]
  (cond
    (and (string? x) (instance? java.util.regex.Pattern p))
    (when-not (re-find p x)
      {:expected (str "match regexp: " p) :but x})

    (fn? p)
    (when-not (p x)
      {:expected (pr-str p) :but x})

    :else (when-not (= p x)
            {:expected p :but x})))

(declare match)

(defn one-of [errors path data values]
  (if (not-any? (partial = data) values)
    (let [expected `(zen.match/one-of ~values)]
     (conj errors {:message (str "Expected " (pr-str expected) " but " (pr-str data))
                   :expected expected
                   :but data
                   :path path}))
    errors))

(defn match-fn [errors path data [fn-name & args]]
  (if (= fn-name 'zen.match/one-of)
    (apply one-of errors path data args)
    errors))

(defn- match-recur [errors path x pattern]
  (cond
    (and (map? x) (map? pattern))
    (reduce (fn [errors [k v]]
              (let [path (conj path k)
                    ev   (get x k)]
                (match-recur errors path ev v)))
            errors
            pattern)

    (and (list? pattern) (qualified-symbol? (first pattern)))
    (match-fn errors path x pattern)

    (and (sequential? pattern)
         (sequential? x))
    (reduce (fn [errors [k v]]
              (let [path (conj path k)
                    ev   (nth (vec x) k nil)]
                (match-recur errors path ev v)))
            errors
            (map (fn [x i] [i x]) pattern (range)))

    (and (set? pattern)
         (sequential? x))
    (->> pattern
         (reduce (fn [errors pat]
                   (if (->> x (filter (fn [x'] (empty? (match x' pat)))) first)
                     errors
                     (conj errors
                           {:message (str "Expected " (pr-str x) " contains " (pr-str pat))
                            :expected pat
                            :but x
                            :path path})))
                 errors))

    :else (let [err (smart-explain-data pattern x)]
            (if err
              (conj errors (assoc err :path path))
              errors))))

(defn match
  "Match against each pattern"
  [x pat]
  (match-recur []  [] x pat))


