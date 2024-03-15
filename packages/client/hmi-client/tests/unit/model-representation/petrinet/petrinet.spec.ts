// Covers generic petrinet functions
import * as amr from '@/examples/mira-petri.json';
import * as simpleAMR from '@/examples/sir.json';
import { describe, expect, it } from 'vitest';
import { getStratificationType, newAMR } from '@/model-representation/petrinet/petrinet-service';

describe('petrinet general utilities test', () => {
	it('determine stratification type', () => {
		let stype = getStratificationType(amr as any);
		expect(stype).to.eq('mira');

		stype = getStratificationType(simpleAMR as any);
		expect(stype).to.eq(null);
	});

	it('new empty amr', () => {
		const res = newAMR('test123');
		expect(res.header.name).to.eq('test123');
		expect(res).toHaveProperty('model');
	});
});
