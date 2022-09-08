(ns zen.cli
  (:gen-class)
  (:require [cli-matic.core]
            [cli-matic.utils]
            [zen.package]
            [zen.core]
            [clojure.pprint]
            [clojure.java.io]
            [clojure.string]
            [clojure.edn]))


(defn init [{[name] :_arguments}]
  (let [to (zen.package/pwd)]
    (zen.package/make-template! to name)
    (zen.package/zen-init! to)))


(defn pull-deps [_]
  (let [to (zen.package/pwd)]
    (zen.package/zen-init-deps! to)))


(defn errors [_]
  (let [pwd (zen.package/pwd :silent true)
        ztx (zen.core/new-context {:package-paths [pwd]})
        zrc (str pwd "/zrc")
        relativize #(subs % (count zrc))
        zrc-edns (->> zrc
                      clojure.java.io/file
                      file-seq
                      (filter #(clojure.string/ends-with? % ".edn"))
                      (map #(relativize (.getAbsolutePath %)))
                      (remove clojure.string/blank?)
                      (map #(subs % 1)))
        namespaces (map #(-> %
                             (clojure.string/replace ".edn" "")
                             (clojure.string/replace \/ \.)
                             symbol)
                        zrc-edns)]
    (run! #(zen.core/read-ns ztx %) namespaces)

    (clojure.pprint/pprint (zen.core/errors ztx))))


(defn validate [{[symbols-str data-str] :_arguments }]
  (let [pwd (zen.package/pwd :silent true)
        ztx (zen.core/new-context {:package-paths [pwd]})

        symbols (clojure.edn/read-string symbols-str)
        data (clojure.edn/read-string data-str)

        namespaces (map (comp symbol namespace) symbols)]

    (run! #(zen.core/read-ns ztx %) namespaces)
    (clojure.pprint/pprint (zen.core/validate ztx symbols data))))


(def cfg
  {:command     "zen"
   :description "Zen-lang cli. Provides zen validation, package managment and build tools."
   :version     "0.0.1"
   :opts        []
   :subcommands [{:description "Builds zen project from provided IG"
                  :command     "init"
                  :runs        init}
                 {:description "Recursively pulls all deps specified in package.edn to zen-modules/"
                  :command     "pull-deps"
                  :runs        pull-deps}
                 {:description "Validates zen, returns errors"
                  :command     "errors"
                  :runs        errors}
                 {:description "Validates data with specified symbol. Use: `zen validate #{symbol} data`"
                  :command     "validate"
                  :runs        validate}]})


(defn -main
  [& args]
  (cli-matic.core/run-cmd args cfg))


(comment

  (require 'clojure.java.shell)

  (do
    (clojure.java.shell/sh "rm" "-rf" "/tmp/zen")
    (clojure.java.shell/sh "mkdir" "-p" "/tmp/zen"))

  (clojure.java.shell/sh "make" "-B" "build")
  ;;
  )
