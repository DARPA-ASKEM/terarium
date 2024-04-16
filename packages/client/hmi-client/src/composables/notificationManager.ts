import { subscribe } from '@/services/ClientEventService';
import { ClientEvent, ClientEventType, ExtractionStatusUpdate } from '@/types/Types';
import { NotificationItem } from '@/types/common';
import { ref, computed } from 'vue';
import { getDocumentAsset } from '@/services/document-assets';
import { useToastService } from '@/services/toast';
import { useProjects } from './project';

let initialized = false;

const { findAsset } = useProjects();

// Items stores the notifications for all projects
const items = ref<NotificationItem[]>([]);

const toastTitle = {
	[ClientEventType.ExtractionPdf]: {
		success: 'PDF Extraction Completed',
		error: 'PDF Extraction Error'
	}
};

const displayToast = (
	assetId: string,
	eventType: ClientEventType,
	status: string,
	msg: string,
	error: string
) => {
	if (!['Completed', 'Failed'].includes(status)) return;
	if (!findAsset(assetId)) return; // Check if the asset is in the active project

	if (status === 'Completed')
		useToastService().success(toastTitle[eventType]?.success ?? 'Process Completed', msg);
	if (status === 'Failed')
		useToastService().error(toastTitle[eventType]?.error ?? 'Process Failed', error);
};

const getStatus = (data: { error: string; t: number }) => {
	if (data.error) return 'Failed';
	if (data.t >= 1.0) return 'Completed';
	return 'Running';
};

const extractionEventHandler = (event: ClientEvent<ExtractionStatusUpdate>) => {
	if (!event.data) return;

	displayToast(
		event.data.documentId,
		ClientEventType.ExtractionPdf,
		getStatus(event.data),
		event.data.message,
		event.data.error
	);

	const existingItem = items.value.find((item) => item.id === event.data.documentId);
	if (!existingItem) {
		// Create a new notification item
		const newItem: NotificationItem = {
			id: event.data.documentId,
			type: ClientEventType.ExtractionPdf,
			assetName: '',
			status: getStatus(event.data),
			msg: event.data.message,
			progress: event.data.t,
			lastUpdated: Date.now(),
			error: event.data.error,
			acknowledged: false
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

export function useNotificationManager() {
	const itemsForActiveProject = computed(() => items.value.filter((item) => !!findAsset(item.id)));
	const hasFinishedItems = computed(() =>
		itemsForActiveProject.value.some(
			(item: NotificationItem) => item.status === 'Completed' || item.status === 'Failed'
		)
	);
	const unacknowledgedFinishedItems = computed(() =>
		itemsForActiveProject.value.filter(
			(item: NotificationItem) =>
				(item.status === 'Completed' || item.status === 'Failed') && !item.acknowledged
		)
	);

	function init() {
		// Make sure this init function gets called only once for the lifetime of the app
		if (initialized) return;
		// Initialize SSE event handlers for the notification manager
		subscribe(ClientEventType.ExtractionPdf, extractionEventHandler);
		initialized = true;
	}

	function clearFinishedItems() {
		items.value = items.value.filter((item) => !!findAsset(item.id) && item.status === 'Running');
	}

	function acknowledgeFinishedItems() {
		items.value.forEach((item) => {
			if (['Completed', 'Failed'].includes(item.status)) {
				item.acknowledged = true;
			}
		});
	}

	return {
		init,
		itemsForActiveProject,
		clearFinishedItems,
		acknowledgeFinishedItems,
		hasFinishedItems,
		unacknowledgedFinishedItems
	};
}
