import { describe, expect, it } from 'vitest';
import { petriNetValidator, PetriNet } from '@/petrinet/petrinet-service';

const falsePetriNets: PetriNet[] = [
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
	// More than one petri net - caught by check #3
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

const truePetriNets: PetriNet[] = [
	// Place -> <- Transition
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
	// Transition -> Place -> Transition
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
	},
	// Slightly larger petri net
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
	// "More complex" petri net - Must not be stopped by check #3
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

describe('test the petri net validator with a variety of graphs', () => {
	// Invalid petri nets
	it('should be invalid as there are no edges', () => {
		expect(petriNetValidator(falsePetriNets[0])).eq(
			'Invalid petri net: Requires at least one edge'
		);
	});

	it('should be invalid as a node is not recognized as a source or a target', () => {
		expect(petriNetValidator(falsePetriNets[1])).eq(
			'Invalid petri net: Every node should be at least either a source or a target'
		);
	});

	it('should be invalid as this has more than one petri net body', () => {
		expect(petriNetValidator(falsePetriNets[2])).eq(
			'Invalid petri net: There are multiple petri net bodies'
		);
	});

	// Valid petri nets
	it('should be valid (small petri net -  Place -> <- Transition)', () => {
		expect(petriNetValidator(truePetriNets[0])).eq(true);
	});

	it('should be valid (small petri net - Transition -> Place -> Transition)', () => {
		expect(petriNetValidator(truePetriNets[1])).eq(true);
	});

	it('should be valid (slightly larger)', () => {
		expect(petriNetValidator(truePetriNets[2])).eq(true);
	});

	it('should be valid ("complex" petri net - should not be stopped by check #3)', () => {
		expect(petriNetValidator(truePetriNets[3])).eq(true);
	});
});
