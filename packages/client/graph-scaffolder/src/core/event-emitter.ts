import { EventCallback, EventName } from '../types';

export default class EventEmitter {
	listeners: Map<EventName, Set<EventCallback>> = new Map();

	on(eventName: EventName, fn: EventCallback): void {
		if (!this.listeners.has(eventName)) {
			this.listeners.set(eventName, new Set());
		}
		this.listeners.get(eventName)?.add(fn);
	}

	once(eventName: EventName, fn: EventCallback): void {
		if (!this.listeners.has(eventName)) {
			this.listeners.set(eventName, new Set());
		}

		const onceWrapper = (evtName: EventName, ...args: any[]) => {
			fn(evtName, ...args);
			this.off(eventName, onceWrapper);
		};
		this.listeners.get(eventName)?.add(onceWrapper);
	}

	off(eventName: EventName, fn: EventCallback): void {
		const set = this.listeners.get(eventName);
		if (set) {
			set.delete(fn);
		}
	}

	emit(eventName: EventName, ...args: any[]): boolean {
		const fns = this.listeners.get(eventName);
		if (!fns) return false;

		fns.forEach((f) => {
			f(eventName, ...args);
		});
		return true;
	}

	// listenerCount(eventName) {
	//   let fns = this.listeners[eventName] || [];
	//   return fns.length;
	// }
	// rawListeners(eventName) {
	//   return this.listeners[eventName];
	// }
}
