(ns zen.core
  (:require
   ;; [zen.validation]
   [zen.effect]
   [zen.store]))

(defn load-ns [ztx ns]
  (zen.store/load-ns ztx ns))

(defn load-ns! [ztx ns]
  (zen.store/load-ns! ztx ns))

(defn read-ns [ztx ns-name]
  (zen.store/read-ns ztx ns-name))

(defn new-context [& [opts]]
  (zen.store/new-context (or opts {})))

(defn get-symbol [ztx sym]
  (zen.store/get-symbol ztx sym))

(defn get-tag [ztx sym]
  (zen.store/get-tag ztx sym))

(defn get-tags [ztx sym]
  (->> 
   (zen.store/get-tag ztx sym)
   (map #(get-symbol ztx %))))

;; (defn validate [ztx symbols data]
;;   (zen.validation/validate ztx symbols data))

;; (defn validate-schema [ztx schema data]
;;   (zen.validation/validate-schema ztx schema data))

;; (defn validate! [ztx symbols data]
;;   (zen.effect/apply-fx ztx (zen.validation/validate ztx symbols data) data))

(comment
  (def ztx (new-context {}))

  (update {:foo (set [{:a 1}])} :foo disj {:a 1})

  ;; (read-ns ztx 'zen)
  ;; (read-ns ztx 'auth.op)
  (read-ns ztx 'myapp)
  (read-ns ztx 'zen)

  (println (str/join "\n" (:errors @ztx)))
  ;; (:syms @ztx)
  ;; @ztx
  (:tps @ztx))
