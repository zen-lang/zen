(ns zen.valueset-test
  (:require [matcho.core :as matcho]
            [zen.validation]
            [clojure.test :refer [deftest is testing]]
            [zen.core]))


(deftest valuset-validation
  (def data
    '{ns data
      v1 {:zen/tags #{zen/valueset zen/enum-valueset}
          :engine   :enum
          :enum     #{"foo" "bar" "baz"}}
      i1 {:zen/tags #{zen/valueset}
          :engine   :enum
          :enum     #{"foo" "bar" "baz"}}
      i2 {:zen/tags #{zen/valueset zen/enum-valueset}
          :engine   :enumz
          :enum     #{"foo" "bar" "baz"}}
      i3 {:zen/tags #{zen/valueset zen/enum-valueset}
          :engine   :enum}
      i4 {:zen/tags #{zen/valueset}}})

  (def tctx (zen.core/new-context {:unsafe true }))

  (def _load-ns-res (zen.core/load-ns tctx data))

  (matcho/match (sort-by :resource (:errors @tctx))
                '[{:path [:enum], :resource data/i1}
                  {:path [:engine], :resource data/i2}
                  {:path [:enum], :resource data/i3}
                  {:path [:engine], :resource data/i4}]))


(deftest valueset-validation
  (def zen-schema
    '{ns  myapp
      vs1 {:tags   #{zen/valueset zen/enum-valueset}
           :engine :enum
           :enum   #{"foo" "bar" "baz"}}

      vs2 {:tags   #{zen/valueset zen/enum-valueset}
           :engine :enum
           :enum   #{{:code "foo" :display "Foo"}
                     {:code "bar" :display "Bar"}
                     {:code "baz" :display "Baz"}}}

      Coding {:zen/tags #{zen/schema}
              :type     zen/map
              :keys     {:code    {:type zen/string}
                         :display {:type zen/string}}}

      sch {:zen/tags #{zen/schema}
           :type     zen/map
           :keys     {:code   {:type     zen/string
                               :valueset vs1}
                      :coding {:type     zen/map
                               :confirms #{Coding}
                               :valueset vs2}

                      :codeableConcept
                      {:type zen/map
                       :keys {:text   {:type zen/string}
                              :coding {:type  zen/vector
                                       :slicing
                                       {:slices
                                        {"bind-vs"
                                         {:filter {:engine :zen
                                                   :zen    {:type     zen/map
                                                            :confirms #{Coding}
                                                            :valueset vs2}}
                                          :schema {:type zen/vector :minItems 1 :maxItems 1}}}}
                                       :every {:type     zen/map
                                               :confirms #{Coding}}}}}}}})


  (def tctx (zen.core/new-context {:unsafe true}))

  (def _load-ns-res (zen.core/load-ns tctx zen-schema))

  (matcho/match @tctx {:errors nil?})

  (testing "Valid"
    (matcho/match
      (zen.core/validate
        tctx
        #{'myapp/sch}
        {:code            "foo"
         :coding          {:code "foo" :display "Foo"}
         :codeableConcept {:text "Foo" :coding [{:code "foo" :display "Foo"}]}})
      {:errors empty?}))

  (testing "Invalid"
    (matcho/match
      (zen.core/validate
        tctx
        #{'myapp/sch}
        {:code            "wrong"
         :coding          {:code "wrong" :display "Wrong"}
         :codeableConcept {:text "Wrong" :coding [{:code "wrong" :display "Wrong"}]}})
      {:errors seq})

    (matcho/match
      (zen.core/validate
        tctx
        #{'myapp/sch}
        {:codeableConcept {:text "Wrong" :coding [{:code "foo" :display "Foo"}
                                                  {:code "bar" :display "Bar"}]}})
      {:errors seq})))


(deftest custom-engine-with-deffereds
  (def zen-schema
    '{ns myapp

      custom-valueset
      {:tags #{zen/schema zen/tag}
       :type zen/map
       :keys {:engine {:const {:value ::custom}}}}

      vs1 {:tags   #{zen/valueset custom-valueset}
           :engine ::custom}

      sch {:zen/tags #{zen/schema}
           :type     zen/map
           :keys     {:coding {:type     zen/map
                               :valueset vs1
                               :keys     {:code    {:type zen/string}
                                          :system  {:type zen/string}
                                          :display {:type zen/string}}}}}})

  (defmethod zen.validation/valueset-engine-apply ::custom [_ctx valueset data]
    {:deffereds [{:kind     :valueset
                  :valueset valueset
                  :data     data}]})

  (def tctx (zen.core/new-context))

  (def _load-ns-res (zen.core/load-ns tctx zen-schema))

  (matcho/match @tctx {:errors nil?})

  (matcho/match
    (zen.core/validate
      tctx
      #{'myapp/sch}
      {:coding {:code "foo" :display "Foo"}})
    {:errors    empty?
     :deffereds [{:kind     :valueset
                  :valueset {:zen/name 'myapp/vs1
                             :engine   ::custom}
                  :data     {:code "foo" :display "Foo"}
                  :path     [:coding]}]}))


(comment
  ;; Example of possibe fhir valueset
  '{ns       CDCICD910CMDiagnosisCodes
    icd10-cm {:tags   #{zen/valueset aidbox/valueset}
              :engine :aidbox/database
              :table  "concept"
              :select {:code    {:engine :sql
                                 :sql    "resource#>>'{code}'"}
                       :display {:engine :sql
                                 :sql    "resource#>>'{display}'"}
                       :system  {:const {:value "http://hl7.org/fhir/sid/icd-10-cm"}}}
              :where  {:engine :sql
                       :sql    "resource#>>'{system}' = 'http://hl7.org/fhir/sid/icd-10-cm'"}}

    icd9 {:tags   #{zen/valueset aidbox/valueset}
          :engine :aidbox/database
          :table  "concept"
          :select {:code    {:engine :sql
                             :sql    "resource#>>'{code}'"}
                   :display {:engine :sql
                             :sql    "resource#>>'{display}'"}
                   :system  {:const {:value "http://terminology.hl7.org/CodeSystem/icd9"}}}
          :where  {:engine :sql
                   :sql    "resource#>>'{system}' = 'http://terminology.hl7.org/CodeSystem/icd9'"}}

    vs {:tags     #{zen/valueset fhir/valueset}
        :engine   :fhir
        :resource {:compose {:include [{:valueset icd10-cm}
                                       {:valueset icd9}]}}}})
