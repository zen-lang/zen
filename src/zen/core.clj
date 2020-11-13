(ns zen.core
  (:require
   [zen.validation]
   [zen.store]))

(def load-ns zen.store/load-ns)
(def load-ns! zen.store/load-ns!)
(def read-ns zen.store/read-ns)
(def new-context zen.store/new-context)

(defn validate [ctx symbols data]
  (zen.validation/validate ctx symbols data))


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
