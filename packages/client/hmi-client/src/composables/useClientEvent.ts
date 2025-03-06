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
export function createTaskListClientEventHandler(node: WorkflowNode<BaseState>, taskIdsKey: string, emit) {
	return async (event: ClientEvent<TaskResponse>) => {
		const taskIds = node.state[taskIdsKey];
		if (!taskIds?.includes(event.data?.id) || !event.data) return;
		if ([TaskStatus.Success, TaskStatus.Cancelled, TaskStatus.Failed].includes(event.data.status)) {
			node.state[taskIdsKey] = taskIds.filter((id) => id !== event.data.id);
			switch (event.data.status) {
				case TaskStatus.Success:
					node.status = OperatorStatus.SUCCESS;
					break;
				case TaskStatus.Failed:
					node.status = OperatorStatus.ERROR;
					break;
				case TaskStatus.Cancelled:
				default:
					node.status = OperatorStatus.DEFAULT;
			}
		}
		if (node.state[taskIdsKey].length > 0) {
			node.status = OperatorStatus.IN_PROGRESS;
		}
		emit('update-state', node.state);
	};
}

export function createTaskProgressClientEventHandler(
	node: WorkflowNode<DocumentOperationState>,
	progressKey: string,
	emit
) {
	return async (event: ClientEvent<TaskResponse> | NotificationEvent) => {
		if (!node.state) return;
		if (event.data?.data?.documentId !== node.state?.documentId) return;
		const taskState = event.data.state || event.data.status;
		node.state[progressKey] = event.data?.progress;
		if ([TaskStatus.Success, TaskStatus.Cancelled, TaskStatus.Failed].includes(taskState)) {
			node.state[progressKey] = undefined;
			switch (taskState) {
				case TaskStatus.Success:
					node.status = OperatorStatus.SUCCESS;
					break;
				case TaskStatus.Failed:
					node.status = OperatorStatus.ERROR;
					break;
				case TaskStatus.Cancelled:
				default:
					node.status = OperatorStatus.DEFAULT;
			}
		} else {
			node.status = OperatorStatus.IN_PROGRESS;
		}
		emit('update-state', node.state);
	};
}

export function createEnrichClientEventHandler(taskStatus: Ref, assetId: string | null) {
	return async (event: ClientEvent<TaskResponse>) => {
		const { datasetId, documentId, modelId } = event.data.additionalProperties;
		if (assetId !== datasetId && assetId !== documentId && assetId !== modelId) return;
		if ([TaskStatus.Success, TaskStatus.Cancelled, TaskStatus.Failed].includes(event.data.status)) {
			taskStatus.value = event.data.status === TaskStatus.Failed ? OperatorStatus.ERROR : undefined;
		} else {
			taskStatus.value = OperatorStatus.IN_PROGRESS;
		}
	};
}
