import { describe, expect, it } from 'vitest';
import { petrinetValidator, Petrinet } from '@/utils/petrinet-validator';

const falsePetrinets: Petrinet[] = [
	// No edges - caught by check #1.
	{
		T: [
			{
				tname: 't-1'
			}
		],
		S: [
			{
				sname: 'p-1'
			}
		],
		I: [],
		O: []
	},
	// A node is not recognized as a source or a target - caught by check #2
	{
		T: [
			{
				tname: 't-1'
			}
		],
		S: [
			{
				sname: 'p-1'
			},
			{
				sname: 'p-2'
			},
			{
				sname: 'p-3'
			}
		],
		I: [
			{
				it: 1,
				is: 1
			}
		],
		O: [
			{
				ot: 1,
				os: 2
			}
		]
	},
	// More than one petrinet - caught by check #5
	{
		T: [
			{
				tname: 't-1'
			},
			{
				tname: 't-2'
			}
		],
		S: [
			{
				sname: 'p-1'
			},
			{
				sname: 'p-2'
			},
			{
				sname: 'p-3'
			},
			{
				sname: 'p-4'
			}
		],
		I: [
			{
				it: 1,
				is: 1
			},
			{
				it: 2,
				is: 3
			}
		],
		O: [
			{
				ot: 1,
				os: 2
			},
			{
				ot: 2,
				os: 4
			}
		]
	}
];

const truePetrinets: Petrinet[] = [
	// Small petrinet
	{
		T: [
			{
				tname: 't-1'
			}
		],
		S: [
			{
				sname: 'p-1'
			}
		],
		I: [
			{
				it: 1,
				is: 1
			}
		],
		O: [
			{
				ot: 1,
				os: 1
			}
		]
	},
	// Medium petrinet
	{
		T: [
			{
				tname: 't-1'
			},
			{
				tname: 't-2'
			},
			{
				tname: 't-3'
			},
			{
				tname: 't-4'
			}
		],
		S: [
			{
				sname: 'p-1'
			},
			{
				sname: 'p-2'
			},
			{
				sname: 'p-3'
			},
			{
				sname: 'p-4'
			}
		],
		I: [
			{
				it: 1,
				is: 1
			},
			{
				it: 2,
				is: 2
			},
			{
				it: 3,
				is: 2
			},
			{
				it: 4,
				is: 3
			}
		],
		O: [
			{
				ot: 1,
				os: 2
			},
			{
				ot: 2,
				os: 1
			},
			{
				ot: 3,
				os: 3
			},
			{
				ot: 4,
				os: 4
			}
		]
	},
	// More complex petrinet - Must not be stopped by check #5
	{
		T: [
			{
				tname: 't-1'
			},
			{
				tname: 't-2'
			},
			{
				tname: 't-3'
			},
			{
				tname: 't-4'
			},
			{
				tname: 't-5'
			}
		],
		S: [
			{
				sname: 'p-1'
			},
			{
				sname: 'p-2'
			},
			{
				sname: 'p-3'
			},
			{
				sname: 'p-4'
			},
			{
				sname: 'p-5'
			},
			{
				sname: 'p-6'
			},
			{
				sname: 'p-7'
			}
		],
		I: [
			{
				it: 1,
				is: 1
			},
			{
				it: 2,
				is: 2
			},
			{
				it: 3,
				is: 1
			},
			{
				it: 3,
				is: 4
			},
			{
				it: 4,
				is: 6
			},
			{
				it: 5,
				is: 6
			}
		],
		O: [
			{
				ot: 1,
				os: 2
			},
			{
				ot: 2,
				os: 3
			},
			{
				ot: 3,
				os: 5
			},
			{
				ot: 4,
				os: 7
			},
			{
				ot: 5,
				os: 1
			}
		]
	}
];

// Caught by check #4 (or not if unbounded graphs are set to be accepted)
const unboundedPetrinet: Petrinet = {
	T: [
		{
			tname: 't-1'
		},
		{
			tname: 't-2'
		}
	],
	S: [
		{
			sname: 'p-1'
		}
	],
	I: [
		{
			it: 2,
			is: 1
		}
	],
	O: [
		{
			ot: 1,
			os: 1
		}
	]
};

describe('test the petrinet validator with a variety of graphs', () => {
	// Returns false
	it('should be invalid as there are no edges', () => {
		expect(petrinetValidator(falsePetrinets[0])).eq(false);
	});

	it('should be invalid as a nodeis not recognized as a source or a target', () => {
		expect(petrinetValidator(falsePetrinets[1])).eq(false);
	});

	it('should be invalid as this has more than one petrinet body', () => {
		expect(petrinetValidator(falsePetrinets[2])).eq(false);
	});

	// Returns true
	it('should be valid (small petrinet)', () => {
		expect(petrinetValidator(truePetrinets[0])).eq(true);
	});

	it('should be valid (medium petrinet)', () => {
		expect(petrinetValidator(truePetrinets[1])).eq(true);
	});

	it('should be valid (complex petrinet - should not be stopped by check #5)', () => {
		expect(petrinetValidator(truePetrinets[2])).eq(true);
	});

	// Bounded/unbounded checks
	it('should be invalid by default as petrinets are recognized as bounded by default', () => {
		expect(petrinetValidator(unboundedPetrinet)).eq(false);
	});

	it('should be valid as petrinet is set to unbounded', () => {
		expect(petrinetValidator(unboundedPetrinet, false)).eq(true);
	});
});
