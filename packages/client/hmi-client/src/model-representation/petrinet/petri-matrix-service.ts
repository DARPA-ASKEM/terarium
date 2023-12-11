// Creates transition and parameter matrices for stratified models
import _ from 'lodash';
import { Model } from '@/types/Types';

export interface MatrixCell {
	row: number;
	col: number;
	rowCriteria: any;
	colCriteria: any;
	content: any;
}

export const prepareMatrixMapping = (amr: Model, transitionMatrixData: any[]) => {
	let inputs: string[] = [];
	let outputs: string[] = [];

	const transitions = _.cloneDeep(
		transitionMatrixData.map((t) => amr.model.transitions.filter(({ id }) => t.id === id)).flat()
	);

	const controllerIndexMap = new Map(); // Maps controllers to their input/output combo

	// Get unique inputs and outputs and sort names alphabetically (these are the rows and columns respectively)
	for (let i = 0; i < transitions.length; i++) {
		const { input, output } = transitions[i];
		// Extract and remove controllers out of inputs array
		const newInputs: string[] = [];
		const newOutputs: string[] = [];
		const newControllers: string[] = [];
		for (let j = 0; j < input.length; j++) {
			if (input[j] !== output[j]) {
				newInputs.push(input[j]);
				newOutputs.push(output[j]);
			} else {
				newControllers.push(input[j]);
			}
		}

		if (!_.isEmpty(newControllers)) {
			// Map controllers unique to these input/output combos
			for (let j = 0; j < newInputs.length; j++) {
				controllerIndexMap.set(newInputs[j].concat('|', newOutputs[j]), newControllers);
			}
		}
		inputs.push(...newInputs);
		outputs.push(...newOutputs);
		// Update input/output for future transitions loop
		transitions[i].input = newInputs;
		transitions[i].output = newOutputs;
	}
	inputs = !_.isEmpty(inputs) ? [...new Set(inputs)].sort() : [''];
	outputs = !_.isEmpty(outputs) ? [...new Set(outputs)].sort() : [''];

	// Build empty matrix
	const rows: any[] = [];
	for (let rowIdx = 0; rowIdx < inputs.length; rowIdx++) {
		const row: MatrixCell[] = [];
		for (let colIdx = 0; colIdx < outputs.length; colIdx++) {
			row.push({
				row: rowIdx,
				col: colIdx,
				rowCriteria: inputs[rowIdx],
				colCriteria: outputs[colIdx],
				content: {
					value: null,
					id: '',
					// Insert controller(s) if they belong in this cell
					controllers: controllerIndexMap.get(inputs[rowIdx].concat('|', outputs[colIdx])) ?? null
				}
			});
		}
		rows.push(row);
	}

	// Map inputs/outputs to their row/col positions
	const rowIndexMap = new Map();
	const colIndexMap = new Map();
	for (let rowIdx = 0; rowIdx < inputs.length; rowIdx++) rowIndexMap.set(inputs[rowIdx], rowIdx);
	for (let colIdx = 0; colIdx < outputs.length; colIdx++) colIndexMap.set(outputs[colIdx], colIdx);

	return { transitions, rowIndexMap, colIndexMap, rows };
};

export function createTransitionMatrix(amr: Model, transitionMatrixData: any[]) {
	const { transitions, rowIndexMap, colIndexMap, rows } = prepareMatrixMapping(
		amr,
		transitionMatrixData
	);

	// For every transition id grab its input/output and row/column index to fill its place in the matrix
	for (let i = 0; i < transitions.length; i++) {
		const { input, output, id } = transitions[i];
		const rate = amr.semantics?.ode.rates.find((r) => r.target === id);

		if (rate) {
			// Go through inputs and outputs of the current transition id
			for (let j = 0; j < input.length; j++) {
				const rowIdx = rowIndexMap.get(input[j]);
				const colIdx = colIndexMap.get(output[j]);
				rows[rowIdx][colIdx].content.value = rate.expression;
				rows[rowIdx][colIdx].content.id = rate.target;
			}
		}
	}
	return { matrix: rows };
}

export function createParameterMatrix(
	amr: Model,
	transitionMatrixData: any[],
	childParameterIds: string[]
) {
	const { transitions, rowIndexMap, colIndexMap, rows } = prepareMatrixMapping(
		amr,
		transitionMatrixData
	);

	// For every transition id grab its input/output and row/column index to fill its place in the matrix
	for (let i = 0; i < transitions.length; i++) {
		const { input, output, id } = transitions[i];
		const rate = amr.semantics?.ode.rates.find((r) => r.target === id);

		if (rate) {
			// Go through inputs and outputs of the current transition id
			for (let j = 0; j < input.length; j++) {
				const rowIdx = rowIndexMap.get(input[j]);
				const colIdx = colIndexMap.get(output[j]);
				if (childParameterIds) {
					for (let k = 0; k < childParameterIds.length; k++) {
						// Fill cell content with parameter content
						if (rate.expression.includes(childParameterIds[k])) {
							const parameter = amr.semantics?.ode.parameters?.find(
								(p) => p.id === childParameterIds[k]
							);
							if (parameter) {
								rows[rowIdx][colIdx].content.value = parameter.value;
								rows[rowIdx][colIdx].content.id = parameter.id;
							}
							break;
						}
					}
				}
			}
		}
	}
	return { matrix: rows };
}
