import { AxiosBasicCredentials, AxiosInstance, AxiosResponse } from 'axios';
import { DomainResource, ResourceType, ResourceTypeMap, SearchParams } from './aidbox-types';
type PathResourceBody<T extends keyof ResourceTypeMap> = Partial<Omit<ResourceTypeMap[T], 'id' | 'meta'>>;
export type UnnecessaryKeys = 'contained' | 'extension' | 'modifierExtension' | '_id' | 'meta' | 'implicitRules' | '_implicitRules' | 'language' | '_language';
type Dir = 'asc' | 'desc';
export type PrefixWithArray = 'eq' | 'ne';
export type Prefix = 'eq' | 'ne' | 'gt' | 'lt' | 'ge' | 'le' | 'sa' | 'eb' | 'ap';
export type ExecuteQueryResponseWrapper<T> = {
    data: ExecuteQueryResponseItem<T>[];
    query: string[];
    total: number;
};
export type ExecuteQueryResponseItem<T> = {
    id: string;
    txid: number;
    cts: string;
    ts: string;
    resource_type: string;
    status: string;
    resource: T;
};
export type CreateQueryParams = {
    isRequired: boolean;
    type: string;
    format?: string;
    default?: unknown;
};
export type CreateQueryBody = {
    params?: Record<string, CreateQueryParams>;
    query: string;
    'count-query': string;
};
type Link = {
    relation: string;
    url: string;
};
export type BaseResponseResources<T extends keyof ResourceTypeMap> = {
    'query-time': number;
    meta: {
        versionId: string;
    };
    type: string;
    resourceType: string;
    total: number;
    link: Link[];
    'query-timeout': number;
    entry: {
        resource: ResourceTypeMap[T];
    }[];
    'query-sql': (string | number)[];
};
export type BaseResponseResource<T extends keyof ResourceTypeMap> = ResourceTypeMap[T];
export type ResourceKeys<T extends keyof ResourceTypeMap, I extends ResourceTypeMap[T]> = Omit<I, UnnecessaryKeys>;
type SortKey<T extends keyof ResourceTypeMap> = keyof SearchParams[T] | `.${string}`;
type ElementsParams<T extends keyof ResourceTypeMap, R extends ResourceTypeMap[T]> = Array<keyof ResourceKeys<T, R>>;
type SubscriptionParams = {
    id: string;
    status: 'active' | 'off';
    trigger: Partial<Record<ResourceType, {
        event: Array<'all' | 'create' | 'update' | 'delete'>;
        filter?: unknown;
    }>>;
    channel: {
        endpoint: string;
        payload?: {
            content: string;
            contentType: string;
            context: unknown;
        };
        headers?: Record<string, string>;
        timeout?: number;
    };
};
type Subscription = DomainResource & SubscriptionParams & {
    resourceType: 'SubsSubscription';
    channel: {
        type: 'rest-hook';
    };
};
export declare class Client {
    client: AxiosInstance;
    constructor(baseURL: string, credentials: AxiosBasicCredentials);
    getResources<T extends keyof ResourceTypeMap>(resourceName: T): GetResources<T, ResourceTypeMap[T]>;
    getResource<T extends keyof ResourceTypeMap>(resourceName: T, id: string): Promise<BaseResponseResource<T> | Error>;
    findResources<T extends keyof ResourceTypeMap>(resourceName: T, params: Record<string, unknown>): Promise<BaseResponseResources<T> | Error>;
    deleteResource<T extends keyof ResourceTypeMap>(resourceName: T, id: string): Promise<BaseResponseResource<T> | Error>;
    createQuery(name: string, body: CreateQueryBody): Promise<any>;
    executeQuery<T>(name: string, params?: Record<string, unknown>): Promise<AxiosResponse<ExecuteQueryResponseWrapper<T>>>;
    patchResource<T extends keyof ResourceTypeMap>(resourceName: T, id: string, body: PathResourceBody<T>): Promise<BaseResponseResource<T> | Error>;
    createResource<T extends keyof ResourceTypeMap>(resourceName: T, body: ResourceTypeMap[T]): Promise<BaseResponseResource<T> | Error>;
    rawSQL(sql: string, params?: unknown[]): Promise<any>;
    createSubscription({ id, status, trigger, channel }: SubscriptionParams): Promise<Subscription | Error>;
}
export declare class GetResources<T extends keyof ResourceTypeMap, R extends ResourceTypeMap[T]> implements PromiseLike<BaseResponseResources<T>> {
    private searchParamsObject;
    resourceName: T;
    client: AxiosInstance;
    constructor(client: AxiosInstance, resourceName: T);
    where<K extends keyof SearchParams[T], SP extends SearchParams[T][K], PR extends PrefixWithArray>(key: K | string, value: SP | SP[], prefix?: PR): this;
    where<K extends keyof SearchParams[T], SP extends SearchParams[T][K], PR extends Exclude<Prefix, PrefixWithArray>>(key: K | string, value: SP, prefix?: PR): this;
    contained(contained: boolean | 'both', containedType?: 'container' | 'contained'): this;
    count(value: number): this;
    elements(args: ElementsParams<T, R>): this;
    summary(type: boolean | 'text' | 'data' | 'count'): this;
    sort(key: SortKey<T>, dir: Dir): this;
    then<TResult1 = BaseResponseResources<T>, TResult2 = never>(onfulfilled?: ((value: BaseResponseResources<T>) => PromiseLike<TResult1> | TResult1) | undefined | null, onrejected?: ((reason: any) => PromiseLike<TResult2> | TResult2) | undefined | null): PromiseLike<TResult1 | TResult2>;
}
export {};
