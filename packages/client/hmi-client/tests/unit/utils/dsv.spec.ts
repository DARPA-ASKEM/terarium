import { dsvParse } from '@/utils/dsv';
import { describe, expect, it } from 'vitest';

const csvRaw = `
1,2,3
4,5,6
`;

const csvWithHeaderRaw = `
a,b,c
1,2,3
4,5,6
`;

const csvWithRowRaw = `
x,1,2,3
y,4,5,6
`;

const csvBothRaw = `
,a,b,c
x,1,2,3
y,4,5,6
`;

const csvSingleRaw = `
xyz,1,2,3,4,5
`;

describe('DSV  utilities', () => {
	it('parse CSV', () => {
		const result = dsvParse(csvRaw);
		expect(result.length).to.eq(6);
		expect(result[4].value).to.eq(5);
	});

	it('parse CSV with col header', () => {
		const result = dsvParse(csvWithHeaderRaw);
		expect(result.length).to.eq(6);
		expect(result[4].value).to.eq(5);
		expect(result[4].colLabel).to.eq('b');
	});

	it('parse CSV with row header', () => {
		const result = dsvParse(csvWithRowRaw);
		expect(result.length).to.eq(6);
		expect(result[4].value).to.eq(5);
		expect(result[4].rowLabel).to.eq('y');
	});

	it('parse CSV with both', () => {
		const result = dsvParse(csvBothRaw);
		expect(result.length).to.eq(6);
		expect(result[4].value).to.eq(5);
		expect(result[4].colLabel).to.eq('b');
		expect(result[4].rowLabel).to.eq('y');
	});

	it('parse CSV with single row', () => {
		const result = dsvParse(csvSingleRaw);
		expect(result[1].value).to.eq(2);
		expect(result[1].colLabel).to.toBeNull();
		expect(result[4].rowLabel).to.eq('xyz');
		console.log(result);
	});
});
