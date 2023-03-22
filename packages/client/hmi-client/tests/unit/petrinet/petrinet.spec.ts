import { assert, it, describe } from 'vitest';
import { parsePetriNet2IGraph, parseIGraph2PetriNet } from '@/petrinet/petrinet-service';

const SIR = {
	S: [{ sname: 'Susceptible' }, { sname: 'Infected' }, { sname: 'Recovered' }],
	T: [{ tname: 't1' }, { tname: 't2' }],
	I: [
		{ is: 2, it: 1 },
		{ is: 1, it: 1 },
		{ is: 2, it: 2 }
	],
	O: [
		{ os: 2, ot: 1 },
		{ os: 2, ot: 1 },
		{ os: 3, ot: 2 }
	]
};

describe('fetchGraphResult', () => {
	it('Should provide correct graph', () => {
		const out = parsePetriNet2IGraph(SIR);
		assert.equal(out.edges.length, 5);
		assert.equal(out.nodes.length, 5);
	});

	it('Should convert graph to petri', () => {
		const outIGraph = parsePetriNet2IGraph(SIR);
		const out = parseIGraph2PetriNet(outIGraph);
		assert.equal(JSON.stringify(SIR), JSON.stringify(out));
	});
});
