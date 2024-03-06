import API, { TaskHandler, FatalError, TaskEventHandlers } from '@/api/api';
import type { TaskResponse } from '@/types/Types';
import { TaskStatus } from '@/types/Types';
import { logger } from '@/utils/logger';

/**
 * Fetches model card data from the server and wait for task to finish.
 * @param {string} documentId - The document ID.
 * @param {string} modelId - The model ID.
 */
export async function modelCard(documentId: string): Promise<void> {
	try {
		const response = await API.post<TaskResponse>('/gollm/model-card', null, {
			params: {
				'document-id': documentId
			}
		});

		// FIXME: I think we need to refactor the response interceptors so that we can handle errors here, or even in the interceptor itself...might be worth a discussion
		const taskId = response.data.id;
		await handleTaskById(taskId, {
			ondata(data, closeConnection) {
				if (data?.status === TaskStatus.Failed) {
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
	modelId: string
): Promise<void> {
	try {
		const response = await API.get<TaskResponse>('/gollm/configure-model', {
			params: {
				'model-id': modelId,
				'document-id': documentId
			}
		});

		const taskId = response.data.id;
		await handleTaskById(taskId, {
			ondata(data, closeConnection) {
				if (data?.status === TaskStatus.Failed) {
					throw new FatalError('Configs from document - Task failed');
				}
				if (data.status === TaskStatus.Success) {
					logger.success('Model configured from document');
					closeConnection();
				}
			}
		});
	} catch (err) {
		logger.error(`An issue occured while exctracting a model configuration from document. ${err}`);
	}
}

export async function configureModelFromDatasets(modelId: string, datasetIds: string[]) {
	try {
		// FIXME: Using first dataset for now...
		const response = await API.post<TaskResponse>('/gollm/configure-from-dataset', null, {
			params: {
				'model-id': modelId,
				'dataset-ids': datasetIds.join()
			}
		});

		const taskId = response.data.id;
		await handleTaskById(taskId, {
			ondata(data, closeConnection) {
				if (data?.status === TaskStatus.Failed) {
					throw new FatalError('Configs from datasets - Task failed');
				}
				if (data.status === TaskStatus.Success) {
					logger.success('Model configured from datasets');
					closeConnection();
				}
			}
		});
	} catch (err) {
		logger.error(`An issue occured while exctracting a model configuration from dataset. ${err}`);
	}
}

/**
 * Handles task for a given task ID.
 * @param {string} id - The task ID.
 */
export async function handleTaskById(id: string, handlers: TaskEventHandlers) {
	const taskHandler = new TaskHandler(`/gollm/${id}`, handlers);
	await taskHandler.start();
}
