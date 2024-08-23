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
	it('mae', () => {
		const arr1 = [
			{ time: 1, value: 1 },
			{ time: 2, value: 1 },
			{ time: 3, value: 1 },
			{ time: 4, value: 1 },
			{ time: 5, value: 1 }
		];
		const arr2 = [
			{ time: 0, value: 5 },
			{ time: 5, value: 5 }
		];
		const arr3 = [
			{ time: 10, value: 10 },
			{ time: 20, value: 10 }
		];
		let mae = -999;
		mae = stats.mae(arr1, arr2, 'time', 'value');
		expect(mae).to.equal(4);
		mae = stats.mae(arr1, arr3, 'time', 'value');
		expect(Number.isNaN(mae)).to.equal(true);
	});
});
