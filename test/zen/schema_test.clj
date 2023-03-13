(ns zen.schema-test
  (:require [zen.schema :as sut]
            [matcho.core :as matcho]
            [clojure.test :as t]
            [clojure.string :as str]
            [zen.core]
            [zen.package]
            [clojure.pprint :as pp]
            [clojure.string :as str]
            [clojure.test :as t]
            [clojure.string :as str]
            [zen.core]))


(sut/register-compile-key-interpreter!
  [:keys ::ts]
  (fn [_ ztx ks]
    (fn [vtx data opts]
      (if-let [s (or (when-let [nm (:zen/name data)]
                       (str "type " (name nm) " = {"))
                     (when-let [tp (:type data)]
                       (str (name (last (:path vtx))) ": "
                            (get {'zen/string "string"}
                                 tp)
                            ";")))]
        (update vtx ::ts conj s)
        vtx))))

(sut/register-compile-key-interpreter!
  [:every ::ts]
  (fn [_ ztx every]
    (fn [vtx data opts]
      (update vtx ::ts conj "Array < "))))

(sut/register-compile-key-interpreter!
  [:type ::ts]
  (fn [_ ztx ks]
    (fn [vtx data opts]
      (-> vtx
          #_(update ::ts conj [:type (:schema vtx) (:path vtx) data])))))

(sut/register-schema-pre-process-hook!
  ::ts
  (fn [ztx schema]
    (fn [vtx data opts]
      (-> vtx
          #_(update ::ts conj [:pre (:schema vtx) (:path vtx) data])))))

(sut/register-schema-post-process-hook!
  ::ts
  (fn [ztx schema]
    (fn [vtx data opts]
      (if-let [nm (:zen/name data)]
        (update vtx ::ts conj "}")
        vtx))))


(t/deftest ^:kaocha/pending custom-interpreter-test
  (t/testing "typescript type generation"
    (def ztx (zen.core/new-context {}))

    (def my-structs-ns
      '{:ns my-sturcts

        User
        {:zen/tags #{zen/schema}
         :type zen/map
         :keys {:id {:type zen/string}
                :email {:type zen/string
                        #_#_:regex "@"}
                :name {:type zen/vector
                       :every {:type zen/map
                               :keys {:given {:type zen/vector
                                              :every {:type zen/string}}
                                      :family {:type zen/string}}}}}}})

    (zen.core/load-ns ztx my-structs-ns)

    (def ts-typedef-assert
      (str "type User = {"
           "id: string;"
           "email: string;"
           "name: Array < {"
           "given: Array < string >;"
           "family: string;"
           "}>;}"))

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
    (t/is (= ts-typedef-assert (str/join "" (::ts r))))))

(comment
  ;; CLASSPATH
  ;; :paths (path to zrc/)
  ;; :package-paths (path to a project. project = dir with zrc/ and zen-package.edn)

  (zen.package/zen-init-deps! "/Users/pavel/Desktop/zen/test_project")


  (def ztx
    (zen.core/new-context

     {:package-paths ["/Users/pavel/Desktop/zen/test_project"]}))


  (pp/pprint @ztx)

  (zen.core/read-ns ztx 'hl7-fhir-r4-core)
  (zen.core/get-symbol ztx 'hl7-fhir-r4-core/ig)
  (zen.core/read-ns ztx 'hl7-fhir-r4-core.Patient)


  (zen.core/read-ns ztx 'hl7-fhir-us-core)

  (defn read-versions [path]
    (with-open [zen-project (io/reader (str path "/zen-package.edn"))]
      (mapv (fn [version]
              (zen.core/read-ns ztx (symbol (first version)))) (:deps (edn/read (java.io.PushbackReader. zen-project))))))


  (read-versions "/Users/pavel/Desktop/zen/test_project")

  (defn get-searches [ztx versions]
    (reduce (fn [acc version]
              (zen.core/read-ns ztx (symbol version))
              (concat acc (:searches (zen.core/get-symbol ztx (symbol version))))) [] versions))


  (println (namespace (first (zen.core/get-tag ztx 'zen.fhir/base-schemas))))

  (get-searches ztx (zen.core/get-tag ztx 'zen.fhir/searches))


  (defn get-schemas [ztx, versions]
    (reduce
     (fn [acc version]
       (zen.core/read-ns ztx (symbol version))
       (concat acc (keys (:schemas (zen.core/get-symbol ztx (symbol version)))))) [] versions))

  (get-schemas ztx (zen.core/get-tag ztx 'zen.fhir/base-schemas))

  (pp/pprint (:tags @ztx))

  (namespace (first (zen.core/get-tag ztx 'zen.fhir/base-schemas)))
  (zen.core/get-symbol ztx 'zen.fhir/base-schemas)

  (zen.core/get-symbol ztx 'hl7-fhir-r4-core.Patient/schema)
  (zen.core/read-ns ztx 'hl7-fhir-r4-core.value-set.clinical-findings)
  (zen.core/get-symbol ztx 'hl7-fhir-r4-core.value-set.clinical-findings/value-set)
  (zen.core/get-symbol ztx 'hl7-fhir-r4-core.value-set.clinical-findings/value-set)
  ;; (get-valueset-values ztx 'hl7-fhir-r4-core.value-set.clinical-findings/value-set)
  (defn generate-types []


    (println (zen.core/get-symbol ztx 'hl7-fhir-r4-core/base-schema))


    (let [result-file-path "./result.ts"
          schema (:schemas (zen.core/get-symbol ztx 'hl7-fhir-r4-core/base-schemas))
          searches (:searches (zen.core/get-symbol ztx 'hl7-fhir-r4-core/searches))
          structures (:schemas (zen.core/get-symbol ztx 'hl7-fhir-r4-core/structures))
          reference-type "export type Reference<T extends ResourceType> = {\nid: string;\nresourceType: T;\ndisplay?: string;\n};\n"
          resource-type-map-interface "export interface ResourceTypeMap {\n"
          resourcetype-type "}\n\nexport type ResourceType = keyof ResourceTypeMap;\n"
          key-value-resources (mapv (fn [[k _v]]
                                      (format "%s: %s;" k k))
                                    schema)
          resource-map-result (conj (into [reference-type resource-type-map-interface] key-value-resources) resourcetype-type)
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
                                                                      (update-in second-acc [item] assoc
                                                                                 (keyword attribute-name)
                                                                                 (cond (= type "reference")
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
          search-params-result (conj (into [search-params-start-interface]  search-params-content) search-params-end-interface)]

      (spit result-file-path (str/join "" resource-map-result) :append true)
      (mapv (fn [[k _v]]
              (zen.core/read-ns ztx (symbol (str "hl7-fhir-r4-core." k)))
              (let [schemas-result (when (not (re-find #"-" k))
                                     (sut/apply-schema ztx
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
                                        (sut/apply-schema ztx
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

(t/deftest ^:kaocha/pending custom-interpreter-test
  (t/testing "should correctly generate"
    (def ztx (zen.core/new-context {}))
    (zen.core/get-tag ztx 'zen/type)

    (def my-ns
      '{:ns my

        defaults
        {:zen/tags #{zen/schema zen/is-key}
         :zen/desc "only primitive default values are supported currently"
         :for #{zen/map}
         :priority 100
         :type zen/map
         :key {:type zen/keyword}
         :values {:type zen/case
                  :case [{:when {:type zen/boolean}}
                         {:when {:type zen/date}}
                         {:when {:type zen/datetime}}
                         {:when {:type zen/integer}}
                         {:when {:type zen/keyword}}
                         {:when {:type zen/number}}
                         {:when {:type zen/qsymbol}}
                         {:when {:type zen/regex}}
                         {:when {:type zen/string}}
                         {:when {:type zen/symbol}}]}}

        HumanName
        {:zen/tags #{zen/schema}
         :type zen/map
         :require #{:family :given}
         :keys {:given {:type zen/vector
                        :minItems 1
                        :every {:type zen/string}}
                :family {:type zen/string}}}

        DefaultHumanName
        {:zen/tags #{zen/schema}
         :type zen/map
         :my/defaults {:family "None"}}

        User
        {:zen/tags #{zen/schema}
         :type zen/map
         :keys {:id {:type zen/string}
                :name {:type zen/vector
                       :every {:confirms #{HumanName DefaultHumanName}}}
                :active {:type zen/boolean}
                :count {:type zen/number}}
         :zen.fhir/type "Patient"}})

    (zen.core/load-ns ztx my-structs-ns)

    #_(matcho/match (zen.core/errors ztx) #_"NOTE: FIXME: keys that use get-cached during compile time won't be recompiled when these schemas used in get-cached updated. E.g. adding new is-key for zen/schema won't cause zen/schema recompile and the key won't be recognized by zen/schema validation"
                    empty?)

    (def data
      {:id "foo"
       :email "bar@baz"
       :name [{:given ["foo"]}]})

    (def r
      (sut/apply-schema ztx
                        {::ts []}
                        (zen.core/get-symbol ztx 'zen/schema)
                        (zen.core/get-symbol ztx 'my-sturcts/User)
                        {:interpreters [::ts]}))

    (t/is (= ts-typedef-assert (str/join "" (distinct (::ts r)))))))


    (matcho/match (::with-defaults r)
                  {:id "foo"
                   :email "bar@baz"
                   :active true
                   :name   [{:family "None"
                             :given  ["foo"]}]})

    User
    {:zen/tags #{zen/schema}
     :type zen/map
     :require #{:string :boolean},
     :keys {:string {:type zen/string}
            :object {:type zen/map
                     :keys {:string {:type zen/boolean}
                            :booalen {:type zen/boolean}
                            :object-2 {:type zen/map
                                       :keys {:string {:type zen/boolean}
                                              :booalen {:type zen/boolean}}
                                       :require #{:booalen}}}
                     :require #{:string}}
            :boolean {:type zen/boolean}
            :number {:type zen/number}}
     :zen.fhir/type "Patient"}})

(zen.core/load-ns ztx my-structs-ns)

(def ts-typedef-assert
  (str "interface Patient { string:string;boolean?:boolean;number?:number;enumV?:'phone' | 'email';arrayConfirms?:Array<HumanName>;confirms?:HumanName; }"))

(def r
  (sut/apply-schema ztx
                    {::ts []
                     ::require {}}
                    (zen.core/get-symbol ztx 'zen/schema)
                    (zen.core/get-symbol ztx 'my-sturcts/User)
                    {:interpreters [::ts ::require]}))

(t/deftest ^:kaocha/pending dynamic-confirms-cache-reset-test
  (def ztx (zen.core/new-context {}))

  #_"NOTE: you can drop cache to see that this fixes validation"
  #_(swap! ztx
           dissoc
           :errors
           :zen.v2-validation/compiled-schemas
           :zen.v2-validation/prop-schemas
           :zen.v2-validation/cached-pops)

  (def my-ns
    '{:ns my-ns

      b {:zen/tags #{zen/schema}
         :type zen/string
         :const {:value "foo"}}

      a {:zen/tags #{zen/schema}
         :my-ns/test-key "should be no errorors, this key is registred via my-ns/test-key"
         :confirms #{b}}})

  (zen.core/load-ns ztx my-ns)

  (matcho/match
    (zen.core/validate ztx #{'my-ns/a} "foo")
    {:errors empty?})

  (zen.core/load-ns ztx (assoc-in my-ns ['b :const :value] "bar"))

  (matcho/match
    (zen.core/validate ztx #{'my-ns/a} "foo")
    {:errors [{} nil]}))


(t/deftest ^:kaocha/pending dynamic-key-schema-cache-reset-test
  (def ztx (zen.core/new-context {}))

  #_"NOTE: you can drop cache to see that this fixes validation"
  #_(swap! ztx
           dissoc
           :errors
           :zen.v2-validation/compiled-schemas
           :zen.v2-validation/prop-schemas
           :zen.v2-validation/cached-pops)

  (def my-ns
    '{:ns my-ns

      test-key
      {:zen/tags #{zen/is-key zen/schema}
       :zen/desc "just a key that should be allowed in any schema"
       :type zen/any}

      a {:zen/tags #{zen/schema}
         :my-ns/test-key "should be no errorors, this key is registred via my-ns/test-key"}})

  (zen.core/load-ns ztx my-ns)

  (matcho/match
    (zen.core/errors ztx)
    empty?))
