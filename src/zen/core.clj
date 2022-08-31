(ns zen.core
  (:require
   [zen.misc]
   [zen.v2-validation :as v2]
   [zen.effect]
   [zen.utils]
   [zen.store]))

(defn load-ns [ztx ns]
  (zen.store/load-ns ztx ns))

(defn load-ns! [ztx ns]
  (zen.store/load-ns! ztx ns))

(defn read-ns [ztx ns-name]
  (zen.store/read-ns ztx ns-name))

(defn new-context [& [{paths :paths entry-point :entry-point :as opts}]]
  (zen.store/new-context (or opts {})))

(defn get-symbol [ztx sym]
  (zen.utils/get-symbol ztx sym))

(defn get-tag [ztx sym]
  (zen.store/get-tag ztx sym))

(defn validate [ztx symbols data]
  (-> (v2/validate ztx symbols data)
      (select-keys [:errors :warnings :effects])))

(defn validate-schema [ztx schema data]
  (v2/validate-schema ztx schema data))

(defn validate! [ztx symbols data]
  (zen.effect/apply-fx ztx (v2/validate ztx symbols data) data))

(defn errors
  "get zen metastorage errors"
  [ztx]
  (->>
   (:errors @ztx)
   (sort-by (fn [e] (str (:ns e) "-" (:message e)) ))
   vec))

;; zen.system functions

(defn engine-or-name [config]
  (or (:engine config) (:zen/name config)))

(defn engine-key [config]
  (or (:engine config) (:zen/name config)))

(defmulti start
  "stop operation"
  (fn [ztx cfg]
    (engine-key cfg)))

(defmulti stop
  "stop operation"
  (fn [ztx cfg state]
    (engine-key cfg)))

(defmulti reload
  "stop operation"
  (fn [ztx cfg state]
    (engine-key cfg)))

(defmulti op
  "define system operation
  * ztx - zen context
  * cfg - zen config
  * params - operation params
  * [session] - session data
  "
  (fn [ztx cfg params & [session]] (engine-or-name cfg)))

(defmethod op
  :default
  [_ztx config req & [session]]
  (println :no-op-impl (engine-or-name config)))

(defn pub
  "publish event"
  [_ztx event-name params & [_session]]
  (println :event event-name params))

(defn error
  "publish error"
  [ztx error-name params & [session]]
  (pub ztx error-name params session)
  {:error (assoc params :name error-name)})

(defn op-call [ztx model-name params & [session]]
  (if-let [config (get-symbol ztx  model-name)]
    (op ztx config params session)
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

(defn start-call [ztx op-name]
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
        (error ztx 'zen/no-start-key {:op op-name})))
    (error ztx 'zen/start-missed {:op op-name})))

(defn start-system [ztx & [entry-point]]
  (let [system (get-symbol ztx entry-point)]
    (doseq [start-fn (:start system)]
      (start-call ztx start-fn))))

(defn stop-system [ztx]
  (doseq [op-name (->>
                   (get-in @ztx [:zen/state])
                   (map (fn [[k v]] (:start v)))
                   (filter identity))]
    (stop-call ztx op-name)))
