import axios, { AxiosBasicCredentials, AxiosInstance } from 'axios';
import { ResourceTypeMap, SearchParams } from './aidbox-types';

type PathResourceBody<T extends keyof ResourceTypeMap> = Partial<Omit<ResourceTypeMap[T], 'id' | 'meta'>>;

export type UnnecessaryKeys =
  | 'contained'
  | 'extension'
  | 'modifierExtension'
  | '_id'
  | 'meta'
  | 'implicitRules'
  | '_implicitRules'
  | 'language'
  | '_language';

type Dir = "asc" | "desc"

export type PrefixWithArray = 'eq' | 'ne';

export type Prefix = 'eq' | 'ne' | 'gt' | 'lt' | 'ge' | 'le' | 'sa' | 'eb' | 'ap';

type Link = { relation: string, url: string }

export type BaseResponseResources<T extends keyof ResourceTypeMap> = {
  'query-time': number,
  meta: { versionId: string },
  type: string,
  resourceType: string,
  total: number,
  link: Link[],
  'query-timeout': number,
  entry: {
    resource: ResourceTypeMap[T];
  }[];
  'query-sql': (string | number)[]
};

export type BaseResponseResource<T extends keyof ResourceTypeMap> = ResourceTypeMap[T];

export type ResourceKeys<T extends keyof ResourceTypeMap, I extends ResourceTypeMap[T]> = Omit<I, UnnecessaryKeys>;

type SortKey<T extends keyof ResourceTypeMap> = keyof SearchParams[T] | `.${string}`

type ElementsParams<T extends keyof ResourceTypeMap, R extends ResourceTypeMap[T]> = Array<keyof ResourceKeys<T, R>>;

export class Client {
  client: AxiosInstance;

  constructor(baseURL: string, credentials: AxiosBasicCredentials) {
    this.client = axios.create({ baseURL, auth: credentials });
  }
  getResources<T extends keyof ResourceTypeMap>(resourceName: T) {
    return new GetResources(this.client, resourceName);
  }

  async getResource<T extends keyof ResourceTypeMap>(
    resourceName: T,
    id: string,
  ): Promise<BaseResponseResource<T> | Error> {
    const response = await this.client.get<BaseResponseResource<T>>(resourceName + '/' + id);
    return response.data;
  }

  async findResources<T extends keyof ResourceTypeMap>(
    resourceName: T,
    params: Record<string, unknown>,
  ): Promise<BaseResponseResources<T> | Error> {
    const response = await this.client.post<BaseResponseResources<T>>(resourceName, { params });
    return response.data;
  }

  async deleteResource<T extends keyof ResourceTypeMap>(
    resourceName: T,
    id: string,
  ): Promise<BaseResponseResource<T> | Error> {
    const response = await this.client.delete<BaseResponseResource<T>>(resourceName + '/' + id);
    return response.data;
  }

  async patchResource<T extends keyof ResourceTypeMap>(
    resourceName: T,
    id: string,
    body: PathResourceBody<T>,
  ): Promise<BaseResponseResource<T> | Error> {
    const response = await this.client.patch<BaseResponseResource<T>>(resourceName + '/' + id, { ...body });
    return response.data;
  }

  async createResource<T extends keyof ResourceTypeMap>(
    resourceName: T,
    body: ResourceTypeMap[T],
  ): Promise<BaseResponseResource<T> | Error> {
    const response = await this.client.post<BaseResponseResource<T>>(resourceName, { ...body });
    return response.data;
  }
}

export class GetResources<T extends keyof ResourceTypeMap, R extends ResourceTypeMap[T]>
  implements PromiseLike<BaseResponseResources<T>>
{
  private searchParamsObject: URLSearchParams;
  resourceName: T;
  client: AxiosInstance;

  constructor(client: AxiosInstance, resourceName: T) {
    this.client = client;
    this.searchParamsObject = new URLSearchParams();
    this.resourceName = resourceName;
  }

  where<K extends keyof SearchParams[T], SP extends SearchParams[T][K], PR extends PrefixWithArray>(
    key: K,
    value: SP | SP[],
    prefix?: PR,
  ): this;
  where<K extends keyof SearchParams[T], SP extends SearchParams[T][K], PR extends Exclude<Prefix, PrefixWithArray>>(
    key: K,
    value: SP,
    prefix?: PR,
  ): this;
  where<K extends keyof SearchParams[T], SP extends SearchParams[T][K], PR extends SP extends number ? Prefix : never>(
    key: K,
    value: SP | SP[],
    prefix?: Prefix | never,
  ): this {
    if (!Array.isArray(value)) {
      const queryValue = `${prefix ?? ''}${value}`;

      this.searchParamsObject.append(key.toString(), queryValue);
      return this;
    }

    if (prefix) {
      if (prefix === 'eq') {
        this.searchParamsObject.append(key.toString(), value.join(','));
        return this;
      }

      value.map((item) => {
        this.searchParamsObject.append(key.toString(), `${prefix}${item}`);
      });

      return this;
    }

    const queryValues = value.join(',');
    this.searchParamsObject.append(key.toString(), queryValues);

    return this;
  }

  contained(contained: boolean | 'both', containedType?: 'container' | 'contained') {
    this.searchParamsObject.set('_contained', contained.toString());

    if (containedType) {
      this.searchParamsObject.set('_containedType', containedType);
    }

    return this;
  }

  count(value: number) {
    this.searchParamsObject.set('_count', value.toString());

    return this;
  }

  elements(args: ElementsParams<T, R>) {
    const queryValue = args.join(',');

    this.searchParamsObject.set('_elements', queryValue);

    return this;
  }

  summary(type: boolean | 'text' | 'data' | 'count') {
    this.searchParamsObject.set('_summary', type.toString());

    return this;
  }

  sort(key: SortKey<T>, dir: Dir ) {
    const existedSortParams = this.searchParamsObject.get('_sort')

    if (existedSortParams) {
      const newSortParams = `${existedSortParams},${dir === 'asc' ? '-' : ''}${key.toString()}`

      this.searchParamsObject.set("_sort", newSortParams)
      return this;
    }

    this.searchParamsObject.set("_sort", dir === 'asc' ? `-${key.toString()}` : key.toString())

    return this;
  }

  then<TResult1 = BaseResponseResources<T>, TResult2 = never>(
    onfulfilled?: ((value: BaseResponseResources<T>) => PromiseLike<TResult1> | TResult1) | undefined | null,
    onrejected?: ((reason: any) => PromiseLike<TResult2> | TResult2) | undefined | null,
  ): PromiseLike<TResult1 | TResult2> {
    return this.client
      .get<BaseResponseResources<T>>(this.resourceName, {
        params: this.searchParamsObject,
      })
      .then((response) => {
        return onfulfilled ? onfulfilled(response.data) : (response.data as TResult1);
      });
  }
}
