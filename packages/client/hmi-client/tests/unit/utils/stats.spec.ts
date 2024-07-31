import * as stats from '@/utils/stats';
import { describe, expect, it } from 'vitest';

describe('statistics utilities', () => {
	it('stddev', () => {
		let stddev = -999;

		const numbers1 = [1, 2, 3, 4, 5];
		stddev = stats.stddev(numbers1);
		expect(stddev).to.be.closeTo(1.5811, 0.0001);

		const numbers2 = [9, 9, 9, 9, 9];
		stddev = stats.stddev(numbers2);
		expect(stddev).to.eq(0);
	});
});
