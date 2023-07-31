import { createMatrix2D } from '@/utils/pivot';
import { describe, expect, it } from 'vitest';

const data: any[] = [
	{ fruit: 'apple', location: 'L1' },
	{ fruit: 'apple', location: 'L2' },
	{ fruit: 'orange', location: 'L2' },
	{ fruit: 'grape', location: 'L1' }
];

describe('pivot table tests', () => {
	it('square pivot table', () => {
		const r = createMatrix2D(data, ['fruit'], ['fruit']);

		expect(r.matrix.length).to.eq(3);
		expect(r.matrix[0].length).to.eq(3);
	});
});
