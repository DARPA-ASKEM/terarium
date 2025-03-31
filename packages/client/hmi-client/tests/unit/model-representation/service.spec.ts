import { checkPetrinetAMR } from '@/model-representation/service';
import type { Model } from '@/types/Types';
import * as bad from '@/examples/bad.json';
import * as good from '@/examples/sir.json';
import { describe, expect, it } from 'vitest';

describe('model-representation service', () => {
	it.skip('check petrinet bad AMR', async () => {
		const result = await checkPetrinetAMR(bad as unknown as Model);
		expect(result.length).to.eq(3);
	});

	it.skip('check petrinet valid AMR', async () => {
		const result = await checkPetrinetAMR(good as unknown as Model);
		expect(result.length).to.eq(0);
	});
});
