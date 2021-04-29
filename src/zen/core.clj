(ns zen.core
  (:require
   [zen.validation]
   [zen.utils]
   [zen.store]))

(defn load-ns [ctx ns]
  (zen.store/load-ns ctx ns))

(defn load-ns! [ctx ns]
  (zen.store/load-ns! ctx ns))

(defn read-ns [ctx ns-name]
  (zen.store/read-ns ctx ns-name))

(defn new-context [& [opts]]
  (zen.store/new-context (or opts {})))

(defn get-symbol [ctx sym]
  (zen.store/get-symbol ctx sym))

(defn get-tag [ctx sym]
  (zen.store/get-tag ctx sym))

(defn validate [ctx symbols data]
  (zen.validation/validate ctx symbols data))

(defn validate-schema [ctx schema data]
  (zen.validation/validate-schema ctx schema data))


(defn fx-prepare-result [_ctx acc fx fx-result]
  (let [additional-keys {:path   (:path fx)
                         :schema (conj (:schema acc) (:name fx))}]
    (into {}
          (map (fn [[k vs]] {k (mapv #(merge % additional-keys) vs)}))
          fx-result)))

(defn fx-evaluator-dispatch [_ctx fx _data] (:name fx))

(defmulti fx-evaluator #'fx-evaluator-dispatch)

(defn merge-acc [acc fx-result]
  (let [data (:data fx-result)
        rest (dissoc fx-result :data)]
    (-> (merge-with into acc rest)
        (update :data (comp zen.utils/strip-nils zen.utils/deep-merge) data))))

(defn apply-fx [ctx validation-result data]
  (reduce (fn [acc fx]
            (merge-acc acc (fx-prepare-result ctx acc fx (fx-evaluator ctx fx data))))
          (assoc (dissoc validation-result :effects) :data data)
          (:effects validation-result)))

(defn validate! [ctx symbols data]
  (apply-fx ctx (zen.validation/validate ctx symbols data) data))


(comment
  (def ctx (new-context {}))

  (update {:foo (set [{:a 1}])} :foo disj {:a 1})

  ;; (read-ns ctx 'zen)
  ;; (read-ns ctx 'auth.op)
  (read-ns ctx 'myapp)
  (read-ns ctx 'zen)

  (println (str/join "\n" (:errors @ctx)))

  ;; (:syms @ctx)
  ;; @ctx
  (:tps @ctx))
