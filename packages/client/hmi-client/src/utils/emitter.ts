export type EventCallback = (args: any) => void;
export type EventName = string | symbol;

/**
 * A simple event emitter with arbitary payload
 *
 * Usage:
 *
 *   const bus = new EventEmitter();
 *   bus.on('ping', (n) => { console.log(`pong ${n}`); })
 *
 *   for (let i = 0; i < 3; i++) {
 *     bus.emit('ping', i);
 *   }
 *
 * */
export class EventEmitter {
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

		const onceWrapper = (args: any) => {
			fn(args);
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

	removeAllEvents(eventName: EventName): void {
		if (this.listeners.has(eventName)) {
			this.listeners.delete(eventName);
		}
	}

	emit(eventName: EventName, args?: any): boolean {
		const fns = this.listeners.get(eventName);
		if (!fns) return false;

		fns.forEach((f) => {
			f(args);
		});
		return true;
	}
}
