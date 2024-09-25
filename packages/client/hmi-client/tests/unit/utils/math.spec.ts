import { describe, expect, it } from 'vitest';
import { percentile } from '@/utils/math';
// percentile tests

describe('math utl tests', () => {
	describe('percentile tests', () => {
		it('should correctly calculate the nth percentile number', () => {
			expect(percentile([1, 2, 3, 4, 5], 50)).to.equal(3);
			expect(percentile([1, 2, 3, 4, 5], 25)).to.equal(2);
			expect(percentile([1, 2, 3, 4, 5], 75)).to.equal(4);
			expect(percentile([1, 2, 3, 4], 50)).to.equal(2.5);
			expect(percentile([1, 2, 3, 4], 25)).to.equal(1.75);
			expect(percentile([1, 2, 3, 4], 75)).to.equal(3.25);
		});
		it('should correctly calculate the nth percentile number for extremes', () => {
			expect(percentile([1, 2, 3, 4, 5], 0)).to.equal(1);
			expect(percentile([1, 2, 3, 4, 5], 100)).to.equal(5);
		});
		it('should fail for invalid percentiles', () => {
			expect(() => percentile([1, 2, 3, 4, 5], -1)).to.throw();
			expect(() => percentile([1, 2, 3, 4, 5], 101)).to.throw();
		});
	});
});
