(ns zen.v2-validation
  (:require
   [zen.validation.utils :refer :all]
   [zen.effect]
   [zen.match]
   [clojure.set :as cljset]
   [clojure.string :as str]
   [zen.utils :as utils]))

(def fhir-date-regex
  (re-pattern
   "([0-9]([0-9]([0-9][1-9]|[1-9]0)|[1-9]00)|[1-9]000)(-(0[1-9]|1[0-2])(-(0[1-9]|[1-2][0-9]|3[0-1]))?)?"))

(def fhir-datetime-regex
  (re-pattern
   "([0-9]([0-9]([0-9][1-9]|[1-9]0)|[1-9]00)|[1-9]000)(-(0[1-9]|1[0-2])(-(0[1-9]|[1-2][0-9]|3[0-1])(T([01][0-9]|2[0-3]):[0-5][0-9]:([0-5][0-9]|60)(\\.[0-9]+)?(Z|(\\+|-)((0[0-9]|1[0-3]):[0-5][0-9]|14:00)))?)?)?"))

(def types-cfg
  {'zen/string {:fn string?
                :to-str "string"}

   'zen/date
   {:fn #(and (string? %) (re-matches fhir-date-regex %))
    :to-str "date"}

   'zen/datetime
   {:fn #(and (string? %) (re-matches fhir-datetime-regex %))
    :to-str "datetime"}

   'zen/number {:fn number?
                :to-str "number"}

   ;; TODO discuss sequential? predicate
   'zen/set {:fn #(or (set? %) (sequential? %))
             :to-str "set"}

   'zen/map {:fn map?
             :to-str "map"}

   'zen/vector {:fn vector?
                :to-str "vector"}

   'zen/boolean {:fn boolean?
                 :to-str "boolean"}

   'zen/keyword {:fn keyword?
                 :to-str "keyword"}

   'zen/list {:fn list?
              :to-str "list"}

   'zen/integer {:fn integer?
                 :to-str "integer"}

   'zen/symbol {:fn symbol?
                :to-str "symbol"}

   'zen/any (constantly true)
   'zen/case (constantly true)

   ;; fn is implemented as a separate multimethod
   'zen/apply {:to-str "apply"}

   'zen/regex
   {:fn #(and (string? %) (re-pattern %))
    :to-str "regex"}})

(def add-err (partial add-err* types-cfg))

(defmulti compile-key (fn [k ztx kfg] k))

(defmulti compile-type-check (fn [tp ztx] tp))

(defn validate-props [vtx data props opts]
  (reduce (fn [vtx* prop]
            (if-let [prop-value (get data prop)]
              (-> (node-vtx&log vtx* [:property prop] [prop])
                  ((get props prop) prop-value opts)
                  (merge-vtx vtx*))
              vtx*))
          vtx
          (keys props)))

(defn cur-keyset [vtx data]
  (->> (keys data)
       (map #(conj (:path vtx) %))
       set))

(defn valtype-rule [vtx data open-world?] #_"NOTE: maybe refactor name to 'set-unknown-keys ?"
  (let [filter-allowed
        (fn [unknown]
          (->> unknown
               (remove #(= (vec (butlast %)) (:path vtx)))
               set))

        set-unknown
        (fn [unknown]
          (let [empty-unknown? (empty? unknown)
                empty-visited? (empty? (:visited vtx))]
            (cond (and empty-unknown? (not empty-visited?))
                  (cljset/difference (cur-keyset vtx data)
                                     (:visited vtx))

                  (and empty-unknown? empty-visited?)
                  (set (cur-keyset vtx data))

                  (not empty-unknown?)
                  (cljset/difference unknown (:visited vtx)))))]

    (if open-world?
      (-> vtx
          (update :unknown-keys filter-allowed)
          (update :visited into (cur-keyset vtx data)))
      (update vtx :unknown-keys set-unknown))))

(defn compile-schema [ztx schema props]
  (let [rulesets (->> (dissoc schema :validation-type)
                      (map (fn [[k kfg]]
                             (compile-key k ztx kfg)))
                      doall)
        open-world? (or (:key schema)
                        (:values schema)
                        (= (:validation-type schema) :open)
                        (= (:type schema) 'zen/any))]
    (fn compiled-sch [vtx data opts]
      (loop [rs rulesets #_"NOTE: refactor to reduce?"
             vtx* (validate-props (assoc vtx :type (:type schema)) data props opts)]
        (cond
          (and (empty? rs) (map? data) (:type schema)) #_"NOTE: why not (= 'zen/map (:type schema)) ?"
          (valtype-rule vtx* data open-world?)

          (empty? rs) vtx*

          :else
          (let [{when-fn :when rule-fn :rule} (first rs)
                when-fn (or when-fn (constantly true))]
            (if (when-fn data)
              (recur (rest rs) (rule-fn vtx* data opts))
              (recur (rest rs) vtx*))))))))

(declare resolve-props)

(defn get-cached
  [ztx schema init?]
  (let [hash* (hash schema)
        v (get-in @ztx [::compiled-schemas hash*])]
    (cond
      (fn? v)
      v

      (true? (get-in @ztx [::in-compile hash*]))
      (do
        (swap! ztx update ::in-compile dissoc hash*)
        (fn [vtx data opts]
          ;; TODO add to vtx :warning
          (let [v (get-in @ztx [::compiled-schemas hash*])]
            (v vtx data opts))))

      :else
      (do
        (swap! ztx assoc-in [::in-compile hash*] true)
        (let [props
              (if init?
                (resolve-props ztx)
                (::prop-schemas @ztx))

              v (compile-schema ztx schema props)]

          (swap! ztx update ::in-compile dissoc hash*)
          (swap! ztx assoc-in [::compiled-schemas hash*] v)
          v)))))

(defn resolve-props [ztx]
  (->> (utils/get-tag ztx 'zen/property)
       (map (fn [prop]
              (zen.utils/get-symbol ztx prop)))
       (map (fn [sch]
              [sch (get-cached ztx sch false)]))
       (reduce (fn [acc [sch v]]
                 (assoc acc (keyword (:zen/name sch)) v))
               {})
       (swap! ztx assoc ::prop-schemas)
       ::prop-schemas))

(defn *validate-schema
  "internal, use validate function"
  [ztx vtx schema data {:keys [sch-symbol] :as opts}]
  (let [vtx (-> vtx
                (assoc :schema [(or sch-symbol (:zen/name schema))])
                (assoc :path [])
                (assoc-in [::confirmed [] (:zen/name schema)] true))

        compiled-schema-fn (get-cached ztx schema true)]

    (compiled-schema-fn vtx data opts)))

(defn validate-schema [ztx schema data & [opts]]
  (let [empty-vtx {:errors []
                   :warnings []
                   :visited #{}
                   :unknown-keys #{}
                   :effects []}]
    (-> ztx
        (*validate-schema empty-vtx schema data opts)
        (unknown-errs))))

(defn validate [ztx schemas data & [opts]]
  (loop [schemas (seq schemas)
         vtx {:errors []
              :warnings []
              :visited #{}
              :unknown-keys #{}
              :effects []}]
    (if (empty? schemas)
      (dissoc (unknown-errs vtx) :visited :unknown-keys ::confirmed)
      (if-let [schema (utils/get-symbol ztx (first schemas))]
        (if (true? (get-in vtx [::confirmed [] (first schemas)]))
          (recur (rest schemas) vtx)
          (recur (rest schemas)
                 (*validate-schema ztx vtx schema data (assoc opts :sch-symbol (first schemas)))))
        (recur (rest schemas)
               (update vtx :errors conj
                       {:message (str "Could not resolve schema '" (first schemas))
                        :type "schema"}))))))

(defn type-fn [sym]
  (let [type-cfg (get types-cfg sym)
        type-pred (if (fn? type-cfg) type-cfg (:fn type-cfg))]
    (fn [vtx data _]
      (if (type-pred data)
        vtx
        (let [error-msg
              {:message (str "Expected type of '" (or (:to-str type-cfg) sym)
                             ", got '" (pretty-type data))}]
          (add-err vtx :type error-msg))))))

(defmethod compile-type-check 'zen/string [_ _] (type-fn 'zen/string))
(defmethod compile-type-check 'zen/number [_ _] (type-fn 'zen/number))
(defmethod compile-type-check 'zen/set [_ _] (type-fn 'zen/set))
(defmethod compile-type-check 'zen/map [_ _] (type-fn 'zen/map))
(defmethod compile-type-check 'zen/vector [_ _] (type-fn 'zen/vector))
(defmethod compile-type-check 'zen/boolean [_ _] (type-fn 'zen/boolean))
(defmethod compile-type-check 'zen/list [_ _] (type-fn 'zen/list))
(defmethod compile-type-check 'zen/keyword [_ _] (type-fn 'zen/keyword))
(defmethod compile-type-check 'zen/any [_ _] (type-fn 'zen/any))
(defmethod compile-type-check 'zen/integer [_ _] (type-fn 'zen/integer))
(defmethod compile-type-check 'zen/symbol [_ _] (type-fn 'zen/symbol))
(defmethod compile-type-check 'zen/regex [_ _] (type-fn 'zen/regex))
(defmethod compile-type-check 'zen/case [_ _] (type-fn 'zen/case))
(defmethod compile-type-check 'zen/date [_ _] (type-fn 'zen/date))
(defmethod compile-type-check 'zen/datetime [_ _] (type-fn 'zen/datetime))

(defmethod compile-type-check :default
  [tp ztx]
  (fn [vtx data opts]
    (add-err vtx :type {:message (format "No validate-type multimethod for '%s" tp)})))

(defmethod compile-type-check 'zen/apply
  [tp ztx]
  (fn [vtx data opts]
    (cond
      (not (list? data))
      (add-err vtx :type {:message (str "Expected fn call '(fn-name args-1 arg-2), got '"
                                        (pretty-type data))})

      (not (symbol? (nth data 0)))
      (add-err vtx :apply {:message (str "Expected symbol, got '" (first data))
                           :type "apply.fn-name"})

      :else
      (let [sch-sym (nth data 0)
            {:keys [zen/tags args] :as sch} (utils/get-symbol ztx sch-sym)]
        (cond
          (nil? sch)
          (add-err vtx :apply {:message (str "Could not resolve fn '" sch-sym)
                               :type "apply.fn-name"})

          (not (contains? tags 'zen/fn))
          (add-err vtx :apply {:message (format "fn definition '%s should be tagged with 'zen/fn, but '%s" sch-sym tags)
                               :type "apply.fn-tag"})

          :else
          (let [v (get-cached ztx args false)]
            (-> (node-vtx vtx [sch-sym :args])
                (v (rest data) opts)
                (merge-vtx vtx))))))))

(defmethod compile-key :type
  [_ ztx tp]
  {:rule (compile-type-check tp ztx)})

(defmethod compile-key :case
  [_ ztx cases]
  (let [vs (doall
            (map (fn [{:keys [when then]}]
                   (cond-> {:when (get-cached ztx when false)}
                     (not-empty then) (assoc :then (get-cached ztx then false))))
                 cases))]
    {:rule
     (fn [vtx data opts]
       (loop [[{wh :when th :then :as v} & rest] vs
              item-idx 0]
         (if (nil? v)
           (add-err vtx
                    :case
                    {:message (format "Expected one of the cases to be true") :type "case"})
           (let [vtx* (wh (node-vtx vtx [:case item-idx :when]) data opts)]
             (cond
               (and (empty? (:errors vtx*)) th)
               (-> (merge-vtx vtx* vtx)
                   (node-vtx [:case item-idx :then])
                   (th data opts)
                   (merge-vtx vtx))

               (empty? (:errors vtx*)) (merge-vtx vtx vtx*)

               :else (recur rest (inc item-idx)))))))}))

(defmethod compile-key :enum
  [_ ztx values]
  (let [values* (set (map :value values))]
    {:rule
     (fn [vtx data opts]
       (if-not (contains? values* data)
         (add-err vtx :enum {:message (str "Expected '" data "' in " values*) :type "enum"})
         vtx))}))

(defmethod compile-key :match
  [_ ztx pattern]
  {:rule
   (fn match-fn [vtx data opts]
     (let [errs (zen.match/match data pattern)]
       (if-not (empty? errs)
         (->> errs
              (reduce (fn [acc err]
                        (let [err-msg
                              (or (:message err)
                                  (str "Expected " (pr-str (:expected err)) ", got " (pr-str (:but err))))]
                          (apply add-err (into [acc :match {:message err-msg :type "match"}]
                                               (:path err)))))
                      vtx))
         vtx)))})

(defmethod compile-key :scale
  [_ ztx scale]
  {:when number?
   :rule
   (fn [vtx num opts]
     (let [dc (BigDecimal/valueOf num)
           num-scale (.scale dc)
           fraction (.remainder dc BigDecimal/ONE)]
       (if (or (= num-scale scale)
               (and (zero? fraction)
                    (= num-scale 1)
                    (> scale 1)))
         vtx
         (add-err vtx :scale
                  {:message (str "Expected scale = " scale ", got " (.scale dc))}))))})

(defmethod compile-key :precision
  [_ ztx precision]
  {:when number?
   :rule
   (fn [vtx num opts]
     (let [dc (BigDecimal/valueOf num)
           num-precision (.precision dc)
           fraction (.remainder dc BigDecimal/ONE)]
       (if (or (= num-precision precision)
               (and (zero? fraction)
                    (= (.scale dc) 1)
                    (< num-precision precision)))
         vtx
         (add-err vtx :precision
                  {:message (str "Expected precision = " precision ", got " num-precision)}))))})

(defmethod compile-key :min
  [_ ztx min]
  {:when number?
   :rule
   (fn [vtx data opts]
     (if (< data min)
       (add-err vtx :min {:message (str "Expected >= " min ", got " data)})
       vtx))})

(defmethod compile-key :max
  [_ ztx max]
  {:when number?
   :rule
   (fn [vtx data opts]
     (if (> data max)
       (add-err vtx :max {:message (str "Expected <= " max ", got " data)})
       vtx))})

(defmethod compile-key :minLength
  [_ ztx min-len]
  {:when string?
   :rule
   (fn [vtx data opts]
     (if (< (count data) min-len)
       (add-err vtx
                :minLength
                {:message (str "Expected length >= " min-len ", got " (count data))})
       vtx))})

(defmethod compile-key :maxLength
  [_ ztx max-len]
  {:when string?
   :rule
   (fn [vtx data opts]
     (if (> (count data) max-len)
       (add-err vtx
                :maxLength
                {:message (str "Expected length <= " max-len ", got " (count data))})
       vtx))})

(defmethod compile-key :minItems
  [_ ztx items-count]
  {:when #(or (sequential? %) (set? %))
   :rule
   (fn [vtx data opts]
     (if (< (count data) items-count)
       (add-err vtx
                :minItems
                {:message (str "Expected >= " items-count ", got " (count data))})
       vtx))})

(defmethod compile-key :maxItems
  [_ ztx items-count]
  {:when #(or (sequential? %) (set? %))
   :rule
   (fn [vtx data opts]
     (if (> (count data) items-count)
       (add-err vtx
                :maxItems
                {:message (str "Expected <= " items-count ", got " (count data))})
       vtx))})

(defmethod compile-key :const
  [_ ztx {:keys [value]}]
  {:rule
   (fn [vtx data opts]
     (if (not= value data)
       (add-err vtx :const
                {:message (str "Expected '" value "', got '" data "'")
                 :type "schema"})
       vtx))})

(defmethod compile-key :keys
  [_ ztx ks]
  (let [key-rules
        (->> ks
             (map (fn [[k sch]]
                    [k (get-cached ztx sch false)]))
             (into {}))]
    {:when map?
     :rule

     (fn keys-sch [vtx data opts]

       (loop [data (seq data)
              unknown (transient [])
              vtx* vtx]
         (if (empty? data)
           (update vtx* :unknown-keys into (persistent! unknown))
           (let [[k v] (first data)]
             (if (not (contains? key-rules k))
               (recur (rest data) (conj! unknown (conj (:path vtx) k)) vtx*)
               (recur (rest data)
                      unknown
                      (-> (node-vtx&log vtx* [k] [k])
                          ((get key-rules k) v opts)
                          (merge-vtx vtx*))))))))}))

(defmethod compile-key :values
  [_ ztx sch]
  (let [v (get-cached ztx sch false)]
    {:when map?
     :rule
     (fn [vtx data opts]
       (reduce (fn [vtx* [key value]]
                 (-> (node-vtx&log vtx* [:values] [key])
                     (v value opts)
                     (merge-vtx vtx*)))
               vtx
               data))}))

(defmethod compile-key :every
  [_ ztx sch]
  (let [v (get-cached ztx sch false)]
    {:when #(or (sequential? %) (set? %))
     :rule
     (fn [vtx data opts]
       (let [err-fn
             (fn [vtx [idx item]]
               (-> (node-vtx vtx [:every idx] [idx])
                   (v item (dissoc opts :indices))
                   (merge-vtx vtx)))

             data*
             (if-let [indices (not-empty (:indices opts))]
               (map vector indices data)
               (map-indexed vector data))]

         (reduce err-fn vtx data*)))}))

(defmethod compile-key :subset-of
  [_ ztx superset]
  {:when set?
   :rule
   (fn [vtx data opts]
     (if-not (clojure.set/subset? data superset)
       (add-err vtx :subset-of {:type "set"})
       vtx))})

(defmethod compile-key :superset-of
  [_ ztx subset]
  {:when set?
   :rule
   (fn [vtx data opts]
     (if-not (clojure.set/subset? subset data)
       (add-err vtx :superset-of {:type "set"})
       vtx))})

(defmethod compile-key :regex
  [_ ztx regex]
  {:when string?
   :rule
   (fn [vtx data opts]
     (if (not (re-find (re-pattern regex) data))
       (add-err vtx :regex
                {:message (str "Expected match /" (str regex) "/, got \"" data "\"")})
       vtx))})

(defmethod compile-key :confirms
  [_ ztx ks]
  (let [compile-confirms
        (fn [sym]
          (if-let [sch (utils/get-symbol ztx sym)]
            [sym (:zen/name sch) (get-cached ztx sch false)]
            [sym]))

        comp-fns
        (->> ks
             (map compile-confirms)
             doall)]
    {:rule
     (fn confirms-sch [vtx data opts]
       (loop [comp-fns comp-fns
              vtx* vtx]
         (if (empty? comp-fns)
           vtx*
           (let [[sym sch-nm v] (first comp-fns)]
             (cond
               (true? (get-in vtx* [::confirmed (:path vtx*) sch-nm]))
               (recur (rest comp-fns) vtx*)

               (fn? v)
               (recur (rest comp-fns)
                      (-> (assoc-in vtx* [::confirmed (:path vtx*) sch-nm] true)
                          (node-vtx [:confirms sch-nm])
                          (v data opts)
                          (merge-vtx vtx*)))

               :else
               (recur (rest comp-fns)
                      (add-err vtx* :confirms {:message (str "Could not resolve schema '" sym)})))))))}))

(defmethod compile-key :require
  [_ ztx ks]
  ;; TODO decide if require should add to :visited keys vector
  (let [one-of-fn
        (fn [vtx data s]
          (let [reqs (->> (select-keys data s) (remove nil?))]
            (if (empty? reqs)
              (add-err vtx :require {:type "map.require"
                                     :message (str "one of keys " s " is required")})
              vtx)))]
    {:when map?
     :rule
     (fn [vtx data opts]
       (reduce (fn [vtx* k]
                 (cond
                   (set? k) (one-of-fn vtx* data k)
                   (contains? data k) vtx*
                   :else
                   (add-err vtx* :require {:type "require" :message (str k " is required")} k)))
               vtx
               ks))}))

(defmethod compile-key :schema-key
  [_ ztx {sk :key sk-ns :ns sk-tags :tags}]
  {:when map?
   :rule
   (fn [vtx data opts]
     (if-let [sch-nm (get data sk)]
       (let [sch-symbol (if sk-ns (symbol sk-ns (name sch-nm)) (symbol sch-nm))
             {tags :zen/tags :as sch} (utils/get-symbol ztx sch-symbol)]
         (cond
           (nil? sch)
           (add-err vtx :schema-key
                    {:message (str "Could not find schema " sch-symbol)
                     :type "schema"})

           (not (contains? tags 'zen/schema))
           (add-err vtx :schema-key
                    {:message (str "'" sch-symbol " should be tagged with zen/schema, but " tags)
                     :type "schema"})

           (and sk-tags (not (clojure.set/subset? sk-tags tags)))
           (add-err vtx :schema-key
                    {:message (str "'" sch-symbol " should be tagged with " sk-tags ", but " tags)
                     :type "schema"})

           :else
           (let [v (get-cached ztx sch false)]
             (-> (node-vtx vtx [:schema-key sch-symbol])
                 (v data opts)
                 (merge-vtx vtx)))))
       vtx))})

(defmethod compile-key :schema-index
  [_ ztx {si :index si-ns :ns}]
  {:when sequential?
   :rule
   (fn [vtx data opts]
     (if-let [sch-nm (or (get data si) (nth data si))]
       (let [sch-symbol (if si-ns (symbol si-ns (name sch-nm)) sch-nm)
             sch (utils/get-symbol ztx sch-symbol)]
         (cond
           (nil? sch)
           (add-err vtx
                    :schema-index
                    {:message (format "Could not find schema %s" sch-symbol)
                     :type "schema"})

           :else
           (let [v (get-cached ztx sch false)]
             (-> (node-vtx vtx [:schema-index sch-symbol])
                 (v data opts)
                 (merge-vtx vtx)))))
       vtx))})

(defmethod compile-key :nth
  [_ ztx cfg]
  (let [schemas (doall
                 (map (fn [[index v]] [index (get-cached ztx v false)])
                      cfg))]
    {:when sequential?
     :rule
     (fn [vtx data opts]
       (reduce (fn [vtx* [index v]]
                 (if-let [nth-el (nth data index)]
                   (-> (node-vtx vtx* [:nth index] [index])
                       (v nth-el opts)
                       (merge-vtx vtx*))
                   vtx*))
               vtx
               schemas))}))

(defmethod compile-key :keyname-schemas
  [_ ztx {:keys [tags]}]
  {:when map?
   :rule
   (fn [vtx data opts]
     (let [rule-fn
           (fn [vtx* [schema-key data*]]
             (if-let [sch (and (qualified-ident? schema-key) (utils/get-symbol ztx (symbol schema-key)))]
                ;; TODO add test on nil case
               (if (or (nil? tags)
                       (clojure.set/subset? tags (:zen/tags sch)))
                 (-> (node-vtx&log vtx* [:keyname-schemas schema-key] [schema-key])
                     ((get-cached ztx sch false) data* opts)
                     (merge-vtx vtx*))
                 vtx*)
               vtx*))]
       (reduce rule-fn vtx data)))})

(defmethod compile-key :default [schema-key ztx sch-params]
  (cond
    (qualified-ident? schema-key)
    (let [{:keys [zen/tags] :as sch} (utils/get-symbol ztx (symbol schema-key))] #_"NOTE: `:keys [zen/tags]` does it work? Is it used?"
      {:rule
       (fn [vtx data opts]
         (cond
           (contains? tags 'zen/schema-fx)
           (add-fx vtx (:zen/name sch)
                   {:name (:zen/name sch)
                    :params sch-params
                    :data data})

           :else vtx))})

    :else {:rule (fn [vtx data opts] vtx)}))

(defn is-exclusive? [group data]
  (->> group
       (filter #(->> (if (set? %) % #{%})
                     (select-keys data)
                     seq))
       (bounded-count 2)
       (> 2)))

(defmethod compile-key :exclusive-keys
  [_ ztx groups]
  (let [err-fn
        (fn [group [vtx data]]
          (if (is-exclusive? group data)
            (list vtx data)
            (let [err-msg
                  (format "Expected only one of keyset %s, but present %s"
                          (str/join " or " group)
                          (keys data))
                  vtx*
                  (add-err vtx :exclusive-keys
                           {:message err-msg
                            :type "map.exclusive-keys"})]
              (list vtx* data))))

        comp-fn
        (->> groups
             (map #(partial err-fn %))
             (apply comp))]

    {:rule
     (fn [vtx data opts]
       (-> (list vtx data)
           comp-fn
           (nth 0)))}))

(defmethod compile-key :key
  [_ ztx sch]
  (let [v (get-cached ztx sch false)]
    {:when map?
     :rule
     (fn [vtx data opts]
       (reduce (fn [vtx* [k _]]
                 (-> (node-vtx&log vtx* [:key] [k])
                     (v k opts)
                     (merge-vtx vtx*)))
               vtx
               data))}))

(defmethod compile-key :tags
  [_ ztx sch-tags]
  ;; currently :tags implements three usecases:
  ;; tags check where schema name is string or symbol
  ;; and zen.apply tags check (list notation)
  {:when #(or (symbol? %)
              (list? %)
              (string? %))
   :rule
   (fn [vtx data opts]
     (let [[sym type-err]
           (cond
             (list? data) [(nth data 0) "apply.fn-tag"]
             (string? data) [(symbol data) "string"]
             (symbol? data) [data "symbol"])
           {:keys [zen/tags] :as sch} (utils/get-symbol ztx sym)]
       (if (not (clojure.set/superset? tags sch-tags))
         (add-err vtx :tags
                  {:message
                   (cond
                     (nil? sch) (format "No symbol '%s found" sym)
                     :else
                     (format "Expected symbol '%s tagged with '%s, but only %s"
                             (str sym) (str sch-tags) (or tags #{})))
                   :type type-err})
         vtx)))})

(defn slice-fn [ztx [slice-name slice-schema]]
  (let [eng (get-in slice-schema [:filter :engine])]
    ;; TODO add error if engine is not found?
    (cond
      (= eng :zen)
      (let [v (get-cached ztx (get-in slice-schema [:filter :zen]) false)]
        (fn [vtx [idx el] opts]
          (let [vtx* (v (node-vtx vtx [:slicing] [idx]) el opts)]
            (when (empty? (:errors vtx*))
              slice-name))))

      (= eng :match)
      (fn [vtx [idx el] opts]
        (let [errs
              (->> (get-in slice-schema [:filter :match])
                   (zen.match/match el))]
          (when (empty? errs)
            slice-name)))

      (= eng :zen-fx)
      (let [v (get-cached ztx (get-in slice-schema [:filter :zen]) false)]
        (fn [vtx [idx el] opts]
          (let [vtx* (v (node-vtx vtx [:slicing] [idx]) el opts)
                effect-errs (zen.effect/apply-fx ztx vtx* el)]
            (when (empty? (:errors effect-errs))
              slice-name)))))))

(defn err-fn [schemas rest-fn opts vtx [slice-name slice]]
  (if (and (= slice-name :slicing/rest) (nil? rest-fn))
    vtx
    (let [v (if (= slice-name :slicing/rest)
              rest-fn
              (get schemas slice-name))]
      (-> (node-vtx vtx [:slicing slice-name])
          (v (mapv #(nth % 1) slice) (assoc opts :indices (map #(nth % 0) slice)))
          (merge-vtx vtx)))))

(defmethod compile-key :slicing
  [_ ztx {slices :slices rest-schema :rest}]
  (let [schemas
        (->> slices
             (map (fn [[slice-name {:keys [schema]}]]
                    [slice-name (get-cached ztx schema false)]))
             (into {}))

        rest-fn
        (when (not-empty rest-schema)
          (get-cached ztx rest-schema false))

        slice-fns (map (partial slice-fn ztx) slices)

        slices-templ
        (->> slices
             (map (fn [[slice-name _]]
                    [slice-name []]))
             (into {}))]

    {:when sequential?
     :rule
     (fn slicing-sch [vtx data opts]
       (->> data
            (map-indexed vector)
            (group-by (fn [indexed-el]
                        (or (some #(apply % [vtx indexed-el opts]) slice-fns)
                            :slicing/rest)))
            (merge slices-templ)
            (reduce (partial err-fn schemas rest-fn opts) vtx)))}))

(defmethod compile-key :fail
  [_ ztx err-msg]
  {:rule
   (fn fail-fn [vtx data opts]
     (add-err vtx :fail {:message err-msg}))})

(defmethod compile-key :key-schema
  [_ ztx {:keys [tags key]}]
  (let [keys-schemas
        (->> tags
             (mapcat #(utils/get-tag ztx %))
             (map (fn [sch-name]
                    (let [sch (utils/get-symbol ztx sch-name)]
                      ;; TODO get rid of type coercion
                      [(keyword (name sch-name)) (:for sch) (get-cached ztx sch false)])))
             doall)]
    {:when map?
     :rule
     (fn key-schema-fn [vtx data opts]
       (let [key-rules
             (->> keys-schemas
                  (filter (fn [[sch-key for? v]]
                            (or (nil? for?) (contains? for? (get data key)))))
                  (reduce (fn [acc [sch-key _ v]] (assoc acc sch-key v)) {}))]
         (loop [data (seq data)
                unknown (transient [])
                vtx* vtx]
           (if (empty? data)
             (update vtx* :unknown-keys into (persistent! unknown))
             (let [[k v] (first data)]
               (if (not (contains? key-rules k))
                 (recur (rest data) (conj! unknown (conj (:path vtx) k)) vtx*)
                 (recur (rest data)
                        unknown
                        (-> (node-vtx&log vtx* [k] [k])
                            ((get key-rules k) v opts)
                            (merge-vtx vtx*)))))))))}))
