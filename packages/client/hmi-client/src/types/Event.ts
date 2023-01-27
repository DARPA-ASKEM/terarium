import { EventType } from '@/types/EventType';

export interface Event {
	id?: string;
	timestampMillis?: number;
	projectId?: number;
	username?: string;
	type: EventType;
	value?: string;
}
