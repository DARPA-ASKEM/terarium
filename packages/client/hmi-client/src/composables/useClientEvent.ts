import { onMounted, onUnmounted, Ref } from 'vue';

import { subscribe, unsubscribe } from '@/services/ClientEventService';
import {
	type ClientEvent,
	ClientEventType,
	type NotificationEvent,
	type TaskResponse,
	TaskStatus
} from '@/types/Types';
import { BaseState, WorkflowNode } from '@/types/workflow';
import { DocumentOperationState } from '@/components/workflow/ops/document/document-operation';

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
export function createTaskListClientEventHandler(node: WorkflowNode<BaseState>, taskIdsKey: string) {
	const { state } = node;
	const taskIds = state[taskIdsKey];
	return async (event: ClientEvent<TaskResponse>) => {
		if (!taskIds?.includes(event.data?.id) || !event.data) return;
		if ([TaskStatus.Success, TaskStatus.Cancelled, TaskStatus.Failed].includes(event.data.status)) {
			state[taskIdsKey] = taskIds.filter((id) => id !== event.data.id);
		}
	};
}

export function createTaskProgressClientEventHandler(node: WorkflowNode<DocumentOperationState>, progressKey: string) {
	const { state } = node;
	return async (event: ClientEvent<TaskResponse> | NotificationEvent) => {
		if (event.data.data.documentId === state.documentId) {
			state[progressKey] = event.data?.progress;
			if (
				event.data.status &&
				[TaskStatus.Success, TaskStatus.Cancelled, TaskStatus.Failed].includes(event.data.status)
			) {
				state[progressKey] = undefined;
			}
		}
	};
}

export function createEnrichClientEventHandler(taskId: Ref, assetId: string | undefined, emit) {
	return async (event: ClientEvent<TaskResponse>) => {
		if (taskId.value !== event.data?.id) return;
		if (assetId !== event.data.additionalProperties.datasetId && assetId !== event.data.additionalProperties.documentId)
			return;
		if ([TaskStatus.Success, TaskStatus.Cancelled, TaskStatus.Failed].includes(event.data.status)) {
			taskId.value = event.data.status === TaskStatus.Failed ? TaskStatus.Failed : '';
			emit('finished-job');
		}
	};
}
