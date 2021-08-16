(ns zen.dev
  (:require [hawk.core :as hawk]
            [zen.core]
            [clojure.string :as str]
            [clojure.java.io :as io]))

(defn ns-name [paths filename]
  (when-let [nm (->> paths
                     (map (fn [p]
                            (when (str/starts-with? filename p)
                              (subs filename (inc (count p)) (- (count filename) 4)))))
                     (filter identity)
                     (first))]
    (-> (str/replace nm #"/" ".")
        (symbol))))

(defn reload-ns [ztx ns]
  (println :zen.watch/reload ns)
  ;; TODO: reload depended namespaces
  (swap! ztx update :errors
         (fn [errs]
           (->> errs
                (remove (fn [{ens :ns}]
                          (= ens ns))))))
  (zen.core/read-ns ztx (symbol ns)))

(defn handle-updates [ztx paths htx {file :file kind :kind}]
  (let [filename (-> (.getPath file)
                     ;; this is strange prefix in macos
                     (str/replace  #"^/private" ""))]
    (when (and (str/ends-with? filename ".edn")
               (not (str/starts-with? (.getName file) ".")))
      (if-let [ns (ns-name paths filename)]
        (reload-ns ztx ns)
        (println :zen.watch/can-not-find filename))))
  htx)

(defn watch [ztx]
  (when-let [paths (:paths @ztx)]
    (let [w (hawk/watch! [{:paths paths :handler (fn [htx e] (handle-updates ztx paths htx e))}])]
      (swap! ztx assoc-in [:zen/services :zen/watch] w)
      (println :zen.watch/started)
      :zen.watch/started)))

(defn stop [ztx]
  (when-let [w (get-in @ztx [:zen/services :zen/watch])]
    (hawk/stop! w)
    (swap! ztx update :zen/services dissoc :zen/watch)
    :zen.watch/stopped))





(comment
  (def w
    )
  (hawk/stop! w)

  )
