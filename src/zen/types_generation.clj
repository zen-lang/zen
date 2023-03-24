(ns zen.types-generation
  (:require [zen.schema :as sut]
            [zen.core]
            [zen.package]
            [zen.utils]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.set :as set]
            [clojure.java.shell :as shell]
            [clojure.edn :as edn]
            [taoensso.nippy :as nippy]))


(def output-reset "\u001B[0m")
(def green "\u001B[32m")
(def blue "\u001B[34m")

(def premitives-map
  {:integer "number"
   :string "string"
   :number "number"
   :boolean "boolean"
   :datetime "dateTime"
   :any "any"})

(def prepared-interfaces
  {:onekey-type "type OneKey<T extends Record<string, unknown>> = { [K in keyof T]-?:\n
                          ({ [P in K]: T[K] } & { [P in Exclude<keyof T, K>]?: never }) extends infer O ? { [P in keyof O]: O[P] } : never\n
                       }[keyof T];\n"
   :require-at-least-one-type "type RequireAtLeastOne<T> = { [K in keyof T]-?: Required<Pick<T, K>> & Partial<Pick<T, Exclude<keyof T, K>>>; }[keyof T];\n"
   :reference-type "export type Reference<T extends ResourceType> = {\nid: string;\nresourceType: T;\ndisplay?: string;\n};\n"
   :resourcetype-type "}\n\nexport type ResourceType = keyof ResourceTypeMap;\n"
   :subs-subscription "export interface SubsSubscription extends DomainResource {\nstatus: 'active' | 'off';
                       trigger: Partial<Record<ResourceType, { event: Array<'all' | 'create' | 'update' | 'delete'>; filter?: unknown }>>;
                       channel: {\ntype: 'rest-hook';\nendpoint: string;\npayload?: { content: string; contentType: string; context: unknown };
                       headers?: Record<string, string>;\ntimeout?: number;\n};\n}"})

(def non-parsable-premitives
  {:string "string"
   :number "number"
   :boolean "boolean"})

(defn set-to-string [vtx value]
  (reduce
   (fn [acc item]
     (cond
       (set? item) (set-to-string vtx item)
       (keyword? item)
       (conj acc
             (str/replace (name item) (str (::version vtx) ".") ""))
       :else
       (conj acc
             (str/replace (namespace item) (str (::version vtx) ".") ""))))
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

(defn get-reference-union-type [vtx references]
  (str "Reference<"
       (if (empty? references) "ResourceType"
           (str/join " | " (map (fn [item]
                                  (cond
                                    (= (name item) "schema") (str "'" (first (set-to-string vtx #{item})) "'")
                                    :else (str "'" (name item) "'"))) references)))
       ">"))

(defn generate-type-value
  [data vtx]
  (if (:confirms data)
    (if
     (get-in data [:zen.fhir/reference :refers])
      (get-reference-union-type vtx (set-to-string vtx (get-in data [:zen.fhir/reference :refers])))
      (str/join " | " (set-to-string vtx (:confirms data))))
    (if
     ((keyword (name (:type data))) premitives-map)
      ((keyword (name (:type data))) premitives-map)
      (name (:type data)))))

(defn generate-type [vtx data]
  (when (and (empty? (::ts vtx))
             (not ((keyword (::interface-name vtx)) non-parsable-premitives)))
    (str "type " (::interface-name vtx)  " = " (generate-type-value data vtx))))

(defn generate-interface [vtx {confirms :confirms}]
  (let [extended-resource (first (set-to-string vtx confirms))
        extand (cond
                 (not extended-resource) ""
                 (= (first confirms) 'zenbox/Resource) " extends Resource "
                 (not= extended-resource "zen.fhir") (format " extends %s " extended-resource)
                 :else " ")]
    (str "export interface " (::interface-name vtx) extand)))

(defn generate-name
  [vtx data]
  (str (get-desc data)
       (if (::is-type vtx)
         (generate-type vtx data)
         (generate-interface vtx data))))

(defn get-valueset-values [ztx value-set]
  (let [{uri :uri} (zen.core/get-symbol ztx value-set)
        ftr-index (get-in @ztx [:zen.fhir/ftr-index "init"])
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

(defn generate-valueset-type [ztx vtx schema]
  (let [confirms (first (set-to-string vtx (:confirms schema)))
        valueset-values (get-valueset-values ztx (get-in schema [:zen.fhir/value-set :symbol]))]
    (cond 
      (= confirms "CodeableConcept") "CodeableConcept"
      (or (> (count valueset-values) 20) (= (count valueset-values) 0)) "string" 
      :else (str/join " | " (map #(format "'%s'" %) valueset-values)))))

(defn generate-confirms [vtx schema]
  (str
   (cond
     (or (= (first (set-to-string vtx (:confirms schema))) "Reference") (= (first (:confirms schema)) 'zenbox/Reference))
     (get-reference-union-type vtx (:refers (:zen.fhir/reference schema)))
     (= (first (set-to-string vtx (:confirms schema))) "BackboneElement") ""
     :else (str (first (set-to-string vtx (:confirms schema)))))))



(defn get-exclusive-keys-values [ztx vtx exclusive-keys ks]
  (str/join "\n" (map (fn [k]
                        (let [value (if (:zen.fhir/value-set (k ks)) (generate-valueset-type ztx vtx (k ks)) (generate-confirms vtx (k ks)))]
                          (format "%s?: %s;" (name k) value))) exclusive-keys)))

(defn get-exclusive-keys-type [ztx vtx schema]
  (let [ks (:keys schema)
        exclusive-keys (first (:exclusive-keys schema))
        non-exclusive-keys (set/difference (set (keys ks)) exclusive-keys)
        non-exclusive-keys-type (if (= (count non-exclusive-keys) 0) ""
                                    (format "{ %s } & "
                                            (get-exclusive-keys-values ztx vtx non-exclusive-keys ks)))
        exclusive-keys-type (get-exclusive-keys-values ztx vtx exclusive-keys ks)]

    (format "RequireAtLeastOne<%sOneKey<{ %s }>>" non-exclusive-keys-type exclusive-keys-type)))

(zen.schema/register-compile-key-interpreter!
 [:keys ::ts]
 (fn [_ ztx ks]
   (fn [vtx data opts]
     (if-let [s (or (when (:zen.fhir/type data) (generate-name vtx data))
                    (when (:exclusive-keys data) (get-exclusive-keys-type ztx vtx data))
                    (when (exclusive-keys-child? vtx) "")
                    (when (keys-in-array-child? vtx) "")
                    (when (:enum data) "")
                    (when (= (:validation-type data) :open) "any")
                    (when (:confirms data)
                      (if (:zen.fhir/value-set data) (generate-valueset-type ztx vtx data) (generate-confirms vtx data)))
                    (when-let [tp (and
                                   (= (:type vtx) 'zen/symbol)
                                   (not (= (last (:path vtx)) :every))
                                   (not (:enum data))
                                   (or (= (:type data) 'zen/string)
                                       (= (:type data) 'zen/number)
                                       (= (:type data) 'zen/boolean)
                                       (= (:type data) 'zen/datetime)
                                       (= (:type data) 'zen/integer)
                                       (= (:type data) 'zen/any))
                                   (:type data))]
                      ((keyword (name tp)) premitives-map))
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
         (and (= (last (:path new-vtx)) :keys) (= (::interface-name vtx) "Resource")) 
         (update new-vtx ::ts conj "{ \n resourceType: ResourceType;")
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

(defn get-ftr-index
  [ztx path]
  (let [in (clojure.java.io/input-stream path)
        out (java.io.ByteArrayOutputStream.)
        _ (clojure.java.io/copy in out)
        ftr-index (->> (.toByteArray out)
                       (nippy/thaw))]
    
     (swap! ztx assoc :zen.fhir/ftr-index ftr-index)))

(defn generate-types-for-version [path version result-file-path]
  (let [ztx  (zen.core/new-context {:package-paths [path]})
        _ (zen.core/read-ns ztx (symbol version))
        _ (zen.core/read-ns ztx 'relatient.campaign-match)
        schema (:schemas (zen.core/get-symbol ztx (symbol (str version "/base-schemas"))))
        structures (:schemas (zen.core/get-symbol ztx (symbol (str version "/structures"))))
        custom-resources ('zenbox/persistent (:tags @ztx))]

    (zen.core/read-ns ztx 'hl7-fhir-r4-core.value-set.clinical-findings)
    (println "Building FTR index...")
    (println (str blue "It may take some time" output-reset))
    (get-ftr-index ztx (str path "/zen-packages/" version "/index.nippy")) 
    (println "Done")



    (println "Custom resource generation...")
    (mapv (fn [resource]
            (let [n (name resource)
                  schemas-result
                  (zen.schema/apply-schema ztx
                                           {::ts []
                                            ::require {}
                                            ::interface-name n
                                            ::is-type false
                                            ::keys-in-array {}
                                            ::exclusive-keys {}}
                                           (zen.core/get-symbol ztx 'zen/schema)
                                           (zen.core/get-symbol ztx (symbol resource))
                                           {:interpreters [::ts]})]
              (spit result-file-path (str/join "" (conj (::ts schemas-result) "\n")) :append true))) custom-resources)
    (println "Done")

    (println "Resource generation...")
    (mapv (fn [[k _v]]
            (zen.core/read-ns ztx (symbol (str version "." k)))
            (zen.core/get-symbol ztx (symbol (str version "." k "/schema")))
            (let [schemas-result (when (not (re-find #"-" k))
                                   (zen.schema/apply-schema ztx
                                                            {::ts []
                                                             ::require {}
                                                             ::interface-name k
                                                             ::is-type false
                                                             ::version version
                                                             ::keys-in-array {}
                                                             ::exclusive-keys {}}
                                                            (zen.core/get-symbol ztx 'zen/schema)
                                                            (zen.core/get-symbol ztx (symbol (str version "." k "/schema")))
                                                            {:interpreters [::ts]}))]
              (spit result-file-path (str/join "" (conj (::ts schemas-result) "\n")) :append true))) schema)


    (mapv (fn [[_k v]]
            (let [n (str/trim (str/replace (namespace v) (str version ".") ""))
                  schema (zen.core/get-symbol ztx (symbol v))
                  structures-result (when (and (or (:type schema) (:confirms schema) (:keys schema)) (not (re-find #"-" n)) (not= n "Reference"))
                                      (zen.schema/apply-schema ztx
                                                               {::ts []
                                                                ::require {}
                                                                ::exclusive-keys {}
                                                                ::interface-name n
                                                                ::version version
                                                                ::keys-in-array {}}
                                                               (zen.core/get-symbol ztx 'zen/schema)
                                                               (zen.core/get-symbol ztx (symbol v))
                                                               {:interpreters [::ts]}))]

              (spit result-file-path (str/join "" (conj (::ts structures-result) "\n")) :append true))) structures)
    :ok))


(defn read-versions [ztx path]
  (println "Reading zen packages...")
  (with-open [zen-project (io/reader (str path "/zen-package.edn"))]
    (mapv (fn [package]
            (println "Reading " (first package))
            (zen.core/read-ns ztx (symbol (first package)))) (:deps (edn/read (java.io.PushbackReader. zen-project))))))


(defn get-searches [ztx versions]
  (println "Generating search parameters...")
  (reduce (fn [acc version]
            (zen.core/read-ns ztx (symbol version))
            (concat acc (:searches (zen.core/get-symbol ztx (symbol version))))) [] versions))


(defn get-schemas [ztx, versions]
  (println "Getting schemas...")
  (reduce
   (fn [acc version]
     (zen.core/read-ns ztx (symbol version))
     (concat acc (keys (:schemas (zen.core/get-symbol ztx (symbol version)))))) [] versions))

(defn get-resources [schema custom-resources-names]
  (mapv (fn [n]
          (format "%s: %s;" n n))
        (conj (concat schema custom-resources-names) "SubsSubscription")))

(defn search-params-generator [ztx, searches]
  (reduce
   (fn [acc [_ v]]
     (reduce
      (fn [second-acc item]
        (zen.core/read-ns ztx (symbol (namespace (last item))))
        (let [sym (last item)
              schema (zen.core/get-symbol ztx (symbol sym))
              schema-keys (keys (:expr schema))
              type (:fhir/type schema)
              attribute-name (:name schema)]

          (reduce
           (fn [third-acc item]
             (let [is-token (= type "token")
                   token-type (when is-token (some #(:type %) (:data-types ((keyword item) (:expr schema)))))
                   token-parsed-type (when token-type ((keyword token-type) non-parsable-premitives))]
               (update-in third-acc [item] assoc
                          (keyword attribute-name)
                          (cond (= type "reference")
                                "`${ResourceType}/${string}`"
                                is-token
                                (if token-type
                                  (if token-parsed-type token-parsed-type "string")
                                  "string")
                                (or (= type "special") (= type "quantity"))
                                "string"
                                :else type))))
           second-acc
           schema-keys)))
      acc v))
   {} searches))

(defn get-search-params [ztx, searches]
  (mapv (fn [[k v]]
          (str (name k) ": {\n"
               (str/join "" (mapv (fn [[k1 v1]] (str "'" (name k1) "'" ": " v1 ";")) v)) "\n};\n"))
        (search-params-generator ztx searches)))

(defn generate-types [path result-file-path] 
  (let [ztx  (zen.core/new-context {:package-paths [path]})
        _ (read-versions ztx path)
        _ (println "Done")
        searches (get-searches ztx (zen.core/get-tag ztx 'zen.fhir/searches))
        _ (println "Done")
        schema  (get-schemas ztx (zen.core/get-tag ztx 'zen.fhir/base-schemas))
        _ (println "Done")
        resource-type-map-interface "export interface ResourceTypeMap {\n"
        resourcetype-type (:resourcetype-type prepared-interfaces)
        reference-type (:reference-type prepared-interfaces)
        onekey-type (:onekey-type prepared-interfaces)
        require-at-least-one-type (:require-at-least-one-type prepared-interfaces)
        subs-subscription (:subs-subscription prepared-interfaces)
        custom-resources ('zenbox/persistent (:tags @ztx))
        custom-resources-names (map (fn [resource] (name resource)) custom-resources)
        key-value-resources (get-resources schema custom-resources-names)
        resource-map-result (conj (into [reference-type onekey-type require-at-least-one-type resource-type-map-interface] key-value-resources) resourcetype-type subs-subscription)
        search-params-start-interface "export interface SearchParams extends Record<ResourceType, unknown> {\n"
        search-params-end-interface "\n}"
        search-params-content (get-search-params ztx searches)
        search-params-result (conj (into [search-params-start-interface]  search-params-content) search-params-end-interface)]

    (spit result-file-path (str/join ""  resource-map-result) :append true)

    (println "Type generation...")
    (generate-types-for-version path (namespace (first (zen.core/get-tag ztx 'zen.fhir/base-schemas))) result-file-path)
    (println "Done")

    (spit result-file-path (str/join "" search-params-result) :append true)))


(defn get-sdk [path]
  (io/make-parents (str path "/package/index.js"))
  (with-open [zen-project (io/reader (str path "/zen-package.edn"))]
    (first (:deps (edn/read (java.io.PushbackReader. zen-project)))))
  (with-open [in (io/input-stream (clojure.java.io/resource "index.js"))]
    (io/copy in (clojure.java.io/file (str path "/package/index.js"))))
  (with-open [in (io/input-stream (clojure.java.io/resource "index.d.ts"))]
    (io/copy in (clojure.java.io/file (str path "/package/index.d.ts"))))
  (with-open [in (io/input-stream (clojure.java.io/resource "package.json"))]
    (io/copy in (clojure.java.io/file (str path "/package/package.json"))))

  (generate-types path "./package/aidbox-types.d.ts")
  (println "Archive generation")
  (shell/sh "bash" "-c" (str " tar -czvf ../aidbox-javascript-sdk-v1.0.0.tgz -C package ."
                             " && rm -rf package"))
  (println "Done")

  (System/exit 0))

(defn get-ts-types [path]
  (generate-types path "../aidbox-types.d.ts")
  (println "Done"))

#_(comment
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
  (get-ftr-index ztx "/Users/ross/Desktop/HS/zen/test_project/zen-packages/hl7-fhir-r4-core/index.nippy")
  (zen.core/get-symbol ztx 'hl7-fhir-r4-core.value-set.clinical-findings/value-set) 
  (get-valueset-values ztx 'hl7-fhir-r4-core.value-set.clinical-findings/value-set)
  
  (get-in @ztx [:zen.fhir/ftr-index "init"])
  
  (defn generate-types []
    (let [result-file-path "./result.ts"
          _ (zen.core/read-ns ztx 'hl7-fhir-r4-core)
          _ (zen.core/read-ns ztx 'relatient.campaign-match)
          schema (:schemas (zen.core/get-symbol ztx 'hl7-fhir-r4-core/base-schemas))
          structures (:schemas (zen.core/get-symbol ztx 'hl7-fhir-r4-core/structures))
          searches (:searches (zen.core/get-symbol ztx 'hl7-fhir-r4-core/searches))
          custom-resources ('zenbox/persistent (:tags @ztx))
          custom-resources-names (map (fn [resource] (name resource)) custom-resources)
          reference-type (:reference-type prepared-interfaces)
          onekey-type (:onekey-type prepared-interfaces)
          require-at-least-one-type (:require-at-least-one-type prepared-interfaces)
          subs-subscription (:subs-subscription prepared-interfaces)
          resource-type-map-interface "export interface ResourceTypeMap {\n"
          resourcetype-type "}\n\nexport type ResourceType = keyof ResourceTypeMap;\n"
          key-value-resources (mapv (fn [n]
                                      (format "%s: %s;" n n))
                                    (conj (concat (map (fn [[k _v]] k) schema) custom-resources-names) "SubsSubscription"))
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
          resource-map-result (conj (into [reference-type onekey-type require-at-least-one-type resource-type-map-interface] key-value-resources) resourcetype-type subs-subscription)
          search-params-result (conj (into [search-params-start-interface]  search-params-content) search-params-end-interface)]



      (spit result-file-path (str/join ""  resource-map-result) :append true)
      (zen.core/read-ns ztx 'hl7-fhir-r4-core.value-set.clinical-findings) 
      (mapv (fn [[k _v]]
              (println k)
              (zen.core/read-ns ztx (symbol (str "hl7-fhir-r4-core." k)))
              (zen.core/get-symbol ztx (symbol (str "hl7-fhir-r4-core." k "/schema")))
              (let [schemas-result (when (not (re-find #"-" k))
                                     (zen.schema/apply-schema ztx
                                                              {::ts []
                                                               ::require {}
                                                               ::interface-name k
                                                               ::version "hl7-fhir-r4-core"
                                                               ::is-type false
                                                               ::keys-in-array {}
                                                               ::exclusive-keys {}}
                                                              (zen.core/get-symbol ztx 'zen/schema)
                                                              (zen.core/get-symbol ztx (symbol (str "hl7-fhir-r4-core." k "/schema")))
                                                              {:interpreters [::ts]}))]
                (spit result-file-path (str/join "" (conj (::ts schemas-result) "\n")) :append true))) schema)


      (mapv (fn [resource]
              (println "resource" resource)
              (let [n (name resource)
                    schemas-result
                    (zen.schema/apply-schema ztx
                                             {::ts []
                                              ::require {}
                                              ::interface-name n
                                              ::is-type false
                                              ::keys-in-array {}
                                              ::exclusive-keys {}}
                                             (zen.core/get-symbol ztx 'zen/schema)
                                             (zen.core/get-symbol ztx (symbol resource))
                                             {:interpreters [::ts]})]
                (spit result-file-path (str/join "" (conj (::ts schemas-result) "\n")) :append true))) custom-resources)

      (mapv (fn [[_k v]]
              (let [n (str/trim (str/replace (namespace v) #"hl7-fhir-r4-core." ""))
                    schema (zen.core/get-symbol ztx (symbol v))
                    structures-result (when (and (or (:type schema) (:confirms schema) (:keys schema)) (not (re-find #"-" n)) (not= n "Reference"))
                                        (zen.schema/apply-schema ztx
                                                                 {::ts []
                                                                  ::require {}
                                                                  ::exclusive-keys {}
                                                                  ::interface-name n
                                                                  ::version "hl7-fhir-r4-core"
                                                                  ::keys-in-array {}}
                                                                 (zen.core/get-symbol ztx 'zen/schema)
                                                                 (zen.core/get-symbol ztx (symbol v))
                                                                 {:interpreters [::ts]}))]

                (spit result-file-path (str/join "" (conj (::ts structures-result) "\n")) :append true))) structures)
      (spit result-file-path (str/join "" search-params-result) :append true)
      :ok))
  (generate-types)
  )
