import { ClientEvent, ClientEventType, ExtractionStatusUpdate } from '@/types/Types';
import { logger } from '@/utils/logger';
import { Ref } from 'vue';
import { NotificationItem } from '@/types/common';
import { getDocumentAsset } from './document-assets';

// Supported client event types for the notification manager
export const SUPPORTED_CLIENT_EVENT_TYPES = [ClientEventType.ExtractionPdf];

const getStatus = (data: { error: string; t: number }) => {
	if (data.error) return 'Failed';
	if (data.t >= 1.0) return 'Completed';
	return 'Running';
};

const toastTitle = {
	[ClientEventType.ExtractionPdf]: {
		success: 'PDF Extraction Completed',
		error: 'PDF Extraction Error'
	}
};

const displayToast = (eventType: ClientEventType, status: string, msg: string, error: string) => {
	if (!['Completed', 'Failed'].includes(status)) return;

	if (status === 'Completed')
		logger.success(msg, {
			showToast: true,
			toastTitle: toastTitle[eventType]?.success ?? 'Process Completed'
		});
	if (status === 'Failed')
		logger.error(error, {
			showToast: true,
			toastTitle: toastTitle[eventType]?.error ?? 'Process Failed'
		});
};

// Creates notification event handlers for each type of client events that manipulates given notification items ref
export const createNotificationEventHandlers = (notificationItems: Ref<NotificationItem[]>) => {
	const handlers: { [eventType in ClientEventType]?: (event: ClientEvent<any>) => void } = {};

	handlers[ClientEventType.ExtractionPdf] = (event: ClientEvent<ExtractionStatusUpdate>) => {
		if (!event.data) return;

		// TODO: extract this logic to a separate function and generalize
		displayToast(
			ClientEventType.ExtractionPdf,
			getStatus(event.data),
			event.data.message,
			event.data.error
		);

		const existingItem = notificationItems.value.find(
			(item) => item.notificationGroupId === event.data.notificationGroupId
		);
		if (!existingItem) {
			// Create a new notification item
			const newItem: NotificationItem = {
				notificationGroupId: event.data.notificationGroupId,
				type: ClientEventType.ExtractionPdf,
				assetId: event.data.documentId,
				assetName: '',
				status: getStatus(event.data),
				msg: event.data.message,
				progress: event.data.t,
				lastUpdated: new Date(event.createdAtMs).getTime(),
				error: event.data.error,
				acknowledged: false
			};
			notificationItems.value.push(newItem);
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
			lastUpdated: new Date(event.createdAtMs).getTime(),
			error: event.data.error
		});
	};

	const getHandler = (eventType: ClientEventType) => handlers[eventType] ?? (() => {});

	return {
		get: getHandler,
		getSupportedEventTypes: () => Object.keys(handlers) as ClientEventType[]
	};
};

// export const createNotificationEventLoggers = (visibleNotificationItems: Ref<NotificationItem[]>) => {
// 	const handleLogging = (event: ClientEvent<{data: {notificationGroupId: string, status: string, msg: string, error: string }}>) => {
// 		if ('notificationGroupId' in event.data) {
// 			const exist = visibleNotificationItems.value.find(item => item.notificationGroupId === event.data.notificationGroupId);
// 			if (!exist) return;

// 		}

// 	}

// }
