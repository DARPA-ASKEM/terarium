// Covers generic petrinet functions
import * as amr from '@/examples/mira-petri.json';
import * as simpleAMR from '@/examples/sir.json';
import { describe, expect, it } from 'vitest';
import {
	getStratificationType,
	convertToIGraph,
	newAMR
} from '@/model-representation/petrinet/petrinet-service';

describe('petrinet general utilities test', () => {
	it('determine stratification type', () => {
		let stype = getStratificationType(amr as any);
		expect(stype).to.eq('mira');

		stype = getStratificationType(simpleAMR as any);
		expect(stype).to.eq(null);
	});

	it('can convert simple AMR to render graph', () => {
		const g = convertToIGraph(simpleAMR as any);
		const gStates = g.nodes.filter((d) => d.type === 'state');
		const gTransitions = g.nodes.filter((d) => d.type === 'transition');
		expect(gStates.length).to.eq(3);
		expect(gTransitions.length).to.eq(2);
	});

	it('can convert stratified AMR to render graph', () => {
		const g = convertToIGraph(amr as any);
		const gStates = g.nodes.filter((d) => d.type === 'state');
		const gTransitions = g.nodes.filter((d) => d.type === 'transition');
		expect(gStates.length).to.eq(18);
		expect(gTransitions.length).to.eq(42);
	});

	it('new empty amr', () => {
		const res = newAMR('test123');
		expect(res.header.name).to.eq('test123');
		expect(res).toHaveProperty('model');
	});
});
