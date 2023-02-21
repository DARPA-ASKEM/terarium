import { expect, it, test } from 'vitest';
import { fetchStratificationResult } from '@/services/models/stratification-service';
import { PetriNet } from '@/petrinet/petrinet-service';

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
