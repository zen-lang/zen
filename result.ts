export type base64Binary = string;
export type canonical = string;
export type code = string;
export type date = string;
export type dateTime = string;
export type decimal = number;
export type email = string;
export type id = string;
export type instant = string;
export type integer = number;
export type keyword = string;
export type markdown = string;
export type oid = string;
export type password = string;
export type positiveInt = number;
export type time = string;
export type unsignedInt = number;
export type uri = string;
export type url = string;
export type uuid = string;
export type xhtml = string;

export interface ResourceTypeMap {
  Patient: Patient;
  Organization: Organization;
  HumanName: HumanName;
  RelatedPerson: RelatedPerson;
  Endpoint: Endpoint;
  PractitionerRole: PractitionerRole;
  Practitioner: Practitioner;
  Location: Location;
  HealthcareService: HealthcareService;
}

export type ResourceType = keyof ResourceTypeMap;

export type Reference<T extends ResourceType> = {
  id: string;
  resourceType: T;
  display?: string;
};
