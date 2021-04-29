(ns zen.valueset-test
  (:require [matcho.core :as matcho]
            [zen.validation]
            [clojure.test :refer [deftest is testing]]
            [zen.core]))


(deftest custom-engine-with-deffereds
  (def zen-schema
    '{ns myapp

      valueset-definition
      {:zen/tags #{zen/schema zen/tag}
       :type zen/map
       :require #{:engine}
       :keys {:engine {:type zen/keyword}}}

      enum-valueset-definition
      {:zen/tags #{zen/schema zen/tag}
       :type zen/map
       :require #{:enum}
       :keys {:engine {:const {:value :enum}}
              :enum {:type zen/set}}}


      valueset
      {:zen/tags #{zen/schema-fx}
       :type zen/symbol :tags #{valueset-definition}}

      foo-bar-baz-valueset {:zen/tags #{valueset-definition enum-valueset-definition}
                            :engine :enum
                            :enum #{"foo" "bar" "baz"}}

      sch {:zen/tags #{zen/schema}
           :type     zen/map
           :keys     {:coding {:type     zen/map
                               valueset  foo-bar-baz-valueset
                               :keys     {:code    {:type zen/string}
                                          :system  {:type zen/string}
                                          :display {:type zen/string}}}}}})

  (def tctx (zen.core/new-context))

  (def _load-ns-res (zen.core/load-ns tctx zen-schema))

  (matcho/match @tctx {:errors nil?})

  (def data {:coding {:code "foo" :display "Foo"}})

  (def validation-result
    (zen.core/validate
     tctx
     #{'myapp/sch}
     data))

  (matcho/match
    validation-result
    {:errors    empty?
     :effects [
               {:name 'myapp/valueset
                :params 'myapp/foo-bar-baz-valueset
                :data {:code "foo" :display "Foo"}
                :path [:coding 'myapp/valueset]}
               nil?
               ]
     })

  (defmethod zen.core/fx-evaluator 'myapp/valueset [ctx {valueset-name :params {code :code} :data} _data]
    (let [{:keys [engine enum]} (zen.core/get-symbol ctx valueset-name)]
      (if (= engine :enum)
        (when-not (contains? enum code)
          {:errors [{:message (format "Expected '%s' to be in valueset %s" code valueset-name)}]})

        {:errors [{:message (format "Engine '%s' is not supported" engine)}]})))

  (def resolved (zen.core/apply-fx tctx validation-result data))

  (matcho/match resolved {:errors empty? :effects empty?})

  (matcho/match (zen.core/validate! tctx #{'myapp/sch} {:coding {:code "Boo" :display "Boo"}}) {:errors seq})

  )


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
