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
interface ProductShelfLife { identifier?:Identifier;type:CodeableConcept;period:Quantity;specialPrecautionsForStorage?:Array<CodeableConcept>; };
interface Dosage { _sequence?:Element;site?:CodeableConcept;_patientInstruction?:Element;asNeeded?:CodeableConcept | boolean;method?:CodeableConcept;patientInstruction?:string;maxDosePerLifetime?:Quantity;maxDosePerAdministration?:Quantity;route?:CodeableConcept;timing?:Timing;additionalInstruction?:Array<CodeableConcept>;sequence?:integer;maxDosePerPeriod?:Ratio;_text?:Element;doseAndRate?:Array<Element{ type?:CodeableConcept;dose?:Range | Quantity;rate?:Range | Ratio | Quantity; }>;text?:string; };
interface MetadataResource { description?:markdown;date?:dateTime;publisher?:string;jurisdiction?:Array<CodeableConcept>;_publisher?:Element;_date?:Element;name?:string;_status?:Element;_experimental?:Element;useContext?:Array<UsageContext>;experimental?:boolean;title?:string;_description?:Element;status:code;_name?:Element;url?:uri;_title?:Element;version?:string;_version?:Element;contact?:Array<ContactDetail>;_url?:Element; };
interface Population { age?:Range | CodeableConcept;gender?:CodeableConcept;race?:CodeableConcept;physiologicalCondition?:CodeableConcept; };
interface SampledData { _period?:Element;_data?:Element;upperLimit?:decimal;_lowerLimit?:Element;lowerLimit?:decimal;_factor?:Element;_upperLimit?:Element;dimensions:positiveInt;factor?:decimal;origin:Quantity;period:decimal;_dimensions?:Element;data?:string; };
interface ProdCharacteristic { _color?:Array<Element>;imprint?:Array<string>;color?:Array<string>;_imprint?:Array<Element>;_shape?:Element;width?:Quantity;nominalVolume?:Quantity;weight?:Quantity;shape?:string;scoring?:CodeableConcept;image?:Array<Attachment>;depth?:Quantity;externalDiameter?:Quantity;height?:Quantity; };
interface Extension { url:uri;value?:unsignedInt | Signature | markdown | date | Dosage | ContactDetail | RelatedArtifact | instant | UsageContext | time | DataRequirement | base64Binary | Meta | Distance | SampledData | TriggerDefinition | Identifier | string | Address | Expression | dateTime | Range | integer | Ratio | oid | ContactPoint | Money | decimal | id | Attachment | Contributor | Period | canonical | url | code | HumanName | positiveInt | ParameterDefinition | Coding | Timing | Duration | uri | CodeableConcept | uuid | Count | Quantity | boolean | Annotation | Age | Reference; };
interface Ratio { numerator?:Quantity;denominator?:Quantity; };
interface ParameterDefinition { min?:integer;_documentation?:Element;use:code;name?:code;_type?:Element;type:code;documentation?:string;_profile?:Element;_min?:Element;max?:string;_name?:Element;_max?:Element;_use?:Element;profile?:canonical; };
interface ContactDetail { name?:string;_name?:Element;telecom?:Array<ContactPoint>; };
interface Address { _line?:Array<Element>;use?:code;city?:string;_type?:Element;type?:code;_city?:Element;state?:string;_district?:Element;_state?:Element;line?:Array<string>;postalCode?:string;_country?:Element;_postalCode?:Element;_text?:Element;period?:Period;country?:string;_use?:Element;district?:string;text?:string; };
interface Coding { userSelected?:boolean;_code?:Element;system?:uri;_userSelected?:Element;code?:code;display?:string;_system?:Element;version?:string;_version?:Element;_display?:Element; };
interface Period { start?:dateTime;_start?:Element;end?:dateTime;_end?:Element; };
interface HumanName { _family?:Element;suffix?:Array<string>;_suffix?:Array<Element>;given?:Array<string>;family?:string;_prefix?:Array<Element>;use?:code;prefix?:Array<string>;_given?:Array<Element>;_text?:Element;period?:Period;_use?:Element;text?:string; };
interface RelatedArtifact { _type?:Element;type:code;document?:Attachment;citation?:markdown;_label?:Element;_resource?:Element;label?:string;resource?:canonical;url?:url;display?:string;_citation?:Element;_url?:Element;_display?:Element; };
interface Expression { description?:string;_reference?:Element;expression?:string;name?:id;_language?:Element;_expression?:Element;_description?:Element;reference?:uri;language:code;_name?:Element; };
interface MarketingStatus { country:CodeableConcept;jurisdiction?:CodeableConcept;status:CodeableConcept;dateRange:Period;restoreDate?:dateTime;_restoreDate?:Element; };
interface Signature { who:Reference<'Patient' | 'PractitionerRole' | 'Organization' | 'Device' | 'Practitioner' | 'RelatedPerson'>;_data?:Element;when:instant;onBehalfOf?:Reference<'Patient' | 'PractitionerRole' | 'Organization' | 'Device' | 'Practitioner' | 'RelatedPerson'>;_sigFormat?:Element;_targetFormat?:Element;sigFormat?:code;type:Array<Coding>;targetFormat?:code;_when?:Element;data?:base64Binary; };
interface Resource { id?:string;_id?:Element;meta?:Meta;implicitRules?:uri;_implicitRules?:Element;language?:code;_language?:Element; };
interface SubstanceAmount { amount?:string | Range | Quantity;amountType?:CodeableConcept;amountText?:string;_amountText?:Element;referenceRange?:Element{ lowLimit?:Quantity;highLimit?:Quantity; }; };
interface Contributor { type:code;_type?:Element;name:string;_name?:Element;contact?:Array<ContactDetail>; };
interface UsageContext { code:Coding;value:Range | CodeableConcept | Quantity | Reference; };
interface Meta { versionId?:id;_versionId?:Element;security?:Array<Coding>;source?:uri;_profile?:Array<Element>;lastUpdated?:instant;_lastUpdated?:Element;tag?:Array<Coding>;_source?:Element;profile?:Array<canonical>; };
interface elementdefinition-de { type?:Array<{ profile?:;aggregation?:; }>;AllowedUnits?:elementdefinition-allowedUnits;representation?:;Question?:Array<elementdefinition-question>;defaultValue?:;fixed?:;pattern?:; };
interface Quantity { _code?:Element;system?:uri;_comparator?:Element;unit?:string;value?:decimal;_value?:Element;code?:code;comparator?:code;_system?:Element;_unit?:Element; };
interface example-composition { section?:; };
interface ContactPoint { system?:code;use?:code;value?:string;rank?:positiveInt;_value?:Element;period?:Period;_system?:Element;_use?:Element;_rank?:Element; };
interface Annotation { author?:string | Reference;time?:dateTime;_time?:Element;text:markdown;_text?:Element; };
interface Attachment { creation?:dateTime;_data?:Element;hash?:base64Binary;_contentType?:Element;_language?:Element;_size?:Element;size?:unsignedInt;title?:string;_hash?:Element;language?:code;_creation?:Element;url?:url;_title?:Element;contentType?:code;_url?:Element;data?:base64Binary; };
interface Element { id?:string;extension?:Array<Extension>; };
interface Narrative { status:code;_status?:Element;div:xhtml;_div?:Element; };
interface TriggerDefinition { type:code;_type?:Element;name?:string;_name?:Element;timing?:date | dateTime | Timing | Reference;data?:Array<DataRequirement>;condition?:Expression; };
interface Range { low?:Quantity;high?:Quantity; };
interface BackboneElement { modifierExtension?:Array<Extension>; };
interface CodeableConcept { coding?:Array<Coding>;text?:string;_text?:Element; };
interface DataRequirement { limit?:positiveInt;_limit?:Element;_mustSupport?:Array<Element>;_type?:Element;type:code;mustSupport?:Array<string>;_profile?:Array<Element>;codeFilter?:Array<Element{ path?:string;_path?:Element;searchParam?:string;_searchParam?:Element;valueSet?:canonical;_valueSet?:Element;code?:Array<Coding>; }>;subject?:CodeableConcept | Reference;dateFilter?:Array<Element{ path?:string;_path?:Element;searchParam?:string;_searchParam?:Element;value?:dateTime | Period | Duration; }>;sort?:Array<Element{ path:string;_path?:Element;direction:code;_direction?:Element; }>;profile?:Array<canonical>; };
interface Money { value?:decimal;_value?:Element;currency?:code;_currency?:Element; };
interface Identifier { assigner?:Reference<'Organization'>;system?:uri;use?:code;value?:string;type?:CodeableConcept;_value?:Element;period?:Period;_system?:Element;_use?:Element; };
interface Timing { event?:Array<dateTime>;_event?:Array<Element>;repeat?:Element{ _period?:Element;_durationMax?:Element;_countMax?:Element;_count?:Element;frequencyMax?:positiveInt;_periodMax?:Element;when?:Array<code>;_offset?:Element;offset?:unsignedInt;_duration?:Element;_frequency?:Element;periodUnit?:code;_timeOfDay?:Array<Element>;_frequencyMax?:Element;frequency?:positiveInt;durationMax?:decimal;duration?:decimal;bounds?:Range | Period | Duration;durationUnit?:code;dayOfWeek?:Array<code>;count?:positiveInt;_periodUnit?:Element;_dayOfWeek?:Array<Element>;_when?:Array<Element>;periodMax?:decimal;period?:decimal;countMax?:positiveInt;_durationUnit?:Element;timeOfDay?:Array<time>; };code?:CodeableConcept; };


