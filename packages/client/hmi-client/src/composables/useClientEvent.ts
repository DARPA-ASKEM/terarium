import { onMounted, onUnmounted } from 'vue';

import { subscribe, unsubscribe } from '@/services/ClientEventService';
import { type ClientEvent, ClientEventType, type StatusUpdate, type TaskResponse, TaskStatus } from '@/types/Types';

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

// accepts a state and key to a string[] to update with in progress task ids
export function createTaskListClientEventHandler(state, taskIdsKey: string) {
	const taskIds = state[taskIdsKey];
	return async (event: ClientEvent<TaskResponse>) => {
		if (!taskIds?.includes(event.data?.id) || !event.data) return;
		if ([TaskStatus.Success, TaskStatus.Cancelled, TaskStatus.Failed].includes(event.data.status)) {
			state[taskIdsKey] = taskIds.filter((id) => id !== event.data.id);
		}
	};
}

export function createTaskProgressClientEventHandler(state, progressKey: string) {
	return async (event: ClientEvent<StatusUpdate>) => {
		if (event.data.data.documentId === state.documentId) {
			state[progressKey] = event.data?.progress;
			if ([TaskStatus.Success, TaskStatus.Cancelled, TaskStatus.Failed].includes(event.data.status)) {
				state[progressKey] = undefined;
			}
		}
	};
}
