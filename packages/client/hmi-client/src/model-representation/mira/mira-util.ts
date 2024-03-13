import { MiraTemplate, MiraMatrix, TemplateSummary } from './mira-common';

export const removeModifiers = (
	v: string,
	context: { [key: string]: string },
	scrubbingKeys: string[]
) => {
	let result = v;
	scrubbingKeys.forEach((key) => {
		result = result.replace(`_${context[key]}`, '');
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
			if (template.controller) {
				names.push(template.controller.name);
			}
			if (template.controllers && template.controllers.length > 0) {
				const list = template.controllers.map((d) => d.name).sort();
				names.push(list.join('-'));
			}
		});
	}

	const uniqueNames = [...new Set(names)];
	uniqueNames.sort();
	return uniqueNames;
};

// Empty matrix placeholder
const emptyMatrix = (rowNames: string[], colNames: string[]) => {
	const matrix: MiraMatrix = [];
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

export const extractSubjectControllersMatrix = (
	templates: MiraTemplate[],
	paramNames: string[],
	paramValueMap: Map<string, any>,
	paramLocationMap: Map<string, TemplateSummary[]>
) => {
	const rowNames = extractConceptNames(templates, 'subject');
	const colNames = extractConceptNames(templates, 'controllers');
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

			matrix[rowIdx][colIdx].value = paramValue;
			matrix[rowIdx][colIdx].id = paramName;
		});
	}
	return { rowNames, colNames, matrix };
};

export const extractTemplateMatrix = (templates: MiraTemplate[]) => {
	// FIXME: subject, outcome may not exist
	const rowNames = extractConceptNames(templates, 'subject');
	const colNames = extractConceptNames(templates, 'outcome');
	const matrix = emptyMatrix(rowNames, colNames);

	for (let i = 0; i < templates.length; i++) {
		const template = templates[i];
		const rowIdx = rowNames.indexOf(template.subject.name);
		const colIdx = colNames.indexOf(template.outcome.name);

		matrix[rowIdx][colIdx].value = template.rate_law;
		matrix[rowIdx][colIdx].id = template.name;
	}
	return { rowNames, colNames, matrix };
};
