import { assert, it, test } from 'vitest';
import { parsePetriNet2IGraph, PetriNet } from '@/petrinet/petrinet-service';

const SIRD: PetriNet = {
	T: [{ tname: 'inf' }, { tname: 'recover' }, { tname: 'death' }],
	S: [{ sname: 'S' }, { sname: 'I' }, { sname: 'R' }, { sname: 'D' }],
	I: [
		{ it: 1, is: 1 },
		{ it: 1, is: 2 },
		{ it: 2, is: 2 },
		{ it: 3, is: 2 }
	],
	O: [
		{ ot: 1, os: 2 },
		{ ot: 1, os: 2 },
		{ ot: 2, os: 3 },
		{ ot: 3, os: 4 }
	]
};
const expectedOutput = {
	width: 500,
	height: 500,
	nodes: [
		{
			id: 's-1',
			label: 'S',
			x: 35,
			y: 0,
			height: 20,
			width: 20,
			data: {
				type: 'S'
			},
			nodes: []
		},
		{
			id: 's-2',
			label: 'I',
			x: 35,
			y: 140,
			height: 20,
			width: 20,
			data: {
				type: 'S'
			},
			nodes: []
		},
		{
			id: 's-3',
			label: 'R',
			x: 0,
			y: 280,
			height: 20,
			width: 20,
			data: {
				type: 'S'
			},
			nodes: []
		},
		{
			id: 's-4',
			label: 'D',
			x: 70,
			y: 280,
			height: 20,
			width: 20,
			data: {
				type: 'S'
			},
			nodes: []
		},
		{
			id: 't-1',
			label: 'inf',
			x: 35,
			y: 70,
			height: 20,
			width: 20,
			data: {
				type: 'T'
			},
			nodes: []
		},
		{
			id: 't-2',
			label: 'recover',
			x: 0,
			y: 210,
			height: 20,
			width: 20,
			data: {
				type: 'T'
			},
			nodes: []
		},
		{
			id: 't-3',
			label: 'death',
			x: 70,
			y: 210,
			height: 20,
			width: 20,
			data: {
				type: 'T'
			},
			nodes: []
		}
	],
	edges: [
		{
			source: 's-1',
			target: 't-1',
			points: [
				{
					x: 45,
					y: 20
				},
				{
					x: 45,
					y: 45
				},
				{
					x: 45,
					y: 70
				}
			]
		},
		{
			source: 's-2',
			target: 't-1',
			points: [
				{
					x: 47.857142857142854,
					y: 140
				},
				{
					x: 55,
					y: 115
				},
				{
					x: 47.857142857142854,
					y: 90
				}
			]
		},
		{
			source: 's-2',
			target: 't-2',
			points: [
				{
					x: 35,
					y: 160
				},
				{
					x: 10,
					y: 185
				},
				{
					x: 10,
					y: 210
				}
			]
		},
		{
			source: 's-2',
			target: 't-3',
			points: [
				{
					x: 55,
					y: 160
				},
				{
					x: 80,
					y: 185
				},
				{
					x: 80,
					y: 210
				}
			]
		},
		{
			source: 't-1',
			target: 's-2',
			points: [
				{
					x: 42.142857142857146,
					y: 90
				},
				{
					x: 35,
					y: 115
				},
				{
					x: 42.142857142857146,
					y: 140
				}
			]
		},
		{
			source: 't-1',
			target: 's-2',
			points: [
				{
					x: 42.142857142857146,
					y: 90
				},
				{
					x: 35,
					y: 115
				},
				{
					x: 42.142857142857146,
					y: 140
				}
			]
		},
		{
			source: 't-2',
			target: 's-3',
			points: [
				{
					x: 10,
					y: 230
				},
				{
					x: 10,
					y: 255
				},
				{
					x: 10,
					y: 280
				}
			]
		},
		{
			source: 't-3',
			target: 's-4',
			points: [
				{
					x: 80,
					y: 230
				},
				{
					x: 80,
					y: 255
				},
				{
					x: 80,
					y: 280
				}
			]
		}
	]
};
test('fetchGraphResult', () => {
	it('Should provide correct graph', () => {
		assert.equal(parsePetriNet2IGraph(SIRD), expectedOutput);
	});
});
