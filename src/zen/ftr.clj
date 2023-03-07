(ns zen.ftr
  (:require [clojure.string :as str]
            [zen.core]
            [zen.package]
            [cheshire.core]
            [clojure.java.io :as io]
            [zen.ftr-utils :as ftr-utils]))

(defn enrich-vs [vs]
  (if (contains? vs :ftr)
    (let [{:as _ftr-manifest,
           :keys [ftr-path source-type source-url]}
          (get vs :ftr)
          zen-file         (:zen/file vs)
          path-to-package  (->> (str/split zen-file #"/")
                                (take-while (complement #{"zrc"})))
          zen-package-name (last path-to-package)
          inferred-ftr-dir (if (= source-type :cloud-storage) ;;TODO Re-design manifests, harmonize source-types on ftr design/runtime phase
                             (str source-url \/ ftr-path) ;;That's uncorrect, cause source-url intended to store path to raw-terminology source,
                                                          ;;same thing with the source-type.
                             (-> path-to-package
                                 vec
                                 (conj "ftr")
                                 (->> (str/join "/"))))]
      (-> vs
          (assoc-in [:ftr :zen/package-name] zen-package-name)
          (assoc-in [:ftr :inferred-ftr-dir] inferred-ftr-dir)))
    vs))

(defn url? [path]
  (let [url (try (java.net.URL. path)
                 (catch Exception _ false))]
    (instance? java.net.URL url)))

(defn read-ndjson-line! [^java.io.BufferedReader buffered-reader]
  (cheshire.core/parse-string (.readLine buffered-reader) keyword))


(defn make-ftr-index-by-tag-index [tag-index ftr-dir module]
  (println "111" (count tag-index))
  (let [suitable-files (reduce
                        (fn [acc {:as _ti-entry
                                  :keys [hash name]}]
                          (let [[_module-name vs-name]
                                (str/split name #"\." 2)

                                tf-path
                                (format "%s/%s/vs/%s/tf.%s.ndjson.gz"
                                        ftr-dir
                                        module
                                        vs-name
                                        hash)]
                            (println "file"  tf-path)
                            (println "size" (.length (io/file tf-path)))
                            (if (> (.length (io/file tf-path)) 4500) acc (conj acc tf-path)))) [] tag-index)] 
    (println "222" (count suitable-files))
    (reduce (fn [{:as ftr-index-by-tag, :keys [valuesets]}
                 tf-path]
              (let [new-tf-reader
                    (ftr-utils/open-gz-reader tf-path)

                    {codesystems "CodeSystem"
                     [{vs-url :url}] "ValueSet"}
                    (loop [line (read-ndjson-line! new-tf-reader)
                           css&vs []]
                      (if (= (:resourceType line) "ValueSet")
                        (group-by :resourceType (conj css&vs line))
                        (recur (read-ndjson-line! new-tf-reader)
                               (conj css&vs line))))

                    codesystems-urls
                    (map :url codesystems)

                    ftr-index-by-tag-with-updated-vss
                    (if (contains? valuesets vs-url)
                      (update-in ftr-index-by-tag [:valuesets vs-url] into codesystems-urls)
                      (assoc-in ftr-index-by-tag [:valuesets vs-url] (set codesystems-urls)))]
                (loop [{:as concept, :keys [code system display]}
                       (read-ndjson-line! new-tf-reader)

                       {:as ftr-index-by-tag, :keys [codesystems]}
                       ftr-index-by-tag-with-updated-vss]

                  (if-not (nil? concept)
                    (recur
                     (read-ndjson-line! new-tf-reader)
                     (if (get-in codesystems [system code])
                       (update-in ftr-index-by-tag [:codesystems system code :valueset] conj vs-url)
                       (assoc-in ftr-index-by-tag [:codesystems system code] {:display display
                                                                              :valueset #{vs-url}})))
                    ftr-index-by-tag))))
            {} suitable-files)))

(defn index-by-tags [tag-index-paths] 
  (reduce (fn [acc {:keys [tag path ftr-dir module]}]
            (let [tag-index (ftr-utils/parse-ndjson-gz path)]
              (update acc tag ftr-utils/deep-merge (make-ftr-index-by-tag-index tag-index ftr-dir module))))
          {} tag-index-paths))

(defn build-complete-ftr-index [ztx version]
  (let [vs-data     (:value-sets (zen.core/get-symbol ztx (symbol (str version "/value-sets"))))
        syms (map (fn [[_k v]] v) vs-data )
        value-sets          (map #(zen.core/get-symbol ztx %) syms)
        enriched-value-sets (map enrich-vs value-sets)
        ftr-cfgs-grouped-by-package-name
        (->> (keep :ftr enriched-value-sets)
             distinct
             (group-by :zen/package-name))

        tag-index-paths
        (for [[_package-name tag&module-pairs]      ftr-cfgs-grouped-by-package-name
              {:keys [module tag inferred-ftr-dir]} (->> tag&module-pairs
                                                         (keep #(not-empty (select-keys % [:tag :module :inferred-ftr-dir])))
                                                         distinct)

              :let  [path (format "%s/%s/tags/%s.ndjson.gz" inferred-ftr-dir module tag)]
              :when (or (.exists (io/file path))
                        (url? path))]
          
          {:tag     tag
           :module  module
           :ftr-dir inferred-ftr-dir
           :path    path})]


    (swap! ztx assoc :zen.fhir/ftr-index {:result    (index-by-tags tag-index-paths)
                                          :complete? true})))