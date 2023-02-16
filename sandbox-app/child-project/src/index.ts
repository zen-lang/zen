import axios, { AxiosBasicCredentials, AxiosInstance } from 'axios';

export interface SearchParams {
  id?: string;
}

export type BaseResponseResources<R extends keyof Resources> = { entry: Resources[R][] };

export type BaseResponseResource<R extends keyof Resources> = Resources[R];

export type Resources = {
  Patient: Patient;
};

export class Client {
  private client: AxiosInstance;

  constructor(baseURL: string, credentials: AxiosBasicCredentials) {
    this.client = axios.create({ baseURL, auth: credentials });
  }

  async getResources<T extends keyof Resources>(resourceName: T): Promise<BaseResponseResources<T> | Error> {
    const response = await this.client.get<BaseResponseResources<T>>(resourceName);
    return response.data;
  }

  async getResource<T extends keyof Resources>(resourceName: T, id: string): Promise<BaseResponseResource<T> | Error> {
    const response = await this.client.get<BaseResponseResource<T>>(resourceName + '/' + id);
    return response.data;
  }

  async findResources<T extends keyof Resources>(
    resourceName: T,
    params: Record<string, unknown>,
  ): Promise<BaseResponseResources<T> | Error> {
    const response = await this.client.post<BaseResponseResources<T>>(resourceName, { params });
    return response.data;
  }

  async deleteResource<T extends keyof Resources>(
    resourceName: T,
    id: string,
  ): Promise<BaseResponseResource<T> | Error> {
    const response = await this.client.delete<BaseResponseResource<T>>(resourceName + '/' + id);
    return response.data;
  }

  async patchResource<T extends keyof Resources>(
    resourceName: T,
    id: string,
    body: Partial<Resources[T]>,
  ): Promise<BaseResponseResource<T> | Error> {
    const response = await this.client.patch<BaseResponseResource<T>>(resourceName + '/' + id, { body });
    return response.data;
  }

  async createResource<T extends keyof Resources>(
    resourceName: T,
    body: Partial<Resources[T]>,
  ): Promise<BaseResponseResource<T> | Error> {
    const response = await this.client.post<BaseResponseResource<T>>(resourceName, { body });
    return response.data;
  }
}
