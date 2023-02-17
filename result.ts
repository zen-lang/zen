export type Reference<T extends ResourceType> = {
  id: string;
  resourceType: T;
  display?: string;
};
export interface ResourceTypeMap {
  ImmunizationEvaluation: ImmunizationEvaluation;
  Appointment: Appointment;
  StructureMap: StructureMap;
  CareTeam: CareTeam;
  Linkage: Linkage;
  Communication: Communication;
  MedicationDispense: MedicationDispense;
  ImagingStudy: ImagingStudy;
  ChargeItem: ChargeItem;
  AdverseEvent: AdverseEvent;
  Media: Media;
  SubstancePolymer: SubstancePolymer;
  QuestionnaireResponse: QuestionnaireResponse;
  Coverage: Coverage;
  Procedure: Procedure;
  AuditEvent: AuditEvent;
  PaymentReconciliation: PaymentReconciliation;
  MedicinalProductManufactured: MedicinalProductManufactured;
  CompartmentDefinition: CompartmentDefinition;
  Organization: Organization;
  ExplanationOfBenefit: ExplanationOfBenefit;
  Composition: Composition;
  CoverageEligibilityResponse: CoverageEligibilityResponse;
  DocumentReference: DocumentReference;
  EventDefinition: EventDefinition;
  SubstanceProtein: SubstanceProtein;
  TerminologyCapabilities: TerminologyCapabilities;
  Encounter: Encounter;
  ImplementationGuide: ImplementationGuide;
  EvidenceVariable: EvidenceVariable;
  ObservationDefinition: ObservationDefinition;
  DiagnosticReport: DiagnosticReport;
  ExampleScenario: ExampleScenario;
  ResearchDefinition: ResearchDefinition;
  Parameters: Parameters;
  SearchParameter: SearchParameter;
  MedicinalProductInteraction: MedicinalProductInteraction;
  CodeSystem: CodeSystem;
  MessageDefinition: MessageDefinition;
  NutritionOrder: NutritionOrder;
  VerificationResult: VerificationResult;
  MedicationAdministration: MedicationAdministration;
  CatalogEntry: CatalogEntry;
  Flag: Flag;
  DeviceUseStatement: DeviceUseStatement;
  Contract: Contract;
  Invoice: Invoice;
  PaymentNotice: PaymentNotice;
  Location: Location;
  Claim: Claim;
  Specimen: Specimen;
  MedicationStatement: MedicationStatement;
  EnrollmentResponse: EnrollmentResponse;
  Evidence: Evidence;
  Bundle: Bundle;
  ResearchElementDefinition: ResearchElementDefinition;
  BodyStructure: BodyStructure;
  MedicinalProduct: MedicinalProduct;
  ResearchStudy: ResearchStudy;
  AppointmentResponse: AppointmentResponse;
  MedicinalProductIndication: MedicinalProductIndication;
  Measure: Measure;
  Person: Person;
  InsurancePlan: InsurancePlan;
  Patient: Patient;
  EffectEvidenceSynthesis: EffectEvidenceSynthesis;
  ResearchSubject: ResearchSubject;
  Medication: Medication;
  ConceptMap: ConceptMap;
  CoverageEligibilityRequest: CoverageEligibilityRequest;
  SubstanceSourceMaterial: SubstanceSourceMaterial;
  VisionPrescription: VisionPrescription;
  MolecularSequence: MolecularSequence;
  MedicinalProductUndesirableEffect: MedicinalProductUndesirableEffect;
  OperationOutcome: OperationOutcome;
  MessageHeader: MessageHeader;
  AllergyIntolerance: AllergyIntolerance;
  SubstanceReferenceInformation: SubstanceReferenceInformation;
  SupplyDelivery: SupplyDelivery;
  EpisodeOfCare: EpisodeOfCare;
  PractitionerRole: PractitionerRole;
  Library: Library;
  Practitioner: Practitioner;
  MedicationRequest: MedicationRequest;
  ImmunizationRecommendation: ImmunizationRecommendation;
  Immunization: Immunization;
  GraphDefinition: GraphDefinition;
  Account: Account;
  MedicinalProductIngredient: MedicinalProductIngredient;
  MeasureReport: MeasureReport;
  DeviceMetric: DeviceMetric;
  Goal: Goal;
  MedicationKnowledge: MedicationKnowledge;
  ClaimResponse: ClaimResponse;
  DeviceDefinition: DeviceDefinition;
  Slot: Slot;
  ValueSet: ValueSet;
  MedicinalProductAuthorization: MedicinalProductAuthorization;
  StructureDefinition: StructureDefinition;
  MedicinalProductContraindication: MedicinalProductContraindication;
  DeviceRequest: DeviceRequest;
  List: List;
  Questionnaire: Questionnaire;
  DomainResource: DomainResource;
  Endpoint: Endpoint;
  NamingSystem: NamingSystem;
  MedicinalProductPackaged: MedicinalProductPackaged;
  Basic: Basic;
  Binary: Binary;
  PlanDefinition: PlanDefinition;
  Subscription: Subscription;
  RelatedPerson: RelatedPerson;
  SubstanceSpecification: SubstanceSpecification;
  SubstanceNucleicAcid: SubstanceNucleicAcid;
  GuidanceResponse: GuidanceResponse;
  ClinicalImpression: ClinicalImpression;
  OrganizationAffiliation: OrganizationAffiliation;
  Condition: Condition;
  CapabilityStatement: CapabilityStatement;
  HealthcareService: HealthcareService;
  SpecimenDefinition: SpecimenDefinition;
  RiskAssessment: RiskAssessment;
  OperationDefinition: OperationDefinition;
  ActivityDefinition: ActivityDefinition;
  Schedule: Schedule;
  BiologicallyDerivedProduct: BiologicallyDerivedProduct;
  Group: Group;
  MedicinalProductPharmaceutical: MedicinalProductPharmaceutical;
  FamilyMemberHistory: FamilyMemberHistory;
  ServiceRequest: ServiceRequest;
  DetectedIssue: DetectedIssue;
  Device: Device;
  RequestGroup: RequestGroup;
  TestScript: TestScript;
  RiskEvidenceSynthesis: RiskEvidenceSynthesis;
  SupplyRequest: SupplyRequest;
  Task: Task;
  CommunicationRequest: CommunicationRequest;
  EnrollmentRequest: EnrollmentRequest;
  ChargeItemDefinition: ChargeItemDefinition;
  Substance: Substance;
  Provenance: Provenance;
  Consent: Consent;
  CarePlan: CarePlan;
  TestReport: TestReport;
  Observation: Observation;
  DocumentManifest: DocumentManifest;
}

export type ResourceType = keyof ResourceTypeMap;
/* Describes a comparison of an immunization event against published recommendations to determine if the administration is "valid" in relation to those  recommendations. */
interface ImmunizationEvaluation {
  /* Who this evaluation is for */
  patient?: Reference<"Patient"> /* Evaluation notes */;
  description?: string /* Date evaluation was performed */;
  date?: dateTime /* Name of vaccine series */;
  series?: string /* Dose number within series */;
  doseNumber?: string | positiveInt;
  _date?: Element /* Who is responsible for publishing the recommendations */;
  authority?: Reference<"Organization">;
  _status?: Element;
  _description?: Element /* Recommended number of doses for immunity */;
  seriesDoses?: string | positiveInt;
  doseStatusReason?: Array<CodeableConcept> /* Immunization being evaluated */;
  immunizationEvent?: Reference<"Immunization"> /* completed | entered-in-error */;
  status?: code;
  identifier?: Array<Identifier> /* Evaluation target disease */;
  targetDisease?: CodeableConcept /* Status of the dose relative to published recommendations */;
  doseStatus?: CodeableConcept;
  _series?: Element;
}
/* A booking of a healthcare event among patient(s), practitioner(s), related person(s) and/or device(s) for a specific date/time. This may result in one or more Encounter(s). */
interface Appointment {
  _created?: Element /* Shown on a subject line in a meeting request, or appointment list */;
  description?: string;
  serviceCategory?: Array<CodeableConcept>;
  slot?: Array<Reference<"Slot">>;
  _patientInstruction?: Element;
  specialty?: Array<CodeableConcept> /* The coded reason for the appointment being cancelled */;
  cancelationReason?: CodeableConcept;
  requestedPeriod?: Array<Period> /* Detailed information and instructions for the patient */;
  patientInstruction?: string;
  _end?: Element;
  _priority?: Element;
  _status?: Element /* When appointment is to take place */;
  start?: instant;
  reasonCode?: Array<CodeableConcept> /* The date that this appointment was initially created */;
  created?: dateTime;
  participant?: Array<{
    type?: Array<CodeableConcept> /* Person, Location/HealthcareService or Device */;
    actor?: Reference<
      | "Patient"
      | "PractitionerRole"
      | "HealthcareService"
      | "Device"
      | "Location"
      | "Practitioner"
      | "RelatedPerson"
    > /* required | optional | information-only */;
    required?: code;
    _required?: Element /* accepted | declined | tentative | needs-action */;
    status?: code;
    _status?: Element /* Participation period of the actor */;
    period?: Period;
  }>;
  serviceType?: Array<CodeableConcept>;
  _description?: Element;
  supportingInformation?: Array<
    Reference<ResourceType>
  > /* Used to make informed decisions if needing to re-prioritize */;
  priority?: unsignedInt /* The style of appointment or patient that has been booked in the slot (not service type) */;
  appointmentType?: CodeableConcept /* proposed | pending | booked | arrived | fulfilled | cancelled | noshow | entered-in-error | checked-in | waitlist */;
  status?: code /* Additional comments */;
  comment?: string /* Can be less than start/end (e.g. estimate) */;
  minutesDuration?: positiveInt;
  identifier?: Array<Identifier>;
  _minutesDuration?: Element;
  basedOn?: Array<
    Reference<"ServiceRequest">
  > /* When appointment is to conclude */;
  end?: instant;
  _start?: Element;
  _comment?: Element;
  reasonReference?: Array<
    Reference<
      "Observation" | "Procedure" | "ImmunizationRecommendation" | "Condition"
    >
  >;
}
/* A Map of relationships between 2 structures that can be used to transform data. */
interface StructureMap {
  /* Natural language description of the structure map */
  description?: markdown /* Date last changed */;
  date?: dateTime;
  _import?: Array<Element>;
  group?: Array<{
    _documentation?: Element /* Another group that this group adds rules to */;
    extends?: id;
    _extends?: Element /* none | types | type-and-types */;
    typeMode?: code /* Human-readable label */;
    name?: id;
    rule?: Array<StructureMap>;
    _typeMode?: Element /* Additional description/explanation for group */;
    documentation?: string;
    _name?: Element;
    input?: Array<{
      /* Name for this instance of data */ name?: id;
      _name?: Element /* Type for this instance of data */;
      type?: string;
      _type?: Element /* source | target */;
      mode?: code;
      _mode?: Element /* Documentation for this instance of data */;
      documentation?: string;
      _documentation?: Element;
    }>;
  }> /* Name of the publisher (organization or individual) */;
  publisher?: string;
  jurisdiction?: Array<CodeableConcept>;
  _publisher?: Element /* Why this structure map is defined */;
  purpose?: markdown;
  _date?: Element /* Name for this structure map (computer friendly) */;
  name?: string;
  _status?: Element;
  _experimental?: Element;
  useContext?: Array<UsageContext> /* Use and/or publishing restrictions */;
  copyright?: markdown /* For testing purposes, not real usage */;
  experimental?: boolean /* Name for this structure map (human friendly) */;
  title?: string;
  _description?: Element;
  _purpose?: Element;
  structure?: Array<{
    /* Canonical reference to structure definition */ url?: canonical;
    _url?: Element /* source | queried | target | produced */;
    mode?: code;
    _mode?: Element /* Name for type in this map */;
    alias?: string;
    _alias?: Element /* Documentation on use of structure */;
    documentation?: string;
    _documentation?: Element;
  }> /* draft | active | retired | unknown */;
  status?: code;
  _name?: Element /* Canonical identifier for this structure map, represented as a URI (globally unique) */;
  url?: uri;
  identifier?: Array<Identifier>;
  _copyright?: Element;
  _title?: Element /* Business version of the structure map */;
  version?: string;
  _version?: Element;
  import?: Array<canonical>;
  contact?: Array<ContactDetail>;
  _url?: Element;
}
/* The Care Team includes all the people and organizations who plan to participate in the coordination and delivery of care for a patient. */
interface CareTeam {
  category?: Array<CodeableConcept>;
  managingOrganization?: Array<
    Reference<"Organization">
  > /* Encounter created as part of */;
  encounter?: Reference<"Encounter"> /* Name of the team, such as crisis assessment team */;
  name?: string;
  _status?: Element;
  reasonCode?: Array<CodeableConcept>;
  participant?: Array<{
    role?: Array<CodeableConcept> /* Who is involved */;
    member?: Reference<
      | "CareTeam"
      | "Patient"
      | "PractitionerRole"
      | "Organization"
      | "Practitioner"
      | "RelatedPerson"
    > /* Organization of the practitioner */;
    onBehalfOf?: Reference<"Organization"> /* Time period of participant */;
    period?: Period;
  }>;
  note?: Array<Annotation> /* proposed | active | suspended | inactive | entered-in-error */;
  status?: code;
  _name?: Element;
  identifier?: Array<Identifier>;
  telecom?: Array<ContactPoint> /* Time period team covers */;
  period?: Period /* Who care team is for */;
  subject?: Reference<"Patient" | "Group">;
  reasonReference?: Array<Reference<"Condition">>;
}
/* Identifies two or more records (resource instances) that refer to the same real-world "occurrence". */
interface Linkage {
  /* Whether this linkage assertion is active or not */ active?: boolean;
  _active?: Element /* Who is responsible for linkages */;
  author?: Reference<"PractitionerRole" | "Organization" | "Practitioner">;
  item?: Array<{
    /* source | alternate | historical */ type?: code;
    _type?: Element /* Resource being linked */;
    resource?: Reference<ResourceType>;
  }>;
}
/* An occurrence of information being transmitted; e.g. an alert that was sent to a responsible provider, a public health agency that was notified about a reportable condition. */
interface Communication {
  category?: Array<CodeableConcept> /* When received */;
  received?: dateTime;
  instantiatesCanonical?: Array<canonical>;
  payload?: Array<{
    /* Message part content */
    content?: string | Attachment | Reference<ResourceType>;
  }>;
  instantiatesUri?: Array<uri>;
  _received?: Element /* Encounter created as part of */;
  encounter?: Reference<"Encounter">;
  medium?: Array<CodeableConcept>;
  _priority?: Element;
  _status?: Element;
  recipient?: Array<
    Reference<
      | "CareTeam"
      | "Patient"
      | "PractitionerRole"
      | "HealthcareService"
      | "Organization"
      | "Device"
      | "Practitioner"
      | "RelatedPerson"
      | "Group"
    >
  >;
  reasonCode?: Array<CodeableConcept> /* Reason for current status */;
  statusReason?: CodeableConcept /* Description of the purpose/content */;
  topic?: CodeableConcept /* When sent */;
  sent?: dateTime;
  note?: Array<Annotation> /* routine | urgent | asap | stat */;
  priority?: code /* preparation | in-progress | not-done | on-hold | stopped | completed | entered-in-error | unknown */;
  status?: code /* Message sender */;
  sender?: Reference<
    | "Patient"
    | "PractitionerRole"
    | "HealthcareService"
    | "Organization"
    | "Device"
    | "Practitioner"
    | "RelatedPerson"
  >;
  identifier?: Array<Identifier>;
  inResponseTo?: Array<Reference<"Communication">>;
  basedOn?: Array<Reference<ResourceType>>;
  partOf?: Array<Reference<ResourceType>>;
  _instantiatesCanonical?: Array<Element> /* Focus of message */;
  subject?: Reference<"Patient" | "Group">;
  about?: Array<Reference<ResourceType>>;
  reasonReference?: Array<
    Reference<
      "Observation" | "DocumentReference" | "DiagnosticReport" | "Condition"
    >
  >;
  _instantiatesUri?: Array<Element>;
  _sent?: Element;
}
/* Indicates that a medication product is to be or has been dispensed for a named person/patient.  This includes a description of the medication product (supply) provided and the instructions for administering the medication.  The medication dispense is the result of a pharmacy system responding to a medication order. */
interface MedicationDispense {
  _whenPrepared?: Element /* Type of medication dispense */;
  category?: CodeableConcept /* When product was given out */;
  whenHandedOver?: dateTime /* When product was packaged and reviewed */;
  whenPrepared?: dateTime;
  eventHistory?: Array<
    Reference<"Provenance">
  > /* Whether a substitution was performed on the dispense */;
  substitution?: {
    /* Whether a substitution was or was not performed on the dispense */
    wasSubstituted?: boolean;
    _wasSubstituted?: Element /* Code signifying whether a different drug was dispensed from what was prescribed */;
    type?: CodeableConcept;
    reason?: Array<CodeableConcept>;
    responsibleParty?: Array<Reference<"PractitionerRole" | "Practitioner">>;
  };
  detectedIssue?: Array<Reference<"DetectedIssue">>;
  _status?: Element /* Trial fill, partial fill, emergency fill, etc. */;
  type?: CodeableConcept /* Why a dispense was not performed */;
  statusReason?: CodeableConcept | Reference<"DetectedIssue">;
  note?: Array<Annotation>;
  supportingInformation?: Array<
    Reference<ResourceType>
  > /* preparation | in-progress | cancelled | on-hold | completed | entered-in-error | stopped | declined | unknown */;
  status?: code;
  dosageInstruction?: Array<Dosage> /* Amount of medication expressed as a timing amount */;
  daysSupply?: Quantity;
  identifier?: Array<Identifier> /* Encounter / Episode associated with event */;
  context?: Reference<"EpisodeOfCare" | "Encounter"> /* Amount dispensed */;
  quantity?: Quantity;
  partOf?: Array<Reference<"Procedure">>;
  _whenHandedOver?: Element /* Where the dispense occurred */;
  location?: Reference<"Location">;
  authorizingPrescription?: Array<Reference<"MedicationRequest">>;
  receiver?: Array<
    Reference<"Patient" | "Practitioner">
  > /* Who the dispense is for */;
  subject?: Reference<"Patient" | "Group"> /* Where the medication was sent */;
  destination?: Reference<"Location">;
  performer?: Array<{
    /* Who performed the dispense and what they did */
    function?: CodeableConcept /* Individual who was performing */;
    actor?: Reference<
      | "Patient"
      | "PractitionerRole"
      | "Organization"
      | "Device"
      | "Practitioner"
      | "RelatedPerson"
    >;
  }> /* What medication was supplied */;
  medication?: CodeableConcept | Reference<"Medication">;
}
/* Representation of the content produced in a DICOM imaging study. A study comprises a set of series, each of which includes a set of Service-Object Pair Instances (SOP Instances - images or other data) acquired or produced in a common context.  A series is of only one modality (e.g. X-ray, CT, MR, ultrasound), but a study may have multiple series of different modalities. */
interface ImagingStudy {
  /* Institution-generated description */
  description?: string /* When the study was started */;
  started?: dateTime /* Number of Study Related Series */;
  numberOfSeries?: unsignedInt;
  interpreter?: Array<Reference<"PractitionerRole" | "Practitioner">>;
  series?: Array<{
    /* A short human readable summary of the series */
    description?: string /* When the series started */;
    started?: dateTime /* Body part laterality */;
    laterality?: Coding;
    instance?: Array<{
      /* DICOM SOP Instance UID */ uid?: id;
      _uid?: Element /* DICOM class type */;
      sopClass?: Coding /* The number of this instance in the series */;
      number?: unsignedInt;
      _number?: Element /* Description of instance */;
      title?: string;
      _title?: Element;
    }> /* Numeric identifier of this series */;
    number?: unsignedInt /* DICOM Series Instance UID for the series */;
    uid?: id;
    specimen?: Array<Reference<"Specimen">>;
    _number?: Element /* The modality of the instances in the series */;
    modality?: Coding;
    _numberOfInstances?: Element;
    _description?: Element;
    _started?: Element /* Body part examined */;
    bodySite?: Coding;
    _uid?: Element;
    endpoint?: Array<
      Reference<"Endpoint">
    > /* Number of Series Related Instances */;
    numberOfInstances?: unsignedInt;
    performer?: Array<{
      /* Type of performance */
      function?: CodeableConcept /* Who performed the series */;
      actor?: Reference<
        | "CareTeam"
        | "Patient"
        | "PractitionerRole"
        | "Organization"
        | "Device"
        | "Practitioner"
        | "RelatedPerson"
      >;
    }>;
  }> /* The performed Procedure reference */;
  procedureReference?: Reference<"Procedure"> /* Encounter with which this imaging study is associated */;
  encounter?: Reference<"Encounter">;
  _numberOfSeries?: Element;
  _status?: Element;
  reasonCode?: Array<CodeableConcept>;
  modality?: Array<Coding>;
  _numberOfInstances?: Element;
  _description?: Element;
  note?: Array<Annotation> /* Referring physician */;
  referrer?: Reference<"PractitionerRole" | "Practitioner">;
  _started?: Element /* registered | available | cancelled | entered-in-error | unknown */;
  status?: code;
  identifier?: Array<Identifier>;
  basedOn?: Array<
    Reference<
      | "ServiceRequest"
      | "CarePlan"
      | "Task"
      | "AppointmentResponse"
      | "Appointment"
    >
  > /* Where ImagingStudy occurred */;
  location?: Reference<"Location">;
  endpoint?: Array<
    Reference<"Endpoint">
  > /* Who or what is the subject of the study */;
  subject?: Reference<
    "Patient" | "Device" | "Group"
  > /* Number of Study Related Instances */;
  numberOfInstances?: unsignedInt;
  reasonReference?: Array<
    Reference<
      | "Media"
      | "Observation"
      | "DocumentReference"
      | "DiagnosticReport"
      | "Condition"
    >
  >;
  procedureCode?: Array<CodeableConcept>;
}
/* The resource ChargeItem describes the provision of healthcare provider products for a certain patient, therefore referring not only to the product, but containing in addition details of the provision, like date, time, amounts and participating organizations and persons. Main Usage of the ChargeItem is to enable the billing process and internal cost allocation. */
interface ChargeItem {
  service?: Array<
    Reference<
      | "MedicationDispense"
      | "MedicationAdministration"
      | "ImagingStudy"
      | "Observation"
      | "Procedure"
      | "DiagnosticReport"
      | "Immunization"
      | "SupplyDelivery"
    >
  >;
  definitionUri?: Array<uri> /* Individual who was entering */;
  enterer?: Reference<
    | "Patient"
    | "PractitionerRole"
    | "Organization"
    | "Device"
    | "Practitioner"
    | "RelatedPerson"
  > /* Organization requesting the charged service */;
  requestingOrganization?: Reference<"Organization">;
  definitionCanonical?: Array<canonical>;
  _status?: Element;
  bodysite?: Array<CodeableConcept>;
  _definitionUri?: Array<Element> /* Organization that has ownership of the (potential, future) revenue */;
  costCenter?: Reference<"Organization">;
  note?: Array<Annotation>;
  account?: Array<Reference<"Account">>;
  _definitionCanonical?: Array<Element>;
  _overrideReason?: Element;
  reason?: Array<CodeableConcept> /* Product charged */;
  product?: CodeableConcept | Reference<"Medication" | "Device" | "Substance">;
  supportingInformation?: Array<
    Reference<ResourceType>
  > /* planned | billable | not-billable | aborted | billed | entered-in-error | unknown */;
  status?: code;
  _factorOverride?: Element /* A code that identifies the charge, like a billing code */;
  code?: CodeableConcept;
  identifier?: Array<Identifier>;
  _enteredDate?: Element /* Encounter / Episode associated with event */;
  context?: Reference<
    "EpisodeOfCare" | "Encounter"
  > /* Quantity of which the charge item has been serviced */;
  quantity?: Quantity;
  partOf?: Array<
    Reference<"ChargeItem">
  > /* Price overriding the associated rules */;
  priceOverride?: Money /* Date the charge item was entered */;
  enteredDate?: dateTime /* Reason for overriding the list price/factor */;
  overrideReason?: string /* Organization providing the charged service */;
  performingOrganization?: Reference<"Organization"> /* Individual service was done for/to */;
  subject?: Reference<
    "Patient" | "Group"
  > /* Factor overriding the associated rules */;
  factorOverride?: decimal /* When the charged service was applied */;
  occurrence?: dateTime | Period | Timing;
  performer?: Array<{
    /* What type of performance was done */
    function?: CodeableConcept /* Individual who was performing */;
    actor?: Reference<
      | "CareTeam"
      | "Patient"
      | "PractitionerRole"
      | "Organization"
      | "Device"
      | "Practitioner"
      | "RelatedPerson"
    >;
  }>;
}
/* Actual or  potential/avoided event causing unintended physical injury resulting from or contributed to by medical care, a research study or other healthcare setting factors that requires additional monitoring, treatment, or hospitalization, or that results in death. */
interface AdverseEvent {
  category?: Array<CodeableConcept> /* actual | potential */;
  actuality?: code /* When the event occurred */;
  date?: dateTime;
  study?: Array<Reference<"ResearchStudy">> /* Encounter created as part of */;
  encounter?: Reference<"Encounter">;
  _date?: Element;
  suspectEntity?: Array<{
    /* Refers to the specific entity that caused the adverse event */
    instance?: Reference<
      | "MedicationAdministration"
      | "Medication"
      | "Device"
      | "Substance"
      | "Procedure"
      | "Immunization"
      | "MedicationStatement"
    >;
    causality?: Array<{
      /* Assessment of if the entity caused the event */
      assessment?: CodeableConcept /* AdverseEvent.suspectEntity.causalityProductRelatedness */;
      productRelatedness?: string;
      _productRelatedness?: Element /* AdverseEvent.suspectEntity.causalityAuthor */;
      author?: Reference<
        "PractitionerRole" | "Practitioner"
      > /* ProbabilityScale | Bayesian | Checklist */;
      method?: CodeableConcept;
    }>;
  }>;
  referenceDocument?: Array<
    Reference<"DocumentReference">
  > /* resolved | recovering | ongoing | resolvedWithSequelae | fatal | unknown */;
  outcome?: CodeableConcept /* When the event was recorded */;
  recordedDate?: dateTime /* Type of the event itself in relation to the subject */;
  event?: CodeableConcept;
  contributor?: Array<
    Reference<"PractitionerRole" | "Device" | "Practitioner">
  >;
  subjectMedicalHistory?: Array<
    Reference<
      | "Media"
      | "Observation"
      | "DocumentReference"
      | "Procedure"
      | "FamilyMemberHistory"
      | "Immunization"
      | "Condition"
      | "AllergyIntolerance"
    >
  >;
  _recordedDate?: Element /* Who recorded the adverse event */;
  recorder?: Reference<
    "Patient" | "PractitionerRole" | "Practitioner" | "RelatedPerson"
  > /* Seriousness of the event */;
  seriousness?: CodeableConcept /* mild | moderate | severe */;
  severity?: CodeableConcept /* Business identifier for the event */;
  identifier?: Identifier /* When the event was detected */;
  detected?: dateTime /* Location where adverse event occurred */;
  location?: Reference<"Location">;
  _detected?: Element /* Subject impacted by event */;
  subject?: Reference<"Patient" | "Practitioner" | "RelatedPerson" | "Group">;
  _actuality?: Element;
  resultingCondition?: Array<Reference<"Condition">>;
}
/* A photo, video, or audio recording acquired or used in healthcare. The actual content may be inline or provided by direct reference. */
interface Media {
  _issued?: Element /* Name of the device/manufacturer */;
  deviceName?: string;
  _width?: Element;
  _duration?: Element /* Encounter associated with media */;
  encounter?: Reference<"Encounter"> /* Actual Media - reference or data */;
  content?: Attachment /* Number of frames if > 1 (photo) */;
  frames?: positiveInt;
  _status?: Element /* Width of the image in pixels (photo/video) */;
  width?: positiveInt;
  reasonCode?: Array<CodeableConcept> /* Classification of media as image, video, or audio */;
  type?: CodeableConcept /* When Media was collected */;
  created?: dateTime | Period /* The type of acquisition equipment/process */;
  modality?: CodeableConcept /* Length in seconds (audio / video) */;
  duration?: decimal;
  _frames?: Element;
  note?: Array<Annotation>;
  _height?: Element /* preparation | in-progress | not-done | on-hold | stopped | completed | entered-in-error | unknown */;
  status?: code;
  identifier?: Array<Identifier> /* The person who generated the image */;
  operator?: Reference<
    | "CareTeam"
    | "Patient"
    | "PractitionerRole"
    | "Organization"
    | "Device"
    | "Practitioner"
    | "RelatedPerson"
  > /* Observed body part */;
  bodySite?: CodeableConcept /* Date/Time this version was made available */;
  issued?: instant /* Observing Device */;
  device?: Reference<"Device" | "DeviceMetric">;
  basedOn?: Array<Reference<"ServiceRequest" | "CarePlan">>;
  _deviceName?: Element;
  partOf?: Array<
    Reference<ResourceType>
  > /* Who/What this Media is a record of */;
  subject?: Reference<
    | "Patient"
    | "PractitionerRole"
    | "Device"
    | "Location"
    | "Practitioner"
    | "Specimen"
    | "Group"
  > /* Imaging view, e.g. Lateral or Antero-posterior */;
  view?: CodeableConcept /* Height of the image in pixels (photo/video) */;
  height?: positiveInt;
}
/* Todo. */
interface SubstancePolymer {
  /* Todo */ class?: CodeableConcept /* Todo */;
  geometry?: CodeableConcept;
  copolymerConnectivity?: Array<CodeableConcept>;
  modification?: Array<string>;
  _modification?: Array<Element>;
  monomerSet?: Array<{
    /* Todo */ ratioType?: CodeableConcept;
    startingMaterial?: Array<{
      /* Todo */ material?: CodeableConcept /* Todo */;
      type?: CodeableConcept /* Todo */;
      isDefining?: boolean;
      _isDefining?: Element /* Todo */;
      amount?: SubstanceAmount;
    }>;
  }>;
  repeat?: Array<{
    /* Todo */ numberOfUnits?: integer;
    _numberOfUnits?: Element /* Todo */;
    averageMolecularFormula?: string;
    _averageMolecularFormula?: Element /* Todo */;
    repeatUnitAmountType?: CodeableConcept;
    repeatUnit?: Array<{
      /* Todo */ orientationOfPolymerisation?: CodeableConcept /* Todo */;
      repeatUnit?: string;
      _repeatUnit?: Element /* Todo */;
      amount?: SubstanceAmount;
      degreeOfPolymerisation?: Array<{
        /* Todo */ degree?: CodeableConcept /* Todo */;
        amount?: SubstanceAmount;
      }>;
      structuralRepresentation?: Array<{
        /* Todo */ type?: CodeableConcept /* Todo */;
        representation?: string;
        _representation?: Element /* Todo */;
        attachment?: Attachment;
      }>;
    }>;
  }>;
}
/* A structured set of questions and their answers. The questions are ordered and grouped into coherent subsets, corresponding to the structure of the grouping of the questionnaire being responded to. */
interface QuestionnaireResponse {
  /* Form being answered */
  questionnaire?: canonical /* Encounter created as part of */;
  encounter?: Reference<"Encounter">;
  item?: Array<QuestionnaireResponse>;
  _status?: Element;
  _authored?: Element /* The person who answered the questions */;
  source?: Reference<
    "Patient" | "PractitionerRole" | "Practitioner" | "RelatedPerson"
  > /* Person who received and recorded the answers */;
  author?: Reference<
    | "Patient"
    | "PractitionerRole"
    | "Organization"
    | "Device"
    | "Practitioner"
    | "RelatedPerson"
  > /* in-progress | completed | amended | entered-in-error | stopped */;
  status?: code /* Unique id for this set of answers */;
  identifier?: Identifier;
  basedOn?: Array<
    Reference<"ServiceRequest" | "CarePlan">
  > /* Date the answers were gathered */;
  authored?: dateTime;
  partOf?: Array<
    Reference<"Observation" | "Procedure">
  > /* The subject of the questions */;
  subject?: Reference<ResourceType>;
  _questionnaire?: Element;
}
/* Financial instrument which may be used to reimburse or pay for health care products and services. Includes both insurance and self-payment. */
interface Coverage {
  _order?: Element /* Owner of the policy */;
  policyHolder?: Reference<
    "Patient" | "Organization" | "RelatedPerson"
  > /* Plan beneficiary */;
  beneficiary?: Reference<"Patient">;
  contract?: Array<Reference<"Contract">>;
  _status?: Element /* Beneficiary relationship to the subscriber */;
  relationship?: CodeableConcept /* Coverage category such as medical or accident */;
  type?: CodeableConcept;
  costToBeneficiary?: Array<{
    /* Cost category */
    type?: CodeableConcept /* The amount or percentage due from the beneficiary */;
    value?: Money | Quantity;
    exception?: Array<{
      /* Exception category */
      type?: CodeableConcept /* The effective period of the exception */;
      period?: Period;
    }>;
  }> /* Reimbursement to insurer */;
  subrogation?: boolean /* Subscriber to the policy */;
  subscriber?: Reference<"Patient" | "RelatedPerson">;
  payor?: Array<
    Reference<"Patient" | "Organization" | "RelatedPerson">
  > /* active | cancelled | draft | entered-in-error */;
  status?: code;
  class?: Array<{
    /* Type of class such as 'group' or 'plan' */
    type?: CodeableConcept /* Value associated with the type */;
    value?: string;
    _value?: Element /* Human readable description of the type and value */;
    name?: string;
    _name?: Element;
  }>;
  identifier?: Array<Identifier> /* Relative order of the coverage */;
  order?: positiveInt /* Insurer network */;
  network?: string /* Coverage start and end dates */;
  period?: Period;
  _network?: Element /* Dependent number */;
  dependent?: string;
  _subscriberId?: Element;
  _dependent?: Element;
  _subrogation?: Element /* ID assigned to the subscriber */;
  subscriberId?: string;
}
/* An action that is or was performed on or for a patient. This can be a physical intervention like an operation, or less invasive like long term services, counseling, or hypnotherapy. */
interface Procedure {
  /* Classification of the procedure */ category?: CodeableConcept;
  report?: Array<
    Reference<"Composition" | "DocumentReference" | "DiagnosticReport">
  >;
  usedCode?: Array<CodeableConcept>;
  usedReference?: Array<Reference<"Medication" | "Device" | "Substance">>;
  instantiatesCanonical?: Array<canonical>;
  instantiatesUri?: Array<uri>;
  focalDevice?: Array<{
    /* Kind of change to device */
    action?: CodeableConcept /* Device that was changed */;
    manipulated?: Reference<"Device">;
  }> /* Encounter created as part of */;
  encounter?: Reference<"Encounter">;
  complicationDetail?: Array<Reference<"Condition">>;
  _status?: Element;
  reasonCode?: Array<CodeableConcept> /* Reason for current status */;
  statusReason?: CodeableConcept /* When the procedure was performed */;
  performed?:
    | string
    | dateTime
    | Range
    | Period
    | Age /* The result of procedure */;
  outcome?: CodeableConcept /* Person who asserts this procedure */;
  asserter?: Reference<
    "Patient" | "PractitionerRole" | "Practitioner" | "RelatedPerson"
  >;
  note?: Array<Annotation>;
  complication?: Array<CodeableConcept> /* preparation | in-progress | not-done | on-hold | stopped | completed | entered-in-error | unknown */;
  status?: code /* Who recorded the procedure */;
  recorder?: Reference<
    "Patient" | "PractitionerRole" | "Practitioner" | "RelatedPerson"
  > /* Identification of the procedure */;
  code?: CodeableConcept;
  identifier?: Array<Identifier>;
  bodySite?: Array<CodeableConcept>;
  basedOn?: Array<Reference<"ServiceRequest" | "CarePlan">>;
  partOf?: Array<
    Reference<"MedicationAdministration" | "Observation" | "Procedure">
  > /* Where the procedure happened */;
  location?: Reference<"Location">;
  followUp?: Array<CodeableConcept>;
  _instantiatesCanonical?: Array<Element> /* Who the procedure was performed on */;
  subject?: Reference<"Patient" | "Group">;
  performer?: Array<{
    /* Type of performance */
    function?: CodeableConcept /* The reference to the practitioner */;
    actor?: Reference<
      | "Patient"
      | "PractitionerRole"
      | "Organization"
      | "Device"
      | "Practitioner"
      | "RelatedPerson"
    > /* Organization the device or practitioner was acting for */;
    onBehalfOf?: Reference<"Organization">;
  }>;
  reasonReference?: Array<
    Reference<
      | "Observation"
      | "DocumentReference"
      | "Procedure"
      | "DiagnosticReport"
      | "Condition"
    >
  >;
  _instantiatesUri?: Array<Element>;
}
/* A record of an event made for purposes of maintaining a security log. Typical uses include detection of intrusion attempts and monitoring for inappropriate usage. */
interface AuditEvent {
  /* Description of the event outcome */ outcomeDesc?: string;
  _action?: Element /* Type/identifier of event */;
  type?: Coding /* Whether the event succeeded or failed */;
  outcome?: code /* Audit Event Reporter */;
  source?: {
    /* Logical source location within the enterprise */ site?: string;
    _site?: Element /* The identity of source detecting the event */;
    observer?: Reference<
      | "Patient"
      | "PractitionerRole"
      | "Organization"
      | "Device"
      | "Practitioner"
      | "RelatedPerson"
    >;
    type?: Array<Coding>;
  } /* Time when the event was recorded */;
  recorded?: instant;
  agent?: Array<{
    role?: Array<CodeableConcept> /* Whether user is initiator */;
    requestor?: boolean /* Identifier of who */;
    who?: Reference<
      | "Patient"
      | "PractitionerRole"
      | "Organization"
      | "Device"
      | "Practitioner"
      | "RelatedPerson"
    > /* Alternative User identity */;
    altId?: string /* Human friendly name for the agent */;
    name?: string /* How agent participated */;
    type?: CodeableConcept;
    _altId?: Element;
    policy?: Array<uri>;
    purposeOfUse?: Array<CodeableConcept>;
    _policy?: Array<Element>;
    _name?: Element /* Logical network location for application activity */;
    network?: {
      /* Identifier for the network access point of the user device */
      address?: string;
      _address?: Element /* The type of network access point */;
      type?: code;
      _type?: Element;
    } /* Where */;
    location?: Reference<"Location"> /* Type of media */;
    media?: Coding;
    _requestor?: Element;
  }>;
  _outcome?: Element;
  purposeOfEvent?: Array<CodeableConcept> /* Type of action performed during the event */;
  action?: code;
  _outcomeDesc?: Element /* When the activity occurred */;
  period?: Period;
  _recorded?: Element;
  entity?: Array<{
    /* What role the entity played */ role?: Coding /* Descriptive text */;
    description?: string;
    _query?: Element /* Descriptor for entity */;
    name?: string /* Type of entity involved */;
    type?: Coding /* Life-cycle stage for the entity */;
    lifecycle?: Coding;
    _description?: Element;
    _name?: Element /* Query parameters */;
    query?: base64Binary;
    securityLabel?: Array<Coding> /* Specific instance of resource */;
    what?: Reference<ResourceType>;
    detail?: Array<{
      /* Name of the property */ type?: string;
      _type?: Element /* Property value */;
      value?: base64Binary | string;
    }>;
  }>;
  subtype?: Array<Coding>;
}
/* This resource provides the details including amount of a payment and allocates the payment items being paid. */
interface PaymentReconciliation {
  _created?: Element /* Responsible practitioner */;
  requestor?: Reference<
    "PractitionerRole" | "Organization" | "Practitioner"
  > /* Reference to requesting resource */;
  request?: Reference<"Task"> /* Total amount of Payment */;
  paymentAmount?: Money;
  processNote?: Array<{
    /* display | print | printoper */ type?: code;
    _type?: Element /* Note explanatory text */;
    text?: string;
    _text?: Element;
  }>;
  _disposition?: Element;
  _status?: Element /* Creation date */;
  created?: dateTime /* queued | complete | error | partial */;
  outcome?: code;
  _outcome?: Element /* Disposition message */;
  disposition?: string /* Business identifier for the payment */;
  paymentIdentifier?: Identifier /* active | cancelled | draft | entered-in-error */;
  status?: code /* When payment issued */;
  paymentDate?: date;
  identifier?: Array<Identifier>;
  _paymentDate?: Element /* Period covered */;
  period?: Period /* Party generating payment */;
  paymentIssuer?: Reference<"Organization"> /* Printed form identifier */;
  formCode?: CodeableConcept;
  detail?: Array<{
    /* Response committing to a payment */
    response?: Reference<ResourceType> /* Amount allocated to this payable */;
    amount?: Money /* Date of commitment to pay */;
    date?: date /* Request giving rise to the payment */;
    request?: Reference<ResourceType>;
    _date?: Element /* Category of payment */;
    type?: CodeableConcept /* Contact for the response */;
    responsible?: Reference<"PractitionerRole"> /* Recipient of the payment */;
    payee?: Reference<
      "PractitionerRole" | "Organization" | "Practitioner"
    > /* Business identifier of the prior payment detail */;
    predecessor?: Identifier /* Business identifier of the payment detail */;
    identifier?: Identifier /* Submitter of the request */;
    submitter?: Reference<"PractitionerRole" | "Organization" | "Practitioner">;
  }>;
}
/* The manufactured item as contained in the packaged medicinal product. */
interface MedicinalProductManufactured {
  /* Dose form as manufactured and before any transformation into the pharmaceutical product */
  manufacturedDoseForm?: CodeableConcept /* The “real world” units in which the quantity of the manufactured item is described */;
  unitOfPresentation?: CodeableConcept /* The quantity or "count number" of the manufactured item */;
  quantity?: Quantity;
  manufacturer?: Array<Reference<"Organization">>;
  ingredient?: Array<
    Reference<"MedicinalProductIngredient">
  > /* Dimensions, color etc. */;
  physicalCharacteristics?: ProdCharacteristic;
  otherCharacteristics?: Array<CodeableConcept>;
}
/* A compartment definition that defines how resources are accessed on a server. */
interface CompartmentDefinition {
  /* Natural language description of the compartment definition */
  description?: markdown;
  _code?: Element /* Date last changed */;
  date?: dateTime /* Name of the publisher (organization or individual) */;
  publisher?: string;
  _publisher?: Element /* Why this compartment definition is defined */;
  purpose?: markdown;
  _date?: Element /* Name for this compartment definition (computer friendly) */;
  name?: string;
  _status?: Element;
  _experimental?: Element;
  useContext?: Array<UsageContext> /* For testing purposes, not real usage */;
  experimental?: boolean /* Whether the search syntax is supported */;
  search?: boolean;
  _description?: Element;
  _search?: Element;
  _purpose?: Element /* draft | active | retired | unknown */;
  status?: code;
  resource?: Array<{
    /* Name of resource type */ code?: code;
    _code?: Element;
    param?: Array<string>;
    _param?: Array<Element> /* Additional documentation about the resource and compartment */;
    documentation?: string;
    _documentation?: Element;
  }>;
  _name?: Element /* Canonical identifier for this compartment definition, represented as a URI (globally unique) */;
  url?: uri /* Patient | Encounter | RelatedPerson | Practitioner | Device */;
  code?: code /* Business version of the compartment definition */;
  version?: string;
  _version?: Element;
  contact?: Array<ContactDetail>;
  _url?: Element;
}
/* A formally or informally recognized grouping of people or organizations formed for the purpose of achieving some form of collective action.  Includes companies, institutions, corporations, departments, community groups, healthcare practice groups, payer/insurer, etc. */
interface Organization {
  _active?: Element;
  address?: Array<Address> /* Name used for the organization */;
  name?: string;
  type?: Array<CodeableConcept>;
  alias?: Array<string> /* Whether the organization's record is still in active use */;
  active?: boolean;
  _name?: Element;
  identifier?: Array<Identifier>;
  telecom?: Array<ContactPoint> /* The organization of which this organization forms a part */;
  partOf?: Reference<"Organization">;
  _alias?: Array<Element>;
  endpoint?: Array<Reference<"Endpoint">>;
  contact?: Array<{
    /* The type of contact */
    purpose?: CodeableConcept /* A name associated with the contact */;
    name?: HumanName;
    telecom?: Array<ContactPoint> /* Visiting or postal addresses for the contact */;
    address?: Address;
  }>;
}
/* This resource provides: the claim details; adjudication details from the processing of a Claim; and optionally account balance information, for informing the subscriber of the benefits provided. */
interface ExplanationOfBenefit {
  _created?: Element /* The recipient of the products and services */;
  patient?: Reference<"Patient"> /* Claim response reference */;
  claimResponse?: Reference<"ClaimResponse">;
  insurance?: Array<{
    /* Coverage to be used for adjudication */ focal?: boolean;
    _focal?: Element /* Insurance information */;
    coverage?: Reference<"Coverage">;
    preAuthRef?: Array<string>;
    _preAuthRef?: Array<Element>;
  }>;
  benefitBalance?: Array<{
    /* Description of the benefit or services covered */
    description?: string /* Benefit classification */;
    category?: CodeableConcept /* Individual or family */;
    unit?: CodeableConcept /* Excluded from the plan */;
    excluded?: boolean /* Short name for the benefit */;
    name?: string;
    financial?: Array<{
      /* Benefit classification */
      type?: CodeableConcept /* Benefits allowed */;
      allowed?: unsignedInt | string | Money /* Benefits used */;
      used?: unsignedInt | Money;
    }>;
    _description?: Element /* Annual or lifetime */;
    term?: CodeableConcept;
    _name?: Element /* In or out of network */;
    network?: CodeableConcept;
    _excluded?: Element;
  }> /* Servicing Facility */;
  facility?: Reference<"Location">;
  processNote?: Array<{
    /* Note instance identifier */ number?: positiveInt;
    _number?: Element /* display | print | printoper */;
    type?: code;
    _type?: Element /* Note explanatory text */;
    text?: string;
    _text?: Element /* Language of the text */;
    language?: CodeableConcept;
  }>;
  diagnosis?: Array<{
    /* Diagnosis instance identifier */ sequence?: positiveInt;
    _sequence?: Element /* Nature of illness or problem */;
    diagnosis?: CodeableConcept | Reference<"Condition">;
    type?: Array<CodeableConcept> /* Present on admission */;
    onAdmission?: CodeableConcept /* Package billing code */;
    packageCode?: CodeableConcept;
  }>;
  preAuthRef?: Array<string>;
  adjudication?: Array<ExplanationOfBenefit>;
  _disposition?: Element /* Author of the claim */;
  enterer?: Reference<"PractitionerRole" | "Practitioner">;
  supportingInfo?: Array<{
    /* Information instance identifier */ sequence?: positiveInt;
    _sequence?: Element /* Classification of the supplied information */;
    category?: CodeableConcept /* Type of information */;
    code?: CodeableConcept /* When it occurred */;
    timing?: date | Period /* Data to be provided */;
    value?:
      | string
      | Attachment
      | Quantity
      | boolean
      | Reference<ResourceType> /* Explanation for the information */;
    reason?: Coding;
  }>;
  _precedence?: Element /* claim | preauthorization | predetermination */;
  use?: code /* Payment Details */;
  payment?: {
    /* Partial or complete payment */
    type?: CodeableConcept /* Payment adjustment for non-claim issues */;
    adjustment?: Money /* Explanation for the variance */;
    adjustmentReason?: CodeableConcept /* Expected date of payment */;
    date?: date;
    _date?: Element /* Payable amount after adjustment */;
    amount?: Money /* Business identifier for the payment */;
    identifier?: Identifier;
  };
  item?: Array<{
    /* Benefit classification */ category?: CodeableConcept;
    diagnosisSequence?: Array<positiveInt>;
    _sequence?: Element;
    procedureSequence?: Array<positiveInt>;
    _noteNumber?: Array<Element>;
    modifier?: Array<CodeableConcept> /* Revenue or cost center code */;
    revenue?: CodeableConcept;
    adjudication?: Array<ExplanationOfBenefit>;
    encounter?: Array<Reference<"Encounter">>;
    _careTeamSequence?: Array<Element>;
    _factor?: Element /* Total item cost */;
    net?: Money /* Date or dates of service or product delivery */;
    serviced?: date | Period;
    subSite?: Array<CodeableConcept>;
    _procedureSequence?: Array<Element>;
    careTeamSequence?: Array<positiveInt> /* Billing, service, product, or drug code */;
    productOrService?: CodeableConcept;
    _informationSequence?: Array<Element>;
    udi?: Array<Reference<"Device">>;
    informationSequence?: Array<positiveInt>;
    programCode?: Array<CodeableConcept> /* Price scaling factor */;
    factor?: decimal;
    _diagnosisSequence?: Array<Element> /* Item instance identifier */;
    sequence?: positiveInt /* Anatomical location */;
    bodySite?: CodeableConcept /* Count of products or services */;
    quantity?: Quantity /* Place of service or where product was supplied */;
    location?: Address | CodeableConcept | Reference<"Location">;
    noteNumber?: Array<positiveInt> /* Fee, charge or cost per item */;
    unitPrice?: Money;
    detail?: Array<{
      /* Benefit classification */ category?: CodeableConcept;
      _sequence?: Element;
      _noteNumber?: Array<Element>;
      modifier?: Array<CodeableConcept> /* Revenue or cost center code */;
      revenue?: CodeableConcept;
      adjudication?: Array<ExplanationOfBenefit>;
      _factor?: Element /* Total item cost */;
      net?: Money /* Billing, service, product, or drug code */;
      productOrService?: CodeableConcept;
      udi?: Array<Reference<"Device">>;
      programCode?: Array<CodeableConcept> /* Price scaling factor */;
      factor?: decimal /* Product or service provided */;
      sequence?: positiveInt;
      subDetail?: Array<{
        /* Benefit classification */ category?: CodeableConcept;
        _sequence?: Element;
        _noteNumber?: Array<Element>;
        modifier?: Array<CodeableConcept> /* Revenue or cost center code */;
        revenue?: CodeableConcept;
        adjudication?: Array<ExplanationOfBenefit>;
        _factor?: Element /* Total item cost */;
        net?: Money /* Billing, service, product, or drug code */;
        productOrService?: CodeableConcept;
        udi?: Array<Reference<"Device">>;
        programCode?: Array<CodeableConcept> /* Price scaling factor */;
        factor?: decimal /* Product or service provided */;
        sequence?: positiveInt /* Count of products or services */;
        quantity?: Quantity;
        noteNumber?: Array<positiveInt> /* Fee, charge or cost per item */;
        unitPrice?: Money;
      }> /* Count of products or services */;
      quantity?: Quantity;
      noteNumber?: Array<positiveInt> /* Fee, charge or cost per item */;
      unitPrice?: Money;
    }>;
  }>;
  _status?: Element /* Category or discipline */;
  type?: CodeableConcept /* Response creation date */;
  created?: dateTime;
  procedure?: Array<{
    /* Procedure instance identifier */ sequence?: positiveInt;
    _sequence?: Element;
    type?: Array<CodeableConcept> /* When the procedure was performed */;
    date?: dateTime;
    _date?: Element /* Specific clinical procedure */;
    procedure?: CodeableConcept | Reference<"Procedure">;
    udi?: Array<Reference<"Device">>;
  }> /* queued | complete | error | partial */;
  outcome?: code;
  related?: Array<{
    /* Reference to the related claim */
    claim?: Reference<"Claim"> /* How the reference claim is related */;
    relationship?: CodeableConcept /* File or case reference */;
    reference?: Identifier;
  }>;
  _outcome?: Element /* Disposition Message */;
  disposition?: string /* Treatment Referral */;
  referral?: Reference<"ServiceRequest">;
  preAuthRefPeriod?: Array<Period>;
  total?: Array<{
    /* Type of adjudication information */
    category?: CodeableConcept /* Financial total for the category */;
    amount?: Money;
  }> /* Party responsible for reimbursement */;
  insurer?: Reference<"Organization"> /* Funds reserved status */;
  fundsReserve?: CodeableConcept /* Desired processing urgency */;
  priority?: CodeableConcept /* Details of the event */;
  accident?: {
    /* When the incident occurred */ date?: date;
    _date?: Element /* The nature of the accident */;
    type?: CodeableConcept /* Where the event occurred */;
    location?: Address | Reference<"Location">;
  } /* active | cancelled | draft | entered-in-error */;
  status?: code /* Recipient of benefits payable */;
  payee?: {
    /* Category of recipient */
    type?: CodeableConcept /* Recipient reference */;
    party?: Reference<
      | "Patient"
      | "PractitionerRole"
      | "Organization"
      | "Practitioner"
      | "RelatedPerson"
    >;
  } /* Prescription authorizing services or products */;
  prescription?: Reference<
    "VisionPrescription" | "MedicationRequest"
  > /* Relevant time frame for the claim */;
  billablePeriod?: Period;
  identifier?: Array<Identifier> /* Printed reference or actual form */;
  form?: Attachment;
  _preAuthRef?: Array<Element> /* More granular claim type */;
  subType?: CodeableConcept /* For whom to reserve funds */;
  fundsReserveRequested?: CodeableConcept /* When the benefits are applicable */;
  benefitPeriod?: Period /* Precedence (primary, secondary, etc.) */;
  precedence?: positiveInt /* Printed form identifier */;
  formCode?: CodeableConcept /* Party responsible for the claim */;
  provider?: Reference<"PractitionerRole" | "Organization" | "Practitioner">;
  addItem?: Array<{
    _noteNumber?: Array<Element>;
    modifier?: Array<CodeableConcept>;
    adjudication?: Array<ExplanationOfBenefit>;
    _factor?: Element;
    itemSequence?: Array<positiveInt> /* Total item cost */;
    net?: Money /* Date or dates of service or product delivery */;
    serviced?: date | Period;
    detailSequence?: Array<positiveInt>;
    subSite?: Array<CodeableConcept> /* Billing, service, product, or drug code */;
    productOrService?: CodeableConcept;
    _detailSequence?: Array<Element>;
    _itemSequence?: Array<Element>;
    programCode?: Array<CodeableConcept>;
    _subDetailSequence?: Array<Element> /* Price scaling factor */;
    factor?: decimal;
    subDetailSequence?: Array<positiveInt> /* Anatomical location */;
    bodySite?: CodeableConcept /* Count of products or services */;
    quantity?: Quantity /* Place of service or where product was supplied */;
    location?: Address | CodeableConcept | Reference<"Location">;
    provider?: Array<
      Reference<"PractitionerRole" | "Organization" | "Practitioner">
    >;
    noteNumber?: Array<positiveInt> /* Fee, charge or cost per item */;
    unitPrice?: Money;
    detail?: Array<{
      _noteNumber?: Array<Element>;
      modifier?: Array<CodeableConcept>;
      adjudication?: Array<ExplanationOfBenefit>;
      _factor?: Element /* Total item cost */;
      net?: Money /* Billing, service, product, or drug code */;
      productOrService?: CodeableConcept /* Price scaling factor */;
      factor?: decimal;
      subDetail?: Array<{
        _noteNumber?: Array<Element>;
        modifier?: Array<CodeableConcept>;
        adjudication?: Array<ExplanationOfBenefit>;
        _factor?: Element /* Total item cost */;
        net?: Money /* Billing, service, product, or drug code */;
        productOrService?: CodeableConcept /* Price scaling factor */;
        factor?: decimal /* Count of products or services */;
        quantity?: Quantity;
        noteNumber?: Array<positiveInt> /* Fee, charge or cost per item */;
        unitPrice?: Money;
      }> /* Count of products or services */;
      quantity?: Quantity;
      noteNumber?: Array<positiveInt> /* Fee, charge or cost per item */;
      unitPrice?: Money;
    }>;
  }> /* Original prescription if superceded by fulfiller */;
  originalPrescription?: Reference<"MedicationRequest">;
  _use?: Element;
  careTeam?: Array<{
    /* Order of care team */ sequence?: positiveInt;
    _sequence?: Element /* Practitioner or organization */;
    provider?: Reference<
      "PractitionerRole" | "Organization" | "Practitioner"
    > /* Indicator of the lead practitioner */;
    responsible?: boolean;
    _responsible?: Element /* Function within the team */;
    role?: CodeableConcept /* Practitioner credential or specialization */;
    qualification?: CodeableConcept;
  }> /* Claim reference */;
  claim?: Reference<"Claim">;
}
/* A set of healthcare-related information that is assembled together into a single logical package that provides a single coherent statement of meaning, establishes its own context and that has clinical attestation with regard to who is making the statement. A Composition defines the structure and narrative content necessary for a document. However, a Composition alone does not constitute a document. Rather, the Composition must be the first entry in a Bundle where Bundle.type=document, and any other resources referenced from Composition must be included as subsequent entries in the Bundle (for example Patient, Practitioner, Encounter, etc.). */
interface Composition {
  category?: Array<CodeableConcept> /* Composition editing time */;
  date?: dateTime /* Context of the Composition */;
  encounter?: Reference<"Encounter">;
  _date?: Element;
  section?: Array<Composition>;
  _status?: Element;
  attester?: Array<{
    /* personal | professional | legal | official */ mode?: code;
    _mode?: Element /* When the composition was attested */;
    time?: dateTime;
    _time?: Element /* Who attested the composition */;
    party?: Reference<
      | "Patient"
      | "PractitionerRole"
      | "Organization"
      | "Practitioner"
      | "RelatedPerson"
    >;
  }> /* Kind of composition (LOINC if possible) */;
  type?: CodeableConcept /* Human Readable name/title */;
  title?: string;
  author?: Array<
    Reference<
      | "Patient"
      | "PractitionerRole"
      | "Organization"
      | "Device"
      | "Practitioner"
      | "RelatedPerson"
    >
  >;
  _confidentiality?: Element;
  event?: Array<{
    code?: Array<CodeableConcept> /* The period covered by the documentation */;
    period?: Period;
    detail?: Array<Reference<ResourceType>>;
  }> /* Organization which maintains the composition */;
  custodian?: Reference<"Organization"> /* preliminary | final | amended | entered-in-error */;
  status?: code /* Version-independent identifier for the Composition */;
  identifier?: Identifier;
  relatesTo?: Array<{
    /* replaces | transforms | signs | appends */ code?: code;
    _code?: Element /* Target of the relationship */;
    target?: Identifier | Reference<"Composition">;
  }>;
  _title?: Element /* Who and/or what the composition is about */;
  subject?: Reference<ResourceType> /* As defined by affinity domain */;
  confidentiality?: code;
}
/* This resource provides eligibility and plan details from the processing of an CoverageEligibilityRequest resource. */
interface CoverageEligibilityResponse {
  _created?: Element /* Intended recipient of products and services */;
  patient?: Reference<"Patient"> /* Party responsible for the request */;
  requestor?: Reference<"PractitionerRole" | "Organization" | "Practitioner">;
  insurance?: Array<{
    /* Insurance information */
    coverage?: Reference<"Coverage"> /* Coverage inforce indicator */;
    inforce?: boolean;
    _inforce?: Element /* When the benefits are applicable */;
    benefitPeriod?: Period;
    item?: Array<{
      /* Description of the benefit or services covered */
      description?: string /* Benefit classification */;
      category?: CodeableConcept /* Authorization required flag */;
      authorizationRequired?: boolean;
      modifier?: Array<CodeableConcept>;
      authorizationSupporting?: Array<CodeableConcept> /* Individual or family */;
      unit?: CodeableConcept /* Excluded from the plan */;
      excluded?: boolean /* Short name for the benefit */;
      name?: string;
      _authorizationUrl?: Element;
      _description?: Element /* Billing, service, product, or drug code */;
      productOrService?: CodeableConcept /* Annual or lifetime */;
      term?: CodeableConcept;
      benefit?: Array<{
        /* Benefit classification */
        type?: CodeableConcept /* Benefits allowed */;
        allowed?: unsignedInt | string | Money /* Benefits used */;
        used?: unsignedInt | string | Money;
      }>;
      _name?: Element /* Preauthorization requirements endpoint */;
      authorizationUrl?: uri /* In or out of network */;
      network?: CodeableConcept;
      _authorizationRequired?: Element /* Performing practitioner */;
      provider?: Reference<"PractitionerRole" | "Practitioner">;
      _excluded?: Element;
    }>;
  }> /* Eligibility request reference */;
  request?: Reference<"CoverageEligibilityRequest"> /* Preauthorization reference */;
  preAuthRef?: string;
  _disposition?: Element;
  purpose?: Array<code>;
  _status?: Element /* Response creation date */;
  created?: dateTime /* Estimated date or dates of service */;
  serviced?: date | Period /* queued | complete | error | partial */;
  outcome?: code;
  _outcome?: Element /* Disposition Message */;
  disposition?: string;
  _purpose?: Array<Element> /* Coverage issuer */;
  insurer?: Reference<"Organization"> /* active | cancelled | draft | entered-in-error */;
  status?: code;
  identifier?: Array<Identifier>;
  error?: Array<{
    /* Error code detailing processing issues */ code?: CodeableConcept;
  }> /* Printed form identifier */;
  form?: CodeableConcept;
  _preAuthRef?: Element;
}
/* A reference to a document of any kind for any purpose. Provides metadata about the document so that the document can be discovered and managed. The scope of a document is any seralized object with a mime-type, so includes formal patient centric documents (CDA), cliical notes, scanned paper, and non-patient specific documents like policy text. */
interface DocumentReference {
  /* Human-readable description */ description?: string;
  category?: Array<CodeableConcept> /* When this document reference was created */;
  date?: instant /* preliminary | final | amended | entered-in-error */;
  docStatus?: code;
  content?: Array<{
    /* Where to access the document */
    attachment?: Attachment /* Format/content rules for the document */;
    format?: Coding;
  }>;
  _date?: Element;
  _status?: Element /* Kind of document (LOINC if possible) */;
  type?: CodeableConcept;
  _description?: Element;
  author?: Array<
    Reference<
      | "Patient"
      | "PractitionerRole"
      | "Organization"
      | "Device"
      | "Practitioner"
      | "RelatedPerson"
    >
  > /* Master Version Specific Identifier */;
  masterIdentifier?: Identifier /* Organization which maintains the document */;
  custodian?: Reference<"Organization"> /* current | superseded | entered-in-error */;
  status?: code;
  identifier?: Array<Identifier>;
  relatesTo?: Array<{
    /* replaces | transforms | signs | appends */ code?: code;
    _code?: Element /* Target of the relationship */;
    target?: Reference<"DocumentReference">;
  }> /* Clinical context of document */;
  context?: {
    encounter?: Array<Reference<"EpisodeOfCare" | "Encounter">>;
    event?: Array<CodeableConcept> /* Time of service that is being documented */;
    period?: Period /* Kind of facility where patient was seen */;
    facilityType?: CodeableConcept /* Additional details about where the content was created (e.g. clinical specialty) */;
    practiceSetting?: CodeableConcept /* Patient demographics from source */;
    sourcePatientInfo?: Reference<"Patient">;
    related?: Array<Reference<ResourceType>>;
  };
  securityLabel?: Array<CodeableConcept>;
  _docStatus?: Element /* Who/what is the subject of the document */;
  subject?: Reference<
    "Patient" | "Device" | "Practitioner" | "Group"
  > /* Who/what authenticated the document */;
  authenticator?: Reference<
    "PractitionerRole" | "Organization" | "Practitioner"
  >;
}
/* The EventDefinition resource provides a reusable description of when a particular event can occur. */
interface EventDefinition {
  /* Natural language description of the event definition */
  description?: markdown;
  _usage?: Element /* Date last changed */;
  date?: dateTime;
  endorser?: Array<ContactDetail> /* Name of the publisher (organization or individual) */;
  publisher?: string /* When the event definition was approved by publisher */;
  approvalDate?: date;
  jurisdiction?: Array<CodeableConcept>;
  _publisher?: Element /* Why this event definition is defined */;
  purpose?: markdown;
  _date?: Element /* Name for this event definition (computer friendly) */;
  name?: string;
  _status?: Element;
  _experimental?: Element;
  useContext?: Array<UsageContext> /* Use and/or publishing restrictions */;
  copyright?: markdown;
  _approvalDate?: Element /* For testing purposes, not real usage */;
  experimental?: boolean;
  topic?: Array<CodeableConcept> /* Name for this event definition (human friendly) */;
  title?: string;
  _description?: Element;
  author?: Array<ContactDetail>;
  _purpose?: Element /* Describes the clinical usage of the event definition */;
  usage?: string;
  _lastReviewDate?: Element /* draft | active | retired | unknown */;
  status?: code /* Subordinate title of the event definition */;
  subtitle?: string;
  _name?: Element /* Canonical identifier for this event definition, represented as a URI (globally unique) */;
  url?: uri;
  identifier?: Array<Identifier> /* When the event definition was last reviewed */;
  lastReviewDate?: date;
  editor?: Array<ContactDetail>;
  _subtitle?: Element;
  _copyright?: Element;
  _title?: Element;
  reviewer?: Array<ContactDetail> /* Business version of the event definition */;
  version?: string;
  _version?: Element;
  trigger?: Array<TriggerDefinition>;
  relatedArtifact?: Array<RelatedArtifact>;
  contact?: Array<ContactDetail> /* Type of individual the event definition is focused on */;
  subject?: CodeableConcept | Reference<"Group">;
  _url?: Element /* When the event definition is expected to be used */;
  effectivePeriod?: Period;
}
/* A SubstanceProtein is defined as a single unit of a linear amino acid sequence, or a combination of subunits that are either covalently linked or have a defined invariant stoichiometric relationship. This includes all synthetic, recombinant and purified SubstanceProteins of defined sequence, whether the use is therapeutic or prophylactic. This set of elements will be used to describe albumins, coagulation factors, cytokines, growth factors, peptide/SubstanceProtein hormones, enzymes, toxins, toxoids, recombinant vaccines, and immunomodulators. */
interface SubstanceProtein {
  /* The SubstanceProtein descriptive elements will only be used when a complete or partial amino acid sequence is available or derivable from a nucleic acid sequence */
  sequenceType?: CodeableConcept /* Number of linear sequences of amino acids linked through peptide bonds. The number of subunits constituting the SubstanceProtein shall be described. It is possible that the number of subunits can be variable */;
  numberOfSubunits?: integer;
  _numberOfSubunits?: Element;
  disulfideLinkage?: Array<string>;
  _disulfideLinkage?: Array<Element>;
  subunit?: Array<{
    /* Unique identifier for molecular fragment modification based on the ISO 11238 Substance ID */
    nTerminalModificationId?: Identifier;
    _sequence?: Element;
    _subunit?: Element /* Unique identifier for molecular fragment modification based on the ISO 11238 Substance ID */;
    cTerminalModificationId?: Identifier;
    _cTerminalModification?: Element;
    _nTerminalModification?: Element /* The modification at the C-terminal shall be specified */;
    cTerminalModification?: string /* The name of the fragment modified at the N-terminal of the SubstanceProtein shall be specified */;
    nTerminalModification?: string /* The sequence information shall be provided enumerating the amino acids from N- to C-terminal end using standard single-letter amino acid codes. Uppercase shall be used for L-amino acids and lowercase for D-amino acids. Transcribed SubstanceProteins will always be described using the translated sequence; for synthetic peptide containing amino acids that are not represented with a single letter code an X should be used within the sequence. The modified amino acids will be distinguished by their position in the sequence */;
    sequence?: string /* Length of linear sequences of amino acids contained in the subunit */;
    length?: integer;
    _length?: Element /* Index of primary sequences of amino acids linked through peptide bonds in order of decreasing length. Sequences of the same length will be ordered by molecular weight. Subunits that have identical sequences will be repeated and have sequential subscripts */;
    subunit?: integer /* The sequence information shall be provided enumerating the amino acids from N- to C-terminal end using standard single-letter amino acid codes. Uppercase shall be used for L-amino acids and lowercase for D-amino acids. Transcribed SubstanceProteins will always be described using the translated sequence; for synthetic peptide containing amino acids that are not represented with a single letter code an X should be used within the sequence. The modified amino acids will be distinguished by their position in the sequence */;
    sequenceAttachment?: Attachment;
  }>;
}
/* A TerminologyCapabilities resource documents a set of capabilities (behaviors) of a FHIR Terminology Server that may be used as a statement of actual server functionality or a statement of required or desired server implementation. */
interface TerminologyCapabilities {
  /* Natural language description of the terminology capabilities */
  description?: markdown;
  _kind?: Element /* Date last changed */;
  date?: dateTime /* Name of the publisher (organization or individual) */;
  publisher?: string;
  jurisdiction?: Array<CodeableConcept>;
  _publisher?: Element /* Why this terminology capabilities is defined */;
  purpose?: markdown;
  _lockedDate?: Element;
  _codeSearch?: Element;
  _date?: Element /* Name for this terminology capabilities (computer friendly) */;
  name?: string;
  _status?: Element;
  _experimental?: Element;
  useContext?: Array<UsageContext> /* Use and/or publishing restrictions */;
  copyright?: markdown /* For testing purposes, not real usage */;
  experimental?: boolean /* Information about the [ValueSet/$expand](valueset-operation-expand.html) operation */;
  expansion?: {
    /* Whether the server can return nested value sets */
    hierarchical?: boolean;
    _incomplete?: Element;
    _paging?: Element /* Documentation about text searching works */;
    textFilter?: markdown /* Allow request for incomplete expansions? */;
    incomplete?: boolean /* Whether the server supports paging on expansion */;
    paging?: boolean;
    _textFilter?: Element;
    _hierarchical?: Element;
    parameter?: Array<{
      /* Expansion Parameter name */ name?: code;
      _name?: Element /* Description of support for parameter */;
      documentation?: string;
      _documentation?: Element;
    }>;
  } /* Name for this terminology capabilities (human friendly) */;
  title?: string;
  _description?: Element;
  _purpose?: Element /* draft | active | retired | unknown */;
  status?: code /* Information about the [ValueSet/$validate-code](valueset-operation-validate-code.html) operation */;
  validateCode?: {
    /* Whether translations are validated */ translations?: boolean;
    _translations?: Element;
  } /* instance | capability | requirements */;
  kind?: code;
  _name?: Element /* Information about the [ConceptMap/$translate](conceptmap-operation-translate.html) operation */;
  translation?: {
    /* Whether the client must identify the map */ needsMap?: boolean;
    _needsMap?: Element;
  } /* Canonical identifier for this terminology capabilities, represented as a URI (globally unique) */;
  url?: uri;
  _copyright?: Element;
  _title?: Element;
  codeSystem?: Array<{
    /* URI for the Code System */ uri?: canonical;
    _uri?: Element;
    version?: Array<{
      _code?: Element /* If this is the default version for this code system */;
      isDefault?: boolean;
      property?: Array<code>;
      _language?: Array<Element>;
      filter?: Array<{
        /* Code of the property supported */ code?: code;
        _code?: Element;
        op?: Array<code>;
        _op?: Array<Element>;
      }> /* If compositional grammar is supported */;
      compositional?: boolean;
      language?: Array<code>;
      _isDefault?: Element /* Version identifier for this version */;
      code?: string;
      _compositional?: Element;
      _property?: Array<Element>;
    }> /* Whether subsumption is supported */;
    subsumption?: boolean;
    _subsumption?: Element;
  }> /* Software that is covered by this terminology capability statement */;
  software?: {
    /* A name the software is known by */ name?: string;
    _name?: Element /* Version covered by this statement */;
    version?: string;
    _version?: Element;
  } /* Business version of the terminology capabilities */;
  version?: string;
  _version?: Element;
  contact?: Array<ContactDetail> /* If this describes a specific instance */;
  implementation?: {
    /* Describes this specific instance */ description?: string;
    _description?: Element /* Base URL for the implementation */;
    url?: url;
    _url?: Element;
  };
  _url?: Element /* explicit | all */;
  codeSearch?: code /* Whether lockedDate is supported */;
  lockedDate?: boolean /* Information about the [ConceptMap/$closure](conceptmap-operation-closure.html) operation */;
  closure?: {
    /* If cross-system closure is supported */ translation?: boolean;
    _translation?: Element;
  };
}
/* An interaction between a patient and healthcare provider(s) for the purpose of providing healthcare service(s) or assessing the health status of a patient. */
interface Encounter {
  appointment?: Array<Reference<"Appointment">>;
  diagnosis?: Array<{
    /* The diagnosis or procedure relevant to the encounter */
    condition?: Reference<
      "Procedure" | "Condition"
    > /* Role that this diagnosis has within the encounter (e.g. admission, billing, discharge …) */;
    use?: CodeableConcept /* Ranking of the diagnosis (for each role type) */;
    rank?: positiveInt;
    _rank?: Element;
  }>;
  _status?: Element /* The organization (facility) responsible for this encounter */;
  serviceProvider?: Reference<"Organization">;
  episodeOfCare?: Array<Reference<"EpisodeOfCare">>;
  reasonCode?: Array<CodeableConcept>;
  type?: Array<CodeableConcept>;
  participant?: Array<{
    type?: Array<CodeableConcept> /* Period of time during the encounter that the participant participated */;
    period?: Period /* Persons involved in the encounter other than the patient */;
    individual?: Reference<
      "PractitionerRole" | "Practitioner" | "RelatedPerson"
    >;
  }> /* Specific type of service */;
  serviceType?: CodeableConcept;
  account?: Array<Reference<"Account">>;
  classHistory?: Array<{
    /* inpatient | outpatient | ambulatory | emergency + */
    class?: Coding /* The time that the episode was in the specified class */;
    period?: Period;
  }> /* Indicates the urgency of the encounter */;
  priority?: CodeableConcept /* planned | arrived | triaged | in-progress | onleave | finished | cancelled + */;
  status?: code /* Classification of patient encounter */;
  class?: Coding /* Quantity of time the encounter lasted (less time absent) */;
  length?: Duration;
  identifier?: Array<Identifier> /* Details about the admission to a healthcare service */;
  hospitalization?: {
    /* Category or kind of location after discharge */
    dischargeDisposition?: CodeableConcept /* Pre-admission identifier */;
    preAdmissionIdentifier?: Identifier;
    specialArrangement?: Array<CodeableConcept>;
    dietPreference?: Array<CodeableConcept> /* From where patient was admitted (physician referral, transfer) */;
    admitSource?: CodeableConcept;
    specialCourtesy?: Array<CodeableConcept> /* The type of hospital re-admission that has occurred (if any). If the value is absent, then this is not identified as a readmission */;
    reAdmission?: CodeableConcept /* The location/organization from which the patient came before admission */;
    origin?: Reference<
      "Organization" | "Location"
    > /* Location/organization to which the patient is discharged */;
    destination?: Reference<"Organization" | "Location">;
  } /* The start and end time of the encounter */;
  period?: Period;
  basedOn?: Array<
    Reference<"ServiceRequest">
  > /* Another Encounter this encounter is part of */;
  partOf?: Reference<"Encounter">;
  location?: Array<{
    /* Location the encounter takes place */
    location?: Reference<"Location"> /* planned | active | reserved | completed */;
    status?: code;
    _status?: Element /* The physical type of the location (usually the level in the location hierachy - bed room ward etc.) */;
    physicalType?: CodeableConcept /* Time period during which the patient was present at the location */;
    period?: Period;
  }> /* The patient or group present at the encounter */;
  subject?: Reference<"Patient" | "Group">;
  statusHistory?: Array<{
    /* planned | arrived | triaged | in-progress | onleave | finished | cancelled + */
    status?: code;
    _status?: Element /* The time that the episode was in the specified status */;
    period?: Period;
  }>;
  reasonReference?: Array<
    Reference<
      "Observation" | "Procedure" | "ImmunizationRecommendation" | "Condition"
    >
  >;
}
/* A set of rules of how a particular interoperability or standards problem is solved - typically through the use of FHIR resources. This resource is used to gather all the parts of an implementation guide into a logical whole and to publish a computable definition of all the parts. */
interface ImplementationGuide {
  /* Natural language description of the implementation guide */
  description?: markdown /* Information needed to build the IG */;
  definition?: {
    grouping?: Array<{
      /* Descriptive name for the package */ name?: string;
      _name?: Element /* Human readable text describing the package */;
      description?: string;
      _description?: Element;
    }>;
    resource?: Array<{
      /* Reason why included in guide */ description?: string;
      fhirVersion?: Array<code> /* Grouping this is part of */;
      groupingId?: id /* Human Name for the resource */;
      name?: string;
      _fhirVersion?: Array<Element>;
      _description?: Element;
      _groupingId?: Element /* Location of the resource */;
      reference?: Reference<ResourceType>;
      _name?: Element /* Is an example/What is this an example of? */;
      example?: canonical | boolean;
    }>;
    page?: ImplementationGuide;
    parameter?: Array<{
      /* apply | path-resource | path-pages | path-tx-cache | expansion-parameter | rule-broken-links | generate-xml | generate-json | generate-turtle | html-template */
      code?: code;
      _code?: Element /* Value for named type */;
      value?: string;
      _value?: Element;
    }>;
    template?: Array<{
      /* Type of template specified */ code?: code;
      _code?: Element /* The source location for the template */;
      source?: string;
      _source?: Element /* The scope in which the template applies */;
      scope?: string;
      _scope?: Element;
    }>;
  };
  _license?: Element /* Date last changed */;
  date?: dateTime /* Name of the publisher (organization or individual) */;
  publisher?: string;
  fhirVersion?: Array<code> /* SPDX license code for this IG (or not-open-source) */;
  license?: code;
  jurisdiction?: Array<CodeableConcept>;
  _publisher?: Element;
  global?: Array<{
    /* Type this profile applies to */ type?: code;
    _type?: Element /* Profile that all resources must conform to */;
    profile?: canonical;
    _profile?: Element;
  }>;
  dependsOn?: Array<{
    /* Identity of the IG that this depends on */ uri?: canonical;
    _uri?: Element /* NPM Package name for IG this depends on */;
    packageId?: id;
    _packageId?: Element /* Version of the IG */;
    version?: string;
    _version?: Element;
  }>;
  _date?: Element /* Name for this implementation guide (computer friendly) */;
  name?: string;
  _status?: Element;
  _fhirVersion?: Array<Element>;
  _experimental?: Element;
  useContext?: Array<UsageContext> /* Use and/or publishing restrictions */;
  copyright?: markdown /* For testing purposes, not real usage */;
  experimental?: boolean /* Information about an assembled IG */;
  manifest?: {
    /* Location of rendered implementation guide */ rendering?: url;
    _rendering?: Element;
    resource?: Array<{
      /* Location of the resource */
      reference?: Reference<ResourceType> /* Is an example/What is this an example of? */;
      example?: canonical | boolean /* Relative path for page in IG */;
      relativePath?: url;
      _relativePath?: Element;
    }>;
    page?: Array<{
      /* HTML page name */ name?: string;
      _name?: Element /* Title of the page, for references */;
      title?: string;
      _title?: Element;
      anchor?: Array<string>;
      _anchor?: Array<Element>;
    }>;
    image?: Array<string>;
    _image?: Array<Element>;
    other?: Array<string>;
    _other?: Array<Element>;
  } /* Name for this implementation guide (human friendly) */;
  title?: string;
  _description?: Element /* draft | active | retired | unknown */;
  status?: code;
  _name?: Element /* Canonical identifier for this implementation guide, represented as a URI (globally unique) */;
  url?: uri;
  _copyright?: Element;
  _packageId?: Element;
  _title?: Element /* Business version of the implementation guide */;
  version?: string;
  _version?: Element /* NPM Package name for IG */;
  packageId?: id;
  contact?: Array<ContactDetail>;
  _url?: Element;
}
/* The EvidenceVariable resource describes a "PICO" element that knowledge (evidence, assertion, recommendation) is about. */
interface EvidenceVariable {
  /* Natural language description of the evidence variable */
  description?: markdown /* Date last changed */;
  date?: dateTime;
  endorser?: Array<ContactDetail> /* Name of the publisher (organization or individual) */;
  publisher?: string /* When the evidence variable was approved by publisher */;
  approvalDate?: date;
  jurisdiction?: Array<CodeableConcept>;
  _publisher?: Element;
  _date?: Element /* Name for this evidence variable (computer friendly) */;
  name?: string;
  _type?: Element;
  _status?: Element;
  useContext?: Array<UsageContext> /* Use and/or publishing restrictions */;
  copyright?: markdown;
  _approvalDate?: Element /* dichotomous | continuous | descriptive */;
  type?: code;
  topic?: Array<CodeableConcept> /* Name for this evidence variable (human friendly) */;
  title?: string;
  _description?: Element;
  note?: Array<Annotation>;
  author?: Array<ContactDetail>;
  characteristic?: Array<{
    /* Natural language description of the characteristic */
    description?: string /* What code or expression defines members? */;
    definition?:
      | DataRequirement
      | TriggerDefinition
      | Expression
      | canonical
      | CodeableConcept
      | Reference<"Group"> /* Whether the characteristic includes or excludes members */;
    exclude?: boolean /* mean | median | mean-of-mean | mean-of-median | median-of-mean | median-of-median */;
    groupMeasure?: code /* Observation time from study start */;
    timeFromStart?: Duration;
    _groupMeasure?: Element;
    _description?: Element /* What time period do participants cover */;
    participantEffective?: dateTime | Period | Timing | Duration;
    _exclude?: Element;
    usageContext?: Array<UsageContext>;
  }>;
  _lastReviewDate?: Element /* draft | active | retired | unknown */;
  status?: code /* Subordinate title of the EvidenceVariable */;
  subtitle?: string;
  _name?: Element /* Canonical identifier for this evidence variable, represented as a URI (globally unique) */;
  url?: uri;
  identifier?: Array<Identifier> /* When the evidence variable was last reviewed */;
  lastReviewDate?: date;
  editor?: Array<ContactDetail>;
  _subtitle?: Element;
  _copyright?: Element;
  _shortTitle?: Element;
  _title?: Element;
  reviewer?: Array<ContactDetail> /* Title for use in informal contexts */;
  shortTitle?: string /* Business version of the evidence variable */;
  version?: string;
  _version?: Element;
  relatedArtifact?: Array<RelatedArtifact>;
  contact?: Array<ContactDetail>;
  _url?: Element /* When the evidence variable is expected to be used */;
  effectivePeriod?: Period;
}
/* Set of definitional characteristics for a kind of observation or measurement produced or consumed by an orderable health care service. */
interface ObservationDefinition {
  /* Characteristics of quantitative results */
  quantitativeDetails?: {
    /* Customary unit for quantitative results */
    customaryUnit?: CodeableConcept /* SI unit for quantitative results */;
    unit?: CodeableConcept /* SI to Customary unit conversion factor */;
    conversionFactor?: decimal;
    _conversionFactor?: Element /* Decimal precision of observation quantitative results */;
    decimalPrecision?: integer;
    _decimalPrecision?: Element;
  };
  category?: Array<CodeableConcept> /* Method used to produce the observation */;
  method?: CodeableConcept;
  _multipleResultsAllowed?: Element /* Value set of valid coded values for the observations conforming to this ObservationDefinition */;
  validCodedValueSet?: Reference<"ValueSet">;
  _preferredReportName?: Element;
  qualifiedInterval?: Array<{
    /* reference | critical | absolute */
    category?: code /* Applicable age range, if relevant */;
    age?: Range;
    appliesTo?: Array<CodeableConcept>;
    _gender?: Element;
    _condition?: Element /* Condition associated with the reference range */;
    condition?: string /* Range context qualifier */;
    context?: CodeableConcept /* Applicable gestational age range, if relevant */;
    gestationalAge?: Range /* male | female | other | unknown */;
    gender?: code;
    _category?: Element /* The interval itself, for continuous or ordinal observations */;
    range?: Range;
  }> /* Value set of abnormal coded values for the observations conforming to this ObservationDefinition */;
  abnormalCodedValueSet?: Reference<"ValueSet"> /* Type of observation (code / type) */;
  code?: CodeableConcept;
  identifier?: Array<Identifier>;
  permittedDataType?: Array<code> /* Multiple results allowed */;
  multipleResultsAllowed?: boolean /* Value set of normal coded values for the observations conforming to this ObservationDefinition */;
  normalCodedValueSet?: Reference<"ValueSet">;
  _permittedDataType?: Array<Element> /* Preferred report name */;
  preferredReportName?: string /* Value set of critical coded values for the observations conforming to this ObservationDefinition */;
  criticalCodedValueSet?: Reference<"ValueSet">;
}
/* The findings and interpretation of diagnostic  tests performed on patients, groups of patients, devices, and locations, and/or specimens derived from these. The report includes clinical context such as requesting and provider information, and some mix of atomic results, images, textual and coded interpretations, and formatted representation of diagnostic reports. */
interface DiagnosticReport {
  category?: Array<CodeableConcept>;
  conclusionCode?: Array<CodeableConcept>;
  _issued?: Element /* Clinical conclusion (interpretation) of test results */;
  conclusion?: string /* Health care event when test ordered */;
  encounter?: Reference<"Encounter">;
  specimen?: Array<Reference<"Specimen">>;
  _status?: Element;
  resultsInterpreter?: Array<
    Reference<"CareTeam" | "PractitionerRole" | "Organization" | "Practitioner">
  > /* registered | partial | preliminary | final + */;
  status?: code;
  result?: Array<
    Reference<"Observation">
  > /* Clinically relevant time/time-period for report */;
  effective?: dateTime | Period /* Name/Code for this diagnostic report */;
  code?: CodeableConcept;
  identifier?: Array<Identifier> /* DateTime this version was made */;
  issued?: instant;
  presentedForm?: Array<Attachment>;
  basedOn?: Array<
    Reference<
      | "NutritionOrder"
      | "ServiceRequest"
      | "CarePlan"
      | "ImmunizationRecommendation"
      | "MedicationRequest"
    >
  >;
  imagingStudy?: Array<Reference<"ImagingStudy">>;
  media?: Array<{
    /* Comment about the image (e.g. explanation) */ comment?: string;
    _comment?: Element /* Reference to the image source */;
    link?: Reference<"Media">;
  }> /* The subject of the report - usually, but not always, the patient */;
  subject?: Reference<"Patient" | "Device" | "Location" | "Group">;
  _conclusion?: Element;
  performer?: Array<
    Reference<"CareTeam" | "PractitionerRole" | "Organization" | "Practitioner">
  >;
}
/* Example of workflow instance. */
interface ExampleScenario {
  /* Date last changed */
  date?: dateTime /* Name of the publisher (organization or individual) */;
  publisher?: string;
  instance?: Array<{
    /* Human-friendly description of the resource instance */
    description?: markdown;
    containedInstance?: Array<ExampleScenario> /* A short name for the resource instance */;
    name?: string;
    _resourceId?: Element /* The type of the resource */;
    resourceType?: code;
    _resourceType?: Element;
    _description?: Element /* The id of the resource for referencing */;
    resourceId?: string;
    _name?: Element;
    version?: Array<{
      /* The identifier of a specific version of a resource */
      versionId?: string;
      _versionId?: Element /* The description of the resource version */;
      description?: markdown;
      _description?: Element;
    }>;
  }>;
  jurisdiction?: Array<CodeableConcept>;
  _publisher?: Element /* The purpose of the example, e.g. to illustrate a scenario */;
  purpose?: markdown;
  _date?: Element /* Name for this example scenario (computer friendly) */;
  name?: string;
  process?: Array<ExampleScenario>;
  _status?: Element;
  _experimental?: Element;
  useContext?: Array<UsageContext> /* Use and/or publishing restrictions */;
  copyright?: markdown;
  _workflow?: Array<Element> /* For testing purposes, not real usage */;
  experimental?: boolean;
  _purpose?: Element;
  workflow?: Array<canonical> /* draft | active | retired | unknown */;
  status?: code;
  _name?: Element /* Canonical identifier for this example scenario, represented as a URI (globally unique) */;
  url?: uri;
  identifier?: Array<Identifier>;
  _copyright?: Element /* Business version of the example scenario */;
  version?: string;
  _version?: Element;
  contact?: Array<ContactDetail>;
  _url?: Element;
  actor?: Array<{
    /* ID or acronym of the actor */ actorId?: string;
    _actorId?: Element /* person | entity */;
    type?: code;
    _type?: Element /* The name of the actor as shown in the page */;
    name?: string;
    _name?: Element /* The description of the actor */;
    description?: markdown;
    _description?: Element;
  }>;
}
/* The ResearchDefinition resource describes the conditional state (population and any exposures being compared within the population) and outcome (if specified) that the knowledge (evidence, assertion, recommendation) is about. */
interface ResearchDefinition {
  /* Natural language description of the research definition */
  description?: markdown;
  _usage?: Element /* What alternative exposure state? */;
  exposureAlternative?: Reference<"ResearchElementDefinition"> /* Date last changed */;
  date?: dateTime;
  endorser?: Array<ContactDetail> /* Name of the publisher (organization or individual) */;
  publisher?: string /* When the research definition was approved by publisher */;
  approvalDate?: date;
  jurisdiction?: Array<CodeableConcept>;
  _publisher?: Element /* Why this research definition is defined */;
  purpose?: markdown;
  _date?: Element /* Name for this research definition (computer friendly) */;
  name?: string;
  _status?: Element;
  _experimental?: Element;
  useContext?: Array<UsageContext> /* Use and/or publishing restrictions */;
  copyright?: markdown;
  _approvalDate?: Element /* For testing purposes, not real usage */;
  experimental?: boolean /* What outcome? */;
  outcome?: Reference<"ResearchElementDefinition">;
  topic?: Array<CodeableConcept> /* Name for this research definition (human friendly) */;
  title?: string;
  _description?: Element;
  library?: Array<canonical>;
  author?: Array<ContactDetail>;
  _purpose?: Element /* Describes the clinical usage of the ResearchDefinition */;
  usage?: string;
  _lastReviewDate?: Element /* draft | active | retired | unknown */;
  status?: code /* Subordinate title of the ResearchDefinition */;
  subtitle?: string /* What population? */;
  population?: Reference<"ResearchElementDefinition">;
  comment?: Array<string>;
  _name?: Element /* Canonical identifier for this research definition, represented as a URI (globally unique) */;
  url?: uri;
  identifier?: Array<Identifier> /* When the research definition was last reviewed */;
  lastReviewDate?: date;
  editor?: Array<ContactDetail>;
  _subtitle?: Element;
  _library?: Array<Element>;
  _copyright?: Element;
  _shortTitle?: Element;
  _title?: Element;
  reviewer?: Array<ContactDetail> /* Title for use in informal contexts */;
  shortTitle?: string /* What exposure? */;
  exposure?: Reference<"ResearchElementDefinition"> /* Business version of the research definition */;
  version?: string;
  _version?: Element;
  relatedArtifact?: Array<RelatedArtifact>;
  contact?: Array<ContactDetail>;
  _comment?: Array<Element> /* E.g. Patient, Practitioner, RelatedPerson, Organization, Location, Device */;
  subject?: CodeableConcept | Reference<"Group">;
  _url?: Element /* When the research definition is expected to be used */;
  effectivePeriod?: Period;
}
/* This resource is a non-persisted resource used to pass information into and back from an [operation](operations.html). It has no other use, and there is no RESTful endpoint associated with it. */
interface Parameters {
  parameter?: Array<Parameters>;
}
/* A search parameter that defines a named search item that can be used to search/filter on a resource. */
interface SearchParameter {
  _base?: Array<Element> /* Natural language description of the search parameter */;
  description?: markdown;
  _code?: Element /* Date last changed */;
  date?: dateTime /* FHIRPath expression that extracts the values */;
  expression?: string;
  modifier?: Array<code> /* Name of the publisher (organization or individual) */;
  publisher?: string /* Allow multiple parameters (and) */;
  multipleAnd?: boolean;
  _comparator?: Array<Element>;
  jurisdiction?: Array<CodeableConcept>;
  _publisher?: Element;
  _chain?: Array<Element> /* Original definition for the search parameter */;
  derivedFrom?: canonical /* Why this search parameter is defined */;
  purpose?: markdown;
  _date?: Element /* Allow multiple values per parameter (or) */;
  multipleOr?: boolean;
  _derivedFrom?: Element /* Name for this search parameter (computer friendly) */;
  name?: string;
  _type?: Element;
  _status?: Element;
  _experimental?: Element;
  useContext?: Array<UsageContext> /* XPath that extracts the values */;
  xpath?: string;
  _xpathUsage?: Element /* normal | phonetic | nearby | distance | other */;
  xpathUsage?: code /* number | date | string | token | reference | composite | quantity | uri | special */;
  type?: code /* For testing purposes, not real usage */;
  experimental?: boolean;
  component?: Array<{
    /* Defines how the part works */ definition?: canonical;
    _definition?: Element /* Subexpression relative to main expression */;
    expression?: string;
    _expression?: Element;
  }>;
  _expression?: Element;
  _description?: Element;
  _purpose?: Element /* draft | active | retired | unknown */;
  status?: code;
  _target?: Array<Element>;
  _name?: Element;
  chain?: Array<string>;
  _multipleOr?: Element /* Canonical identifier for this search parameter, represented as a URI (globally unique) */;
  url?: uri /* Code used in URL */;
  code?: code;
  comparator?: Array<code>;
  target?: Array<code>;
  base?: Array<code> /* Business version of the search parameter */;
  version?: string;
  _version?: Element;
  _modifier?: Array<Element>;
  contact?: Array<ContactDetail>;
  _xpath?: Element;
  _url?: Element;
  _multipleAnd?: Element;
}
/* The interactions of the medicinal product with other medicinal products, or other forms of interactions. */
interface MedicinalProductInteraction {
  subject?: Array<
    Reference<"Medication" | "Substance" | "MedicinalProduct">
  > /* The interaction described */;
  description?: string;
  _description?: Element;
  interactant?: Array<{
    /* The specific medication, food or laboratory test that interacts */
    item?:
      | CodeableConcept
      | Reference<
          | "Medication"
          | "Substance"
          | "MedicinalProduct"
          | "ObservationDefinition"
        >;
  }> /* The type of the interaction e.g. drug-drug interaction, drug-food interaction, drug-lab test interaction */;
  type?: CodeableConcept /* The effect of the interaction, for example "reduced gastric absorption of primary medication" */;
  effect?: CodeableConcept /* The incidence of the interaction, e.g. theoretical, observed */;
  incidence?: CodeableConcept /* Actions for managing the interaction */;
  management?: CodeableConcept;
}
/* The CodeSystem resource is used to declare the existence of and describe a code system or code system supplement and its key properties, and optionally define a part or all of its content. */
interface CodeSystem {
  _supplements?: Element /* Natural language description of the code system */;
  description?: markdown;
  _content?: Element /* Date last changed */;
  date?: dateTime;
  _versionNeeded?: Element;
  _count?: Element /* If definitions are not stable */;
  versionNeeded?: boolean /* Name of the publisher (organization or individual) */;
  publisher?: string;
  _hierarchyMeaning?: Element;
  jurisdiction?: Array<CodeableConcept>;
  _publisher?: Element /* Why this code system is defined */;
  purpose?: markdown /* not-present | example | fragment | complete | supplement */;
  content?: code;
  _date?: Element;
  property?: Array<{
    /* Identifies the property on the concepts, and when referred to in operations */
    code?: code;
    _code?: Element /* Formal identifier for the property */;
    uri?: uri;
    _uri?: Element /* Why the property is defined, and/or what it conveys */;
    description?: string;
    _description?: Element /* code | Coding | string | integer | boolean | dateTime | decimal */;
    type?: code;
    _type?: Element;
  }> /* Name for this code system (computer friendly) */;
  name?: string;
  _status?: Element;
  _experimental?: Element;
  useContext?: Array<UsageContext> /* Use and/or publishing restrictions */;
  copyright?: markdown;
  _caseSensitive?: Element /* For testing purposes, not real usage */;
  experimental?: boolean /* Name for this code system (human friendly) */;
  title?: string;
  _description?: Element;
  _purpose?: Element;
  filter?: Array<{
    /* Code that identifies the filter */ code?: code;
    _code?: Element /* How or why the filter is used */;
    description?: string;
    _description?: Element;
    operator?: Array<code>;
    _operator?: Array<Element> /* What to use for the value */;
    value?: string;
    _value?: Element;
  }> /* Canonical URL of Code System this adds designations and properties to */;
  supplements?: canonical /* If code system defines a compositional grammar */;
  compositional?: boolean /* draft | active | retired | unknown */;
  status?: code /* grouped-by | is-a | part-of | classified-with */;
  hierarchyMeaning?: code /* Canonical reference to the value set with entire code system */;
  valueSet?: canonical;
  _name?: Element /* Total concepts in the code system */;
  count?: unsignedInt /* Canonical identifier for this code system, represented as a URI (globally unique) (Coding.system) */;
  url?: uri;
  identifier?: Array<Identifier>;
  _valueSet?: Element;
  concept?: Array<CodeSystem> /* If code comparison is case sensitive */;
  caseSensitive?: boolean;
  _copyright?: Element;
  _title?: Element /* Business version of the code system (Coding.version) */;
  version?: string;
  _version?: Element;
  _compositional?: Element;
  contact?: Array<ContactDetail>;
  _url?: Element;
}
/* Defines the characteristics of a message that can be shared between systems, including the type of event that initiates the message, the content to be transmitted and what response(s), if any, are permitted. */
interface MessageDefinition {
  _base?: Element /* Natural language description of the message definition */;
  description?: markdown /* consequence | currency | notification */;
  category?: code /* Date last changed */;
  date?: dateTime;
  _graph?: Array<Element> /* Name of the publisher (organization or individual) */;
  publisher?: string;
  parent?: Array<canonical>;
  jurisdiction?: Array<CodeableConcept>;
  _publisher?: Element /* Why this message definition is defined */;
  purpose?: markdown;
  _date?: Element;
  _replaces?: Array<Element> /* Name for this message definition (computer friendly) */;
  name?: string;
  _status?: Element;
  _parent?: Array<Element>;
  _experimental?: Element;
  useContext?: Array<UsageContext> /* Use and/or publishing restrictions */;
  copyright?: markdown /* For testing purposes, not real usage */;
  experimental?: boolean /* Name for this message definition (human friendly) */;
  title?: string;
  _description?: Element;
  _purpose?: Element /* Event code  or link to the EventDefinition */;
  event?: Coding | uri /* draft | active | retired | unknown */;
  status?: code;
  allowedResponse?: Array<{
    /* Reference to allowed message definition response */ message?: canonical;
    _message?: Element /* When should this response be used */;
    situation?: markdown;
    _situation?: Element;
  }>;
  graph?: Array<canonical>;
  _name?: Element;
  _responseRequired?: Element /* Business Identifier for a given MessageDefinition */;
  url?: uri;
  identifier?: Array<Identifier>;
  focus?: Array<{
    /* Type of resource */ code?: code;
    _code?: Element /* Profile that must be adhered to by focus */;
    profile?: canonical;
    _profile?: Element /* Minimum number of focuses of this type */;
    min?: unsignedInt;
    _min?: Element /* Maximum number of focuses of this type */;
    max?: string;
    _max?: Element;
  }>;
  _copyright?: Element;
  _title?: Element;
  replaces?: Array<canonical> /* always | on-error | never | on-success */;
  responseRequired?: code;
  _category?: Element /* Definition this one is based on */;
  base?: canonical /* Business version of the message definition */;
  version?: string;
  _version?: Element;
  contact?: Array<ContactDetail>;
  _url?: Element;
}
/* A request to supply a diet, formula feeding (enteral) or oral nutritional supplement to a patient/resident. */
interface NutritionOrder {
  /* The person who requires the diet, formula or nutritional supplement */
  patient?: Reference<"Patient"> /* Oral diet components */;
  oralDiet?: {
    type?: Array<CodeableConcept>;
    schedule?: Array<Timing>;
    nutrient?: Array<{
      /* Type of nutrient that is being modified */
      modifier?: CodeableConcept /* Quantity of the specified nutrient */;
      amount?: Quantity;
    }>;
    texture?: Array<{
      /* Code to indicate how to alter the texture of the foods, e.g. pureed */
      modifier?: CodeableConcept /* Concepts that are used to identify an entity that is ingested for nutritional purposes */;
      foodType?: CodeableConcept;
    }>;
    fluidConsistencyType?: Array<CodeableConcept> /* Instructions or additional information about the oral diet */;
    instruction?: string;
    _instruction?: Element;
  };
  instantiatesCanonical?: Array<canonical>;
  instantiatesUri?: Array<uri>;
  instantiates?: Array<uri> /* The encounter associated with this nutrition order */;
  encounter?: Reference<"Encounter">;
  _dateTime?: Element;
  _instantiates?: Array<Element>;
  _status?: Element;
  note?: Array<Annotation> /* Date and time the nutrition order was requested */;
  dateTime?: dateTime /* Enteral formula components */;
  enteralFormula?: {
    _administrationInstruction?: Element /* Type of modular component to add to the feeding */;
    additiveType?: CodeableConcept;
    _baseFormulaProductName?: Element /* Upper limit on formula volume per unit of time */;
    maxVolumeToDeliver?: Quantity /* Type of enteral or infant formula */;
    baseFormulaType?: CodeableConcept /* How the formula should enter the patient's gastrointestinal tract */;
    routeofAdministration?: CodeableConcept /* Product or brand name of the modular additive */;
    additiveProductName?: string /* Amount of energy per specified volume that is required */;
    caloricDensity?: Quantity /* Formula feeding instructions expressed as text */;
    administrationInstruction?: string;
    administration?: Array<{
      /* Scheduled frequency of enteral feeding */
      schedule?: Timing /* The volume of formula to provide */;
      quantity?: Quantity /* Speed with which the formula is provided per period of time */;
      rate?: Ratio | Quantity;
    }>;
    _additiveProductName?: Element /* Product or brand name of the enteral or infant formula */;
    baseFormulaProductName?: string;
  };
  _intent?: Element;
  foodPreferenceModifier?: Array<CodeableConcept> /* draft | active | on-hold | revoked | completed | entered-in-error | unknown */;
  status?: code;
  excludeFoodModifier?: Array<CodeableConcept>;
  identifier?: Array<Identifier> /* proposal | plan | directive | order | original-order | reflex-order | filler-order | instance-order | option */;
  intent?: code /* Who ordered the diet, formula or nutritional supplement */;
  orderer?: Reference<"PractitionerRole" | "Practitioner">;
  supplement?: Array<{
    /* Type of supplement product requested */
    type?: CodeableConcept /* Product or brand name of the nutritional supplement */;
    productName?: string;
    _productName?: Element;
    schedule?: Array<Timing> /* Amount of the nutritional supplement */;
    quantity?: Quantity /* Instructions or additional information about the oral supplement */;
    instruction?: string;
    _instruction?: Element;
  }>;
  _instantiatesCanonical?: Array<Element>;
  allergyIntolerance?: Array<Reference<"AllergyIntolerance">>;
  _instantiatesUri?: Array<Element>;
}
/* Describes validation requirements, source(s), status and dates for one or more elements. */
interface VerificationResult {
  /* fatal | warn | rec-only | none */
  failureAction?: CodeableConcept /* nothing | primary | multiple */;
  validationType?: CodeableConcept;
  targetLocation?: Array<string>;
  _targetLocation?: Array<Element>;
  validator?: Array<{
    /* Reference to the organization validating information */
    organization?: Reference<"Organization"> /* A digital identity certificate associated with the validator */;
    identityCertificate?: string;
    _identityCertificate?: Element /* Validator signature */;
    attestationSignature?: Signature;
  }> /* none | initial | periodic */;
  need?: CodeableConcept /* Frequency of revalidation */;
  frequency?: Timing;
  _status?: Element /* The date when target is next validated, if appropriate */;
  nextScheduled?: date;
  primarySource?: Array<{
    /* Reference to the primary source */
    who?: Reference<"PractitionerRole" | "Organization" | "Practitioner">;
    type?: Array<CodeableConcept>;
    communicationMethod?: Array<CodeableConcept> /* successful | failed | unknown */;
    validationStatus?: CodeableConcept /* When the target was validated against the primary source */;
    validationDate?: dateTime;
    _validationDate?: Element /* yes | no | undetermined */;
    canPushUpdates?: CodeableConcept;
    pushTypeAvailable?: Array<CodeableConcept>;
  }> /* Information about the entity attesting to information */;
  attestation?: {
    /* The individual or organization attesting to information */
    who?: Reference<
      "PractitionerRole" | "Organization" | "Practitioner"
    > /* The date the information was attested to */;
    date?: date /* A digital identity certificate associated with the proxy entity submitting attested information on behalf of the attestation source */;
    proxyIdentityCertificate?: string /* Attester signature */;
    sourceSignature?: Signature /* When the who is asserting on behalf of another (organization or individual) */;
    onBehalfOf?: Reference<
      "PractitionerRole" | "Organization" | "Practitioner"
    >;
    _proxyIdentityCertificate?: Element;
    _date?: Element /* Proxy signature */;
    proxySignature?: Signature /* The method by which attested information was submitted/retrieved */;
    communicationMethod?: CodeableConcept;
    _sourceIdentityCertificate?: Element /* A digital identity certificate associated with the attestation source */;
    sourceIdentityCertificate?: string;
  } /* attested | validated | in-process | req-revalid | val-fail | reval-fail */;
  status?: code;
  validationProcess?: Array<CodeableConcept> /* When the validation status was updated */;
  statusDate?: dateTime;
  target?: Array<Reference<ResourceType>>;
  _lastPerformed?: Element;
  _nextScheduled?: Element /* The date/time validation was last completed (including failed validations) */;
  lastPerformed?: dateTime;
  _statusDate?: Element;
}
/* Describes the event of a patient consuming or otherwise being administered a medication.  This may be as simple as swallowing a tablet or it may be a long running infusion.  Related resources tie this event to the authorizing prescription, and the specific encounter between patient and health care practitioner. */
interface MedicationAdministration {
  /* Type of medication usage */
  category?: CodeableConcept /* Request administration performed against */;
  request?: Reference<"MedicationRequest">;
  eventHistory?: Array<
    Reference<"Provenance">
  > /* Details of how medication was taken */;
  dosage?: {
    /* Free text dosage instructions e.g. SIG */ text?: string;
    _text?: Element /* Body site administered to */;
    site?: CodeableConcept /* Path of substance into body */;
    route?: CodeableConcept /* How drug was administered */;
    method?: CodeableConcept /* Amount of medication per dose */;
    dose?: Quantity /* Dose quantity per unit of time */;
    rate?: Ratio | Quantity;
  };
  instantiates?: Array<uri>;
  _instantiates?: Array<Element>;
  _status?: Element;
  reasonCode?: Array<CodeableConcept>;
  statusReason?: Array<CodeableConcept>;
  note?: Array<Annotation>;
  supportingInformation?: Array<
    Reference<ResourceType>
  > /* in-progress | not-done | on-hold | completed | entered-in-error | stopped | unknown */;
  status?: code /* Start and end time of administration */;
  effective?: dateTime | Period;
  identifier?: Array<Identifier> /* Encounter or Episode of Care administered as part of */;
  context?: Reference<"EpisodeOfCare" | "Encounter">;
  device?: Array<Reference<"Device">>;
  partOf?: Array<
    Reference<"MedicationAdministration" | "Procedure">
  > /* Who received medication */;
  subject?: Reference<"Patient" | "Group">;
  performer?: Array<{
    /* Type of performance */
    function?: CodeableConcept /* Who performed the medication administration */;
    actor?: Reference<
      | "Patient"
      | "PractitionerRole"
      | "Device"
      | "Practitioner"
      | "RelatedPerson"
    >;
  }> /* What was administered */;
  medication?: CodeableConcept | Reference<"Medication">;
  reasonReference?: Array<
    Reference<"Observation" | "DiagnosticReport" | "Condition">
  >;
}
/* Catalog entries are wrappers that contextualize items included in a catalog. */
interface CatalogEntry {
  additionalCharacteristic?: Array<CodeableConcept>;
  additionalClassification?: Array<CodeableConcept> /* The item that is being defined */;
  referencedItem?: Reference<
    | "PractitionerRole"
    | "HealthcareService"
    | "Medication"
    | "Organization"
    | "Device"
    | "Practitioner"
    | "PlanDefinition"
    | "SpecimenDefinition"
    | "ActivityDefinition"
    | "Binary"
    | "ObservationDefinition"
  >;
  _validTo?: Element;
  _status?: Element /* The type of item - medication, device, service, protocol or other */;
  type?: CodeableConcept;
  classification?: Array<CodeableConcept> /* The time period in which this catalog entry is expected to be active */;
  validityPeriod?: Period /* Whether the entry represents an orderable item */;
  orderable?: boolean /* draft | active | retired | unknown */;
  status?: code /* The date until which this catalog entry is expected to be active */;
  validTo?: dateTime;
  identifier?: Array<Identifier>;
  additionalIdentifier?: Array<Identifier> /* When was this catalog last updated */;
  lastUpdated?: dateTime;
  _lastUpdated?: Element;
  _orderable?: Element;
  relatedEntry?: Array<{
    /* triggers | is-replaced-by */ relationtype?: code;
    _relationtype?: Element /* The reference to the related item */;
    item?: Reference<"CatalogEntry">;
  }>;
}
/* Prospective warnings of potential issues when providing care to the patient. */
interface Flag {
  category?: Array<CodeableConcept> /* Alert relevant during encounter */;
  encounter?: Reference<"Encounter">;
  _status?: Element /* Flag creator */;
  author?: Reference<
    "Patient" | "PractitionerRole" | "Organization" | "Device" | "Practitioner"
  > /* active | inactive | entered-in-error */;
  status?: code /* Coded or textual message to display to user */;
  code?: CodeableConcept;
  identifier?: Array<Identifier> /* Time period when flag is active */;
  period?: Period /* Who/What is flag about? */;
  subject?: Reference<
    | "Patient"
    | "Medication"
    | "Organization"
    | "Location"
    | "Practitioner"
    | "PlanDefinition"
    | "Procedure"
    | "Group"
  >;
}
/* A record of a device being used by a patient where the record is the result of a report from the patient or another clinician. */
interface DeviceUseStatement {
  _recordedOn?: Element;
  derivedFrom?: Array<
    Reference<
      | "QuestionnaireResponse"
      | "Observation"
      | "DocumentReference"
      | "ServiceRequest"
      | "Procedure"
      | "Claim"
    >
  >;
  _status?: Element;
  reasonCode?: Array<CodeableConcept> /* Who made the statement */;
  source?: Reference<
    "Patient" | "PractitionerRole" | "Practitioner" | "RelatedPerson"
  >;
  note?: Array<Annotation> /* active | completed | entered-in-error + */;
  status?: code /* How often  the device was used */;
  timing?: dateTime | Period | Timing /* When statement was recorded */;
  recordedOn?: dateTime;
  identifier?: Array<Identifier> /* Target body site */;
  bodySite?: CodeableConcept /* Reference to device used */;
  device?: Reference<"Device">;
  basedOn?: Array<Reference<"ServiceRequest">> /* Patient using device */;
  subject?: Reference<"Patient" | "Group">;
  reasonReference?: Array<
    Reference<
      | "Media"
      | "Observation"
      | "DocumentReference"
      | "DiagnosticReport"
      | "Condition"
    >
  >;
}
/* Legally enforceable, formally recorded unilateral or bilateral directive i.e., a policy or agreement. */
interface Contract {
  _issued?: Element /* Source Contract Definition */;
  instantiatesCanonical?: Reference<"Contract"> /* External Contract Definition */;
  instantiatesUri?: uri;
  site?: Array<Reference<"Location">>;
  relevantHistory?: Array<Reference<"Provenance">>;
  supportingInfo?: Array<Reference<ResourceType>> /* Effective time */;
  applies?: Period /* Computer friendly designation */;
  name?: string;
  authority?: Array<Reference<"Organization">>;
  rule?: Array<{
    /* Computable Contract Rules */
    content?: Attachment | Reference<"DocumentReference">;
  }>;
  _status?: Element /* Legal instrument category */;
  type?: CodeableConcept;
  legal?: Array<{
    /* Contract Legal Text */
    content?:
      | Attachment
      | Reference<
          "QuestionnaireResponse" | "Composition" | "DocumentReference"
        >;
  }> /* Content derived from the basal information */;
  contentDerivative?: CodeableConcept /* Focus of contract interest */;
  topic?: CodeableConcept | Reference<ResourceType> /* Negotiation status */;
  legalState?: CodeableConcept /* Contract precursor content */;
  contentDefinition?: {
    /* Publisher Entity */
    publisher?: Reference<
      "PractitionerRole" | "Organization" | "Practitioner"
    > /* Publication Ownership */;
    copyright?: markdown /* Content structure and use */;
    type?: CodeableConcept;
    _publicationStatus?: Element;
    _publicationDate?: Element /* amended | appended | cancelled | disputed | entered-in-error | executable | executed | negotiable | offered | policy | rejected | renewed | revoked | resolved | terminated */;
    publicationStatus?: code;
    _copyright?: Element /* Detailed Content Type Definition */;
    subType?: CodeableConcept /* When published */;
    publicationDate?: dateTime;
  } /* Range of Legal Concerns */;
  scope?: CodeableConcept /* Human Friendly name */;
  title?: string;
  signer?: Array<{
    /* Contract Signatory Role */ type?: Coding /* Contract Signatory Party */;
    party?: Reference<
      | "Patient"
      | "PractitionerRole"
      | "Organization"
      | "Practitioner"
      | "RelatedPerson"
    >;
    signature?: Array<Signature>;
  }> /* Source of Contract */;
  author?: Reference<
    "Patient" | "PractitionerRole" | "Organization" | "Practitioner"
  >;
  term?: Array<Contract>;
  friendly?: Array<{
    /* Easily comprehended representation of this Contract */
    content?:
      | Attachment
      | Reference<
          "QuestionnaireResponse" | "Composition" | "DocumentReference"
        >;
  }>;
  alias?: Array<string> /* amended | appended | cancelled | disputed | entered-in-error | executable | executed | negotiable | offered | policy | rejected | renewed | revoked | resolved | terminated */;
  status?: code /* Subordinate Friendly name */;
  subtitle?: string;
  _name?: Element /* Basal definition */;
  url?: uri;
  identifier?: Array<Identifier>;
  _subtitle?: Element /* Contract cessation cause */;
  expirationType?: CodeableConcept /* When this Contract was issued */;
  issued?: dateTime;
  _title?: Element;
  domain?: Array<Reference<"Location">>;
  subType?: Array<CodeableConcept>;
  _alias?: Array<Element> /* Business edition */;
  version?: string;
  _version?: Element;
  subject?: Array<Reference<ResourceType>>;
  _url?: Element /* Binding Contract */;
  legallyBinding?:
    | Attachment
    | Reference<
        | "QuestionnaireResponse"
        | "Composition"
        | "Contract"
        | "DocumentReference"
      >;
  _instantiatesUri?: Element;
}
/* Invoice containing collected ChargeItems from an Account with calculated individual and total price for Billing purpose. */
interface Invoice {
  /* Invoice date / posting date */
  date?: dateTime /* Net total of this Invoice */;
  totalNet?: Money;
  _date?: Element;
  _status?: Element /* Recipient of this invoice */;
  recipient?: Reference<"Patient" | "Organization" | "RelatedPerson">;
  totalPriceComponent?: Array<Invoice> /* Type of Invoice */;
  type?: CodeableConcept /* Gross total of this Invoice */;
  totalGross?: Money;
  participant?: Array<{
    /* Type of involvement in creation of this Invoice */
    role?: CodeableConcept /* Individual who was involved */;
    actor?: Reference<
      | "Patient"
      | "PractitionerRole"
      | "Organization"
      | "Device"
      | "Practitioner"
      | "RelatedPerson"
    >;
  }>;
  _cancelledReason?: Element;
  _paymentTerms?: Element;
  note?: Array<Annotation> /* Account that is being balanced */;
  account?: Reference<"Account"> /* draft | issued | balanced | cancelled | entered-in-error */;
  status?: code;
  lineItem?: Array<{
    /* Sequence number of line item */ sequence?: positiveInt;
    _sequence?: Element /* Reference to ChargeItem containing details of this line item or an inline billing code */;
    chargeItem?: CodeableConcept | Reference<"ChargeItem">;
    priceComponent?: Array<Invoice>;
  }>;
  identifier?: Array<Identifier> /* Issuing Organization of Invoice */;
  issuer?: Reference<"Organization"> /* Reason for cancellation of this Invoice */;
  cancelledReason?: string /* Payment details */;
  paymentTerms?: markdown /* Recipient(s) of goods and services */;
  subject?: Reference<"Patient" | "Group">;
}
/* This resource provides the status of the payment for goods and services rendered, and the request and response resource references. */
interface PaymentNotice {
  /* Response reference */ response?: Reference<ResourceType>;
  _created?: Element /* Monetary amount of the payment */;
  amount?: Money /* Request reference */;
  request?: Reference<ResourceType> /* Payment reference */;
  payment?: Reference<"PaymentReconciliation">;
  _status?: Element /* Party being notified */;
  recipient?: Reference<"Organization"> /* Creation date */;
  created?: dateTime /* Issued or cleared Status of the payment */;
  paymentStatus?: CodeableConcept /* active | cancelled | draft | entered-in-error */;
  status?: code /* Party being paid */;
  payee?: Reference<
    "PractitionerRole" | "Organization" | "Practitioner"
  > /* Payment or clearing date */;
  paymentDate?: date;
  identifier?: Array<Identifier>;
  _paymentDate?: Element /* Responsible practitioner */;
  provider?: Reference<"PractitionerRole" | "Organization" | "Practitioner">;
}
/* Details and position information for a physical place where services are provided and resources and participants may be stored, found, contained, or accommodated. */
interface Location {
  /* Additional details about the location that could be displayed as further information to identify the location beyond its name */
  description?: string /* Physical location */;
  address?: Address /* Organization responsible for provisioning and upkeep */;
  managingOrganization?: Reference<"Organization"> /* Name of the location as used by humans */;
  name?: string;
  _status?: Element /* instance | kind */;
  mode?: code;
  type?: Array<CodeableConcept>;
  _description?: Element;
  alias?: Array<string> /* active | suspended | inactive */;
  status?: code;
  _name?: Element;
  _availabilityExceptions?: Element;
  identifier?: Array<Identifier>;
  hoursOfOperation?: Array<{
    daysOfWeek?: Array<code>;
    _daysOfWeek?: Array<Element> /* The Location is open all day */;
    allDay?: boolean;
    _allDay?: Element /* Time that the Location opens */;
    openingTime?: time;
    _openingTime?: Element /* Time that the Location closes */;
    closingTime?: time;
    _closingTime?: Element;
  }> /* Description of availability exceptions */;
  availabilityExceptions?: string /* The absolute geographic location */;
  position?: {
    /* Longitude with WGS84 datum */ longitude?: decimal;
    _longitude?: Element /* Latitude with WGS84 datum */;
    latitude?: decimal;
    _latitude?: Element /* Altitude with WGS84 datum */;
    altitude?: decimal;
    _altitude?: Element;
  };
  telecom?: Array<ContactPoint> /* The operational status of the location (typically only for a bed/room) */;
  operationalStatus?: Coding /* Another Location this one is physically a part of */;
  partOf?: Reference<"Location">;
  _alias?: Array<Element>;
  _mode?: Element /* Physical form of the location */;
  physicalType?: CodeableConcept;
  endpoint?: Array<Reference<"Endpoint">>;
}
/* A provider issued list of professional services and products which have been provided, or are to be provided, to a patient which is sent to an insurer for reimbursement. */
interface Claim {
  _created?: Element /* The recipient of the products and services */;
  patient?: Reference<"Patient">;
  insurance?: Array<{
    /* Adjudication results */ claimResponse?: Reference<"ClaimResponse">;
    _sequence?: Element;
    _focal?: Element;
    preAuthRef?: Array<string> /* Coverage to be used for adjudication */;
    focal?: boolean /* Additional provider contract number */;
    businessArrangement?: string /* Insurance information */;
    coverage?: Reference<"Coverage"> /* Insurance instance identifier */;
    sequence?: positiveInt /* Pre-assigned Claim number */;
    identifier?: Identifier;
    _businessArrangement?: Element;
    _preAuthRef?: Array<Element>;
  }> /* Servicing facility */;
  facility?: Reference<"Location">;
  diagnosis?: Array<{
    /* Diagnosis instance identifier */ sequence?: positiveInt;
    _sequence?: Element /* Nature of illness or problem */;
    diagnosis?: CodeableConcept | Reference<"Condition">;
    type?: Array<CodeableConcept> /* Present on admission */;
    onAdmission?: CodeableConcept /* Package billing code */;
    packageCode?: CodeableConcept;
  }> /* Author of the claim */;
  enterer?: Reference<"PractitionerRole" | "Practitioner">;
  supportingInfo?: Array<{
    /* Information instance identifier */ sequence?: positiveInt;
    _sequence?: Element /* Classification of the supplied information */;
    category?: CodeableConcept /* Type of information */;
    code?: CodeableConcept /* When it occurred */;
    timing?: date | Period /* Data to be provided */;
    value?:
      | string
      | Attachment
      | Quantity
      | boolean
      | Reference<ResourceType> /* Explanation for the information */;
    reason?: CodeableConcept;
  }> /* claim | preauthorization | predetermination */;
  use?: code;
  item?: Array<{
    /* Benefit classification */ category?: CodeableConcept;
    diagnosisSequence?: Array<positiveInt>;
    _sequence?: Element;
    procedureSequence?: Array<positiveInt>;
    modifier?: Array<CodeableConcept> /* Revenue or cost center code */;
    revenue?: CodeableConcept;
    encounter?: Array<Reference<"Encounter">>;
    _careTeamSequence?: Array<Element>;
    _factor?: Element /* Total item cost */;
    net?: Money /* Date or dates of service or product delivery */;
    serviced?: date | Period;
    subSite?: Array<CodeableConcept>;
    _procedureSequence?: Array<Element>;
    careTeamSequence?: Array<positiveInt> /* Billing, service, product, or drug code */;
    productOrService?: CodeableConcept;
    _informationSequence?: Array<Element>;
    udi?: Array<Reference<"Device">>;
    informationSequence?: Array<positiveInt>;
    programCode?: Array<CodeableConcept> /* Price scaling factor */;
    factor?: decimal;
    _diagnosisSequence?: Array<Element> /* Item instance identifier */;
    sequence?: positiveInt /* Anatomical location */;
    bodySite?: CodeableConcept /* Count of products or services */;
    quantity?: Quantity /* Place of service or where product was supplied */;
    location?:
      | Address
      | CodeableConcept
      | Reference<"Location"> /* Fee, charge or cost per item */;
    unitPrice?: Money;
    detail?: Array<{
      /* Benefit classification */ category?: CodeableConcept;
      _sequence?: Element;
      modifier?: Array<CodeableConcept> /* Revenue or cost center code */;
      revenue?: CodeableConcept;
      _factor?: Element /* Total item cost */;
      net?: Money /* Billing, service, product, or drug code */;
      productOrService?: CodeableConcept;
      udi?: Array<Reference<"Device">>;
      programCode?: Array<CodeableConcept> /* Price scaling factor */;
      factor?: decimal /* Item instance identifier */;
      sequence?: positiveInt;
      subDetail?: Array<{
        /* Benefit classification */ category?: CodeableConcept;
        _sequence?: Element;
        modifier?: Array<CodeableConcept> /* Revenue or cost center code */;
        revenue?: CodeableConcept;
        _factor?: Element /* Total item cost */;
        net?: Money /* Billing, service, product, or drug code */;
        productOrService?: CodeableConcept;
        udi?: Array<Reference<"Device">>;
        programCode?: Array<CodeableConcept> /* Price scaling factor */;
        factor?: decimal /* Item instance identifier */;
        sequence?: positiveInt /* Count of products or services */;
        quantity?: Quantity /* Fee, charge or cost per item */;
        unitPrice?: Money;
      }> /* Count of products or services */;
      quantity?: Quantity /* Fee, charge or cost per item */;
      unitPrice?: Money;
    }>;
  }>;
  _status?: Element /* Category or discipline */;
  type?: CodeableConcept /* Resource creation date */;
  created?: dateTime;
  procedure?: Array<{
    /* Procedure instance identifier */ sequence?: positiveInt;
    _sequence?: Element;
    type?: Array<CodeableConcept> /* When the procedure was performed */;
    date?: dateTime;
    _date?: Element /* Specific clinical procedure */;
    procedure?: CodeableConcept | Reference<"Procedure">;
    udi?: Array<Reference<"Device">>;
  }>;
  related?: Array<{
    /* Reference to the related claim */
    claim?: Reference<"Claim"> /* How the reference claim is related */;
    relationship?: CodeableConcept /* File or case reference */;
    reference?: Identifier;
  }> /* Treatment referral */;
  referral?: Reference<"ServiceRequest"> /* Total claim cost */;
  total?: Money /* Target */;
  insurer?: Reference<"Organization"> /* For whom to reserve funds */;
  fundsReserve?: CodeableConcept /* Desired processing ugency */;
  priority?: CodeableConcept /* Details of the event */;
  accident?: {
    /* When the incident occurred */ date?: date;
    _date?: Element /* The nature of the accident */;
    type?: CodeableConcept /* Where the event occurred */;
    location?: Address | Reference<"Location">;
  } /* active | cancelled | draft | entered-in-error */;
  status?: code /* Recipient of benefits payable */;
  payee?: {
    /* Category of recipient */
    type?: CodeableConcept /* Recipient reference */;
    party?: Reference<
      | "Patient"
      | "PractitionerRole"
      | "Organization"
      | "Practitioner"
      | "RelatedPerson"
    >;
  } /* Prescription authorizing services and products */;
  prescription?: Reference<
    "VisionPrescription" | "DeviceRequest" | "MedicationRequest"
  > /* Relevant time frame for the claim */;
  billablePeriod?: Period;
  identifier?: Array<Identifier> /* More granular claim type */;
  subType?: CodeableConcept /* Party responsible for the claim */;
  provider?: Reference<
    "PractitionerRole" | "Organization" | "Practitioner"
  > /* Original prescription if superseded by fulfiller */;
  originalPrescription?: Reference<
    "VisionPrescription" | "DeviceRequest" | "MedicationRequest"
  >;
  _use?: Element;
  careTeam?: Array<{
    /* Order of care team */ sequence?: positiveInt;
    _sequence?: Element /* Practitioner or organization */;
    provider?: Reference<
      "PractitionerRole" | "Organization" | "Practitioner"
    > /* Indicator of the lead practitioner */;
    responsible?: boolean;
    _responsible?: Element /* Function within the team */;
    role?: CodeableConcept /* Practitioner credential or specialization */;
    qualification?: CodeableConcept;
  }>;
}
/* A sample to be used for analysis. */
interface Specimen {
  request?: Array<
    Reference<"ServiceRequest">
  > /* The time when specimen was received for processing */;
  receivedTime?: dateTime;
  processing?: Array<{
    /* Textual description of procedure */ description?: string;
    _description?: Element /* Indicates the treatment step  applied to the specimen */;
    procedure?: CodeableConcept;
    additive?: Array<
      Reference<"Substance">
    > /* Date and time of specimen processing */;
    time?: dateTime | Period;
  }>;
  parent?: Array<Reference<"Specimen">>;
  _status?: Element /* Kind of material that forms the specimen */;
  type?: CodeableConcept;
  note?: Array<Annotation> /* available | unavailable | unsatisfactory | entered-in-error */;
  status?: code;
  condition?: Array<CodeableConcept>;
  container?: Array<{
    identifier?: Array<Identifier> /* Textual description of the container */;
    description?: string;
    _description?: Element /* Kind of container directly associated with specimen */;
    type?: CodeableConcept /* Container volume or size */;
    capacity?: Quantity /* Quantity of specimen within container */;
    specimenQuantity?: Quantity /* Additive associated with container */;
    additive?: CodeableConcept | Reference<"Substance">;
  }>;
  identifier?: Array<Identifier> /* Identifier assigned by the lab */;
  accessionIdentifier?: Identifier;
  _receivedTime?: Element /* Collection details */;
  collection?: {
    /* Who collected the specimen */
    collector?: Reference<
      "PractitionerRole" | "Practitioner"
    > /* Collection time */;
    collected?: dateTime | Period /* How long it took to collect specimen */;
    duration?: Duration /* The quantity of specimen collected */;
    quantity?: Quantity /* Technique used to perform collection */;
    method?: CodeableConcept /* Anatomical collection site */;
    bodySite?: CodeableConcept /* Whether or how long patient abstained from food and/or drink */;
    fastingStatus?: Duration | CodeableConcept;
  } /* Where the specimen came from. This may be from patient(s), from a location (e.g., the source of an environmental sample), or a sampling of a substance or a device */;
  subject?: Reference<
    "Patient" | "Device" | "Location" | "Substance" | "Group"
  >;
}
/* A record of a medication that is being consumed by a patient.   A MedicationStatement may indicate that the patient may be taking the medication now or has taken the medication in the past or will be taking the medication in the future.  The source of this information can be the patient, significant other (such as a family member or spouse), or a clinician.  A common scenario where this information is captured is during the history taking process during a patient visit or stay.   The medication information may come from sources such as the patient's memory, from a prescription bottle,  or from a list of medications the patient, clinician or other party maintains. 

The primary difference between a medication statement and a medication administration is that the medication administration has complete administration information and is based on actual administration information from the person who administered the medication.  A medication statement is often, if not always, less specific.  There is no required date/time when the medication was administered, in fact we only know that a source has reported the patient is taking this medication, where details such as time, quantity, or rate or even medication product may be incomplete or missing or less precise.  As stated earlier, the medication statement information may come from the patient's memory, from a prescription bottle or from a list of medications the patient, clinician or other party maintains.  Medication administration is more formal and is not missing detailed information. */
interface MedicationStatement {
  /* Type of medication usage */ category?: CodeableConcept;
  dosage?: Array<Dosage>;
  derivedFrom?: Array<Reference<ResourceType>>;
  _status?: Element;
  reasonCode?: Array<CodeableConcept>;
  statusReason?: Array<CodeableConcept>;
  note?: Array<Annotation> /* active | completed | entered-in-error | intended | stopped | on-hold | unknown | not-taken */;
  status?: code /* The date/time or interval when the medication is/was/will be taken */;
  effective?: dateTime | Period;
  _dateAsserted?: Element;
  identifier?: Array<Identifier> /* Encounter / Episode associated with MedicationStatement */;
  context?: Reference<
    "EpisodeOfCare" | "Encounter"
  > /* When the statement was asserted? */;
  dateAsserted?: dateTime;
  basedOn?: Array<
    Reference<"ServiceRequest" | "CarePlan" | "MedicationRequest">
  >;
  partOf?: Array<
    Reference<
      | "MedicationDispense"
      | "MedicationAdministration"
      | "Observation"
      | "Procedure"
      | "MedicationStatement"
    >
  > /* Person or organization that provided the information about the taking of this medication */;
  informationSource?: Reference<
    | "Patient"
    | "PractitionerRole"
    | "Organization"
    | "Practitioner"
    | "RelatedPerson"
  > /* Who is/was taking  the medication */;
  subject?: Reference<"Patient" | "Group"> /* What medication was taken */;
  medication?: CodeableConcept | Reference<"Medication">;
  reasonReference?: Array<
    Reference<"Observation" | "DiagnosticReport" | "Condition">
  >;
}
/* This resource provides enrollment and plan details from the processing of an EnrollmentRequest resource. */
interface EnrollmentResponse {
  _created?: Element /* Claim reference */;
  request?: Reference<"EnrollmentRequest"> /* Responsible practitioner */;
  requestProvider?: Reference<
    "PractitionerRole" | "Organization" | "Practitioner"
  >;
  _disposition?: Element;
  _status?: Element /* Creation date */;
  created?: dateTime /* queued | complete | error | partial */;
  outcome?: code /* Insurer */;
  organization?: Reference<"Organization">;
  _outcome?: Element /* Disposition Message */;
  disposition?: string /* active | cancelled | draft | entered-in-error */;
  status?: code;
  identifier?: Array<Identifier>;
}
/* The Evidence resource describes the conditional state (population and any exposures being compared within the population) and outcome (if specified) that the knowledge (evidence, assertion, recommendation) is about. */
interface Evidence {
  /* Natural language description of the evidence */
  description?: markdown /* Date last changed */;
  date?: dateTime;
  endorser?: Array<ContactDetail> /* Name of the publisher (organization or individual) */;
  publisher?: string /* When the evidence was approved by publisher */;
  approvalDate?: date;
  jurisdiction?: Array<CodeableConcept>;
  _publisher?: Element;
  _date?: Element /* Name for this evidence (computer friendly) */;
  name?: string;
  _status?: Element;
  useContext?: Array<UsageContext> /* Use and/or publishing restrictions */;
  copyright?: markdown;
  _approvalDate?: Element;
  outcome?: Array<Reference<"EvidenceVariable">>;
  topic?: Array<CodeableConcept> /* Name for this evidence (human friendly) */;
  title?: string;
  _description?: Element;
  note?: Array<Annotation>;
  author?: Array<ContactDetail>;
  _lastReviewDate?: Element /* draft | active | retired | unknown */;
  status?: code /* Subordinate title of the Evidence */;
  subtitle?: string;
  _name?: Element /* Canonical identifier for this evidence, represented as a URI (globally unique) */;
  url?: uri;
  identifier?: Array<Identifier> /* When the evidence was last reviewed */;
  lastReviewDate?: date;
  editor?: Array<ContactDetail>;
  _subtitle?: Element;
  _copyright?: Element;
  _shortTitle?: Element;
  _title?: Element;
  reviewer?: Array<ContactDetail> /* Title for use in informal contexts */;
  shortTitle?: string /* Business version of the evidence */;
  version?: string;
  _version?: Element;
  relatedArtifact?: Array<RelatedArtifact>;
  contact?: Array<ContactDetail> /* What population? */;
  exposureBackground?: Reference<"EvidenceVariable">;
  _url?: Element /* When the evidence is expected to be used */;
  effectivePeriod?: Period;
  exposureVariant?: Array<Reference<"EvidenceVariable">>;
}
/* A container for a collection of resources. */
interface Bundle {
  _timestamp?: Element /* Digital Signature */;
  signature?: Signature;
  _type?: Element /* document | message | transaction | transaction-response | batch | batch-response | history | searchset | collection */;
  type?: code /* If search, the total number of matches */;
  total?: unsignedInt;
  link?: Array<Bundle> /* Persistent identifier for the bundle */;
  identifier?: Identifier;
  entry?: Array<{
    link?: Array<Bundle> /* URI for resource (Absolute URL server address or URI for UUID/OID) */;
    fullUrl?: uri;
    _fullUrl?: Element /* A resource in the bundle */;
    resource?: Resource /* Search related information */;
    search?: {
      /* match | include | outcome - why this is in the result set */
      mode?: code;
      _mode?: Element /* Search ranking (between 0 and 1) */;
      score?: decimal;
      _score?: Element;
    } /* Additional execution information (transaction/batch/history) */;
    request?: {
      /* For conditional creates */ ifNoneExist?: string;
      _ifNoneMatch?: Element;
      _ifNoneExist?: Element /* GET | HEAD | POST | PUT | DELETE | PATCH */;
      method?: code /* For managing cache currency */;
      ifModifiedSince?: instant;
      _ifMatch?: Element;
      _method?: Element /* For managing cache currency */;
      ifNoneMatch?: string /* URL for HTTP equivalent of this entry */;
      url?: uri;
      _url?: Element /* For managing update contention */;
      ifMatch?: string;
      _ifModifiedSince?: Element;
    } /* Results of execution (transaction/batch/history) */;
    response?: {
      _status?: Element;
      _location?: Element /* The Etag for the resource (if relevant) */;
      etag?: string /* OperationOutcome with hints and warnings (for batch/transaction) */;
      outcome?: Resource;
      _etag?: Element /* Server's date time modified */;
      lastModified?: instant /* Status response code (text optional) */;
      status?: string;
      _lastModified?: Element /* The location (if the operation returns a location) */;
      location?: uri;
    };
  }> /* When the bundle was assembled */;
  timestamp?: instant;
  _total?: Element;
}
/* The ResearchElementDefinition resource describes a "PICO" element that knowledge (evidence, assertion, recommendation) is about. */
interface ResearchElementDefinition {
  /* Natural language description of the research element definition */
  description?: markdown;
  _usage?: Element /* Date last changed */;
  date?: dateTime;
  endorser?: Array<ContactDetail> /* Name of the publisher (organization or individual) */;
  publisher?: string /* When the research element definition was approved by publisher */;
  approvalDate?: date /* dichotomous | continuous | descriptive */;
  variableType?: code;
  jurisdiction?: Array<CodeableConcept>;
  _publisher?: Element /* Why this research element definition is defined */;
  purpose?: markdown;
  _date?: Element /* Name for this research element definition (computer friendly) */;
  name?: string;
  _type?: Element;
  _status?: Element;
  _experimental?: Element;
  useContext?: Array<UsageContext> /* Use and/or publishing restrictions */;
  copyright?: markdown;
  _approvalDate?: Element /* population | exposure | outcome */;
  type?: code /* For testing purposes, not real usage */;
  experimental?: boolean;
  topic?: Array<CodeableConcept> /* Name for this research element definition (human friendly) */;
  title?: string;
  _description?: Element;
  library?: Array<canonical>;
  author?: Array<ContactDetail>;
  characteristic?: Array<{
    /* What code or expression defines members? */
    definition?:
      | DataRequirement
      | Expression
      | canonical
      | CodeableConcept /* Whether the characteristic includes or excludes members */;
    exclude?: boolean;
    _studyEffectiveDescription?: Element /* mean | median | mean-of-mean | mean-of-median | median-of-mean | median-of-median */;
    studyEffectiveGroupMeasure?: code /* mean | median | mean-of-mean | mean-of-median | median-of-mean | median-of-median */;
    participantEffectiveGroupMeasure?: code /* What time period does the study cover */;
    studyEffectiveDescription?: string /* Observation time from study start */;
    studyEffectiveTimeFromStart?: Duration /* What unit is the outcome described in? */;
    unitOfMeasure?: CodeableConcept /* What time period does the study cover */;
    studyEffective?:
      | dateTime
      | Period
      | Timing
      | Duration /* What time period do participants cover */;
    participantEffectiveDescription?: string /* What time period do participants cover */;
    participantEffective?: dateTime | Period | Timing | Duration;
    _participantEffectiveDescription?: Element;
    _exclude?: Element;
    _studyEffectiveGroupMeasure?: Element;
    usageContext?: Array<UsageContext>;
    _participantEffectiveGroupMeasure?: Element /* Observation time from study start */;
    participantEffectiveTimeFromStart?: Duration;
  }>;
  _purpose?: Element /* Describes the clinical usage of the ResearchElementDefinition */;
  usage?: string;
  _lastReviewDate?: Element /* draft | active | retired | unknown */;
  status?: code /* Subordinate title of the ResearchElementDefinition */;
  subtitle?: string;
  comment?: Array<string>;
  _name?: Element /* Canonical identifier for this research element definition, represented as a URI (globally unique) */;
  url?: uri;
  identifier?: Array<Identifier> /* When the research element definition was last reviewed */;
  lastReviewDate?: date;
  editor?: Array<ContactDetail>;
  _subtitle?: Element;
  _library?: Array<Element>;
  _copyright?: Element;
  _shortTitle?: Element;
  _title?: Element;
  reviewer?: Array<ContactDetail> /* Title for use in informal contexts */;
  shortTitle?: string /* Business version of the research element definition */;
  version?: string;
  _version?: Element;
  relatedArtifact?: Array<RelatedArtifact>;
  _variableType?: Element;
  contact?: Array<ContactDetail>;
  _comment?: Array<Element> /* E.g. Patient, Practitioner, RelatedPerson, Organization, Location, Device */;
  subject?: CodeableConcept | Reference<"Group">;
  _url?: Element /* When the research element definition is expected to be used */;
  effectivePeriod?: Period;
}
/* Record details about an anatomical structure.  This resource may be used when a coded concept does not provide the necessary detail needed for the use case. */
interface BodyStructure {
  /* Who this is about */ patient?: Reference<"Patient"> /* Text description */;
  description?: string;
  _active?: Element /* Kind of Structure */;
  morphology?: CodeableConcept;
  _description?: Element /* Whether this record is in active use */;
  active?: boolean;
  identifier?: Array<Identifier>;
  image?: Array<Attachment> /* Body site */;
  location?: CodeableConcept;
  locationQualifier?: Array<CodeableConcept>;
}
/* Detailed definition of a medicinal product, typically for uses other than direct patient care (e.g. regulatory use). */
interface MedicinalProduct {
  /* Whether the Medicinal Product is subject to additional monitoring for regulatory reasons */
  additionalMonitoringIndicator?: CodeableConcept;
  manufacturingBusinessOperation?: Array<{
    /* The type of manufacturing operation */
    operationType?: CodeableConcept /* Regulatory authorization reference number */;
    authorisationReferenceNumber?: Identifier /* Regulatory authorization date */;
    effectiveDate?: dateTime;
    _effectiveDate?: Element /* To indicate if this proces is commercially confidential */;
    confidentialityIndicator?: CodeableConcept;
    manufacturer?: Array<
      Reference<"Organization">
    > /* A regulator which oversees the operation */;
    regulator?: Reference<"Organization">;
  }> /* The dose form for a single part product, or combined form of a multiple part product */;
  combinedPharmaceuticalDoseForm?: CodeableConcept;
  clinicalTrial?: Array<Reference<"ResearchStudy">>;
  productClassification?: Array<CodeableConcept>;
  name?: Array<{
    /* The full product name */ productName?: string;
    _productName?: Element;
    namePart?: Array<{
      /* A fragment of a product name */ part?: string;
      _part?: Element /* Idenifying type for this part of the name (e.g. strength part) */;
      type?: Coding;
    }>;
    countryLanguage?: Array<{
      /* Country code for where this name applies */
      country?: CodeableConcept /* Jurisdiction code for where this name applies */;
      jurisdiction?: CodeableConcept /* Language code for this name */;
      language?: CodeableConcept;
    }>;
  }>;
  masterFile?: Array<Reference<"DocumentReference">>;
  pharmaceuticalProduct?: Array<
    Reference<"MedicinalProductPharmaceutical">
  > /* Regulatory type, e.g. Investigational or Authorized */;
  type?: CodeableConcept;
  marketingStatus?: Array<MarketingStatus>;
  specialMeasures?: Array<string>;
  specialDesignation?: Array<{
    identifier?: Array<Identifier> /* The type of special designation, e.g. orphan drug, minor use */;
    type?: CodeableConcept /* The intended use of the product, e.g. prevention, treatment */;
    intendedUse?: CodeableConcept /* Condition for which the medicinal use applies */;
    indication?:
      | CodeableConcept
      | Reference<"MedicinalProductIndication"> /* For example granted, pending, expired or withdrawn */;
    status?: CodeableConcept /* Date when the designation was granted */;
    date?: dateTime;
    _date?: Element /* Animal species for which this applies */;
    species?: CodeableConcept;
  }>;
  packagedMedicinalProduct?: Array<Reference<"MedicinalProductPackaged">>;
  _specialMeasures?: Array<Element>;
  identifier?: Array<Identifier>;
  crossReference?: Array<Identifier>;
  attachedDocument?: Array<
    Reference<"DocumentReference">
  > /* If this medicine applies to human or veterinary uses */;
  domain?: Coding /* The legal status of supply of the medicinal product as classified by the regulator */;
  legalStatusOfSupply?: CodeableConcept /* If authorised for use in children */;
  paediatricUseIndicator?: CodeableConcept;
  contact?: Array<Reference<"PractitionerRole" | "Organization">>;
}
/* A process where a researcher or organization plans and then executes a series of steps intended to increase the field of healthcare-related knowledge.  This includes studies of safety, efficacy, comparative effectiveness and other information about medications, devices, therapies and other interventional and investigative techniques.  A ResearchStudy involves the gathering of information about human or animal subjects. */
interface ResearchStudy {
  /* What this is study doing */ description?: markdown;
  category?: Array<CodeableConcept>;
  enrollment?: Array<Reference<"Group">>;
  arm?: Array<{
    /* Label for study arm */ name?: string;
    _name?: Element /* Categorization of study arm */;
    type?: CodeableConcept /* Short explanation of study path */;
    description?: string;
    _description?: Element;
  }>;
  site?: Array<Reference<"Location">>;
  protocol?: Array<
    Reference<"PlanDefinition">
  > /* Researcher who oversees multiple aspects of the study */;
  principalInvestigator?: Reference<"PractitionerRole" | "Practitioner">;
  _status?: Element /* n-a | early-phase-1 | phase-1 | phase-1-phase-2 | phase-2 | phase-2-phase-3 | phase-3 | phase-4 */;
  phase?: CodeableConcept /* accrual-goal-met | closed-due-to-toxicity | closed-due-to-lack-of-study-progress | temporarily-closed-per-study-design */;
  reasonStopped?: CodeableConcept /* Name for this study */;
  title?: string;
  _description?: Element;
  note?: Array<Annotation>;
  keyword?: Array<CodeableConcept> /* active | administratively-completed | approved | closed-to-accrual | closed-to-accrual-and-intervention | completed | disapproved | in-review | temporarily-closed-to-accrual | temporarily-closed-to-accrual-and-intervention | withdrawn */;
  status?: code;
  condition?: Array<CodeableConcept>;
  identifier?: Array<Identifier> /* treatment | prevention | diagnostic | supportive-care | screening | health-services-research | basic-science | device-feasibility */;
  primaryPurposeType?: CodeableConcept;
  focus?: Array<CodeableConcept>;
  _title?: Element;
  objective?: Array<{
    /* Label for the objective */ name?: string;
    _name?: Element /* primary | secondary | exploratory */;
    type?: CodeableConcept;
  }> /* When the study began and ended */;
  period?: Period;
  partOf?: Array<Reference<"ResearchStudy">>;
  relatedArtifact?: Array<RelatedArtifact>;
  location?: Array<CodeableConcept>;
  contact?: Array<ContactDetail> /* Organization that initiates and is legally responsible for the study */;
  sponsor?: Reference<"Organization">;
}
/* A reply to an appointment request for a patient and/or practitioner(s), such as a confirmation or rejection. */
interface AppointmentResponse {
  /* Appointment this response relates to */
  appointment?: Reference<"Appointment">;
  _end?: Element /* Time from appointment, or requested new start time */;
  start?: instant /* accepted | declined | tentative | needs-action */;
  participantStatus?: code;
  participantType?: Array<CodeableConcept> /* Additional comments */;
  comment?: string;
  identifier?: Array<Identifier>;
  _participantStatus?: Element /* Time from appointment, or requested new end time */;
  end?: instant;
  _start?: Element;
  _comment?: Element /* Person, Location, HealthcareService, or Device */;
  actor?: Reference<
    | "Patient"
    | "PractitionerRole"
    | "HealthcareService"
    | "Device"
    | "Location"
    | "Practitioner"
    | "RelatedPerson"
  >;
}
/* Indication for the Medicinal Product. */
interface MedicinalProductIndication {
  /* The disease, symptom or procedure that is the indication for treatment */
  diseaseSymptomProcedure?: CodeableConcept;
  undesirableEffect?: Array<
    Reference<"MedicinalProductUndesirableEffect">
  > /* Timing or duration information as part of the indication */;
  duration?: Quantity;
  otherTherapy?: Array<{
    /* The type of relationship between the medicinal product indication or contraindication and another therapy */
    therapyRelationshipType?: CodeableConcept /* Reference to a specific medication (active substance, medicinal product or class of products) as part of an indication or contraindication */;
    medication?:
      | CodeableConcept
      | Reference<
          | "Medication"
          | "SubstanceSpecification"
          | "Substance"
          | "MedicinalProduct"
        >;
  }>;
  comorbidity?: Array<CodeableConcept> /* The intended effect, aim or strategy to be achieved by the indication */;
  intendedEffect?: CodeableConcept;
  population?: Array<Population> /* The status of the disease or symptom for which the indication applies */;
  diseaseStatus?: CodeableConcept;
  subject?: Array<Reference<"Medication" | "MedicinalProduct">>;
}
/* The Measure resource provides the definition of a quality measure. */
interface Measure {
  /* Natural language description of the measure */ description?: markdown;
  _riskAdjustment?: Element;
  _usage?: Element;
  definition?: Array<markdown>;
  _rateAggregation?: Element /* Date last changed */;
  date?: dateTime;
  group?: Array<{
    /* Meaning of the group */ code?: CodeableConcept /* Summary description */;
    description?: string;
    _description?: Element;
    population?: Array<{
      /* initial-population | numerator | numerator-exclusion | denominator | denominator-exclusion | denominator-exception | measure-population | measure-population-exclusion | measure-observation */
      code?: CodeableConcept /* The human readable description of this population criteria */;
      description?: string;
      _description?: Element /* The criteria that defines this population */;
      criteria?: Expression;
    }>;
    stratifier?: Array<{
      /* Meaning of the stratifier */
      code?: CodeableConcept /* The human readable description of this stratifier */;
      description?: string;
      _description?: Element /* How the measure should be stratified */;
      criteria?: Expression;
      component?: Array<{
        /* Meaning of the stratifier component */
        code?: CodeableConcept /* The human readable description of this stratifier component */;
        description?: string;
        _description?: Element /* Component of how the measure should be stratified */;
        criteria?: Expression;
      }>;
    }>;
  }>;
  endorser?: Array<ContactDetail> /* Name of the publisher (organization or individual) */;
  publisher?: string /* When the measure was approved by publisher */;
  approvalDate?: date /* opportunity | all-or-nothing | linear | weighted */;
  compositeScoring?: CodeableConcept /* Disclaimer for use of the measure or its referenced content */;
  disclaimer?: markdown;
  jurisdiction?: Array<CodeableConcept>;
  _publisher?: Element /* Why this measure is defined */;
  purpose?: markdown;
  _date?: Element /* Name for this measure (computer friendly) */;
  name?: string;
  _definition?: Array<Element>;
  _status?: Element;
  _experimental?: Element;
  useContext?: Array<UsageContext> /* Use and/or publishing restrictions */;
  copyright?: markdown;
  _clinicalRecommendationStatement?: Element /* Additional guidance for implementers */;
  guidance?: markdown;
  _approvalDate?: Element;
  type?: Array<CodeableConcept> /* For testing purposes, not real usage */;
  experimental?: boolean;
  topic?: Array<CodeableConcept> /* Name for this measure (human friendly) */;
  title?: string;
  _description?: Element;
  supplementalData?: Array<{
    /* Meaning of the supplemental data */ code?: CodeableConcept;
    usage?: Array<CodeableConcept> /* The human readable description of this supplemental data */;
    description?: string;
    _description?: Element /* Expression describing additional data to be reported */;
    criteria?: Expression;
  }>;
  library?: Array<canonical>;
  author?: Array<ContactDetail>;
  _purpose?: Element /* Describes the clinical usage of the measure */;
  usage?: string /* Detailed description of why the measure exists */;
  rationale?: markdown;
  _lastReviewDate?: Element /* draft | active | retired | unknown */;
  status?: code;
  _rationale?: Element /* Subordinate title of the measure */;
  subtitle?: string;
  _name?: Element /* Canonical identifier for this measure, represented as a URI (globally unique) */;
  url?: uri;
  identifier?: Array<Identifier> /* When the measure was last reviewed */;
  lastReviewDate?: date;
  editor?: Array<ContactDetail>;
  _subtitle?: Element;
  _library?: Array<Element>;
  _copyright?: Element /* How risk adjustment is applied for this measure */;
  riskAdjustment?: string /* proportion | ratio | continuous-variable | cohort */;
  scoring?: CodeableConcept;
  _title?: Element;
  reviewer?: Array<ContactDetail> /* Business version of the measure */;
  version?: string;
  _version?: Element;
  relatedArtifact?: Array<RelatedArtifact>;
  contact?: Array<ContactDetail> /* increase | decrease */;
  improvementNotation?: CodeableConcept /* E.g. Patient, Practitioner, RelatedPerson, Organization, Location, Device */;
  subject?: CodeableConcept | Reference<"Group">;
  _url?: Element;
  _disclaimer?: Element /* How is rate aggregation performed for this measure */;
  rateAggregation?: string /* When the measure is expected to be used */;
  effectivePeriod?: Period /* Summary of clinical guidelines */;
  clinicalRecommendationStatement?: markdown;
  _guidance?: Element;
}
/* Demographics and administrative information about a person independent of a specific health-related context. */
interface Person {
  _active?: Element;
  address?: Array<Address> /* The organization that is the custodian of the person record */;
  managingOrganization?: Reference<"Organization">;
  name?: Array<HumanName>;
  _gender?: Element /* The date on which the person was born */;
  birthDate?: date;
  _birthDate?: Element /* Image of the person */;
  photo?: Attachment;
  link?: Array<{
    /* The resource to which this actual person is associated */
    target?: Reference<
      "Patient" | "Person" | "Practitioner" | "RelatedPerson"
    > /* level1 | level2 | level3 | level4 */;
    assurance?: code;
    _assurance?: Element;
  }> /* This person's record is in active use */;
  active?: boolean;
  identifier?: Array<Identifier>;
  telecom?: Array<ContactPoint> /* male | female | other | unknown */;
  gender?: code;
}
/* Details of a Health Insurance product/plan provided by an organization. */
interface InsurancePlan {
  coverageArea?: Array<Reference<"Location">> /* Official name */;
  name?: string;
  _status?: Element;
  coverage?: Array<{
    /* Type of coverage */ type?: CodeableConcept;
    network?: Array<Reference<"Organization">>;
    benefit?: Array<{
      /* Type of benefit */ type?: CodeableConcept /* Referral requirements */;
      requirement?: string;
      _requirement?: Element;
      limit?: Array<{
        /* Maximum value allowed */
        value?: Quantity /* Benefit limit details */;
        code?: CodeableConcept;
      }>;
    }>;
  }>;
  type?: Array<CodeableConcept>;
  alias?: Array<string> /* draft | active | retired | unknown */;
  status?: code;
  _name?: Element;
  identifier?: Array<Identifier> /* Product administrator */;
  administeredBy?: Reference<"Organization"> /* Plan issuer */;
  ownedBy?: Reference<"Organization">;
  network?: Array<
    Reference<"Organization">
  > /* When the product is available */;
  period?: Period;
  _alias?: Array<Element>;
  plan?: Array<{
    identifier?: Array<Identifier> /* Type of plan */;
    type?: CodeableConcept;
    coverageArea?: Array<Reference<"Location">>;
    network?: Array<Reference<"Organization">>;
    generalCost?: Array<{
      /* Type of cost */ type?: CodeableConcept /* Number of enrollees */;
      groupSize?: positiveInt;
      _groupSize?: Element /* Cost value */;
      cost?: Money /* Additional cost information */;
      comment?: string;
      _comment?: Element;
    }>;
    specificCost?: Array<{
      /* General category of benefit */ category?: CodeableConcept;
      benefit?: Array<{
        /* Type of specific benefit */ type?: CodeableConcept;
        cost?: Array<{
          /* Type of cost */
          type?: CodeableConcept /* in-network | out-of-network | other */;
          applicability?: CodeableConcept;
          qualifiers?: Array<CodeableConcept> /* The actual cost value */;
          value?: Quantity;
        }>;
      }>;
    }>;
  }>;
  endpoint?: Array<Reference<"Endpoint">>;
  contact?: Array<{
    /* The type of contact */
    purpose?: CodeableConcept /* A name associated with the contact */;
    name?: HumanName;
    telecom?: Array<ContactPoint> /* Visiting or postal addresses for the contact */;
    address?: Address;
  }>;
}
/* Demographics and other administrative information about an individual or animal receiving care or other health-related services. */
interface Patient {
  _active?: Element;
  address?: Array<Address> /* Organization that is the custodian of the patient record */;
  managingOrganization?: Reference<"Organization">;
  name?: Array<HumanName>;
  _gender?: Element /* The date of birth for the individual */;
  birthDate?: date;
  _birthDate?: Element /* Whether patient is part of a multiple birth */;
  multipleBirth?:
    | integer
    | boolean /* Indicates if the individual is deceased or not */;
  deceased?: dateTime | boolean;
  photo?: Array<Attachment>;
  link?: Array<{
    /* The other patient or related person resource that the link refers to */
    other?: Reference<
      "Patient" | "RelatedPerson"
    > /* replaced-by | replaces | refer | seealso */;
    type?: code;
    _type?: Element;
  }> /* Whether this patient's record is in active use */;
  active?: boolean;
  communication?: Array<{
    /* The language which can be used to communicate with the patient about his or her health */
    language?: CodeableConcept /* Language preference indicator */;
    preferred?: boolean;
    _preferred?: Element;
  }>;
  identifier?: Array<Identifier>;
  telecom?: Array<ContactPoint>;
  generalPractitioner?: Array<
    Reference<"PractitionerRole" | "Organization" | "Practitioner">
  > /* male | female | other | unknown */;
  gender?: code /* Marital (civil) status of a patient */;
  maritalStatus?: CodeableConcept;
  contact?: Array<{
    relationship?: Array<CodeableConcept> /* A name associated with the contact person */;
    name?: HumanName;
    telecom?: Array<ContactPoint> /* Address for the contact person */;
    address?: Address /* male | female | other | unknown */;
    gender?: code;
    _gender?: Element /* Organization that is associated with the contact */;
    organization?: Reference<"Organization"> /* The period during which this contact person or organization is valid to be contacted relating to this patient */;
    period?: Period;
  }>;
}
/* The EffectEvidenceSynthesis resource describes the difference in an outcome between exposures states in a population where the effect estimate is derived from a combination of research studies. */
interface EffectEvidenceSynthesis {
  /* Natural language description of the effect evidence synthesis */
  description?: markdown /* What comparison exposure? */;
  exposureAlternative?: Reference<"EvidenceVariable"> /* Date last changed */;
  date?: dateTime;
  effectEstimate?: Array<{
    /* Description of effect estimate */ description?: string;
    _description?: Element /* Type of efffect estimate */;
    type?: CodeableConcept /* Variant exposure states */;
    variantState?: CodeableConcept /* Point estimate */;
    value?: decimal;
    _value?: Element /* What unit is the outcome described in? */;
    unitOfMeasure?: CodeableConcept;
    precisionEstimate?: Array<{
      /* Type of precision estimate */
      type?: CodeableConcept /* Level of confidence interval */;
      level?: decimal;
      _level?: Element /* Lower bound */;
      from?: decimal;
      _from?: Element /* Upper bound */;
      to?: decimal;
      _to?: Element;
    }>;
  }>;
  endorser?: Array<ContactDetail> /* Name of the publisher (organization or individual) */;
  publisher?: string /* When the effect evidence synthesis was approved by publisher */;
  approvalDate?: date;
  jurisdiction?: Array<CodeableConcept>;
  _publisher?: Element;
  _date?: Element /* What sample size was involved? */;
  sampleSize?: {
    /* Description of sample size */ description?: string;
    _description?: Element /* How many studies? */;
    numberOfStudies?: integer;
    _numberOfStudies?: Element /* How many participants? */;
    numberOfParticipants?: integer;
    _numberOfParticipants?: Element;
  } /* Name for this effect evidence synthesis (computer friendly) */;
  name?: string;
  _status?: Element;
  useContext?: Array<UsageContext> /* Use and/or publishing restrictions */;
  copyright?: markdown /* Type of study */;
  studyType?: CodeableConcept;
  _approvalDate?: Element /* What outcome? */;
  outcome?: Reference<"EvidenceVariable">;
  topic?: Array<CodeableConcept> /* Name for this effect evidence synthesis (human friendly) */;
  title?: string;
  _description?: Element;
  note?: Array<Annotation>;
  author?: Array<ContactDetail> /* Type of synthesis */;
  synthesisType?: CodeableConcept;
  _lastReviewDate?: Element /* draft | active | retired | unknown */;
  status?: code /* What population? */;
  population?: Reference<"EvidenceVariable">;
  _name?: Element /* Canonical identifier for this effect evidence synthesis, represented as a URI (globally unique) */;
  url?: uri;
  identifier?: Array<Identifier> /* When the effect evidence synthesis was last reviewed */;
  lastReviewDate?: date;
  editor?: Array<ContactDetail>;
  _copyright?: Element;
  _title?: Element;
  certainty?: Array<{
    rating?: Array<CodeableConcept>;
    note?: Array<Annotation>;
    certaintySubcomponent?: Array<{
      /* Type of subcomponent of certainty rating */ type?: CodeableConcept;
      rating?: Array<CodeableConcept>;
      note?: Array<Annotation>;
    }>;
  }>;
  reviewer?: Array<ContactDetail> /* What exposure? */;
  exposure?: Reference<"EvidenceVariable">;
  resultsByExposure?: Array<{
    /* Description of results by exposure */ description?: string;
    _description?: Element /* exposure | exposure-alternative */;
    exposureState?: code;
    _exposureState?: Element /* Variant exposure states */;
    variantState?: CodeableConcept /* Risk evidence synthesis */;
    riskEvidenceSynthesis?: Reference<"RiskEvidenceSynthesis">;
  }> /* Business version of the effect evidence synthesis */;
  version?: string;
  _version?: Element;
  relatedArtifact?: Array<RelatedArtifact>;
  contact?: Array<ContactDetail>;
  _url?: Element /* When the effect evidence synthesis is expected to be used */;
  effectivePeriod?: Period;
}
/* A physical entity which is the primary unit of operational and/or administrative interest in a study. */
interface ResearchSubject {
  /* Study subject is part of */ study?: Reference<"ResearchStudy">;
  _status?: Element;
  _assignedArm?: Element /* Agreement to participate in study */;
  consent?: Reference<"Consent"> /* What path should be followed */;
  assignedArm?: string /* candidate | eligible | follow-up | ineligible | not-registered | off-study | on-study | on-study-intervention | on-study-observation | pending-on-study | potential-candidate | screening | withdrawn */;
  status?: code /* What path was followed */;
  actualArm?: string;
  identifier?: Array<Identifier>;
  _actualArm?: Element /* Who is part of study */;
  individual?: Reference<"Patient"> /* Start and end of participation */;
  period?: Period;
}
/* This resource is primarily used for the identification and definition of a medication for the purposes of prescribing, dispensing, and administering a medication as well as for making statements about medication use. */
interface Medication {
  /* Amount of drug in package */ amount?: Ratio;
  _status?: Element /* Details about packaged medications */;
  batch?: {
    /* Identifier assigned to batch */ lotNumber?: string;
    _lotNumber?: Element /* When batch will expire */;
    expirationDate?: dateTime;
    _expirationDate?: Element;
  };
  ingredient?: Array<{
    /* The actual ingredient or content */
    item?:
      | CodeableConcept
      | Reference<"Medication" | "Substance"> /* Active ingredient indicator */;
    isActive?: boolean;
    _isActive?: Element /* Quantity of ingredient present */;
    strength?: Ratio;
  }> /* active | inactive | entered-in-error */;
  status?: code /* Codes that identify this medication */;
  code?: CodeableConcept;
  identifier?: Array<Identifier> /* Manufacturer of the item */;
  manufacturer?: Reference<"Organization"> /* powder | tablets | capsule + */;
  form?: CodeableConcept;
}
/* A statement of relationships from one set of concepts to one or more other concepts - either concepts in code systems, or data element/data element concepts, or classes in class models. */
interface ConceptMap {
  /* Natural language description of the concept map */
  description?: markdown /* Date last changed */;
  date?: dateTime;
  group?: Array<{
    _sourceVersion?: Element /* What to do when there is no mapping for the source concept */;
    unmapped?: {
      /* provided | fixed | other-map */ mode?: code;
      _mode?: Element /* Fixed code when mode = fixed */;
      code?: code;
      _code?: Element /* Display for the code */;
      display?: string;
      _display?: Element /* canonical reference to an additional ConceptMap to use for mapping if the source concept is unmapped */;
      url?: canonical;
      _url?: Element;
    };
    element?: Array<{
      /* Identifies element being mapped */ code?: code;
      _code?: Element /* Display for the code */;
      display?: string;
      _display?: Element;
      target?: Array<{
        _code?: Element;
        dependsOn?: Array<ConceptMap>;
        _equivalence?: Element;
        product?: Array<ConceptMap> /* Description of status/issues in mapping */;
        comment?: string /* Code that identifies the target element */;
        code?: code /* Display for the code */;
        display?: string /* relatedto | equivalent | equal | wider | subsumes | narrower | specializes | inexact | unmatched | disjoint */;
        equivalence?: code;
        _comment?: Element;
        _display?: Element;
      }>;
    }>;
    _targetVersion?: Element /* Specific version of the  code system */;
    targetVersion?: string /* Source system where concepts to be mapped are defined */;
    source?: uri /* Specific version of the  code system */;
    sourceVersion?: string;
    _target?: Element /* Target system that the concepts are to be mapped to */;
    target?: uri;
    _source?: Element;
  }> /* Name of the publisher (organization or individual) */;
  publisher?: string;
  jurisdiction?: Array<CodeableConcept>;
  _publisher?: Element /* Why this concept map is defined */;
  purpose?: markdown;
  _date?: Element /* Name for this concept map (computer friendly) */;
  name?: string;
  _status?: Element;
  _experimental?: Element;
  useContext?: Array<UsageContext> /* Use and/or publishing restrictions */;
  copyright?: markdown /* For testing purposes, not real usage */;
  experimental?: boolean /* The source value set that contains the concepts that are being mapped */;
  source?: canonical | uri /* Name for this concept map (human friendly) */;
  title?: string;
  _description?: Element;
  _purpose?: Element /* draft | active | retired | unknown */;
  status?: code;
  _name?: Element /* Canonical identifier for this concept map, represented as a URI (globally unique) */;
  url?: uri /* Additional identifier for the concept map */;
  identifier?: Identifier;
  _copyright?: Element;
  _title?: Element /* The target value set which provides context for the mappings */;
  target?: canonical | uri /* Business version of the concept map */;
  version?: string;
  _version?: Element;
  contact?: Array<ContactDetail>;
  _url?: Element;
}
/* The CoverageEligibilityRequest provides patient and insurance coverage information to an insurer for them to respond, in the form of an CoverageEligibilityResponse, with information regarding whether the stated coverage is valid and in-force and optionally to provide the insurance details of the policy. */
interface CoverageEligibilityRequest {
  _created?: Element /* Intended recipient of products and services */;
  patient?: Reference<"Patient">;
  insurance?: Array<{
    /* Applicable coverage */ focal?: boolean;
    _focal?: Element /* Insurance information */;
    coverage?: Reference<"Coverage"> /* Additional provider contract number */;
    businessArrangement?: string;
    _businessArrangement?: Element;
  }> /* Servicing facility */;
  facility?: Reference<"Location"> /* Author */;
  enterer?: Reference<"PractitionerRole" | "Practitioner">;
  supportingInfo?: Array<{
    /* Information instance identifier */ sequence?: positiveInt;
    _sequence?: Element /* Data to be provided */;
    information?: Reference<ResourceType> /* Applies to all items */;
    appliesToAll?: boolean;
    _appliesToAll?: Element;
  }>;
  purpose?: Array<code>;
  item?: Array<{
    /* Benefit classification */
    category?: CodeableConcept /* Servicing facility */;
    facility?: Reference<"Organization" | "Location">;
    diagnosis?: Array<{
      /* Nature of illness or problem */
      diagnosis?: CodeableConcept | Reference<"Condition">;
    }>;
    modifier?: Array<CodeableConcept>;
    _supportingInfoSequence?: Array<Element> /* Billing, service, product, or drug code */;
    productOrService?: CodeableConcept /* Count of products or services */;
    quantity?: Quantity /* Perfoming practitioner */;
    provider?: Reference<"PractitionerRole" | "Practitioner">;
    supportingInfoSequence?: Array<positiveInt> /* Fee, charge or cost per item */;
    unitPrice?: Money;
    detail?: Array<Reference<ResourceType>>;
  }>;
  _status?: Element /* Creation date */;
  created?: dateTime /* Estimated date or dates of service */;
  serviced?: date | Period;
  _purpose?: Array<Element> /* Coverage issuer */;
  insurer?: Reference<"Organization"> /* Desired processing priority */;
  priority?: CodeableConcept /* active | cancelled | draft | entered-in-error */;
  status?: code;
  identifier?: Array<Identifier> /* Party responsible for the request */;
  provider?: Reference<"PractitionerRole" | "Organization" | "Practitioner">;
}
/* Source material shall capture information on the taxonomic and anatomical origins as well as the fraction of a material that can result in or can be modified to form a substance. This set of data elements shall be used to define polymer substances isolated from biological matrices. Taxonomic and anatomical origins shall be described using a controlled vocabulary as required. This information is captured for naturally derived polymers ( . starch) and structurally diverse substances. For Organisms belonging to the Kingdom Plantae the Substance level defines the fresh material of a single species or infraspecies, the Herbal Drug and the Herbal preparation. For Herbal preparations, the fraction information will be captured at the Substance information level and additional information for herbal extracts will be captured at the Specified Substance Group 1 information level. See for further explanation the Substance Class: Structurally Diverse and the herbal annex. */
interface SubstanceSourceMaterial {
  parentSubstanceName?: Array<string> /* This subclause describes the organism which the substance is derived from. For vaccines, the parent organism shall be specified based on these subclause elements. As an example, full taxonomy will be described for the Substance Name: ., Leaf */;
  organism?: {
    /* The Intraspecific type of an organism shall be specified */
    intraspecificType?: CodeableConcept /* 4.9.13.8.1 Hybrid species maternal organism ID (Optional) */;
    hybrid?: {
      /* The hybrid type of an organism shall be specified */
      hybridType?: CodeableConcept;
      _paternalOrganismName?: Element;
      _paternalOrganismId?: Element;
      _maternalOrganismName?: Element /* The identifier of the maternal species constituting the hybrid organism shall be specified based on a controlled vocabulary. For plants, the parents aren’t always known, and it is unlikely that it will be known which is maternal and which is paternal */;
      maternalOrganismId?: string;
      _maternalOrganismId?: Element /* The name of the paternal species constituting the hybrid organism shall be specified */;
      paternalOrganismName?: string /* The identifier of the paternal species constituting the hybrid organism shall be specified based on a controlled vocabulary */;
      paternalOrganismId?: string /* The name of the maternal species constituting the hybrid organism shall be specified. For plants, the parents aren’t always known, and it is unlikely that it will be known which is maternal and which is paternal */;
      maternalOrganismName?: string;
    } /* The family of an organism shall be specified */;
    family?: CodeableConcept /* 4.9.13.7.1 Kingdom (Conditional) */;
    organismGeneral?: {
      /* The kingdom of an organism shall be specified */
      kingdom?: CodeableConcept /* The phylum of an organism shall be specified */;
      phylum?: CodeableConcept /* The class of an organism shall be specified */;
      class?: CodeableConcept /* The order of an organism shall be specified, */;
      order?: CodeableConcept;
    } /* The intraspecific description of an organism shall be specified based on a controlled vocabulary. For Influenza Vaccine, the intraspecific description shall contain the syntax of the antigen in line with the WHO convention */;
    intraspecificDescription?: string;
    _intraspecificDescription?: Element /* The species of an organism shall be specified; refers to the Latin epithet of the species of the plant/animal; it is present in names for species and infraspecies */;
    species?: CodeableConcept;
    author?: Array<{
      /* The type of author of an organism species shall be specified. The parenthetical author of an organism species refers to the first author who published the plant/animal name (of any rank). The primary author of an organism species refers to the first author(s), who validly published the plant/animal name */
      authorType?: CodeableConcept /* The author of an organism species shall be specified. The author year of an organism shall also be specified when applicable; refers to the year in which the first author(s) published the infraspecific plant/animal name (of any rank) */;
      authorDescription?: string;
      _authorDescription?: Element;
    }> /* The genus of an organism shall be specified; refers to the Latin epithet of the genus element of the plant/animal scientific name; it is present in names for genera, species and infraspecies */;
    genus?: CodeableConcept;
  };
  partDescription?: Array<{
    /* Entity of anatomical origin of source material within an organism */
    part?: CodeableConcept /* The detailed anatomic location when the part can be extracted from different anatomical locations of the organism. Multiple alternative locations may apply */;
    partLocation?: CodeableConcept;
  }> /* Stage of life for animals, plants, insects and microorganisms. This information shall be provided only when the substance is significantly different in these stages (e.g. foetal bovine serum) */;
  developmentStage?: CodeableConcept;
  fractionDescription?: Array<{
    /* This element is capturing information about the fraction of a plant part, or human plasma for fractionation */
    fraction?: string;
    _fraction?: Element /* The specific type of the material constituting the component. For Herbal preparations the particulars of the extracts (liquid/dry) is described in Specified Substance Group 1 */;
    materialType?: CodeableConcept;
  }>;
  _organismName?: Element /* The state of the source material when extracted */;
  sourceMaterialState?: CodeableConcept;
  countryOfOrigin?: Array<CodeableConcept>;
  _geographicalLocation?: Array<Element> /* The type of the source material shall be specified based on a controlled vocabulary. For vaccines, this subclause refers to the class of infectious agent */;
  sourceMaterialType?: CodeableConcept /* The unique identifier associated with the source material parent organism shall be specified */;
  organismId?: Identifier /* The organism accepted Scientific name shall be provided based on the organism taxonomy */;
  organismName?: string;
  parentSubstanceId?: Array<Identifier>;
  geographicalLocation?: Array<string>;
  _parentSubstanceName?: Array<Element> /* General high level classification of the source material specific to the origin of the material */;
  sourceMaterialClass?: CodeableConcept;
}
/* An authorization for the provision of glasses and/or contact lenses to a patient. */
interface VisionPrescription {
  _created?: Element /* Who prescription is for */;
  patient?: Reference<"Patient"> /* Created during encounter / admission / stay */;
  encounter?: Reference<"Encounter">;
  _status?: Element /* Response creation date */;
  created?: dateTime /* active | cancelled | draft | entered-in-error */;
  status?: code;
  identifier?: Array<Identifier> /* Who authorized the vision prescription */;
  prescriber?: Reference<
    "PractitionerRole" | "Practitioner"
  > /* When prescription was authorized */;
  dateWritten?: dateTime;
  _dateWritten?: Element;
  lensSpecification?: Array<{
    /* Power of the lens */ sphere?: decimal;
    _eye?: Element;
    _color?: Element /* Color required */;
    color?: string /* right | left */;
    eye?: code;
    _add?: Element /* Contact lens diameter */;
    diameter?: decimal;
    _axis?: Element /* Lens wear duration */;
    duration?: Quantity;
    _diameter?: Element /* Brand required */;
    brand?: string;
    _cylinder?: Element;
    note?: Array<Annotation> /* Contact lens power */;
    power?: decimal /* Product to be supplied */;
    product?: CodeableConcept /* Lens power for astigmatism */;
    cylinder?: decimal;
    _sphere?: Element;
    _brand?: Element;
    _power?: Element;
    prism?: Array<{
      /* Amount of adjustment */ amount?: decimal;
      _amount?: Element /* up | down | in | out */;
      base?: code;
      _base?: Element;
    }> /* Lens meridian which contain no power for astigmatism */;
    axis?: integer /* Added power for multifocal levels */;
    add?: decimal;
    _backCurve?: Element /* Contact lens back curvature */;
    backCurve?: decimal;
  }>;
}
/* Raw data describing a biological sequence. */
interface MolecularSequence {
  /* Who and/or what this is about */ patient?: Reference<"Patient">;
  structureVariant?: Array<{
    /* Structural variant change type */
    variantType?: CodeableConcept /* Does the structural variant have base pair resolution breakpoints? */;
    exact?: boolean;
    _exact?: Element /* Structural variant length */;
    length?: integer;
    _length?: Element /* Structural variant outer */;
    outer?: {
      /* Structural variant outer start */ start?: integer;
      _start?: Element /* Structural variant outer end */;
      end?: integer;
      _end?: Element;
    } /* Structural variant inner */;
    inner?: {
      /* Structural variant inner start */ start?: integer;
      _start?: Element /* Structural variant inner end */;
      end?: integer;
      _end?: Element;
    };
  }>;
  repository?: Array<{
    /* Id of the read */ readsetId?: string /* Repository's name */;
    name?: string;
    _type?: Element /* directlink | openapi | login | oauth | other */;
    type?: code;
    _datasetId?: Element /* Id of the dataset that used to call for dataset in repository */;
    datasetId?: string;
    _variantsetId?: Element /* Id of the variantset that used to call for variantset in repository */;
    variantsetId?: string;
    _name?: Element /* URI of the repository */;
    url?: uri;
    _readsetId?: Element;
    _url?: Element;
  }>;
  variant?: Array<{
    /* Pointer to observed variant information */
    variantPointer?: Reference<"Observation"> /* Allele in the reference sequence */;
    referenceAllele?: string;
    _end?: Element /* Start position of the variant on the  reference sequence */;
    start?: integer;
    _observedAllele?: Element;
    _referenceAllele?: Element /* Allele that was observed */;
    observedAllele?: string;
    _cigar?: Element /* End position of the variant on the reference sequence */;
    end?: integer;
    _start?: Element /* Extended CIGAR string for aligning the sequence with reference bases */;
    cigar?: string;
  }> /* Specimen used for sequencing */;
  specimen?: Reference<"Specimen">;
  _type?: Element /* aa | dna | rna */;
  type?: code;
  _coordinateSystem?: Element;
  _observedSeq?: Element;
  _readCoverage?: Element;
  pointer?: Array<
    Reference<"MolecularSequence">
  > /* Sequence that was observed */;
  observedSeq?: string;
  identifier?: Array<Identifier>;
  quality?: Array<{
    /* True positives from the perspective of the truth data */
    truthTP?: decimal /* F-score */;
    fScore?: decimal;
    _fScore?: Element /* False negatives */;
    truthFN?: decimal /* False positives */;
    queryFP?: decimal /* Method to get quality */;
    method?: CodeableConcept;
    _end?: Element /* Precision of comparison */;
    precision?: decimal;
    _type?: Element /* Start position of the sequence */;
    start?: integer /* True positives from the perspective of the query data */;
    queryTP?: decimal /* indel | snp | unknown */;
    type?: code;
    _truthFN?: Element;
    _gtFP?: Element /* Recall of comparison */;
    recall?: decimal;
    _truthTP?: Element;
    _recall?: Element /* Receiver Operator Characteristic (ROC) Curve */;
    roc?: {
      _numFN?: Array<Element>;
      _numTP?: Array<Element>;
      sensitivity?: Array<decimal>;
      precision?: Array<decimal>;
      _fMeasure?: Array<Element>;
      _score?: Array<Element>;
      numFN?: Array<integer>;
      _sensitivity?: Array<Element>;
      numFP?: Array<integer>;
      score?: Array<integer>;
      _precision?: Array<Element>;
      _numFP?: Array<Element>;
      fMeasure?: Array<decimal>;
      numTP?: Array<integer>;
    } /* Quality score for the comparison */;
    score?: Quantity;
    _precision?: Element;
    _queryTP?: Element /* End position of the sequence */;
    end?: integer;
    _queryFP?: Element;
    _start?: Element /* Standard sequence for comparison */;
    standardSequence?: CodeableConcept /* False positives where the non-REF alleles in the Truth and Query Call Sets match */;
    gtFP?: decimal;
  }> /* The method for sequencing */;
  device?: Reference<"Device"> /* The number of copies of the sequence of interest.  (RNASeq) */;
  quantity?: Quantity /* Base number of coordinate system (0 for 0-based numbering or coordinates, inclusive start, exclusive end, 1 for 1-based numbering, inclusive start, inclusive end) */;
  coordinateSystem?: integer /* A sequence used as reference */;
  referenceSeq?: {
    /* Chromosome containing genetic finding */
    chromosome?: CodeableConcept /* Reference identifier */;
    referenceSeqId?: CodeableConcept;
    _referenceSeqString?: Element /* End position of the window on the reference sequence */;
    windowEnd?: integer;
    _genomeBuild?: Element /* watson | crick */;
    strand?: code /* The Genome Build used for reference, following GRCh build versions e.g. 'GRCh 37' */;
    genomeBuild?: string /* sense | antisense */;
    orientation?: code;
    _strand?: Element /* A pointer to another MolecularSequence entity as reference sequence */;
    referenceSeqPointer?: Reference<"MolecularSequence">;
    _windowStart?: Element;
    _orientation?: Element /* A string to represent reference sequence */;
    referenceSeqString?: string;
    _windowEnd?: Element /* Start position of the window on the  reference sequence */;
    windowStart?: integer;
  } /* Who should be responsible for test result */;
  performer?: Reference<"Organization"> /* Average number of reads representing a given nucleotide in the reconstructed sequence */;
  readCoverage?: integer;
}
/* Describe the undesirable effects of the medicinal product. */
interface MedicinalProductUndesirableEffect {
  subject?: Array<
    Reference<"Medication" | "MedicinalProduct">
  > /* The symptom, condition or undesirable effect */;
  symptomConditionEffect?: CodeableConcept /* Classification of the effect */;
  classification?: CodeableConcept /* The frequency of occurrence of the effect */;
  frequencyOfOccurrence?: CodeableConcept;
  population?: Array<Population>;
}
/* A collection of error, warning, or information messages that result from a system action. */
interface OperationOutcome {
  issue?: Array<{
    _code?: Element /* Additional diagnostic information about the issue */;
    diagnostics?: string;
    expression?: Array<string>;
    _location?: Array<Element>;
    _expression?: Array<Element> /* Additional details about the error */;
    details?: CodeableConcept /* fatal | error | warning | information */;
    severity?: code /* Error or warning code */;
    code?: code;
    _severity?: Element;
    location?: Array<string>;
    _diagnostics?: Element;
  }>;
}
/* The header for a message exchange that is either requesting or responding to an action.  The reference(s) that are the subject of the action as well as other information related to the action are typically transmitted in a bundle in which the MessageHeader resource instance is the first resource in the bundle. */
interface MessageHeader {
  /* If this is a reply to prior message */
  response?: {
    /* Id of original message */ identifier?: id;
    _identifier?: Element /* ok | transient-error | fatal-error */;
    code?: code;
    _code?: Element /* Specific list of hints/warnings/errors */;
    details?: Reference<"OperationOutcome">;
  } /* Link to the definition for this message */;
  definition?: canonical /* The source of the data entry */;
  enterer?: Reference<"PractitionerRole" | "Practitioner">;
  _definition?: Element /* Message source application */;
  source?: {
    /* Name of system */ name?: string;
    _endpoint?: Element;
    _name?: Element;
    _software?: Element /* Name of software running the system */;
    software?: string /* Version of software running */;
    version?: string;
    _version?: Element /* Actual message source address or id */;
    endpoint?: url /* Human contact for problems */;
    contact?: ContactPoint;
  } /* The source of the decision */;
  author?: Reference<"PractitionerRole" | "Practitioner"> /* Cause of event */;
  reason?: CodeableConcept /* Final responsibility for event */;
  responsible?: Reference<
    "PractitionerRole" | "Organization" | "Practitioner"
  > /* Code for the event this message represents or link to event definition */;
  event?: Coding | uri /* Real world sender of the message */;
  sender?: Reference<"PractitionerRole" | "Organization" | "Practitioner">;
  focus?: Array<Reference<ResourceType>>;
  destination?: Array<{
    /* Name of system */ name?: string;
    _name?: Element /* Particular delivery destination within the destination */;
    target?: Reference<"Device"> /* Actual destination address or id */;
    endpoint?: url;
    _endpoint?: Element /* Intended "real-world" recipient for the data */;
    receiver?: Reference<"PractitionerRole" | "Organization" | "Practitioner">;
  }>;
}
/* Risk of harmful or undesirable, physiological response which is unique to an individual and associated with exposure to a substance. */
interface AllergyIntolerance {
  /* Who the sensitivity is for */
  patient?: Reference<"Patient"> /* When allergy or intolerance was identified */;
  onset?: string | dateTime | Range | Period | Age;
  category?: Array<code> /* low | high | unable-to-assess */;
  criticality?: code /* active | inactive | resolved */;
  clinicalStatus?: CodeableConcept /* Encounter when the allergy or intolerance was asserted */;
  encounter?: Reference<"Encounter">;
  _type?: Element /* allergy | intolerance - Underlying mechanism (if known) */;
  type?: code /* Source of the information about the allergy */;
  asserter?: Reference<
    "Patient" | "PractitionerRole" | "Practitioner" | "RelatedPerson"
  >;
  note?: Array<Annotation> /* Date first version of the resource instance was recorded */;
  recordedDate?: dateTime;
  _recordedDate?: Element /* Who recorded the sensitivity */;
  recorder?: Reference<
    "Patient" | "PractitionerRole" | "Practitioner" | "RelatedPerson"
  > /* Code that identifies the allergy or intolerance */;
  code?: CodeableConcept;
  identifier?: Array<Identifier>;
  _criticality?: Element;
  _category?: Array<Element> /* Date(/time) of last known occurrence of a reaction */;
  lastOccurrence?: dateTime /* unconfirmed | confirmed | refuted | entered-in-error */;
  verificationStatus?: CodeableConcept;
  reaction?: Array<{
    /* Description of the event as a whole */
    description?: string /* Date(/time) when manifestations showed */;
    onset?: dateTime;
    _onset?: Element;
    manifestation?: Array<CodeableConcept> /* How the subject was exposed to the substance */;
    exposureRoute?: CodeableConcept;
    _description?: Element;
    note?: Array<Annotation> /* Specific substance or pharmaceutical product considered to be responsible for event */;
    substance?: CodeableConcept /* mild | moderate | severe (of event as a whole) */;
    severity?: code;
    _severity?: Element;
  }>;
  _lastOccurrence?: Element;
}
/* Todo. */
interface SubstanceReferenceInformation {
  /* Todo */ comment?: string;
  _comment?: Element;
  gene?: Array<{
    /* Todo */ geneSequenceOrigin?: CodeableConcept /* Todo */;
    gene?: CodeableConcept;
    source?: Array<Reference<"DocumentReference">>;
  }>;
  geneElement?: Array<{
    /* Todo */ type?: CodeableConcept /* Todo */;
    element?: Identifier;
    source?: Array<Reference<"DocumentReference">>;
  }>;
  classification?: Array<{
    /* Todo */ domain?: CodeableConcept /* Todo */;
    classification?: CodeableConcept;
    subtype?: Array<CodeableConcept>;
    source?: Array<Reference<"DocumentReference">>;
  }>;
  target?: Array<{
    /* Todo */ target?: Identifier /* Todo */;
    type?: CodeableConcept /* Todo */;
    interaction?: CodeableConcept /* Todo */;
    organism?: CodeableConcept /* Todo */;
    organismType?: CodeableConcept /* Todo */;
    amount?: string | Range | Quantity /* Todo */;
    amountType?: CodeableConcept;
    source?: Array<Reference<"DocumentReference">>;
  }>;
}
/* Record of delivery of what is supplied. */
interface SupplyDelivery {
  /* Patient for whom the item is supplied */
  patient?: Reference<"Patient"> /* Dispenser */;
  supplier?: Reference<
    "PractitionerRole" | "Organization" | "Practitioner"
  > /* The item that is delivered or supplied */;
  suppliedItem?: {
    /* Amount dispensed */
    quantity?: Quantity /* Medication, Substance, or Device supplied */;
    item?: CodeableConcept | Reference<"Medication" | "Device" | "Substance">;
  };
  _status?: Element /* Category of dispense event */;
  type?: CodeableConcept /* in-progress | completed | abandoned | entered-in-error */;
  status?: code;
  identifier?: Array<Identifier>;
  basedOn?: Array<Reference<"SupplyRequest">>;
  partOf?: Array<Reference<"Contract" | "SupplyDelivery">>;
  receiver?: Array<
    Reference<"PractitionerRole" | "Practitioner">
  > /* Where the Supply was sent */;
  destination?: Reference<"Location"> /* When event occurred */;
  occurrence?: dateTime | Period | Timing;
}
/* An association between a patient and an organization / healthcare provider(s) during which time encounters may occur. The managing organization assumes a level of responsibility for the patient during this time. */
interface EpisodeOfCare {
  /* The patient who is the focus of this episode of care */
  patient?: Reference<"Patient">;
  diagnosis?: Array<{
    /* Conditions/problems/diagnoses this episode of care is for */
    condition?: Reference<"Condition"> /* Role that this diagnosis has within the episode of care (e.g. admission, billing, discharge …) */;
    role?: CodeableConcept /* Ranking of the diagnosis (for each role type) */;
    rank?: positiveInt;
    _rank?: Element;
  }> /* Organization that assumes care */;
  managingOrganization?: Reference<"Organization">;
  _status?: Element;
  type?: Array<CodeableConcept>;
  account?: Array<Reference<"Account">>;
  referralRequest?: Array<Reference<"ServiceRequest">>;
  team?: Array<
    Reference<"CareTeam">
  > /* planned | waitlist | active | onhold | finished | cancelled | entered-in-error */;
  status?: code;
  identifier?: Array<Identifier> /* Interval during responsibility is assumed */;
  period?: Period /* Care manager/care coordinator for the patient */;
  careManager?: Reference<"PractitionerRole" | "Practitioner">;
  statusHistory?: Array<{
    /* planned | waitlist | active | onhold | finished | cancelled | entered-in-error */
    status?: code;
    _status?: Element /* Duration the EpisodeOfCare was in the specified status */;
    period?: Period;
  }>;
}
/* A specific set of Roles/Locations/specialties/services that a practitioner may perform at an organization for a period of time. */
interface PractitionerRole {
  _active?: Element;
  availableTime?: Array<{
    daysOfWeek?: Array<code>;
    _daysOfWeek?: Array<Element> /* Always available? e.g. 24 hour service */;
    allDay?: boolean;
    _allDay?: Element /* Opening time of day (ignored if allDay = true) */;
    availableStartTime?: time;
    _availableStartTime?: Element /* Closing time of day (ignored if allDay = true) */;
    availableEndTime?: time;
    _availableEndTime?: Element;
  }>;
  specialty?: Array<CodeableConcept>;
  notAvailable?: Array<{
    /* Reason presented to the user explaining why time not available */
    description?: string;
    _description?: Element /* Service not available from this date */;
    during?: Period;
  }> /* Organization where the roles are available */;
  organization?: Reference<"Organization"> /* Whether this practitioner role record is in active use */;
  active?: boolean;
  _availabilityExceptions?: Element;
  code?: Array<CodeableConcept>;
  identifier?: Array<Identifier> /* Description of availability exceptions */;
  availabilityExceptions?: string /* Practitioner that is able to provide the defined services for the organization */;
  practitioner?: Reference<"Practitioner">;
  telecom?: Array<ContactPoint> /* The period during which the practitioner is authorized to perform in these role(s) */;
  period?: Period;
  location?: Array<Reference<"Location">>;
  endpoint?: Array<Reference<"Endpoint">>;
  healthcareService?: Array<Reference<"HealthcareService">>;
}
/* The Library resource is a general-purpose container for knowledge asset definitions. It can be used to describe and expose existing knowledge assets such as logic libraries and information model descriptions, as well as to describe a collection of knowledge assets. */
interface Library {
  /* Natural language description of the library */ description?: markdown;
  _usage?: Element /* Date last changed */;
  date?: dateTime;
  dataRequirement?: Array<DataRequirement>;
  endorser?: Array<ContactDetail> /* Name of the publisher (organization or individual) */;
  publisher?: string /* When the library was approved by publisher */;
  approvalDate?: date;
  jurisdiction?: Array<CodeableConcept>;
  _publisher?: Element /* Why this library is defined */;
  purpose?: markdown;
  content?: Array<Attachment>;
  _date?: Element /* Name for this library (computer friendly) */;
  name?: string;
  _status?: Element;
  _experimental?: Element;
  useContext?: Array<UsageContext> /* Use and/or publishing restrictions */;
  copyright?: markdown;
  _approvalDate?: Element /* logic-library | model-definition | asset-collection | module-definition */;
  type?: CodeableConcept /* For testing purposes, not real usage */;
  experimental?: boolean;
  topic?: Array<CodeableConcept> /* Name for this library (human friendly) */;
  title?: string;
  _description?: Element;
  author?: Array<ContactDetail>;
  _purpose?: Element /* Describes the clinical usage of the library */;
  usage?: string;
  _lastReviewDate?: Element /* draft | active | retired | unknown */;
  status?: code /* Subordinate title of the library */;
  subtitle?: string;
  _name?: Element /* Canonical identifier for this library, represented as a URI (globally unique) */;
  url?: uri;
  identifier?: Array<Identifier> /* When the library was last reviewed */;
  lastReviewDate?: date;
  editor?: Array<ContactDetail>;
  _subtitle?: Element;
  _copyright?: Element;
  _title?: Element;
  reviewer?: Array<ContactDetail> /* Business version of the library */;
  version?: string;
  _version?: Element;
  relatedArtifact?: Array<RelatedArtifact>;
  contact?: Array<ContactDetail> /* Type of individual the library content is focused on */;
  subject?: CodeableConcept | Reference<"Group">;
  parameter?: Array<ParameterDefinition>;
  _url?: Element /* When the library is expected to be used */;
  effectivePeriod?: Period;
}
/* A person who is directly or indirectly involved in the provisioning of healthcare. */
interface Practitioner {
  _active?: Element;
  address?: Array<Address>;
  name?: Array<HumanName>;
  _gender?: Element /* The date  on which the practitioner was born */;
  birthDate?: date;
  _birthDate?: Element;
  photo?: Array<Attachment> /* Whether this practitioner's record is in active use */;
  active?: boolean;
  communication?: Array<CodeableConcept>;
  identifier?: Array<Identifier>;
  qualification?: Array<{
    identifier?: Array<Identifier> /* Coded representation of the qualification */;
    code?: CodeableConcept /* Period during which the qualification is valid */;
    period?: Period /* Organization that regulates and issues the qualification */;
    issuer?: Reference<"Organization">;
  }>;
  telecom?: Array<ContactPoint> /* male | female | other | unknown */;
  gender?: code;
}
/* An order or request for both supply of the medication and the instructions for administration of the medication to a patient. The resource is called "MedicationRequest" rather than "MedicationPrescription" or "MedicationOrder" to generalize the use across inpatient and outpatient settings, including care plans, etc., and to harmonize with workflow patterns. */
interface MedicationRequest {
  /* Desired kind of performer of the medication administration */
  performerType?: CodeableConcept;
  category?: Array<CodeableConcept>;
  insurance?: Array<Reference<"ClaimResponse" | "Coverage">>;
  instantiatesCanonical?: Array<canonical>;
  eventHistory?: Array<Reference<"Provenance">>;
  instantiatesUri?: Array<uri> /* Any restrictions on medication substitution */;
  substitution?: {
    /* Whether substitution is allowed or not */
    allowed?:
      | CodeableConcept
      | boolean /* Why should (not) substitution be made */;
    reason?: CodeableConcept;
  };
  _authoredOn?: Element;
  detectedIssue?: Array<
    Reference<"DetectedIssue">
  > /* Encounter created as part of encounter/admission/stay */;
  encounter?: Reference<"Encounter">;
  _doNotPerform?: Element /* Medication supply authorization */;
  dispenseRequest?: {
    /* First fill details */
    initialFill?: {
      /* First fill quantity */ quantity?: Quantity /* First fill duration */;
      duration?: Duration;
    } /* Minimum period of time between dispenses */;
    dispenseInterval?: Duration /* Time period supply is authorized for */;
    validityPeriod?: Period /* Number of refills authorized */;
    numberOfRepeatsAllowed?: unsignedInt;
    _numberOfRepeatsAllowed?: Element /* Amount of medication to supply per dispense */;
    quantity?: Quantity /* Number of days supply per dispense */;
    expectedSupplyDuration?: Duration /* Intended dispenser */;
    performer?: Reference<"Organization">;
  };
  _priority?: Element;
  _status?: Element /* Reported rather than primary record */;
  reported?:
    | boolean
    | Reference<
        | "Patient"
        | "PractitionerRole"
        | "Organization"
        | "Practitioner"
        | "RelatedPerson"
      >;
  reasonCode?: Array<CodeableConcept> /* Reason for current status */;
  statusReason?: CodeableConcept /* When request was initially authored */;
  authoredOn?: dateTime;
  note?: Array<Annotation>;
  _intent?: Element /* Who/What requested the Request */;
  requester?: Reference<
    | "Patient"
    | "PractitionerRole"
    | "Organization"
    | "Device"
    | "Practitioner"
    | "RelatedPerson"
  >;
  supportingInformation?: Array<
    Reference<ResourceType>
  > /* routine | urgent | asap | stat */;
  priority?: code /* active | on-hold | cancelled | completed | entered-in-error | stopped | draft | unknown */;
  status?: code;
  dosageInstruction?: Array<Dosage> /* Composite request this is part of */;
  groupIdentifier?: Identifier /* Person who entered the request */;
  recorder?: Reference<"PractitionerRole" | "Practitioner">;
  identifier?: Array<Identifier> /* True if request is prohibiting action */;
  doNotPerform?: boolean /* proposal | plan | order | original-order | reflex-order | filler-order | instance-order | option */;
  intent?: code;
  basedOn?: Array<
    Reference<
      | "ServiceRequest"
      | "CarePlan"
      | "ImmunizationRecommendation"
      | "MedicationRequest"
    >
  > /* An order/prescription that is being replaced */;
  priorPrescription?: Reference<"MedicationRequest"> /* Overall pattern of medication administration */;
  courseOfTherapyType?: CodeableConcept;
  _instantiatesCanonical?: Array<Element> /* Who or group medication request is for */;
  subject?: Reference<
    "Patient" | "Group"
  > /* Intended performer of administration */;
  performer?: Reference<
    | "CareTeam"
    | "Patient"
    | "PractitionerRole"
    | "Organization"
    | "Device"
    | "Practitioner"
    | "RelatedPerson"
  > /* Medication to be taken */;
  medication?: CodeableConcept | Reference<"Medication">;
  reasonReference?: Array<Reference<"Observation" | "Condition">>;
  _instantiatesUri?: Array<Element>;
}
/* A patient's point-in-time set of recommendations (i.e. forecasting) according to a published schedule with optional supporting justification. */
interface ImmunizationRecommendation {
  identifier?: Array<Identifier> /* Who this profile is for */;
  patient?: Reference<"Patient"> /* Date recommendation(s) created */;
  date?: dateTime;
  _date?: Element /* Who is responsible for protocol */;
  authority?: Reference<"Organization">;
  recommendation?: Array<{
    /* Protocol details */ description?: string;
    contraindicatedVaccineCode?: Array<CodeableConcept> /* Name of vaccination series */;
    series?: string /* Recommended dose number within series */;
    doseNumber?: string | positiveInt;
    vaccineCode?: Array<CodeableConcept>;
    _description?: Element /* Recommended number of doses for immunity */;
    seriesDoses?: string | positiveInt /* Vaccine recommendation status */;
    forecastStatus?: CodeableConcept;
    forecastReason?: Array<CodeableConcept>;
    dateCriterion?: Array<{
      /* Type of date */ code?: CodeableConcept /* Recommended date */;
      value?: dateTime;
      _value?: Element;
    }> /* Disease to be immunized against */;
    targetDisease?: CodeableConcept;
    _series?: Element;
    supportingImmunization?: Array<
      Reference<"ImmunizationEvaluation" | "Immunization">
    >;
    supportingPatientInformation?: Array<Reference<ResourceType>>;
  }>;
}
/* Describes the event of a patient being administered a vaccine or a record of an immunization as reported by a patient, a clinician or another party. */
interface Immunization {
  /* Who was immunized */ patient?: Reference<"Patient"> /* Dose potency */;
  isSubpotent?: boolean /* Indicates the source of a secondarily reported record */;
  reportOrigin?: CodeableConcept;
  protocolApplied?: Array<{
    /* Name of vaccine series */ series?: string;
    _series?: Element /* Who is responsible for publishing the recommendations */;
    authority?: Reference<"Organization">;
    targetDisease?: Array<CodeableConcept> /* Dose number within series */;
    doseNumber?:
      | string
      | positiveInt /* Recommended number of doses for immunity */;
    seriesDoses?: string | positiveInt;
  }> /* Body site vaccine  was administered */;
  site?: CodeableConcept;
  _isSubpotent?: Element /* Encounter immunization was part of */;
  encounter?: Reference<"Encounter"> /* Vaccine product administered */;
  vaccineCode?: CodeableConcept /* Amount of vaccine administered */;
  doseQuantity?: Quantity;
  _status?: Element;
  reasonCode?: Array<CodeableConcept> /* Reason not done */;
  statusReason?: CodeableConcept /* How vaccine entered body */;
  route?: CodeableConcept /* When the immunization was first captured in the subject's record */;
  recorded?: dateTime;
  _expirationDate?: Element;
  programEligibility?: Array<CodeableConcept>;
  note?: Array<Annotation> /* Indicates context the data was recorded in */;
  primarySource?: boolean /* completed | entered-in-error | not-done */;
  status?: code /* Vaccine lot number */;
  lotNumber?: string;
  identifier?: Array<Identifier> /* Vaccine manufacturer */;
  manufacturer?: Reference<"Organization">;
  education?: Array<{
    /* Educational material document identifier */ documentType?: string;
    _documentType?: Element /* Educational material reference pointer */;
    reference?: uri;
    _reference?: Element /* Educational material publication date */;
    publicationDate?: dateTime;
    _publicationDate?: Element /* Educational material presentation date */;
    presentationDate?: dateTime;
    _presentationDate?: Element;
  }>;
  _primarySource?: Element;
  _recorded?: Element;
  _lotNumber?: Element;
  reaction?: Array<{
    /* When reaction started */ date?: dateTime;
    _date?: Element /* Additional information on reaction */;
    detail?: Reference<"Observation"> /* Indicates self-reported reaction */;
    reported?: boolean;
    _reported?: Element;
  }> /* Where immunization occurred */;
  location?: Reference<"Location"> /* Funding source for the vaccine */;
  fundingSource?: CodeableConcept;
  subpotentReason?: Array<CodeableConcept> /* Vaccine administration date */;
  occurrence?: string | dateTime /* Vaccine expiration date */;
  expirationDate?: date;
  performer?: Array<{
    /* What type of performance was done */
    function?: CodeableConcept /* Individual or organization who was performing */;
    actor?: Reference<"PractitionerRole" | "Organization" | "Practitioner">;
  }>;
  reasonReference?: Array<
    Reference<"Observation" | "DiagnosticReport" | "Condition">
  >;
}
/* A formal computable definition of a graph of resources - that is, a coherent set of resources that form a graph by following references. The Graph Definition resource defines a set and makes rules about the set. */
interface GraphDefinition {
  /* Natural language description of the graph definition */
  description?: markdown /* Date last changed */;
  date?: dateTime /* Name of the publisher (organization or individual) */;
  publisher?: string;
  jurisdiction?: Array<CodeableConcept>;
  _publisher?: Element /* Why this graph definition is defined */;
  purpose?: markdown;
  _date?: Element /* Name for this graph definition (computer friendly) */;
  name?: string;
  _status?: Element /* Type of resource at which the graph starts */;
  start?: code;
  _experimental?: Element;
  useContext?: Array<UsageContext> /* For testing purposes, not real usage */;
  experimental?: boolean;
  _description?: Element;
  _profile?: Element;
  _purpose?: Element /* draft | active | retired | unknown */;
  status?: code;
  link?: Array<GraphDefinition>;
  _name?: Element /* Canonical identifier for this graph definition, represented as a URI (globally unique) */;
  url?: uri /* Business version of the graph definition */;
  version?: string;
  _version?: Element;
  _start?: Element;
  contact?: Array<ContactDetail>;
  _url?: Element /* Profile on base resource */;
  profile?: canonical;
}
/* A financial tool for tracking value accrued for a particular purpose.  In the healthcare field, used to track charges for a patient, cost centers, etc. */
interface Account {
  /* Explanation of purpose/use */
  description?: string /* Human-readable label */;
  name?: string /* Transaction window */;
  servicePeriod?: Period;
  _status?: Element;
  coverage?: Array<{
    /* The party(s), such as insurances, that may contribute to the payment of this account */
    coverage?: Reference<"Coverage"> /* The priority of the coverage in the context of this account */;
    priority?: positiveInt;
    _priority?: Element;
  }> /* E.g. patient, expense, depreciation */;
  type?: CodeableConcept;
  _description?: Element;
  guarantor?: Array<{
    /* Responsible entity */
    party?: Reference<
      "Patient" | "Organization" | "RelatedPerson"
    > /* Credit or other hold applied */;
    onHold?: boolean;
    _onHold?: Element /* Guarantee account during */;
    period?: Period;
  }> /* active | inactive | entered-in-error | on-hold | unknown */;
  status?: code;
  _name?: Element;
  identifier?: Array<Identifier> /* Reference to a parent Account */;
  partOf?: Reference<"Account">;
  subject?: Array<
    Reference<
      | "Patient"
      | "PractitionerRole"
      | "HealthcareService"
      | "Organization"
      | "Device"
      | "Location"
      | "Practitioner"
    >
  > /* Entity managing the Account */;
  owner?: Reference<"Organization">;
}
/* An ingredient of a manufactured item or pharmaceutical product. */
interface MedicinalProductIngredient {
  /* Identifier for the ingredient */
  identifier?: Identifier /* Ingredient role e.g. Active ingredient, excipient */;
  role?: CodeableConcept /* If the ingredient is a known or suspected allergen */;
  allergenicIndicator?: boolean;
  _allergenicIndicator?: Element;
  manufacturer?: Array<Reference<"Organization">>;
  specifiedSubstance?: Array<{
    /* The specified substance */
    code?: CodeableConcept /* The group of specified substance, e.g. group 1 to 4 */;
    group?: CodeableConcept /* Confidentiality level of the specified substance as the ingredient */;
    confidentiality?: CodeableConcept;
    strength?: Array<MedicinalProductIngredient>;
  }> /* The ingredient substance */;
  substance?: {
    /* The ingredient substance */ code?: CodeableConcept;
    strength?: Array<MedicinalProductIngredient>;
  };
}
/* The MeasureReport resource contains the results of the calculation of a measure; and optionally a reference to the resources involved in that calculation. */
interface MeasureReport {
  evaluatedResource?: Array<Reference<ResourceType>>;
  _measure?: Element /* When the report was generated */;
  date?: dateTime;
  group?: Array<{
    /* Meaning of the group */ code?: CodeableConcept;
    population?: Array<{
      /* initial-population | numerator | numerator-exclusion | denominator | denominator-exclusion | denominator-exception | measure-population | measure-population-exclusion | measure-observation */
      code?: CodeableConcept /* Size of the population */;
      count?: integer;
      _count?: Element /* For subject-list reports, the subject results in this population */;
      subjectResults?: Reference<"List">;
    }> /* What score this group achieved */;
    measureScore?: Quantity;
    stratifier?: Array<{
      code?: Array<CodeableConcept>;
      stratum?: Array<{
        /* The stratum value, e.g. male */ value?: CodeableConcept;
        component?: Array<{
          /* What stratifier component of the group */
          code?: CodeableConcept /* The stratum component value, e.g. male */;
          value?: CodeableConcept;
        }>;
        population?: Array<{
          /* initial-population | numerator | numerator-exclusion | denominator | denominator-exclusion | denominator-exception | measure-population | measure-population-exclusion | measure-observation */
          code?: CodeableConcept /* Size of the population */;
          count?: integer;
          _count?: Element /* For subject-list reports, the subject results in this population */;
          subjectResults?: Reference<"List">;
        }> /* What score this stratum achieved */;
        measureScore?: Quantity;
      }>;
    }>;
  }>;
  _date?: Element;
  _type?: Element;
  _status?: Element /* individual | subject-list | summary | data-collection */;
  type?: code /* What measure was calculated */;
  measure?: canonical /* Who is reporting the data */;
  reporter?: Reference<
    "PractitionerRole" | "Organization" | "Location" | "Practitioner"
  > /* complete | pending | error */;
  status?: code;
  identifier?: Array<Identifier> /* What period the report covers */;
  period?: Period /* increase | decrease */;
  improvementNotation?: CodeableConcept /* What individual(s) the report is for */;
  subject?: Reference<
    | "Patient"
    | "PractitionerRole"
    | "Device"
    | "Location"
    | "Practitioner"
    | "RelatedPerson"
    | "Group"
  >;
}
/* Describes a measurement, calculation or setting capability of a medical device. */
interface DeviceMetric {
  /* measurement | setting | calculation | unspecified */ category?: code;
  _color?: Element /* Describes the measurement repetition time */;
  measurementPeriod?: Timing /* black | red | green | yellow | blue | magenta | cyan | white */;
  color?: code /* Describes the link to the parent Device */;
  parent?: Reference<"Device"> /* Unit of Measure for the Metric */;
  unit?: CodeableConcept /* Identity of metric, for example Heart Rate or PEEP Setting */;
  type?: CodeableConcept /* Describes the link to the source Device */;
  source?: Reference<"Device">;
  _operationalStatus?: Element;
  identifier?: Array<Identifier>;
  calibration?: Array<{
    /* unspecified | offset | gain | two-point */ type?: code;
    _type?: Element /* not-calibrated | calibration-required | calibrated | unspecified */;
    state?: code;
    _state?: Element /* Describes the time last calibration has been performed */;
    time?: instant;
    _time?: Element;
  }> /* on | off | standby | entered-in-error */;
  operationalStatus?: code;
  _category?: Element;
}
/* Describes the intended objective(s) for a patient, group or organization care, for example, weight loss, restoring an activity of daily living, obtaining herd immunity via immunization, meeting a process improvement objective, etc. */
interface Goal {
  /* Code or text describing goal */ description?: CodeableConcept;
  category?: Array<CodeableConcept>;
  addresses?: Array<
    Reference<
      | "RiskAssessment"
      | "Observation"
      | "NutritionOrder"
      | "ServiceRequest"
      | "Condition"
      | "MedicationStatement"
    >
  > /* Who's responsible for creating Goal? */;
  expressedBy?: Reference<
    "Patient" | "PractitionerRole" | "Practitioner" | "RelatedPerson"
  > /* When goal pursuit begins */;
  start?:
    | date
    | CodeableConcept /* in-progress | improving | worsening | no-change | achieved | sustaining | not-achieved | no-progress | not-attainable */;
  achievementStatus?: CodeableConcept /* Reason for current status */;
  statusReason?: string;
  _lifecycleStatus?: Element;
  note?: Array<Annotation> /* high-priority | medium-priority | low-priority */;
  priority?: CodeableConcept;
  outcomeCode?: Array<CodeableConcept>;
  identifier?: Array<Identifier> /* When goal status took effect */;
  statusDate?: date;
  target?: Array<{
    /* The parameter whose value is being tracked */
    measure?: CodeableConcept /* The target value to be achieved */;
    detail?:
      | string
      | Range
      | integer
      | Ratio
      | CodeableConcept
      | Quantity
      | boolean /* Reach goal on or before */;
    due?: date | Duration;
  }>;
  outcomeReference?: Array<
    Reference<"Observation">
  > /* Who this goal is intended for */;
  subject?: Reference<"Patient" | "Organization" | "Group">;
  _statusReason?: Element /* proposed | planned | accepted | active | on-hold | completed | cancelled | entered-in-error | rejected */;
  lifecycleStatus?: code;
  _statusDate?: Element;
}
/* Information about a medication that is used to support knowledge. */
interface MedicationKnowledge {
  /* The instructions for preparing the medication */
  preparationInstruction?: markdown /* Amount of drug in package */;
  amount?: Quantity;
  monograph?: Array<{
    /* The category of medication document */
    type?: CodeableConcept /* Associated documentation about the medication */;
    source?: Reference<"Media" | "DocumentReference">;
  }>;
  _preparationInstruction?: Element;
  regulatory?: Array<{
    /* Specifies the authority of the regulation */
    regulatoryAuthority?: Reference<"Organization">;
    substitution?: Array<{
      /* Specifies the type of substitution allowed */
      type?: CodeableConcept /* Specifies if regulation allows for changes in the medication when dispensing */;
      allowed?: boolean;
      _allowed?: Element;
    }>;
    schedule?: Array<{
      /* Specifies the specific drug schedule */ schedule?: CodeableConcept;
    }> /* The maximum number of units of the medication that can be dispensed in a period */;
    maxDispense?: {
      /* The maximum number of units of the medication that can be dispensed */
      quantity?: Quantity /* The period that applies to the maximum number of units */;
      period?: Duration;
    };
  }>;
  _synonym?: Array<Element> /* powder | tablets | capsule + */;
  doseForm?: CodeableConcept;
  intendedRoute?: Array<CodeableConcept>;
  drugCharacteristic?: Array<{
    /* Code specifying the type of characteristic of medication */
    type?: CodeableConcept /* Description of the characteristic */;
    value?: base64Binary | string | CodeableConcept | Quantity;
  }> /* Details about packaged medications */;
  packaging?: {
    /* A code that defines the specific type of packaging that the medication can be found in */
    type?: CodeableConcept /* The number of product units the package would contain if fully loaded */;
    quantity?: Quantity;
  };
  relatedMedicationKnowledge?: Array<{
    /* Category of medicationKnowledge */ type?: CodeableConcept;
    reference?: Array<Reference<"MedicationKnowledge">>;
  }>;
  medicineClassification?: Array<{
    /* The type of category for the medication (for example, therapeutic classification, therapeutic sub-classification) */
    type?: CodeableConcept;
    classification?: Array<CodeableConcept>;
  }>;
  _status?: Element;
  kinetics?: Array<{
    areaUnderCurve?: Array<Quantity>;
    lethalDose50?: Array<Quantity> /* Time required for concentration in the body to decrease by half */;
    halfLifePeriod?: Duration;
  }>;
  associatedMedication?: Array<Reference<"Medication">>;
  ingredient?: Array<{
    /* Medication(s) or substance(s) contained in the medication */
    item?:
      | CodeableConcept
      | Reference<"Substance"> /* Active ingredient indicator */;
    isActive?: boolean;
    _isActive?: Element /* Quantity of ingredient present */;
    strength?: Ratio;
  }>;
  monitoringProgram?: Array<{
    /* Type of program under which the medication is monitored */
    type?: CodeableConcept /* Name of the reviewing program */;
    name?: string;
    _name?: Element;
  }>;
  contraindication?: Array<
    Reference<"DetectedIssue">
  > /* active | inactive | entered-in-error */;
  status?: code;
  productType?: Array<CodeableConcept>;
  synonym?: Array<string> /* Code that identifies this medication */;
  code?: CodeableConcept;
  administrationGuidelines?: Array<{
    dosage?: Array<{
      /* Type of dosage */ type?: CodeableConcept;
      dosage?: Array<Dosage>;
    }> /* Indication for use that apply to the specific administration guidelines */;
    indication?: CodeableConcept | Reference<"ObservationDefinition">;
    patientCharacteristics?: Array<{
      /* Specific characteristic that is relevant to the administration guideline */
      characteristic?: CodeableConcept | Quantity;
      value?: Array<string>;
      _value?: Array<Element>;
    }>;
  }> /* Manufacturer of the item */;
  manufacturer?: Reference<"Organization">;
  cost?: Array<{
    /* The category of the cost information */
    type?: CodeableConcept /* The source or owner for the price information */;
    source?: string;
    _source?: Element /* The price of the medication */;
    cost?: Money;
  }>;
}
/* This resource provides the adjudication details from the processing of a Claim resource. */
interface ClaimResponse {
  _created?: Element /* The recipient of the products and services */;
  patient?: Reference<"Patient"> /* Party responsible for the claim */;
  requestor?: Reference<
    "PractitionerRole" | "Organization" | "Practitioner"
  > /* Party to be paid any benefits payable */;
  payeeType?: CodeableConcept;
  insurance?: Array<{
    /* Insurance instance identifier */ sequence?: positiveInt;
    _sequence?: Element /* Coverage to be used for adjudication */;
    focal?: boolean;
    _focal?: Element /* Insurance information */;
    coverage?: Reference<"Coverage"> /* Additional provider contract number */;
    businessArrangement?: string;
    _businessArrangement?: Element /* Adjudication results */;
    claimResponse?: Reference<"ClaimResponse">;
  }> /* Id of resource triggering adjudication */;
  request?: Reference<"Claim">;
  processNote?: Array<{
    /* Note instance identifier */ number?: positiveInt;
    _number?: Element /* display | print | printoper */;
    type?: code;
    _type?: Element /* Note explanatory text */;
    text?: string;
    _text?: Element /* Language of the text */;
    language?: CodeableConcept;
  }> /* Preauthorization reference */;
  preAuthRef?: string;
  adjudication?: Array<ClaimResponse>;
  _disposition?: Element /* claim | preauthorization | predetermination */;
  use?: code /* Payment Details */;
  payment?: {
    /* Partial or complete payment */
    type?: CodeableConcept /* Payment adjustment for non-claim issues */;
    adjustment?: Money /* Explanation for the adjustment */;
    adjustmentReason?: CodeableConcept /* Expected date of payment */;
    date?: date;
    _date?: Element /* Payable amount after adjustment */;
    amount?: Money /* Business identifier for the payment */;
    identifier?: Identifier;
  };
  item?: Array<{
    /* Claim item instance identifier */ itemSequence?: positiveInt;
    _itemSequence?: Element;
    noteNumber?: Array<positiveInt>;
    _noteNumber?: Array<Element>;
    adjudication?: Array<ClaimResponse>;
    detail?: Array<{
      /* Claim detail instance identifier */ detailSequence?: positiveInt;
      _detailSequence?: Element;
      noteNumber?: Array<positiveInt>;
      _noteNumber?: Array<Element>;
      adjudication?: Array<ClaimResponse>;
      subDetail?: Array<{
        /* Claim sub-detail instance identifier */
        subDetailSequence?: positiveInt;
        _subDetailSequence?: Element;
        noteNumber?: Array<positiveInt>;
        _noteNumber?: Array<Element>;
        adjudication?: Array<ClaimResponse>;
      }>;
    }>;
  }>;
  _status?: Element /* More granular claim type */;
  type?: CodeableConcept /* Response creation date */;
  created?: dateTime /* Preauthorization reference effective period */;
  preAuthPeriod?: Period /* queued | complete | error | partial */;
  outcome?: code;
  _outcome?: Element /* Disposition Message */;
  disposition?: string;
  communicationRequest?: Array<Reference<"CommunicationRequest">>;
  total?: Array<{
    /* Type of adjudication information */
    category?: CodeableConcept /* Financial total for the category */;
    amount?: Money;
  }> /* Party responsible for reimbursement */;
  insurer?: Reference<"Organization"> /* Funds reserved status */;
  fundsReserve?: CodeableConcept /* active | cancelled | draft | entered-in-error */;
  status?: code;
  identifier?: Array<Identifier>;
  error?: Array<{
    /* Item sequence number */ itemSequence?: positiveInt;
    _itemSequence?: Element /* Detail sequence number */;
    detailSequence?: positiveInt;
    _detailSequence?: Element /* Subdetail sequence number */;
    subDetailSequence?: positiveInt;
    _subDetailSequence?: Element /* Error code detailing processing issues */;
    code?: CodeableConcept;
  }> /* Printed reference or actual form */;
  form?: Attachment;
  _preAuthRef?: Element /* More granular claim type */;
  subType?: CodeableConcept /* Printed form identifier */;
  formCode?: CodeableConcept;
  addItem?: Array<{
    _subdetailSequence?: Array<Element>;
    _noteNumber?: Array<Element>;
    modifier?: Array<CodeableConcept>;
    adjudication?: Array<ClaimResponse>;
    subdetailSequence?: Array<positiveInt>;
    _factor?: Element;
    itemSequence?: Array<positiveInt> /* Total item cost */;
    net?: Money /* Date or dates of service or product delivery */;
    serviced?: date | Period;
    detailSequence?: Array<positiveInt>;
    subSite?: Array<CodeableConcept> /* Billing, service, product, or drug code */;
    productOrService?: CodeableConcept;
    _detailSequence?: Array<Element>;
    _itemSequence?: Array<Element>;
    programCode?: Array<CodeableConcept> /* Price scaling factor */;
    factor?: decimal /* Anatomical location */;
    bodySite?: CodeableConcept /* Count of products or services */;
    quantity?: Quantity /* Place of service or where product was supplied */;
    location?: Address | CodeableConcept | Reference<"Location">;
    provider?: Array<
      Reference<"PractitionerRole" | "Organization" | "Practitioner">
    >;
    noteNumber?: Array<positiveInt> /* Fee, charge or cost per item */;
    unitPrice?: Money;
    detail?: Array<{
      _noteNumber?: Array<Element>;
      modifier?: Array<CodeableConcept>;
      adjudication?: Array<ClaimResponse>;
      _factor?: Element /* Total item cost */;
      net?: Money /* Billing, service, product, or drug code */;
      productOrService?: CodeableConcept /* Price scaling factor */;
      factor?: decimal;
      subDetail?: Array<{
        _noteNumber?: Array<Element>;
        modifier?: Array<CodeableConcept>;
        adjudication?: Array<ClaimResponse>;
        _factor?: Element /* Total item cost */;
        net?: Money /* Billing, service, product, or drug code */;
        productOrService?: CodeableConcept /* Price scaling factor */;
        factor?: decimal /* Count of products or services */;
        quantity?: Quantity;
        noteNumber?: Array<positiveInt> /* Fee, charge or cost per item */;
        unitPrice?: Money;
      }> /* Count of products or services */;
      quantity?: Quantity;
      noteNumber?: Array<positiveInt> /* Fee, charge or cost per item */;
      unitPrice?: Money;
    }>;
  }>;
  _use?: Element;
}
/* The characteristics, operational status and capabilities of a medical-related component of a medical device. */
interface DeviceDefinition {
  deviceName?: Array<{
    /* The name of the device */ name?: string;
    _name?: Element /* udi-label-name | user-friendly-name | patient-reported-name | manufacturer-name | model-name | other */;
    type?: code;
    _type?: Element;
  }>;
  shelfLifeStorage?: Array<ProductShelfLife>;
  property?: Array<{
    /* Code that specifies the property DeviceDefinitionPropetyCode (Extensible) */
    type?: CodeableConcept;
    valueQuantity?: Array<Quantity>;
    valueCode?: Array<CodeableConcept>;
  }>;
  _onlineInformation?: Element;
  _modelNumber?: Element /* The model number for the device */;
  modelNumber?: string;
  udiDeviceIdentifier?: Array<{
    /* The identifier that is to be associated with every Device that references this DeviceDefintiion for the issuer and jurisdication porvided in the DeviceDefinition.udiDeviceIdentifier */
    deviceIdentifier?: string;
    _deviceIdentifier?: Element /* The organization that assigns the identifier algorithm */;
    issuer?: uri;
    _issuer?: Element /* The jurisdiction to which the deviceIdentifier applies */;
    jurisdiction?: uri;
    _jurisdiction?: Element;
  }> /* What kind of device or device system this is */;
  type?: CodeableConcept;
  capability?: Array<{
    /* Type of capability */ type?: CodeableConcept;
    description?: Array<CodeableConcept>;
  }>;
  specialization?: Array<{
    /* The standard that is used to operate and communicate */
    systemType?: string;
    _systemType?: Element /* The version of the standard that is used to operate and communicate */;
    version?: string;
    _version?: Element;
  }> /* The parent device it can be part of */;
  parentDevice?: Reference<"DeviceDefinition">;
  note?: Array<Annotation>;
  languageCode?: Array<CodeableConcept>;
  safety?: Array<CodeableConcept>;
  material?: Array<{
    /* The substance */
    substance?: CodeableConcept /* Indicates an alternative material of the device */;
    alternate?: boolean;
    _alternate?: Element /* Whether the substance is a known or suspected allergen */;
    allergenicIndicator?: boolean;
    _allergenicIndicator?: Element;
  }> /* Network address to contact device */;
  url?: uri;
  identifier?: Array<Identifier> /* Name of device manufacturer */;
  manufacturer?:
    | string
    | Reference<"Organization"> /* The quantity of the device present in the packaging (e.g. the number of devices present in a pack, or the number of devices in the same package of the medicinal product) */;
  quantity?: Quantity;
  version?: Array<string>;
  _version?: Array<Element>;
  contact?: Array<ContactPoint> /* Organization responsible for device */;
  owner?: Reference<"Organization">;
  _url?: Element /* Access to on-line information */;
  onlineInformation?: uri /* Dimensions, color etc. */;
  physicalCharacteristics?: ProdCharacteristic;
}
/* A slot of time on a schedule that may be available for booking appointments. */
interface Slot {
  /* The schedule resource that this slot defines an interval of status information */
  schedule?: Reference<"Schedule">;
  serviceCategory?: Array<CodeableConcept>;
  specialty?: Array<CodeableConcept>;
  _end?: Element;
  _status?: Element /* Date/Time that the slot is to begin */;
  start?: instant;
  serviceType?: Array<CodeableConcept> /* The style of appointment or patient that may be booked in the slot (not service type) */;
  appointmentType?: CodeableConcept /* busy | free | busy-unavailable | busy-tentative | entered-in-error */;
  status?: code /* Comments on the slot to describe any extended information. Such as custom constraints on the slot */;
  comment?: string;
  identifier?: Array<Identifier> /* Date/Time that the slot is to conclude */;
  end?: instant;
  _start?: Element;
  _overbooked?: Element;
  _comment?: Element /* This slot has already been overbooked, appointments are unlikely to be accepted for this time */;
  overbooked?: boolean;
}
/* A ValueSet resource instance specifies a set of codes drawn from one or more code systems, intended for use in a particular context. Value sets link between [[[CodeSystem]]] definitions and their use in [coded elements](terminologies.html). */
interface ValueSet {
  /* Natural language description of the value set */
  description?: markdown /* Content logical definition of the value set (CLD) */;
  compose?: {
    /* Fixed date for references with no specified version (transitive) */
    lockedDate?: date;
    _lockedDate?: Element /* Whether inactive codes are in the value set */;
    inactive?: boolean;
    _inactive?: Element;
    include?: Array<ValueSet>;
    exclude?: Array<ValueSet>;
  } /* Date last changed */;
  date?: dateTime /* Name of the publisher (organization or individual) */;
  publisher?: string;
  jurisdiction?: Array<CodeableConcept>;
  _publisher?: Element /* Why this value set is defined */;
  purpose?: markdown;
  _date?: Element /* Name for this value set (computer friendly) */;
  name?: string;
  _status?: Element;
  _experimental?: Element;
  useContext?: Array<UsageContext> /* Use and/or publishing restrictions */;
  copyright?: markdown /* For testing purposes, not real usage */;
  experimental?: boolean /* Used when the value set is "expanded" */;
  expansion?: {
    contains?: Array<ValueSet>;
    _timestamp?: Element;
    _offset?: Element /* Offset at which this resource starts */;
    offset?: integer /* Total number of codes in the expansion */;
    total?: integer;
    _identifier?: Element /* Identifies the value set expansion (business identifier) */;
    identifier?: uri /* Time ValueSet expansion happened */;
    timestamp?: dateTime;
    parameter?: Array<{
      /* Name as assigned by the client or server */ name?: string;
      _name?: Element /* Value of the named parameter */;
      value?: string | dateTime | integer | decimal | code | uri | boolean;
    }>;
    _total?: Element;
  } /* Name for this value set (human friendly) */;
  title?: string;
  _description?: Element;
  _purpose?: Element /* draft | active | retired | unknown */;
  status?: code;
  _immutable?: Element;
  _name?: Element /* Canonical identifier for this value set, represented as a URI (globally unique) */;
  url?: uri;
  identifier?: Array<Identifier> /* Indicates whether or not any change to the content logical definition may occur */;
  immutable?: boolean;
  _copyright?: Element;
  _title?: Element /* Business version of the value set */;
  version?: string;
  _version?: Element;
  contact?: Array<ContactDetail>;
  _url?: Element;
}
/* The regulatory authorization of a medicinal product. */
interface MedicinalProductAuthorization {
  /* A period of time after authorization before generic product applicatiosn can be submitted */
  dataExclusivityPeriod?: Period /* The date when a suspended the marketing or the marketing authorization of the product is anticipated to be restored */;
  restoreDate?: dateTime;
  jurisdiction?: Array<CodeableConcept>;
  jurisdictionalAuthorization?: Array<{
    identifier?: Array<Identifier> /* Country of authorization */;
    country?: CodeableConcept;
    jurisdiction?: Array<CodeableConcept> /* The legal status of supply in a jurisdiction or region */;
    legalStatusOfSupply?: CodeableConcept /* The start and expected end date of the authorization */;
    validityPeriod?: Period;
  }>;
  procedure?: MedicinalProductAuthorization /* The legal framework against which this authorization is granted */;
  legalBasis?: CodeableConcept /* The beginning of the time period in which the marketing authorization is in the specific status shall be specified A complete date consisting of day, month and year shall be specified using the ISO 8601 date format */;
  validityPeriod?: Period /* Medicines Regulatory Agency */;
  regulator?: Reference<"Organization"> /* The status of the marketing authorization */;
  status?: CodeableConcept;
  identifier?: Array<Identifier> /* The date at which the given status has become applicable */;
  statusDate?: dateTime;
  _internationalBirthDate?: Element /* The date when the first authorization was granted by a Medicines Regulatory Agency */;
  dateOfFirstAuthorization?: dateTime /* Date of first marketing authorization for a company's new medicinal product in any country in the World */;
  internationalBirthDate?: dateTime /* Marketing Authorization Holder */;
  holder?: Reference<"Organization">;
  _restoreDate?: Element;
  _dateOfFirstAuthorization?: Element;
  country?: Array<CodeableConcept> /* The medicinal product that is being authorized */;
  subject?: Reference<"MedicinalProductPackaged" | "MedicinalProduct">;
  _statusDate?: Element;
}
/* A definition of a FHIR structure. This resource is used to describe the underlying resources, data types defined in FHIR, and also for describing extensions and constraints on resources and data types. */
interface StructureDefinition {
  /* Natural language description of the structure definition */
  description?: markdown;
  _kind?: Element /* Date last changed */;
  date?: dateTime /* specialization | constraint - How relates to base definition */;
  derivation?: code /* Name of the publisher (organization or individual) */;
  publisher?: string;
  contextInvariant?: Array<string> /* FHIR Version this StructureDefinition targets */;
  fhirVersion?: code;
  jurisdiction?: Array<CodeableConcept>;
  _publisher?: Element /* Why this structure definition is defined */;
  purpose?: markdown;
  _date?: Element /* Name for this structure definition (computer friendly) */;
  name?: string;
  mapping?: Array<{
    /* Internal id when this mapping is used */ identity?: id;
    _identity?: Element /* Identifies what this mapping refers to */;
    uri?: uri;
    _uri?: Element /* Names what this mapping refers to */;
    name?: string;
    _name?: Element /* Versions, Issues, Scope limitations etc. */;
    comment?: string;
    _comment?: Element;
  }>;
  _type?: Element;
  _status?: Element;
  _fhirVersion?: Element;
  _experimental?: Element;
  useContext?: Array<UsageContext> /* Whether the structure is abstract */;
  abstract?: boolean /* Use and/or publishing restrictions */;
  copyright?: markdown /* Type defined or constrained by this structure */;
  type?: uri /* For testing purposes, not real usage */;
  experimental?: boolean /* Name for this structure definition (human friendly) */;
  title?: string;
  _description?: Element;
  _purpose?: Element /* Snapshot view of the structure */;
  snapshot?: { element?: Array<ElementDefinition> };
  _abstract?: Element;
  keyword?: Array<Coding> /* draft | active | retired | unknown */;
  status?: code /* primitive-type | complex-type | resource | logical */;
  kind?: code;
  _name?: Element /* Canonical identifier for this structure definition, represented as a URI (globally unique) */;
  url?: uri;
  identifier?: Array<Identifier>;
  _derivation?: Element;
  context?: Array<{
    /* fhirpath | element | extension */ type?: code;
    _type?: Element /* Where the extension can be used in instances */;
    expression?: string;
    _expression?: Element;
  }>;
  _baseDefinition?: Element;
  _copyright?: Element;
  _title?: Element /* Business version of the structure definition */;
  version?: string;
  _version?: Element /* Differential view of the structure */;
  differential?: { element?: Array<ElementDefinition> };
  contact?: Array<ContactDetail>;
  _url?: Element;
  _contextInvariant?: Array<Element> /* Definition that this type is constrained/specialized from */;
  baseDefinition?: canonical;
}
/* The clinical particulars - indications, contraindications etc. of a medicinal product, including for regulatory purposes. */
interface MedicinalProductContraindication {
  subject?: Array<
    Reference<"Medication" | "MedicinalProduct">
  > /* The disease, symptom or procedure for the contraindication */;
  disease?: CodeableConcept /* The status of the disease or symptom for the contraindication */;
  diseaseStatus?: CodeableConcept;
  comorbidity?: Array<CodeableConcept>;
  therapeuticIndication?: Array<Reference<"MedicinalProductIndication">>;
  otherTherapy?: Array<{
    /* The type of relationship between the medicinal product indication or contraindication and another therapy */
    therapyRelationshipType?: CodeableConcept /* Reference to a specific medication (active substance, medicinal product or class of products) as part of an indication or contraindication */;
    medication?:
      | CodeableConcept
      | Reference<
          | "Medication"
          | "SubstanceSpecification"
          | "Substance"
          | "MedicinalProduct"
        >;
  }>;
  population?: Array<Population>;
}
/* Represents a request for a patient to employ a medical device. The device may be an implantable device, or an external assistive device, such as a walker. */
interface DeviceRequest {
  /* Filler role */ performerType?: CodeableConcept;
  insurance?: Array<Reference<"ClaimResponse" | "Coverage">>;
  instantiatesCanonical?: Array<canonical>;
  instantiatesUri?: Array<uri>;
  relevantHistory?: Array<Reference<"Provenance">>;
  _authoredOn?: Element;
  supportingInfo?: Array<
    Reference<ResourceType>
  > /* Encounter motivating request */;
  encounter?: Reference<"Encounter">;
  priorRequest?: Array<Reference<ResourceType>>;
  _priority?: Element;
  _status?: Element;
  reasonCode?: Array<CodeableConcept> /* When recorded */;
  authoredOn?: dateTime;
  note?: Array<Annotation>;
  _intent?: Element /* Who/what is requesting diagnostics */;
  requester?: Reference<
    "PractitionerRole" | "Organization" | "Device" | "Practitioner"
  > /* routine | urgent | asap | stat */;
  priority?: code /* draft | active | on-hold | revoked | completed | entered-in-error | unknown */;
  status?: code /* Identifier of composite request */;
  groupIdentifier?: Identifier /* Device requested */;
  code?: CodeableConcept | Reference<"Device">;
  identifier?: Array<Identifier> /* proposal | plan | directive | order | original-order | reflex-order | filler-order | instance-order | option */;
  intent?: code;
  basedOn?: Array<Reference<ResourceType>>;
  _instantiatesCanonical?: Array<Element> /* Focus of request */;
  subject?: Reference<"Patient" | "Device" | "Location" | "Group">;
  parameter?: Array<{
    /* Device detail */ code?: CodeableConcept /* Value of detail */;
    value?: Range | CodeableConcept | Quantity | boolean;
  }> /* Desired time or schedule for use */;
  occurrence?: dateTime | Period | Timing /* Requested Filler */;
  performer?: Reference<
    | "CareTeam"
    | "Patient"
    | "PractitionerRole"
    | "HealthcareService"
    | "Organization"
    | "Device"
    | "Practitioner"
    | "RelatedPerson"
  >;
  reasonReference?: Array<
    Reference<
      "Observation" | "DocumentReference" | "DiagnosticReport" | "Condition"
    >
  >;
  _instantiatesUri?: Array<Element>;
}
/* A list is a curated collection of resources. */
interface List {
  /* When the list was prepared */
  date?: dateTime /* Context in which list created */;
  encounter?: Reference<"Encounter">;
  _date?: Element /* What order the list has */;
  orderedBy?: CodeableConcept;
  _status?: Element /* working | snapshot | changes */;
  mode?: code /* Who and/or what defined the list contents (aka Author) */;
  source?: Reference<
    "Patient" | "PractitionerRole" | "Device" | "Practitioner"
  > /* Descriptive name for the list */;
  title?: string;
  note?: Array<Annotation> /* Why list is empty */;
  emptyReason?: CodeableConcept /* current | retired | entered-in-error */;
  status?: code /* What the purpose of this list is */;
  code?: CodeableConcept;
  identifier?: Array<Identifier>;
  entry?: Array<{
    /* Status/Workflow information about this item */
    flag?: CodeableConcept /* If this item is actually marked as deleted */;
    deleted?: boolean;
    _deleted?: Element /* When item added to list */;
    date?: dateTime;
    _date?: Element /* Actual entry */;
    item?: Reference<ResourceType>;
  }>;
  _title?: Element;
  _mode?: Element /* If all resources have the same subject */;
  subject?: Reference<"Patient" | "Device" | "Location" | "Group">;
}
/* A structured set of questions intended to guide the collection of answers from end-users. Questionnaires provide detailed control over order, presentation, phraseology and grouping to allow coherent, consistent data collection. */
interface Questionnaire {
  /* Natural language description of the questionnaire */
  description?: markdown;
  subjectType?: Array<code> /* Date last changed */;
  date?: dateTime /* Name of the publisher (organization or individual) */;
  publisher?: string /* When the questionnaire was approved by publisher */;
  approvalDate?: date;
  jurisdiction?: Array<CodeableConcept>;
  _publisher?: Element;
  derivedFrom?: Array<canonical> /* Why this questionnaire is defined */;
  purpose?: markdown;
  _date?: Element;
  _derivedFrom?: Array<Element> /* Name for this questionnaire (computer friendly) */;
  name?: string;
  item?: Array<Questionnaire>;
  _status?: Element;
  _experimental?: Element;
  useContext?: Array<UsageContext> /* Use and/or publishing restrictions */;
  copyright?: markdown;
  _approvalDate?: Element /* For testing purposes, not real usage */;
  experimental?: boolean /* Name for this questionnaire (human friendly) */;
  title?: string;
  _description?: Element;
  _purpose?: Element;
  _lastReviewDate?: Element /* draft | active | retired | unknown */;
  status?: code;
  _name?: Element /* Canonical identifier for this questionnaire, represented as a URI (globally unique) */;
  url?: uri;
  code?: Array<Coding>;
  identifier?: Array<Identifier> /* When the questionnaire was last reviewed */;
  lastReviewDate?: date;
  _copyright?: Element;
  _title?: Element /* Business version of the questionnaire */;
  version?: string;
  _version?: Element;
  contact?: Array<ContactDetail>;
  _subjectType?: Array<Element>;
  _url?: Element /* When the questionnaire is expected to be used */;
  effectivePeriod?: Period;
}
/* A resource that includes narrative, extensions, and contained resources. */
interface DomainResource {
  /* Text summary of the resource, for human interpretation */ text?: Narrative;
  contained?: Array<Resource>;
  extension?: Array<Extension>;
  modifierExtension?: Array<Extension>;
}
/* The technical details of an endpoint that can be used for electronic services, such as for web services providing XDS.b or a REST endpoint for another FHIR server. This may include any security context information. */
interface Endpoint {
  _address?: Element /* Protocol/Profile/Standard to be used with this endpoint connection */;
  connectionType?: Coding /* The technical base address for connecting to this endpoint */;
  address?: url /* Organization that manages this endpoint (might not be the organization that exposes the endpoint) */;
  managingOrganization?: Reference<"Organization"> /* A name that this endpoint can be identified by */;
  name?: string;
  payloadMimeType?: Array<code>;
  _status?: Element;
  _header?: Array<Element>;
  payloadType?: Array<CodeableConcept>;
  header?: Array<string> /* active | suspended | error | off | entered-in-error | test */;
  status?: code;
  _name?: Element;
  identifier?: Array<Identifier> /* Interval the endpoint is expected to be operational */;
  period?: Period;
  contact?: Array<ContactPoint>;
  _payloadMimeType?: Array<Element>;
}
/* A curated namespace that issues unique symbols within that namespace for the identification of concepts, people, devices, etc.  Represents a "System" used within the Identifier and Coding data types. */
interface NamingSystem {
  /* Natural language description of the naming system */
  description?: markdown;
  _usage?: Element;
  _kind?: Element /* Date last changed */;
  date?: dateTime;
  _responsible?: Element /* Name of the publisher (organization or individual) */;
  publisher?: string;
  jurisdiction?: Array<CodeableConcept>;
  _publisher?: Element;
  _date?: Element /* Name for this naming system (computer friendly) */;
  name?: string;
  _status?: Element;
  useContext?: Array<UsageContext> /* e.g. driver,  provider,  patient, bank etc. */;
  type?: CodeableConcept;
  _description?: Element /* Who maintains system namespace? */;
  responsible?: string /* How/where is it used */;
  usage?: string /* draft | active | retired | unknown */;
  status?: code /* codesystem | identifier | root */;
  kind?: code;
  _name?: Element;
  uniqueId?: Array<{
    /* The unique identifier */ value?: string;
    _type?: Element /* oid | uuid | uri | other */;
    type?: code;
    _value?: Element /* Notes about identifier usage */;
    comment?: string;
    _preferred?: Element /* Is this the id that should be used for this type */;
    preferred?: boolean /* When is identifier valid? */;
    period?: Period;
    _comment?: Element;
  }>;
  contact?: Array<ContactDetail>;
}
/* A medicinal product in a container or package. */
interface MedicinalProductPackaged {
  /* Textual description */ description?: string;
  _description?: Element;
  marketingStatus?: Array<MarketingStatus> /* Manufacturer of this Package Item */;
  marketingAuthorization?: Reference<"MedicinalProductAuthorization">;
  identifier?: Array<Identifier>;
  manufacturer?: Array<
    Reference<"Organization">
  > /* The legal status of supply of the medicinal product as classified by the regulator */;
  legalStatusOfSupply?: CodeableConcept;
  batchIdentifier?: Array<{
    /* A number appearing on the outer packaging of a specific batch */
    outerPackaging?: Identifier /* A number appearing on the immediate packaging (and not the outer packaging) */;
    immediatePackaging?: Identifier;
  }>;
  subject?: Array<Reference<"MedicinalProduct">>;
  packageItem?: Array<MedicinalProductPackaged>;
}
/* Basic is used for handling concepts not yet defined in FHIR, narrative-only resources that don't map to an existing resource, and custom resources not appropriate for inclusion in the FHIR specification. */
interface Basic {
  identifier?: Array<Identifier> /* Kind of Resource */;
  code?: CodeableConcept /* Identifies the focus of this resource */;
  subject?: Reference<ResourceType> /* When created */;
  created?: date;
  _created?: Element /* Who created */;
  author?: Reference<
    | "Patient"
    | "PractitionerRole"
    | "Organization"
    | "Practitioner"
    | "RelatedPerson"
  >;
}
/* A resource that represents the data of a single raw artifact as digital content accessible in its native format.  A Binary resource can contain any content, whether text, image, pdf, zip archive, etc. */
interface Binary {
  /* MimeType of the binary content */ contentType?: code;
  _contentType?: Element /* Identifies another resource to use as proxy when enforcing access control */;
  securityContext?: Reference<ResourceType> /* The actual content */;
  data?: base64Binary;
  _data?: Element;
}
/* This resource allows for the definition of various types of plans as a sharable, consumable, and executable artifact. The resource is general enough to support the description of a broad range of clinical artifacts such as clinical decision support rules, order sets and protocols. */
interface PlanDefinition {
  /* Natural language description of the plan definition */
  description?: markdown;
  _usage?: Element /* Date last changed */;
  date?: dateTime;
  endorser?: Array<ContactDetail> /* Name of the publisher (organization or individual) */;
  publisher?: string /* When the plan definition was approved by publisher */;
  approvalDate?: date;
  jurisdiction?: Array<CodeableConcept>;
  _publisher?: Element /* Why this plan definition is defined */;
  purpose?: markdown;
  _date?: Element /* Name for this plan definition (computer friendly) */;
  name?: string;
  _status?: Element;
  _experimental?: Element;
  useContext?: Array<UsageContext>;
  goal?: Array<{
    /* E.g. Treatment, dietary, behavioral */
    category?: CodeableConcept /* Code or text describing the goal */;
    description?: CodeableConcept /* high-priority | medium-priority | low-priority */;
    priority?: CodeableConcept /* When goal pursuit begins */;
    start?: CodeableConcept;
    addresses?: Array<CodeableConcept>;
    documentation?: Array<RelatedArtifact>;
    target?: Array<{
      /* The parameter whose value is to be tracked */
      measure?: CodeableConcept /* The target value to be achieved */;
      detail?: Range | CodeableConcept | Quantity /* Reach goal within */;
      due?: Duration;
    }>;
  }> /* Use and/or publishing restrictions */;
  copyright?: markdown;
  _approvalDate?: Element /* order-set | clinical-protocol | eca-rule | workflow-definition */;
  type?: CodeableConcept /* For testing purposes, not real usage */;
  experimental?: boolean;
  topic?: Array<CodeableConcept> /* Name for this plan definition (human friendly) */;
  title?: string;
  _description?: Element;
  library?: Array<canonical>;
  author?: Array<ContactDetail>;
  _purpose?: Element /* Describes the clinical usage of the plan */;
  usage?: string;
  _lastReviewDate?: Element /* draft | active | retired | unknown */;
  status?: code /* Subordinate title of the plan definition */;
  subtitle?: string;
  _name?: Element /* Canonical identifier for this plan definition, represented as a URI (globally unique) */;
  url?: uri;
  identifier?: Array<Identifier> /* When the plan definition was last reviewed */;
  lastReviewDate?: date;
  editor?: Array<ContactDetail>;
  _subtitle?: Element;
  _library?: Array<Element>;
  _copyright?: Element;
  action?: Array<PlanDefinition>;
  _title?: Element;
  reviewer?: Array<ContactDetail> /* Business version of the plan definition */;
  version?: string;
  _version?: Element;
  relatedArtifact?: Array<RelatedArtifact>;
  contact?: Array<ContactDetail> /* Type of individual the plan definition is focused on */;
  subject?: CodeableConcept | Reference<"Group">;
  _url?: Element /* When the plan definition is expected to be used */;
  effectivePeriod?: Period;
}
/* The subscription resource is used to define a push-based subscription from a server to another system. Once a subscription is registered with the server, the server checks every resource that is created or updated, and if the resource matches the given criteria, it sends a message on the defined "channel" so that another system can take an appropriate action. */
interface Subscription {
  _end?: Element /* Rule for server push */;
  criteria?: string;
  _status?: Element /* The channel on which to report matches to the criteria */;
  channel?: {
    /* rest-hook | websocket | email | sms | message */ type?: code;
    _type?: Element /* Where the channel points to */;
    endpoint?: url;
    _endpoint?: Element /* MIME type to send, or omit for no payload */;
    payload?: code;
    _payload?: Element;
    header?: Array<string>;
    _header?: Array<Element>;
  };
  _criteria?: Element /* Description of why this subscription was created */;
  reason?: string /* requested | active | error | off */;
  status?: code;
  _reason?: Element /* Latest error note */;
  error?: string;
  _error?: Element /* When to automatically delete the subscription */;
  end?: instant;
  contact?: Array<ContactPoint>;
}
/* Information about a person that is involved in the care for a patient, but who is not the target of healthcare, nor has a formal responsibility in the care process. */
interface RelatedPerson {
  /* The patient this person is related to */ patient?: Reference<"Patient">;
  _active?: Element;
  address?: Array<Address>;
  name?: Array<HumanName>;
  _gender?: Element /* The date on which the related person was born */;
  birthDate?: date;
  relationship?: Array<CodeableConcept>;
  _birthDate?: Element;
  photo?: Array<Attachment> /* Whether this related person's record is in active use */;
  active?: boolean;
  communication?: Array<{
    /* The language which can be used to communicate with the patient about his or her health */
    language?: CodeableConcept /* Language preference indicator */;
    preferred?: boolean;
    _preferred?: Element;
  }>;
  identifier?: Array<Identifier>;
  telecom?: Array<ContactPoint> /* male | female | other | unknown */;
  gender?: code /* Period of time that this relationship is considered valid */;
  period?: Period;
}
/* The detailed description of a substance, typically at a level beyond what is used for prescribing. */
interface SubstanceSpecification {
  /* Textual description of the substance */ description?: string;
  property?: Array<{
    /* A category for this property, e.g. Physical, Chemical, Enzymatic */
    category?: CodeableConcept /* Property type e.g. viscosity, pH, isoelectric point */;
    code?: CodeableConcept /* Parameters that were used in the measurement of a property (e.g. for viscosity: measured at 20C with a pH of 7.1) */;
    parameters?: string;
    _parameters?: Element /* A substance upon which a defining property depends (e.g. for solubility: in water, in alcohol) */;
    definingSubstance?:
      | CodeableConcept
      | Reference<
          "SubstanceSpecification" | "Substance"
        > /* Quantitative value for this property */;
    amount?: string | Quantity;
  }>;
  name?: Array<SubstanceSpecification> /* General information detailing this substance */;
  referenceInformation?: Reference<"SubstanceReferenceInformation">;
  relationship?: Array<{
    /* A pointer to another substance, as a resource or just a representational code */
    substance?:
      | CodeableConcept
      | Reference<"SubstanceSpecification"> /* For example "salt to parent", "active moiety", "starting material" */;
    relationship?: CodeableConcept /* For example where an enzyme strongly bonds with a particular substance, this is a defining relationship for that enzyme, out of several possible substance relationships */;
    isDefining?: boolean;
    _isDefining?: Element /* A numeric factor for the relationship, for instance to express that the salt of a substance has some percentage of the active substance in relation to some other */;
    amount?: string | Range | Ratio | Quantity /* For use when the numeric */;
    amountRatioLowLimit?: Ratio /* An operator for the amount, for example "average", "approximately", "less than" */;
    amountType?: CodeableConcept;
    source?: Array<Reference<"DocumentReference">>;
  }> /* High level categorization, e.g. polymer or nucleic acid */;
  type?: CodeableConcept;
  moiety?: Array<{
    /* Role that the moiety is playing */
    role?: CodeableConcept /* Quantitative value for this moiety */;
    amount?: string | Quantity /* Textual name for this moiety substance */;
    name?: string /* Molecular formula */;
    molecularFormula?: string;
    _molecularFormula?: Element /* Optical activity type */;
    opticalActivity?: CodeableConcept;
    _name?: Element /* Identifier by which this moiety substance is known */;
    identifier?: Identifier /* Stereochemistry type */;
    stereochemistry?: CodeableConcept;
  }>;
  source?: Array<Reference<"DocumentReference">>;
  _description?: Element /* Data items specific to nucleic acids */;
  nucleicAcid?: Reference<"SubstanceNucleicAcid"> /* Structural information */;
  structure?: {
    isotope?: Array<{
      /* Substance identifier for each non-natural or radioisotope */
      identifier?: Identifier /* Substance name for each non-natural or radioisotope */;
      name?: CodeableConcept /* The type of isotopic substitution present in a single substance */;
      substitution?: CodeableConcept /* Half life - for a non-natural nuclide */;
      halfLife?: Quantity;
      molecularWeight?: SubstanceSpecification;
    }>;
    _molecularFormulaByMoiety?: Element /* Molecular formula */;
    molecularFormula?: string;
    source?: Array<Reference<"DocumentReference">>;
    representation?: Array<{
      /* The type of structure (e.g. Full, Partial, Representative) */
      type?: CodeableConcept /* The structural representation as text string in a format e.g. InChI, SMILES, MOLFILE, CDX */;
      representation?: string;
      _representation?: Element /* An attached file with the structural representation */;
      attachment?: Attachment;
    }>;
    _molecularFormula?: Element /* Optical activity type */;
    opticalActivity?: CodeableConcept /* The molecular weight or weight range (for proteins, polymers or nucleic acids) */;
    molecularWeight?: SubstanceSpecification /* Stereochemistry type */;
    stereochemistry?: CodeableConcept /* Specified per moiety according to the Hill system, i.e. first C, then H, then alphabetical, each moiety separated by a dot */;
    molecularFormulaByMoiety?: string;
  } /* Status of substance within the catalogue e.g. approved */;
  status?: CodeableConcept /* Textual comment about this record of a substance */;
  comment?: string;
  code?: Array<{
    /* The specific code */
    code?: CodeableConcept /* Status of the code assignment */;
    status?: CodeableConcept /* The date at which the code status is changed as part of the terminology maintenance */;
    statusDate?: dateTime;
    _statusDate?: Element /* Any comment can be provided in this field, if necessary */;
    comment?: string;
    _comment?: Element;
    source?: Array<Reference<"DocumentReference">>;
  }> /* Identifier by which this substance is known */;
  identifier?: Identifier;
  molecularWeight?: Array<SubstanceSpecification> /* Data items specific to polymers */;
  polymer?: Reference<"SubstancePolymer"> /* Data items specific to proteins */;
  protein?: Reference<"SubstanceProtein"> /* If the substance applies to only human or veterinary use */;
  domain?: CodeableConcept /* Material or taxonomic/anatomical source for the substance */;
  sourceMaterial?: Reference<"SubstanceSourceMaterial">;
  _comment?: Element;
}
/* Nucleic acids are defined by three distinct elements: the base, sugar and linkage. Individual substance/moiety IDs will be created for each of these elements. The nucleotide sequence will be always entered in the 5’-3’ direction. */
interface SubstanceNucleicAcid {
  /* The type of the sequence shall be specified based on a controlled vocabulary */
  sequenceType?: CodeableConcept /* The number of linear sequences of nucleotides linked through phosphodiester bonds shall be described. Subunits would be strands of nucleic acids that are tightly associated typically through Watson-Crick base pairing. NOTE: If not specified in the reference source, the assumption is that there is 1 subunit */;
  numberOfSubunits?: integer;
  _numberOfSubunits?: Element /* The area of hybridisation shall be described if applicable for double stranded RNA or DNA. The number associated with the subunit followed by the number associated to the residue shall be specified in increasing order. The underscore “” shall be used as separator as follows: “Subunitnumber Residue” */;
  areaOfHybridisation?: string;
  _areaOfHybridisation?: Element /* (TBC) */;
  oligoNucleotideType?: CodeableConcept;
  subunit?: Array<{
    _sequence?: Element;
    _subunit?: Element /* The nucleotide present at the 5’ terminal shall be specified based on a controlled vocabulary. Since the sequence is represented from the 5' to the 3' end, the 5’ prime nucleotide is the letter at the first position in the sequence. A separate representation would be redundant */;
    fivePrime?: CodeableConcept;
    linkage?: Array<{
      /* The entity that links the sugar residues together should also be captured for nearly all naturally occurring nucleic acid the linkage is a phosphate group. For many synthetic oligonucleotides phosphorothioate linkages are often seen. Linkage connectivity is assumed to be 3’-5’. If the linkage is either 3’-3’ or 5’-5’ this should be specified */
      connectivity?: string;
      _connectivity?: Element /* Each linkage will be registered as a fragment and have an ID */;
      identifier?: Identifier /* Each linkage will be registered as a fragment and have at least one name. A single name shall be assigned to each linkage */;
      name?: string;
      _name?: Element /* Residues shall be captured as described in 5.3.6.8.3 */;
      residueSite?: string;
      _residueSite?: Element;
    }> /* Actual nucleotide sequence notation from 5' to 3' end using standard single letter codes. In addition to the base sequence, sugar and type of phosphate or non-phosphate linkage should also be captured */;
    sequence?: string /* The nucleotide present at the 3’ terminal shall be specified based on a controlled vocabulary. Since the sequence is represented from the 5' to the 3' end, the 5’ prime nucleotide is the letter at the last position in the sequence. A separate representation would be redundant */;
    threePrime?: CodeableConcept /* The length of the sequence shall be captured */;
    length?: integer;
    sugar?: Array<{
      /* The Substance ID of the sugar or sugar-like component that make up the nucleotide */
      identifier?: Identifier /* The name of the sugar or sugar-like component that make up the nucleotide */;
      name?: string;
      _name?: Element /* The residues that contain a given sugar will be captured. The order of given residues will be captured in the 5‘-3‘direction consistent with the base sequences listed above */;
      residueSite?: string;
      _residueSite?: Element;
    }>;
    _length?: Element /* Index of linear sequences of nucleic acids in order of decreasing length. Sequences of the same length will be ordered by molecular weight. Subunits that have identical sequences will be repeated and have sequential subscripts */;
    subunit?: integer /* (TBC) */;
    sequenceAttachment?: Attachment;
  }>;
}
/* A guidance response is the formal response to a guidance request, including any output parameters returned by the evaluation, as well as the description of any proposed actions to be taken. */
interface GuidanceResponse {
  dataRequirement?: Array<DataRequirement> /* Encounter during which the response was returned */;
  encounter?: Reference<"Encounter">;
  _status?: Element;
  reasonCode?: Array<CodeableConcept> /* The output parameters of the evaluation, if any */;
  outputParameters?: Reference<"Parameters">;
  evaluationMessage?: Array<
    Reference<"OperationOutcome">
  > /* The identifier of the request associated with this response, if any */;
  requestIdentifier?: Identifier /* What guidance was requested */;
  module?: canonical | uri | CodeableConcept;
  note?: Array<Annotation> /* success | data-requested | data-required | in-progress | failure | entered-in-error */;
  status?: code /* Proposed actions, if any */;
  result?: Reference<"RequestGroup" | "CarePlan">;
  identifier?: Array<Identifier> /* When the guidance response was processed */;
  occurrenceDateTime?: dateTime /* Patient the request was performed for */;
  subject?: Reference<"Patient" | "Group"> /* Device returning the guidance */;
  performer?: Reference<"Device">;
  reasonReference?: Array<
    Reference<
      "Observation" | "DocumentReference" | "DiagnosticReport" | "Condition"
    >
  >;
  _occurrenceDateTime?: Element;
}
/* A record of a clinical assessment performed to determine what problem(s) may affect the patient and before planning the treatments or management strategies that are best to manage a patient's condition. Assessments are often 1:1 with a clinical consultation / encounter,  but this varies greatly depending on the clinical workflow. This resource is called "ClinicalImpression" rather than "ClinicalAssessment" to avoid confusion with the recording of assessment tools such as Apgar score. */
interface ClinicalImpression {
  /* Why/how the assessment was performed */
  description?: string /* When the assessment was documented */;
  date?: dateTime;
  investigation?: Array<{
    /* A name/code for the set */ code?: CodeableConcept;
    item?: Array<
      Reference<
        | "Media"
        | "QuestionnaireResponse"
        | "RiskAssessment"
        | "ImagingStudy"
        | "Observation"
        | "DiagnosticReport"
        | "FamilyMemberHistory"
      >
    >;
  }>;
  protocol?: Array<uri> /* The clinician performing the assessment */;
  assessor?: Reference<"PractitionerRole" | "Practitioner">;
  supportingInfo?: Array<
    Reference<ResourceType>
  > /* Encounter created as part of */;
  encounter?: Reference<"Encounter">;
  _date?: Element;
  _status?: Element;
  problem?: Array<
    Reference<"Condition" | "AllergyIntolerance">
  > /* Reason for current status */;
  statusReason?: CodeableConcept;
  _description?: Element;
  note?: Array<Annotation> /* Summary of the assessment */;
  summary?: string;
  _protocol?: Array<Element>;
  prognosisCodeableConcept?: Array<CodeableConcept> /* in-progress | completed | entered-in-error */;
  status?: code /* Time of assessment */;
  effective?: dateTime | Period /* Reference to last assessment */;
  previous?: Reference<"ClinicalImpression"> /* Kind of assessment performed */;
  code?: CodeableConcept;
  identifier?: Array<Identifier>;
  _summary?: Element;
  finding?: Array<{
    /* What was found */
    itemCodeableConcept?: CodeableConcept /* What was found */;
    itemReference?: Reference<
      "Media" | "Observation" | "Condition"
    > /* Which investigations support finding */;
    basis?: string;
    _basis?: Element;
  }>;
  prognosisReference?: Array<
    Reference<"RiskAssessment">
  > /* Patient or group assessed */;
  subject?: Reference<"Patient" | "Group">;
}
/* Defines an affiliation/assotiation/relationship between 2 distinct oganizations, that is not a part-of relationship/sub-division relationship. */
interface OrganizationAffiliation {
  _active?: Element;
  specialty?: Array<CodeableConcept> /* Organization where the role is available */;
  organization?: Reference<"Organization"> /* Organization that provides/performs the role (e.g. providing services or is a member of) */;
  participatingOrganization?: Reference<"Organization"> /* Whether this organization affiliation record is in active use */;
  active?: boolean;
  code?: Array<CodeableConcept>;
  identifier?: Array<Identifier>;
  telecom?: Array<ContactPoint>;
  network?: Array<
    Reference<"Organization">
  > /* The period during which the participatingOrganization is affiliated with the primary organization */;
  period?: Period;
  location?: Array<Reference<"Location">>;
  endpoint?: Array<Reference<"Endpoint">>;
  healthcareService?: Array<Reference<"HealthcareService">>;
}
/* A clinical condition, problem, diagnosis, or other event, situation, issue, or clinical concept that has risen to a level of concern. */
interface Condition {
  /* Estimated or actual date,  date-time, or age */
  onset?: string | dateTime | Range | Period | Age;
  category?: Array<CodeableConcept> /* active | recurrence | relapse | inactive | remission | resolved */;
  clinicalStatus?: CodeableConcept;
  stage?: Array<{
    /* Simple summary (disease specific) */ summary?: CodeableConcept;
    assessment?: Array<
      Reference<"Observation" | "ClinicalImpression" | "DiagnosticReport">
    > /* Kind of staging */;
    type?: CodeableConcept;
  }> /* Encounter created as part of */;
  encounter?: Reference<"Encounter">;
  evidence?: Array<{
    code?: Array<CodeableConcept>;
    detail?: Array<Reference<ResourceType>>;
  }> /* When in resolution/remission */;
  abatement?:
    | string
    | dateTime
    | Range
    | Period
    | Age /* Person who asserts this condition */;
  asserter?: Reference<
    "Patient" | "PractitionerRole" | "Practitioner" | "RelatedPerson"
  >;
  note?: Array<Annotation> /* Date record was first recorded */;
  recordedDate?: dateTime;
  _recordedDate?: Element /* Who recorded the condition */;
  recorder?: Reference<
    "Patient" | "PractitionerRole" | "Practitioner" | "RelatedPerson"
  > /* Subjective severity of condition */;
  severity?: CodeableConcept /* Identification of the condition, problem or diagnosis */;
  code?: CodeableConcept;
  identifier?: Array<Identifier>;
  bodySite?: Array<CodeableConcept> /* unconfirmed | provisional | differential | confirmed | refuted | entered-in-error */;
  verificationStatus?: CodeableConcept /* Who has the condition? */;
  subject?: Reference<"Patient" | "Group">;
}
/* A Capability Statement documents a set of capabilities (behaviors) of a FHIR Server for a particular version of FHIR that may be used as a statement of actual server functionality or a statement of required or desired server implementation. */
interface CapabilityStatement {
  /* Natural language description of the capability statement */
  description?: markdown;
  _kind?: Element;
  format?: Array<code>;
  _patchFormat?: Array<Element>;
  _imports?: Array<Element>;
  _format?: Array<Element> /* Date last changed */;
  date?: dateTime /* Name of the publisher (organization or individual) */;
  publisher?: string;
  patchFormat?: Array<code> /* FHIR Version the system supports */;
  fhirVersion?: code;
  jurisdiction?: Array<CodeableConcept>;
  _publisher?: Element;
  instantiates?: Array<canonical> /* Why this capability statement is defined */;
  purpose?: markdown;
  _date?: Element /* Name for this capability statement (computer friendly) */;
  name?: string;
  _instantiates?: Array<Element>;
  _status?: Element;
  _fhirVersion?: Element;
  _experimental?: Element;
  useContext?: Array<UsageContext> /* Use and/or publishing restrictions */;
  copyright?: markdown /* For testing purposes, not real usage */;
  experimental?: boolean;
  imports?: Array<canonical> /* Name for this capability statement (human friendly) */;
  title?: string;
  _description?: Element;
  document?: Array<{
    /* producer | consumer */ mode?: code;
    _mode?: Element /* Description of document support */;
    documentation?: markdown;
    _documentation?: Element /* Constraint on the resources used in the document */;
    profile?: canonical;
    _profile?: Element;
  }>;
  _purpose?: Element /* draft | active | retired | unknown */;
  status?: code;
  messaging?: Array<{
    endpoint?: Array<{
      /* http | ftp | mllp + */
      protocol?: Coding /* Network address or identifier of the end-point */;
      address?: url;
      _address?: Element;
    }> /* Reliable Message Cache Length (min) */;
    reliableCache?: unsignedInt;
    _reliableCache?: Element /* Messaging interface behavior details */;
    documentation?: markdown;
    _documentation?: Element;
    supportedMessage?: Array<{
      /* sender | receiver */ mode?: code;
      _mode?: Element /* Message supported by this system */;
      definition?: canonical;
      _definition?: Element;
    }>;
  }> /* instance | capability | requirements */;
  kind?: code;
  _name?: Element;
  implementationGuide?: Array<canonical> /* Canonical identifier for this capability statement, represented as a URI (globally unique) */;
  url?: uri;
  _copyright?: Element;
  _title?: Element /* Software that is covered by this capability statement */;
  software?: {
    /* A name the software is known by */ name?: string;
    _name?: Element /* Version covered by this statement */;
    version?: string;
    _version?: Element /* Date this version was released */;
    releaseDate?: dateTime;
    _releaseDate?: Element;
  } /* Business version of the capability statement */;
  version?: string;
  _version?: Element;
  contact?: Array<ContactDetail> /* If this describes a specific instance */;
  implementation?: {
    /* Describes this specific instance */ description?: string;
    _description?: Element /* Base URL for the installation */;
    url?: url;
    _url?: Element /* Organization that manages the data */;
    custodian?: Reference<"Organization">;
  };
  rest?: Array<{
    _documentation?: Element;
    searchParam?: Array<CapabilityStatement> /* Information about security of implementation */;
    security?: {
      /* Adds CORS Headers (http://enable-cors.org/) */ cors?: boolean;
      _cors?: Element;
      service?: Array<CodeableConcept> /* General description of how security works */;
      description?: markdown;
      _description?: Element;
    };
    operation?: Array<CapabilityStatement> /* client | server */;
    mode?: code;
    _compartment?: Array<Element>;
    interaction?: Array<{
      /* transaction | batch | search-system | history-system */ code?: code;
      _code?: Element /* Anything special about operation behavior */;
      documentation?: markdown;
      _documentation?: Element;
    }> /* General description of implementation */;
    documentation?: markdown;
    resource?: Array<{
      _readHistory?: Element;
      _searchRevInclude?: Array<Element>;
      searchRevInclude?: Array<string>;
      _documentation?: Element;
      searchParam?: Array<CapabilityStatement> /* If allows/uses conditional update */;
      conditionalUpdate?: boolean;
      _updateCreate?: Element;
      _conditionalCreate?: Element /* not-supported | modified-since | not-match | full-support */;
      conditionalRead?: code;
      _type?: Element;
      operation?: Array<CapabilityStatement>;
      _conditionalUpdate?: Element;
      referencePolicy?: Array<code> /* Whether vRead can return past versions */;
      readHistory?: boolean /* A resource type that is supported */;
      type?: code;
      interaction?: Array<{
        /* read | vread | update | patch | delete | history-instance | history-type | create | search-type */
        code?: code;
        _code?: Element /* Anything special about operation behavior */;
        documentation?: markdown;
        _documentation?: Element;
      }>;
      _conditionalRead?: Element /* Additional information about the use of the resource type */;
      documentation?: markdown /* If update can commit to a new identity */;
      updateCreate?: boolean;
      _profile?: Element;
      _conditionalDelete?: Element /* If allows/uses conditional create */;
      conditionalCreate?: boolean;
      supportedProfile?: Array<canonical>;
      _referencePolicy?: Array<Element>;
      _searchInclude?: Array<Element>;
      searchInclude?: Array<string> /* no-version | versioned | versioned-update */;
      versioning?: code /* Base System profile for all uses of resource */;
      profile?: canonical;
      _versioning?: Element;
      _supportedProfile?: Array<Element> /* not-supported | single | multiple - how conditional delete is supported */;
      conditionalDelete?: code;
    }>;
    compartment?: Array<canonical>;
    _mode?: Element;
  }>;
  _url?: Element;
  _implementationGuide?: Array<Element>;
}
/* The details of a healthcare service available at a location. */
interface HealthcareService {
  coverageArea?: Array<Reference<"Location">>;
  _active?: Element;
  category?: Array<CodeableConcept>;
  availableTime?: Array<{
    daysOfWeek?: Array<code>;
    _daysOfWeek?: Array<Element> /* Always available? e.g. 24 hour service */;
    allDay?: boolean;
    _allDay?: Element /* Opening time of day (ignored if allDay = true) */;
    availableStartTime?: time;
    _availableStartTime?: Element /* Closing time of day (ignored if allDay = true) */;
    availableEndTime?: time;
    _availableEndTime?: Element;
  }>;
  _extraDetails?: Element;
  specialty?: Array<CodeableConcept> /* Description of service as presented to a consumer while searching */;
  name?: string;
  notAvailable?: Array<{
    /* Reason presented to the user explaining why time not available */
    description?: string;
    _description?: Element /* Service not available from this date */;
    during?: Period;
  }> /* Organization that provides this service */;
  providedBy?: Reference<"Organization">;
  type?: Array<CodeableConcept>;
  eligibility?: Array<{
    /* Coded value for the eligibility */
    code?: CodeableConcept /* Describes the eligibility conditions for the service */;
    comment?: markdown;
    _comment?: Element;
  }>;
  _appointmentRequired?: Element /* Extra details about the service that can't be placed in the other fields */;
  extraDetails?: markdown;
  characteristic?: Array<CodeableConcept> /* Facilitates quick identification of the service */;
  photo?: Attachment /* Whether this HealthcareService record is in active use */;
  active?: boolean;
  communication?: Array<CodeableConcept> /* Additional description and/or any specific issues not covered elsewhere */;
  comment?: string;
  _name?: Element;
  _availabilityExceptions?: Element;
  identifier?: Array<Identifier>;
  serviceProvisionCode?: Array<CodeableConcept> /* Description of availability exceptions */;
  availabilityExceptions?: string /* If an appointment is required for access to this service */;
  appointmentRequired?: boolean;
  referralMethod?: Array<CodeableConcept>;
  telecom?: Array<ContactPoint>;
  location?: Array<Reference<"Location">>;
  program?: Array<CodeableConcept>;
  endpoint?: Array<Reference<"Endpoint">>;
  _comment?: Element;
}
/* A kind of specimen with associated set of requirements. */
interface SpecimenDefinition {
  /* Business identifier of a kind of specimen */
  identifier?: Identifier /* Kind of material to collect */;
  typeCollected?: CodeableConcept;
  patientPreparation?: Array<CodeableConcept> /* Time aspect for collection */;
  timeAspect?: string;
  _timeAspect?: Element;
  collection?: Array<CodeableConcept>;
  typeTested?: Array<{
    /* Specimen requirements */
    requirement?: string /* Specimen retention time */;
    retentionTime?: Duration;
    _preference?: Element /* Primary or secondary specimen */;
    isDerived?: boolean;
    _isDerived?: Element /* Type of intended specimen */;
    type?: CodeableConcept;
    rejectionCriterion?: Array<CodeableConcept> /* preferred | alternate */;
    preference?: code;
    handling?: Array<{
      /* Temperature qualifier */
      temperatureQualifier?: CodeableConcept /* Temperature range */;
      temperatureRange?: Range /* Maximum preservation time */;
      maxDuration?: Duration /* Preservation instruction */;
      instruction?: string;
      _instruction?: Element;
    }> /* The specimen's container */;
    container?: {
      /* Container description */ description?: string;
      _preparation?: Element /* Container capacity */;
      capacity?: Quantity /* Kind of container associated with the kind of specimen */;
      type?: CodeableConcept /* Color of container cap */;
      cap?: CodeableConcept /* Specimen container preparation */;
      preparation?: string;
      _description?: Element /* Minimum volume */;
      minimumVolume?: string | Quantity /* Container material */;
      material?: CodeableConcept;
      additive?: Array<{
        /* Additive associated with container */
        additive?: CodeableConcept | Reference<"Substance">;
      }>;
    };
    _requirement?: Element;
  }>;
}
/* An assessment of the likely outcome(s) for a patient or other subject as well as the likelihood of each outcome. */
interface RiskAssessment {
  _mitigation?: Element /* Part of this occurrence */;
  parent?: Reference<ResourceType> /* Where was assessment performed? */;
  encounter?: Reference<"Encounter">;
  prediction?: Array<{
    /* Possible outcome for the subject */
    outcome?: CodeableConcept /* Likelihood of specified outcome */;
    probability?:
      | Range
      | decimal /* Likelihood of specified outcome as a qualitative value */;
    qualitativeRisk?: CodeableConcept /* Relative likelihood */;
    relativeRisk?: decimal;
    _relativeRisk?: Element /* Timeframe or age range */;
    when?: Range | Period /* Explanation of prediction */;
    rationale?: string;
    _rationale?: Element;
  }> /* Evaluation mechanism */;
  method?: CodeableConcept;
  _status?: Element;
  basis?: Array<Reference<ResourceType>>;
  reasonCode?: Array<CodeableConcept> /* How to reduce risk */;
  mitigation?: string;
  note?: Array<Annotation> /* registered | preliminary | final | amended + */;
  status?: code /* Condition assessed */;
  condition?: Reference<"Condition"> /* Type of assessment */;
  code?: CodeableConcept;
  identifier?: Array<Identifier> /* Request fulfilled by this assessment */;
  basedOn?: Reference<ResourceType> /* Who/what does assessment apply to? */;
  subject?: Reference<"Patient" | "Group"> /* When was assessment made? */;
  occurrence?: dateTime | Period /* Who did assessment? */;
  performer?: Reference<"PractitionerRole" | "Device" | "Practitioner">;
  reasonReference?: Array<
    Reference<
      "Observation" | "DocumentReference" | "DiagnosticReport" | "Condition"
    >
  >;
}
/* A formal computable definition of an operation (on the RESTful interface) or a named query (using the search interaction). */
interface OperationDefinition {
  _base?: Element /* Natural language description of the operation definition */;
  description?: markdown;
  _code?: Element;
  _kind?: Element /* Date last changed */;
  date?: dateTime /* Invoke at the system level? */;
  system?: boolean /* Name of the publisher (organization or individual) */;
  publisher?: string /* Invoke on an instance? */;
  instance?: boolean;
  jurisdiction?: Array<CodeableConcept>;
  _publisher?: Element /* Why this operation definition is defined */;
  purpose?: markdown;
  _date?: Element /* Name for this operation definition (computer friendly) */;
  name?: string;
  _outputProfile?: Element;
  _type?: Element;
  _status?: Element;
  _experimental?: Element;
  useContext?: Array<UsageContext> /* Invoke at the type level? */;
  type?: boolean;
  overload?: Array<{
    parameterName?: Array<string>;
    _parameterName?: Array<Element> /* Comments to go on overload */;
    comment?: string;
    _comment?: Element;
  }> /* For testing purposes, not real usage */;
  experimental?: boolean /* Validation information for out parameters */;
  outputProfile?: canonical /* Name for this operation definition (human friendly) */;
  title?: string;
  _description?: Element;
  _purpose?: Element;
  _resource?: Array<Element> /* draft | active | retired | unknown */;
  status?: code;
  resource?: Array<code> /* Whether content is changed by the operation */;
  affectsState?: boolean /* operation | query */;
  kind?: code /* Additional information about use */;
  comment?: markdown;
  _name?: Element /* Canonical identifier for this operation definition, represented as a URI (globally unique) */;
  url?: uri /* Name used to invoke the operation */;
  code?: code;
  _title?: Element;
  _instance?: Element;
  _system?: Element /* Marks this as a profile of the base */;
  base?: canonical /* Business version of the operation definition */;
  version?: string;
  _version?: Element;
  _inputProfile?: Element;
  contact?: Array<ContactDetail> /* Validation information for in parameters */;
  inputProfile?: canonical;
  _comment?: Element;
  _affectsState?: Element;
  parameter?: Array<OperationDefinition>;
  _url?: Element;
}
/* This resource allows for the definition of some activity to be performed, independent of a particular patient, practitioner, or other performance context. */
interface ActivityDefinition {
  observationResultRequirement?: Array<
    Reference<"ObservationDefinition">
  > /* Natural language description of the activity definition */;
  description?: markdown;
  _usage?: Element;
  _kind?: Element /* Date last changed */;
  date?: dateTime /* Transform to apply the template */;
  transform?: canonical;
  endorser?: Array<ContactDetail> /* Name of the publisher (organization or individual) */;
  publisher?: string /* When the activity definition was approved by publisher */;
  approvalDate?: date;
  jurisdiction?: Array<CodeableConcept>;
  dosage?: Array<Dosage>;
  _publisher?: Element;
  observationRequirement?: Array<
    Reference<"ObservationDefinition">
  > /* Why this activity definition is defined */;
  purpose?: markdown;
  _date?: Element;
  _doNotPerform?: Element /* Name for this activity definition (computer friendly) */;
  name?: string;
  _priority?: Element;
  _status?: Element;
  _experimental?: Element;
  useContext?: Array<UsageContext> /* Use and/or publishing restrictions */;
  copyright?: markdown;
  _approvalDate?: Element /* For testing purposes, not real usage */;
  experimental?: boolean;
  topic?: Array<CodeableConcept>;
  participant?: Array<{
    /* patient | practitioner | related-person | device */ type?: code;
    _type?: Element /* E.g. Nurse, Surgeon, Parent, etc. */;
    role?: CodeableConcept;
  }> /* Name for this activity definition (human friendly) */;
  title?: string;
  _description?: Element;
  _profile?: Element;
  library?: Array<canonical>;
  author?: Array<ContactDetail>;
  _intent?: Element;
  _purpose?: Element /* What's administered/supplied */;
  product?:
    | CodeableConcept
    | Reference<
        "Medication" | "Substance"
      > /* Describes the clinical usage of the activity definition */;
  usage?: string;
  _lastReviewDate?: Element /* routine | urgent | asap | stat */;
  priority?: code;
  _transform?: Element /* draft | active | retired | unknown */;
  status?: code /* When activity is to occur */;
  timing?:
    | dateTime
    | Range
    | Period
    | Timing
    | Duration
    | Age /* Subordinate title of the activity definition */;
  subtitle?: string /* Kind of resource */;
  kind?: code;
  _name?: Element;
  dynamicValue?: Array<{
    /* The path to the element to be set dynamically */ path?: string;
    _path?: Element /* An expression that provides the dynamic value for the customization */;
    expression?: Expression;
  }> /* Canonical identifier for this activity definition, represented as a URI (globally unique) */;
  url?: uri /* Detail type of activity */;
  code?: CodeableConcept;
  identifier?: Array<Identifier> /* When the activity definition was last reviewed */;
  lastReviewDate?: date;
  editor?: Array<ContactDetail>;
  _subtitle?: Element /* True if the activity should not be performed */;
  doNotPerform?: boolean;
  bodySite?: Array<CodeableConcept> /* proposal | plan | directive | order | original-order | reflex-order | filler-order | instance-order | option */;
  intent?: code;
  _library?: Array<Element>;
  _copyright?: Element;
  _title?: Element;
  specimenRequirement?: Array<Reference<"SpecimenDefinition">>;
  reviewer?: Array<ContactDetail> /* How much is administered/consumed/supplied */;
  quantity?: Quantity /* Business version of the activity definition */;
  version?: string;
  _version?: Element;
  relatedArtifact?: Array<RelatedArtifact> /* Where it should happen */;
  location?: Reference<"Location">;
  contact?: Array<ContactDetail> /* Type of individual the activity definition is intended for */;
  subject?: CodeableConcept | Reference<"Group">;
  _url?: Element /* What profile the resource needs to conform to */;
  profile?: canonical /* When the activity definition is expected to be used */;
  effectivePeriod?: Period;
}
/* A container for slots of time that may be available for booking appointments. */
interface Schedule {
  _active?: Element;
  serviceCategory?: Array<CodeableConcept>;
  specialty?: Array<CodeableConcept>;
  serviceType?: Array<CodeableConcept> /* Period of time covered by schedule */;
  planningHorizon?: Period /* Whether this schedule is in active use */;
  active?: boolean /* Comments on availability */;
  comment?: string;
  identifier?: Array<Identifier>;
  _comment?: Element;
  actor?: Array<
    Reference<
      | "Patient"
      | "PractitionerRole"
      | "HealthcareService"
      | "Device"
      | "Location"
      | "Practitioner"
      | "RelatedPerson"
    >
  >;
}
/* A material substance originating from a biological entity intended to be transplanted or infused
into another (possibly the same) biological entity. */
interface BiologicallyDerivedProduct {
  _quantity?: Element;
  request?: Array<Reference<"ServiceRequest">>;
  processing?: Array<{
    /* Description of of processing */ description?: string;
    _description?: Element /* Procesing code */;
    procedure?: CodeableConcept /* Substance added during processing */;
    additive?: Reference<"Substance"> /* Time of processing */;
    time?: dateTime | Period;
  }>;
  _productCategory?: Element;
  parent?: Array<Reference<"BiologicallyDerivedProduct">>;
  _status?: Element /* available | unavailable */;
  status?: code;
  identifier?: Array<Identifier> /* What this biologically derived product is */;
  productCode?: CodeableConcept;
  storage?: Array<{
    /* Description of storage */ description?: string;
    _description?: Element /* Storage temperature */;
    temperature?: decimal;
    _temperature?: Element /* farenheit | celsius | kelvin */;
    scale?: code;
    _scale?: Element /* Storage timeperiod */;
    duration?: Period;
  }> /* The amount of this biologically derived product */;
  quantity?: integer /* organ | tissue | fluid | cells | biologicalAgent */;
  productCategory?: code /* Any manipulation of product post-collection */;
  manipulation?: {
    /* Description of manipulation */ description?: string;
    _description?: Element /* Time of manipulation */;
    time?: dateTime | Period;
  } /* How this product was collected */;
  collection?: {
    /* Individual performing collection */
    collector?: Reference<
      "PractitionerRole" | "Practitioner"
    > /* Who is product from */;
    source?: Reference<
      "Patient" | "Organization"
    > /* Time of product collection */;
    collected?: dateTime | Period;
  };
}
/* Represents a defined collection of entities that may be discussed or acted upon collectively but which are not expected to act collectively, and are not formally or legally recognized; i.e. a collection of entities that isn't an Organization. */
interface Group {
  _active?: Element;
  _quantity?: Element /* Label for Group */;
  name?: string;
  _type?: Element /* person | animal | practitioner | device | medication | substance */;
  type?: code;
  member?: Array<{
    /* Reference to the group member */
    entity?: Reference<
      | "Patient"
      | "PractitionerRole"
      | "Medication"
      | "Device"
      | "Substance"
      | "Practitioner"
      | "Group"
    > /* Period member belonged to the group */;
    period?: Period /* If member is no longer in group */;
    inactive?: boolean;
    _inactive?: Element;
  }>;
  characteristic?: Array<{
    /* Kind of characteristic */
    code?: CodeableConcept /* Value held by characteristic */;
    value?:
      | Range
      | CodeableConcept
      | Quantity
      | boolean
      | Reference<ResourceType> /* Group includes or excludes */;
    exclude?: boolean;
    _exclude?: Element /* Period over which characteristic is tested */;
    period?: Period;
  }> /* Whether this group's record is in active use */;
  active?: boolean;
  _name?: Element /* Kind of Group members */;
  code?: CodeableConcept;
  identifier?: Array<Identifier>;
  _actual?: Element /* Number of members */;
  quantity?: unsignedInt /* Entity that is the custodian of the Group's definition */;
  managingEntity?: Reference<
    "PractitionerRole" | "Organization" | "Practitioner" | "RelatedPerson"
  > /* Descriptive or actual */;
  actual?: boolean;
}
/* A pharmaceutical product described in terms of its composition and dose form. */
interface MedicinalProductPharmaceutical {
  identifier?: Array<Identifier> /* The administrable dose form, after necessary reconstitution */;
  administrableDoseForm?: CodeableConcept /* Todo */;
  unitOfPresentation?: CodeableConcept;
  ingredient?: Array<Reference<"MedicinalProductIngredient">>;
  device?: Array<Reference<"DeviceDefinition">>;
  characteristics?: Array<{
    /* A coded characteristic */
    code?: CodeableConcept /* The status of characteristic e.g. assigned or pending */;
    status?: CodeableConcept;
  }>;
  routeOfAdministration?: Array<{
    /* Coded expression for the route */
    code?: CodeableConcept /* The first dose (dose quantity) administered in humans can be specified, for a product under investigation, using a numerical value and its unit of measurement */;
    firstDose?: Quantity /* The maximum single dose that can be administered as per the protocol of a clinical trial can be specified using a numerical value and its unit of measurement */;
    maxSingleDose?: Quantity /* The maximum dose per day (maximum dose quantity to be administered in any one 24-h period) that can be administered as per the protocol referenced in the clinical trial authorisation */;
    maxDosePerDay?: Quantity /* The maximum dose per treatment period that can be administered as per the protocol referenced in the clinical trial authorisation */;
    maxDosePerTreatmentPeriod?: Ratio /* The maximum treatment period during which an Investigational Medicinal Product can be administered as per the protocol referenced in the clinical trial authorisation */;
    maxTreatmentPeriod?: Duration;
    targetSpecies?: Array<{
      /* Coded expression for the species */ code?: CodeableConcept;
      withdrawalPeriod?: Array<{
        /* Coded expression for the type of tissue for which the withdrawal period applues, e.g. meat, milk */
        tissue?: CodeableConcept /* A value for the time */;
        value?: Quantity /* Extra information about the withdrawal period */;
        supportingInformation?: string;
        _supportingInformation?: Element;
      }>;
    }>;
  }>;
}
/* Significant health conditions for a person related to the patient relevant in the context of care for the patient. */
interface FamilyMemberHistory {
  /* Patient history is about */
  patient?: Reference<"Patient"> /* When history was recorded or last updated */;
  date?: dateTime;
  instantiatesCanonical?: Array<canonical>;
  instantiatesUri?: Array<uri> /* (approximate) age */;
  age?: string | Range | Age /* male | female | other | unknown */;
  sex?: CodeableConcept;
  _date?: Element /* The family member described */;
  name?: string;
  _status?: Element /* Relationship to the subject */;
  relationship?: CodeableConcept;
  reasonCode?: Array<CodeableConcept>;
  _estimatedAge?: Element /* Dead? How old/when? */;
  deceased?: date | string | Range | boolean | Age;
  note?: Array<Annotation> /* partial | completed | entered-in-error | health-unknown */;
  status?: code;
  condition?: Array<{
    /* Condition suffered by relation */
    code?: CodeableConcept /* deceased | permanent disability | etc. */;
    outcome?: CodeableConcept /* Whether the condition contributed to the cause of death */;
    contributedToDeath?: boolean;
    _contributedToDeath?: Element /* When condition first manifested */;
    onset?: string | Range | Period | Age;
    note?: Array<Annotation>;
  }>;
  _name?: Element;
  identifier?: Array<Identifier> /* (approximate) date of birth */;
  born?: date | string | Period;
  _instantiatesCanonical?: Array<Element> /* subject-unknown | withheld | unable-to-obtain | deferred */;
  dataAbsentReason?: CodeableConcept;
  reasonReference?: Array<
    Reference<
      | "QuestionnaireResponse"
      | "Observation"
      | "DocumentReference"
      | "DiagnosticReport"
      | "Condition"
      | "AllergyIntolerance"
    >
  >;
  _instantiatesUri?: Array<Element> /* Age is estimated? */;
  estimatedAge?: boolean;
}
/* A record of a request for service such as diagnostic investigations, treatments, or operations to be performed. */
interface ServiceRequest {
  /* Performer role */ performerType?: CodeableConcept;
  category?: Array<CodeableConcept>;
  insurance?: Array<Reference<"ClaimResponse" | "Coverage">>;
  instantiatesCanonical?: Array<canonical>;
  instantiatesUri?: Array<uri>;
  relevantHistory?: Array<Reference<"Provenance">>;
  _patientInstruction?: Element /* Preconditions for service */;
  asNeeded?: CodeableConcept | boolean;
  _authoredOn?: Element;
  supportingInfo?: Array<
    Reference<ResourceType>
  > /* Encounter in which the request was created */;
  encounter?: Reference<"Encounter"> /* Patient or consumer-oriented instructions */;
  patientInstruction?: string;
  _doNotPerform?: Element;
  specimen?: Array<Reference<"Specimen">>;
  _priority?: Element;
  _status?: Element;
  reasonCode?: Array<CodeableConcept> /* Date request signed */;
  authoredOn?: dateTime;
  note?: Array<Annotation>;
  _intent?: Element /* Composite Request ID */;
  requisition?: Identifier;
  locationReference?: Array<
    Reference<"Location">
  > /* Who/what is requesting service */;
  requester?: Reference<
    | "Patient"
    | "PractitionerRole"
    | "Organization"
    | "Device"
    | "Practitioner"
    | "RelatedPerson"
  > /* routine | urgent | asap | stat */;
  priority?: code /* draft | active | on-hold | revoked | completed | entered-in-error | unknown */;
  status?: code /* What is being requested/ordered */;
  code?: CodeableConcept;
  identifier?: Array<Identifier> /* True if service/procedure should not be performed */;
  doNotPerform?: boolean;
  bodySite?: Array<CodeableConcept> /* proposal | plan | directive | order | original-order | reflex-order | filler-order | instance-order | option */;
  intent?: code;
  replaces?: Array<Reference<"ServiceRequest">>;
  orderDetail?: Array<CodeableConcept>;
  basedOn?: Array<
    Reference<"ServiceRequest" | "CarePlan" | "MedicationRequest">
  > /* Service amount */;
  quantity?: Range | Ratio | Quantity;
  locationCode?: Array<CodeableConcept>;
  _instantiatesCanonical?: Array<Element> /* Individual or Entity the service is ordered for */;
  subject?: Reference<
    "Patient" | "Device" | "Location" | "Group"
  > /* When service should occur */;
  occurrence?: dateTime | Period | Timing;
  performer?: Array<
    Reference<
      | "CareTeam"
      | "Patient"
      | "PractitionerRole"
      | "HealthcareService"
      | "Organization"
      | "Device"
      | "Practitioner"
      | "RelatedPerson"
    >
  >;
  reasonReference?: Array<
    Reference<
      "Observation" | "DocumentReference" | "DiagnosticReport" | "Condition"
    >
  >;
  _instantiatesUri?: Array<Element>;
}
/* Indicates an actual or potential clinical issue with or between one or more active or proposed clinical actions for a patient; e.g. Drug-drug interaction, Ineffective treatment frequency, Procedure-condition conflict, etc. */
interface DetectedIssue {
  /* Associated patient */ patient?: Reference<"Patient">;
  _reference?: Element;
  evidence?: Array<{
    code?: Array<CodeableConcept>;
    detail?: Array<Reference<ResourceType>>;
  }>;
  _status?: Element;
  mitigation?: Array<{
    /* What mitigation? */ action?: CodeableConcept /* Date committed */;
    date?: dateTime;
    _date?: Element /* Who is committing? */;
    author?: Reference<"PractitionerRole" | "Practitioner">;
  }> /* The provider or device that identified the issue */;
  author?: Reference<
    "PractitionerRole" | "Device" | "Practitioner"
  > /* Authority for issue */;
  reference?: uri /* registered | preliminary | final | amended + */;
  status?: code /* high | moderate | low */;
  severity?: code /* Issue Category, e.g. drug-drug, duplicate therapy, etc. */;
  code?: CodeableConcept;
  identifier?: Array<Identifier>;
  _severity?: Element /* When identified */;
  identified?: dateTime | Period;
  implicated?: Array<Reference<ResourceType>>;
  _detail?: Element /* Description and context */;
  detail?: string;
}
/* A type of a manufactured item that is used in the provision of healthcare without being substantially changed through that activity. The device may be a medical or non-medical device. */
interface Device {
  /* Patient to whom Device is affixed */
  patient?: Reference<"Patient"> /* The reference to the definition for the device */;
  definition?: Reference<"DeviceDefinition"> /* Serial number assigned by the manufacturer */;
  serialNumber?: string /* The parent device */;
  parent?: Reference<"Device">;
  deviceName?: Array<{
    /* The name of the device */ name?: string;
    _name?: Element /* udi-label-name | user-friendly-name | patient-reported-name | manufacturer-name | model-name | other */;
    type?: code;
    _type?: Element;
  }>;
  _partNumber?: Element;
  property?: Array<{
    /* Code that specifies the property DeviceDefinitionPropetyCode (Extensible) */
    type?: CodeableConcept;
    valueQuantity?: Array<Quantity>;
    valueCode?: Array<CodeableConcept>;
  }>;
  _modelNumber?: Element;
  _status?: Element /* The part number of the device */;
  partNumber?: string /* The model number for the device */;
  modelNumber?: string;
  _manufactureDate?: Element /* The kind or type of device */;
  type?: CodeableConcept;
  statusReason?: Array<CodeableConcept>;
  specialization?: Array<{
    /* The standard that is used to operate and communicate */
    systemType?: CodeableConcept /* The version of the standard that is used to operate and communicate */;
    version?: string;
    _version?: Element;
  }>;
  _expirationDate?: Element /* The distinct identification string */;
  distinctIdentifier?: string;
  note?: Array<Annotation> /* active | inactive | entered-in-error | unknown */;
  status?: code;
  safety?: Array<CodeableConcept> /* Lot number of manufacture */;
  lotNumber?: string /* Network address to contact device */;
  url?: uri;
  _distinctIdentifier?: Element;
  identifier?: Array<Identifier> /* Name of device manufacturer */;
  manufacturer?: string;
  _manufacturer?: Element;
  _serialNumber?: Element;
  version?: Array<{
    /* The type of the device version */
    type?: CodeableConcept /* A single component of the device version */;
    component?: Identifier /* The version text */;
    value?: string;
    _value?: Element;
  }>;
  _lotNumber?: Element /* Where the device is found */;
  location?: Reference<"Location">;
  contact?: Array<ContactPoint> /* Date when the device was made */;
  manufactureDate?: dateTime /* Organization responsible for device */;
  owner?: Reference<"Organization">;
  _url?: Element /* Date and time of expiry of this device (if applicable) */;
  expirationDate?: dateTime;
  udiCarrier?: Array<{
    _jurisdiction?: Element /* Mandatory fixed portion of UDI */;
    deviceIdentifier?: string;
    _entryType?: Element /* Regional UDI authority */;
    jurisdiction?: uri /* barcode | rfid | manual + */;
    entryType?: code /* UDI Issuing Organization */;
    issuer?: uri;
    _deviceIdentifier?: Element /* UDI Machine Readable Barcode String */;
    carrierAIDC?: base64Binary;
    _carrierHRF?: Element;
    _carrierAIDC?: Element;
    _issuer?: Element /* UDI Human Readable Barcode String */;
    carrierHRF?: string;
  }>;
}
/* A group of related requests that can be used to capture intended activities that have inter-dependencies such as "give this medication after that one". */
interface RequestGroup {
  instantiatesCanonical?: Array<canonical>;
  instantiatesUri?: Array<uri>;
  _authoredOn?: Element /* Created as part of */;
  encounter?: Reference<"Encounter">;
  _priority?: Element;
  _status?: Element;
  reasonCode?: Array<CodeableConcept> /* When the request group was authored */;
  authoredOn?: dateTime;
  note?: Array<Annotation> /* Device or practitioner that authored the request group */;
  author?: Reference<"PractitionerRole" | "Device" | "Practitioner">;
  _intent?: Element /* routine | urgent | asap | stat */;
  priority?: code /* draft | active | on-hold | revoked | completed | entered-in-error | unknown */;
  status?: code /* Composite request this is part of */;
  groupIdentifier?: Identifier /* What's being requested/ordered */;
  code?: CodeableConcept;
  identifier?: Array<Identifier> /* proposal | plan | directive | order | original-order | reflex-order | filler-order | instance-order | option */;
  intent?: code;
  action?: Array<RequestGroup>;
  replaces?: Array<Reference<ResourceType>>;
  basedOn?: Array<Reference<ResourceType>>;
  _instantiatesCanonical?: Array<Element> /* Who the request group is about */;
  subject?: Reference<"Patient" | "Group">;
  reasonReference?: Array<
    Reference<
      "Observation" | "DocumentReference" | "DiagnosticReport" | "Condition"
    >
  >;
  _instantiatesUri?: Array<Element>;
}
/* A structured set of tests against a FHIR server or client implementation to determine compliance against the FHIR specification. */
interface TestScript {
  /* Natural language description of the test script */
  description?: markdown /* Date last changed */;
  date?: dateTime;
  variable?: Array<{
    /* Natural language description of the variable */
    description?: string /* XPath or JSONPath against the fixture body */;
    path?: string /* HTTP header field name for source */;
    headerField?: string /* The FHIRPath expression against the fixture body */;
    expression?: string;
    _path?: Element /* Descriptive name for this variable */;
    name?: string;
    _hint?: Element /* Hint help text for default value to enter */;
    hint?: string;
    _expression?: Element;
    _description?: Element;
    _defaultValue?: Element;
    _headerField?: Element;
    _name?: Element /* Fixture Id of source expression or headerField within this variable */;
    sourceId?: id;
    _sourceId?: Element /* Default, hard-coded, or user-defined value for this variable */;
    defaultValue?: string;
  }> /* Name of the publisher (organization or individual) */;
  publisher?: string;
  jurisdiction?: Array<CodeableConcept>;
  _publisher?: Element /* Why this test script is defined */;
  purpose?: markdown;
  _date?: Element /* Name for this test script (computer friendly) */;
  name?: string;
  _status?: Element;
  _experimental?: Element;
  useContext?: Array<UsageContext> /* Use and/or publishing restrictions */;
  copyright?: markdown /* For testing purposes, not real usage */;
  experimental?: boolean /* Name for this test script (human friendly) */;
  title?: string;
  _description?: Element /* A series of required setup operations before tests are executed */;
  setup?: { action?: Array<{ operation?: TestScript; assert?: TestScript }> };
  _purpose?: Element /* draft | active | retired | unknown */;
  status?: code;
  _name?: Element /* Canonical identifier for this test script, represented as a URI (globally unique) */;
  url?: uri /* Additional identifier for the test script */;
  identifier?: Identifier;
  _copyright?: Element;
  origin?: Array<{
    /* The index of the abstract origin server starting at 1 */ index?: integer;
    _index?: Element /* FHIR-Client | FHIR-SDC-FormFiller */;
    profile?: Coding;
  }>;
  _title?: Element;
  fixture?: Array<{
    /* Whether or not to implicitly create the fixture during setup */
    autocreate?: boolean;
    _autocreate?: Element /* Whether or not to implicitly delete the fixture during teardown */;
    autodelete?: boolean;
    _autodelete?: Element /* Reference of the resource */;
    resource?: Reference<ResourceType>;
  }> /* Business version of the test script */;
  version?: string;
  _version?: Element /* A series of required clean up steps */;
  teardown?: {
    action?: Array<{
      /* The teardown operation to perform */ operation?: TestScript;
    }>;
  };
  contact?: Array<ContactDetail> /* Required capability that is assumed to function correctly on the FHIR server being tested */;
  metadata?: {
    link?: Array<{
      /* URL to the specification */ url?: uri;
      _url?: Element /* Short description */;
      description?: string;
      _description?: Element;
    }>;
    capability?: Array<{
      /* The expected capabilities of the server */
      description?: string /* Required Capability Statement */;
      capabilities?: canonical;
      _destination?: Element;
      _link?: Array<Element>;
      _origin?: Array<Element>;
      _required?: Element;
      _description?: Element;
      _validated?: Element;
      link?: Array<uri>;
      _capabilities?: Element;
      origin?: Array<integer> /* Are the capabilities validated? */;
      validated?: boolean /* Which server these requirements apply to */;
      destination?: integer /* Are the capabilities required? */;
      required?: boolean;
    }>;
  };
  destination?: Array<{
    /* The index of the abstract destination server starting at 1 */
    index?: integer;
    _index?: Element /* FHIR-Server | FHIR-SDC-FormManager | FHIR-SDC-FormReceiver | FHIR-SDC-FormProcessor */;
    profile?: Coding;
  }>;
  test?: Array<{
    /* Tracking/logging name of this test */ name?: string;
    _name?: Element /* Tracking/reporting short description of the test */;
    description?: string;
    _description?: Element;
    action?: Array<{
      /* The setup operation to perform */
      operation?: TestScript /* The setup assertion to perform */;
      assert?: TestScript;
    }>;
  }>;
  _url?: Element;
  profile?: Array<Reference<ResourceType>>;
}
/* The RiskEvidenceSynthesis resource describes the likelihood of an outcome in a population plus exposure state where the risk estimate is derived from a combination of research studies. */
interface RiskEvidenceSynthesis {
  /* Natural language description of the risk evidence synthesis */
  description?: markdown /* Date last changed */;
  date?: dateTime;
  endorser?: Array<ContactDetail> /* Name of the publisher (organization or individual) */;
  publisher?: string /* When the risk evidence synthesis was approved by publisher */;
  approvalDate?: date;
  jurisdiction?: Array<CodeableConcept>;
  _publisher?: Element;
  _date?: Element /* What sample size was involved? */;
  sampleSize?: {
    /* Description of sample size */ description?: string;
    _description?: Element /* How many studies? */;
    numberOfStudies?: integer;
    _numberOfStudies?: Element /* How many participants? */;
    numberOfParticipants?: integer;
    _numberOfParticipants?: Element;
  } /* Name for this risk evidence synthesis (computer friendly) */;
  name?: string;
  _status?: Element;
  useContext?: Array<UsageContext> /* Use and/or publishing restrictions */;
  copyright?: markdown /* Type of study */;
  studyType?: CodeableConcept;
  _approvalDate?: Element /* What outcome? */;
  outcome?: Reference<"EvidenceVariable">;
  topic?: Array<CodeableConcept> /* Name for this risk evidence synthesis (human friendly) */;
  title?: string;
  _description?: Element;
  note?: Array<Annotation>;
  author?: Array<ContactDetail> /* Type of synthesis */;
  synthesisType?: CodeableConcept;
  _lastReviewDate?: Element /* draft | active | retired | unknown */;
  status?: code /* What population? */;
  population?: Reference<"EvidenceVariable">;
  _name?: Element /* Canonical identifier for this risk evidence synthesis, represented as a URI (globally unique) */;
  url?: uri;
  identifier?: Array<Identifier> /* When the risk evidence synthesis was last reviewed */;
  lastReviewDate?: date;
  editor?: Array<ContactDetail>;
  _copyright?: Element;
  _title?: Element;
  certainty?: Array<{
    rating?: Array<CodeableConcept>;
    note?: Array<Annotation>;
    certaintySubcomponent?: Array<{
      /* Type of subcomponent of certainty rating */ type?: CodeableConcept;
      rating?: Array<CodeableConcept>;
      note?: Array<Annotation>;
    }>;
  }>;
  reviewer?: Array<ContactDetail> /* What exposure? */;
  exposure?: Reference<"EvidenceVariable"> /* Business version of the risk evidence synthesis */;
  version?: string;
  _version?: Element;
  relatedArtifact?: Array<RelatedArtifact>;
  contact?: Array<ContactDetail> /* What was the estimated risk */;
  riskEstimate?: {
    /* Description of risk estimate */ description?: string;
    precisionEstimate?: Array<{
      /* Type of precision estimate */
      type?: CodeableConcept /* Level of confidence interval */;
      level?: decimal;
      _level?: Element /* Lower bound */;
      from?: decimal;
      _from?: Element /* Upper bound */;
      to?: decimal;
      _to?: Element;
    }> /* Point estimate */;
    value?: decimal /* Number with the outcome */;
    numeratorCount?: integer /* Type of risk estimate */;
    type?: CodeableConcept;
    _value?: Element /* Sample size for group measured */;
    denominatorCount?: integer;
    _description?: Element /* What unit is the outcome described in? */;
    unitOfMeasure?: CodeableConcept;
    _numeratorCount?: Element;
    _denominatorCount?: Element;
  };
  _url?: Element /* When the risk evidence synthesis is expected to be used */;
  effectivePeriod?: Period;
}
/* A record of a request for a medication, substance or device used in the healthcare setting. */
interface SupplyRequest {
  /* The kind of supply (central, non-stock, etc.) */
  category?: CodeableConcept;
  supplier?: Array<
    Reference<"HealthcareService" | "Organization">
  > /* The destination of the supply */;
  deliverTo?: Reference<"Patient" | "Organization" | "Location">;
  _authoredOn?: Element /* Medication, Substance, or Device requested to be supplied */;
  item?: CodeableConcept | Reference<"Medication" | "Device" | "Substance">;
  _priority?: Element;
  _status?: Element;
  reasonCode?: Array<CodeableConcept> /* When the request was made */;
  authoredOn?: dateTime /* The origin of the supply */;
  deliverFrom?: Reference<
    "Organization" | "Location"
  > /* Individual making the request */;
  requester?: Reference<
    | "Patient"
    | "PractitionerRole"
    | "Organization"
    | "Device"
    | "Practitioner"
    | "RelatedPerson"
  > /* routine | urgent | asap | stat */;
  priority?: code /* draft | active | suspended + */;
  status?: code;
  identifier?: Array<Identifier> /* The requested amount of the item indicated */;
  quantity?: Quantity;
  parameter?: Array<{
    /* Item detail */ code?: CodeableConcept /* Value of detail */;
    value?: Range | CodeableConcept | Quantity | boolean;
  }> /* When the request should be fulfilled */;
  occurrence?: dateTime | Period | Timing;
  reasonReference?: Array<
    Reference<
      "Observation" | "DocumentReference" | "DiagnosticReport" | "Condition"
    >
  >;
}
/* A task to be performed. */
interface Task {
  /* Constraints on fulfillment tasks */
  restriction?: {
    /* How many times to repeat */ repetitions?: positiveInt;
    _repetitions?: Element /* When fulfillment sought */;
    period?: Period;
    recipient?: Array<
      Reference<
        | "Patient"
        | "PractitionerRole"
        | "Organization"
        | "Practitioner"
        | "RelatedPerson"
        | "Group"
      >
    >;
  } /* Human-readable explanation of task */;
  description?: string;
  performerType?: Array<CodeableConcept> /* Start and end time of execution */;
  executionPeriod?: Period;
  insurance?: Array<
    Reference<"ClaimResponse" | "Coverage">
  > /* Formal definition of task */;
  instantiatesCanonical?: canonical /* Formal definition of task */;
  instantiatesUri?: uri;
  relevantHistory?: Array<Reference<"Provenance">>;
  _authoredOn?: Element /* Healthcare event during which this task originated */;
  encounter?: Reference<"Encounter">;
  _priority?: Element;
  _status?: Element /* Why task is needed */;
  reasonCode?: CodeableConcept /* Reason for current status */;
  statusReason?: CodeableConcept /* Task Creation Date */;
  authoredOn?: dateTime;
  output?: Array<{
    /* Label for output */ type?: CodeableConcept /* Result of output */;
    value?:
      | unsignedInt
      | Signature
      | markdown
      | date
      | Dosage
      | ContactDetail
      | RelatedArtifact
      | instant
      | UsageContext
      | time
      | DataRequirement
      | base64Binary
      | Meta
      | Distance
      | SampledData
      | TriggerDefinition
      | Identifier
      | string
      | Address
      | Expression
      | dateTime
      | Range
      | integer
      | Ratio
      | oid
      | ContactPoint
      | Money
      | decimal
      | id
      | Attachment
      | Contributor
      | Period
      | canonical
      | url
      | code
      | HumanName
      | positiveInt
      | ParameterDefinition
      | Coding
      | Timing
      | Duration
      | uri
      | CodeableConcept
      | uuid
      | Count
      | Quantity
      | boolean
      | Annotation
      | Age
      | Reference<ResourceType>;
  }> /* E.g. "Specimen collected", "IV prepped" */;
  businessStatus?: CodeableConcept;
  _description?: Element;
  note?: Array<Annotation>;
  _intent?: Element /* Beneficiary of the Task */;
  for?: Reference<ResourceType> /* Who is asking for task to be done */;
  requester?: Reference<
    | "Patient"
    | "PractitionerRole"
    | "Organization"
    | "Device"
    | "Practitioner"
    | "RelatedPerson"
  > /* Task Last Modified Date */;
  lastModified?: dateTime /* routine | urgent | asap | stat */;
  priority?: code /* draft | requested | received | accepted | + */;
  status?: code /* Requisition or grouper id */;
  groupIdentifier?: Identifier /* Task Type */;
  code?: CodeableConcept;
  identifier?: Array<Identifier> /* unknown | proposal | plan | order | original-order | reflex-order | filler-order | instance-order | option */;
  intent?: code /* What task is acting on */;
  focus?: Reference<ResourceType>;
  input?: Array<{
    /* Label for the input */
    type?: CodeableConcept /* Content to use in performing the task */;
    value?:
      | unsignedInt
      | Signature
      | markdown
      | date
      | Dosage
      | ContactDetail
      | RelatedArtifact
      | instant
      | UsageContext
      | time
      | DataRequirement
      | base64Binary
      | Meta
      | Distance
      | SampledData
      | TriggerDefinition
      | Identifier
      | string
      | Address
      | Expression
      | dateTime
      | Range
      | integer
      | Ratio
      | oid
      | ContactPoint
      | Money
      | decimal
      | id
      | Attachment
      | Contributor
      | Period
      | canonical
      | url
      | code
      | HumanName
      | positiveInt
      | ParameterDefinition
      | Coding
      | Timing
      | Duration
      | uri
      | CodeableConcept
      | uuid
      | Count
      | Quantity
      | boolean
      | Annotation
      | Age
      | Reference<ResourceType>;
  }>;
  basedOn?: Array<Reference<ResourceType>>;
  partOf?: Array<Reference<"Task">>;
  _lastModified?: Element /* Where task occurs */;
  location?: Reference<"Location">;
  _instantiatesCanonical?: Element /* Responsible individual */;
  owner?: Reference<
    | "CareTeam"
    | "Patient"
    | "PractitionerRole"
    | "HealthcareService"
    | "Organization"
    | "Device"
    | "Practitioner"
    | "RelatedPerson"
  > /* Why task is needed */;
  reasonReference?: Reference<ResourceType>;
  _instantiatesUri?: Element;
}
/* A request to convey information; e.g. the CDS system proposes that an alert be sent to a responsible provider, the CDS system proposes that the public health agency be notified about a reportable condition. */
interface CommunicationRequest {
  category?: Array<CodeableConcept>;
  payload?: Array<{
    /* Message part content */
    content?: string | Attachment | Reference<ResourceType>;
  }>;
  _authoredOn?: Element /* Encounter created as part of */;
  encounter?: Reference<"Encounter">;
  medium?: Array<CodeableConcept>;
  _doNotPerform?: Element;
  _priority?: Element;
  _status?: Element;
  recipient?: Array<
    Reference<
      | "CareTeam"
      | "Patient"
      | "PractitionerRole"
      | "HealthcareService"
      | "Organization"
      | "Device"
      | "Practitioner"
      | "RelatedPerson"
      | "Group"
    >
  >;
  reasonCode?: Array<CodeableConcept> /* Reason for current status */;
  statusReason?: CodeableConcept /* When request transitioned to being actionable */;
  authoredOn?: dateTime;
  note?: Array<Annotation> /* Who/what is requesting service */;
  requester?: Reference<
    | "Patient"
    | "PractitionerRole"
    | "Organization"
    | "Device"
    | "Practitioner"
    | "RelatedPerson"
  > /* routine | urgent | asap | stat */;
  priority?: code /* draft | active | on-hold | revoked | completed | entered-in-error | unknown */;
  status?: code /* Composite request this is part of */;
  groupIdentifier?: Identifier /* Message sender */;
  sender?: Reference<
    | "Patient"
    | "PractitionerRole"
    | "HealthcareService"
    | "Organization"
    | "Device"
    | "Practitioner"
    | "RelatedPerson"
  >;
  identifier?: Array<Identifier> /* True if request is prohibiting action */;
  doNotPerform?: boolean;
  replaces?: Array<Reference<"CommunicationRequest">>;
  basedOn?: Array<Reference<ResourceType>> /* Focus of message */;
  subject?: Reference<"Patient" | "Group"> /* When scheduled */;
  occurrence?: dateTime | Period;
  about?: Array<Reference<ResourceType>>;
  reasonReference?: Array<
    Reference<
      "Observation" | "DocumentReference" | "DiagnosticReport" | "Condition"
    >
  >;
}
/* This resource provides the insurance enrollment details to the insurer regarding a specified coverage. */
interface EnrollmentRequest {
  _created?: Element /* The subject to be enrolled */;
  candidate?: Reference<"Patient">;
  _status?: Element /* Insurance information */;
  coverage?: Reference<"Coverage"> /* Creation date */;
  created?: dateTime /* Target */;
  insurer?: Reference<"Organization"> /* active | cancelled | draft | entered-in-error */;
  status?: code;
  identifier?: Array<Identifier> /* Responsible practitioner */;
  provider?: Reference<"PractitionerRole" | "Organization" | "Practitioner">;
}
/* The ChargeItemDefinition resource provides the properties that apply to the (billing) codes necessary to calculate costs and prices. The properties may differ largely depending on type and realm, therefore this resource gives only a rough structure and requires profiling for each type of billing code system. */
interface ChargeItemDefinition {
  /* Natural language description of the charge item definition */
  description?: markdown /* Date last changed */;
  date?: dateTime /* Name of the publisher (organization or individual) */;
  publisher?: string /* When the charge item definition was approved by publisher */;
  approvalDate?: date;
  propertyGroup?: Array<{
    applicability?: Array<ChargeItemDefinition>;
    priceComponent?: Array<{
      /* base | surcharge | deduction | discount | tax | informational */
      type?: code;
      _type?: Element /* Code identifying the specific component */;
      code?: CodeableConcept /* Factor used for calculating this component */;
      factor?: decimal;
      _factor?: Element /* Monetary amount associated with this component */;
      amount?: Money;
    }>;
  }>;
  instance?: Array<Reference<"Medication" | "Device" | "Substance">>;
  jurisdiction?: Array<CodeableConcept>;
  _publisher?: Element;
  _date?: Element;
  _replaces?: Array<Element>;
  _status?: Element;
  _experimental?: Element;
  useContext?: Array<UsageContext> /* Use and/or publishing restrictions */;
  copyright?: markdown;
  _approvalDate?: Element /* For testing purposes, not real usage */;
  experimental?: boolean /* Name for this charge item definition (human friendly) */;
  title?: string;
  _description?: Element;
  derivedFromUri?: Array<uri>;
  _lastReviewDate?: Element /* draft | active | retired | unknown */;
  status?: code;
  _derivedFromUri?: Array<Element> /* Canonical identifier for this charge item definition, represented as a URI (globally unique) */;
  url?: uri /* Billing codes or product types this definition applies to */;
  code?: CodeableConcept;
  identifier?: Array<Identifier> /* When the charge item definition was last reviewed */;
  lastReviewDate?: date;
  _copyright?: Element;
  _title?: Element;
  replaces?: Array<canonical>;
  partOf?: Array<canonical> /* Business version of the charge item definition */;
  version?: string;
  _version?: Element;
  contact?: Array<ContactDetail>;
  _partOf?: Array<Element>;
  _url?: Element;
  applicability?: Array<ChargeItemDefinition> /* When the charge item definition is expected to be used */;
  effectivePeriod?: Period;
}
/* A homogeneous material with a definite composition. */
interface Substance {
  /* Textual description of the substance, comments */ description?: string;
  category?: Array<CodeableConcept>;
  instance?: Array<{
    /* Identifier of the package/container */
    identifier?: Identifier /* When no longer valid to use */;
    expiry?: dateTime;
    _expiry?: Element /* Amount of substance in the package */;
    quantity?: Quantity;
  }>;
  _status?: Element;
  _description?: Element;
  ingredient?: Array<{
    /* Optional amount (concentration) */
    quantity?: Ratio /* A component of the substance */;
    substance?: CodeableConcept | Reference<"Substance">;
  }> /* active | inactive | entered-in-error */;
  status?: code /* What substance this is */;
  code?: CodeableConcept;
  identifier?: Array<Identifier>;
}
/* Provenance of a resource is a record that describes entities and processes involved in producing and delivering or otherwise influencing that resource. Provenance provides a critical foundation for assessing authenticity, enabling trust, and allowing reproducibility. Provenance assertions are a form of contextual metadata and can themselves become important records with their own provenance. Provenance statement indicates clinical significance in terms of confidence in authenticity, reliability, and trustworthiness, integrity, and stage in lifecycle (e.g. Document Completion - has the artifact been legally authenticated), all of which may impact security, privacy, and trust policies. */
interface Provenance {
  /* When the activity occurred */ occurred?: dateTime | Period;
  signature?: Array<Signature> /* When the activity was recorded / updated */;
  recorded?: instant;
  agent?: Array<Provenance>;
  policy?: Array<uri>;
  reason?: Array<CodeableConcept> /* Activity that occurred */;
  activity?: CodeableConcept;
  _policy?: Array<Element>;
  target?: Array<Reference<ResourceType>>;
  _recorded?: Element /* Where the activity occurred, if relevant */;
  location?: Reference<"Location">;
  entity?: Array<{
    /* derivation | revision | quotation | source | removal */ role?: code;
    _role?: Element /* Identity of entity */;
    what?: Reference<ResourceType>;
    agent?: Array<Provenance>;
  }>;
}
/* A record of a healthcare consumer’s  choices, which permits or denies identified recipient(s) or recipient role(s) to perform one or more actions within a given policy context, for specific purposes and periods of time. */
interface Consent {
  /* Who the consent applies to */ patient?: Reference<"Patient">;
  category?: Array<CodeableConcept>;
  provision?: Consent;
  _dateTime?: Element;
  _status?: Element;
  organization?: Array<Reference<"Organization">>;
  verification?: Array<{
    /* Has been verified */ verified?: boolean;
    _verified?: Element /* Person who verified */;
    verifiedWith?: Reference<
      "Patient" | "RelatedPerson"
    > /* When consent verified */;
    verificationDate?: dateTime;
    _verificationDate?: Element;
  }> /* Source from which this consent is taken */;
  source?:
    | Attachment
    | Reference<
        "QuestionnaireResponse" | "Contract" | "DocumentReference" | "Consent"
      > /* Which of the four areas this resource covers (extensible) */;
  scope?: CodeableConcept;
  policy?: Array<{
    /* Enforcement source for policy */ authority?: uri;
    _authority?: Element /* Specific policy covered by this consent */;
    uri?: uri;
    _uri?: Element;
  }> /* When this Consent was created or indexed */;
  dateTime?: dateTime /* draft | proposed | active | rejected | inactive | entered-in-error */;
  status?: code /* Regulation that this consents to */;
  policyRule?: CodeableConcept;
  identifier?: Array<Identifier>;
  performer?: Array<
    Reference<
      | "Patient"
      | "PractitionerRole"
      | "Organization"
      | "Practitioner"
      | "RelatedPerson"
    >
  >;
}
/* Describes the intention of how one or more practitioners intend to deliver care for a particular patient, group or community for a period of time, possibly limited to care for a specific condition or set of conditions. */
interface CarePlan {
  _created?: Element /* Summary of nature of plan */;
  description?: string;
  category?: Array<CodeableConcept>;
  addresses?: Array<Reference<"Condition">>;
  instantiatesCanonical?: Array<canonical>;
  instantiatesUri?: Array<uri>;
  supportingInfo?: Array<
    Reference<ResourceType>
  > /* Encounter created as part of */;
  encounter?: Reference<"Encounter">;
  _status?: Element;
  goal?: Array<Reference<"Goal">> /* Date record was first recorded */;
  created?: dateTime /* Human-friendly name for the care plan */;
  title?: string;
  _description?: Element;
  note?: Array<Annotation> /* Who is the designated responsible party */;
  author?: Reference<
    | "CareTeam"
    | "Patient"
    | "PractitionerRole"
    | "Organization"
    | "Device"
    | "Practitioner"
    | "RelatedPerson"
  >;
  _intent?: Element;
  activity?: Array<{
    outcomeCodeableConcept?: Array<CodeableConcept>;
    outcomeReference?: Array<Reference<ResourceType>>;
    progress?: Array<Annotation> /* Activity details defined in specific resource */;
    reference?: Reference<
      | "RequestGroup"
      | "NutritionOrder"
      | "ServiceRequest"
      | "CommunicationRequest"
      | "VisionPrescription"
      | "DeviceRequest"
      | "Task"
      | "MedicationRequest"
      | "Appointment"
    > /* In-line definition of activity */;
    detail?: {
      /* Extra info describing activity to perform */ description?: string;
      _kind?: Element;
      instantiatesCanonical?: Array<canonical>;
      instantiatesUri?: Array<uri>;
      _doNotPerform?: Element;
      _status?: Element;
      goal?: Array<Reference<"Goal">>;
      reasonCode?: Array<CodeableConcept> /* Reason for current status */;
      statusReason?: CodeableConcept /* How to consume/day? */;
      dailyAmount?: Quantity;
      _description?: Element /* What is to be administered/supplied */;
      product?:
        | CodeableConcept
        | Reference<
            "Medication" | "Substance"
          > /* not-started | scheduled | in-progress | on-hold | completed | cancelled | stopped | unknown | entered-in-error */;
      status?: code /* Appointment | CommunicationRequest | DeviceRequest | MedicationRequest | NutritionOrder | Task | ServiceRequest | VisionPrescription */;
      kind?: code /* Detail type of activity */;
      code?: CodeableConcept /* If true, activity is prohibiting action */;
      doNotPerform?: boolean /* When activity is to occur */;
      scheduled?:
        | string
        | Period
        | Timing /* How much to administer/supply/consume */;
      quantity?: Quantity /* Where it should happen */;
      location?: Reference<"Location">;
      _instantiatesCanonical?: Array<Element>;
      performer?: Array<
        Reference<
          | "CareTeam"
          | "Patient"
          | "PractitionerRole"
          | "HealthcareService"
          | "Organization"
          | "Device"
          | "Practitioner"
          | "RelatedPerson"
        >
      >;
      reasonReference?: Array<
        Reference<
          "Observation" | "DocumentReference" | "DiagnosticReport" | "Condition"
        >
      >;
      _instantiatesUri?: Array<Element>;
    };
  }>;
  contributor?: Array<
    Reference<
      | "CareTeam"
      | "Patient"
      | "PractitionerRole"
      | "Organization"
      | "Device"
      | "Practitioner"
      | "RelatedPerson"
    >
  > /* draft | active | on-hold | revoked | completed | entered-in-error | unknown */;
  status?: code;
  identifier?: Array<Identifier> /* proposal | plan | order | option */;
  intent?: code;
  _title?: Element;
  replaces?: Array<Reference<"CarePlan">> /* Time period plan covers */;
  period?: Period;
  basedOn?: Array<Reference<"CarePlan">>;
  partOf?: Array<Reference<"CarePlan">>;
  _instantiatesCanonical?: Array<Element> /* Who the care plan is for */;
  subject?: Reference<"Patient" | "Group">;
  careTeam?: Array<Reference<"CareTeam">>;
  _instantiatesUri?: Array<Element>;
}
/* A summary of information based on the results of executing a TestScript. */
interface TestReport {
  _issued?: Element /* Name of the tester producing this report (Organization or individual) */;
  tester?: string /* Informal name of the executed TestScript */;
  name?: string;
  _status?: Element;
  _score?: Element /* Reference to the  version-specific TestScript that was executed to produce this TestReport */;
  testScript?: Reference<"TestScript">;
  participant?: Array<{
    /* test-engine | client | server */ type?: code;
    _type?: Element /* The uri of the participant. An absolute URL is preferred */;
    uri?: uri;
    _uri?: Element /* The display name of the participant */;
    display?: string;
    _display?: Element;
  }>;
  _result?: Element /* The results of the series of required setup operations before the tests were executed */;
  setup?: {
    action?: Array<{ operation?: TestReport; assert?: TestReport }>;
  } /* completed | in-progress | waiting | stopped | entered-in-error */;
  status?: code /* pass | fail | pending */;
  result?: code /* The final score (percentage of tests passed) resulting from the execution of the TestScript */;
  score?: decimal;
  _name?: Element /* External identifier */;
  identifier?: Identifier;
  _tester?: Element /* When the TestScript was executed and this TestReport was generated */;
  issued?: dateTime /* The results of running the series of required clean up steps */;
  teardown?: {
    action?: Array<{
      /* The teardown operation performed */ operation?: TestReport;
    }>;
  };
  test?: Array<{
    /* Tracking/logging name of this test */ name?: string;
    _name?: Element /* Tracking/reporting short description of the test */;
    description?: string;
    _description?: Element;
    action?: Array<{
      /* The operation performed */
      operation?: TestReport /* The assertion performed */;
      assert?: TestReport;
    }>;
  }>;
}
/* Measurements and simple assertions made about a patient, device or other subject. */
interface Observation {
  category?: Array<CodeableConcept>;
  _issued?: Element;
  referenceRange?: Array<Observation>;
  hasMember?: Array<
    Reference<"QuestionnaireResponse" | "Observation" | "MolecularSequence">
  >;
  derivedFrom?: Array<
    Reference<
      | "Media"
      | "QuestionnaireResponse"
      | "ImagingStudy"
      | "Observation"
      | "DocumentReference"
      | "MolecularSequence"
    >
  >;
  interpretation?: Array<CodeableConcept> /* Healthcare event during which this observation is made */;
  encounter?: Reference<"Encounter"> /* How it was done */;
  method?: CodeableConcept /* Specimen used for this observation */;
  specimen?: Reference<"Specimen"> /* Actual result */;
  value?:
    | time
    | SampledData
    | string
    | dateTime
    | Range
    | integer
    | Ratio
    | Period
    | CodeableConcept
    | Quantity
    | boolean;
  _status?: Element;
  component?: Array<{
    /* Type of component observation (code / type) */
    code?: CodeableConcept /* Actual component result */;
    value?:
      | time
      | SampledData
      | string
      | dateTime
      | Range
      | integer
      | Ratio
      | Period
      | CodeableConcept
      | Quantity
      | boolean /* Why the component result is missing */;
    dataAbsentReason?: CodeableConcept;
    interpretation?: Array<CodeableConcept>;
    referenceRange?: Array<Observation>;
  }>;
  note?: Array<Annotation> /* registered | preliminary | final | amended + */;
  status?: code /* Clinically relevant time/time-period for observation */;
  effective?:
    | instant
    | dateTime
    | Period
    | Timing /* Type of observation (code / type) */;
  code?: CodeableConcept;
  identifier?: Array<Identifier> /* Observed body part */;
  bodySite?: CodeableConcept;
  focus?: Array<
    Reference<ResourceType>
  > /* Date/Time this version was made available */;
  issued?: instant /* (Measurement) Device */;
  device?: Reference<"Device" | "DeviceMetric">;
  basedOn?: Array<
    Reference<
      | "NutritionOrder"
      | "ServiceRequest"
      | "CarePlan"
      | "ImmunizationRecommendation"
      | "DeviceRequest"
      | "MedicationRequest"
    >
  >;
  partOf?: Array<
    Reference<
      | "MedicationDispense"
      | "MedicationAdministration"
      | "ImagingStudy"
      | "Procedure"
      | "Immunization"
      | "MedicationStatement"
    >
  > /* Who and/or what the observation is about */;
  subject?: Reference<"Patient" | "Device" | "Location" | "Group">;
  performer?: Array<
    Reference<
      | "CareTeam"
      | "Patient"
      | "PractitionerRole"
      | "Organization"
      | "Practitioner"
      | "RelatedPerson"
    >
  > /* Why the result is missing */;
  dataAbsentReason?: CodeableConcept;
}
/* A collection of documents compiled for a purpose together with metadata that applies to the collection. */
interface DocumentManifest {
  _created?: Element /* Human-readable description (title) */;
  description?: string;
  content?: Array<Reference<ResourceType>>;
  _status?: Element;
  recipient?: Array<
    Reference<
      | "Patient"
      | "PractitionerRole"
      | "Organization"
      | "Practitioner"
      | "RelatedPerson"
    >
  > /* Kind of document set */;
  type?: CodeableConcept /* When this document manifest created */;
  created?: dateTime;
  related?: Array<{
    /* Identifiers of things that are related */
    identifier?: Identifier /* Related Resource */;
    ref?: Reference<ResourceType>;
  }> /* The source system/application/software */;
  source?: uri;
  _description?: Element;
  author?: Array<
    Reference<
      | "Patient"
      | "PractitionerRole"
      | "Organization"
      | "Device"
      | "Practitioner"
      | "RelatedPerson"
    >
  > /* Unique Identifier for the set of documents */;
  masterIdentifier?: Identifier /* current | superseded | entered-in-error */;
  status?: code;
  identifier?: Array<Identifier>;
  _source?: Element /* The subject of the set of documents */;
  subject?: Reference<"Patient" | "Device" | "Practitioner" | "Group">;
}

/* Base StructureDefinition for Age Type: A duration of time during which an organism (or a process) has existed. */
type Age = Quantity;

/* The Human Language of the item. */
type language = code;
/* Base StructureDefinition for string Type: A sequence of Unicode characters */
/* Base StructureDefinition for string Type: A sequence of Unicode characters */

/* Base StructureDefinition for markdown type: A string that may contain Github Flavored Markdown syntax for optional processing by a mark down presentation engine */
type markdown =
  string; /* Base StructureDefinition for markdown type: A string that may contain Github Flavored Markdown syntax for optional processing by a mark down presentation engine */

/* Base StructureDefinition for date Type: A date or partial date (e.g. just year or year + month). There is no time zone. The format is a union of the schema types gYear, gYearMonth and date.  Dates SHALL be valid dates. */
type date =
  string; /* Base StructureDefinition for date Type: A date or partial date (e.g. just year or year + month). There is no time zone. The format is a union of the schema types gYear, gYearMonth and date.  Dates SHALL be valid dates. */

/* Base StructureDefinition for ProductShelfLife Type: The shelf-life and storage information for a medicinal product item or container can be described using this class. */
interface ProductShelfLife {
  /* Unique identifier for the packaged Medicinal Product */
  identifier?: Identifier /* This describes the shelf life, taking into account various scenarios such as shelf life of the packaged Medicinal Product itself, shelf life after transformation where necessary and shelf life after the first opening of a bottle, etc. The shelf life type shall be specified using an appropriate controlled vocabulary The controlled term and the controlled term identifier shall be specified */;
  type?: CodeableConcept /* The shelf life time period can be specified using a numerical value for the period of time and its unit of time measurement The unit of measurement shall be specified in accordance with ISO 11240 and the resulting terminology The symbol and the symbol identifier shall be used */;
  period?: Quantity;
  specialPrecautionsForStorage?: Array<CodeableConcept>;
}

/* Base StructureDefinition for Duration Type: A length of time. */
type Duration = Quantity;
/* Base StructureDefinition for Dosage Type: Indicates how the medication is/was taken or should be taken by the patient. */
interface Dosage {
  _sequence?: Element /* Body site to administer to */;
  site?: CodeableConcept;
  _patientInstruction?: Element /* Take "as needed" (for x) */;
  asNeeded?:
    | CodeableConcept
    | boolean /* Technique for administering medication */;
  method?: CodeableConcept /* Patient or consumer oriented instructions */;
  patientInstruction?: string /* Upper limit on medication per lifetime of the patient */;
  maxDosePerLifetime?: Quantity /* Upper limit on medication per administration */;
  maxDosePerAdministration?: Quantity /* How drug should enter body */;
  route?: CodeableConcept /* When medication should be administered */;
  timing?: Timing;
  additionalInstruction?: Array<CodeableConcept> /* The order of the dosage instructions */;
  sequence?: integer /* Upper limit on medication per unit of time */;
  maxDosePerPeriod?: Ratio;
  _text?: Element;
  doseAndRate?: Array<{
    /* The kind of dose or rate specified */
    type?: CodeableConcept /* Amount of medication per dose */;
    dose?: Range | Quantity /* Amount of medication per unit of time */;
    rate?: Range | Ratio | Quantity;
  }> /* Free text dosage instructions e.g. SIG */;
  text?: string;
}

/* Base StructureDefinition for url type: A URI that is a literal reference */
type url =
  uri; /* Base StructureDefinition for url type: A URI that is a literal reference */

/* Record details about the anatomical location of a specimen or body part. This resource may be used when a coded concept does not provide the necessary detail needed for the use case. */
type bodySite = Reference<"BodyStructure">;

/* For attachment answers, indicates the maximum size an attachment can be. */
type maxSize = decimal;

/* Common Ancestor declaration for conformance and knowledge artifact resources. */
interface MetadataResource {
  /* Natural language description of the metadata resource */
  description?: markdown /* Date last changed */;
  date?: dateTime /* Name of the publisher (organization or individual) */;
  publisher?: string;
  jurisdiction?: Array<CodeableConcept>;
  _publisher?: Element;
  _date?: Element /* Name for this metadata resource (computer friendly) */;
  name?: string;
  _status?: Element;
  _experimental?: Element;
  useContext?: Array<UsageContext> /* For testing purposes, not real usage */;
  experimental?: boolean /* Name for this metadata resource (human friendly) */;
  title?: string;
  _description?: Element /* draft | active | retired | unknown */;
  status?: code;
  _name?: Element /* Canonical identifier for this metadata resource, represented as a URI (globally unique) */;
  url?: uri;
  _title?: Element /* Business version of the metadata resource */;
  version?: string;
  _version?: Element;
  contact?: Array<ContactDetail>;
  _url?: Element;
}

/* Base StructureDefinition for Population Type: A populatioof people with some set of grouping criteria. */
interface Population {
  /* The age of the specific population */
  age?: Range | CodeableConcept /* The gender of the specific population */;
  gender?: CodeableConcept /* Race of the specific population */;
  race?: CodeableConcept /* The existing physiological conditions of the specific population to which this applies */;
  physiologicalCondition?: CodeableConcept;
}

/* Base StructureDefinition for SampledData Type: A series of measurements taken by a device, with upper and lower limits. There may be more than one dimension in the data. */
interface SampledData {
  _period?: Element;
  _data?: Element /* Upper limit of detection */;
  upperLimit?: decimal;
  _lowerLimit?: Element /* Lower limit of detection */;
  lowerLimit?: decimal;
  _factor?: Element;
  _upperLimit?: Element /* Number of sample points at each time point */;
  dimensions?: positiveInt /* Multiply data by this before adding to origin */;
  factor?: decimal /* Zero value and units */;
  origin?: Quantity /* Number of milliseconds between samples */;
  period?: decimal;
  _dimensions?: Element /* Decimal values with spaces, or "E" | "U" | "L" */;
  data?: string;
}

/* Base StructureDefinition for ProdCharacteristic Type: The marketing status describes the date when a medicinal product is actually put on the market or the date as of which it is no longer available. */
interface ProdCharacteristic {
  _color?: Array<Element>;
  imprint?: Array<string>;
  color?: Array<string>;
  _imprint?: Array<Element>;
  _shape?: Element /* Where applicable, the width can be specified using a numerical value and its unit of measurement The unit of measurement shall be specified in accordance with ISO 11240 and the resulting terminology The symbol and the symbol identifier shall be used */;
  width?: Quantity /* Where applicable, the nominal volume can be specified using a numerical value and its unit of measurement The unit of measurement shall be specified in accordance with ISO 11240 and the resulting terminology The symbol and the symbol identifier shall be used */;
  nominalVolume?: Quantity /* Where applicable, the weight can be specified using a numerical value and its unit of measurement The unit of measurement shall be specified in accordance with ISO 11240 and the resulting terminology The symbol and the symbol identifier shall be used */;
  weight?: Quantity /* Where applicable, the shape can be specified An appropriate controlled vocabulary shall be used The term and the term identifier shall be used */;
  shape?: string /* Where applicable, the scoring can be specified An appropriate controlled vocabulary shall be used The term and the term identifier shall be used */;
  scoring?: CodeableConcept;
  image?: Array<Attachment> /* Where applicable, the depth can be specified using a numerical value and its unit of measurement The unit of measurement shall be specified in accordance with ISO 11240 and the resulting terminology The symbol and the symbol identifier shall be used */;
  depth?: Quantity /* Where applicable, the external diameter can be specified using a numerical value and its unit of measurement The unit of measurement shall be specified in accordance with ISO 11240 and the resulting terminology The symbol and the symbol identifier shall be used */;
  externalDiameter?: Quantity /* Where applicable, the height can be specified using a numerical value and its unit of measurement The unit of measurement shall be specified in accordance with ISO 11240 and the resulting terminology The symbol and the symbol identifier shall be used */;
  height?: Quantity;
}

/* Information captured by the author/maintainer of the questionnaire for development purposes, not intended to be seen by users. */
type designNote = markdown;
/* Base StructureDefinition for integer Type: A whole number */
type integer = number;

/* Base StructureDefinition for base64Binary Type: A stream of bytes */
type base64Binary =
  string; /* Base StructureDefinition for base64Binary Type: A stream of bytes */

/* Base StructureDefinition for Extension Type: Optional Extension Element - found in all resources. */
interface Extension {
  /* identifies the meaning of the extension */
  url?: uri /* Value of extension */;
  value?:
    | unsignedInt
    | Signature
    | markdown
    | date
    | Dosage
    | ContactDetail
    | RelatedArtifact
    | instant
    | UsageContext
    | time
    | DataRequirement
    | base64Binary
    | Meta
    | Distance
    | SampledData
    | TriggerDefinition
    | Identifier
    | string
    | Address
    | Expression
    | dateTime
    | Range
    | integer
    | Ratio
    | oid
    | ContactPoint
    | Money
    | decimal
    | id
    | Attachment
    | Contributor
    | Period
    | canonical
    | url
    | code
    | HumanName
    | positiveInt
    | ParameterDefinition
    | Coding
    | Timing
    | Duration
    | uri
    | CodeableConcept
    | uuid
    | Count
    | Quantity
    | boolean
    | Annotation
    | Age
    | Reference<ResourceType>;
}

/* Base StructureDefinition for Ratio Type: A relationship of two Quantity values - expressed as a numerator and a denominator. */
interface Ratio {
  /* Numerator value */ numerator?: Quantity /* Denominator value */;
  denominator?: Quantity;
}

/* Base StructureDefinition for Count Type: A measured amount (or an amount that can potentially be measured). Note that measured amounts include amounts that are not precisely quantified, including amounts involving arbitrary units and floating currencies. */
type Count = Quantity;

/* Base StructureDefinition for ParameterDefinition Type: The parameters to the module. This collection specifies both the input and output parameters. Input parameters are provided by the caller as part of the $evaluate operation. Output parameters are included in the GuidanceResponse. */
interface ParameterDefinition {
  /* Minimum cardinality */ min?: integer;
  _documentation?: Element /* in | out */;
  use?: code /* Name used to access the parameter value */;
  name?: code;
  _type?: Element /* What type of value */;
  type?: code /* A brief description of the parameter */;
  documentation?: string;
  _profile?: Element;
  _min?: Element /* Maximum cardinality (a number of *) */;
  max?: string;
  _name?: Element;
  _max?: Element;
  _use?: Element /* What profile the value is expected to be */;
  profile?: canonical;
}

/* Base StructureDefinition for instant Type: An instant in time - known at least to the second */
type instant =
  string; /* Base StructureDefinition for instant Type: An instant in time - known at least to the second */

/* Base StructureDefinition for ContactDetail Type: Specifies contact information for a person or organization. */
interface ContactDetail {
  /* Name of an individual to contact */ name?: string;
  _name?: Element;
  telecom?: Array<ContactPoint>;
}

/* Base StructureDefinition for Address Type: An address expressed using postal conventions (as opposed to GPS or other location definition formats).  This data type may be used to convey addresses for use in delivering mail as well as for visiting locations which might not be valid for mail delivery.  There are a variety of postal address formats defined around the world. */
interface Address {
  _line?: Array<Element> /* home | work | temp | old | billing - purpose of this address */;
  use?: code /* Name of city, town etc. */;
  city?: string;
  _type?: Element /* postal | physical | both */;
  type?: code;
  _city?: Element /* Sub-unit of country (abbreviations ok) */;
  state?: string;
  _district?: Element;
  _state?: Element;
  line?: Array<string> /* Postal code for area */;
  postalCode?: string;
  _country?: Element;
  _postalCode?: Element;
  _text?: Element /* Time period when address was/is in use */;
  period?: Period /* Country (e.g. can be ISO 3166 2 or 3 letter code) */;
  country?: string;
  _use?: Element /* District name (aka county) */;
  district?: string /* Text representation of the address */;
  text?: string;
}

/* Base StructureDefinition for Coding Type: A reference to a code defined by a terminology system. */
interface Coding {
  /* If this coding was chosen directly by the user */ userSelected?: boolean;
  _code?: Element /* Identity of the terminology system */;
  system?: uri;
  _userSelected?: Element /* Symbol in syntax defined by the system */;
  code?: code /* Representation defined by the system */;
  display?: string;
  _system?: Element /* Version of the system - if relevant */;
  version?: string;
  _version?: Element;
  _display?: Element;
}

/* Base StructureDefinition for ElementDefinition Type: Captures constraints on each element within the resource, profile, or extension. */
interface ElementDefinition {
  constraint?: Array<{
    /* Why this constraint is necessary or appropriate */
    requirements?: string /* FHIRPath expression of constraint */;
    expression?: string /* Target of 'condition' reference above */;
    key?: id;
    _requirements?: Element /* Human description of constraint */;
    human?: string /* XPath expression of constraint */;
    xpath?: string /* Reference to original source of constraint */;
    source?: canonical;
    _expression?: Element;
    _human?: Element /* error | warning */;
    severity?: code;
    _severity?: Element;
    _source?: Element;
    _xpath?: Element;
    _key?: Element;
  }>;
  _representation?: Array<Element> /* Path of the element in the hierarchy of elements */;
  path?: string /* Why this resource has been created */;
  requirements?: markdown /* Minimum Cardinality */;
  min?: unsignedInt /* Full formal definition as narrative text */;
  definition?: markdown /* Maximum Allowed Value (for some types) */;
  maxValue?:
    | unsignedInt
    | date
    | instant
    | time
    | dateTime
    | integer
    | decimal
    | positiveInt
    | Quantity;
  _meaningWhenMissing?: Element;
  _maxLength?: Element /* If this modifies the meaning of other elements */;
  isModifier?: boolean /* Concise definition for space-constrained presentation */;
  short?: string;
  _path?: Element;
  _orderMeaning?: Element;
  _requirements?: Element;
  _mustSupport?: Element;
  _definition?: Element;
  _sliceName?: Element;
  mapping?: Array<{
    /* Reference to mapping declaration */ identity?: id;
    _identity?: Element /* Computable language of mapping */;
    language?: code;
    _language?: Element /* Details of the mapping */;
    map?: string;
    _map?: Element /* Comments about the mapping or its use */;
    comment?: string;
    _comment?: Element;
  }> /* Reference to definition of content for the element */;
  contentReference?: uri;
  _isSummary?: Element;
  _sliceIsConstraining?: Element /* This element is sliced - slices follow */;
  slicing?: {
    discriminator?: Array<{
      /* value | exists | pattern | type | profile */ type?: code;
      _type?: Element /* Path to element value */;
      path?: string;
      _path?: Element;
    }> /* Text description of how slicing works (or not) */;
    description?: string;
    _description?: Element /* If elements must be in same order as slices */;
    ordered?: boolean;
    _ordered?: Element /* closed | open | openAtEnd */;
    rules?: code;
    _rules?: Element;
  };
  type?: Array<{
    _code?: Element;
    _aggregation?: Array<Element>;
    _profile?: Array<Element> /* Data type or Resource (reference to definition) */;
    code?: uri;
    targetProfile?: Array<canonical>;
    aggregation?: Array<code> /* either | independent | specific */;
    versioning?: code;
    _targetProfile?: Array<Element>;
    profile?: Array<canonical>;
    _versioning?: Element;
  }> /* If the element must be supported */;
  mustSupport?: boolean;
  _short?: Element /* Minimum Allowed Value (for some types) */;
  minValue?:
    | unsignedInt
    | date
    | instant
    | time
    | dateTime
    | integer
    | decimal
    | positiveInt
    | Quantity /* Name for this particular element (in a set of slices) */;
  sliceName?: string /* Implicit meaning when this element is missing */;
  meaningWhenMissing?: markdown;
  _contentReference?: Element;
  _min?: Element;
  _label?: Element /* ValueSet details if this is coded */;
  binding?: {
    /* required | extensible | preferred | example */ strength?: code;
    _strength?: Element /* Human explanation of the value set */;
    description?: string;
    _description?: Element /* Source of value set */;
    valueSet?: canonical;
    _valueSet?: Element;
  };
  alias?: Array<string>;
  representation?: Array<code>;
  _isModifier?: Element;
  _condition?: Array<Element> /* Maximum Cardinality (a number or *) */;
  max?: string /* Name for element to display with or prompt for element */;
  label?: string;
  condition?: Array<id> /* Comments about the use of this element */;
  comment?: markdown;
  code?: Array<Coding> /* Max length for strings */;
  maxLength?: integer;
  _isModifierReason?: Element /* If this slice definition constrains an inherited slice definition (or not) */;
  sliceIsConstraining?: boolean;
  example?: Array<{
    /* Describes the purpose of this example */ label?: string;
    _label?: Element /* Value of Example (one of allowed types) */;
    value?:
      | unsignedInt
      | Signature
      | markdown
      | date
      | Dosage
      | ContactDetail
      | RelatedArtifact
      | instant
      | UsageContext
      | time
      | DataRequirement
      | base64Binary
      | Meta
      | Distance
      | SampledData
      | TriggerDefinition
      | Identifier
      | string
      | Address
      | Expression
      | dateTime
      | Range
      | integer
      | Ratio
      | oid
      | ContactPoint
      | Money
      | decimal
      | id
      | Attachment
      | Contributor
      | Period
      | canonical
      | url
      | code
      | HumanName
      | positiveInt
      | ParameterDefinition
      | Coding
      | Timing
      | Duration
      | uri
      | CodeableConcept
      | uuid
      | Count
      | Quantity
      | boolean
      | Annotation
      | Age
      | Reference<ResourceType>;
  }>;
  _max?: Element /* What the order of the elements means */;
  orderMeaning?: string /* Specified value if missing from instance */;
  defaultValue?:
    | unsignedInt
    | Signature
    | markdown
    | date
    | Dosage
    | ContactDetail
    | RelatedArtifact
    | instant
    | UsageContext
    | time
    | DataRequirement
    | base64Binary
    | Meta
    | Distance
    | SampledData
    | TriggerDefinition
    | Identifier
    | string
    | Address
    | Expression
    | dateTime
    | Range
    | integer
    | Ratio
    | oid
    | ContactPoint
    | Money
    | decimal
    | id
    | Attachment
    | Contributor
    | Period
    | canonical
    | url
    | code
    | HumanName
    | positiveInt
    | ParameterDefinition
    | Coding
    | Timing
    | Duration
    | uri
    | CodeableConcept
    | uuid
    | Count
    | Quantity
    | boolean
    | Annotation
    | Age
    | Reference<ResourceType> /* Value must be exactly this */;
  fixed?:
    | unsignedInt
    | Signature
    | markdown
    | date
    | Dosage
    | ContactDetail
    | RelatedArtifact
    | instant
    | UsageContext
    | time
    | DataRequirement
    | base64Binary
    | Meta
    | Distance
    | SampledData
    | TriggerDefinition
    | Identifier
    | string
    | Address
    | Expression
    | dateTime
    | Range
    | integer
    | Ratio
    | oid
    | ContactPoint
    | Money
    | decimal
    | id
    | Attachment
    | Contributor
    | Period
    | canonical
    | url
    | code
    | HumanName
    | positiveInt
    | ParameterDefinition
    | Coding
    | Timing
    | Duration
    | uri
    | CodeableConcept
    | uuid
    | Count
    | Quantity
    | boolean
    | Annotation
    | Age
    | Reference<ResourceType>;
  _alias?: Array<Element> /* Base definition information for tools */;
  base?: {
    /* Path that identifies the base element */ path?: string;
    _path?: Element /* Min cardinality of the base element */;
    min?: unsignedInt;
    _min?: Element /* Max cardinality of the base element */;
    max?: string;
    _max?: Element;
  } /* Reason that this element is marked as a modifier */;
  isModifierReason?: string;
  _comment?: Element /* Include when _summary = true? */;
  isSummary?: boolean /* Value must have at least these property values */;
  pattern?:
    | unsignedInt
    | Signature
    | markdown
    | date
    | Dosage
    | ContactDetail
    | RelatedArtifact
    | instant
    | UsageContext
    | time
    | DataRequirement
    | base64Binary
    | Meta
    | Distance
    | SampledData
    | TriggerDefinition
    | Identifier
    | string
    | Address
    | Expression
    | dateTime
    | Range
    | integer
    | Ratio
    | oid
    | ContactPoint
    | Money
    | decimal
    | id
    | Attachment
    | Contributor
    | Period
    | canonical
    | url
    | code
    | HumanName
    | positiveInt
    | ParameterDefinition
    | Coding
    | Timing
    | Duration
    | uri
    | CodeableConcept
    | uuid
    | Count
    | Quantity
    | boolean
    | Annotation
    | Age
    | Reference<ResourceType>;
}

/* A regular expression that defines the syntax for the data element to be considered valid. */
type regex = string;
/* Base StructureDefinition for Period Type: A time period defined by a start and end date and optionally time. */
interface Period {
  /* Starting time with inclusive boundary */ start?: dateTime;
  _start?: Element /* End time with inclusive boundary, if not ongoing */;
  end?: dateTime;
  _end?: Element;
}

/* Base StructureDefinition for xhtml Type */
type xhtml = string; /* Base StructureDefinition for xhtml Type */

/* Base StructureDefinition for HumanName Type: A human's name with the ability to identify parts and usage. */
interface HumanName {
  _family?: Element;
  suffix?: Array<string>;
  _suffix?: Array<Element>;
  given?: Array<string> /* Family name (often called 'Surname') */;
  family?: string;
  _prefix?: Array<Element> /* usual | official | temp | nickname | anonymous | old | maiden */;
  use?: code;
  prefix?: Array<string>;
  _given?: Array<Element>;
  _text?: Element /* Time period when name was/is in use */;
  period?: Period;
  _use?: Element /* Text representation of the full name */;
  text?: string;
}

/* Identifies the kind(s) of attachment allowed to be sent for an element. */
type mimeType = code;

/* Base StructureDefinition for RelatedArtifact Type: Related artifacts such as additional documentation, justification, or bibliographic references. */
interface RelatedArtifact {
  _type?: Element /* documentation | justification | citation | predecessor | successor | derived-from | depends-on | composed-of */;
  type?: code /* What document is being referenced */;
  document?: Attachment /* Bibliographic citation for the artifact */;
  citation?: markdown;
  _label?: Element;
  _resource?: Element /* Short label */;
  label?: string /* What resource is being referenced */;
  resource?: canonical /* Where the artifact can be accessed */;
  url?: url /* Brief description of the related artifact */;
  display?: string;
  _citation?: Element;
  _url?: Element;
  _display?: Element;
}

/* Base StructureDefinition for Expression Type: A expression that is evaluated in a specified context and returns a value. The context of use of the expression must specify the context in which the expression is evaluated, and how the result of the expression is used. */
interface Expression {
  /* Natural language description of the condition */ description?: string;
  _reference?: Element /* Expression in specified language */;
  expression?: string /* Short name assigned to expression for reuse */;
  name?: id;
  _language?: Element;
  _expression?: Element;
  _description?: Element /* Where the expression is found */;
  reference?: uri /* text/cql | text/fhirpath | application/x-fhir-query | etc. */;
  language?: code;
  _name?: Element;
}

/* Base StructureDefinition for uuid type: A UUID, represented as a URI */
type uuid =
  uri; /* Base StructureDefinition for uuid type: A UUID, represented as a URI */

/* Base StructureDefinition for id type: Any combination of letters, numerals, "-" and ".", with a length limit of 64 characters.  (This might be an integer, an unprefixed OID, UUID or any other identifier pattern that meets these constraints.)  Ids are case-insensitive. */
type id =
  string; /* Base StructureDefinition for id type: Any combination of letters, numerals, "-" and ".", with a length limit of 64 characters.  (This might be an integer, an unprefixed OID, UUID or any other identifier pattern that meets these constraints.)  Ids are case-insensitive. */

/* An amount of money. With regard to precision, see [Decimal Precision](datatypes.html#precision) */
type MoneyQuantity = Quantity;

/* Base StructureDefinition for unsignedInt type: An integer with a value that is not negative (e.g. >= 0) */
type unsignedInt = integer;

/* Additional instructions for the user to guide their input (i.e. a human readable version of a regular expression like "nnn-nnn-nnn"). In most UIs this is the placeholder (or 'ghost') text placed directly inside the edit controls and that disappear when the control gets the focus. */
type entryFormat = string;

/* Base StructureDefinition for MarketingStatus Type: The marketing status describes the date when a medicinal product is actually put on the market or the date as of which it is no longer available. */
interface MarketingStatus {
  /* The country in which the marketing authorisation has been granted shall be specified It should be specified using the ISO 3166 ‑ 1 alpha-2 code elements */
  country?: CodeableConcept /* Where a Medicines Regulatory Agency has granted a marketing authorisation for which specific provisions within a jurisdiction apply, the jurisdiction can be specified using an appropriate controlled terminology The controlled term and the controlled term identifier shall be specified */;
  jurisdiction?: CodeableConcept /* This attribute provides information on the status of the marketing of the medicinal product See ISO/TS 20443 for more information and examples */;
  status?: CodeableConcept /* The date when the Medicinal Product is placed on the market by the Marketing Authorisation Holder (or where applicable, the manufacturer/distributor) in a country and/or jurisdiction shall be provided A complete date consisting of day, month and year shall be specified using the ISO 8601 date format NOTE “Placed on the market” refers to the release of the Medicinal Product into the distribution chain */;
  dateRange?: Period /* The date when the Medicinal Product is placed on the market by the Marketing Authorisation Holder (or where applicable, the manufacturer/distributor) in a country and/or jurisdiction shall be provided A complete date consisting of day, month and year shall be specified using the ISO 8601 date format NOTE “Placed on the market” refers to the release of the Medicinal Product into the distribution chain */;
  restoreDate?: dateTime;
  _restoreDate?: Element;
}

/* Base StructureDefinition for Signature Type: A signature along with supporting context. The signature may be a digital signature that is cryptographic in nature, or some other signature acceptable to the domain. This other signature may be as simple as a graphical image representing a hand-written signature, or a signature ceremony Different signature approaches have different utilities. */
interface Signature {
  /* Who signed */
  who?: Reference<
    | "Patient"
    | "PractitionerRole"
    | "Organization"
    | "Device"
    | "Practitioner"
    | "RelatedPerson"
  >;
  _data?: Element /* When the signature was created */;
  when?: instant /* The party represented */;
  onBehalfOf?: Reference<
    | "Patient"
    | "PractitionerRole"
    | "Organization"
    | "Device"
    | "Practitioner"
    | "RelatedPerson"
  >;
  _sigFormat?: Element;
  _targetFormat?: Element /* The technical format of the signature */;
  sigFormat?: code;
  type?: Array<Coding> /* The technical format of the signed resources */;
  targetFormat?: code;
  _when?: Element /* The actual signature content (XML DigSig. JWS, picture, etc.) */;
  data?: base64Binary;
}

/* A human language representation of the concept (resource/element) as seen/selected/uttered by the user who entered the data and/or which represents the full intended meaning of the user. This can be provided either directly as text, or as a url that is a reference to a portion of the narrative of a resource ([DomainResource.text](narrative.html)). */
type originalText = string;
/* This is the base resource type for everything. */
interface Resource {
  /* Logical id of this artifact */ id?: string;
  _id?: Element /* Metadata about the resource */;
  meta?: Meta /* A set of rules under which this content was created */;
  implicitRules?: uri;
  _implicitRules?: Element /* Language of the resource content */;
  language?: code;
  _language?: Element;
}

/* Base StructureDefinition for SubstanceAmount Type: Chemical substances are a single substance type whose primary defining element is the molecular structure. Chemical substances shall be defined on the basis of their complete covalent molecular structure; the presence of a salt (counter-ion) and/or solvates (water, alcohols) is also captured. Purity, grade, physical form or particle size are not taken into account in the definition of a chemical substance or in the assignment of a Substance ID. */
interface SubstanceAmount {
  /* Used to capture quantitative values for a variety of elements. If only limits are given, the arithmetic mean would be the average. If only a single definite value for a given element is given, it would be captured in this field */
  amount?:
    | string
    | Range
    | Quantity /* Most elements that require a quantitative value will also have a field called amount type. Amount type should always be specified because the actual value of the amount is often dependent on it. EXAMPLE: In capturing the actual relative amounts of substances or molecular fragments it is essential to indicate whether the amount refers to a mole ratio or weight ratio. For any given element an effort should be made to use same the amount type for all related definitional elements */;
  amountType?: CodeableConcept /* A textual comment on a numeric value */;
  amountText?: string;
  _amountText?: Element /* Reference range of possible or expected values */;
  referenceRange?: {
    /* Lower limit possible or expected */
    lowLimit?: Quantity /* Upper limit possible or expected */;
    highLimit?: Quantity;
  };
}

/* Base StructureDefinition for Contributor Type: A contributor to the content of a knowledge asset, including authors, editors, reviewers, and endorsers. */
interface Contributor {
  /* author | editor | reviewer | endorser */ type?: code;
  _type?: Element /* Who contributed the content */;
  name?: string;
  _name?: Element;
  contact?: Array<ContactDetail>;
}

/* Base StructureDefinition for UsageContext Type: Specifies clinical/business/etc. metadata that can be used to retrieve, index and/or categorize an artifact. This metadata can either be specific to the applicable population (e.g., age category, DRG) or the specific context of care (e.g., venue, care setting, provider of care). */
interface UsageContext {
  /* Type of context being specified */
  code?: Coding /* Value that defines the context */;
  value?:
    | Range
    | CodeableConcept
    | Quantity
    | Reference<
        | "InsurancePlan"
        | "HealthcareService"
        | "Organization"
        | "ResearchStudy"
        | "Location"
        | "PlanDefinition"
        | "Group"
      >;
}

/* Base StructureDefinition for canonical type: A URI that is a reference to a canonical URL on a FHIR resource */
type canonical =
  uri; /* Base StructureDefinition for canonical type: A URI that is a reference to a canonical URL on a FHIR resource */

/* A numeric value that allows the comparison (less than, greater than) or other numerical 
manipulation of a concept (e.g. Adding up components of a score). Scores are usually a whole number, but occasionally decimals are encountered in scores. */
type ordinalValue = decimal;
/* Base StructureDefinition for Meta Type: The metadata about a resource. This is content in the resource that is maintained by the infrastructure. Changes to the content might not always be associated with version changes to the resource. */
interface Meta {
  /* Version specific identifier */ versionId?: id;
  _versionId?: Element;
  security?: Array<Coding> /* Identifies where the resource comes from */;
  source?: uri;
  _profile?: Array<Element> /* When the resource version last changed */;
  lastUpdated?: instant;
  _lastUpdated?: Element;
  tag?: Array<Coding>;
  _source?: Element;
  profile?: Array<canonical>;
}

/* Base StructureDefinition for code type: A string which has at least one character and no leading or trailing whitespace and where there is no whitespace other than single spaces in the contents */
type code =
  string; /* Base StructureDefinition for code type: A string which has at least one character and no leading or trailing whitespace and where there is no whitespace other than single spaces in the contents */

/* Base StructureDefinition for Distance Type: A length - a value with a unit that is a physical distance. */
type Distance = Quantity;

/* A set of codes that defines what the server is capable of. */
type capabilities = code;
/* Base StructureDefinition for Quantity Type: A measured amount (or an amount that can potentially be measured). Note that measured amounts include amounts that are not precisely quantified, including amounts involving arbitrary units and floating currencies. */
interface Quantity {
  _code?: Element /* System that defines coded unit form */;
  system?: uri;
  _comparator?: Element /* Unit representation */;
  unit?: string /* Numerical value (with implicit precision) */;
  value?: decimal;
  _value?: Element /* Coded form of the unit */;
  code?: code /* < | <= | >= | > - how to understand the value */;
  comparator?: code;
  _system?: Element;
  _unit?: Element;
}

/* Base StructureDefinition for oid type: An OID represented as a URI */
type oid =
  uri; /* Base StructureDefinition for oid type: An OID represented as a URI */

/* Base StructureDefinition for ContactPoint Type: Details for all kinds of technology mediated contact points for a person or organization, including telephone, email, etc. */
interface ContactPoint {
  /* phone | fax | email | pager | url | sms | other */
  system?: code /* home | work | temp | old | mobile - purpose of this contact point */;
  use?: code /* The actual contact point details */;
  value?: string /* Specify preferred order of use (1 = highest) */;
  rank?: positiveInt;
  _value?: Element /* Time period when the contact point was/is in use */;
  period?: Period;
  _system?: Element;
  _use?: Element;
  _rank?: Element;
}

/* Base StructureDefinition for Annotation Type: A  text note which also  contains information about who made the statement and when. */
interface Annotation {
  /* Individual responsible for the annotation */
  author?:
    | string
    | Reference<
        "Patient" | "Organization" | "Practitioner" | "RelatedPerson"
      > /* When the annotation was made */;
  time?: dateTime;
  _time?: Element /* The annotation  - text content (as markdown) */;
  text?: markdown;
  _text?: Element;
}

/* Base StructureDefinition for Attachment Type: For referring to data content defined in other formats. */
interface Attachment {
  /* Date attachment was first created */ creation?: dateTime;
  _data?: Element /* Hash of the data (sha-1, base64ed) */;
  hash?: base64Binary;
  _contentType?: Element;
  _language?: Element;
  _size?: Element /* Number of bytes of content (if url provided) */;
  size?: unsignedInt /* Label to display in place of the data */;
  title?: string;
  _hash?: Element /* Human language of the content (BCP-47) */;
  language?: code;
  _creation?: Element /* Uri where the data can be found */;
  url?: url;
  _title?: Element /* Mime type of the content, with charset etc. */;
  contentType?: code;
  _url?: Element /* Data inline, base64ed */;
  data?: base64Binary;
}

/* A human language representation of the concept (resource/element), as a url that is a reference to a portion of the narrative of a resource ([DomainResource.text](narrative.html)). */
type narrativeLink = url;

/* Base StructureDefinition for boolean Type: Value of "true" or "false" */

/* The title or other name to display when referencing a resource by canonical URL. */
type display = string;
/* Variable specifying a logic to generate a variable for use in subsequent logic.  The name of the variable will be added to FHIRPath's context when processing descendants of the element that contains this extension. */
type variable = Expression;

/* The minimum number of characters that must be present in the simple data type to be considered a "valid" instance. */
type minLength = integer;

/* Base StructureDefinition for Element Type: Base definition for all elements in a resource. */
interface Element {
  /* Unique id for inter-element referencing */ id?: string;
  extension?: Array<Extension>;
}

/* Base StructureDefinition for Narrative Type: A human-readable summary of the resource conveying the essential clinical and business information for the resource. */
interface Narrative {
  /* generated | extensions | additional | empty */ status?: code;
  _status?: Element /* Limited xhtml content */;
  div?: xhtml;
  _div?: Element;
}
/* Indicates a resource that this resource is replacing. */
type replaces = canonical;

/* A fixed quantity (no comparator) */
type SimpleQuantity = Quantity;

/* Base StructureDefinition for time Type: A time during the day, with no date specified */
type time =
  string; /* Base StructureDefinition for time Type: A time during the day, with no date specified */

/* Base StructureDefinition for TriggerDefinition Type: A description of a triggering event. Triggering events can be named events, data events, or periodic, as determined by the type element. */
interface TriggerDefinition {
  /* named-event | periodic | data-changed | data-added | data-modified | data-removed | data-accessed | data-access-ended */
  type?: code;
  _type?: Element /* Name or URI that identifies the event */;
  name?: string;
  _name?: Element /* Timing of the event */;
  timing?: date | dateTime | Timing | Reference<"Schedule">;
  data?: Array<DataRequirement> /* Whether the event triggers (boolean expression) */;
  condition?: Expression;
}

/* Base StructureDefinition for Range Type: A set of ordered Quantities defined by a low and high limit. */
interface Range {
  /* Low limit */ low?: Quantity /* High limit */;
  high?: Quantity;
}
/* Base StructureDefinition for BackboneElement Type: Base definition for all elements that are defined inside a resource - but not those in a data type. */
interface BackboneElement {
  modifierExtension?: Array<Extension>;
}

/* Base StructureDefinition for CodeableConcept Type: A concept that may be defined by a formal reference to a terminology or ontology or may be provided by text. */
interface CodeableConcept {
  coding?: Array<Coding> /* Plain text representation of the concept */;
  text?: string;
  _text?: Element;
}
/* Base StructureDefinition for DataRequirement Type: Describes a required data item for evaluation in terms of the type of data, and optional code or date-based filters of the data. */
interface DataRequirement {
  /* Number of results */ limit?: positiveInt;
  _limit?: Element;
  _mustSupport?: Array<Element>;
  _type?: Element /* The type of the required data */;
  type?: code;
  mustSupport?: Array<string>;
  _profile?: Array<Element>;
  codeFilter?: Array<{
    /* A code-valued attribute to filter on */ path?: string;
    _path?: Element /* A coded (token) parameter to search on */;
    searchParam?: string;
    _searchParam?: Element /* Valueset for the filter */;
    valueSet?: canonical;
    _valueSet?: Element;
    code?: Array<Coding>;
  }> /* E.g. Patient, Practitioner, RelatedPerson, Organization, Location, Device */;
  subject?: CodeableConcept | Reference<"Group">;
  dateFilter?: Array<{
    /* A date-valued attribute to filter on */ path?: string;
    _path?: Element /* A date valued parameter to search on */;
    searchParam?: string;
    _searchParam?: Element /* The value of the filter, as a Period, DateTime, or Duration value */;
    value?: dateTime | Period | Duration;
  }>;
  sort?: Array<{
    /* The name of the attribute to perform the sort */ path?: string;
    _path?: Element /* ascending | descending */;
    direction?: code;
    _direction?: Element;
  }>;
  profile?: Array<canonical>;
}

/* Base StructureDefinition for Money Type: An amount of economic utility in some recognized currency. */
interface Money {
  /* Numerical value (with implicit precision) */ value?: decimal;
  _value?: Element /* ISO 4217 Currency Code */;
  currency?: code;
  _currency?: Element;
}

/* Base StructureDefinition for Identifier Type: An identifier - identifies some entity uniquely and unambiguously. Typically this is used for business identifiers. */
interface Identifier {
  /* Organization that issued id (may be just text) */
  assigner?: Reference<"Organization"> /* The namespace for the identifier value */;
  system?: uri /* usual | official | temp | secondary | old (If known) */;
  use?: code /* The value that is unique */;
  value?: string /* Description of identifier */;
  type?: CodeableConcept;
  _value?: Element /* Time period when id is/was valid for use */;
  period?: Period;
  _system?: Element;
  _use?: Element;
}
/* Base StructureDefinition for dateTime Type: A date, date-time or partial date (e.g. just year or year + month).  If hours and minutes are specified, a time zone SHALL be populated. The format is a union of the schema types gYear, gYearMonth, date and dateTime. Seconds must be provided due to schema type constraints but may be zero-filled and may be ignored.                 Dates SHALL be valid dates. */
type dateTime =
  string; /* Base StructureDefinition for dateTime Type: A date, date-time or partial date (e.g. just year or year + month).  If hours and minutes are specified, a time zone SHALL be populated. The format is a union of the schema types gYear, gYearMonth, date and dateTime. Seconds must be provided due to schema type constraints but may be zero-filled and may be ignored.                 Dates SHALL be valid dates. */

/* Base StructureDefinition for uri Type: String of characters used to identify a name or a resource */
type uri =
  string; /* Base StructureDefinition for uri Type: String of characters used to identify a name or a resource */

/* Base StructureDefinition for decimal Type: A rational number with implicit precision */
type decimal = number;

/* Base StructureDefinition for Timing Type: Specifies an event that may occur multiple times. Timing schedules are used to record when things are planned, expected or requested to occur. The most common usage is in dosage instructions for medications. They are also used when planning care of various kinds, and may be used for reporting the schedule to which past regular activities were carried out. */
interface Timing {
  event?: Array<dateTime>;
  _event?: Array<Element> /* When the event is to occur */;
  repeat?: {
    _period?: Element;
    _durationMax?: Element;
    _countMax?: Element;
    _count?: Element /* Event occurs up to frequencyMax times per period */;
    frequencyMax?: positiveInt;
    _periodMax?: Element;
    when?: Array<code>;
    _offset?: Element /* Minutes from event (before or after) */;
    offset?: unsignedInt;
    _duration?: Element;
    _frequency?: Element /* s | min | h | d | wk | mo | a - unit of time (UCUM) */;
    periodUnit?: code;
    _timeOfDay?: Array<Element>;
    _frequencyMax?: Element /* Event occurs frequency times per period */;
    frequency?: positiveInt /* How long when it happens (Max) */;
    durationMax?: decimal /* How long when it happens */;
    duration?: decimal /* Length/Range of lengths, or (Start and/or end) limits */;
    bounds?:
      | Range
      | Period
      | Duration /* s | min | h | d | wk | mo | a - unit of time (UCUM) */;
    durationUnit?: code;
    dayOfWeek?: Array<code> /* Number of times to repeat */;
    count?: positiveInt;
    _periodUnit?: Element;
    _dayOfWeek?: Array<Element>;
    _when?: Array<Element> /* Upper limit of period (3-4 hours) */;
    periodMax?: decimal /* Event occurs frequency times per period */;
    period?: decimal /* Maximum number of times to repeat */;
    countMax?: positiveInt;
    _durationUnit?: Element;
    timeOfDay?: Array<time>;
  } /* BID | TID | QID | AM | PM | QD | QOD | + */;
  code?: CodeableConcept;
}

/* Base StructureDefinition for positiveInt type: An integer with a value that is positive (e.g. >0) */
type positiveInt = integer;

/* Identifies the maximum number of decimal places that may be specified for the data element. */
type maxDecimalPlaces = integer;

export interface SearchParams {
  DeviceRequest: {
    "based-on": string;
    "instantiates-uri": string;
    "prior-request": string;
    insurance: string;
    "event-date": string;
    patient: string;
    "authored-on": string;
  };
  ServiceRequest: {
    authored: string;
    requisition: string;
    occurrence: string;
    patient: string;
    "performer-type": string;
  };
  CarePlan: {
    "activity-code": string;
    "activity-reference": string;
    replaces: string;
    patient: string;
    "instantiates-canonical": string;
    "activity-date": string;
    goal: string;
  };
  Observation: {
    patient: string;
    category: string;
    "value-string": string;
    "component-value-concept": string;
    "combo-value-concept": string;
    "combo-code": string;
    "combo-data-absent-reason": string;
    "component-data-absent-reason": string;
    "value-quantity": string;
    method: string;
    "gene-identifier": string;
    "gene-amino-acid-change": string;
    specimen: string;
    "value-concept": string;
    "component-code": string;
    "component-value-quantity": string;
    "data-absent-reason": string;
    "amino-acid-change": string;
    "has-member": string;
    "value-date": string;
    "dna-variant": string;
    "gene-dnavariant": string;
    "combo-value-quantity": string;
  };
  Group: {
    member: string;
    "managing-entity": string;
    exclude: string;
    actual: string;
  };
  MessageDefinition: {
    "context-quantity": string;
    url: string;
    parent: string;
  };
  Appointment: {
    slot: string;
    "service-type": string;
  };
  QuestionnaireResponse: {
    "item-subject": string;
    questionnaire: string;
    source: string;
  };
  EffectEvidenceSynthesis: {
    jurisdiction: string;
    title: string;
    description: string;
  };
  EpisodeOfCare: {
    "incoming-referral": string;
    patient: string;
    "care-manager": string;
  };
  SupplyDelivery: {
    patient: string;
  };
  AdverseEvent: {
    resultingcondition: string;
    seriousness: string;
    substance: string;
    actuality: string;
  };
  Endpoint: {
    "connection-type": string;
    "payload-type": string;
  };
  CompartmentDefinition: {
    "context-quantity": string;
    url: string;
  };
  DetectedIssue: {
    implicated: string;
    identified: string;
    patient: string;
  };
  MedicationAdministration: {
    "reason-given": string;
    medication: string;
    "reason-not-given": string;
    "effective-time": string;
    patient: string;
  };
  EvidenceVariable: {
    effective: string;
  };
  ImplementationGuide: {
    "context-quantity": string;
    url: string;
    global: string;
  };
  Goal: {
    "target-date": string;
    "achievement-status": string;
    "lifecycle-status": string;
    patient: string;
    "start-date": string;
  };
  Communication: {
    sent: string;
    recipient: string;
    medium: string;
    subject: string;
    received: string;
    sender: string;
  };
  DocumentReference: {
    patient: string;
    facility: string;
    contenttype: string;
    related: string;
    setting: string;
    relatesto: string;
    custodian: string;
    language: string;
    relation: string;
    authenticator: string;
  };
  OrganizationAffiliation: {
    role: string;
    specialty: string;
    network: string;
    location: string;
    telecom: string;
    "participating-organization": string;
    "primary-organization": string;
  };
  Coverage: {
    "class-type": string;
    "policy-holder": string;
    "class-value": string;
    beneficiary: string;
    payor: string;
    subscriber: string;
    dependent: string;
  };
  AuditEvent: {
    "agent-name": string;
    "entity-role": string;
    subtype: string;
    "entity-type": string;
    altid: string;
    "entity-name": string;
    policy: string;
  };
  MessageHeader: {
    destination: string;
    "source-uri": string;
    focus: string;
    event: string;
    receiver: string;
    "response-id": string;
    "destination-uri": string;
  };
  Contract: {
    signer: string;
    authority: string;
    domain: string;
    instantiates: string;
  };
  TestReport: {
    tester: string;
    testscript: string;
    result: string;
  };
  CodeSystem: {
    supplements: string;
    "context-quantity": string;
    url: string;
    "content-mode": string;
  };
  PlanDefinition: {
    definition: string;
    "derived-from": string;
    "composed-of": string;
  };
  Invoice: {
    totalnet: string;
    "participant-role": string;
    issuer: string;
    account: string;
    totalgross: string;
    date: string;
  };
  ClaimResponse: {
    "payment-date": string;
    outcome: string;
  };
  ChargeItem: {
    "performing-organization": string;
    "requesting-organization": string;
    "performer-function": string;
    "performer-actor": string;
    "entered-date": string;
    "price-override": string;
    service: string;
    "factor-override": string;
  };
  CoverageEligibilityResponse: {
    requestor: string;
  };
  BodyStructure: {
    morphology: string;
  };
  ClinicalImpression: {
    patient: string;
    investigation: string;
    assessor: string;
    "finding-code": string;
    problem: string;
    "finding-ref": string;
    status: string;
    previous: string;
    "supporting-info": string;
  };
  FamilyMemberHistory: {
    relationship: string;
    patient: string;
    sex: string;
  };
  MedicinalProductAuthorization: {
    holder: string;
    country: string;
  };
  Composition: {
    entry: string;
    "related-id": string;
    confidentiality: string;
    attester: string;
    section: string;
    patient: string;
  };
  PractitionerRole: {
    email: string;
    phone: string;
    active: string;
  };
  HealthcareService: {
    "service-category": string;
    "coverage-area": string;
    characteristic: string;
    program: string;
  };
  Patient: {
    birthdate: string;
    address: string;
    given: string;
    email: string;
    "death-date": string;
    family: string;
    phonetic: string;
    mothersMaidenName: string;
    phone: string;
    name: string;
    deceased: string;
    "address-city": string;
    "general-practitioner": string;
    gender: string;
    "part-agree": string;
  };
  MedicationDispense: {
    prescription: string;
    whenprepared: string;
    medication: string;
    patient: string;
    whenhandedover: string;
    responsibleparty: string;
  };
  DeviceUseStatement: {
    device: string;
    patient: string;
  };
  StructureMap: {
    "context-quantity": string;
    url: string;
  };
  ImmunizationEvaluation: {
    "dose-status": string;
    "immunization-event": string;
  };
  Library: {
    "content-type": string;
    successor: string;
  };
  Basic: {
    created: string;
  };
  Slot: {
    schedule: string;
    "appointment-type": string;
    start: string;
  };
  ActivityDefinition: {
    predecessor: string;
  };
  Bundle: {
    message: string;
    timestamp: string;
    composition: string;
  };
  MolecularSequence: {
    chromosome: string;
    "window-end": string;
    "window-start": string;
    referenceseqid: string;
    "variant-start": string;
    identifier: string;
    "variant-end": string;
  };
  Specimen: {
    accession: string;
    collected: string;
    bodysite: string;
    "container-id": string;
    container: string;
    collector: string;
  };
  DiagnosticReport: {
    media: string;
    issued: string;
    conclusion: string;
    "assessed-condition": string;
    patient: string;
    "results-interpreter": string;
    performer: string;
  };
  Subscription: {
    criteria: string;
    payload: string;
    contact: string;
  };
  RequestGroup: {
    encounter: string;
    intent: string;
  };
  Provenance: {
    agent: string;
    recorded: string;
    "agent-type": string;
    when: string;
    "signature-type": string;
    "agent-role": string;
    entity: string;
  };
  MedicinalProduct: {
    "name-language": string;
  };
  Practitioner: {
    address: string;
    given: string;
    email: string;
    family: string;
    phonetic: string;
    phone: string;
    "address-city": string;
    communication: string;
    gender: string;
  };
  Flag: {
    patient: string;
  };
  ExplanationOfBenefit: {
    coverage: string;
    "detail-udi": string;
    "subdetail-udi": string;
    provider: string;
    claim: string;
    "care-team": string;
  };
  Linkage: {
    author: string;
    item: string;
  };
  MedicinalProductPharmaceutical: {
    "target-species": string;
  };
  Immunization: {
    reaction: string;
    "target-disease": string;
    "reaction-date": string;
    "status-reason": string;
    series: string;
    "vaccine-code": string;
    patient: string;
    "reason-code": string;
  };
  MedicationKnowledge: {
    "monitoring-program-name": string;
    "monitoring-program-type": string;
    "monograph-type": string;
    doseform: string;
    "classification-type": string;
    "source-cost": string;
    monograph: string;
    classification: string;
  };
  ResearchSubject: {
    individual: string;
    study: string;
  };
  PaymentNotice: {
    "payment-status": string;
    request: string;
    response: string;
  };
  NamingSystem: {
    "context-quantity": string;
    "id-type": string;
    responsible: string;
    value: string;
    kind: string;
    period: string;
  };
  MedicationStatement: {
    "part-of": string;
    medication: string;
    patient: string;
  };
  NutritionOrder: {
    supplement: string;
    oraldiet: string;
    datetime: string;
    additive: string;
    patient: string;
    formula: string;
  };
  Questionnaire: {
    "subject-type": string;
    code: string;
  };
  Account: {
    owner: string;
    type: string;
  };
  EventDefinition: {
    "depends-on": string;
    topic: string;
  };
  VerificationResult: {
    target: string;
  };
  DocumentManifest: {
    "related-ref": string;
    patient: string;
  };
  Task: {
    requester: string;
    "group-identifier": string;
    modified: string;
    "business-status": string;
  };
  RiskEvidenceSynthesis: {
    context: string;
  };
  ValueSet: {
    "context-quantity": string;
    url: string;
    expansion: string;
    reference: string;
  };
  Claim: {
    payee: string;
    insurer: string;
    "procedure-udi": string;
    enterer: string;
    use: string;
    "item-udi": string;
  };
  InsurancePlan: {
    "owned-by": string;
    "administered-by": string;
  };
  ExampleScenario: {
    version: string;
  };
  ResearchStudy: {
    protocol: string;
    partof: string;
    principalinvestigator: string;
    keyword: string;
    sponsor: string;
  };
  MedicationRequest: {
    "intended-performertype": string;
    priority: string;
    "intended-performer": string;
    authoredon: string;
    medication: string;
    patient: string;
    "intended-dispenser": string;
  };
  Measure: {
    "context-type": string;
  };
  List: {
    patient: string;
    notes: string;
    "empty-reason": string;
  };
  Resource: {
    _source: string;
    _lastUpdated: string;
    _profile: string;
    _tag: string;
    _id: string;
    _security: string;
  };
  Encounter: {
    patient: string;
    appointment: string;
    diagnosis: string;
    "participant-type": string;
    participant: string;
    "special-arrangement": string;
    "service-provider": string;
    class: string;
    length: string;
    "location-period": string;
    "episode-of-care": string;
  };
  CapabilityStatement: {
    format: string;
    "supported-profile": string;
    mode: string;
    guide: string;
    fhirversion: string;
    resource: string;
    url: string;
    "context-quantity": string;
    software: string;
    "resource-profile": string;
    "security-service": string;
  };
  VisionPrescription: {
    patient: string;
    datewritten: string;
    prescriber: string;
  };
  RiskAssessment: {
    probability: string;
    condition: string;
    patient: string;
    risk: string;
  };
  ImmunizationRecommendation: {
    "vaccine-type": string;
    support: string;
    information: string;
  };
  RelatedPerson: {
    gender: string;
    email: string;
    "address-city": string;
    address: string;
    phone: string;
    phonetic: string;
    birthdate: string;
  };
  Medication: {
    form: string;
    "ingredient-code": string;
    "expiration-date": string;
    ingredient: string;
    "lot-number": string;
  };
  AppointmentResponse: {
    practitioner: string;
    "part-status": string;
    actor: string;
  };
  ResearchElementDefinition: {
    name: string;
    publisher: string;
  };
  Substance: {
    "container-identifier": string;
    "substance-reference": string;
    quantity: string;
    expiry: string;
  };
  PaymentReconciliation: {
    disposition: string;
    "payment-issuer": string;
  };
  TestScript: {
    "testscript-capability": string;
  };
  ConceptMap: {
    dependson: string;
    "target-code": string;
    other: string;
    "target-uri": string;
    "source-code": string;
    product: string;
    "target-system": string;
    url: string;
    "context-quantity": string;
    "source-system": string;
  };
  Person: {
    birthdate: string;
    address: string;
    email: string;
    phonetic: string;
    phone: string;
    "address-city": string;
    link: string;
    relatedperson: string;
    gender: string;
  };
  Condition: {
    "verification-status": string;
    patient: string;
    stage: string;
    "abatement-string": string;
    "body-site": string;
    evidence: string;
    "abatement-age": string;
    "abatement-date": string;
    "recorded-date": string;
    "evidence-detail": string;
    "onset-age": string;
    "onset-date": string;
    subject: string;
    "onset-info": string;
  };
  CareTeam: {
    patient: string;
  };
  StructureDefinition: {
    path: string;
    derivation: string;
    abstract: string;
    valueset: string;
    experimental: string;
    "base-path": string;
    url: string;
    "context-quantity": string;
    "ext-context": string;
  };
  Procedure: {
    "reason-reference": string;
    patient: string;
  };
  Consent: {
    "security-label": string;
    consentor: string;
    scope: string;
    action: string;
    patient: string;
    "source-reference": string;
    data: string;
    purpose: string;
  };
  Location: {
    "address-use": string;
    "operational-status": string;
    "address-state": string;
    endpoint: string;
    organization: string;
    "address-postalcode": string;
    near: string;
  };
  Organization: {
    "address-country": string;
  };
  Device: {
    model: string;
    "device-name": string;
    manufacturer: string;
    din: string;
    "udi-carrier": string;
    "udi-di": string;
  };
  SupplyRequest: {
    supplier: string;
  };
  AllergyIntolerance: {
    patient: string;
    onset: string;
    criticality: string;
    "clinical-status": string;
    route: string;
    manifestation: string;
    asserter: string;
    recorder: string;
    severity: string;
    "last-date": string;
  };
  SearchParameter: {
    "context-quantity": string;
    url: string;
    component: string;
    base: string;
  };
  OperationDefinition: {
    "context-quantity": string;
    url: string;
    "input-profile": string;
    instance: string;
    "output-profile": string;
    system: string;
  };
  ImagingStudy: {
    started: string;
    referrer: string;
    "dicom-class": string;
    interpreter: string;
    reason: string;
    patient: string;
    basedon: string;
  };
  Media: {
    operator: string;
    modality: string;
    site: string;
    view: string;
  };
  MeasureReport: {
    "evaluated-resource": string;
    reporter: string;
    measure: string;
  };
  GraphDefinition: {
    "context-quantity": string;
    url: string;
  };
  TerminologyCapabilities: {
    "context-quantity": string;
    url: string;
  };
}
