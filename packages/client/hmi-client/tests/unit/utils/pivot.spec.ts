import { createMatrix2D } from '@/utils/pivot';
import { describe, expect, it } from 'vitest';

const data: any[] = [
	{ fruit: ['apple'], location: ['L1'] },
	{ fruit: ['apple'], location: ['L2'] },
	{ fruit: ['orange'], location: ['L2'] },
	{ fruit: ['grape'], location: ['L1'] }
];

const data2: any[] = [{ fruit: ['apple', 'apple'] }, { fruit: ['pear', 'pear'] }, { fruit: ['orange', 'orange'] }];

const data3: any[] = [{ fruit: ['apple', 'pear'] }, { fruit: ['apple', 'orange'] }];

const data4: any[] = [{ fruit: ['pear'] }, { fruit: ['orange'] }];

describe('pivot table tests', () => {
	it('square pivot table', () => {
		const r = createMatrix2D(data, ['fruit'], ['fruit']);

		expect(r.matrix.length).to.eq(3);
		expect(r.matrix[0].length).to.eq(3);
	});

	it('2D pivot with identity pattern', () => {
		const r = createMatrix2D(data2, ['fruit'], ['fruit']);
		expect(r.matrix.length).to.eq(3);

		let undefinedCells = 0;
		r.matrix.forEach((row) => {
			row.forEach((d: any) => {
				if (!d.content) undefinedCells++;
			});
		});

		expect(r.matrix[0][0].content).to.not.eq(undefined);
		expect(r.matrix[1][1].content).to.not.eq(undefined);
		expect(r.matrix[2][2].content).to.not.eq(undefined);
		expect(undefinedCells).to.eq(6);
	});

	it('2D pivot with non-identity pattern', () => {
		const r = createMatrix2D(data3, ['fruit'], ['fruit']);
		expect(r.matrix.length).to.eq(3);

		let undefinedCells = 0;
		r.matrix.forEach((row) => {
			row.forEach((d: any) => {
				if (!d.content) undefinedCells++;
			});
		});
		expect(undefinedCells).to.eq(7);
	});

	it('2D pivot short form', () => {
		const r = createMatrix2D(data4, ['fruit'], ['fruit']);
		expect(r.matrix.length).to.eq(2);
		expect(r.matrix[0][0].content).to.not.eq(undefined);
		expect(r.matrix[1][1].content).to.not.eq(undefined);
	});
});
