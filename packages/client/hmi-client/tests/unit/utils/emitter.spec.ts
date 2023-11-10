import { describe, expect, it } from 'vitest';
import { EventEmitter } from '@/utils/emitter';

describe('event emitter', () => {
	it('general emit', () => {
		const bus = new EventEmitter();
		let counter = 0;
		const callback = (n: number) => {
			counter += n;
		};

		bus.on('ping', callback);
		for (let i = 1; i <= 4; i++) {
			bus.emit('ping', i);
		}
		expect(counter).to.eq(10);
	});

	it('once emit', () => {
		const bus = new EventEmitter();
		let counter = 0;
		const callback = (n: number) => {
			counter += n;
		};

		bus.once('ping', callback);
		for (let i = 1; i <= 4; i++) {
			bus.emit('ping', i);
		}
		expect(counter).to.eq(1);
	});

	it('off emit', () => {
		const bus = new EventEmitter();
		let counter = 0;
		const callback = (n: number) => {
			counter += n;
		};

		bus.on('ping', callback);
		for (let i = 1; i <= 4; i++) {
			bus.emit('ping', i);
			if (i === 3) {
				bus.off('ping', callback);
			}
		}
		expect(counter).to.eq(6);
	});
});
