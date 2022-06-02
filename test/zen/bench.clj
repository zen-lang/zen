(ns zen.bench
  (:require
   ;; start cider with :test alias to get the criterium dependency
   [criterium.core :as c]
   [zen.core :as zen]
   [zen.v2-validation :as v]))

(def pt
  {:address [{:use "home" :line ["2222 Home Street"]}]
   :meta {:lastUpdated "2012-05-29T23:45:32Z"}
   :managingOrganization {:reference "Organization/hl7"}
   :name [{:use "official" :family "Everywoman" :given ["Eve"]}]
   :birthDate "1973-05-31"
   :resourceType "Patient"
   :active true
   :id "1"
   :identifier
   [{:type
     {:coding
      [{:system "http://terminology.hl7.org/CodeSystem/v2-0203"
        :code "SS"}]}
     :system "http://hl7.org/fhir/sid/us-ssn"
     :value "444222222"}]
   :telecom [{:system "phone" :value "555-555-2003" :use "work"}]
   :gender "female"
   :text
   {:status "generated"
    :div
    "<div xmlns=\"http://www.w3.org/1999/xhtml\">Everywoman Eve. SSN:\n            444222222</div>"}})

(def pt-1
  {:address
   [{:use "home"
     :city "PleasantVille"
     :type "both"
     :state "Vic"
     :line ["534 Erewhon St"]
     :postalCode "3999"
     :period {:start "1974-12-25"}
     :district "Rainbow"
     :text "534 Erewhon St PeasantVille Rainbow Vic  3999"}]
   :managingOrganization {:reference "Organization/1"}
   :deceasedBoolean false
   :name
   [{:use "official" :family "Chalmers" :given ["Peter" "James"]}
    {:use "usual" :given ["Jim"]}
    {:use "maiden"
     :family "Windsor"
     :given ["Peter" "James"]
     :period {:end "2002"}}]
   :birthDate "1974-12-25"
   :_birthDate
   {:extension
    [{:url "http://hl7.org/fhir/StructureDefinition/patient-birthTime"
      :valueDateTime "1974-12-25T14:35:45-05:00"}]}
   :resourceType "Patient"
   :active true
   :id "example"
   :identifier
   [{:use "usual"
     :type
     {:coding
      [{:system "http://terminology.hl7.org/CodeSystem/v2-0203"
        :code "MR"}]}
     :system "urn:oid:1.2.36.146.595.217.0.1"
     :value "12345"
     :period {:start "2001-05-06"}
     :assigner {:display "Acme Healthcare"}}]
   :telecom
   [{:use "home"}
    {:system "phone" :value "(03) 5555 6473" :use "work" :rank 1}
    {:system "phone" :value "(03) 3410 5613" :use "mobile" :rank 2}
    {:system "phone"
     :value "(03) 5555 8834"
     :use "old"
     :period {:end "2014"}}]
   :gender "male"
   :contact
   [{:relationship
     [{:coding
       [{:system "http://terminology.hl7.org/CodeSystem/v2-0131"
         :code "N"}]}]
     :name
     {:family "du Marché"
      :_family
      {:extension
       [{:url
         "http://hl7.org/fhir/StructureDefinition/humanname-own-prefix"
         :valueString "VV"}]}
      :given ["Bénédicte"]}
     :telecom [{:system "phone" :value "+33 (237) 998327"}]
     :address
     {:use "home"
      :type "both"
      :line ["534 Erewhon St"]
      :city "PleasantVille"
      :district "Rainbow"
      :state "Vic"
      :postalCode "3999"
      :period {:start "1974-12-25"}}
     :gender "female"
     :period {:start "2012"}}]})

(defn validate-patients [& pts]
  (assert (zen.core/validate ztx #{'hl7-fhir-r4-core.Patient/schema} pt))

  (assert (v/validate ztx #{'hl7-fhir-r4-core.Patient/schema} pt))

  'ok)

(defn bench-patients [& pts]
  (def ztx (zen/new-context {:unsafe true}))

  (zen.core/read-ns ztx 'hl7-fhir-r4-core.Patient)

  (doseq [pt pts]

    (println "Patient OLD VERSION"
             (c/with-progress-reporting
               (c/bench (zen.core/validate ztx #{'hl7-fhir-r4-core.Patient/schema} pt)
                        :verbose))

             (println "Patient NEW VERSION")
             (c/with-progress-reporting
               (c/bench (v/validate ztx #{'hl7-fhir-r4-core.Patient/schema} pt)
                        :verbose)))))

(def enc
  {:resourceType "Encounter",
   :classHistory
   [{:class
     {:system "http://terminology.hl7.org/CodeSystem/v3-ActCode",
      :code "EMER",
      :display "emergency"},
     :period
     {:start "2017-02-01T07:15:00+10:00",
      :end "2017-02-01T09:27:00+10:00"}}
    {:class
     {:system "http://terminology.hl7.org/CodeSystem/v3-ActCode",
      :code "IMP",
      :display "inpatient encounter"},
     :period {:start "2017-02-01T09:27:00+10:00"}}],
   :status "in-progress",
   :id "emerg",
   :class
   {:system "http://terminology.hl7.org/CodeSystem/v3-ActCode",
    :code "IMP",
    :display "inpatient encounter"},
   :hospitalization
   {:admitSource
    {:coding
     [{:system "http://terminology.hl7.org/CodeSystem/admit-source",
       :code "emd",
       :display "From accident/emergency department"}]}},
   :period {:start "2017-02-01T07:15:00+10:00"},
   :location
   [{:location {:display "Emergency Waiting Room"},
     :status "active",
     :period
     {:start "2017-02-01T07:15:00+10:00",
      :end "2017-02-01T08:45:00+10:00"}}
    {:location {:display "Emergency"},
     :status "active",
     :period
     {:start "2017-02-01T08:45:00+10:00",
      :end "2017-02-01T09:27:00+10:00"}}
    {:location {:display "Ward 1, Room 42, Bed 1"},
     :status "active",
     :period
     {:start "2017-02-01T09:27:00+10:00",
      :end "2017-02-01T12:15:00+10:00"}}
    {:location {:display "Ward 1, Room 42, Bed 1"},
     :status "reserved",
     :period
     {:start "2017-02-01T12:15:00+10:00",
      :end "2017-02-01T12:45:00+10:00"}}
    {:location {:display "Ward 1, Room 42, Bed 1"},
     :status "active",
     :period {:start "2017-02-01T12:45:00+10:00"}}],
   :subject {:reference "Patient/example"},
   :statusHistory
   [{:status "arrived",
     :period
     {:start "2017-02-01T07:15:00+10:00",
      :end "2017-02-01T07:35:00+10:00"}}
    {:status "triaged",
     :period
     {:start "2017-02-01T07:35:00+10:00",
      :end "2017-02-01T08:45:00+10:00"}}
    {:status "in-progress",
     :period
     {:start "2017-02-01T08:45:00+10:00",
      :end "2017-02-01T12:15:00+10:00"}}
    {:status "onleave",
     :period
     {:start "2017-02-01T12:15:00+10:00",
      :end "2017-02-01T12:45:00+10:00"}}
    {:status "in-progress",
     :period {:start "2017-02-01T12:45:00+10:00"}}],
   :text
   {:status "generated",
    :div
    "<div xmlns=\"http://www.w3.org/1999/xhtml\">Emergency visit that escalated into inpatient patient @example</div>"}})

(def enc-1
{:appointment [{:reference "Appointment/example"}],
 :diagnosis
 [{:condition {:reference "Condition/stroke"},
   :use
   {:coding
    [{:system "http://terminology.hl7.org/CodeSystem/diagnosis-role",
      :code "AD",
      :display "Admission diagnosis"}]},
   :rank 1}
  {:condition {:reference "Condition/f201"},
   :use
   {:coding
    [{:system "http://terminology.hl7.org/CodeSystem/diagnosis-role",
      :code "DD",
      :display "Discharge diagnosis"}]}}],
 :serviceProvider {:reference "Organization/2"},
 :episodeOfCare [{:reference "EpisodeOfCare/example"}],
 :reasonCode
 [{:text
   "The patient seems to suffer from bilateral pneumonia and renal insufficiency, most likely due to chemotherapy."}],
 :type
 [{:coding
   [{:system "http://snomed.info/sct",
     :code "183807002",
     :display "Inpatient stay for nine days"}]}],
 :participant
 [{:type
   [{:coding
     [{:system
       "http://terminology.hl7.org/CodeSystem/v3-ParticipationType",
       :code "PART"}]}],
   :individual {:reference "Practitioner/f201"}}],
 :resourceType "Encounter",
 :account [{:reference "Account/example"}],
 :priority
 {:coding
  [{:system "http://snomed.info/sct",
    :code "394849002",
    :display "High priority"}]},
 :status "finished",
 :id "f203",
 :class
 {:system "http://terminology.hl7.org/CodeSystem/v3-ActCode",
  :code "IMP",
  :display "inpatient encounter"},
 :identifier [{:use "temp", :value "Encounter_Roel_20130311"}],
 :hospitalization
 {:origin {:reference "Location/2"},
  :admitSource
  {:coding
   [{:system "http://snomed.info/sct",
     :code "309902002",
     :display "Clinical Oncology Department"}]},
  :reAdmission {:coding [{:display "readmitted"}]},
  :dietPreference
  [{:coding
    [{:system "http://snomed.info/sct",
      :code "276026009",
      :display "Fluid balance regulation"}]}],
  :specialCourtesy
  [{:coding
    [{:system
      "http://terminology.hl7.org/CodeSystem/v3-EncounterSpecialCourtesy",
      :code "NRM",
      :display "normal courtesy"}]}],
  :specialArrangement
  [{:coding
    [{:system
      "http://terminology.hl7.org/CodeSystem/encounter-special-arrangements",
      :code "wheel",
      :display "Wheelchair"}]}],
  :destination {:reference "Location/2"}},
 :period {:start "2013-03-11", :end "2013-03-20"},
 :basedOn [{:reference "ServiceRequest/myringotomy"}],
 :partOf {:reference "Encounter/f203"},
 :subject {:reference "Patient/f201", :display "Roel"},
 :statusHistory [{:status "arrived", :period {:start "2013-03-08"}}]})

(defn bench-encounters [& encs]
  (def ztx (zen/new-context {:unsafe true}))

  (zen.core/read-ns ztx 'hl7-fhir-r4-core.Encounter)

  (doseq [enc encs]

    (println "Encounter OLD VERSION")
    (c/with-progress-reporting
      (c/bench (zen.core/validate ztx #{'hl7-fhir-r4-core.Encounter/schema} enc)
               :verbose))

    (println "Encounter NEW VERSION")
    (c/with-progress-reporting
      (c/bench (v/validate ztx #{'hl7-fhir-r4-core.Encounter/schema} enc)
               :verbose))))

(comment

  (bench-patients pt pt-1)

  (bench-encounters enc enc-1)

  (do

    (def pt-sch (zen.core/get-symbol ztx 'hl7-fhir-r4-core.Patient/schema))

    (zen.core/validate ztx #{'zen/schema} pt-sch)

    (v/validate ztx #{'hl7-fhir-r4-core.Patient/schema} pt)))
