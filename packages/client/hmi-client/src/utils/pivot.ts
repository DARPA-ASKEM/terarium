// Create pivot table
import _ from 'lodash';
import { Model } from '@/types/Types';

export interface PivotMatrixCell {
	row: number;
	col: number;
	rowCriteria: any;
	colCriteria: any;
	content: any;
}

// Helper function to expand the row and column terms into "lookup" axes
//
// For example: given a data array:
//   [ { a:[1], b: [1]}, { a: [2], b: [2] } ]
//
// If rowDimension = [a, b] and colDimension = [a]
//
//   rowAxis = [ { a: 1, b: 1 }, { a: 1, b: 2 }, { a: 2, b: 1 }, { a: 2, b: 2} ]
//   colAxis = [ { a: 1 }, { a: 2 } ]
//
const pivotAxes = (data: any[], rowDimensions: string[], colDimensions: string[]) => {
	const cardinality = new Map();
	for (let i = 0; i < data.length; i++) {
		const keys = Object.keys(data[i]);

		keys.forEach((key) => {
			if (!_.isArray(data[i][key])) return;

			let terms: any[] = [];
			if (cardinality.has(key)) {
				terms = cardinality.get(key);
			} else {
				terms = [];
			}

			data[i][key].forEach((term: any) => {
				if (!terms.includes(term)) {
					terms.push(term);
				}
			});

			cardinality.set(key, terms);
		});
	}

	// Expansion, basically this is a cartesian product across the terms of the specified dimensions
	let rowAxis: any[] = [];
	rowDimensions.forEach((key) => {
		const terms = cardinality.get(key);
		if (rowAxis.length === 0) {
			rowAxis = terms.map((d: any) => ({ [key]: d }));
		} else {
			const temp = rowAxis;
			rowAxis = [];
			for (let i1 = 0; i1 < temp.length; i1++) {
				for (let i2 = 0; i2 < terms.length; i2++) {
					const obj = _.cloneDeep(temp[i1]);
					obj[key] = terms[i2];
					rowAxis.push(obj);
				}
			}
		}
	});

	// Expansion, basically this is a cartesian product across the terms of the specified dimensions
	let colAxis: any[] = [];
	colDimensions.forEach((key) => {
		const terms = cardinality.get(key);
		if (colAxis.length === 0) {
			colAxis = terms.map((d: any) => ({ [key]: d }));
		} else {
			const temp = colAxis;
			colAxis = [];
			for (let i1 = 0; i1 < temp.length; i1++) {
				for (let i2 = 0; i2 < terms.length; i2++) {
					const obj = _.cloneDeep(temp[i1]);
					obj[key] = terms[i2];
					colAxis.push(obj);
				}
			}
		}
	});
	return { colAxis, rowAxis, termsMap: cardinality };
};

// Creates a M x 1 matrix where
// M =  cardinality(rowDimensions[0]) * cardinality(rowDimensions[1]) * ... * cardinality(rowDimensions[m])
export const createMatrix1D = (data: any[]) => {
	const rows: any[] = [];

	// Construct 1D matrix for state data
	for (let rowIdx = 0; rowIdx < data.length; rowIdx++) {
		const row: PivotMatrixCell[] = [];
		row.push({
			row: rowIdx,
			col: 0,
			rowCriteria: data[rowIdx],
			colCriteria: null,
			content: data[rowIdx]
		});
		rows.push(row);
	}

	return { matrix: rows };
};

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
		const row: PivotMatrixCell[] = [];
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

// Creates a M x N matrix where
// M =  cardinality(rowDimensions[0]) * cardinality(rowDimensions[1]) * ... * cardinality(rowDimensions[m])
// N =  cardinality(colDimensions[0]) * cardinality(colDimensions[1]) * ... * cardinality(colDimensions[n])
export const createMatrix2D = (data: any[], rowDimensions: string[], colDimensions: string[]) => {
	const axes = pivotAxes(data, colDimensions, rowDimensions);
	const rows: any[] = [];

	// Bootstrap the matrix structure and set the row and col criteria
	for (let rowIdx = 0; rowIdx < axes.rowAxis.length; rowIdx++) {
		const row: PivotMatrixCell[] = [];
		for (let colIdx = 0; colIdx < axes.colAxis.length; colIdx++) {
			row.push({
				row: rowIdx,
				col: colIdx,
				rowCriteria: axes.rowAxis[rowIdx],
				colCriteria: axes.colAxis[colIdx],
				content: null
			});
		}
		rows.push(row);
	}

	// Populate matrix
	// A cell has a non-null content if the row and col criteria matches
	// with an element in the data array
	for (let rowIdx = 0; rowIdx < axes.rowAxis.length; rowIdx++) {
		for (let colIdx = 0; colIdx < axes.colAxis.length; colIdx++) {
			const obj = rows[rowIdx][colIdx];

			const dataObj = data.find((d) => {
				const rowCriteria = obj.rowCriteria;
				const colCriteria = obj.colCriteria;
				let found = true;
				const rowKeys = Object.keys(rowCriteria);
				const colKeys = Object.keys(colCriteria);

				for (let i = 0; i < rowKeys.length; i++) {
					if (_.first(d[rowKeys[i]]) !== rowCriteria[rowKeys[i]]) {
						found = false;
						break;
					}
				}
				if (found === false) return found;

				for (let i = 0; i < colKeys.length; i++) {
					if (_.last(d[colKeys[i]]) !== colCriteria[colKeys[i]]) {
						found = false;
						break;
					}
				}
				return found;
			});
			rows[rowIdx][colIdx].content = dataObj;
		}
	}

	return {
		matrix: rows,
		termsMap: axes.termsMap,
		colDimensions,
		rowDimensions
	};
};
