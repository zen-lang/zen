import { AxiosBasicCredentials, AxiosInstance } from 'axios';
import { ResourceTypeMap, SearchParams } from "./aidbox-types";
export type UnnecessaryKeys = 'contained' | 'extension' | 'modifierExtension' | "_id" | "meta" | "implicitRules" | "_implicitRules" | "language" | "_language";
export type PrefixWithArray = 'eq' | 'ne';
export type Prefix = 'eq' | 'ne' | 'gt' | 'lt' | 'ge' | 'le' | 'sa' | "eb" | 'ap';
export type BaseResponseResources<T extends keyof ResourceTypeMap> = {
    entry: {
        resource: ResourceTypeMap[T];
    }[];
};
export type BaseResponseResource<T extends keyof ResourceTypeMap> = ResourceTypeMap[T];
export type ResourceKeys<T extends keyof ResourceTypeMap, I extends ResourceTypeMap[T]> = Omit<I, UnnecessaryKeys>;
type SortParams<T extends keyof ResourceTypeMap> = {
    key: keyof SearchParams[T] | `.${string}`;
    dir: 'acs' | 'desc';
}[];
type ElementsParams<T extends keyof ResourceTypeMap, R extends ResourceTypeMap[T]> = Array<keyof ResourceKeys<T, R>>;
export declare class Client {
    client: AxiosInstance;
    constructor(baseURL: string, credentials: AxiosBasicCredentials);
    getResources<T extends keyof ResourceTypeMap>(resourceName: T): GetResources<T, ResourceTypeMap[T]>;
    getResource<T extends keyof ResourceTypeMap>(resourceName: T, id: string): Promise<BaseResponseResource<T> | Error>;
    findResources<T extends keyof ResourceTypeMap>(resourceName: T, params: Record<string, unknown>): Promise<BaseResponseResources<T> | Error>;
    deleteResource<T extends keyof ResourceTypeMap>(resourceName: T, id: string): Promise<BaseResponseResource<T> | Error>;
    patchResource<T extends keyof ResourceTypeMap>(resourceName: T, id: string, body: Partial<ResourceTypeMap[T]>): Promise<BaseResponseResource<T> | Error>;
    createResource<T extends keyof ResourceTypeMap>(resourceName: T, body: ResourceTypeMap[T]): Promise<BaseResponseResource<T> | Error>;
}
export declare class GetResources<T extends keyof ResourceTypeMap, R extends ResourceTypeMap[T]> implements PromiseLike<BaseResponseResources<T>> {
    private searchParamsObject;
    resourceName: T;
    client: AxiosInstance;
    constructor(client: AxiosInstance, resourceName: T);
    where<K extends keyof SearchParams[T], SP extends SearchParams[T][K], PR extends PrefixWithArray>(key: K, value: SP | SP[], prefix?: PR): this;
    where<K extends keyof SearchParams[T], SP extends SearchParams[T][K], PR extends Exclude<Prefix, PrefixWithArray>>(key: K, value: SP, prefix?: PR): this;
    contained(contained: boolean | "both", containedType?: "container" | "contained"): this;
    count(value: number): this;
    elements(args: ElementsParams<T, R>): this;
    summary(type: boolean | "text" | 'data' | 'count'): this;
    sort(args: SortParams<T>): this;
    then<TResult1 = BaseResponseResources<T>, TResult2 = never>(onfulfilled?: ((value: BaseResponseResources<T>) => (PromiseLike<TResult1> | TResult1)) | undefined | null, onrejected?: ((reason: any) => (PromiseLike<TResult2> | TResult2)) | undefined | null): PromiseLike<TResult1 | TResult2>;
}
export {};
