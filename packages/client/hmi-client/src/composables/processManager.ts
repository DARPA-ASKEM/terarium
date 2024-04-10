import { subscribe } from '@/services/ClientEventService';
import { getDocumentAsset } from '@/services/document-assets';
import { ClientEvent, ClientEventType, ExtractionStatusUpdate } from '@/types/Types';
import { ProcessItem } from '@/types/common';
import { ref, computed } from 'vue';

const items = ref<ProcessItem[]>([
	{
		id: '1',
		projectId: '4cebc2ba-98f0-4183-bc1c-598b77e4e03e',
		status: 'Running',
		lastUpdated: 1712786899469,
		assetName: 'Document Document Document Document Document Document Document Document.pdf',
		type: ClientEventType.Extraction,
		msg: 'Extracting text from PDF',
		progress: 0.5
	},
	{
		id: '2',
		projectId: '4cebc2ba-98f0-4183-bc1c-598b77e4e03e',
		status: 'Running',
		lastUpdated: 1712786899469,
		assetName: 'Document',
		type: ClientEventType.Extraction,
		msg: 'Extracting text from PDF',
		progress: 0.5
	},
	{
		id: '3',
		projectId: '4cebc2ba-98f0-4183-bc1c-598b77e4e03e',
		status: 'Running',
		lastUpdated: 1712786899469,
		assetName: 'Document',
		type: ClientEventType.Extraction,
		msg: 'Extracting text from PDF',
		progress: 0.5
	},
	{
		id: '4',
		projectId: '4cebc2ba-98f0-4183-bc1c-598b77e4e03e',
		status: 'Running',
		lastUpdated: 1712786899469,
		assetName: 'Document',
		type: ClientEventType.Extraction,
		msg: 'Extracting text from PDF',
		progress: 0.5
	},
	{
		id: '5',
		projectId: '4cebc2ba-98f0-4183-bc1c-598b77e4e03e',
		status: 'Running',
		lastUpdated: 1712786899469,
		assetName: 'Document',
		type: ClientEventType.Extraction,
		msg: 'Extracting text from PDF',
		progress: 0.5
	},
	{
		id: '6',
		projectId: '4cebc2ba-98f0-4183-bc1c-598b77e4e03e',
		status: 'Running',
		lastUpdated: 1712786899469,
		assetName: 'Document',
		type: ClientEventType.Extraction,
		msg: 'Extracting text from PDF',
		progress: 0.5
	},
	{
		id: '7',
		projectId: '4cebc2ba-98f0-4183-bc1c-598b77e4e03e',
		status: 'Running',
		lastUpdated: 1712786899469,
		assetName: 'Document',
		type: ClientEventType.Extraction,
		msg: 'Extracting text from PDF',
		progress: 0.5
	},
	{
		id: '8',
		projectId: '4cebc2ba-98f0-4183-bc1c-598b77e4e03e',
		status: 'Running',
		lastUpdated: 1712786899469,
		assetName: 'Document',
		type: ClientEventType.Extraction,
		msg: 'Extracting text from PDF',
		progress: 0.5
	},
	{
		id: '9',
		projectId: '4cebc2ba-98f0-4183-bc1c-598b77e4e03e',
		status: 'Failed',
		lastUpdated: 1712786899469,
		assetName: 'Document',
		type: ClientEventType.Extraction,
		msg: 'Extracting text from PDF',
		progress: 0.5
	},
	{
		id: 'l0',
		projectId: '4cebc2ba-98f0-4183-bc1c-598b77e4e03e',
		status: 'Failed',
		lastUpdated: 1712786899469,
		assetName: 'Document',
		type: ClientEventType.Extraction,
		msg: 'Extracting text from PDF',
		progress: 0.5
	},
	{
		id: '11',
		projectId: '4cebc2ba-98f0-4183-bc1c-598b77e4e03e',
		status: 'Completed',
		lastUpdated: 1712786899469,
		assetName: 'Document',
		type: ClientEventType.Extraction,
		msg: 'Extracting text from PDF',
		progress: 1
	},
	{
		id: '12',
		projectId: '4cebc2ba-98f0-4183-bc1c-598b77e4e03e',
		status: 'Completed',
		lastUpdated: 1712786899469,
		assetName: 'Document',
		type: ClientEventType.FileUploadProgress,
		msg: 'Extracting text from PDF',
		progress: 1
	}
]);

const extractionEventHandler = async (event: ClientEvent<ExtractionStatusUpdate>) => {
	if (!event.data) return;
	const existingItem = items.value.find((item) => item.id === event.data.documentId);
	if (!existingItem) {
		// Create a new process item
		const document = await getDocumentAsset(event.data.documentId);
		items.value.push({
			id: event.data.documentId,
			projectId: '',
			type: ClientEventType.Extraction,
			assetName: document?.name || '',
			status: event.data.t >= 1.0 ? 'Completed' : 'Running',
			msg: event.data.message,
			progress: event.data.t,
			lastUpdated: Date.now()
		});
		return;
	}
	// Update the existing item
	Object.assign(existingItem, {
		status: event.data.t >= 1.0 ? 'Completed' : 'Running',
		msg: event.data.message,
		progress: event.data.t,
		lastUpdated: Date.now()
	});
};

export function useProcessManager() {
	const activeProjectId = ref('');

	const itemsByActiveProject = computed(() =>
		items.value.filter((item) => item.projectId === activeProjectId.value)
	);

	function init() {
		// Initialize SSE event handlers for the process manager
		subscribe(ClientEventType.Extraction, extractionEventHandler);
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
