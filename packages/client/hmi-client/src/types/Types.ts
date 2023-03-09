/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 3.1.1185 on 2023-03-09 17:09:45.

export interface Event {
	id?: string;
	timestampMillis?: number;
	projectId?: number;
	username?: string;
	type: EventType;
	value?: string;
}

export enum EventType {
	Search = 'SEARCH'
}
