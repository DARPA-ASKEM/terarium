import { onMounted, onUnmounted } from 'vue';
import _ from 'lodash';

import { subscribe, unsubscribe } from '@/services/ClientEventService';
import { type ClientEvent, ClientEventType, type TaskResponse, TaskStatus } from '@/types/Types';

export function useClientEvent(eventType: ClientEventType | ClientEventType[], taskIds: string[]) {
	const handlerFn = createClientEventHandler(taskIds);
	const eventTypes = Array.isArray(eventType) ? eventType : [eventType];
	onMounted(async () => {
		await Promise.all(eventTypes.map((type) => subscribe(type, handlerFn)));
	});
	onUnmounted(async () => {
		await Promise.all(eventTypes.map((type) => unsubscribe(type, handlerFn)));
	});
}

function createClientEventHandler(taskIds) {
	return async (event: ClientEvent<TaskResponse>) => {
		if (!_.isArray(taskIds)) {
			taskIds = [];
		}
		if (!taskIds.value.includes(event.data?.id)) return;
		if ([TaskStatus.Success, TaskStatus.Cancelled, TaskStatus.Failed].includes(event.data.status)) {
			taskIds.value = taskIds.value.filter((id) => id !== event.data.id);
		}
	};
}
