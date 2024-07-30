import { FIFOCache } from '@/utils/FifoCache';
import { describe, expect, it } from 'vitest';

describe('fifo cache test', () => {
	it('can store value', () => {
		const cache = new FIFOCache<string>(3);
		cache.set('a', 'foo');
		cache.set('b', 'bar');

		expect(cache.get('a')).to.eq('foo');
		expect(cache.get('b')).to.eq('bar');
		expect(cache.get('c')).toBeUndefined();
	});

	it('expunge policy', () => {
		const cache = new FIFOCache<string>(3);
		for (let i = 0; i < 10; i++) {
			cache.set(`${i}`, `${i}`);
		}

		console.log(cache);

		expect(cache.get('1')).toBeUndefined();
		expect(cache.get('6')).toBeUndefined();
		expect(cache.get('8')).to.eq('8');
	});
});
