import API, { FatalError, TaskEventHandlers, TaskHandler } from '@/api/api';
import type { TaskResponse } from '@/types/Types';
import { TaskStatus } from '@/types/Types';
import { logger } from '@/utils/logger';

/**
 * Fetches model card data from the server and wait for task to finish.
 * @param {string} modelId - The model ID.
 * @param {string} documentId - The document ID.
 */
export async function modelCard(modelId: string, documentId?: string): Promise<void> {
	try {
		const response = await API.post<TaskResponse>('/gollm/model-card', null, {
			params: {
				'model-id': modelId,
				'document-id': documentId
			}
		});
		// FIXME: I think we need to refactor the response interceptors so that we can handle errors here, or even in the interceptor itself...might be worth a discussion
		const taskId = response.data.id;
		await handleTaskById(taskId, {
			ondata(data, closeConnection) {
				if (data?.status === TaskStatus.Failed) {
					closeConnection();
					throw new FatalError('Task failed');
				}
				if (data.status === TaskStatus.Success) {
					closeConnection();
				}
			}
		});
	} catch (err) {
		logger.error(err);
	}
}

export async function configureModelFromDocument(
	documentId: string,
	modelId: string,
	workflowId?: string,
	nodeId?: string
): Promise<TaskResponse> {
	const { data } = await API.get<TaskResponse>('/gollm/configure-model', {
		params: {
			'model-id': modelId,
			'document-id': documentId,
			'workflow-id': workflowId,
			'node-id': nodeId
		}
	});
	return data;
}

export async function configureModelFromDataset(
	modelId: string,
	datasetId: string,
	matrixStr: string,
	workflowId?: string,
	nodeId?: string
): Promise<TaskResponse> {
	const { data } = await API.post<TaskResponse>(
		'/gollm/configure-from-dataset',
		{ matrixStr },
		{
			params: {
				'model-id': modelId,
				'dataset-id': datasetId,
				'workflow-id': workflowId,
				'node-id': nodeId
			}
		}
	);
	return data;
}

export async function compareModels(modelIds: string[], workflowId?: string, nodeId?: string): Promise<TaskResponse> {
	const { data } = await API.get<TaskResponse>('/gollm/compare-models', {
		params: {
			'model-ids': modelIds.join(','),
			'workflow-id': workflowId,
			'node-id': nodeId
		}
	});
	return data;
}

export async function cancelTask(taskId: string): Promise<void> {
	try {
		await API.put<TaskResponse>(`/gollm/${taskId}`);
	} catch (err) {
		logger.error(`An issue occurred while cancelling task with id: ${taskId}. ${err}`);
	}
}

/**
 * Handles task for a given task ID.
 * @param {string} id - The task ID.
 */
export async function handleTaskById(id: string, handlers: TaskEventHandlers): Promise<TaskHandler> {
	const taskHandler = new TaskHandler(`/gollm/${id}`, handlers);
	await taskHandler.start();
	return taskHandler;
}
