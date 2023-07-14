// Create pivot table
import _ from 'lodash';

export interface PivotMatrixCell {
	row: number;
	col: number;
	rowCriteria: any;
	colCriteria: any;
	value: any;
}

export const pivotAxes = (data: any[], xDimensions: string[], yDimensions: string[]) => {
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

	let yAxis: any[] = [];
	yDimensions.forEach((key) => {
		const terms = cardinality.get(key);
		if (yAxis.length === 0) {
			yAxis = terms.map((d) => ({ [key]: d }));
		} else {
			const temp = yAxis;
			yAxis = [];
			for (let i1 = 0; i1 < temp.length; i1++) {
				for (let i2 = 0; i2 < terms.length; i2++) {
					const obj = _.cloneDeep(temp[i1]);
					obj[key] = terms[i2];
					yAxis.push(obj);
				}
			}
		}
	});

	let xAxis: any[] = [];
	xDimensions.forEach((key) => {
		const terms = cardinality.get(key);
		if (xAxis.length === 0) {
			xAxis = terms.map((d) => ({ [key]: d }));
		} else {
			const temp = xAxis;
			xAxis = [];
			for (let i1 = 0; i1 < temp.length; i1++) {
				for (let i2 = 0; i2 < terms.length; i2++) {
					const obj = _.cloneDeep(temp[i1]);
					obj[key] = terms[i2];
					xAxis.push(obj);
				}
			}
		}
	});
	return { xAxis, yAxis };
};

export const createMatrix = (data: any[], xDimensions: string[], yDimensions: string[]) => {
	const axes = pivotAxes(data, xDimensions, yDimensions);
	const rows: any[] = [];

	// Bootstrap the matrix
	for (let y = 0; y < axes.yAxis.length; y++) {
		const row: PivotMatrixCell[] = [];
		for (let x = 0; x < axes.xAxis.length; x++) {
			row.push({
				row: y,
				col: x,
				rowCriteria: axes.yAxis[y],
				colCriteria: axes.xAxis[x],
				value: null
			});
		}
		rows.push(row);
	}

	// Populate matrix
	for (let y = 0; y < axes.yAxis.length; y++) {
		for (let x = 0; x < axes.xAxis.length; x++) {
			const obj = rows[y][x];

			const dataObj = data.find((d) => {
				const rowCriteria = obj.rowCriteria;
				const colCriteria = obj.colCriteria;
				let found = true;
				const rowKeys = Object.keys(rowCriteria);
				const colKeys = Object.keys(colCriteria);

				for (let i = 0; i < rowKeys.length; i++) {
					if (d[rowKeys[i]] !== rowCriteria[rowKeys[i]]) {
						found = false;
					}
				}

				for (let i = 0; i < colKeys.length; i++) {
					if (d[colKeys[i]] !== colCriteria[colKeys[i]]) {
						found = false;
					}
				}
				return found;
			});
			rows[y][x].value = dataObj;
		}
	}
	return rows;
};
