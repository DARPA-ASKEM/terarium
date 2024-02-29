import API, { FatalError, TaskEventHandlers, TaskHandler } from '@/api/api';
import type { TaskResponse } from '@/types/Types';
import { TaskStatus } from '@/types/Types';
import { logger } from '@/utils/logger';
import { CompareModelsResponseType } from '@/types/common';

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

export async function configureModel(documentId: string, modelId: string): Promise<void> {
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
					throw new FatalError('Task failed');
				}
				if (data.status === TaskStatus.Success) {
					closeConnection();
				}
			}
		});
	} catch (err) {
		logger.error(`An issue occurred while extracting a model configuration. ${err}`);
	}
}

export async function compareModels(modelIds: string[]): Promise<CompareModelsResponseType> {
	let compareResult: CompareModelsResponseType = {
		response: ''
	};

	try {
		const response = await API.get<TaskResponse>('/gollm/compare-models', {
			params: {
				models: modelIds.join(',')
			}
		});

		const taskId = response.data.id;

		await handleTaskById(taskId, {
			ondata(data, closeConnection) {
				if (data?.status === TaskStatus.Failed) {
					throw new FatalError('Task failed');
				}
				if (data.status === TaskStatus.Success) {
					closeConnection();

					// data.output is a base64 encoded json object. We decode it and return the json object.
					const str = atob(data.output);
					compareResult = JSON.parse(str);
				}
			}
		});
	} catch (err) {
		logger.error(`An issue occurred while comparing models. ${err}`);
	}

	return compareResult;
}

/**
 * Handles task for a given task ID.
 * @param {string} id - The task ID.
 */
export async function handleTaskById(id: string, handlers: TaskEventHandlers) {
	const taskHandler = new TaskHandler(`/gollm/${id}`, handlers);
	await taskHandler.start();
}
