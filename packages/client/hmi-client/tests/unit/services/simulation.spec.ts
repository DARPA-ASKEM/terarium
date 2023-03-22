import { parseSimulationPlan } from '@/services/simulation';
import { describe, expect, it } from 'vitest';

// box1 => box2 => box3
const plan = {
	InPort: [
		{ in_port_box: 2, in_port_type: '213' },
		{ in_port_box: 3, in_port_type: '213' }
	],
	OutPort: [
		{ out_port_box: 1, out_port_type: '213' },
		{ out_port_box: 2, out_port_type: '213' }
	],
	Wire: [
		{ src: 1, tgt: 1, wire_value: '123' },
		{ src: 2, tgt: 2, wire_value: '123' }
	],
	InWire: [],
	OutWire: [],
	OuterOutPort: [],
	OuterInPort: [],
	Box: [
		{ value: 'box1', box_type: '123' },
		{ value: 'box2', box_type: '123' },
		{ value: 'box3', box_type: '123' }
	],
	PassWire: null
};

describe('basic tests to make sure it all works', () => {
	it('test', () => {
		const graph = parseSimulationPlan(plan);
		expect(graph).deep.eq({
			nodes: [
				{ id: '0', name: 'box1', boxType: '123' },
				{ id: '1', name: 'box2', boxType: '123' },
				{ id: '2', name: 'box3', boxType: '123' }
			],
			edges: [
				{ source: '0', target: '1' },
				{ source: '1', target: '2' }
			]
		});
	});
});
