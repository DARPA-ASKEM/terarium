import { describe, expect, it } from 'vitest';
import { petrinetValidator } from '@/utils/petrinet-validator';
import { IGraph } from 'graph-scaffolder';

const falseGraphs: IGraph[] = [
	{
		// No edges - caught by check #1.
		nodes: [
			{
				id: 'p-1',
				label: 'p-1',
				x: 170.05545025778872,
				y: 250.45248542460433,
				height: 50,
				width: 50,
				data: {
					type: 'species'
				},
				nodes: []
			},
			{
				id: 't-1',
				label: 't-1',
				x: 339.9656724130901,
				y: 326.15760027211405,
				height: 50,
				width: 50,
				data: {
					type: 'transition'
				},
				nodes: []
			}
		],
		edges: []
	},
	{
		// A node is not recognized as a source or a target - caught by check #2
		nodes: [
			{
				id: 'p-1',
				label: 'p-1',
				x: 332.8005286299471,
				y: 147.0478802228028,
				height: 50,
				width: 50,
				data: {
					type: 'species'
				},
				nodes: []
			},
			{
				id: 't-1',
				label: 't-1',
				x: 96.92963584744075,
				y: 249.31697305720368,
				height: 50,
				width: 50,
				data: {
					type: 'transition'
				},
				nodes: []
			},
			{
				id: 'p-2',
				label: 'p-2',
				x: 298.25726788450913,
				y: 297.4634691129239,
				height: 50,
				width: 50,
				data: {
					type: 'species'
				},
				nodes: []
			}
		],
		edges: [
			{
				source: 'p-1',
				target: 't-1',
				points: [
					{
						x: 357.8005286299471,
						y: 172.0478802228028
					},
					{
						x: 121.92963584744075,
						y: 274.31697305720365
					}
				]
			},
			{
				source: 't-1',
				target: 'p-1',
				points: [
					{
						x: 121.92963584744075,
						y: 274.31697305720365
					},
					{
						x: 357.8005286299471,
						y: 172.0478802228028
					}
				]
			}
		]
	},
	{
		// Doesn't follow bipartite pattern (place -> place -> transition) - caught by check #3
		nodes: [
			{
				id: 'p-1',
				label: 'p-1',
				x: 239.91407197447495,
				y: 336.8917522793606,
				height: 50,
				width: 50,
				data: {
					type: 'species'
				},
				nodes: []
			},
			{
				id: 't-1',
				label: 't-1',
				x: 370.7601958348405,
				y: 162.42420695618054,
				height: 50,
				width: 50,
				data: {
					type: 'transition'
				},
				nodes: []
			},
			{
				id: 'p-2',
				label: 'p-2',
				x: 41.69384821557887,
				y: 57.259704024531175,
				height: 50,
				width: 50,
				data: {
					type: 'species'
				},
				nodes: []
			}
		],
		edges: [
			{
				source: 'p-1',
				target: 'p-2',
				points: [
					{
						x: 264.91407197447495,
						y: 361.8917522793606
					},
					{
						x: 66.69384821557887,
						y: 82.25970402453117
					}
				]
			},
			{
				source: 'p-2',
				target: 't-1',
				points: [
					{
						x: 66.69384821557887,
						y: 82.25970402453117
					},
					{
						x: 395.7601958348405,
						y: 187.42420695618054
					}
				]
			}
		]
	}
];

const trueGraphs: IGraph[] = [
	{
		// Small petrinet
		nodes: [
			{
				id: 'p-1',
				label: 'p-1',
				x: 170.05545025778872,
				y: 250.45248542460433,
				height: 50,
				width: 50,
				data: {
					type: 'species'
				},
				nodes: []
			},
			{
				id: 't-1',
				label: 't-1',
				x: 339.9656724130901,
				y: 326.15760027211405,
				height: 50,
				width: 50,
				data: {
					type: 'transition'
				},
				nodes: []
			}
		],
		edges: [
			{
				source: 'p-1',
				target: 't-1',
				points: [
					{
						x: 195.05545025778872,
						y: 275.45248542460433
					},
					{
						x: 364.9656724130901,
						y: 351.15760027211405
					}
				]
			},
			{
				source: 't-1',
				target: 'p-1',
				points: [
					{
						x: 364.9656724130901,
						y: 351.15760027211405
					},
					{
						x: 195.05545025778872,
						y: 275.45248542460433
					}
				]
			}
		]
	}
];

const unbounded: IGraph = {
	// Caught by check #4 (or not if unbounded graphs are set to be accepted)
	nodes: [
		{
			id: 't-1',
			label: 't-1',
			x: 366.3974053737369,
			y: 290.84623748582527,
			height: 50,
			width: 50,
			data: {
				type: 'transition'
			},
			nodes: []
		},
		{
			id: 'p-1',
			label: 'p-1',
			x: 202.64657098455228,
			y: 304.2128530639816,
			height: 50,
			width: 50,
			data: {
				type: 'species'
			},
			nodes: []
		},
		{
			id: 't-2',
			label: 't-2',
			x: 139.02354564325094,
			y: 191.1284910117791,
			height: 50,
			width: 50,
			data: {
				type: 'transition'
			},
			nodes: []
		}
	],
	edges: [
		{
			source: 't-1',
			target: 'p-1',
			points: [
				{
					x: 391.3974053737369,
					y: 315.84623748582527
				},
				{
					x: 227.64657098455228,
					y: 329.2128530639816
				}
			]
		},
		{
			source: 'p-1',
			target: 't-2',
			points: [
				{
					x: 227.64657098455228,
					y: 329.2128530639816
				},
				{
					x: 164.02354564325094,
					y: 216.1284910117791
				}
			]
		}
	]
};

describe('test the petrinet validator with a variety of graphs', () => {
	// Returns false
	it('should be invalid as there are no edges', () => {
		expect(petrinetValidator(falseGraphs[0])).eq(false);
	});

	it('should be invalid as a nodeis not recognized as a source or a target', () => {
		expect(petrinetValidator(falseGraphs[1])).eq(false);
	});

	it('should be invalid as this doesnt follow the bipartite pattern', () => {
		expect(petrinetValidator(falseGraphs[2])).eq(false);
	});

	// Returns true
	it('should be valid', () => {
		expect(petrinetValidator(trueGraphs[0])).eq(true);
	});

	// Bounded/unbounded checks
	it('should be invalid by default as petrinets are recognized as bounded by default', () => {
		expect(petrinetValidator(unbounded)).eq(false);
	});

	it('should be valid as petrinet is set to unbounded', () => {
		expect(petrinetValidator(unbounded, false)).eq(true);
	});
});
