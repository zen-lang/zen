(ns zen.dev
  (:require [hawk.core :as hawk]
            [zen.core]
            [clojure.string :as str]
            [clojure.java.io :as io]))

(defn make-ns-name [paths filename]
  (when-let [nm (->> paths
                     (keep io/as-file)
                     (map (fn [^java.io.File f] (.getPath f))) #_"NOTE: path sanitization"
                     (map (fn [p]
                            (when (str/starts-with? filename p)
                              (subs filename (inc (count p)) (- (count filename) 4)))))
                     (filter identity)
                     (first))]
    (-> (str/replace nm #"/" ".")
        (symbol))))

(defn reload-ns [ztx ns-sym]
  (println :zen.watch/reload ns-sym)
  ;; TODO: reload depended namespaces
  (swap! ztx update :errors
         (partial remove (fn [error]
                           (or (= ns-sym (:ns error))
                               (and (:resource error)
                                    (= ns-sym (-> error :resource namespace symbol)))
                               (and (:missing-ns error)
                                    (= ns-sym (:missing-ns error)))))))
  (swap! ztx assoc-in [:ns-reloads ns-sym] (hash (get-in @ztx [:ns ns-sym])))
  (zen.core/read-ns ztx (symbol ns-sym)))

(defn handle-updates [ztx paths htx {^java.io.File file :file kind :kind}]
  (let [filename (-> (.getPath file)
                     ;; this is strange prefix in macos
                     (str/replace  #"^/private" ""))]
    (when (and (str/ends-with? filename ".edn")
               (not (str/starts-with? (.getName file) ".")))
      (if-let [ns-name (make-ns-name paths filename)]
        (if (->> (lazy-cat (keys (:ns @ztx))
                           (map :ns (:errors @ztx))
                           (map :missing-ns (:errors @ztx)))
                 (some #{ns-name}))
          (reload-ns ztx ns-name)
          (println :zen.watch/reload-ignore ns-name))
        (println :zen.watch/can-not-find filename))))
  htx)

(defn watch [ztx]
  (when-let [paths (concat (:paths @ztx)
                           (map #(str % "/zrc") (:package-paths @ztx))) #_"TODO: refactor to unify paths collection with functions from zen.store/find-file"]
    (let [w (hawk/watch! [{:paths paths :handler (fn [htx e] (handle-updates ztx paths htx e))}])]
      (swap! ztx assoc-in [:zen/services :zen/watch] w)
      (println :zen.watch/started)
      :zen.watch/started)))

(defn stop [ztx]
  (when-let [w (get-in @ztx [:zen/services :zen/watch])]
    (hawk/stop! w)
    (swap! ztx update :zen/services dissoc :zen/watch)
    (println :zen.watch/stopped)
    :zen.watch/stopped))





(comment
  (def w
    )
  (hawk/stop! w)

  )
