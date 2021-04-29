(ns zen.core
  (:require
   [zen.validation]
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

(defn apply-fx [ctx validation-result data]) ;; TODO

(comment
  (def ctx (new-context {}))

  ;; (read-ns ctx 'zen)
  ;; (read-ns ctx 'auth.op)
  (read-ns ctx 'myapp)
  (read-ns ctx 'zen)




  (println
   (str/join "\n" (:errors @ctx)))

  ;; (:syms @ctx)
  ;; @ctx
  (:tps @ctx)
  )
