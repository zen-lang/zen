(ns zen.types-generation
  (:require [zen.schema :as sut]
            [zen.core]
            [zen.package]
            [zen.utils]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.java.shell :as shell]
            [zen.ftr]))

(def premitives-map
  {:integer "number"})

(def non-parsable-premitives
  {:string "string"
   :number "number"
   :boolean "boolean"})

(defn set-to-string [value]
  (reduce
   (fn [acc item]
     (cond
       (set? item) (set-to-string item)
       (keyword? item) (conj acc
                             (str/replace (name item) #"hl7-fhir-r4-core." ""))
       :else (conj acc
                   (str/replace (namespace item) #"hl7-fhir-r4-core." ""))))
   [] value))

(defn get-desc [{desc :zen/desc}]
  (when desc
    (str "/* " desc " */\n")))

(defn get-not-required-filed-sign [vtx]
  (when-not (contains? (get
                        (::require vtx)
                        (if (= (count (pop  (pop (:path vtx)))) 0)
                          "root"
                          (str/join "." (pop (pop (:path vtx)))))) (last (:path vtx))) "?"))

(defn exclusive-keys-child? [vtx]
  (and (> (count (::exclusive-keys vtx)) 0) (> (count (:path vtx)) 1)
       (or (contains? (::exclusive-keys vtx) (str/join "." (pop (pop (:path vtx)))))
           (contains? (::exclusive-keys vtx) (str/join "." (pop (:path vtx)))))))

(defn keys-in-array-child? [vtx]
  (and (> (count (::keys-in-array vtx)) 0) (> (count (:path vtx)) 1)
       (contains? (::keys-in-array vtx) (str/join "." (:path vtx)))))

(defn get-reference-union-type [references]
  (str "Reference<"
       (if (empty? references) "ResourceType" (str/join " | " (map (fn [item] (str "'" item "'")) references)))
       ">"))

(defn get-exlusive-keys-type [data]
  (let [union-type (str/join " | " (set-to-string (:exclusive-keys data)))]
    (cond (:Reference (:keys data))
          (str/replace union-type #"Reference"
                       (get-reference-union-type (set-to-string (:refers (:zen.fhir/reference (:Reference (:keys data)))))))
          :else union-type)))

(defn generate-type-value
  [data]
  (if (:confirms data)
    (if
     (get-in data [:zen.fhir/reference :refers])
      (get-reference-union-type (set-to-string (get-in data [:zen.fhir/reference :refers])))
      (str/join " | " (set-to-string (:confirms data))))
    (if
     ((keyword (name (:type data))) premitives-map)
      ((keyword (name (:type data))) premitives-map)
      (name (:type data)))))

(defn generate-type [vtx data]
  (when (and (empty? (::ts vtx))
             (not ((keyword (::interface-name vtx)) non-parsable-premitives)))
    (str "type " (::interface-name vtx)  " = " (generate-type-value data))))

(defn generate-interface [vtx {confirms :confirms}]
  (let [extended-resource (first (set-to-string confirms))
        extand (if (and extended-resource (not= extended-resource "zen.fhir")) (format " extends %s " extended-resource) " ")]
    (str "interface " (::interface-name vtx) extand)))

(defn generate-name
  [vtx data]
  (str (get-desc data)
       (if (::is-type vtx)
         (generate-type vtx data)
         (generate-interface vtx data))))

(defn get-valueset-values [ztx value-set]
  (let [{uri :uri} (zen.core/get-symbol ztx value-set)
        ftr-index (get-in @ztx [:zen.fhir/ftr-index :result "init"])
        result (->>  (get-in ftr-index [:valuesets uri])
                     (filter #(not= "http://snomed.info/sct" %))
                     (map (fn [item]
                            (->> (get-in ftr-index [:codesystems item])
                                 (filter (fn [[_ value]] (contains? (:valueset value) uri)))
                                 keys)))
                     flatten
                     (remove nil?)
                     seq)]
    result))

(defn generate-valueset-union-type [ztx schema]
  (let [valueset-values (get-valueset-values ztx (get-in schema [:zen.fhir/value-set :symbol]))]
    (if (or (> (count valueset-values) 20) (= (count valueset-values) 0))
      "string" (str/join " | " (map #(format "'%s'" %) valueset-values)))))

(defn generate-confirms [schema]
  (str
   (cond
     (= (first (set-to-string (:confirms schema))) "Reference")
     (get-reference-union-type (set-to-string (:refers (:zen.fhir/reference schema))))
     (= (first (set-to-string (:confirms schema))) "BackboneElement") ""
     :else (str (first (set-to-string (:confirms schema)))))))

(zen.schema/register-compile-key-interpreter! 
 [:keys ::ts]
 (fn [_ ztx ks]
   (fn [vtx data opts]
     (if-let [s (or (when (:zen.fhir/type data) (generate-name vtx data))
                    (when (:exclusive-keys data) (get-exlusive-keys-type data))
                    (when (exclusive-keys-child? vtx) "")
                    (when (keys-in-array-child? vtx) "")
                    (when (:confirms data)
                      (if (:zen.fhir/value-set data) (generate-valueset-union-type ztx data) (generate-confirms data)))
                    (when-let [tp (and
                                   (= (:type vtx) 'zen/symbol)
                                   (not (= (last (:path vtx)) :every))
                                   (not (:enum data))
                                   (or (= (:type data) 'zen/string)
                                       (= (:type data) 'zen/number)
                                       (= (:type data) 'zen/boolean))
                                   (:type data))]
                      (name tp))
                    (when (and (= (last (:path vtx)) :every) (= (last (:schema vtx)) 'zen/string))
                      "string; "))]
       (update vtx ::ts conj s)
       vtx))))

(defn generate-map-keys-in-array [vtx data]
  {(if (empty? (:path vtx))
     "root"
     (str/join "." (:path vtx))) (:keys data)})

(defn generate-exclusive-keys [vtx data]
  {(str/join "." (:path vtx)) (:exclusive-keys data)})

(defn generate-require [vtx data]
  {(if (empty? (:path vtx)) "root" (str/join "." (:path vtx))) (:require data)})

(defn generate-enum [data]
  (str  (str/join " | " (map (fn [item] (str "'" (:value item) "'")) data))))

(defn generate-values [vtx]
  (str (name (last (:path vtx)))
       (get-not-required-filed-sign vtx)
       ":"))

(defn update-require-and-keys-in-array [vtx data]
  (let [new-vtx (update vtx ::keys-in-array conj (generate-map-keys-in-array vtx data))]
    (update new-vtx ::require conj (generate-require vtx data))))

(zen.schema/register-schema-pre-process-hook!
 ::ts
 (fn [ztx schema]
   (fn [vtx data opts]
     (let [new-vtx (cond
                     (and (not (:keys data)) (empty? (:path vtx)))
                     (assoc vtx ::is-type true)
                     (or (and (:confirms data) (:keys data)) (:require data))
                     (update-require-and-keys-in-array vtx data)
                     (:exclusive-keys data)
                     (update vtx ::exclusive-keys conj (generate-exclusive-keys vtx data))
                     :else vtx)]

       (cond
         (= (last (:path new-vtx)) :zen.fhir/type) new-vtx
         (exclusive-keys-child? new-vtx) new-vtx
         (= (last (:schema new-vtx)) :enum)
         (update new-vtx ::ts conj (generate-enum data))
         (= (last (:schema new-vtx)) :values)
         (update new-vtx ::ts conj (get-desc data) (generate-values new-vtx))
         (= (last (:path new-vtx)) :keys) (update new-vtx ::ts conj "{ ")
         (= (last (:schema new-vtx)) :every) (update new-vtx ::ts conj "Array<")
         :else new-vtx)))))

(zen.schema/register-schema-post-process-hook!
 ::ts
 (fn [ztx schema]
   (fn [vtx data opts]
     (cond
       (exclusive-keys-child? vtx) vtx
       (= (last (:path vtx)) :keys) (update vtx ::ts conj " }")
       (= (last (:schema vtx)) :every) (update vtx ::ts conj ">")
       (= (last (:schema vtx)) :values) (update vtx ::ts conj ";")))))

(defn generate-types [path]
  (io/make-parents (str path "/package/index.js"))
  (with-open [in (io/input-stream (clojure.java.io/resource "index.js"))]
    (io/copy in (clojure.java.io/file (str path "/package/index.js"))))
  (with-open [in (io/input-stream (clojure.java.io/resource "index.d.ts"))]
    (io/copy in (clojure.java.io/file (str path "/package/index.d.ts"))))
  (with-open [in (io/input-stream (clojure.java.io/resource "package.json"))]
    (io/copy in (clojure.java.io/file (str path "/package/package.json"))))

  (let [result-file-path "./package/aidbox-types.d.ts"
        ztx  (zen.core/new-context {:package-paths [path]})
        _ (zen.core/read-ns ztx 'hl7-fhir-r4-core)
        schema (:schemas (zen.core/get-symbol ztx 'hl7-fhir-r4-core/base-schemas))
        structures (:schemas (zen.core/get-symbol ztx 'hl7-fhir-r4-core/structures))
        searches (:searches (zen.core/get-symbol ztx 'hl7-fhir-r4-core/searches))
        reference-type "export type Reference<T extends ResourceType> = {\nid: string;\nresourceType: T;\ndisplay?: string;\n};\n"
        resource-type-map-interface "export interface ResourceTypeMap {\n"
        resourcetype-type "}\n\nexport type ResourceType = keyof ResourceTypeMap;\n"
        key-value-resources (mapv (fn [[k _v]]
                                    (format "%s: %s;" k k))
                                  schema)
        search-params-start-interface "export interface SearchParams extends Record<ResourceType, unknown> {\n"
        search-params-end-interface "\n}"
        search-params-content (mapv (fn [[k v]]
                                      (println k v)
                                      (str (name k) ": {\n"
                                           (str/join "" (mapv (fn [[k1 v1]] (str "'" (name k1) "'" ": " v1 ";")) v)) "\n};\n"))
                                    (reduce (fn [acc [_ v]]
                                              (reduce (fn [third-acc item]
                                                        (zen.core/read-ns ztx (symbol (namespace (last item))))
                                                        (let [sym (last item)
                                                              schema (zen.core/get-symbol ztx (symbol sym))
                                                              schema-keys (keys (:expr schema))
                                                              type (:fhir/type schema)
                                                              attribute-name (:name schema)]

                                                          (reduce (fn [second-acc item]
                                                                    (update-in second-acc [item] assoc (keyword attribute-name) (cond (= type "reference")
                                                                                                                                      "`${ResourceType}/${string}`"
                                                                                                                                      (= type "token")
                                                                                                                                      (if (some #(:type %) (:data-types ((keyword item) (:expr schema))))
                                                                                                                                        (some #(:type %) (:data-types ((keyword item) (:expr schema))))
                                                                                                                                        "string")
                                                                                                                                      (or (= type "special") (= type "quantity"))
                                                                                                                                      "string"
                                                                                                                                      :else type)))
                                                                  third-acc
                                                                  schema-keys))) acc v))
                                            {} searches))
        resource-map-result (conj (into [reference-type resource-type-map-interface] key-value-resources) resourcetype-type)
        search-params-result (conj (into [search-params-start-interface]  search-params-content) search-params-end-interface)]



    (spit result-file-path (str/join ""  resource-map-result) :append true)
    (zen.core/read-ns ztx 'hl7-fhir-r4-core.value-set.clinical-findings)
    (zen.ftr/build-complete-ftr-index ztx)
    (mapv (fn [[k _v]]
            (println k)
            (zen.core/read-ns ztx (symbol (str "hl7-fhir-r4-core." k)))
            (zen.core/get-symbol ztx (symbol (str "hl7-fhir-r4-core." k "/schema")))
            (let [schemas-result (when (not (re-find #"-" k))
                                   (zen.schema/apply-schema ztx
                                                            {::ts []
                                                             ::require {}
                                                             ::interface-name k
                                                             ::is-type false
                                                             ::keys-in-array {}
                                                             ::exclusive-keys {}}
                                                            (zen.core/get-symbol ztx 'zen/schema)
                                                            (zen.core/get-symbol ztx (symbol (str "hl7-fhir-r4-core." k "/schema")))
                                                            {:interpreters [::ts]}))]
              (spit result-file-path (str/join "" (conj (::ts schemas-result) "\n")) :append true))) schema)

    (mapv (fn [[_k v]]
            (let [n (str/trim (str/replace (namespace v) #"hl7-fhir-r4-core." ""))
                  schema (zen.core/get-symbol ztx (symbol v))
                  structures-result (when (and (or (:type schema) (:confirms schema) (:keys schema)) (not (re-find #"-" n)) (not= n "Reference"))
                                      (zen.schema/apply-schema ztx
                                                               {::ts []
                                                                ::require {}
                                                                ::exclusive-keys {}
                                                                ::interface-name n
                                                                ::keys-in-array {}}
                                                               (zen.core/get-symbol ztx 'zen/schema)
                                                               (zen.core/get-symbol ztx (symbol v))
                                                               {:interpreters [::ts]}))]

              (spit result-file-path (str/join "" (conj (::ts structures-result) "\n")) :append true))) structures)
    (spit result-file-path (str/join "" search-params-result) :append true)
    (shell/sh "bash" "-c" (str " tar -czvf ../aidbox-javascript-sdk-v1.0.0.tgz -C package ."
                               " && rm -rf package"))
    (System/exit 0)
    :ok))

(comment
  ;; CLASSPATH
  ;; :paths (path to zrc/)
  ;; :package-paths (path to a project. project = dir with zrc/ and zen-package.edn)

  (zen.package/zen-init-deps! "/Users/pavel/Desktop/zen/test/test_project")

  (def ztx
    (zen.core/new-context
     {:package-paths ["/Users/ross/Desktop/HS/zen/test/test_project"]}))


  (zen.core/read-ns ztx 'hl7-fhir-r4-core)
  (zen.core/get-symbol ztx 'hl7-fhir-r4-core/ig)
  (zen.core/read-ns ztx 'hl7-fhir-r4-core.Patient)
  (zen.core/get-symbol ztx 'hl7-fhir-r4-core.Patient/schema)
  (zen.core/read-ns ztx 'hl7-fhir-r4-core.value-set.clinical-findings)
  (zen.core/get-symbol ztx 'hl7-fhir-r4-core.value-set.clinical-findings/value-set)
  (zen.ftr/build-complete-ftr-index ztx)
  (zen.core/get-symbol ztx 'hl7-fhir-r4-core.value-set.clinical-findings/value-set)
  ;; (get-valueset-values ztx 'hl7-fhir-r4-core.value-set.clinical-findings/value-set)
  (defn generate-types []
    (let [result-file-path "./result.ts"
          schema (:schemas (zen.core/get-symbol ztx 'hl7-fhir-r4-core/base-schemas))
          structures (:schemas (zen.core/get-symbol ztx 'hl7-fhir-r4-core/structures))
          searches (:searches (zen.core/get-symbol ztx 'hl7-fhir-r4-core/searches))
          reference-type "export type Reference<T extends ResourceType> = {\nid: string;\nresourceType: T;\ndisplay?: string;\n};\n"
          onekey-type "type OneKey<T extends object> = { [K in keyof T]-?:\n
                          ({ [P in K]: T[K] } & { [P in Exclude<keyof T, K>]?: never }) extends infer O ? { [P in keyof O]: O[P] } : never\n
                       }[keyof T];\n"
          resource-type-map-interface "export interface ResourceTypeMap {\n"
          resourcetype-type "}\n\nexport type ResourceType = keyof ResourceTypeMap;\n"
          key-value-resources (mapv (fn [[k _v]]
                                      (format "%s: %s;" k k))
                                    schema)
          search-params-start-interface "export interface SearchParams extends Record<ResourceType, unknown> {\n"
          search-params-end-interface "\n}"
          search-params-content (mapv (fn [[k v]]
                                        (println k v)
                                        (str (name k) ": {\n"
                                             (str/join "" (mapv (fn [[k1 v1]] (str "'" (name k1) "'" ": " v1 ";")) v)) "\n};\n"))
                                      (reduce (fn [acc [_ v]]
                                                (reduce (fn [third-acc item]
                                                          (zen.core/read-ns ztx (symbol (namespace (last item))))
                                                          (let [sym (last item)
                                                                schema (zen.core/get-symbol ztx (symbol sym))
                                                                schema-keys (keys (:expr schema))
                                                                type (:fhir/type schema)
                                                                attribute-name (:name schema)]

                                                            (reduce (fn [second-acc item]
                                                                      (update-in second-acc [item] assoc (keyword attribute-name) (cond (= type "reference")
                                                                                                                                        "`${ResourceType}/${string}`"
                                                                                                                                        (= type "token")
                                                                                                                                        (if (some #(:type %) (:data-types ((keyword item) (:expr schema))))
                                                                                                                                          (some #(:type %) (:data-types ((keyword item) (:expr schema))))
                                                                                                                                          "string")
                                                                                                                                        (or (= type "special") (= type "quantity"))
                                                                                                                                        "string"
                                                                                                                                        :else type)))
                                                                    third-acc
                                                                    schema-keys))) acc v))
                                              {} searches))
          resource-map-result (conj (into [reference-type onekey-type resource-type-map-interface] key-value-resources) resourcetype-type)
          search-params-result (conj (into [search-params-start-interface]  search-params-content) search-params-end-interface)]



    (spit result-file-path (str/join ""  resource-map-result) :append true)
    (zen.core/read-ns ztx 'hl7-fhir-r4-core.value-set.clinical-findings)
    (zen.ftr/build-complete-ftr-index ztx)
    (mapv (fn [[k _v]]
            (println k)
            (zen.core/read-ns ztx (symbol (str "hl7-fhir-r4-core." k)))
            (zen.core/get-symbol ztx (symbol (str "hl7-fhir-r4-core." k "/schema")))
            (let [schemas-result (when (not (re-find #"-" k))
                                   (zen.schema/apply-schema ztx
                                                            {::ts []
                                                             ::require {}
                                                             ::interface-name k
                                                             ::is-type false
                                                             ::keys-in-array {}
                                                             ::exclusive-keys {}}
                                                            (zen.core/get-symbol ztx 'zen/schema)
                                                            (zen.core/get-symbol ztx (symbol (str "hl7-fhir-r4-core." k "/schema")))
                                                            {:interpreters [::ts]}))]
              (spit result-file-path (str/join "" (conj (::ts schemas-result) "\n")) :append true))) schema)

    (mapv (fn [[_k v]]
            (let [n (str/trim (str/replace (namespace v) #"hl7-fhir-r4-core." ""))
                  schema (zen.core/get-symbol ztx (symbol v))
                  structures-result (when (and (or (:type schema) (:confirms schema) (:keys schema)) (not (re-find #"-" n)) (not= n "Reference"))
                                      (zen.schema/apply-schema ztx
                                                               {::ts []
                                                                ::require {}
                                                                ::exclusive-keys {}
                                                                ::interface-name n
                                                                ::keys-in-array {}}
                                                               (zen.core/get-symbol ztx 'zen/schema)
                                                               (zen.core/get-symbol ztx (symbol v))
                                                               {:interpreters [::ts]}))]

              (spit result-file-path (str/join "" (conj (::ts structures-result) "\n")) :append true))) structures)
    (spit result-file-path (str/join "" search-params-result) :append true)
      :ok))
  (generate-types))
