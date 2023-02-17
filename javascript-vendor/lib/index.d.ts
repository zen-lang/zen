import { AxiosBasicCredentials } from 'axios';
import { ResourceTypeMap } from './aidbox-types';
export type BaseResponseResources<R extends keyof ResourceTypeMap> = {
  entry: ResourceTypeMap[R][];
};
export type BaseResponseResource<R extends keyof ResourceTypeMap> = ResourceTypeMap[R];
export declare class Client {
  private client;
  constructor(baseURL: string, credentials: AxiosBasicCredentials);
  getResources<T extends keyof ResourceTypeMap>(resourceName: T): Promise<BaseResponseResources<T> | Error>;
  getResource<T extends keyof ResourceTypeMap>(resourceName: T, id: string): Promise<BaseResponseResource<T> | Error>;
  findResources<T extends keyof ResourceTypeMap>(
    resourceName: T,
    params: Record<string, unknown>,
  ): Promise<BaseResponseResources<T> | Error>;
  deleteResource<T extends keyof ResourceTypeMap>(
    resourceName: T,
    id: string,
  ): Promise<BaseResponseResource<T> | Error>;
  patchResource<T extends keyof ResourceTypeMap>(
    resourceName: T,
    id: string,
    body: Partial<ResourceTypeMap[T]>,
  ): Promise<BaseResponseResource<T> | Error>;
  createResource<T extends keyof ResourceTypeMap>(
    resourceName: T,
    body: Partial<ResourceTypeMap[T]>,
  ): Promise<BaseResponseResource<T> | Error>;
}
