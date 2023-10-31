import * as amr from '@/examples/mira-petri.json';
import {
	getMiraAMRPresentationData,
	filterParameterLocations
} from '@/model-representation/petrinet/mira-petri';
import {
	createParameterMatrix,
	createTransitionMatrix
} from '@/model-representation/petrinet/petri-matrix-service';
import { describe, expect, it } from 'vitest';

describe('parameter and transition matrix tests', () => {
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
