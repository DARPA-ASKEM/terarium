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
		const map1 = new Map<number, number>([
			[1, 1],
			[2, 1],
			[3, 1],
			[4, 1],
			[5, 1]
		]);
		const map2 = new Map<number, number>([
			[0, 5],
			[5, 5]
		]);
		const map3 = new Map<number, number>([
			[10, 10],
			[20, 10]
		]);
		let mae = -999;
		mae = stats.mae(map1, map2);
		expect(mae).to.equal(4);
		mae = stats.mae(map1, map3);
		expect(Number.isNaN(mae)).to.equal(true);
	});
});
