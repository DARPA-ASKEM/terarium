import { fetchEventSource, EventSourceMessage } from '@microsoft/fetch-event-source';
import { ClientEvent, ClientEventType } from '@/types/Types';
import useAuthStore from '@/stores/auth';
import { logger } from '@/utils/logger';

const subscribers = new Map<ClientEventType, ((data: ClientEvent<any>) => void)[]>();

/**
 * Message handler. Parse the {@link EventSourceMessage} and call the subscribers for the {@link ClientEventType}
 * @param event The {@link EventSourceMessage} from the SSE emitter
 */
function handleMessage(event: EventSourceMessage) {
	const data = JSON.parse(event.data) as ClientEvent<any>;
	const handlers = subscribers.get(data.type);
	if (handlers) {
		handlers.forEach((handler) => handler(data));
	}
}

/**
 * Connects to the SSE endpoint and adds a message handler to pass on the messages to the subscribers
 */
export async function init(): Promise<void> {
	const authStore = useAuthStore();
	const options = {
		headers: {
			Authorization: `Bearer ${authStore.token}`
		},
		onmessage: handleMessage,
		async onopen(response: Response) {
			init();
			if (response.status === 401) {
				// redirect to login
				authStore.keycloak?.login({
					redirectUri: window.location.href
				});
			}
		},
		onerror(error: any) {
			logger.error(`EventSource error: ${error}`);
		},
		onclose() {
			init();
		}
	};
	await fetchEventSource('/api/client-event', options);
}

/**
 * Subscribes to a specific event type
 * @param eventType       The event type to subscribe to
 * @param messageHandler  The message handler
 */
export async function subscribe(
	eventType: ClientEventType,
	messageHandler: (data: ClientEvent<any>) => void
): Promise<void> {
	if (!subscribers.has(eventType)) {
		subscribers.set(eventType, []);
	}
	subscribers.get(eventType)?.push(messageHandler);
}

/**
 * Unsubscribes from a specific event type
 * @param eventType
 * @param messageHandler
 */
export async function unsubscribe(
	eventType: ClientEventType,
	messageHandler: (data: ClientEvent<any>) => void
): Promise<void> {
	const handlers = subscribers.get(eventType);
	if (handlers) {
		const index = handlers.indexOf(messageHandler);
		if (index > -1) {
			handlers.splice(index, 1);
		}
	}
}
