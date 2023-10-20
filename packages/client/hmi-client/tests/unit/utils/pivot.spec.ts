import * as amr from '@/examples/mira-petri.json';
import {
	getMiraAMRPresentationData,
	filterParameterLocations
} from '@/model-representation/petrinet/mira-petri';
import { createMatrix2D, createParameterMatrix, createTransitionMatrix } from '@/utils/pivot';
import { describe, expect, it } from 'vitest';

const data: any[] = [
	{ fruit: ['apple'], location: ['L1'] },
	{ fruit: ['apple'], location: ['L2'] },
	{ fruit: ['orange'], location: ['L2'] },
	{ fruit: ['grape'], location: ['L1'] }
];

const data2: any[] = [
	{ fruit: ['apple', 'apple'] },
	{ fruit: ['pear', 'pear'] },
	{ fruit: ['orange', 'orange'] }
];

const data3: any[] = [{ fruit: ['apple', 'pear'] }, { fruit: ['apple', 'orange'] }];

const data4: any[] = [{ fruit: ['pear'] }, { fruit: ['orange'] }];

describe('pivot table tests', () => {
	it('square pivot table', () => {
		const r = createMatrix2D(data, ['fruit'], ['fruit']);

		expect(r.matrix.length).to.eq(3);
		expect(r.matrix[0].length).to.eq(3);
	});

	it('2D pivot with identity pattern', () => {
		const r = createMatrix2D(data2, ['fruit'], ['fruit']);
		expect(r.matrix.length).to.eq(3);

		let undefinedCells = 0;
		r.matrix.forEach((row) => {
			row.forEach((d: any) => {
				if (!d.content) undefinedCells++;
			});
		});

		expect(r.matrix[0][0].content).to.not.eq(undefined);
		expect(r.matrix[1][1].content).to.not.eq(undefined);
		expect(r.matrix[2][2].content).to.not.eq(undefined);
		expect(undefinedCells).to.eq(6);
	});

	it('2D pivot with non-identity pattern', () => {
		const r = createMatrix2D(data3, ['fruit'], ['fruit']);
		expect(r.matrix.length).to.eq(3);

		let undefinedCells = 0;
		r.matrix.forEach((row) => {
			row.forEach((d: any) => {
				if (!d.content) undefinedCells++;
			});
		});
		expect(undefinedCells).to.eq(7);
	});

	it('2D pivot short form', () => {
		const r = createMatrix2D(data4, ['fruit'], ['fruit']);
		expect(r.matrix.length).to.eq(2);
		expect(r.matrix[0][0].content).to.not.eq(undefined);
		expect(r.matrix[1][1].content).to.not.eq(undefined);
	});

	it('create parameter table with one child parameter', () => {
		const childParameterIds = ['epsilon'];
		const matrixData = filterParameterLocations(
			amr as any,
			getMiraAMRPresentationData(amr as any).transitionMatrixData,
			childParameterIds
		);
		const { matrix } = createParameterMatrix(amr as any, matrixData, childParameterIds);
		expect(matrix.length).to.eq(3);
		expect(matrix[0].length).to.eq(3);
		expect(matrix[0][0].content.value).to.not.eq(null);
		expect(matrix[1][1].content.value).to.not.eq(null);
		expect(matrix[2][2].content.value).to.not.eq(null);
		expect(matrix[0][0].content.controllers).to.eq(null);
		expect(matrix[1][1].content.controllers).to.eq(null);
		expect(matrix[2][2].content.controllers).to.eq(null);
	});

	it('create parameter table with multiple child parameters', () => {
		const childParameterIds = ['beta_c', 'beta_nc', 'beta_s'];
		const matrixData = filterParameterLocations(
			amr as any,
			getMiraAMRPresentationData(amr as any).transitionMatrixData,
			childParameterIds
		);
		const { matrix } = createParameterMatrix(amr as any, matrixData, childParameterIds);
		expect(matrix.length).to.eq(3);
		expect(matrix[0].length).to.eq(3);
		expect(matrix[0][0].content.value).to.not.eq(null);
		expect(matrix[1][1].content.value).to.not.eq(null);
		expect(matrix[2][2].content.value).to.not.eq(null);
		expect(matrix[0][0].content.controllers).to.not.eq(null);
		expect(matrix[1][1].content.controllers).to.not.eq(null);
		expect(matrix[2][2].content.controllers).to.not.eq(null);
	});

	it('create transition table without controller', () => {
		const matrixData = getMiraAMRPresentationData(amr as any).transitionMatrixData.filter(
			({ base }) => base === 'T1'
		);
		const { matrix } = createTransitionMatrix(amr as any, matrixData);
		expect(matrix.length).to.eq(3);
		expect(matrix[0].length).to.eq(6);
		expect(matrix[0][0].content.value).to.not.eq(null);
		expect(matrix[0][1].content.value).to.not.eq(null);
		expect(matrix[1][2].content.value).to.not.eq(null);
		expect(matrix[1][3].content.value).to.not.eq(null);
		expect(matrix[2][4].content.value).to.not.eq(null);
		expect(matrix[2][5].content.value).to.not.eq(null);
	});

	it('create transition table with controller', () => {
		const matrixData = getMiraAMRPresentationData(amr as any).transitionMatrixData.filter(
			({ base }) => base === 'T0'
		);
		const { matrix } = createTransitionMatrix(amr as any, matrixData);
		expect(matrix.length).to.eq(3);
		expect(matrix[0].length).to.eq(3);
		expect(matrix[0][0].content.value).to.not.eq(null);
		expect(matrix[1][1].content.value).to.not.eq(null);
		expect(matrix[2][2].content.value).to.not.eq(null);
		expect(matrix[0][0].content.controllers).to.not.eq(null);
		expect(matrix[1][1].content.controllers).to.not.eq(null);
		expect(matrix[2][2].content.controllers).to.not.eq(null);
	});
});
