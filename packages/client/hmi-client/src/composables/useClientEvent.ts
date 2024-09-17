import { subscribe, unsubscribe } from '@/services/ClientEventService';
import { ClientEvent, ClientEventType } from '@/types/Types';
import { onMounted, onUnmounted } from 'vue';

export function useClientEvent(eventType: ClientEventType, handlerFn: (data: ClientEvent<any>) => void) {
	onMounted(async () => {
		await subscribe(eventType, handlerFn);
	});
	onUnmounted(async () => {
		await unsubscribe(eventType, handlerFn);
	});
}
