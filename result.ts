export type Reference<T extends ResourceType> = {
  id: string;
  resourceType: T;
  display?: string;
};
