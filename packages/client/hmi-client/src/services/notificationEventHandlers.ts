import _ from 'lodash';
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
import { getDocumentAsset } from './document-assets';

export const getStatus = (data: { error: string; t: number }) => {
	if (data.error) return ProgressState.Failed;
	if (data.t >= 1.0) return ProgressState.Complete;
	return ProgressState.Running;
};
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
			toastTitle: toastTitle[eventType]?.success ?? 'Process Completed'
		});
	if (status === ProgressState.Failed)
		logger.error(error, {
			showToast: true,
			toastTitle: toastTitle[eventType]?.error ?? 'Process Failed'
		});
};

const updateStatusFromTaskResponse = (
	event: ClientEvent<TaskResponse>
): NotificationItemStatus => ({
	status: getStatusFromTaskResponse(event.data),
	msg: `${_.startCase(_.toLower(event.type))} in progress...`,
	error: event.data.stderr
});

// Creates notification event handlers for each type of client events that manipulates given notification items ref
export const createNotificationEventHandlers = (notificationItems: Ref<NotificationItem[]>) => {
	const handlers = {} as Record<ClientEventType, (event: ClientEvent<any>) => void>;

	const registerHandler = <T>(
		eventType: ClientEventType,
		updateStatusFn: (event: ClientEvent<T>) => NotificationItemStatus,
		onCreate: (event: ClientEvent<T>, createdItem: NotificationItem) => void = () => {}
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
					assetId: '',
					assetName: '',
					...updateStatusFn(event)
				};
				notificationItems.value.push(newItem);
				onCreate(event, newItem);
				return;
			}
			// Update existing item status
			Object.assign(existingItem, {
				lastUpdated,
				...updateStatusFn(event)
			});
		};
	};

	registerHandler<ExtractionStatusUpdate>(
		ClientEventType.ExtractionPdf,
		(event) => ({
			status: getStatus(event.data),
			msg: event.data.message,
			progress: event.data.t,
			error: event.data.error
		}),
		(event, created) => {
			created.assetId = event.data.documentId;
			// TODO: make sure UI is updated after updating assetId and name
			getDocumentAsset(event.data.documentId).then((document) =>
				Object.assign(created, { assetName: document?.name || '' })
			);
		}
	);

	registerHandler<TaskResponse>(
		ClientEventType.TaskGollmModelCard,
		updateStatusFromTaskResponse,
		(event, created) => {
			created.assetId = event.data.additionalProperties.documentId;
			getDocumentAsset(created.assetId).then((document) =>
				Object.assign(created, { assetName: document?.name || '' })
			);
		}
	);

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
