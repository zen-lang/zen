(ns zen.changes
  (:require [clojure.data]))


(defn namespace-check [acc old-ztx new-ztx]
  (let [get-set-of-ns #(set (keys (:ns %)))

        [lost _new unchanged]
        (clojure.data/diff (get-set-of-ns old-ztx)
                           (get-set-of-ns new-ztx))]
    (cond-> acc
      :always    (update :data merge {::unchanged-namespaces unchanged})
      (seq lost) (update :errors
                         into
                         (map (fn [lost-ns]
                                {:type      :namespace/lost
                                 :message   (str "Lost namespace: "  lost-ns)
                                 :namespace lost-ns}))
                         lost))))


(def core-zen-syms #{'import 'ns 'alias})


(defn ztx->ns-symbols-set
  ([ztx selected-namespaces]
   (ztx->ns-symbols-set (update ztx :ns select-keys selected-namespaces)))

  ([ztx]
   (update-vals (:ns ztx)
                (fn [zen-ns]
                  (let [symbols (set (keys zen-ns))]
                    (apply disj symbols core-zen-syms))))))


(defn symbols-check [acc old-ztx new-ztx]
  (let [unchanged-namespaces (get-in acc [:data ::unchanged-namespaces])

        [lost _new unchanged]
        (clojure.data/diff (ztx->ns-symbols-set old-ztx unchanged-namespaces)
                           (ztx->ns-symbols-set new-ztx unchanged-namespaces))]
    (cond-> acc
      :always    (update :data merge {::unchanged-symbols unchanged})
      (seq lost) (update :errors
                         into
                         (mapcat (fn [[zen-ns lost-syms]]
                                   (map (fn [lost-sym]
                                          (let [ns-sym (symbol (name zen-ns) (name lost-sym))]
                                            {:type    :symbol/lost
                                             :message (str "Lost symbol: " ns-sym)
                                             :symbol  ns-sym}))
                                        lost-syms)))
                         lost))))


(defn check-compatible [old-ztx new-ztx]
  (let [acc (-> {:data {}, :errors []}
                (namespace-check old-ztx new-ztx)
                (symbols-check old-ztx new-ztx))
        errors (:errors acc)]
    (if (not-empty errors)
      {:status :error
       :errors errors}
      {:status :ok})))
