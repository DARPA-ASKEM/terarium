import { expect, it, test } from 'vitest';
import { fetchStratificationResult } from '@/services/models/stratification-service';

test('fetchStratificationResult', () => {
	it('throws error  when not provided 3 modelIDs', () => {
		const modelA = '1';
		const modelB = '2';
		const typeModel = '';
		expect(fetchStratificationResult.bind(this, modelA, modelB, typeModel)).to.throw(
			`An ID must be provided for each model`
		);
	});
});
