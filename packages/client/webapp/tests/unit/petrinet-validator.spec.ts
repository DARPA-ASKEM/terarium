import { describe, expect, it } from 'vitest';
import { petrinetValidator } from '@/utils/petrinet-validator';
import { IGraph } from 'graph-scaffolder';

const falseGraphs: IGraph[] = [
	// No edges - caught by check #1.
	{
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
	// A node is not recognized as a source or a target - caught by check #2
	{
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
	// Doesn't follow bipartite pattern (place -> place -> transition) - caught by check #3
	{
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
	},
	// More than one petrinet - caught by check #5
	{
		nodes: [
			{
				id: 'p-1',
				label: 'p-1',
				x: 60.67503543234125,
				y: 126.25294246492132,
				height: 50,
				width: 50,
				data: {
					type: 'species'
				},
				nodes: []
			},
			{
				id: 'p-2',
				label: 'p-2',
				x: 53.13222367263619,
				y: 237.06566045801452,
				height: 50,
				width: 50,
				data: {
					type: 'species'
				},
				nodes: []
			},
			{
				id: 'p-3',
				label: 'p-3',
				x: 63.688290815932646,
				y: 340.60175819844176,
				height: 50,
				width: 50,
				data: {
					type: 'species'
				},
				nodes: []
			},
			{
				id: 'p-4',
				label: 'p-4',
				x: 378.7902531873407,
				y: 215.39431028241899,
				height: 50,
				width: 50,
				data: {
					type: 'species'
				},
				nodes: []
			},
			{
				id: 'p-5',
				label: 'p-5',
				x: 144.32973665691807,
				y: 413.20837242854924,
				height: 50,
				width: 50,
				data: {
					type: 'species'
				},
				nodes: []
			},
			{
				id: 'p-6',
				label: 'p-6',
				x: 350.33017600133735,
				y: 342.4147151004979,
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
				x: 219.75296756084975,
				y: 120.70958886872572,
				height: 50,
				width: 50,
				data: {
					type: 'transition'
				},
				nodes: []
			},
			{
				id: 't-2',
				label: 't-2',
				x: 206.54255576608784,
				y: 247.3004822645882,
				height: 50,
				width: 50,
				data: {
					type: 'transition'
				},
				nodes: []
			},
			{
				id: 't-3',
				label: 't-3',
				x: 319.44631726843954,
				y: 65.9681721208084,
				height: 50,
				width: 50,
				data: {
					type: 'transition'
				},
				nodes: []
			},
			{
				id: 't-4',
				label: 't-4',
				x: 267.6705148230597,
				y: 403.78483535300114,
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
						x: 85.67503543234125,
						y: 151.25294246492132
					},
					{
						x: 244.75296756084975,
						y: 145.70958886872575
					}
				]
			},
			{
				source: 't-1',
				target: 'p-2',
				points: [
					{
						x: 244.75296756084975,
						y: 145.70958886872575
					},
					{
						x: 78.1322236726362,
						y: 262.0656604580145
					}
				]
			},
			{
				source: 'p-2',
				target: 't-2',
				points: [
					{
						x: 78.1322236726362,
						y: 262.0656604580145
					},
					{
						x: 231.54255576608784,
						y: 272.3004822645882
					}
				]
			},
			{
				source: 't-2',
				target: 'p-3',
				points: [
					{
						x: 231.54255576608784,
						y: 272.3004822645882
					},
					{
						x: 88.68829081593265,
						y: 365.60175819844176
					}
				]
			},
			{
				source: 'p-1',
				target: 't-3',
				points: [
					{
						x: 85.67503543234125,
						y: 151.25294246492132
					},
					{
						x: 344.44631726843954,
						y: 90.9681721208084
					}
				]
			},
			{
				source: 't-3',
				target: 'p-4',
				points: [
					{
						x: 344.44631726843954,
						y: 90.9681721208084
					},
					{
						x: 403.7902531873407,
						y: 240.39431028241899
					}
				]
			},
			{
				source: 'p-5',
				target: 't-4',
				points: [
					{
						x: 169.32973665691807,
						y: 438.20837242854924
					},
					{
						x: 292.6705148230597,
						y: 428.78483535300114
					}
				]
			},
			{
				source: 't-4',
				target: 'p-6',
				points: [
					{
						x: 292.6705148230597,
						y: 428.78483535300114
					},
					{
						x: 375.33017600133735,
						y: 367.4147151004979
					}
				]
			}
		]
	}
];

const trueGraphs: IGraph[] = [
	// Small petrinet
	{
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
	},
	// Medium petrinet
	{
		width: 500,
		height: 500,
		nodes: [
			{
				id: 'p-1',
				label: 'p-1',
				x: 156.3987992299838,
				y: 127.85776572856165,
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
				x: 136.802545176425,
				y: 268.89236554002144,
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
				x: 348.70277959715816,
				y: 271.0228815990105,
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
				x: 306.99338617050756,
				y: 167.10413914607528,
				height: 50,
				width: 50,
				data: {
					type: 'transition'
				},
				nodes: []
			},
			{
				id: 't-3',
				label: 't-3',
				x: 348.00474356833513,
				y: 414.55695580698944,
				height: 50,
				width: 50,
				data: {
					type: 'transition'
				},
				nodes: []
			},
			{
				id: 'p-3',
				label: 'p-3',
				x: 204.43796310283506,
				y: 396.0648581771284,
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
						x: 181.3987992299838,
						y: 152.85776572856165
					},
					{
						x: 161.802545176425,
						y: 293.89236554002144
					}
				]
			},
			{
				source: 't-1',
				target: 'p-2',
				points: [
					{
						x: 161.802545176425,
						y: 293.89236554002144
					},
					{
						x: 373.70277959715816,
						y: 296.0228815990105
					}
				]
			},
			{
				source: 'p-2',
				target: 't-2',
				points: [
					{
						x: 373.70277959715816,
						y: 296.0228815990105
					},
					{
						x: 331.99338617050756,
						y: 192.10413914607528
					}
				]
			},
			{
				source: 'p-2',
				target: 't-3',
				points: [
					{
						x: 373.70277959715816,
						y: 296.0228815990105
					},
					{
						x: 373.00474356833513,
						y: 439.55695580698944
					}
				]
			},
			{
				source: 't-2',
				target: 'p-1',
				points: [
					{
						x: 331.99338617050756,
						y: 192.10413914607528
					},
					{
						x: 181.3987992299838,
						y: 152.85776572856165
					}
				]
			},
			{
				source: 't-3',
				target: 'p-3',
				points: [
					{
						x: 373.00474356833513,
						y: 439.55695580698944
					},
					{
						x: 229.43796310283506,
						y: 421.0648581771284
					}
				]
			}
		]
	},
	// More complex petrinet - Must not be stopped by check #5
	{
		nodes: [
			{
				id: 'p-1',
				label: 'p-1',
				x: 60.67503543234125,
				y: 126.25294246492132,
				height: 50,
				width: 50,
				data: {
					type: 'species'
				},
				nodes: []
			},
			{
				id: 'p-2',
				label: 'p-2',
				x: 53.13222367263619,
				y: 237.06566045801452,
				height: 50,
				width: 50,
				data: {
					type: 'species'
				},
				nodes: []
			},
			{
				id: 'p-3',
				label: 'p-3',
				x: 63.688290815932646,
				y: 340.60175819844176,
				height: 50,
				width: 50,
				data: {
					type: 'species'
				},
				nodes: []
			},
			{
				id: 'p-4',
				label: 'p-4',
				x: 378.7902531873407,
				y: 215.39431028241899,
				height: 50,
				width: 50,
				data: {
					type: 'species'
				},
				nodes: []
			},
			{
				id: 'p-5',
				label: 'p-5',
				x: 144.32973665691807,
				y: 413.20837242854924,
				height: 50,
				width: 50,
				data: {
					type: 'species'
				},
				nodes: []
			},
			{
				id: 'p-6',
				label: 'p-6',
				x: 350.33017600133735,
				y: 342.4147151004979,
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
				x: 219.75296756084975,
				y: 120.70958886872572,
				height: 50,
				width: 50,
				data: {
					type: 'transition'
				},
				nodes: []
			},
			{
				id: 't-2',
				label: 't-2',
				x: 206.54255576608784,
				y: 247.3004822645882,
				height: 50,
				width: 50,
				data: {
					type: 'transition'
				},
				nodes: []
			},
			{
				id: 't-3',
				label: 't-3',
				x: 319.44631726843954,
				y: 65.9681721208084,
				height: 50,
				width: 50,
				data: {
					type: 'transition'
				},
				nodes: []
			},
			{
				id: 't-4',
				label: 't-4',
				x: 267.6705148230597,
				y: 403.78483535300114,
				height: 50,
				width: 50,
				data: {
					type: 'transition'
				},
				nodes: []
			},
			{
				id: 't-5',
				label: 't-5',
				x: 294.571389841297,
				y: 228.43336265738958,
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
						x: 85.67503543234125,
						y: 151.25294246492132
					},
					{
						x: 244.75296756084975,
						y: 145.70958886872575
					}
				]
			},
			{
				source: 't-1',
				target: 'p-2',
				points: [
					{
						x: 244.75296756084975,
						y: 145.70958886872575
					},
					{
						x: 78.1322236726362,
						y: 262.0656604580145
					}
				]
			},
			{
				source: 'p-2',
				target: 't-2',
				points: [
					{
						x: 78.1322236726362,
						y: 262.0656604580145
					},
					{
						x: 231.54255576608784,
						y: 272.3004822645882
					}
				]
			},
			{
				source: 't-2',
				target: 'p-3',
				points: [
					{
						x: 231.54255576608784,
						y: 272.3004822645882
					},
					{
						x: 88.68829081593265,
						y: 365.60175819844176
					}
				]
			},
			{
				source: 'p-1',
				target: 't-3',
				points: [
					{
						x: 85.67503543234125,
						y: 151.25294246492132
					},
					{
						x: 344.44631726843954,
						y: 90.9681721208084
					}
				]
			},
			{
				source: 't-3',
				target: 'p-4',
				points: [
					{
						x: 344.44631726843954,
						y: 90.9681721208084
					},
					{
						x: 403.7902531873407,
						y: 240.39431028241899
					}
				]
			},
			{
				source: 'p-5',
				target: 't-4',
				points: [
					{
						x: 169.32973665691807,
						y: 438.20837242854924
					},
					{
						x: 292.6705148230597,
						y: 428.78483535300114
					}
				]
			},
			{
				source: 't-4',
				target: 'p-6',
				points: [
					{
						x: 292.6705148230597,
						y: 428.78483535300114
					},
					{
						x: 375.33017600133735,
						y: 367.4147151004979
					}
				]
			},
			{
				source: 'p-6',
				target: 't-5',
				points: [
					{
						x: 375.33017600133735,
						y: 367.4147151004979
					},
					{
						x: 319.571389841297,
						y: 253.43336265738958
					}
				]
			},
			{
				source: 't-5',
				target: 'p-1',
				points: [
					{
						x: 319.571389841297,
						y: 253.43336265738958
					},
					{
						x: 85.67503543234125,
						y: 151.25294246492132
					}
				]
			}
		]
	}
];

// Caught by check #4 (or not if unbounded graphs are set to be accepted)
const unbounded: IGraph = {
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

	it('should be invalid as this has more than one petrinet body', () => {
		expect(petrinetValidator(falseGraphs[3])).eq(false);
	});

	// Returns true
	it('should be valid (small petrinet)', () => {
		expect(petrinetValidator(trueGraphs[0])).eq(true);
	});

	it('should be valid (medium petrinet)', () => {
		expect(petrinetValidator(trueGraphs[1])).eq(true);
	});

	it('should be valid (complex petrinet - should not be stopped by check #5)', () => {
		expect(petrinetValidator(trueGraphs[2])).eq(true);
	});

	// Bounded/unbounded checks
	it('should be invalid by default as petrinets are recognized as bounded by default', () => {
		expect(petrinetValidator(unbounded)).eq(false);
	});

	it('should be valid as petrinet is set to unbounded', () => {
		expect(petrinetValidator(unbounded, false)).eq(true);
	});
});
