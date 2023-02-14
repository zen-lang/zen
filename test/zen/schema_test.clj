(ns zen.schema-test
  (:require [zen.schema :as sut]
            [matcho.core :as matcho]
            [clojure.test :as t]
            [clojure.string :as str]
            [zen.core]
            [zen.package]
            [clojure.pprint :as pp]))

(defn set-to-string [value]
  (reduce
   (fn [acc item]
     (println item)
     (if (set? item)
       (set-to-string item)
       (if
        (keyword? item)
         (conj acc
               (str/replace (name item) #"hl7-fhir-r4-core." ""))
         (conj acc
               (str/replace (namespace item) #"hl7-fhir-r4-core." "")))))
   [] value))

(defn get-desc [data]
  (if (:zen/desc data) (str " /* " (:zen/desc data) " */ ") ""))

(sut/register-compile-key-interpreter!
 [:keys ::ts]
 (fn [_ ztx ks]
   (fn [vtx data opts]
    ;; (println "COMPILE")
    ;;  (println "path:")
    ;;  (pp/pprint (:path vtx)) 
    ;;  (println "type:")
    ;;  (pp/pprint (:type vtx)) 
    ;;  (println "schema:")
    ;;  (pp/pprint (:schema vtx))
    ;;  (println "data:")
    ;;  (pp/pprint data)
    ;;  (println "ts:")
    ;;  (pp/pprint (:zen.schema-test/ts vtx))
     (if-let [s (or (when-let [nm (:zen.fhir/type data)]
                      (str (get-desc data) "interface " (name nm) " "))
                    (when (:confirms data)
                      (str
                       (get {'zen/string "string"} (:type data))
                       (cond
                         (= (first (set-to-string (:confirms data))) "Reference")
                         (str "Reference<" (str/join " | " (map (fn [item] (str "'" item "'")) (set-to-string (:refers (:zen.fhir/reference data))))) ">")
                         (= (first (set-to-string (:confirms data))) "BackboneElement") ""
                         :else (str
                                (first (set-to-string (:confirms data)))))))

                    (when-let [tp (and
                                   (= (:type vtx) 'zen/symbol)
                                   (not (= (last (:path vtx)) :every))
                                   (not (:enum data))
                                   (or (= (:type data) 'zen/string)
                                       (= (:type data) 'zen/number)
                                       (= (:type data) 'zen/boolean))
                                   (:type data))]
                      (str (name tp)))
                    (when (and (= (last (:path vtx)) :every) (= (last (:schema vtx)) 'zen/string))
                      "string; "))]
       (update vtx ::ts conj s)
       vtx))))

(zen.schema/register-schema-pre-process-hook!
 ::ts
 (fn [ztx schema]
   (fn [vtx data opts]
    ;;  (println "PRE")
    ;;  (println "path:")
    ;;  (pp/pprint (:path vtx)) 
    ;;  (println "type:")
    ;;  (pp/pprint (:type vtx)) 
    ;;  (println "schema:")
    ;;  (pp/pprint (:schema vtx))
    ;;  (println "data:")
    ;;  (println data)
    ;;  (println "ts:")
    ;;  (pp/pprint (:zen.schema-test/ts vtx))
     (cond
       (= (last (:path vtx)) :zen.fhir/type) vtx
       (= (last (:schema vtx)) :enum)
       (update vtx ::ts conj (str  (str/join " | " (map (fn [item] (str "'" (:value item) "'")) data))))
       (= (last (:schema vtx)) :values)
       (update vtx ::ts conj (get-desc data) (str (name (last (:path vtx))) ":"))
       (= (last (:path vtx)) :keys) (update vtx ::ts conj "{ ")
       (= (last (:schema vtx)) :every) (update vtx ::ts conj "Array<")))))

(zen.schema/register-schema-post-process-hook!
 ::ts
 (fn [ztx schema]
   (fn [vtx data opts]
     (println "POST")
     (println "path:")
     (pp/pprint (:path vtx))
     (println "type:")
     (pp/pprint (:type vtx))
     (println "schema:")
     (pp/pprint (:schema vtx))
     (println "data:")
     (pp/pprint data)
     (println "ts:")
     (pp/pprint (:zen.schema-test/ts vtx))

     (cond
       (= (last (:path vtx)) :keys) (update vtx ::ts conj " }")
       (= (last (:schema vtx)) :every) (update vtx ::ts conj ">")
       (= (last (:schema vtx)) :values) (update vtx ::ts conj ";")))))


(t/deftest patient-test
  (t/testing "patient test"
    (def ztx (zen.core/new-context {}))

    (def my-structs-ns
      '{:ns my-sturcts

        defaults
        {:zen/tags #{zen/property zen/schema}
         :type zen/boolean}

        User
        {:zen.fhir/version "0.6.12-1",
         :confirms #{hl7-fhir-r4-core.DomainResource/schema zen.fhir/Resource},
         :zen/tags #{zen/schema zen.fhir/base-schema},
         :zen.fhir/profileUri "http://hl7.org/fhir/StructureDefinition/Patient",
         :zen/file
         "/Users/ross/Desktop/HS/zen/test/test_project/zen-packages/hl7-fhir-r4-core/zrc/hl7-fhir-r4-core/Patient.edn",
         :type zen/map,
         :zen/desc
         "Demographics and other administrative information about an individual or animal receiving care or other health-related services.",
         :zen/name hl7-fhir-r4-core.Patient/schema,
         :keys
         {:address
          {:type zen/vector,
           :every
           {:confirms #{hl7-fhir-r4-core.Address/schema}, :fhir/flags #{:SU}, :zen/desc "An address for the individual"}},
          :managingOrganization
          {:confirms #{hl7-fhir-r4-core.Reference/schema zen.fhir/Reference},
           :fhir/flags #{:SU},
           :zen.fhir/reference {:refers #{hl7-fhir-r4-core.Organization/schema}},
           :zen/desc "Organization that is the custodian of the patient record"},
          :name
          {:type zen/vector
           :every
           {:confirms #{hl7-fhir-r4-core.HumanName/schema},
            :fhir/flags #{:SU}
            :zen/desc "A name associated with the patient"}},
          :_gender {:confirms #{hl7-fhir-r4-core.Element/schema}},
          :birthDate
          {:confirms #{hl7-fhir-r4-core.date/schema}, :fhir/flags #{:SU}, :zen/desc "The date of birth for the individual"},
          :_birthDate {:confirms #{hl7-fhir-r4-core.Element/schema}},
          :multipleBirth
          {:fhir/polymorphic true,
           :type zen/map,
           :exclusive-keys #{#{:integer :boolean}},
           :keys
           {:boolean {:confirms #{hl7-fhir-r4-core.boolean/schema}},
            :_boolean {:confirms #{hl7-fhir-r4-core.Element/schema}},
            :integer {:confirms #{hl7-fhir-r4-core.integer/schema}},
            :_integer {:confirms #{hl7-fhir-r4-core.Element/schema}}},
           :zen/desc "Whether patient is part of a multiple birth"},
          :deceased
          {:fhir/polymorphic true,
           :type zen/map,
           :keys
           {:boolean {:confirms #{hl7-fhir-r4-core.boolean/schema}},
            :_boolean {:confirms #{hl7-fhir-r4-core.Element/schema}},
            :dateTime {:confirms #{hl7-fhir-r4-core.dateTime/schema}},
            :_dateTime {:confirms #{hl7-fhir-r4-core.Element/schema}}},
           :fhir/flags #{:SU :?!},
           :zen/desc "Indicates if the individual is deceased or not"},
          :photo
          {:type zen/vector, :every {:confirms #{hl7-fhir-r4-core.Attachment/schema}, :zen/desc "Image of the patient"}},
          :link
          {:type zen/vector,
           :every
           {:confirms #{hl7-fhir-r4-core.BackboneElement/schema},
            :type zen/map,
            :keys
            {:other
             {:confirms #{hl7-fhir-r4-core.Reference/schema zen.fhir/Reference},
              :fhir/flags #{:SU},
              :zen.fhir/reference {:refers #{hl7-fhir-r4-core.Patient/schema hl7-fhir-r4-core.RelatedPerson/schema}},
              :zen/desc "The other patient or related person resource that the link refers to"},
             :type
             {:confirms #{hl7-fhir-r4-core.code/schema},
              :fhir/flags #{:SU},
              :zen.fhir/value-set {:symbol hl7-fhir-r4-core.value-set.link-type/value-set, :strength :required},
              :zen/desc "replaced-by | replaces | refer | seealso"},
             :_type {:confirms #{hl7-fhir-r4-core.Element/schema}}},
            :require #{:other :type},
            :fhir/flags #{:SU :?!},
            :zen/desc "Link to another patient resource that concerns the same actual person"}}
          :generalPractitioner
          {:type zen/vector,
           :every
           {:confirms #{hl7-fhir-r4-core.Reference/schema zen.fhir/Reference},
            :zen.fhir/reference
            {:refers
             #{hl7-fhir-r4-core.PractitionerRole/schema
               hl7-fhir-r4-core.Organization/schema
               hl7-fhir-r4-core.Practitioner/schema}}
            :zen/desc "Patient's nominated primary care provider"}}}
         :zen.fhir/type "Patient"}})

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
                        {::ts []}
                        (zen.core/get-symbol ztx 'zen/schema)
                        (zen.core/get-symbol ztx 'my-sturcts/User)
                        {:interpreters [::ts]}))

    (str/join "" (::ts r))

    (t/is (= ts-typedef-assert (str/join "" (::ts r))))))

(defn remove-prac [st]
  (str/replace
   st #"generalPractitioner:Array<Reference<'PractitionerRole' \| 'Organization' \| 'Practitioner'>>;" ""))

(comment
  ;; CLASSPATH
  ;; :paths (path to zrc/)
  ;; :package-paths (path to a project. project = dir with zrc/ and zen-package.edn)

  (zen.package/zen-init-deps! "/Users/pavel/Desktop/zen/test/test_project")

  (def ztx
    (zen.core/new-context
     {:package-paths ["/Users/pavel/Desktop/zen/test/test_project"]}))
  (zen.core/read-ns ztx 'hl7-fhir-r4-core)
  (zen.core/get-symbol ztx 'hl7-fhir-r4-core/ig)
  (zen.core/get-symbol ztx 'hl7-fhir-r4-core/base-schemas)
  (zen.core/read-ns ztx 'hl7-fhir-r4-core.code)
  (zen.core/get-symbol ztx 'hl7-fhir-r4-core.code/schema)


  (zen.core/get-symbol ztx 'hl7-fhir-r4-core.CodeableConcept/schema)


  (def r
    (sut/apply-schema ztx
                      {::ts []}
                      (zen.core/get-symbol ztx 'zen/schema)
                      (zen.core/get-symbol ztx 'hl7-fhir-r4-core.Patient/schema)
                      {:interpreters [::ts]}))


  (str/join "" (::ts r))

  (def resources ["Period"
                  "Address"
                  "Patient"
                  "HumanName"
                  "Identifier"
                  "Organization"
                  "CodeableConcept"
                  "PractitionerRole"
                  "Practitioner"
                  "Coding"
                  "RelatedPerson"
                  "ContactPoint"
                  "DomainResource"
                  "Resource"
                  "Attachment"
                  "Endpoint"
                  "Location"
                  "Narrative"
                  "Meta"
                  "Extension"
                  "HealthcareService"])


  (map (fn [k] (zen.core/read-ns ztx (symbol (str "hl7-fhir-r4-core." k)))
         (zen.core/get-symbol ztx (symbol (str "hl7-fhir-r4-core." k "/schema")))
         (def r (sut/apply-schema ztx
                                  {::ts []}
                                  (zen.core/get-symbol ztx 'zen/schema)
                                  (zen.core/get-symbol ztx (symbol (str "hl7-fhir-r4-core." k "/schema")))
                                  {:interpreters [::ts]}))
         (spit "./result.ts" (str/join "" (conj (::ts r) ";\n")) :append true)) resources))

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
                        {::ts []}
                        (zen.core/get-symbol ztx 'zen/schema)
                        (zen.core/get-symbol ztx 'my-sturcts/User)
                        {:interpreters [::ts]}))

    (t/is (= ts-typedef-assert (str/join "" (::ts r))))))



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

    (t/is (= ts-typedef-assert (str/join "" (::ts r))))))


    (matcho/match (::with-defaults r)
                  {:id "foo"
                   :email "bar@baz"
                   :active true
                   :name   [{:family "None"
                             :given  ["foo"]}]})))


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
