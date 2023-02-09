(ns zen.v2-validation
  (:require
   [zen.schema]
   [zen.validation.utils
    :refer :all
    :exclude [add-err fhir-date-regex fhir-datetime-regex types-cfg]
    :as validation.utils]
   [zen.effect]
   [zen.match]
   [clojure.set :as set]
   [clojure.string :as str]
   [zen.utils :as utils]))


;; backwards-compatible aliases used in this ns
(def get-cached       zen.schema/get-cached)
(def compile-key      zen.schema/compile-key)
(def add-err   zen.validation.utils/add-err)
(def types-cfg zen.validation.utils/types-cfg)


#_"NOTE: aliases for backwards-compatibility.
Uncomment if something breaks.
Probably safe to remove if no one relies on them"
#_(def resolve-props zen.schema/resolve-props)
#_(def compile-schema zen.schema/compile-schema)
#_(def safe-compile-key zen.schema/safe-compile-key)
#_(def validate-props zen.schema/navigate-props)
#_(def rule-priority zen.schema/rule-priority)
#_(def fhir-date-regex zen.validation.utils/fhir-date-regex)
#_(def fhir-datetime-regex zen.validation.utils/fhir-datetime-regex)


(defn resolve-props [ztx]
  (let [props-syms   (utils/get-tag ztx 'zen/property)
        cached-props (::cached-props @ztx)]
    (if (= cached-props props-syms)
      (::prop-schemas @ztx)
      (->> props-syms
           (map (fn [prop]
                  (zen.utils/get-symbol ztx prop)))
           (map (fn [sch]
                  [sch (get-cached ztx sch false)]))
           (reduce (fn [acc [sch v]]
                     (assoc acc (keyword (:zen/name sch)) v))
                   {})
           (swap! ztx assoc ::cached-pops props-syms, ::prop-schemas)
           ::prop-schemas))))


(defn validate-props [vtx data props opts]
  ;; props is clojure map
  (utils/iter-reduce (fn [vtx* prop-entry]
                       (let [prop (nth prop-entry 0)]
                         (if-let [prop-value (get data prop)]
                           (-> (validation.utils/node-vtx&log vtx* [:property prop] [prop])
                               ((get props prop) prop-value opts)
                               (validation.utils/merge-vtx vtx*))
                           vtx*)))
                     vtx
                     props))


(defn props-pre-process-hook [ztx schema]
  (let [#_"FIXME: won't reeval proprs if they change in run-time"
        props (resolve-props ztx)]
    (fn pre-process-props [vtx data opts]
      (validate-props vtx data props opts))))


(zen.schema/register-schema-pre-process-hook!
  ::validate
  props-pre-process-hook)


(defn valtype-rule [vtx data open-world?] #_"NOTE: maybe refactor name to 'set-unknown-keys ?"
  (let [filter-allowed
        #_"TODO: Revise the performance of this function on appropriate benchmarking fixtures"
        (fn [unknown]
          (->> unknown
               (remove #(= (vec (butlast %)) (:path vtx)))
               set))

        set-unknown
        (fn [unknown]
          (let [empty-unknown? (empty? unknown)
                empty-visited? (empty? (:visited vtx))]
            (cond (and empty-unknown? (not empty-visited?))
                  (utils/set-diff (validation.utils/cur-keyset vtx data)
                                  (:visited vtx))

                  (and empty-unknown? empty-visited?)
                  (set (validation.utils/cur-keyset vtx data))

                  (not empty-unknown?)
                  (utils/set-diff unknown (:visited vtx)))))]

    (if open-world?
      (-> vtx
          (update :unknown-keys filter-allowed)
          (update :visited into (validation.utils/cur-keyset vtx data)))
      (update vtx :unknown-keys set-unknown))))


(defn unknown-keys-post-process-hook [ztx schema]
  (when (and (some? (:type schema))
             (nil? (:validation-type schema)))
    (let [open-world? (or (:key schema)
                          (:values schema)
                          (= (:validation-type schema) :open)
                          (= (:type schema) 'zen/any))]
      (fn post-process-unknown-keys [vtx data opts]
        (when (map? data)
          (valtype-rule vtx data open-world?))))))


(zen.schema/register-schema-post-process-hook!
  ::validate
  unknown-keys-post-process-hook)


(zen.schema/register-compile-key-interpreter!
  [:validation-type ::validate]
  (fn [_ ztx tp]
    (let [open-world? (= :open tp)]
      (fn validate-validation-type [vtx data opts] (valtype-rule vtx data open-world?)))))


(defmulti compile-type-check (fn [tp ztx] tp))


(defn *validate-schema
  "internal, use validate function"
  [ztx vtx schema data {:keys [_sch-symbol] :as opts}]
  (zen.schema/apply-schema ztx vtx schema data (assoc opts :interpreters [::validate])))


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
      (unknown-errs vtx)
      #_(-> (unknown-errs vtx)
            (dissoc :unknown-keys ::confirmed)
            (cond-> (not (:vtx-visited opts)) (dissoc :visited)))
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
    (fn validate-type-sym [vtx data _]
      (let [pth-key (peek (:path vtx))]
        (cond
          ;; TODO fix this when compile-opts are implemented
          (get #{:zen/tags :zen/file :zen/desc :zen/name} pth-key) vtx

          (type-pred data) vtx

          :else
          (let [error-msg
                {:message (str "Expected type of '" (or (:to-str type-cfg) sym)
                               ", got '" (pretty-type data))}]
            (add-err vtx :type error-msg)))))))

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
(defmethod compile-type-check 'zen/qsymbol [_ _] (type-fn 'zen/qsymbol))
(defmethod compile-type-check 'zen/regex [_ _] (type-fn 'zen/regex))
(defmethod compile-type-check 'zen/case [_ _] (type-fn 'zen/case))
(defmethod compile-type-check 'zen/date [_ _] (type-fn 'zen/date))
(defmethod compile-type-check 'zen/datetime [_ _] (type-fn 'zen/datetime))

(defmethod compile-type-check :default
  [tp ztx]
  (fn validate-type-default [vtx data opts]
    (add-err vtx :type {:message (format "No validate-type multimethod for '%s" tp)})))

(defmethod compile-type-check 'zen/apply
  [tp ztx]
  (fn validate-type-zen.apply [vtx data opts]
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
                (v (vec (rest data)) opts)
                (merge-vtx vtx))))))))

(zen.schema/register-compile-key-interpreter!
  [:type ::validate]
  (fn [_ ztx tp] (compile-type-check tp ztx)))

(zen.schema/register-compile-key-interpreter!
  [:case ::validate]
  #_"NOTE: this is a conditional navigation.
           Conditions are taken from ::validate interpreter
           Can't split into independant :zen.schema/navigate and ::validate"
  (fn [_ ztx cases]
    (let [vs (doall
               (map (fn [{:keys [when then]}]
                      (cond-> {:when (get-cached ztx when false)}
                        (not-empty then) (assoc :then (get-cached ztx then false))))
                    cases))]
      (fn validate-case [vtx data opts]
        (loop [[{wh :when th :then :as v} & rest] vs
               item-idx 0
               vtx* vtx
               passed []]
          (cond
            (and (nil? v) (not-empty passed))
            vtx*

            (nil? v)
            (add-err vtx*
                     :case
                     {:message (format "Expected one of the cases to be true") :type "case"})

            :else
            (let [when-vtx (wh (node-vtx vtx* [:case item-idx :when]) data opts)]
              (cond
                (and (empty? (:errors when-vtx)) th)
                (let [merged-vtx (merge-vtx when-vtx vtx*)]
                  (-> merged-vtx
                      (node-vtx [:case item-idx :then])
                      (th data opts)
                      (merge-vtx merged-vtx)))

                (empty? (:errors when-vtx))
                (recur rest (inc item-idx) (merge-vtx when-vtx vtx*) (conj passed v))

                :else (recur rest (inc item-idx) vtx* passed)))))))))

(zen.schema/register-compile-key-interpreter!
  [:enum ::validate]
  (fn [_ ztx values]
    (let [values* (set (map :value values))]
      (fn validate-enum [vtx data opts]
        (if-not (contains? values* data)
          (add-err vtx :enum {:message (str "Expected '" data "' in " values*) :type "enum"})
          vtx)))))

(zen.schema/register-compile-key-interpreter!
  [:match ::validate]
  (fn [_ ztx pattern]
    (fn validate-match [vtx data opts]
      (let [errs (zen.match/match data pattern)]
        (if-not (empty? errs)
          (->> errs
               (reduce (fn [acc err]
                         (let [err-msg
                               (or (:message err)
                                   (str "Expected " (pr-str (:expected err)) ", got " (pr-str (:but err))))]
                           (apply add-err (utils/iter-into [acc :match {:message err-msg :type "match"}]
                                                           (:path err)))))
                       vtx))
          vtx)))))

(zen.schema/register-compile-key-interpreter!
  [:scale ::validate]
  (fn [_ ztx scale]
    (fn validate-scale [vtx num opts]
      (let [dc (bigdec num)
            num-scale (.scale dc)]
        (if (<= num-scale scale)
          vtx
          (add-err vtx :scale
                   {:message (str "Expected scale = " scale ", got " (.scale dc))}))))))

(zen.schema/register-compile-key-interpreter!
  [:precision ::validate]
  (fn [_ ztx precision]
    (fn validate-precision [vtx num opts]
      (let [dc ^BigDecimal (bigdec num)
            num-precision (.precision dc)
            ;; NOTE: fraction will be used when we add composite checking scale + precision
            #_#_fraction (.remainder dc BigDecimal/ONE)]
        (if (<= num-precision precision)
          vtx
          (add-err vtx :precision
                   {:message (str "Expected precision = " precision ", got " num-precision)}))))))

(zen.schema/register-compile-key-interpreter!
  [:min ::validate]
  (fn [_ ztx ^Long min]
    (fn validate-min [vtx ^Long data opts]
      (if (< data min)
        (add-err vtx :min {:message (str "Expected >= " min ", got " data)})
        vtx))))

(zen.schema/register-compile-key-interpreter!
  [:max ::validate]
  (fn [_ ztx ^Long max]
    (fn validate-max [vtx ^Long data opts]
      (if (> data max)
        (add-err vtx :max {:message (str "Expected <= " max ", got " data)})
        vtx))))

(zen.schema/register-compile-key-interpreter!
  [:minLength ::validate]
  (fn [_ ztx ^Long min-len]
    (fn validate-minLength [vtx ^String data opts]
      (if (< (.length data) min-len)
        (add-err vtx
                 :minLength
                 {:message (str "Expected length >= " min-len ", got " (.length data))})
        vtx))))

(zen.schema/register-compile-key-interpreter!
  [:maxLength ::validate]
  (fn [_ ztx ^Long max-len]
    (fn validate-maxLength [vtx ^String data opts]
      (if (> (.length data) max-len)
        (add-err vtx
                 :maxLength
                 {:message (str "Expected length <= " max-len ", got " (.length data))})
        vtx))))

(zen.schema/register-compile-key-interpreter!
  [:minItems ::validate]
  (fn [_ ztx ^Long items-count]
    (fn validate-minItems [vtx data opts]
      (if (< (count data) items-count)
        (add-err vtx
                 :minItems
                 {:message (str "Expected >= " items-count ", got " (count data))})
        vtx))))

(zen.schema/register-compile-key-interpreter!
  [:maxItems ::validate]
  (fn [_ ztx ^Long items-count]
    (fn validate-maxItems [vtx data opts]
      (if (> (count data) items-count)
        (add-err vtx
                 :maxItems
                 {:message (str "Expected <= " items-count ", got " (count data))})
        vtx))))

(zen.schema/register-compile-key-interpreter!
  [:const ::validate]
  (fn [_ ztx {:keys [value]}]
    (fn validate-const [vtx data opts]
      (if (not= value data)
        (add-err vtx :const
                 {:message (str "Expected '" value "', got '" data "'")
                  :type "schema"})
        vtx))))

(zen.schema/register-compile-key-interpreter!
  [:keys ::validate]
  (fn [_ ztx ks]
    (let [known-keys (set (keys ks))]
      (fn validate-keys [vtx data opts]
        (let [data-keys (->> data
                             (utils/iter-reduce (fn [keys-set data-entry]
                                                  (conj! keys-set (nth data-entry 0)))
                                                (transient #{}))
                             (persistent!))
              unknown-keys (utils/set-diff data-keys known-keys)]
          (update vtx
                  :unknown-keys
                  (fn [vtx-unk-keys]
                    (let [path (:path vtx)]
                      (->> unknown-keys
                           (utils/iter-reduce (fn [vtx-unk-keys* unk-key]
                                                (conj! vtx-unk-keys* (conj path unk-key)))
                                              (transient vtx-unk-keys))
                           (persistent!))))))))))

(zen.schema/register-compile-key-interpreter!
  [:subset-of ::validate]
  (fn [_ ztx superset]
    (fn validate-subset-of [vtx data opts]
      (if-not (clojure.set/subset? data superset)
        (add-err vtx :subset-of {:type "set"})
        vtx))))

(zen.schema/register-compile-key-interpreter!
  [:superset-of ::validate]
  (fn [_ ztx subset]
    (fn validate-superset-of [vtx data opts]
      (if-not (clojure.set/subset? subset data)
        (add-err vtx :superset-of {:type "set"})
        vtx))))

(zen.schema/register-compile-key-interpreter!
  [:regex ::validate]
  (fn [_ ztx regex]
    (fn validate-regex [vtx data opts]
      (if (not (re-find (re-pattern regex) data))
        (add-err vtx :regex
                 {:message (str "Expected match /" (str regex) "/, got \"" data "\"")})
        vtx))))

(zen.schema/register-compile-key-interpreter!
  [:require ::validate]
  (fn [_ ztx ks] ;; TODO decide if require should add to :visited keys vector
    (let [one-of-fn
          (fn [vtx data s]
            (let [reqs (->> (select-keys data s) (remove nil?))]
              (if (empty? reqs)
                (add-err vtx :require {:type "map.require"
                                       :message (str "one of keys " s " is required")})
                vtx)))]
      (fn validate-require [vtx data opts]
        (reduce (fn [vtx* k]
                  (cond
                    (set? k) (one-of-fn vtx* data k)
                    (contains? data k) vtx*
                    :else
                    (add-err vtx* :require {:type "require" :message (str k " is required")} k)))
                vtx
                ks)))))

(defn is-exclusive? [group data]
  (->> group
       (filter #(->> (if (set? %) % #{%})
                     (select-keys data)
                     seq))
       (bounded-count 2)
       (> 2)))

(zen.schema/register-compile-key-interpreter!
 [:exclusive-keys ::validate]
 (fn [_ ztx groups]
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
                             :type    "map.exclusive-keys"})]
               (list vtx* data))))

         comp-fn
         (->> groups
              (map #(partial err-fn %))
              (apply comp))]
     (fn validate-exclusive-keys [vtx data opts]
       (-> (list vtx data)
           comp-fn
           (nth 0))))))

(defmethod compile-key :default [schema-key ztx sch-params]
  (cond
    (qualified-ident? schema-key)
    (let [{:keys [zen/tags] :as sch} (utils/get-symbol ztx (symbol schema-key))] #_"NOTE: `:keys [zen/tags]` does it work? Is it used?"
      {:rule
       (fn default-sch [vtx data opts]
         (cond
           (contains? tags 'zen/schema-fx)
           (add-fx vtx (:zen/name sch)
                   {:name (:zen/name sch)
                    :params sch-params
                    :data data})

           :else vtx))})

    :else {:rule (fn default-sch [vtx data opts] vtx)}))

(zen.schema/register-compile-key-interpreter!
 [:tags ::validate]
 (fn [_ ztx sch-tags]
   ;; currently :tags implements three usecases:
   ;; tags check where schema name is string or symbol
   ;; and zen.apply tags check (list notation)
   (fn validate-tags [vtx data opts]
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
         vtx)))))


(defmulti slice-fn (fn [ztx [_slice-name slice-schema]]
                     (get-in slice-schema [:filter :engine])))


(defmethod slice-fn :default [ztx [slice-name slice-schema]]
  (fn [vtx [idx el] opts]
    nil) #_"TODO add error if engine is not found?")


(defmethod slice-fn :zen [ztx [slice-name slice-schema]]
  (let [v (get-cached ztx (get-in slice-schema [:filter :zen]) false)]
    (fn [vtx [idx el] opts]
      (let [vtx* (v (node-vtx vtx [:slicing] [idx]) el opts)]
        (when (empty? (:errors vtx*))
          slice-name)))))


(defmethod slice-fn :match [ztx [slice-name slice-schema]]
  (fn [vtx [idx el] opts]
    (let [errs
          (->> (get-in slice-schema [:filter :match])
               (zen.match/match el))]
      (when (empty? errs)
        slice-name))))


(defmethod slice-fn :zen-fx [ztx [slice-name slice-schema]]
  (let [v (get-cached ztx (get-in slice-schema [:filter :zen]) false)]
        (fn [vtx [idx el] opts]
          (let [vtx* (v (node-vtx vtx [:slicing] [idx]) el opts)
                effect-errs (zen.effect/apply-fx ztx vtx* el)]
            (when (empty? (:errors effect-errs))
              slice-name)))))


(defn err-fn [schemas rest-fn opts vtx [slice-name slice]]
  (if (and (= slice-name :slicing/rest) (nil? rest-fn))
    vtx
    (let [v (if (= slice-name :slicing/rest)
              rest-fn
              (get schemas slice-name))]
      (-> (node-vtx vtx [:slicing slice-name])
          (v (mapv #(nth % 1) slice) (assoc opts :indices (map #(nth % 0) slice)))
          (merge-vtx vtx)))))

#_"NOTE: Navigation and validation should be untied from each other."
(zen.schema/register-compile-key-interpreter!
 [:slicing ::validate]
 (fn [_ ztx {slices :slices rest-schema :rest}]
   (let [schemas
         (->> slices
              (map (fn [[slice-name {:keys [schema]}]]
                     [slice-name (get-cached ztx schema false)]))
              (utils/iter-into {}))

         rest-fn
         (when (not-empty rest-schema)
           (get-cached ztx rest-schema false))

         slice-fns (map (partial slice-fn ztx) slices)

         slices-templ
         (->> slices
              (map (fn [[slice-name _]]
                     [slice-name []]))
              (utils/iter-into {}))]
     (fn validate-slicing [vtx data opts]
       (->> data
            (map-indexed vector)
            (group-by (fn [indexed-el]
                        (or (some #(apply % [vtx indexed-el opts]) slice-fns)
                            :slicing/rest)))
            (merge slices-templ)
            (reduce (partial err-fn schemas rest-fn opts) vtx))))))

(zen.schema/register-compile-key-interpreter!
  [:fail ::validate]
  (fn [_ ztx err-msg]
    (fn validate-fail [vtx data opts]
      (add-err vtx :fail {:message err-msg}))))

(zen.schema/register-compile-key-interpreter!
 [:key-schema ::validate]
 (fn [_ ztx {:keys [tags key]}]
   (let [keys-schemas
         (->> tags
              (mapcat #(utils/get-tag ztx %))
              (mapv (fn [sch-name]
                      (let [sch (utils/get-symbol ztx sch-name)] ;; TODO get rid of type coercion
                        [(if (= "zen" (namespace sch-name))
                           (keyword (name sch-name))
                           (keyword sch-name))
                         (:for sch)]))))]
     (fn validate-key-schema [vtx data opts]
      (let [correct-keys
            (into #{}
                  (keep (fn [[sch-key for]]
                          (when (or (nil? for)
                                    (contains? for (get data key)))
                            sch-key)))
                  keys-schemas)

            all-keys
            (-> data keys set)

            incorrect-keys
            (utils/set-diff all-keys correct-keys)]
       (update vtx
               :unknown-keys
               into
               (map #(conj (:path vtx) %))
               incorrect-keys))))))
