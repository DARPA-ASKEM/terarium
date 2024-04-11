import { subscribe } from '@/services/ClientEventService';
import { getDocumentAsset } from '@/services/document-assets';
import { ClientEvent, ClientEventType, ExtractionStatusUpdate } from '@/types/Types';
import { ProcessItem } from '@/types/common';
import { ref, computed } from 'vue';

const items = ref<ProcessItem[]>([]);

const getStatus = (data: { error: string; t: number }) => {
	if (data.error) return 'Failed';
	if (data.t >= 1.0) return 'Completed';
	return 'Running';
};

const extractionEventHandler = (event: ClientEvent<ExtractionStatusUpdate>) => {
	if (!event.data) return;
	const existingItem = items.value.find((item) => item.id === event.data.documentId);
	if (!existingItem) {
		// Create a new process item
		const newItem: ProcessItem = {
			id: event.data.documentId,
			projectId: '38e2f5f1-8c35-45c6-9735-09edd8318f69',
			type: ClientEventType.ExtractionPdf,
			assetName: '',
			status: getStatus(event.data),
			msg: event.data.message,
			progress: event.data.t,
			lastUpdated: Date.now(),
			error: event.data.error
		};
		items.value.push(newItem);
		// Update asset name asynchronously on the next tick to avoid blocking the event handler
		getDocumentAsset(event.data.documentId).then((document) =>
			Object.assign(newItem, { assetName: document?.name || '' })
		);
		return;
	}
	// Update the existing item
	Object.assign(existingItem, {
		status: getStatus(event.data),
		msg: event.data.message,
		progress: event.data.t,
		lastUpdated: Date.now(),
		error: event.data.error
	});
};

export function useProcessManager() {
	const activeProjectId = ref('');

	const itemsByActiveProject = computed(() =>
		items.value.filter((item) => item.projectId === activeProjectId.value)
	);

	function init() {
		// Initialize SSE event handlers for the process manager
		subscribe(ClientEventType.ExtractionPdf, extractionEventHandler);
	}

	function setActiveProjectId(projectId: string) {
		activeProjectId.value = projectId;
	}

	function clearFinishedItems() {
		items.value = items.value.filter(
			(item) => item.projectId === activeProjectId.value && item.status === 'Running'
		);
	}

	return { init, itemsByActiveProject, setActiveProjectId, clearFinishedItems };
}
