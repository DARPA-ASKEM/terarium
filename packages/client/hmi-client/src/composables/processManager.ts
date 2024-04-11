import { subscribe } from '@/services/ClientEventService';
import { ClientEvent, ClientEventType, ExtractionStatusUpdate } from '@/types/Types';
import { ProcessItem } from '@/types/common';
import { ref, computed } from 'vue';
import { getDocumentAsset } from '@/services/document-assets';
import { useProjects } from './project';

const { findAsset } = useProjects();

// Items stores the processes for all projects
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
			type: ClientEventType.ExtractionPdf,
			assetName: '',
			status: getStatus(event.data),
			msg: event.data.message,
			progress: event.data.t,
			lastUpdated: Date.now(),
			error: event.data.error
		};
		items.value.push(newItem);
		// There's a delay until newly created asset (with assetName) is added to the active project's assets list so we need to fetch the asset name separately.
		// Update the asset name asynchronously on the next tick to avoid blocking the event handler
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
	const itemsForActiveProject = computed(() => items.value.filter((item) => !!findAsset(item.id)));

	function init() {
		// Initialize SSE event handlers for the process manager
		subscribe(ClientEventType.ExtractionPdf, extractionEventHandler);
	}

	function clearFinishedItems() {
		items.value = items.value.filter((item) => !!findAsset(item.id) && item.status === 'Running');
	}

	return { init, itemsForActiveProject, clearFinishedItems };
}
