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
		const response = await API.post('/gollm/model-card', null, {
			params: {
				'document-id': documentId,
				'model-id': modelId
			}
		});

		// FIXME: I think we need to refactor the response interceptors so that we can handle errors here, or even in the interceptor itself...might be worth a discussion
		return response.data;
	} catch (err) {
		logger.error(err);
		return null;
	}
}

/**
 * Handles SSE connection for a given task ID.
 * @param {string} id - The task ID.
 */
export async function handleTaskById(id: string) {
	const sseHandler = new SSEHandler(`/gollm/${id}`, {
		ondata(data) {
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
