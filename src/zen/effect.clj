(ns zen.effect
  (:require [zen.utils]))

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

