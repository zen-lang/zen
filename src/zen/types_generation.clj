(ns zen.types-generation
  (:require [zen.schema :as sut]
            [zen.core]
            [zen.package]
            [zen.utils]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.set :as set]
            [clojure.java.shell :as shell]
            [zen.ftr]
            [clojure.pprint :as pp]))

(def premitives-map
  {:integer "number"
   :string "string"
   :number "number"
   :boolean "boolean"
   :datetime "dateTime"
   :any "any"
   })

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
       (if (empty? references) "ResourceType" 
           (str/join " | " (map (fn [item] 
                                  (cond 
                                    (= (name item) "schema") (str "'" (first (set-to-string #{item})) "'")
                                    :else (str "'" (name item) "'")
                                    )) references)))
       ">"))

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
        extand (cond 
                 (not extended-resource) "" 
                 (= (first confirms) 'zenbox/Resource) " extends Resource "
                 (not= extended-resource "zen.fhir") (format " extends %s " extended-resource) 
                 :else " ")]
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
     (or (= (first (set-to-string (:confirms schema))) "Reference") (= (first (:confirms schema)) 'zenbox/Reference))
     (get-reference-union-type (:refers (:zen.fhir/reference schema)))
     (= (first (set-to-string (:confirms schema))) "BackboneElement") ""
     :else (str (first (set-to-string (:confirms schema)))))))

(defn get-exclusive-keys-values [ztx exclusive-keys ks]
  (str/join "\n" (map (fn [k]
                        (let [value (if (:zen.fhir/value-set (k ks)) (generate-valueset-union-type ztx (k ks)) (generate-confirms (k ks)))]
                          (format "%s?: %s;" (name k) value))) exclusive-keys)))

(defn get-exclusive-keys-type [ztx schema]
  (let [ks (:keys schema)
        exclusive-keys (first (:exclusive-keys schema))
        non-exclusive-keys (set/difference (set (keys ks)) exclusive-keys)
        non-exclusive-keys-type (if (= (count non-exclusive-keys) 0) ""
                                    (format "{ %s } & "
                                             (get-exclusive-keys-values ztx non-exclusive-keys ks)))
        exclusive-keys-type (get-exclusive-keys-values ztx exclusive-keys ks) 
        ]

    (format "RequireAtLeastOne<%sOneKey<{ %s }>>" non-exclusive-keys-type exclusive-keys-type)))

(zen.schema/register-compile-key-interpreter!
 [:keys ::ts]
 (fn [_ ztx ks]
   (fn [vtx data opts]
     (if-let [s (or (when (:zen.fhir/type data) (generate-name vtx data))
                    (when (:exclusive-keys data) (get-exclusive-keys-type ztx data))
                    (when (exclusive-keys-child? vtx) "")
                    (when (keys-in-array-child? vtx) "")
                    (when (:enum data) "")
                    (when (= (:validation-type data) :open) "any")
                    (when (:confirms data)
                      (if (:zen.fhir/value-set data) (generate-valueset-union-type ztx data) (generate-confirms data)))
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
        _ (zen.core/read-ns ztx 'relatient.campaign-match)
        schema (:schemas (zen.core/get-symbol ztx 'hl7-fhir-r4-core/base-schemas))
        structures (:schemas (zen.core/get-symbol ztx 'hl7-fhir-r4-core/structures))
        searches (:searches (zen.core/get-symbol ztx 'hl7-fhir-r4-core/searches))
        custom-resources ('zenbox/persistent (:tags @ztx))
        custom-resources-names (map (fn [resource] (name resource)) custom-resources)
        reference-type "export type Reference<T extends ResourceType> = {\nid: string;\nresourceType: T;\ndisplay?: string;\n};\n"
        onekey-type "type OneKey<T extends Record<string, unknown>> = { [K in keyof T]-?:\n
                          ({ [P in K]: T[K] } & { [P in Exclude<keyof T, K>]?: never }) extends infer O ? { [P in keyof O]: O[P] } : never\n
                       }[keyof T];\n"
        require-at-least-one-type "type RequireAtLeastOne<T> = { [K in keyof T]-?: Required<Pick<T, K>> & Partial<Pick<T, Exclude<keyof T, K>>>; }[keyof T];\n"
        resource-type-map-interface "export interface ResourceTypeMap {\n"
        resourcetype-type "}\n\nexport type ResourceType = keyof ResourceTypeMap;\n"
        key-value-resources (mapv (fn [n]
                                    (format "%s: %s;" n n))
                                  (concat (map (fn [[k _v]] k) schema) custom-resources-names))
        search-params-start-interface "export interface SearchParams extends Record<ResourceType, unknown> {\n"
        search-params-end-interface "\n}"
        search-params-content (mapv (fn [[k v]]
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
        resource-map-result (conj (into [reference-type onekey-type require-at-least-one-type resource-type-map-interface] key-value-resources) resourcetype-type)
        search-params-result (conj (into [search-params-start-interface]  search-params-content) search-params-end-interface)]



    (spit result-file-path (str/join ""  resource-map-result) :append true)
    (zen.core/read-ns ztx 'hl7-fhir-r4-core.value-set.clinical-findings)
    (zen.ftr/build-complete-ftr-index ztx)

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
  (def ztx (zen.core/new-context {}))

  (def my-structs-ns
    '{:ns my-sturcts

      defaults
      {:zen/tags #{zen/property zen/schema}
       :type zen/boolean}

      User
      {:zen.fhir/version "0.6.12-1",
       :confirms #{hl7-fhir-r4-core.DomainResource/schema
                   zen.fhir/Resource},
       :zen/tags #{zen/schema zen.fhir/base-schema},
       :zen.fhir/profileUri "http://hl7.org/fhir/StructureDefinition/Composition",
       :require #{:date :type :title :author :status},
       :type zen/map,
       :zen/desc "A set of healthcare-related information that is assembled together into a single logical package that provides a single coherent statement of meaning, establishes its own context and that has clinical attestation with regard to who is making the statement. A Composition defines the structure and narrative content necessary for a document. However, a Composition alone does not constitute a document. Rather, the Composition must be the first entry in a Bundle where Bundle.type=document, and any other resources referenced from Composition must be included as subsequent entries in the Bundle (for example Patient, Practitioner, Encounter, etc.).",
       :keys {:deceased {:fhir/polymorphic true,
                         :type zen/map,
                         :exclusive-keys #{#{:dateTime :boolean}},
                         :keys {:boolean {:confirms #{hl7-fhir-r4-core.boolean/schema}},
                                :_boolean {:confirms #{hl7-fhir-r4-core.Element/schema}},
                                :dateTime {:confirms #{hl7-fhir-r4-core.dateTime/schema}},
                                :_dateTime {:confirms #{hl7-fhir-r4-core.Element/schema}}},
                         :fhir/flags #{:SU :?!},
                         :zen/desc "Indicates if the individual is deceased or not"}},
       :zen.fhir/type "Composition"}})

  (zen.core/load-ns ztx my-structs-ns)

  (def r
    (sut/apply-schema ztx
                      {::ts []
                       ::require {}
                       ::exclusive-keys {}
                       ::interface-name "User"
                       ::keys-in-array {}}
                      (zen.core/get-symbol ztx 'zen/schema)
                      (zen.core/get-symbol ztx 'my-sturcts/User)
                      {:interpreters [::ts]}))

  (str/join "" (::ts r))

  )

(comment
  ;; CLASSPATH
  ;; :paths (path to zrc/)
  ;; :package-paths (path to a project. project = dir with zrc/ and zen-package.edn)

  (zen.package/zen-init-deps! "/Users/pavel/Desktop/zen/test/test_project")

  (def ztx
    (zen.core/new-context
     {:package-paths ["/Users/ross/Desktop/HS/zen/test/test_project"]})) 

  (pp/pprint ('zenbox/persistent (:tags @ztx))) 
  (pp/pprint (:tags @ztx))
  (zen.core/read-ns ztx 'relatient.campaign-match)
  (zen.core/get-symbol ztx 'relatient.campaign-match/CampaignMatch)
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
          _ (zen.core/read-ns ztx 'hl7-fhir-r4-core)
          _ (zen.core/read-ns ztx 'relatient.campaign-match)
          schema (:schemas (zen.core/get-symbol ztx 'hl7-fhir-r4-core/base-schemas))
          structures (:schemas (zen.core/get-symbol ztx 'hl7-fhir-r4-core/structures))
          searches (:searches (zen.core/get-symbol ztx 'hl7-fhir-r4-core/searches))
          custom-resources ('zenbox/persistent (:tags @ztx))
          custom-resources-names (map (fn [resource] (name resource)) custom-resources)
          reference-type "export type Reference<T extends ResourceType> = {\nid: string;\nresourceType: T;\ndisplay?: string;\n};\n"
          onekey-type "type OneKey<T extends Record<string, unknown>> = { [K in keyof T]-?:\n
                          ({ [P in K]: T[K] } & { [P in Exclude<keyof T, K>]?: never }) extends infer O ? { [P in keyof O]: O[P] } : never\n
                       }[keyof T];\n"
          require-at-least-one-type "type RequireAtLeastOne<T> = { [K in keyof T]-?: Required<Pick<T, K>> & Partial<Pick<T, Exclude<keyof T, K>>>; }[keyof T];\n"
          resource-type-map-interface "export interface ResourceTypeMap {\n"
          resourcetype-type "}\n\nexport type ResourceType = keyof ResourceTypeMap;\n"
          key-value-resources (mapv (fn [n]
                                      (format "%s: %s;" n n))
                                    (concat (map (fn [[k _v]] k) schema) custom-resources-names))
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
          resource-map-result (conj (into [reference-type onekey-type require-at-least-one-type resource-type-map-interface] key-value-resources) resourcetype-type)
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
                                                                  ::keys-in-array {}}
                                                                 (zen.core/get-symbol ztx 'zen/schema)
                                                                 (zen.core/get-symbol ztx (symbol v))
                                                                 {:interpreters [::ts]}))]

                (spit result-file-path (str/join "" (conj (::ts structures-result) "\n")) :append true))) structures)
      (spit result-file-path (str/join "" search-params-result) :append true)
      :ok))
  (generate-types)
  )
