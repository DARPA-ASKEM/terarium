import {
	ClientEvent,
	ClientEventType,
	ExtractionStatusUpdate,
	TaskNotificationEventData
} from '@/types/Types';
import { logger } from '@/utils/logger';
import { Ref } from 'vue';
import { NotificationItem } from '@/types/common';
import { getDocumentAsset } from './document-assets';

export const getStatus = (data: { error: string; t: number }) => {
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

const logStatusMessage = (
	eventType: ClientEventType,
	status: string,
	msg: string,
	error: string
) => {
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
	const handlers = {} as Record<ClientEventType, (event: ClientEvent<any>) => void>;

	handlers[ClientEventType.ExtractionPdf] = (event: ClientEvent<ExtractionStatusUpdate>) => {
		if (!event.data) return;

		const existingItem = notificationItems.value.find(
			(item) => item.notificationGroupId === event.notificationGroupId
		);
		if (!existingItem) {
			// Create a new notification item
			const newItem: NotificationItem = {
				notificationGroupId: event.notificationGroupId ?? '',
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

	handlers[ClientEventType.TaskGollmModelCard] = (
		event: ClientEvent<TaskNotificationEventData>
	) => {
		// TODO: Create a notification item and implement notification item UI for this event
		console.log(event);
	};

	const getHandler = (eventType: ClientEventType) => handlers[eventType] ?? (() => {});

	return {
		get: getHandler,
		getSupportedEventTypes: () => Object.keys(handlers) as ClientEventType[]
	};
};

/**
 * Creates notification event logger for the provided visible notification items.
 * @param visibleNotificationItems
 * @returns
 */
export const createNotificationEventLogger = (
	visibleNotificationItems: Ref<NotificationItem[]>
) => {
	const handleLogging = <
		T extends { notificationGroupId: string; t: number; message: string; error: string }
	>(
		event: ClientEvent<T>
	) => {
		if (!event.notificationGroupId) return;
		const found = visibleNotificationItems.value.find(
			(item) => item.notificationGroupId === event.notificationGroupId
		);
		if (!found) return;
		logStatusMessage(
			event.type,
			getStatus(event.data),
			event.data.message || '',
			event.data.error || ''
		);
	};
	return handleLogging;
};
