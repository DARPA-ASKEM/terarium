import { MiraTemplate } from './mira-common';

export const removeModifiers = (v: string, context: { [key: string]: string }) => {
	let result = v;
	const contextKeys = Object.keys(context);
	contextKeys.forEach((key) => {
		result = result.replace(`_${context[key]}`, '');
	});
	return result;
};

export const extractConceptNames = (templates: MiraTemplate[], key: string) => {
	const names = templates.map((t) => (t[key] ? t[key].name : ''));
	const uniqueNames = [...new Set(names)];
	uniqueNames.sort();
	return uniqueNames;
};

const emptyMatrix = (rowNames: string[], colNames: string[]) => {
	const matrix: any[] = [];
	for (let rowIdx = 0; rowIdx < rowNames.length; rowIdx++) {
		const row: any[] = [];
		for (let colIdx = 0; colIdx < colNames.length; colIdx++) {
			row.push({
				value: null,
				id: null
			});
		}
		matrix.push(row);
	}
	return matrix;
};

export const extractSubjectOutcomeMatrix = (
	templates: MiraTemplate[],
	paramNames: string[],
	paramValueMap: Map<string, any>,
	paramLocationMap: Map<string, any>
) => {
	const rowNames = extractConceptNames(templates, 'subject');
	const colNames = extractConceptNames(templates, 'outcome');
	const matrix = emptyMatrix(rowNames, colNames);

	for (let i = 0; i < paramNames.length; i++) {
		const paramName = paramNames[i];
		const paramValue = paramValueMap.get(paramName);
		const paramLocations = paramLocationMap.get(paramName);
		if (!paramLocationMap) continue;

		paramLocations.forEach((location) => {
			const rowIdx = rowNames.indexOf(location.subject);
			const colIdx = colNames.indexOf(location.outcome);

			matrix[rowIdx][colIdx].value = paramValue;
			matrix[rowIdx][colIdx].id = paramName;
		});
	}
	return { rowNames, colNames, matrix };
};

export const extractSubjectControllerMatrix = (
	templates: MiraTemplate[],
	paramNames: string[],
	paramValueMap: Map<string, any>,
	paramLocationMap: Map<string, any>
) => {
	const rowNames = extractConceptNames(templates, 'subject');
	const colNames = extractConceptNames(templates, 'controller');
	const matrix = emptyMatrix(rowNames, colNames);

	for (let i = 0; i < paramNames.length; i++) {
		const paramName = paramNames[i];
		const paramValue = paramValueMap.get(paramName);
		const paramLocations = paramLocationMap.get(paramName);
		if (!paramLocationMap) continue;

		paramLocations.forEach((location) => {
			const rowIdx = rowNames.indexOf(location.subject);
			const colIdx = colNames.indexOf(location.controllers[0]); // FIXME: may need to process array

			matrix[rowIdx][colIdx].value = paramValue;
			matrix[rowIdx][colIdx].id = paramName;
		});
	}
	return { rowNames, colNames, matrix };
};
