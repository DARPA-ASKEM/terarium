/* tslint:disable */
/* eslint-disable */

export interface Event {
    id?: string;
    timestampMillis?: number;
    projectId?: number;
    username?: string;
    type: EventType;
    value?: string;
}

export interface DocumentAsset {
    id?: number;
    title: string;
    xdd_uri: string;
}

export enum EventType {
    Search = "SEARCH",
}
