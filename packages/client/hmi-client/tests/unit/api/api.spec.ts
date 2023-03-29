import { describe, expect, it } from 'vitest';
import { Poller } from '@/api/api';

describe('API utilities test', () => {
	it('polling immediate', async () => {
		const poller = new Poller<number[]>()
			.setInterval(1000)
			.setThreshold(5)
			.setPollAction(async () => ({ data: [1, 2, 3], error: null }));

		const result = await poller.start();
		expect(result.data).deep.eq([1, 2, 3]);
	});

	it('polling several times and return data', async () => {
		let c = 0;
		const start = Date.now();
		const poller = new Poller<number[]>()
			.setInterval(1000)
			.setThreshold(5)
			.setPollAction(async () => {
				c++;
				if (c <= 3) {
					return { data: null, error: null };
				}
				return { data: [1, 2, 3], error: null };
			});

		const result2 = await poller.start();
		const end = Date.now();

		expect(end - start).gt(2000);
		expect(result2.data).deep.eq([1, 2, 3]);
	});

	it('polling exceeds threshold', async () => {
		let c = 0;
		const poller = new Poller<number[]>()
			.setInterval(1000)
			.setThreshold(3)
			.setPollAction(async () => {
				c++;
				if (c <= 10) {
					return { data: null, error: null };
				}
				return { data: [1, 2, 3], error: null };
			});

		const result = await poller.start();
		expect(result.state).eq('ExceedThreshold');
	});
});
