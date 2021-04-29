(ns zen.valueset-test
  (:require [matcho.core :as matcho]
            [zen.validation]
            [clojure.test :refer [deftest is testing]]
            [zen.core]))


(def zen-fx
  '{ns fx

    valueset-definition
    {:zen/tags #{zen/schema zen/tag}
     :type     zen/map
     :require  #{:engine}
     :keys     {:engine {:type zen/keyword}}
     :zen/desc "Registry (vocabulary) with free form data to make symbolic refs for validation.
                :engine is used to define a extrnal tool for valueset protocol evaluation"}

    valueset
    {:zen/tags #{zen/schema-fx}
     :type     zen/symbol :tags #{valueset-definition}
     :zen/desc "Check that value is in valueset.
                Validation can create errors and deferred checks
                Doesn't perform any side effects, they should be executed on the application level"}})

(defn valueset-engine-apply-dispatch [_ctx {engine :engine} _code] engine)

(defmulti valueset-engine-apply #'valueset-engine-apply-dispatch)

(defmethod valueset-engine-apply :default [_ctx {:keys [engine]} _code]
  {:errors [{:message (str "Unknown valueset engine: " engine)}]})

(defmethod zen.core/fx-evaluator 'fx/valueset [ctx {valueset-sym :params, value :data} _data]
  (valueset-engine-apply ctx (zen.core/get-symbol ctx valueset-sym) value))


(deftest custom-engine-with-deffereds
  (def zen-schema
    '{ns     myapp
      import #{fx}

      fhir-code-enum-valueset-definition
      {:zen/tags #{zen/schema zen/tag}
       :type     zen/map
       :require  #{:enum}
       :keys     {:engine {:const {:value :fhir-code-enum}}
                  :enum   {:type zen/set}}}

      foo-bar-baz-valueset {:zen/tags #{fx/valueset-definition fhir-code-enum-valueset-definition}
                            :engine   :fhir-code-enum
                            :enum     #{"foo" "bar" "baz"}}

      sch {:zen/tags #{zen/schema}
           :type     zen/map
           :keys     {:code   {:type       zen/string
                               fx/valueset foo-bar-baz-valueset}
                      :coding {:type       zen/map
                               fx/valueset foo-bar-baz-valueset
                               :keys       {:code    {:type zen/string}
                                            :system  {:type zen/string}
                                            :display {:type zen/string}}}}}})

  (defmethod valueset-engine-apply :fhir-code-enum [_ctx valueset data]
    (let [code (cond-> data (map? data) :code)]
      (when-not (contains? (:enum valueset) code)
        {:errors [{:message (format "Expected '%s' to be in valueset %s" code (:zen/name valueset))}]})))

  (def tctx (zen.core/new-context))

  (do (zen.core/load-ns tctx zen-fx)
      (zen.core/load-ns tctx zen-schema)
      nil)

  (matcho/match @tctx {:errors nil?})

  (def data {:coding {:code "foo" :display "Foo"}})

  (def validation-result (zen.core/validate tctx #{'myapp/sch} data))

  (matcho/match validation-result
                {:errors  empty?
                 :effects [{:name   'fx/valueset
                            :params 'myapp/foo-bar-baz-valueset
                            :data   {:code "foo" :display "Foo"}
                            :path   [:coding 'fx/valueset]}
                           nil?]})

  (def resolved (zen.core/apply-fx tctx validation-result data))

  (matcho/match resolved {:errors empty? :effects empty?})

  (matcho/match (zen.core/validate! tctx #{'myapp/sch} {:coding {:code "Boo" :display "Boo"}})
                {:errors [{:path [:coding 'fx/valueset nil?]}
                          nil?]}))


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
