import API from '@/api/api';
import { ClientEvent, ClientEventType, NotificationEvent, NotificationGroup } from '@/types/Types';
import { logger } from '@/utils/logger';

/**
 * Get notification
 * @return Array<NotificationGroup> - the list of all notifications
 */
export async function getNotifications(): Promise<NotificationGroup[]> {
	const { data } = await API.get('/notification');
	return data as NotificationGroup[];
}

/**
 * Get the list of the latest unacknowledged notification events
 * @returns Array<NotificationEvent> - the latest unacknowledged notification events
 */
export async function getLatestUnacknowledgedNotifications(
	allowedEventTypes?: ClientEventType[]
): Promise<NotificationGroup[]> {
	let notifications = await getNotifications();
	if (allowedEventTypes) {
		notifications = notifications.filter((notification: NotificationGroup) =>
			allowedEventTypes.includes(notification.type as ClientEventType)
		);
	}
	// Only include the latest notification event per notification group
	notifications.forEach((notification: NotificationGroup) => {
		notification.notificationEvents = notification.notificationEvents.splice(0, 1);
	});
	return notifications;
}

/**
 * Convert notification group to client events
 * @param notificationGroup
 * @returns Array<ClientEvent<T>> - the list of client events
 */
export function convertToClientEvents<T>(notificationGroup: NotificationGroup) {
	const { notificationEvents, type } = notificationGroup;
	if (!Object.values(ClientEventType).includes(type as ClientEventType)) {
		logger.error(`Notification type: ${type} is not supported client event type`, {
			showToast: false
		});
		return [];
	}

	const events: ClientEvent<T>[] = notificationEvents.map((event: NotificationEvent) => ({
		id: event.id || '',
		createdAtMs: new Date(event.createdOn || Date.now()).getTime(),
		type: type as ClientEventType,
		notificationGroupId: notificationGroup.id,
		projectId: notificationGroup.projectId,
		data: event.data
	}));
	return events;
}

/**
 * Acknowledge notification
 * @param notificationGroupId - the notification group id
 */
export async function acknowledgeNotification(notificationGroupId: string) {
	await API.put(`/notification/ack/${notificationGroupId}`);
}
