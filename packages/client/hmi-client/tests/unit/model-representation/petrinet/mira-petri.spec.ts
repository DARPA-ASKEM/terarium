import * as amr from '@/examples/mira-petri.json';
import { describe, expect, it } from 'vitest';
import {
	getMiraAMRPresentationData,
	getUnstratifiedParameters
} from '@/model-representation/petrinet/mira-petri';

describe('mira petrinet ', () => {
	it('parse mira amr', () => {
		const res = getMiraAMRPresentationData(amr as any);

		// SEIRD * { y, m, o }  => 15
		// I{y, m, o} * { d, u } => 15 - 3 + 6
		expect(res.stateMatrixData.length).to.eq(18);
		expect(res.transitionMatrixData.length).to.eq(42);
	});

	it('compact graph', () => {
		const res = getMiraAMRPresentationData(amr as any);
		// SEIRD
		expect(res.compactModel.model.states.length).to.eq(5);
	});

	it('getting root parameters of parameters', () => {
		const res = getUnstratifiedParameters(amr as any);
		const originalSize = amr.semantics.ode.parameters.length;

		expect(res.size).to.lessThan(originalSize);
		expect(res.size).to.eq(11);
	});
});
