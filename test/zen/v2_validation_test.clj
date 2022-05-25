(ns zen.v2-validation-test
  (:require
   [zen.test-runner :as r]
   [clojure.test :refer [deftest is]]
   [zen.v2-validation :as v]
   [zen.core :as zen]))

(deftest implemented-validations
  (do

    (def ztx (zen/new-context {:unsafe true}))

    (r/zen-read-ns ztx 'zen.tests.require-test)

    (r/zen-read-ns ztx 'zen.tests.boolean-test)

    (r/zen-read-ns ztx 'zen.tests.case-test)

    (r/zen-read-ns ztx 'zen.tests.schema-key-test)

    (r/zen-read-ns ztx 'zen.tests.types-test)

    (r/zen-read-ns ztx 'zen.tests.keyname-schemas-test)

    (r/zen-read-ns ztx 'zen.tests.map-test)

    (r/zen-read-ns ztx 'zen.tests.core-validate-test)

    (r/zen-read-ns ztx 'zen.tests.effects-test)

    (r/zen-read-ns ztx 'zen.tests.fn-test)

    (r/zen-read-ns ztx 'zen.tests.slicing-test)

    (r/run-tests ztx)))

(comment

  (do

    (def ztx (zen/new-context {:unsafe true}))

    (r/zen-read-ns ztx 'hl7-fhir-r4-core.Patient)

    (def pt-sch (zen.core/get-symbol ztx 'hl7-fhir-r4-core.Patient/schema))

    (def pt
      {:address [{:use "home", :line ["2222 Home Street"]}],
       :meta {:lastUpdated "2012-05-29T23:45:32Z"},
       :managingOrganization {:reference "Organization/hl7"},
       :name [{:use "official", :family "Everywoman", :given ["Eve"]}],
       :birthDate "1973-05-31",
       :resourceType "Patient",
       :active true,
       :id "1",
       :identifier
       [{:type
         {:coding
          [{:system "http://terminology.hl7.org/CodeSystem/v2-0203",
            :code "SS"}]},
         :system "http://hl7.org/fhir/sid/us-ssn",
         :value "444222222"}],
       :telecom [{:system "phone", :value "555-555-2003", :use "work"}],
       :gender "female",
       :text
       {:status "generated",
        :div
        "<div xmlns=\"http://www.w3.org/1999/xhtml\">Everywoman, Eve. SSN:\n            444222222</div>"}})

    (zen.core/validate ztx #{'zen/schema} pt-sch)

    (zen.core/validate ztx #{'hl7-fhir-r4-core.Patient/schema} pt)

    (v/validate ztx #{'zen/schema} (zen.core/get-symbol ztx 'hl7-fhir-r4-core.Patient/schema))

    (v/validate ztx #{'hl7-fhir-r4-core.Patient/schema} pt)))

