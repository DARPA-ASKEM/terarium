import { MiraTemplate, MiraMatrix, TemplateSummary, MiraMatrixEntry } from './mira-common';

export const getControllerNames = (t: MiraTemplate) => {
	if (t.controllers) {
		return t.controllers.map((d) => d.name);
	}
	if (t.controller) {
		return [t.controller.name];
	}
	return [];
};

export const removeModifiers = (v: string, context: { [key: string]: string }, scrubbingKeys: string[]) => {
	let result = v;
	scrubbingKeys.forEach((key) => {
		result = result.replace(`_${context[key]}`, '');
	});
	return result;
};

export const removeModifiersByValues = (v: string, values: string[]) => {
	let result = v;
	values.forEach((key) => {
		result = result.replace(`_${key}`, '');
	});
	return result;
};

export const extractConceptNames = (templates: MiraTemplate[], key: string) => {
	let names: string[] = [];
	if (key === 'subject') {
		names = templates.map((t) => (t.subject ? t.subject.name : ''));
	}
	if (key === 'outcome') {
		names = templates.map((t) => (t.outcome ? t.outcome.name : ''));
	}
	if (key === 'controllers') {
		templates.forEach((template) => {
			const controllerNames = getControllerNames(template).sort();
			if (controllerNames.length > 0) {
				names.push(controllerNames.join('-'));
			}
		});
	}
	names = names.filter((v) => v !== '');

	const uniqueNames = [...new Set(names)];
	uniqueNames.sort();
	return uniqueNames;
};

// Empty matrix placeholder
const emptyMatrix = (rowNames: string[], colNames: string[]) => {
	const matrix: MiraMatrix = [];
	for (let rowIdx = 0; rowIdx < rowNames.length; rowIdx++) {
		const row: MiraMatrixEntry[] = [];
		for (let colIdx = 0; colIdx < colNames.length; colIdx++) {
			row.push({
				row: rowIdx,
				col: colIdx,
				rowCriteria: rowNames[rowIdx],
				colCriteria: colNames[colIdx],
				content: { id: null, value: null }
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
	if (rowNames.length === 0 || colNames.length === 0) {
		return { rowNames, colNames, matrix: [] };
	}
	const matrix = emptyMatrix(rowNames, colNames);

	for (let i = 0; i < paramNames.length; i++) {
		const paramName = paramNames[i];
		const paramValue = paramValueMap.get(paramName);
		const paramLocations = paramLocationMap.get(paramName);
		if (!paramLocations) continue;

		paramLocations.forEach((location) => {
			const rowIdx = rowNames.indexOf(location.subject);
			const colIdx = colNames.indexOf(location.outcome);

			if (rowIdx === -1 || colIdx === -1) return;

			matrix[rowIdx][colIdx].rowCriteria = location.subject;
			matrix[rowIdx][colIdx].colCriteria = location.outcome;
			matrix[rowIdx][colIdx].content = {
				value: paramValue,
				id: paramName
			};
		});
	}
	return { rowNames, colNames, matrix };
};

export const extractSubjectControllersMatrix = (
	templates: MiraTemplate[],
	paramNames: string[],
	paramValueMap: Map<string, any>,
	paramLocationMap: Map<string, TemplateSummary[]>
) => {
	const rowNames = extractConceptNames(templates, 'subject');
	const colNames = extractConceptNames(templates, 'controllers');
	if (rowNames.length === 0 || colNames.length === 0) {
		return { rowNames, colNames, matrix: [] };
	}

	const matrix = emptyMatrix(rowNames, colNames);

	for (let i = 0; i < paramNames.length; i++) {
		const paramName = paramNames[i];
		const paramValue = paramValueMap.get(paramName);
		const paramLocations = paramLocationMap.get(paramName);
		if (!paramLocationMap) continue;
		if (!paramLocations) continue;

		paramLocations.forEach((location) => {
			const rowIdx = rowNames.indexOf(location.subject);
			const colIdx = colNames.indexOf(location.controllers.join('-'));

			if (rowIdx === -1 || colIdx === -1) return;

			matrix[rowIdx][colIdx].rowCriteria = location.subject;
			matrix[rowIdx][colIdx].colCriteria = location.controllers.join('-');
			matrix[rowIdx][colIdx].content = {
				value: paramValue,
				id: paramName
			};
		});
	}
	return { rowNames, colNames, matrix };
};

export const extractOutcomeControllersMatrix = (
	templates: MiraTemplate[],
	paramNames: string[],
	paramValueMap: Map<string, any>,
	paramLocationMap: Map<string, TemplateSummary[]>
) => {
	const rowNames = extractConceptNames(templates, 'outcome');
	const colNames = extractConceptNames(templates, 'controllers');
	if (rowNames.length === 0 || colNames.length === 0) {
		return { rowNames, colNames, matrix: [] };
	}

	const matrix = emptyMatrix(rowNames, colNames);

	for (let i = 0; i < paramNames.length; i++) {
		const paramName = paramNames[i];
		const paramValue = paramValueMap.get(paramName);
		const paramLocations = paramLocationMap.get(paramName);
		if (!paramLocationMap) continue;
		if (!paramLocations) continue;

		paramLocations.forEach((location) => {
			const rowIdx = rowNames.indexOf(location.outcome);
			const colIdx = colNames.indexOf(location.controllers.join('-'));

			if (rowIdx === -1 || colIdx === -1) return;

			matrix[rowIdx][colIdx].rowCriteria = location.outcome;
			matrix[rowIdx][colIdx].colCriteria = location.controllers.join('-');
			matrix[rowIdx][colIdx].content = {
				value: paramValue,
				id: paramName
			};
		});
	}
	return { rowNames, colNames, matrix };
};

export const extractTemplateMatrix = (templates: MiraTemplate[]) => {
	const rowNames = extractConceptNames(templates, 'subject');

	const colNames = templates[0]?.controller
		? extractConceptNames(templates, 'controllers')
		: extractConceptNames(templates, 'outcome');

	// Not sure how to parse templates with no subject or no outcomes
	// and interacts with only controllers and itself, return a matrix
	// with a single row for now - Mar 13, 2024
	if (rowNames.length === 0 || colNames.length === 0) {
		const vector: MiraMatrix = [];
		vector.push([]);
		for (let i = 0; i < templates.length; i++) {
			const template = templates[i];
			vector[0].push({
				row: 0,
				col: i,
				rowCriteria: '',
				colCriteria: '',
				content: {
					value: template.rate_law,
					id: template.name
				}
			});
		}
		return { rowNames, colNames, matrix: vector };
	}

	const matrix = emptyMatrix(rowNames, colNames);
	for (let i = 0; i < templates.length; i++) {
		const template = templates[i];
		let rowIdx = 0;
		let colIdx = 0;

		if (template.subject) {
			rowIdx = rowNames.indexOf(template.subject.name);
		}

		// Use subject-controller matrix if possible, otherwise fallback to subject-outcome matrix
		// Note we still use the subject-outcome matrix grid
		if (template.controller) {
			colIdx = colNames.indexOf(template.controller.name);
		} else if (template.outcome) {
			colIdx = colNames.indexOf(template.outcome.name);
		}
		matrix[rowIdx][colIdx].content = {
			value: template.rate_law,
			id: template.name
		};
	}

	return { rowNames, colNames, matrix };
};
