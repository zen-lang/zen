(ns zen.valueset-test
  (:require [matcho.core :as matcho]
            [clojure.test :refer [deftest is testing]]
            [zen.core]))


(def tctx (zen.core/new-context {:unsafe true}))


(zen.core/load-ns!
 tctx '{ns myapp
        vs1 {:tags #{zen/valueset}
             :engine :enum
             :enum #{"foo" "bar" "baz"}}

        vs2 {:tags #{zen/valueset}
             :engine :enum
             :enum #{{:code "foo" :display "Foo"}
                     {:code "bar" :display "Bar"}
                     {:code "baz"  :display "Baz"}}}

        Coding {:type zen/map
                :keys {:code {:type zen/string}
                       :display {:type zen/string}}}

        sch {:type zen/map
             :keys {
                    :code {:type zen/string
                           :valueset vs}

                    :coding {:type zen/map
                             :confirms #{Coding}
                             :valueset vs}

                    :codeableConcept {:type zen/map
                                      :keys {:text {:type zen/string}
                                             :coding {:type zen/vector
                                                      :slicing {:slices {"bind-vs" {:filter {:engine :zen
                                                                                             :zen {:type zen/map
                                                                                                   :confirms #{Coding}
                                                                                                   :valueset vs}}
                                                                                    :schema {:type zen/vector :minItems 1 :maxItems 1}}}}
                                                      :every {:type zen/map
                                                              :confirms #{Coding}}}}}}}})


(comment
  {ns CDCICD910CMDiagnosisCodes
   icd10-cm {:tags #{zen/valueset aidbox/valueset}
             :engine :aidbox/database
             :table "concept"
             :code {:engine :sql
                    :sql "resource#>>'{code}'"}
             :display {:engine :sql
                       :sql "resource#>>'{display}'"}
             :system {:const {:value "http://hl7.org/fhir/sid/icd-10-cm"}}
             :where {:engine :sql
                     :sql "resource#>>'{system}' = 'http://hl7.org/fhir/sid/icd-10-cm'"}}

   icd9 {:tags #{zen/valueset aidbox/valueset}
         :engine :aidbox/database
         :table "concept"
         :code {:engine :sql
                :sql "resource#>>'{code}'"}
         :display {:engine :sql
                   :sql "resource#>>'{display}'"}
         :system {:const {:value "http://terminology.hl7.org/CodeSystem/icd9"}}
         :where {:engine :sql
                 :sql "resource#>>'{system}' = 'http://terminology.hl7.org/CodeSystem/icd9'"}}

   vs {:tags #{zen/valueset fhir/valueset}
       :engine :fhir
       :resource {:compose {:include [{:valueset icd10-cm}
                                      {:valueset icd9}]}}}})
