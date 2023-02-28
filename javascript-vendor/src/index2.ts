import axios, {AxiosBasicCredentials, AxiosError, AxiosInstance} from 'axios';
import { ResourceTypeMap, SearchParams } from './aidbox-types';

export type BaseResponseResources<R extends keyof ResourceTypeMap> = { entry: ResourceTypeMap[R][] };

export type BaseResponseResource<R extends keyof ResourceTypeMap> = ResourceTypeMap[R];

export type SearchParamsKeysVariations = "or" | "and"

export type SearchParamsValues<R extends keyof ResourceTypeMap, K extends keyof SearchParams[R]> =
    | Partial<Record<SearchParamsKeysVariations, SearchParams[R][K][]>>
    | SearchParams[R][K]

export type SearchParamsProps<R extends keyof ResourceTypeMap> = {
    [P in keyof SearchParams[R]]: SearchParamsValues<R, P>
}


export class Client{
    client: AxiosInstance;

    constructor(baseURL: string, credentials: AxiosBasicCredentials) {
        this.client = axios.create({ baseURL, auth: credentials });
    }
    async getResources<T extends keyof ResourceTypeMap>(resourceName: T, searchParams?: Partial<SearchParamsProps<T>>) {
        const response = await this.client.get<BaseResponseResources<T>>(resourceName);
        return response.data
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

const aidbox = new Client('https://aidboxvlad.aidbox.app', { username: 'basic', password: 'secret' });

async function getPatient () {
    const patients = await aidbox.getResources('Patient', {
        phone: {and: ["2", '2']},
        language: "a",
        "general-practitioner": "Patient/1"
    })
}






getPatient()



