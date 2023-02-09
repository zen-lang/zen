type User = {
    _active: Element;
    address: Array<Address>;
    managingOrganization: Reference<Organization>
    name: Array<HumanName>;
    _gender: Element;
    birthDate: date;
    _birthDate: Element;
    multipleBirth: {
        boolean: boolean;
        _boolean: Element;
        integer: integer;
        _integer: Element;
    }
    deceased: { boolean: boolean; _boolean: Element; dateTime: dateTime; _dateTime: Element; }
    photo: Array<Attachment>;
    link: Array<{
        other: Reference<Patient | RelatedPerson>
        type: code;
        _type: Element;
    }>;
    active: boolean;
    communication: Array<{
        language: CodeableConcept;
        preferred: boolean;
        _preferred: Element;
    }>;
    identifier: Array<Identifier>;
    telecom: Array<ContactPoint>;
    generalPractitioner: Array<Reference<PractitionerRole | Organization | Practitioner>>;
    gender: code;
    maritalStatus: CodeableConcept;
    contact: Array<{
        relationship: Array<CodeableConcept>;
        name: HumanName;
        telecom: Array<ContactPoint>;
        address: Address;
        gender: code;
        _gender: Element;
        organization: Reference<Organization>period: Period;
    }>;
}
