import { onMounted, onUnmounted, Ref } from 'vue';

import { subscribe, unsubscribe } from '@/services/ClientEventService';
import {
	type ClientEvent,
	ClientEventType,
	type NotificationEvent,
	type TaskResponse,
	TaskStatus
} from '@/types/Types';
import { BaseState, OperatorStatus, WorkflowNode } from '@/types/workflow';
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
export function createTaskListClientEventHandler(
	node: WorkflowNode<BaseState>,
	taskIdsKey: string,
	statusKey: string,
	emit
) {
	return async (event: ClientEvent<TaskResponse>) => {
		const taskIds = node.state[taskIdsKey];
		if (!taskIds?.includes(event.data?.id) || !event.data) return;
		if ([TaskStatus.Success, TaskStatus.Cancelled, TaskStatus.Failed].includes(event.data.status)) {
			node.state[taskIdsKey] = taskIds.filter((id) => id !== event.data.id);
			switch (event.data.status) {
				case TaskStatus.Success:
					node.state[statusKey] = OperatorStatus.SUCCESS;
					break;
				case TaskStatus.Failed:
					node.state[statusKey] = OperatorStatus.ERROR;
					break;
				case TaskStatus.Cancelled:
				default:
					node.state[statusKey] = OperatorStatus.DEFAULT;
			}
		}
		if (node.state[taskIdsKey].length > 0) {
			node.state[statusKey] = OperatorStatus.IN_PROGRESS;
		}
		emit('update-state');
	};
}

export function createTaskProgressClientEventHandler(
	node: WorkflowNode<DocumentOperationState>,
	progressKey: string,
	statusKey: string,
	emit
) {
	const { state } = node;
	return async (event: ClientEvent<TaskResponse> | NotificationEvent) => {
		const taskState = event.data.state || event.data.status;
		if (event.data.data.documentId === state.documentId) {
			node.state[progressKey] = event.data?.progress;
			if ([TaskStatus.Success, TaskStatus.Cancelled, TaskStatus.Failed].includes(taskState)) {
				node.state[progressKey] = undefined;
				switch (taskState) {
					case TaskStatus.Success:
						node.state[statusKey] = OperatorStatus.SUCCESS;
						break;
					case TaskStatus.Failed:
						node.state[statusKey] = OperatorStatus.ERROR;
						break;
					case TaskStatus.Cancelled:
					default:
						node.state[statusKey] = OperatorStatus.DEFAULT;
				}
			} else if (statusKey) {
				node.state[statusKey] = OperatorStatus.IN_PROGRESS;
			}
			emit('update-state');
		}
		console.log('New status for ');
		console.log(state);
		console.log(event.data);
		console.log(taskState);
	};
}

export function createEnrichClientEventHandler(taskStatus: Ref, assetId: string | null, emit) {
	return async (event: ClientEvent<TaskResponse>) => {
		const { datasetId, documentId, modelId } = event.data.additionalProperties;
		if (assetId !== datasetId && assetId !== documentId && assetId !== modelId) return;
		if ([TaskStatus.Success, TaskStatus.Cancelled, TaskStatus.Failed].includes(event.data.status)) {
			taskStatus.value = event.data.status === TaskStatus.Failed ? OperatorStatus.ERROR : undefined;
			emit('finished-job');
		} else {
			taskStatus.value = OperatorStatus.IN_PROGRESS;
			emit('append-output');
		}
	};
}
