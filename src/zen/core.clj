(ns zen.core
  (:require
   [zen.effect]
   [zen.misc]
   [zen.store]
   [zen.utils]
   [zen.v2-validation :as v2]))

(defn load-ns [ztx ns]
  (zen.store/load-ns ztx ns))

(defn load-ns! [ztx ns]
  (zen.store/load-ns! ztx ns))

(defn read-ns [ztx ns-name]
  (zen.store/read-ns ztx ns-name))

(defn new-context [& [{_paths :paths _entry-point :entry-point :as opts}]]
  (zen.store/new-context (or opts {})))

(defn get-symbol [ztx sym]
  (zen.utils/get-symbol ztx sym))

(defn get-tag [ztx sym]
  (zen.store/get-tag ztx sym))

(defn get-tagged [ztx sym]
  (->> (zen.store/get-tag ztx sym)
       (mapv (fn [t] (get-symbol ztx t)))))

(defn validate [ztx symbols data]
  (-> (v2/validate ztx symbols data)
      (select-keys [:errors :warnings :effects])))

(defn validate-schema [ztx schema data]
  (v2/validate-schema ztx schema data))

(defn validate! [ztx symbols data]
  (zen.effect/apply-fx ztx (v2/validate ztx symbols data) data))

(defn errors
  "Get zen metastorage errors. 
   :order param values :ns&message (default) or :as-is
   throws error if order param value is unknown"
  [ztx & {:keys [order]}]
  (let [binding-errs
        (->> (:bindings @ztx)
             (remove #(:backref (second %)))
             (map (fn [[binding-name {:keys [diref]}]]
                    {:message (format "No binding for '%s" binding-name)
                     :type :unbound-binding
                     :ns 'zen.store
                     :diref diref})))
        errs (into (:errors @ztx) binding-errs)]
    (case (or order :ns&message)
      :ns&message #_"NOTE: by errors fn was always sorting messages,
                           now this is the default for compatibility"
      (->> errs
           (sort-by (fn [e] (str (:ns e) "-" (:message e))))
           vec)

      :as-is errs)))

(defn engine-or-name [config]
  (or (:engine config) (:zen/name config)))

(defn engine-key [config]
  (or (:engine config) (:zen/name config)))

(defmulti start
  "Start operation."
  (fn [_ztx cfg]
    (engine-key cfg)))

(defmulti stop
  "Stop operation."
  (fn [_ztx cfg _state]
    (engine-key cfg)))

(defmulti reload
  "Reload operation."
  (fn [_ztx cfg _state]
    (engine-key cfg)))

(defmulti op
  "Define system operation.
  * ztx - zen context
  * cfg - zen config
  * params - operation params
  * [session] - session data"
  (fn [_ztx cfg _params & [_session]] (engine-or-name cfg)))

(defmethod op
  :default
  [_ztx config _req & [_session]]
  (println :no-op-impl (engine-or-name config)))

(defn pub
  "Publish event."
  [_ztx event-name params & [_session]]
  (println :event event-name params))

(defn error
  "Publish error."
  [ztx error-name params & [session]]
  (pub ztx error-name params session)
  {:error (assoc params :name error-name)})

;; TODO: impl partial op
(defn op-call [ztx model-name params & [session]]
  (if-let [config (get-symbol ztx  model-name)]
    (if-let [proto-nm (:op config)]
      (if-let [proto (get-symbol ztx proto-nm)]
        (op ztx proto (merge-with merge params config) session)
        (error ztx 'zen/op-missed {:op proto-nm}))
      (op ztx config params session))
    (error ztx 'zen/op-missed {:op model-name})))

(defn set-state [ztx key state & [start]]
  (swap! ztx assoc-in [:zen/state key] {:state state :start start}))

(defn get-state [ztx key]
  (when key
    (get-in @ztx [:zen/state key :state])))

(defn model-state-key [ztx model]
  (or (:zen/state-key model)
      (when-let [tp (:engine model)]
        (:zen/state-key (get-symbol ztx tp)))))

(defn model-state [ztx model]
  (get-state ztx (model-state-key ztx model)))

(defn stop-call [ztx op-name]
  (if-let [model (get-symbol ztx op-name)]
    (let [k (model-state-key ztx model)
          state (get-state ztx k)]
      (when state
        (pub ztx 'zen/on-stop {:op op-name :zen/state-key k})
        (stop ztx model state)
        (swap! ztx update :zen/state dissoc k)))
    (error ztx 'zen/on-stop-no-model {:op op-name})))

;; find method model (model or if engine then model)
;; check is it start (rename to state) or op
(defn get-op-model [ztx op-name]
  (let [model (get-symbol ztx op-name)]
    (if-let [op (or (:engine model) (:op model))]
      (get-symbol ztx op)
      model)))

(defn start-call [ztx op-name]
  (if-let [op-model (get-op-model ztx op-name)]
    (cond
      (contains? (:zen/tags op-model) 'zen/start)
      (do
        (stop-call ztx op-name)
        (if-let [model (get-symbol ztx op-name)]
          (let [k (model-state-key ztx model)]
            (if k
              (try
                (pub ztx 'zen/on-start {:op op-name :zen/state-key k})
                (let [state (start ztx model)]
                  (set-state ztx k state op-name))
                (catch Exception ex
                  (error ztx 'zen/start-failed {:exception ex :op op-name})))
              (error ztx 'zen/no-state-key {:op op-name})))
          (error ztx 'zen/start-missed {:op op-name})))
      (contains? (:zen/tags op-model) 'zen/op)
      (op ztx op-model (get-symbol ztx op-name))
      :else
      (error ztx 'zen/start-unknown {:op op-name :message "expected op-name is zen/start or zen/op"}))
    (error ztx 'zen/start-missed {:op op-name})))

(defn start-system [ztx & [entry-point]]
  (if-let [system (get-symbol ztx entry-point)]
    (doseq [start-fn (:start system)]
      (start-call ztx start-fn))
    (error ztx 'zen/system {:message (str "No entry point " entry-point)})))

(defn stop-system [ztx]
  (doseq [op-name (->>
                   (get @ztx :zen/state)
                   (map (fn [[_k v]] (:start v)))
                   (filter identity))]
    (stop-call ztx op-name)))

(defn tag-reduce [ztx tag f]
  (reduce
   (fn [acc sym]
     (f acc (get-symbol ztx sym)))
   {}
   (get-tag ztx tag)))


(defn tag-filter [ztx tag f]
  (->> (get-tag ztx tag)
       (map #(get-symbol ztx %))
       (filter f)))


(defn tag-filter-first [ztx tag f]
  (first (tag-filter ztx tag f)))
