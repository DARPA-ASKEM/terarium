import API, { FatalError, TaskEventHandlers, TaskHandler } from '@/api/api';
import type { TaskResponse } from '@/types/Types';
import { TaskStatus } from '@/types/Types';
import { CompareModelsResponseType } from '@/types/common';
import { logger } from '@/utils/logger';
import { b64DecodeUnicode } from '@/utils/binary';

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
	handlers: TaskEventHandlers,
	workflowId?: string,
	nodeId?: string
): Promise<TaskHandler | null> {
	try {
		const response = await API.get<TaskResponse>('/gollm/configure-model', {
			params: {
				'model-id': modelId,
				'document-id': documentId,
				'workflow-id': workflowId,
				'node-id': nodeId
			}
		});

		const taskId = response.data.id;
		return await handleTaskById(taskId, handlers);
	} catch (err) {
		const message = `An issue occurred while extracting a model configuration from document. ${err}`;
		logger.error(message);
		console.debug(message);
	}

	return null;
}

export async function configureModelFromDatasets(
	modelId: string,
	datasetIds: string[],
	matrixStr: string,
	handlers: TaskEventHandlers,
	workflowId?: string,
	nodeId?: string
): Promise<TaskHandler | null> {
	try {
		// FIXME: Using first dataset for now...
		const response = await API.post<TaskResponse>(
			'/gollm/configure-from-dataset',
			{ matrixStr },
			{
				params: {
					'model-id': modelId,
					'dataset-ids': datasetIds.join(),
					'workflow-id': workflowId,
					'node-id': nodeId
				}
			}
		);

		const taskId = response.data.id;
		return await handleTaskById(taskId, handlers);
	} catch (err) {
		logger.error(`An issue occured while exctracting a model configuration from dataset. ${err}`);
	}

	return null;
}

export async function compareModels(
	modelIds: string[],
	workflowId?: string,
	nodeId?: string
): Promise<CompareModelsResponseType> {
	let resolve;
	let reject;

	const promise = new Promise<CompareModelsResponseType>((res, rej) => {
		resolve = res;
		reject = rej;
	});

	try {
		const response = await API.get<TaskResponse>('/gollm/compare-models', {
			params: {
				'model-ids': modelIds.join(','),
				'workflow-id': workflowId,
				'node-id': nodeId
			}
		});

		const taskId = response.data.id;

		await handleTaskById(taskId, {
			ondata(data, closeConnection) {
				if (data?.status === TaskStatus.Failed) {
					closeConnection();
					logger.warn(`Failed to Compare Models with ids: ${modelIds.join(',')}`);
					reject({ response: '' });
				}
				if (data.status === TaskStatus.Success) {
					closeConnection();

					// data.output is a base64 encoded json object. We decode it and return the json object.
					const str = b64DecodeUnicode(data.output);
					resolve(JSON.parse(str));
				}
			}
		});
	} catch (err) {
		logger.error(`An issue occurred while comparing models. ${err}`);
	}

	return promise;
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
export async function handleTaskById(
	id: string,
	handlers: TaskEventHandlers
): Promise<TaskHandler> {
	const taskHandler = new TaskHandler(`/gollm/${id}`, handlers);
	await taskHandler.start();
	return taskHandler;
}
