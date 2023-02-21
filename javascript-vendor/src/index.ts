import axios, {AxiosBasicCredentials, AxiosInstance} from 'axios';
import { ResourceTypeMap, SearchParams } from './aidbox-types';

export type BaseResponseResources<R extends keyof ResourceTypeMap> = { entry: ResourceTypeMap[R][] };

export type BaseResponseResource<R extends keyof ResourceTypeMap> = ResourceTypeMap[R];

export class Client{
  client: AxiosInstance;

  constructor(baseURL: string, credentials: AxiosBasicCredentials) {
    this.client = axios.create({ baseURL, auth: credentials });
  }
  getResources<T extends keyof ResourceTypeMap>(resourceName: T) {
    return new GetResources(this.client, resourceName)
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
    body: Partial<ResourceTypeMap[T]>,
  ): Promise<BaseResponseResource<T> | Error> {
    const response = await this.client.patch<BaseResponseResource<T>>(resourceName + '/' + id, { body });
    return response.data;
  }

  async createResource<T extends keyof ResourceTypeMap>(
    resourceName: T,
    body: ResourceTypeMap[T],
  ): Promise<BaseResponseResource<T> | Error> {
    const response = await this.client.post<BaseResponseResource<T>>(resourceName, { body });
    return response.data;
  }
}

class GetResources<T extends keyof ResourceTypeMap> implements PromiseLike<BaseResponseResources<T>>{
  private searchParamsObject: URLSearchParams;
  resourceName: T;
  client: AxiosInstance;

  constructor(client: AxiosInstance, resourceName: T) {
    this.client = client;
    this.searchParamsObject = new URLSearchParams();
    this.resourceName = resourceName;
  }

  where<K extends keyof SearchParams[T]>(key: K, value: SearchParams[T][K]) {
    this.searchParamsObject.append(key.toString(), value?.toString() ?? '')
    return this;
  }

  andWhere<K extends keyof SearchParams[T]>(key: K, value: SearchParams[T][K]) {
    this.searchParamsObject.append(key.toString(), value?.toString() ?? '')
    return this;
  }

 then<TResult1 = BaseResponseResources<T>, TResult2 = never>(onfulfilled?: ((value: BaseResponseResources<T>) => (PromiseLike<TResult1> | TResult1)) | undefined | null, onrejected?: ((reason: any) => (PromiseLike<TResult2> | TResult2)) | undefined | null): PromiseLike<TResult1 | TResult2> {
    return this.client.get<BaseResponseResources<T>>(this.resourceName, {
      params: this.searchParamsObject
    })
       .then((response) => {
         return onfulfilled ? onfulfilled(response.data ) : response.data as TResult1
       })
 }
}


const c = new Client('https://aidboxvlad.aidbox.app', { username: 'basic', password: 'secret' });


async function getPatients() {
  let patient = await c.getResources('Patient')
      .where("name", "Vlad")
      .andWhere('active', true)


  console.log(patient.entry)
}
async function getRes () {
  let a = await c.getResources('ChargeItem')
      .where("patient", "Patient/12")
      .andWhere("patient", "Patient/32")
      .where('factor-override', 2)



  console.log(a)
}

getPatients()




