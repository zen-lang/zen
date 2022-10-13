(ns zen.changes
  (:require [clojure.data]
            [zen.core]
            [zen.walk]))


(defn namespace-check [acc old-ztx new-ztx]
  (let [get-set-of-ns #(set (keys (:ns %)))

        [lost new same]
        (clojure.data/diff (get-set-of-ns @old-ztx)
                           (get-set-of-ns @new-ztx))]
    (cond-> acc
      :always    (update :data merge {::same-namespaces same})
      (seq lost) (update :changes
                         into
                         (map (fn [lost-ns]
                                {:type      :namespace/lost
                                 :message   (str "Lost namespace: "  lost-ns)
                                 :namespace lost-ns}))
                         lost)
      (seq new) (update :changes
                         into
                         (map (fn [new-ns]
                                {:type      :namespace/new
                                 :message   (str "New namespace: "  new-ns)
                                 :namespace new-ns}))
                         new))))


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
  (let [same-namespaces (get-in acc [:data ::same-namespaces])

        [lost new same]
        (map #(mapcat ns-symbols->qualified-symbols %)
             (clojure.data/diff (ztx->ns-symbols-set @old-ztx same-namespaces)
                                (ztx->ns-symbols-set @new-ztx same-namespaces)))]

    (cond-> acc
      :always    (update :data merge {::same-symbols same})
      (seq lost) (update :changes
                         into
                         (map (fn [lost-sym]
                                {:type    :symbol/lost
                                 :message (str "Lost symbol: " lost-sym)
                                 :symbol  lost-sym}))
                         lost)
      (seq new)  (update :changes
                        into
                        (map (fn [new-sym]
                               {:type    :symbol/new
                                :message (str "New symbol: " new-sym)
                                :symbol  new-sym}))
                        new))))


(defn index-sch-seq [sch-seq]
  (into {}
        (map (fn [el] [(:path el) el]))
        sch-seq))


(defn get-indexed-sch-seq [ztx sym]
  (index-sch-seq (zen.walk/zen-dsl-leafs-seq ztx (zen.core/get-symbol ztx sym))))


(defn process-change [sym path before after]
  (let [change-type (cond
                      (= before after) :same
                      (nil? before)    :added
                      (nil? after)     :removed
                      :else            :updated)]
    (when (not= :same change-type)
      {:change-type change-type
       :sym         sym
       :path        path
       :before      before
       :after       after})))


(defn sym-changes [old-ztx new-ztx sym]
  (let [old (get-indexed-sch-seq old-ztx sym)
        new (get-indexed-sch-seq new-ztx sym)

        {errs :acc, added :new}
        (reduce (fn [{:keys [acc new]} [ch-pth old-el]]
                  (let [{:keys [path]} old-el
                        new-el (get new ch-pth)
                        change (process-change sym path (:value old-el) (:value new-el))]
                    {:acc (cond-> acc (some? change) (conj change))
                     :new (dissoc new ch-pth)}))
                {:acc []
                 :new new}
                old)]

    (reduce (fn [acc [ch-pth new-el]]
              (let [{:keys [path]} new-el
                    change (process-change sym path nil (:value new-el))]
                (cond-> acc (some? change) (conj change))))
            errs
            added)))


(defn schema-check [acc old-ztx new-ztx]
  (let [same-symbols (get-in acc [:data ::same-symbols])
        changes      (mapcat #(sym-changes old-ztx new-ztx %) same-symbols)]
    (cond-> acc
      (seq changes)
      (update :changes
              into
              (map (fn [{:keys [change-type before after sym path]}]
                     (let [error-type (keyword "schema" (name change-type))]
                       {:type   error-type
                        :sym    sym
                        :path   path
                        :before before
                        :after  after})))
              changes))))


(defn check-changes [old-ztx new-ztx]
  (let [acc (-> {:data {}, :changes []}
                (namespace-check old-ztx new-ztx)
                (symbols-check old-ztx new-ztx)
                (schema-check old-ztx new-ztx))
        errors (:changes acc)]
    (if (not-empty errors)
      {:status :changed
       :changes errors}
      {:status :unchanged})))
