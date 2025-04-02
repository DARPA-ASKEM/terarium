import { onMounted, onUnmounted, Ref } from 'vue';

import { subscribe, unsubscribe } from '@/services/ClientEventService';
import {
	type ClientEvent,
	ClientEventType,
	type NotificationEvent,
	type TaskResponse,
	ProgressState
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
		if ([ProgressState.Complete, ProgressState.Cancelled, ProgressState.Error].includes(event.data.status)) {
			node.state[taskIdsKey] = taskIds.filter((id) => id !== event.data.id);
			switch (event.data.status) {
				case ProgressState.Complete:
					node.status = ProgressState.Complete;
					break;
				case ProgressState.Error:
					node.status = ProgressState.Error;
					break;
				case ProgressState.Cancelled:
				default:
					node.status = ProgressState.Complete;
			}
		}
		if (node.state[taskIdsKey].length > 0) {
			node.status = ProgressState.Running;
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
		if ([ProgressState.Complete, ProgressState.Cancelled, ProgressState.Error].includes(taskState)) {
			node.state[progressKey] = undefined;
			node.status = taskState;
		} else {
			node.status = OperatorStatus.RUNNING;
		}
		emit('update-state', node.state);
	};
}
// TODO: remove OperatorStatus
export function createEnrichClientEventHandler(taskStatus: Ref, assetId: string | null, emit) {
	return async (event: ClientEvent<TaskResponse>) => {
		const { datasetId, documentId, modelId } = event.data.additionalProperties;
		if (assetId !== datasetId && assetId !== documentId && assetId !== modelId) return;
		if (ProgressState.Complete === event.data.status) {
			taskStatus.value = OperatorStatus.COMPLETE;
			emit('finished-job');
		} else {
			taskStatus.value = event.data.status;
		}
	};
}
