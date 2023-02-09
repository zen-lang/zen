export interface Patient {
  readonly resourceType: "Patient";
  /* Logical id of this artifact */
  readonly id: id;
  /* Whether this patient's record is in active use */
  readonly active?: boolean;
  /* An address for the individual */
  readonly address?: Array<Address>;
  /* The date of birth for the individual */
  readonly birthDate?: date;
  /* A language which may be used to communicate with the patient about his or her health */
  readonly communication?: Array<{
    /* Additional content defined by implementations */
    readonly extension?: Array<Extension>;
    /* Unique id for inter-element referencing */
    readonly id?: string;
    /* The language which can be used to communicate with the patient about his or her health */
    readonly language: CodeableConcept;
    /* Extensions that cannot be ignored even if unrecognized */
    readonly modifierExtension?: Array<Extension>;
    /* Language preference indicator */
    readonly preferred?: boolean;
  }>;
  /* A contact party (e.g. guardian, partner, friend) for the patient */
  readonly contact?: Array<{
    /* Address for the contact person */
    readonly address?: Address;
    /* Additional content defined by implementations */
    readonly extension?: Array<Extension>;
    /* male | female | other | unknown */
    readonly gender?: "male" | "female" | "other" | "unknown" | string;
    /* Unique id for inter-element referencing */
    readonly id?: string;
    /* Extensions that cannot be ignored even if unrecognized */
    readonly modifierExtension?: Array<Extension>;
    /* A name associated with the contact person */
    readonly name?: HumanName;
    /* Organization that is associated with the contact */
    readonly organization?: Reference<"Organization">;
    /* The period during which this contact person or organization is valid to be contacted relating to this patient */
    readonly period?: Period;
    /* The kind of relationship */
    readonly relationship?: Array<CodeableConcept>;
    /* A contact detail for the person */
    readonly telecom?: Array<ContactPoint>;
  }>;
  /* Indicates if the individual is deceased or not */
  readonly deceased?: boolean | dateTime;
  /* Additional content defined by implementations */
  readonly extension?: Array<Extension>;
  /* male | female | other | unknown */
  readonly gender?: "male" | "female" | "other" | "unknown" | string;
  /* Patient's nominated primary care provider */
  readonly generalPractitioner?: Array<
    Reference<"Organization" | "Practitioner" | "PractitionerRole">
  >;
  /* An identifier for this patient */
  readonly identifier?: Array<Identifier>;
  /* A set of rules under which this content was created */
  readonly implicitRules?: uri;
  /* Language of the resource content */
  readonly language?: code;
  /* Link to another patient resource that concerns the same actual person */
  readonly link?: Array<{
    /* Additional content defined by implementations */
    readonly extension?: Array<Extension>;
    /* Unique id for inter-element referencing */
    readonly id?: string;
    /* Extensions that cannot be ignored even if unrecognized */
    readonly modifierExtension?: Array<Extension>;
    /* The other patient or related person resource that the link refers to */
    readonly other: Reference<"Patient" | "RelatedPerson">;
    /* replaced-by | replaces | refer | seealso */
    readonly type: "replaced-by" | "replaces" | "refer" | "seealso" | string;
  }>;
  /* Organization that is the custodian of the patient record */
  readonly managingOrganization?: Reference<"Organization">;
  /* Marital (civil) status of a patient */
  readonly maritalStatus?: CodeableConcept;
  /* Metadata about the resource */
  readonly meta?: Meta;
  /* Extensions that cannot be ignored */
  readonly modifierExtension?: Array<Extension>;
  /* Whether patient is part of a multiple birth */
  readonly multipleBirth?: boolean | integer;
  /* A name associated with the patient */
  readonly name?: Array<HumanName>;
  /* Image of the patient */
  readonly photo?: Array<Attachment>;
  /* A contact detail for the individual */
  readonly telecom?: Array<ContactPoint>;
  /* Text summary of the resource, for human interpretation */
  readonly text?: Narrative;
  readonly createdBy?: Reference<"Broadcast">;
  readonly preferredModality?: {
    readonly email?: boolean;
    readonly voice?: boolean;
    readonly sms?: boolean;
  };
}
