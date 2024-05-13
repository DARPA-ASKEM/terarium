import {
	ClientEvent,
	ClientEventType,
	ExtractionStatusUpdate,
	ProgressState,
	TaskResponse,
	TaskStatus
} from '@/types/Types';
import { logger } from '@/utils/logger';
import { Ref } from 'vue';
import { NotificationItem, NotificationItemStatus } from '@/types/common';
import { snakeToCapitalSentence } from '@/utils/text';
import { getDocumentAsset } from './document-assets';

const isTaskResponse = (data: any): data is TaskResponse =>
	data.id !== undefined && data.script !== undefined && data.status;

export const getStatusFromTaskResponse = (data: TaskResponse) => {
	const statusMap = {
		[TaskStatus.Queued]: ProgressState.Queued,
		[TaskStatus.Running]: ProgressState.Running,
		[TaskStatus.Success]: ProgressState.Complete,
		[TaskStatus.Failed]: ProgressState.Failed,
		[TaskStatus.Cancelled]: ProgressState.Cancelled,
		[TaskStatus.Cancelling]: ProgressState.Cancelling
	};
	return statusMap[data.status];
};

export const getStatusFromProgress = (data: { error: string; t: number }) => {
	if (data.error) return ProgressState.Failed;
	if (data.t >= 1.0) return ProgressState.Complete;
	return ProgressState.Running;
};

const toastTitle = {
	[ClientEventType.ExtractionPdf]: {
		success: 'PDF Extraction Completed',
		error: 'PDF Extraction Error'
	}
};

const logStatusMessage = (
	eventType: ClientEventType,
	status: ProgressState,
	msg: string,
	error: string
) => {
	if (![ProgressState.Complete, ProgressState.Failed].includes(status)) return;

	if (status === ProgressState.Complete)
		logger.success(msg, {
			showToast: true,
			toastTitle: toastTitle[eventType]?.success ?? `${snakeToCapitalSentence(eventType)} Completed`
		});
	if (status === ProgressState.Failed)
		logger.error(error, {
			showToast: true,
			toastTitle: toastTitle[eventType]?.error ?? `${snakeToCapitalSentence(eventType)} Failed`
		});
};

const updateStatusFromTaskResponse = (
	event: ClientEvent<TaskResponse>
): NotificationItemStatus => ({
	status: getStatusFromTaskResponse(event.data),
	msg: `${snakeToCapitalSentence(event.type)} in progress...`,
	error: event.data.stderr
});

const buildNotificationItemStatus = <T>(
	event: ClientEvent<T | TaskResponse>
): NotificationItemStatus => {
	if (isTaskResponse(event.data))
		return updateStatusFromTaskResponse(event as ClientEvent<TaskResponse>);
	// Currently only ExtractionPdf events are supported. Assume all other events are ExtractionPdf events for now.
	// TODO: Replace ExtractionStatusUpdate with more generic type (e.g NotificationEventStatus<T>) that represent a notification event data type for other events.
	const eventData: ExtractionStatusUpdate = event.data as ExtractionStatusUpdate;
	return {
		status: getStatusFromProgress(eventData),
		msg: eventData.message,
		progress: eventData.t,
		error: eventData.error
	};
};

// Creates notification event handlers for each type of client events that manipulates given notification items ref
export const createNotificationEventHandlers = (notificationItems: Ref<NotificationItem[]>) => {
	const handlers = {} as Record<ClientEventType, (event: ClientEvent<any>) => void>;

	const registerHandler = <T>(
		eventType: ClientEventType,
		onCreateNewNotificationItem: (
			event: ClientEvent<T>,
			createdItem: NotificationItem
		) => void = () => {}
	) => {
		handlers[eventType] = (event: ClientEvent<T>) => {
			if (!event.data) return;
			const existingItem = notificationItems.value.find(
				(item) => item.notificationGroupId === event.notificationGroupId
			);
			const lastUpdated = new Date(event.createdAtMs).getTime();
			if (!existingItem) {
				const newItem: NotificationItem = {
					notificationGroupId: event.notificationGroupId ?? '',
					type: event.type,
					lastUpdated,
					acknowledged: false,
					supportCancel: false,
					assetId: '',
					assetName: '',
					...buildNotificationItemStatus(event)
				};
				notificationItems.value.push(newItem);
				onCreateNewNotificationItem(event, newItem);
				return;
			}
			// Update existing item status
			Object.assign(existingItem, {
				lastUpdated,
				...buildNotificationItemStatus(event)
			});
		};
	};

	// Register handlers for each client event type

	registerHandler<ExtractionStatusUpdate>(ClientEventType.ExtractionPdf, (event, created) => {
		created.assetId = event.data.documentId;
		// TODO: make sure UI is updated after updating assetId and name
		getDocumentAsset(event.data.documentId).then((document) =>
			Object.assign(created, { assetName: document?.name || '' })
		);
	});

	registerHandler<TaskResponse>(ClientEventType.TaskGollmModelCard, (event, created) => {
		created.supportCancel = true;
		created.assetId = event.data.additionalProperties.documentId;
		getDocumentAsset(created.assetId).then((document) =>
			Object.assign(created, { assetName: document?.name || '' })
		);
	});

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
		const notificationItem = visibleNotificationItems.value.find(
			(item) => item.notificationGroupId === event.notificationGroupId
		);
		if (!notificationItem) return;
		const status = buildNotificationItemStatus(event);
		logStatusMessage(event.type, status.status, status.msg, status.error);
	};
	return handleLogging;
};
