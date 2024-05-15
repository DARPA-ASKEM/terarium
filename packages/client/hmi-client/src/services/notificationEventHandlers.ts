import {
	AssetType,
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
import { ProjectPages } from '@/types/Project';
import { getDocumentAsset } from './document-assets';
import { getWorkflow } from './workflow';

export const getStatusFromProgress = (data: { error: string; t: number }) => {
	if (data.error) return ProgressState.Failed;
	if (data.t >= 1.0) return ProgressState.Complete;
	return ProgressState.Running;
};

const isTaskResponse = (data: any): data is TaskResponse =>
	data.id !== undefined && data.script !== undefined && data.status;

const getStatusFromTaskResponse = (data: TaskResponse) => {
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
	if (![ProgressState.Complete, ProgressState.Failed, ProgressState.Cancelled].includes(status))
		return;

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
	if (status === ProgressState.Cancelled)
		logger.info(msg, {
			showToast: true,
			toastTitle:
				toastTitle[eventType]?.cancelled ?? `${snakeToCapitalSentence(eventType)} Cancelled`
		});
};

const updateStatusFromTaskResponse = (event: ClientEvent<TaskResponse>): NotificationItemStatus => {
	const status = getStatusFromTaskResponse(event.data);
	const error = status === ProgressState.Failed ? event.data.stderr : '';
	const statusMsg = {
		[ProgressState.Running]: 'in progress...',
		[ProgressState.Cancelling]: 'is cancelling...',
		[ProgressState.Cancelled]: 'was cancelled.',
		[ProgressState.Complete]: 'completed.'
	};
	const msg = `${snakeToCapitalSentence(event.type)} ${statusMsg[status] ?? ''}`;
	return { status, msg, error };
};

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
					projectId: event.projectId,
					type: event.type,
					lastUpdated,
					acknowledged: false,
					supportCancel: false,
					contextPath: '',
					sourceName: '',
					assetId: '',
					pageType: ProjectPages.OVERVIEW, // Default to overview page
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
		created.pageType = AssetType.Document;
		getDocumentAsset(created.assetId).then((document) =>
			Object.assign(created, { sourceName: document?.name || '' })
		);
	});
	registerHandler<TaskResponse>(ClientEventType.TaskGollmModelCard, (event, created) => {
		created.supportCancel = true;
		created.assetId = event.data.additionalProperties.documentId as string;
		created.pageType = AssetType.Document;
		getDocumentAsset(created.assetId).then((document) =>
			Object.assign(created, { sourceName: document?.name || '' })
		);
	});
	registerHandler<TaskResponse>(ClientEventType.TaskGollmConfigureModel, (event, created) => {
		created.supportCancel = true;
		created.sourceName = 'Configure Model';
		created.assetId = event.data.additionalProperties.workflowId as string;
		created.pageType = AssetType.Workflow;
		created.nodeId = event.data.additionalProperties.nodeId as string;
		getWorkflow(created.assetId).then((workflow) =>
			Object.assign(created, { contextPath: workflow?.name || '' })
		);
	});
	registerHandler<TaskResponse>(ClientEventType.TaskGollmDatasetConfigure, (event, created) => {
		created.supportCancel = true;
		created.sourceName = 'Configure Model';
		created.assetId = event.data.additionalProperties.workflowId as string;
		created.pageType = AssetType.Workflow;
		created.nodeId = event.data.additionalProperties.nodeId as string;
		getWorkflow(created.assetId).then((workflow) =>
			Object.assign(created, { contextPath: workflow?.name || '' })
		);
	});
	registerHandler<TaskResponse>(ClientEventType.TaskGollmCompareModel, (event, created) => {
		created.supportCancel = true;
		created.sourceName = 'Compare Models';
		created.assetId = event.data.additionalProperties.workflowId as string;
		created.pageType = AssetType.Workflow;
		created.nodeId = event.data.additionalProperties.nodeId as string;
		getWorkflow(created.assetId).then((workflow) =>
			Object.assign(created, { contextPath: workflow?.name || '' })
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
	const handleLogging = <T>(event: ClientEvent<T>) => {
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
