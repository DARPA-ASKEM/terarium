import { checkPetrinetAMR } from '@/model-representation/service';
import type { Model } from '@/types/Types';
import * as bad from '@/examples/bad.json';
import * as good from '@/examples/sir.json';
import { describe, expect, it } from 'vitest';

describe('model-representation service', () => {
	it('check petrinet bad AMR', () => {
		const result = checkPetrinetAMR(bad as unknown as Model);
		expect(result.length).to.eq(3);
	});

	it('check petrinet valid AMR', () => {
		const result = checkPetrinetAMR(good as unknown as Model);
		expect(result.length).to.eq(0);
	});
});
