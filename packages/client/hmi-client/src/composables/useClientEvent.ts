import { onMounted, onUnmounted } from 'vue';
import _ from 'lodash';

import { subscribe, unsubscribe } from '@/services/ClientEventService';
import { type ClientEvent, ClientEventType, type TaskResponse, TaskStatus } from '@/types/Types';

export function useClientEvent(
	eventType: ClientEventType | ClientEventType[],
	handlerFn: (data: ClientEvent<any>) => void
) {
	const eventTypes = Array.isArray(eventType) ? eventType : [eventType];
	onMounted(async () => {
		await Promise.all(eventTypes.map((type) => subscribe(type, handlerFn)));
	});
	onUnmounted(async () => {
		await Promise.all(eventTypes.map((type) => unsubscribe(type, handlerFn)));
	});
}

// accepts a string[] to update with in progress task ids
export function createClientEventHandler(state, taskIdKey: string) {
	const taskIds = state[taskIdKey];
	return async (event: ClientEvent<TaskResponse>) => {
		if (!taskIds.includes(event.data?.id)) return;
		if ([TaskStatus.Success, TaskStatus.Cancelled, TaskStatus.Failed].includes(event.data.status)) {
			state[taskIdKey] = taskIds.filter((id) => id !== event.data.id);
		}
	};
}
