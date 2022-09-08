(ns zen.cli
  (:gen-class)
  (:require [cli-matic.core]
            [cli-matic.utils]
            [zen.package]))

(def cfg
  {:command     "zen"
   :description "Zen-lang cli. Provides zen validation, package managment and build tools."
   :version     "0.0.1"
   :opts        []
   :subcommands [{:description "Builds zen project from provided IG"
                  :command     "init"
                  :opts        [{:option "into"
                                 :short ""
                                 :type :string}]
                  :runs        (fn [{:keys [into]}]
                                 (println (or into (zen.package/pwd))))}
                 {:description "Recursively pulls all deps specified in package.edn to zen-modules/"
                  :command     "pull-deps"
                  :runs        (fn [{:keys []}])}]})

(defn -main
  [& args]
  (cli-matic.core/run-cmd args cfg))

(comment

zen init | zen i

/
package.edn
zrc
zen-modules
.gitignore


  ;;
  )
