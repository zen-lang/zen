(ns zen.changes
  (:require [clojure.data]))

(defn namespace-check [acc old-ztx new-ztx]
  (let [[lost _new unchanged]
        (clojure.data/diff (-> old-ztx :ns keys set)
                           (-> new-ztx :ns keys set))]
    (cond-> acc
      :always    (update :data merge {::unchanged-namespaces unchanged})
      (seq lost) (update :errors conj {:type       :namespace/lost
                                       :message    (str "Lost namespaces: "  lost)
                                       :namespaces lost}))))

(def reserverd-syms #{'import 'ns 'alias})

(defn zen-ns-syms [zen-ns]
  (apply disj (set (keys zen-ns)) reserverd-syms))

(defn symbols-check [acc old-ztx new-ztx]
  (let [namespaces-in-both (get-in acc [:data ::unchanged-namespaces])
        to-set-of-syms     #(update-vals (select-keys (:ns %) namespaces-in-both)
                                         zen-ns-syms)
        [lost _new unchanged]
        (clojure.data/diff (to-set-of-syms old-ztx)
                           (to-set-of-syms new-ztx))]
    (cond-> acc
      :always    (update :data merge {::unchanged-symbols unchanged})
      (seq lost) (update :errors conj {:type    :symbol/lost
                                       :message (apply str "Lost syms: " (map (fn [[ns syms]] (str "in " ns " " syms "\n")) lost))
                                       :ns-syms lost}))))


(defn check-compatible [old-ztx new-ztx]
  (let [acc (-> {:data {}, :errors []}
                (namespace-check old-ztx new-ztx)
                (symbols-check old-ztx new-ztx))
        errors (:errors acc)]
    (if (not-empty errors)
      {:status :error
       :errors errors}
      {:status :ok})))
