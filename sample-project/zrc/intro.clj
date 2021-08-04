(ns zen.intro
  (:require [zen.core :as zen]
            [zen.effect]))


(do 
  (defmethod zen.effect/fx-evaluator 'fhir/bind
    [ztx {{vsn :valueset} :params  data :data :as err} resource]
    (let [vs (zen/get-symbol ztx vsn)
          code (or (:code data))]
      (when (empty? (->> (:concepts vs)
                         (filterv (fn [c] (= (:code c) code)))
                         (take 1)))
        [(-> (dissoc err :params)
             (assoc :message (str "Should match valueset " (:zen/name vs))))])))
  (def ztx (zen/new-context))
  (defn v [sym x]
    (let [schs (if (set? sym) sym #{sym})]
      (swap! ztx assoc :errors {})
      (zen/read-ns ztx (symbol(namespace sym)))
      (let [res (zen/validate ztx schs x)]
        (->
         (zen.effect/apply-fx ztx res x)
         (select-keys [:errors]);; :effects
         (assoc :schema-errors (->> (let [err (:errors @ztx)] (if (sequential? err) err [err]))
                                    (remove (fn [x]
                                              (= 'fhir/bind (:resource x)))))))))))

(comment "EDN Intro")


;; {:resourceType "Patient",
;;  :id "pt1"
;;  :name [{:family "Chalmers"}
;;         {:given ["Johny"]}]}
;; Set: #{1 2 3}
;; Symbol: some-symbol


(comment "Let's write schema for Patient from scratch")
;; about namespaces

(def p {:name [{:given ["Nikolai"] :family "Ryzhikov"}]
        :birthDate "1980-03-5"})

(v 'intro/Patient p)

;; extract human-name to re-usable schema

(v 'intro/MyPatient {:resourceType "Patient"
                     :id "pt1"
                     :gender "female"
                     :name [{:given ["Nikolai"]}]
                     :birthDate "1980-03-05"})

;; :confirms & Resource & DomainResource
;; mixins with :confirms


(def pt
  {:resourceType "Patient"
   :id "example"
   :active true
   :name [{:use "official" :family "Chalmers" :given ["Peter" "James"]}
          {:use "usual" :given ["Jim"]}
          {:use "maiden" :family "Windsor" :given ["Peter" "James"]}]
   :address [{:use "home"
              :city "PleasantVille"
              :type "both"
              :state "Vic"
              :line ["534 Erewhon St"]
              :postalCode "3999"}]
   :identifier [{:use "usual"
                 :type {:coding [{:system "http://terminology.hl7.org/CodeSystem/v2-0203" :code "MR"}]}
                 :system "urn:oid:1.2.36.146.595.217.0.1"
                 :value "12345"
                 :period {:start "2001-05-06"}
                 :assigner {:display "Acme Healthcare"}}]
   :managingOrganization {:reference "Organization/1"}
   :gender "male"
   :birthDate "1974-12-25"
   :telecom [{:system "phone" :value "(03) 5555 6473" :use "work" :rank 1}
             {:system "phone" :value "(03) 3410 5613" :use "mobile" :rank 2}]})

(v 'intro/Patient pt)

(v 'intro/MyPatient pt)


(v 'fhir/Patient pt)

;; us-core Profiles & Extension

;; * Constraints
;; * Extension
;; * Enum
;; * valueset

;; (do (zen/read-ns ztx 'fhir) :ok)
(def upt {:resourceType "Patient"
          :id "example"
          :active true
          :identifier [{:system "mrn" :value "123"}]
          :gender "female"
          :name [{:given ["Johny"]}]
          ;; #extension
          :race {:texto "some-race"
                 :ombCategory {:code "1002-54"
                               :system "urn:oid:2.16.840.1.113883.6.238"}}
          })


(v 'fhir.us-core/Patient upt)

;; # match vs slicing

(def bmi {:resourceType "Observation"
          :code {:coding [{:code "59576-9xxx"
                           :system "http://loinc.org"}]}
          :valueQuantity {:system "http://unitsofmeasure.org"
                          :unit "kg/m2"
                          :value 20}})

(v 'fhir/Observation bmi)

(v 'fhir.us-core/bmi bmi)


;; # filters vs slicing
(def bp {:resourceType "Observation"
         :code {:coding [{:code "85354-9"
                          :system "http://loinc.org"}]}
         :component [{:ext "ups"
                      :code {:coding [{:code "8480-6"
                                       :system "http://loinc.org"}]}
                      :valueQuantity {:system "http://unitsofmeasure.org"
                                      :unit "mm[Hg]"
                                      :value -1}}
                     {:code {:coding [{:code "8462-4"
                                       :system "http://loinc.org"}]}
                      :valueQuantity {:system "http://unitsofmeasure.org"
                                      :unit "mm[Hg]"
                                      :value 20}}]})

(v 'fhir.us-core/blood-pressure bp)

