import axios, { AxiosBasicCredentials, AxiosInstance } from 'axios';

interface Patient {}
export interface SearchParams {
  id?: string;
}

export type BaseResponse<R extends keyof Resources> = Resources[R];

export type Resources = {
  Patient: Patient;
};

export class Client {
  private client: AxiosInstance;

  constructor(baseURL: string, credentials: AxiosBasicCredentials) {
    this.client = axios.create({ baseURL, auth: credentials });
  }

  async getResource<T extends keyof Resources>(resourceName: T, id: string): Promise<BaseResponse<T> | Error> {
    try {
      const response = await this.client.get<BaseResponse<T>>(resourceName + '/' + id);
      return response.data;
    } catch (e) {
      throw e;
    }
  }

  async findResources() {}

  async deleteResource() {}

  async updateResource() {}
}
