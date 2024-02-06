import API from '@/api/api';
import useAuthStore from '@/stores/auth';
import { TaskStatus } from '@/types/Types';
import { EventSourceMessage, fetchEventSource } from '@microsoft/fetch-event-source';

const controller = new AbortController();

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
	const options = {
		headers: {
			Authorization: `Bearer ${authStore.token}`
		},
		onmessage(message: EventSourceMessage) {
			const data = JSON.parse(message.data);
			if (data?.status === TaskStatus.Failed) {
				console.log('Task failed');
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
