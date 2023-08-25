import useAuthStore from '@/stores/auth';
import { EventSourcePolyfill } from 'event-source-polyfill';

type MessageHandler = (message: any) => void;

// This class gives us a way to handle event sources
export class EventSourceManager {
	private connections: Map<string, EventSourcePolyfill> = new Map();

	public messageHandlers: Map<string, MessageHandler> = new Map();

	// open a connection with a unique id, if a connection is already open we will not open it again
	public openConnection(id: string) {
		if (!this.connections.has(id)) {
			const auth = useAuthStore();
			const eventSource = new EventSourcePolyfill(`/api/simulations/${id}/partial-result`, {
				headers: {
					Authorization: `Bearer ${auth.token}`
				}
			});

			eventSource.onopen = () => {
				console.log(`EventSource opened for ID: ${id}`);
			};

			eventSource.onmessage = (event) => {
				const handler = this.messageHandlers.get(id);
				if (handler) {
					handler(event.data);
				}
			};

			eventSource.onerror = (error) => {
				console.error(`EventSource error for ID ${id}: ${error}`);
			};

			this.connections.set(id, eventSource);
		} else {
			console.log(`EventSource already open for ID: ${id}`);
		}
	}

	// handle message for a connection id with a handler function
	public setMessageHandler(id: string, handler: MessageHandler) {
		this.messageHandlers.set(id, handler);
	}

	public closeConnection(id: string) {
		const eventSource = this.connections.get(id);

		if (eventSource) {
			eventSource.close();
			console.log(`EventSource closed for ID: ${id}`);
			this.messageHandlers.delete(id);
			this.connections.delete(id);
		}
	}
}
