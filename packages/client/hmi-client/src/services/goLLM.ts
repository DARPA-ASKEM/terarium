import API, { SSEHandler, SSEStatus } from '@/api/api';
import type { TaskResponse } from '@/types/Types';
import { TaskStatus } from '@/types/Types';
import { logger } from '@/utils/logger';

/**
 * Fetches model card data from the server.
 * @param {string} documentId - The document ID.
 * @param {string} modelId - The model ID.
 * @returns {Promise<TaskResponse|null>} A promise resolving to the task response or null if unsuccessful.
 */
export async function modelCard(documentId: string, modelId: string): Promise<TaskResponse | null> {
	try {
		const response = await API.post('/gollm/model_card', null, {
			params: {
				'document-id': documentId,
				'model-id': modelId
			}
		});

		// FIXME: I think we need to refactor the response interceptors so that we can handle errors here, or even in the interceptor itself...might be worth a discussion
		if (response.status !== 200) {
			switch (response.status) {
				case 404: {
					logger.error('The provided model or document arguments are not found');
					break;
				}
				case 500:
				default: {
					logger.error('There was an issue dispatching the request');
					break;
				}
			}
			return null;
		}
		return response.data;
	} catch (err) {
		logger.error(err);
		return null;
	}
}

/**
 * Handles SSE connection for a given task ID.
 * @param {string} id - The task ID.
 * @param {(message: any) => void} successHandler - The success handler function.
 */
export async function handleTaskById(id: string) {
	const sseHandler = new SSEHandler(`/api/gollm/${id}`, {
		onmessage(message) {
			const data = JSON.parse(message);
			if (data?.status === TaskStatus.Failed) {
				throw Error('Task failed');
			}
			if (data.status === TaskStatus.Success) {
				sseHandler.setStatus(SSEStatus.DONE);
				sseHandler.closeConnection();
			}
		}
	});
	const result = await sseHandler.connect();
	return result;
}
