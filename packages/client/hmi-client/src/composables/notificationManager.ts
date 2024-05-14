import { subscribe } from '@/services/ClientEventService';
import { NotificationItem } from '@/types/common';
import { ref, computed } from 'vue';
import {
	acknowledgeNotification,
	convertToClientEvents,
	getLatestUnacknowledgedNotifications
} from '@/services/notification';
import {
	createNotificationEventHandlers,
	createNotificationEventLogger
} from '@/services/notificationEventHandlers';
import { ProgressState } from '@/types/Types';
import { useProjects } from './project';

let initialized = false;

const { findAsset } = useProjects();

const isNotificationForActiveProject = (item: NotificationItem) => !!findAsset(item.assetId);
// const isNotificationForActiveProject = (item: NotificationItem) => { console.log(item); return true };

const isFinished = (item: NotificationItem) =>
	[ProgressState.Complete, ProgressState.Failed, ProgressState.Cancelled].includes(item.status);

// Items stores the notifications for all projects
const items = ref<NotificationItem[]>([]);

export function useNotificationManager() {
	const itemsForActiveProject = computed(() => items.value.filter(isNotificationForActiveProject));

	const hasFinishedItems = computed(() =>
		itemsForActiveProject.value.some((item: NotificationItem) => isFinished(item))
	);
	const unacknowledgedFinishedItems = computed(() =>
		itemsForActiveProject.value.filter(
			(item: NotificationItem) => isFinished(item) && !item.acknowledged
		)
	);

	async function init() {
		// Make sure this init function gets called only once for the lifetime of the app
		if (initialized) return;
		const handlers = createNotificationEventHandlers(items);
		// Supported client event types for the notification manager
		const supportedEventTypes = handlers.getSupportedEventTypes();

		const initialEvents = (await getLatestUnacknowledgedNotifications(supportedEventTypes))
			.map(convertToClientEvents)
			.flat();
		initialEvents.forEach((event) => handlers.get(event.type)(event));

		// Initialize SSE event handlers for the subsequent events for the notification manager
		supportedEventTypes.forEach((eventType) => subscribe(eventType, handlers.get(eventType)));
		// Attach handlers for logging
		supportedEventTypes.forEach((eventType) =>
			subscribe(eventType, createNotificationEventLogger(itemsForActiveProject))
		);

		initialized = true;
	}

	function clearFinishedItems() {
		itemsForActiveProject.value.forEach((item) => {
			if (item.status !== ProgressState.Running) acknowledgeNotification(item.notificationGroupId);
		});
		items.value = items.value.filter(
			(item) => !isNotificationForActiveProject(item) || item.status === ProgressState.Running
		);
	}

	function acknowledgeFinishedItems() {
		items.value.forEach((item) => {
			if (isFinished(item)) {
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
