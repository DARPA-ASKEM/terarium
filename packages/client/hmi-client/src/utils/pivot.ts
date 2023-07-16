// Create pivot table
import _ from 'lodash';

export interface PivotMatrixCell {
	row: number;
	col: number;
	rowCriteria: any;
	colCriteria: any;
	value: any;
}

// Helper function to expand the row and column terms into "lookup" axes
//
// For example: given a data array:
//   [ { a:1, b: 1}, { a: 2, b: 2 } ]
//
// If both row = [a, b] and col = [a]
//
//   rowAxis = [ { a: 1, b: 1 }, { a: 1, b: 2 }, { a: 2, b: 1 }, { a: 2, b: 2} ]
//   colAxis = [ { a: 1 }, { a: 2 } ]
//
const pivotAxes = (data: any[], rowDimensions: string[], colDimensions: string[]) => {
	const cardinality = new Map();
	for (let i = 0; i < data.length; i++) {
		const keys = Object.keys(data[i]);

		keys.forEach((key) => {
			let terms: any[] = [];
			if (cardinality.has(key)) {
				terms = cardinality.get(key);
			} else {
				terms = [];
			}

			if (!terms.includes(data[i][key])) {
				terms.push(data[i][key]);
			}
			cardinality.set(key, terms);
		});
	}

	// Expansion, basica this is a certesian product across the terms of the specified dimensions
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

	// Expansion, basica this is a certesian product across the terms of the specified dimensions
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
	return { colAxis, rowAxis };
};

// Creates a M x N matrix where
// M =  cardinality(rowDimensions[0]) * cardinality(rowDimensions[1]) * ... * cardinality(rowDimensions[m])
// N =  cardinality(colDimensions[0]) * cardinality(colDimensions[1]) * ... * cardinality(colDimensions[n])
export const createMatrix = (data: any[], rowDimensions: string[], colDimensions: string[]) => {
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
				value: null
			});
		}
		rows.push(row);
	}

	// Populate matrix
	// A cell has a non-null value if the row and col criteria matches
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
					if (d[rowKeys[i]] !== rowCriteria[rowKeys[i]]) {
						found = false;
						break;
					}
				}
				if (found === false) return found;

				for (let i = 0; i < colKeys.length; i++) {
					if (d[colKeys[i]] !== colCriteria[colKeys[i]]) {
						found = false;
						break;
					}
				}
				return found;
			});
			rows[rowIdx][colIdx].value = dataObj;
		}
	}

	return {
		matrix: rows,
		colDimensions,
		rowDimensions
	};
};
