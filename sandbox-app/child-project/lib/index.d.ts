import { AxiosBasicCredentials } from 'axios';
interface Patient {
}
export interface SearchParams {
    id?: string;
}
export type BaseResponse<R extends keyof Resources> = Resources[R];
export type Resources = {
    Patient: Patient;
};
export declare class Client {
    private client;
    constructor(baseURL: string, credentials: AxiosBasicCredentials);
    getResource<T extends keyof Resources>(resourceName: T, id: string): Promise<BaseResponse<T> | Error>;
    findResources(): Promise<void>;
    deleteResource(): Promise<void>;
    updateResource(): Promise<void>;
}
export {};
