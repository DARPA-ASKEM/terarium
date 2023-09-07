import { describe, expect, it, test } from 'vitest';
import {
	fetchStratificationResult,
	generateAgeStrataModel,
	generateLocationStrataModel
} from '@/services/models/stratification-service';
import { PetriNet } from '@/petrinet/petrinet-service';

describe('test generate age strata model', () => {
	it(`with inputs 'Young,Old'`, () => {
		const stateNames = ['Young', 'Old'];
		const model = generateAgeStrataModel(stateNames);
		expect(model).toEqual({
			id: 'age-contact',
			header: {
				name: 'Age-contact strata model',
				description: 'Age-contact strata model',
				schema:
					'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.1/petrinet/petrinet_schema.json',
				schema_name: 'petrinet',
				model_version: '0.1'
			},
			model: {
				states: [
					{
						id: 'Young',
						name: 'Young',
						description:
							'Number of individuals relative to the total population that are in age group Young.',
						units: {
							expression: 'person',
							expression_mathml: '<ci>person</ci>'
						}
					},
					{
						id: 'Old',
						name: 'Old',
						description:
							'Number of individuals relative to the total population that are in age group Old.',
						units: {
							expression: 'person',
							expression_mathml: '<ci>person</ci>'
						}
					}
				],
				transitions: [
					{
						id: 'c11',
						input: ['Young', 'Young'],
						output: ['Young', 'Young'],
						properties: {
							name: 'c&#8321&#8321',
							description: 'Infective interaction between individuals.'
						}
					},
					{
						id: 'c12',
						input: ['Young', 'Old'],
						output: ['Young', 'Old'],
						properties: {
							name: 'c&#8321&#8322',
							description: 'Infective interaction between individuals.'
						}
					},
					{
						id: 'c21',
						input: ['Old', 'Young'],
						output: ['Old', 'Young'],
						properties: {
							name: 'c&#8322&#8321',
							description: 'Infective interaction between individuals.'
						}
					},
					{
						id: 'c22',
						input: ['Old', 'Old'],
						output: ['Old', 'Old'],
						properties: {
							name: 'c&#8322&#8322',
							description: 'Infective interaction between individuals.'
						}
					}
				]
			},
			semantics: {
				ode: {
					rates: [
						{
							target: 'c11',
							expression: 'Young*Young*beta11',
							expression_mathml:
								'<apply><times/><ci>Young</ci><ci>Young</ci><ci>beta11</ci></apply>'
						},
						{
							target: 'c12',
							expression: 'Young*Old*beta12',
							expression_mathml: '<apply><times/><ci>Young</ci><ci>Old</ci><ci>beta12</ci></apply>'
						},
						{
							target: 'c21',
							expression: 'Old*Young*beta21',
							expression_mathml: '<apply><times/><ci>Old</ci><ci>Young</ci><ci>beta21</ci></apply>'
						},
						{
							target: 'c22',
							expression: 'Old*Old*beta22',
							expression_mathml: '<apply><times/><ci>Old</ci><ci>Old</ci><ci>beta22</ci></apply>'
						}
					],
					initials: [
						{
							target: 'Young',
							expression: 'Younginit',
							expression_mathml: '<ci>Younginit</ci>'
						},
						{
							target: 'Old',
							expression: 'Oldinit',
							expression_mathml: '<ci>Oldinit</ci>'
						}
					],
					parameters: [
						{
							id: 'beta11',
							description: "infection rate from age group 'Young' to 'Young'",
							value: 0.25
						},
						{
							id: 'beta12',
							description: "infection rate from age group 'Young' to 'Old'",
							value: 0.25
						},
						{
							id: 'beta21',
							description: "infection rate from age group 'Old' to 'Young'",
							value: 0.25
						},
						{
							id: 'beta22',
							description: "infection rate from age group 'Old' to 'Old'",
							value: 0.25
						},
						{
							id: 'Younginit',
							description: "Proportion of population in age group 'Young' at timestep 0",
							value: 0.5
						},
						{
							id: 'Oldinit',
							description: "Proportion of population in age group 'Old' at timestep 0",
							value: 0.5
						}
					]
				},
				typing: {
					system: {
						name: 'Age-contact strata model',
						description: 'Age-contact strata model',
						schema:
							'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.1/petrinet/petrinet_schema.json',
						model_version: '0.1',
						model: {
							states: [
								{
									id: 'Pop',
									name: 'Pop',
									description: 'Compartment of individuals in a human population.'
								}
							],
							transitions: [
								{
									id: 'Infect',
									input: ['Pop', 'Pop'],
									output: ['Pop', 'Pop'],
									properties: {
										name: 'Infect',
										description:
											'2-to-2 interaction that represents infectious contact between two human individuals.'
									}
								}
							]
						}
					},
					map: [
						['Young', 'Pop'],
						['Old', 'Pop'],
						['c11', 'Infect'],
						['c12', 'Infect'],
						['c21', 'Infect'],
						['c22', 'Infect']
					]
				}
			},
			metadata: {
				processed_at: 0,
				processed_by: '',
				variable_statements: [],
				annotations: {},
				attributes: []
			}
		});
	});
});

describe('test generate location strata model', () => {
	it(`with inputs 'Toronto,Montreal'`, () => {
		const stateNames = ['Toronto', 'Montreal'];
		const model = generateLocationStrataModel(stateNames);
		expect(model).toEqual({
			id: 'location-travel',
			header: {
				name: 'Location-travel strata model',
				description: 'Location-travel strata model',
				schema:
					'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.1/petrinet/petrinet_schema.json',
				schema_name: 'petrinet',
				model_version: '0.1'
			},
			model: {
				states: [
					{
						id: 'Toronto',
						name: 'Toronto',
						description:
							'Number of individuals relative to the total population that are in location Toronto.',
						units: {
							expression: 'person',
							expression_mathml: '<ci>person</ci>'
						}
					},
					{
						id: 'Montreal',
						name: 'Montreal',
						description:
							'Number of individuals relative to the total population that are in location Montreal.',
						units: {
							expression: 'person',
							expression_mathml: '<ci>person</ci>'
						}
					}
				],
				transitions: [
					{
						id: 't12',
						input: ['Toronto'],
						output: ['Montreal'],
						properties: {
							name: 't&#8321&#8322',
							description: 'Travel of an individual from location Toronto and Montreal.'
						}
					},
					{
						id: 't21',
						input: ['Montreal'],
						output: ['Toronto'],
						properties: {
							name: 't&#8322&#8321',
							description: 'Travel of an individual from location Montreal and Toronto.'
						}
					}
				]
			},
			semantics: {
				ode: {
					rates: [
						{
							target: 't12',
							expression: 'Toronto*tau12',
							expression_mathml: '<apply><times/><ci>Toronto</ci><ci>tau12</ci></apply>'
						},
						{
							target: 't21',
							expression: 'Montreal*tau21',
							expression_mathml: '<apply><times/><ci>Montreal</ci><ci>tau21</ci></apply>'
						}
					],
					initials: [
						{
							target: 'Toronto',
							expression: 'Torontoinit',
							expression_mathml: '<ci>Torontoinit</ci>'
						},
						{
							target: 'Montreal',
							expression: 'Montrealinit',
							expression_mathml: '<ci>Montrealinit</ci>'
						}
					],
					parameters: [
						{
							id: 'tau12',
							description: "travel rate from location 'Toronto' to 'Montreal'",
							value: 0.5
						},
						{
							id: 'tau21',
							description: "travel rate from location 'Montreal' to 'Toronto'",
							value: 0.5
						},
						{
							id: 'Torontoinit',
							description: "Proportion of population in location 'Toronto' at timestep 0",
							value: 0.5
						},
						{
							id: 'Montrealinit',
							description: "Proportion of population in location 'Montreal' at timestep 0",
							value: 0.5
						}
					]
				},
				typing: {
					system: {
						name: 'Location-travel strata model',
						description: 'Location-travel strata model',
						schema:
							'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.1/petrinet/petrinet_schema.json',
						model_version: '0.1',
						model: {
							states: [
								{
									id: 'Pop',
									name: 'Pop',
									description: 'Compartment of individuals in a human population.'
								}
							],
							transitions: [
								{
									id: 'Strata',
									input: ['Pop'],
									output: ['Pop'],
									properties: {
										name: 'Strata',
										description:
											'1-to-1 process that represents a change in the demographic division of a human individual.'
									}
								}
							]
						}
					},
					map: [
						['Toronto', 'Pop'],
						['Montreal', 'Pop'],
						['t12', 'Strata'],
						['t21', 'Strata']
					]
				}
			},
			metadata: {
				processed_at: 0,
				processed_by: '',
				variable_statements: [],
				annotations: {},
				attributes: []
			}
		});
	});
});

// const SIRDModel: PetriNet = {
// 	T: [{ tname: 'inf' }, { tname: 'recover' }, { tname: 'death' }],
// 	S: [{ sname: 'S' }, { sname: 'I' }, { sname: 'R' }, { sname: 'D' }],
// 	I: [
// 		{ it: 1, is: 1 },
// 		{ it: 1, is: 2 },
// 		{ it: 2, is: 2 },
// 		{ it: 3, is: 2 }
// 	],
// 	O: [
// 		{ ot: 1, os: 2 },
// 		{ ot: 1, os: 2 },
// 		{ ot: 2, os: 3 },
// 		{ ot: 3, os: 4 }
// 	]
// };
// const QNotQModel: PetriNet = {
// 	T: [{ tname: 'quarantine' }, { tname: 'unquarantine' }],
// 	S: [{ sname: 'Q' }, { sname: 'NQ' }],
// 	I: [
// 		{ it: 1, is: 2 },
// 		{ it: 2, is: 1 }
// 	],
// 	O: [
// 		{ ot: 1, os: 1 },
// 		{ ot: 2, os: 2 }
// 	]
// };
// const typeModel: PetriNet = {
// 	T: [{ tname: 'infect' }, { tname: 'disease' }, { tname: 'strata' }],
// 	S: [{ sname: 'Pop' }],
// 	I: [
// 		{ it: 1, is: 1 },
// 		{ it: 1, is: 1 },
// 		{ it: 2, is: 1 },
// 		{ it: 3, is: 1 }
// 	],
// 	O: [
// 		{ ot: 1, os: 1 },
// 		{ ot: 1, os: 1 },
// 		{ ot: 2, os: 1 },
// 		{ ot: 3, os: 1 }
// 	]
// };

test('fetchStratificationResult', () => {
	it('throws error  when not provided 3 modelIDs', () => {
		const modelA = '1';
		const modelB = '2';
		const typeModel = '';
		expect(fetchStratificationResult.bind(this, modelA, modelB, typeModel)).to.throw(
			`An ID must be provided for each model`
		);
	});
	it('Correctly stratifys sample models', () => {
		// Create SIRD Model:
		fetch(`http://localhost:8888/api/models/1`, {
			method: 'POST',
			headers: {
				Accept: 'application/json',
				'Content-Type': 'application/json'
			},
			body: JSON.stringify({
				nodes: [
					{ name: 'inf', type: 'T' },
					{ name: 'recovers', type: 'T' },
					{ name: 'death', type: 'T' },
					{ name: 'S', type: 'S' },
					{ name: 'I', type: 'S' },
					{ name: 'R', type: 'S' },
					{ name: 'D', type: 'S' }
				],
				edges: [
					{ source: 'S', target: 'inf' },
					{ source: 'inf', target: 'I' },
					{ source: 'I', target: 'inf' },
					{ source: 'I', target: 'death' },
					{ source: 'I', target: 'recover' },
					{ source: 'recover', target: 'R' },
					{ source: 'recover', target: 'D' }
				]
			})
		});

		// Create QNQ Model:
		fetch(`http://localhost:8888/api/models/2`, {
			method: 'POST',
			headers: {
				Accept: 'application/json',
				'Content-Type': 'application/json'
			},
			body: JSON.stringify({
				nodes: [
					{ name: 'Q', type: 'S' },
					{ name: 'NQ', type: 'S' },
					{ name: 'quarantine', type: 'T' },
					{ name: 'unquarantine', type: 'T' }
				],
				edges: [
					{ source: 'Q', target: 'unquarantine' },
					{ source: 'unquarantine', target: 'NQ' },
					{ source: 'NQ', target: 'quarantine' },
					{ source: 'quarantine', target: 'Q' }
				]
			})
		});

		// Create Type Model:
		fetch(`http://localhost:8888/api/models/3`, {
			method: 'POST',
			headers: {
				Accept: 'application/json',
				'Content-Type': 'application/json'
			},
			body: JSON.stringify({
				nodes: [
					{ name: 'Pop', type: 'S' },
					{ name: 'infect', type: 'T' },
					{ name: 'disease', type: 'T' },
					{ name: 'strata', type: 'T' }
				],
				edges: [
					{ source: 'Pop', target: 'infect' },
					{ source: 'infect', target: 'Pop' },
					{ source: 'Pop', target: 'disease' },
					{ source: 'disease', target: 'Pop' },
					{ source: 'Pop', target: 'strata' },
					{ source: 'strata', target: 'Pop' }
				]
			})
		});

		const expectedResult: PetriNet = {
			T: [
				{ tname: 'strata_quarantine_1,' },
				{ tname: 'strata_quarantine_2,' },
				{ tname: 'strata_quarantine_3,' },
				{ tname: 'strata_unquarantine_4,' },
				{ tname: 'strata_unquarantine_5,' },
				{ tname: 'strata_unquarantine_6,' },
				{ tname: 'recover_disease_7,' },
				{ tname: 'death_disease_8,' },
				{ tname: 'recover_disease_9,' },
				{ tname: 'death_disease_10,' },
				{ tname: 'inf_infect_11,' }
			],
			S: [
				{ sname: 'S,Q' },
				{ sname: 'I,Q' },
				{ sname: 'R,Q' },
				{ sname: 'D,Q' },
				{ sname: 'S,NQ' },
				{ sname: 'I,NQ' },
				{ sname: 'R,NQ' },
				{ sname: 'D,NQ' }
			],
			I: [
				{ it: 1, is: 5 },
				{ it: 2, is: 6 },
				{ it: 3, is: 7 },
				{ it: 4, is: 1 },
				{ it: 5, is: 2 },
				{ it: 6, is: 3 },
				{ it: 7, is: 2 },
				{ it: 8, is: 2 },
				{ it: 9, is: 6 },
				{ it: 10, is: 6 },
				{ it: 11, is: 5 },
				{ it: 11, is: 6 }
			],
			O: [
				{ ot: 1, os: 1 },
				{ ot: 2, os: 2 },
				{ ot: 3, os: 3 },
				{ ot: 4, os: 5 },
				{ ot: 5, os: 6 },
				{ ot: 6, os: 7 },
				{ ot: 7, os: 3 },
				{ ot: 8, os: 4 },
				{ ot: 9, os: 7 },
				{ ot: 10, os: 8 },
				{ ot: 11, os: 6 },
				{ ot: 11, os: 6 }
			]
		};

		const SIRD_ID = '1';
		const QNQ_ID = '2';
		const typeModelID = '3';
		expect(fetchStratificationResult.bind(this, SIRD_ID, QNQ_ID, typeModelID)).to.equals(
			expectedResult
		);
	});
});
