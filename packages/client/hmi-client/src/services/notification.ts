import API from '@/api/api';
import { ClientEventType, NotificationEvent, NotificationGroup } from '@/types/Types';

/**
 * Get notification
 * @return Array<NotificationGroup> - the list of all notifications
 */
export async function getNotification(): Promise<NotificationGroup[]> {
	const { data } = await API.get('/notification');
	return data as NotificationGroup[];
}

/**
 * Get the list of the latest unacknowledged notification event per notification group
 * @returns Array<NotificationEvent> - the latest unacknowledged notification events
 */
export async function getLatestUnacknowledgedNotificationEvents(
	allowedEventTypes?: ClientEventType[]
): Promise<NotificationEvent[]> {
	let notifications = await getNotification();
	if (allowedEventTypes) {
		notifications = notifications.filter((notification: NotificationGroup) =>
			allowedEventTypes.includes(notification.type as ClientEventType)
		);
	}
	// Only show the latest notification event per notification group
	const events = notifications
		.filter((notification: NotificationGroup) => notification.notificationEvents.length > 0)
		.map((notification: NotificationGroup) => notification.notificationEvents[0]);
	return events;
}

/**
 * Acknowledge notification
 * @param notificationId - the ID of the notification to acknowledge
 * @return Notification - the acknowledged notification
 */
export async function acknowledgeNotification(notificationId: string) {
	const { data } = await API.put(`/notification/ack/${notificationId}`);
	return data;
}
