(ns zen.types-generation
  (:require [zen.schema]
            [zen.core]
            [zen.package] 
            [clojure.string :as str]))

  (def premitives-map
    {:integer "number"})

  (def non-parsable-premitives
    {:string "string" :number "number" :boolean "boolean"})

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

  (defn generate-interface [vtx]
    (str "interface " (::interface-name vtx) " "))

  (defn generate-name
    [vtx data]
    (str (get-desc data)
         (if (::is-type vtx)
           (generate-type vtx data)
           (generate-interface vtx))))

  (defn generate-confirms [data]
    (str
     (cond
       (= (first (set-to-string (:confirms data))) "Reference")
       (get-reference-union-type (set-to-string (:refers (:zen.fhir/reference data))))
       (= (first (set-to-string (:confirms data))) "BackboneElement") ""
       :else (str (first (set-to-string (:confirms data)))))))

  (zen.schema/register-compile-key-interpreter!
   [:keys ::ts]
   (fn [_ ztx ks]
     (fn [vtx data opts]
       (if-let [s (or (when (:zen.fhir/type data) (generate-name vtx data))
                      (when (:exclusive-keys data) (get-exlusive-keys-type data))
                      (when (exclusive-keys-child? vtx) "")
                      (when (keys-in-array-child? vtx) "")
                      (when (:confirms data) (generate-confirms data))
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

  (zen.schema/register-schema-pre-process-hook!
   ::ts
   (fn [ztx schema]
     (fn [vtx data opts]
       (let [new-vtx (cond
                       (and (not (:keys data)) (empty? (:path vtx)))
                       (assoc vtx ::is-type true)
                       (and (:confirms data) (:keys data))
                       (update vtx ::keys-in-array conj (generate-map-keys-in-array vtx data))
                       (:exclusive-keys data)
                       (update vtx ::exclusive-keys conj (generate-exclusive-keys vtx data))
                       (:require data)
                       (update vtx ::require conj (generate-require vtx data))
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
    (def ztx
      (zen.core/new-context
       {:package-paths [path]}))

    (zen.core/read-ns ztx 'hl7-fhir-r4-core)

    (def schema (:schemas (zen.core/get-symbol ztx 'hl7-fhir-r4-core/base-schemas)))
    (def structures (:schemas (zen.core/get-symbol ztx 'hl7-fhir-r4-core/structures)))

    (defn generate-resource-type-map []
      (let [reference-type "export type Reference<T extends ResourceType> = {\nid: string;\nresourceType: T;\ndisplay?: string;\n};\n"
            resource-type-map-interface "export interface ResourceTypeMap {\n"
            resourcetype-type "}\n\nexport type ResourceType = keyof ResourceTypeMap;\n"
            key-value-resources (mapv (fn [[k _v]]
                                        (format "%s: %s;" k k))
                                      schema)
            result (conj (into [reference-type resource-type-map-interface] key-value-resources) resourcetype-type)]
        (spit "./result.ts" (str/join "" result) :append true)))

    (generate-resource-type-map)

    (mapv (fn [[k _v]]
            (println k)
            (zen.core/read-ns ztx (symbol (str "hl7-fhir-r4-core." k)))
            (zen.core/get-symbol ztx (symbol (str "hl7-fhir-r4-core." k "/schema")))
            (when (not (re-find #"-" k))
              (def r (zen.schema/apply-schema ztx
                                              {::ts []
                                               ::require {}
                                               ::interface-name k
                                               ::is-type false
                                               ::keys-in-array {}
                                               ::exclusive-keys {}}
                                              (zen.core/get-symbol ztx 'zen/schema)
                                              (zen.core/get-symbol ztx (symbol (str "hl7-fhir-r4-core." k "/schema")))
                                              {:interpreters [::ts]})))
            (spit "./result.ts" (str/join "" (conj (::ts r) ";\n")) :append true)) schema)

    (do (mapv (fn [[_k v]]
                (let [n (str/trim (str/replace (namespace v) #"hl7-fhir-r4-core." ""))
                      ns  (zen.core/read-ns ztx (symbol (namespace v)))
                      schema (zen.core/get-symbol ztx (symbol v))]

                  (when (and (or (:type schema) (:confirms schema) (:keys schema)) (not (re-find #"-" n)) (not= n "Reference"))
                    ((def r (zen.schema/apply-schema ztx
                                                     {::ts []
                                                      ::require {}
                                                      ::exclusive-keys {}
                                                      ::interface-name n
                                                      ::keys-in-array {}}
                                                     (zen.core/get-symbol ztx 'zen/schema)
                                                     (zen.core/get-symbol ztx (symbol v))
                                                     {:interpreters [::ts]}))
                     (spit "./result.ts" (str/join "" (conj (::ts r) ";\n")) :append true))))) structures) :ok))
  