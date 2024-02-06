import API from '@/api/api';
import useAuthStore from '@/stores/auth';
import { TaskStatus } from '@/types/Types';
import { logger } from '@/utils/logger';
import { EventSourceMessage, fetchEventSource } from '@microsoft/fetch-event-source';

export async function modelCard(documentId: string, modelId: string): Promise<any> {
	const response = await API.post('/gollm/model_card', null, {
		params: {
			'document-id': documentId,
			'model-id': modelId
		}
	});

	return response.data ?? null;
}

export async function handleTaskById(id: string, successHandler: (message: any) => void) {
	const authStore = useAuthStore();
	const controller = new AbortController();
	const options = {
		headers: {
			Authorization: `Bearer ${authStore.token}`
		},
		onmessage(message: EventSourceMessage) {
			console.log(message);
			const data = JSON.parse(message.data);
			if (data?.status === TaskStatus.Failed) {
				logger.error('Failed to generate model card.');
				controller.abort();
			}

			if (data.status === TaskStatus.Success) {
				successHandler(data);
				console.log('Aborting connection on success');
				controller.abort();
			}
		},
		async onopen(response: Response) {
			console.log('open');
			console.log(response);
		},
		onerror(error: Error) {
			throw error;
		},
		onclose() {
			console.log('closed');
		},
		signal: controller.signal,
		openWhenHidden: true
	};
	await fetchEventSource(`/api/gollm/${id}`, options);
}
