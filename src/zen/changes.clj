(ns zen.changes
  (:require [clojure.data]
            [zen.core]
            [zen.walk]))


(defn namespace-check [acc old-ztx new-ztx]
  (let [get-set-of-ns #(set (keys (:ns %)))

        [lost _new unchanged]
        (clojure.data/diff (get-set-of-ns @old-ztx)
                           (get-set-of-ns @new-ztx))]
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


(defn ns-symbols->qualified-symbols [[zen-ns symbols]]
  (map #(symbol (name zen-ns) (name %))
       symbols))


(defn symbols-check [acc old-ztx new-ztx]
  (let [unchanged-namespaces (get-in acc [:data ::unchanged-namespaces])

        [lost _new unchanged]
        (map #(mapcat ns-symbols->qualified-symbols %)
             (clojure.data/diff (ztx->ns-symbols-set @old-ztx unchanged-namespaces)
                                (ztx->ns-symbols-set @new-ztx unchanged-namespaces)))]

    (cond-> acc
      :always    (update :data merge {::unchanged-symbols unchanged})
      (seq lost) (update :errors
                         into
                         (map (fn [lost-sym]
                                {:type    :symbol/lost
                                 :message (str "Lost symbol: " lost-sym)
                                 :symbol  lost-sym}))
                         lost))))


(defn index-sch-seq [sch-seq]
  (reduce (fn [acc {:keys [path], value :value}]
            (let [attr (last path)]
              (assoc acc [path attr] {:path path, :attr attr :value value})))
          {}
          sch-seq))


(defn get-indexed-sch-seq [ztx sym]
  (index-sch-seq (zen.walk/zen-dsl-seq ztx (zen.core/get-symbol ztx sym))))


(defn process-change [sym path attr before after]
  (let [change-type (cond
                      (= before after) :same
                      (nil? before)    :added
                      (nil? after)     :removed
                      :else            :changed)]
    (when (not= :same change-type)
      {:change-type change-type
       :sym         sym
       :path        path
       :attr        attr
       :before      before
       :after       after})))


(defn sym-changes [old-ztx new-ztx sym]
  (let [old (get-indexed-sch-seq old-ztx sym)
        new (get-indexed-sch-seq new-ztx sym)

        {errs :acc, added :new}
        (reduce (fn [{:keys [acc new]} [ch-pth old-el]]
                  (let [{:keys [attr path]} old-el
                        new-el (get new ch-pth)
                        change (process-change sym path attr (:value old-el) (:value new-el))]
                    {:acc (cond-> acc (some? change) (conj change))
                     :new (dissoc new ch-pth)}))
                {:acc []
                 :new new}
                old)]

    (reduce (fn [acc [ch-pth new-el]]
              (let [{:keys [attr path]} new-el
                    change (process-change sym path attr nil (:value new-el))]
                (cond-> acc (some? change) (conj change))))
            errs
            added)))


(defn schema-check [acc old-ztx new-ztx]
  (let [unchanged-symbols (get-in acc [:data ::unchanged-symbols])
        changes (mapcat #(sym-changes old-ztx new-ztx %) unchanged-symbols)]
    (cond-> acc
      (seq changes)
      (update :errors
              into
              (map (fn [{:keys [change-type before after sym path attr]}]
                     (let [error-type (keyword "schema" (name change-type))]
                       {:type   error-type
                        :sym    sym
                        :path   path
                        :attr   attr
                        :before before
                        :after  after})))
              changes))))


(defn check-compatible [old-ztx new-ztx]
  (let [acc (-> {:data {}, :errors []}
                (namespace-check old-ztx new-ztx)
                (symbols-check old-ztx new-ztx)
                (schema-check old-ztx new-ztx))
        errors (:errors acc)]
    (if (not-empty errors)
      {:status :error
       :errors errors}
      {:status :ok})))
