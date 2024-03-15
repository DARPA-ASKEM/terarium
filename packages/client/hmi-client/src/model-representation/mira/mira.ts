import _ from 'lodash';
import { IGraph } from '@graph-scaffolder/types';
import {
	extractOutcomeControllersMatrix,
	extractSubjectControllersMatrix,
	extractSubjectOutcomeMatrix,
	removeModifiers
} from './mira-util';
import type { MiraModel, MiraTemplate, MiraTemplateParams, TemplateSummary } from './mira-common';

export const emptyMiraModel = () => {
	const newModel: MiraModel = {
		templates: [],
		parameters: {},
		initials: {},
		observables: {},
		annotations: {},
		time: {}
	};
	return newModel;
};

/**
 * Collection of MMT related functions
 * */
export const getContextKeys = (miraModel: MiraModel) => {
	const modifierKeys = new Set<string>();

	// Heuristics to avoid picking up wrong stuff
	// - if the modifier value starts with 'ncit:' then it is not a user initiated stratification
	// const modifiers = rawModifiers.filter((v) => {
	// 	if (v.startsWith('ncit:')) return false;
	// 	return true;
	// });
	const addWithGuard = (k: string, v: string) => {
		if (v.startsWith('ncit:')) return;
		modifierKeys.add(k);
	};

	miraModel.templates.forEach((t) => {
		if (t.subject?.context) {
			Object.entries(t.subject.context).forEach(([k, v]) => {
				addWithGuard(k, v as string);
			});
		}
		if (t.outcome?.context) {
			Object.entries(t.outcome.context).forEach(([k, v]) => {
				addWithGuard(k, v as string);
			});
		}
		if (t.controller?.context) {
			Object.entries(t.controller.context).forEach(([k, v]) => {
				addWithGuard(k, v as string);
			});
		}
		if (t.controllers && t.controllers.length > 0) {
			t.controllers.forEach((miraConcept) => {
				// Object.keys(miraConcept.context).forEach((key) => modifierKeys.add(key));
				Object.entries(miraConcept.context).forEach(([k, v]) => {
					addWithGuard(k, v as string);
				});
			});
		}
	});

	const modifiers = [...modifierKeys];
	return modifiers;
};

export const isStratifiedModel = (miraModel: MiraModel) => {
	const keys = getContextKeys(miraModel);
	return keys.length > 0;
};

/**
 * Collapse parameters into a lookup map:
 *
 * { var1 => [var1_a, var1_b ...] }
 *
 * Note this is mostly heuristically based, so there may be some edge cases where this will not work,
 * expecially if the model is changed manually after a stratification operation, e.g. rename a stratified variable
 * */
export const collapseParameters = (
	miraModel: MiraModel,
	miraTemplateParams: MiraTemplateParams
) => {
	const map = new Map<string, string[]>();
	const keys = Object.keys(miraModel.parameters);

	for (let i = 0; i < keys.length; i++) {
		const key = keys[i];
		const name = key;
		const tokens = key.split('_') as string[];
		const rootName = _.first(tokens) as string;

		// Ignore non-numerics
		if (tokens.length > 1) {
			let numerical = true;
			for (let j = 1; j < tokens.length; j++) {
				if (Number.isNaN(parseInt(tokens[j], 10))) {
					numerical = false;
				}
			}
			if (!numerical) {
				map.set(name, [name]);
				continue;
			}
		}

		if (map.has(rootName)) {
			map.get(rootName)?.push(name);
		} else {
			map.set(rootName, [name]);
		}
	}

	const mapKeys = [...map.keys()];
	const templateParams = Object.values(miraTemplateParams);

	/**
	 * Assume that a stratified parameters do not intermingle. E.g.
	 * if we stratify age into [a1, a2, a3, ... a8], an expression
	 * will only have 1 of the new age-strata-variable. If it is not the
	 * case we will re-classify them.
	 * */
	for (let i = 0; i < mapKeys.length; i++) {
		const mapKey = mapKeys[i];
		const childrenParams = map.get(mapKey);

		const isNormalStratify = _.every(
			templateParams,
			(t) => _.intersection(t.params, childrenParams).length <= 1
		);
		if (!isNormalStratify) {
			map.delete(mapKey);
			childrenParams?.forEach((p) => {
				map.set(p, [p]);
			});
		}
	}
	return map;
};

export const collapseInitials = (miraModel: MiraModel) => {
	const map = new Map<string, string[]>();
	const keys = Object.keys(miraModel.initials);

	for (let i = 0; i < keys.length; i++) {
		const key = keys[i];
		const name = key;
		const rootName = _.first(key.split('_')) as string;

		if (map.has(rootName)) {
			map.get(rootName)?.push(name);
		} else {
			map.set(rootName, [name]);
		}
	}
	return map;
};

export const rawTemplatesSummary = (miraModel: MiraModel) => {
	const allTemplates: TemplateSummary[] = [];
	miraModel.templates.forEach((t) => {
		const summary: TemplateSummary = {
			name: t.name,
			subject: '',
			outcome: '',
			controllers: []
		};

		if (t.subject) {
			summary.subject = t.subject.name;
		}
		if (t.outcome) {
			summary.outcome = t.outcome.name;
		}

		// note controller and controllers are mutually exclusive
		if (t.controller) {
			summary.controllers.push(t.controller.name);
		}
		if (t.controllers && t.controllers.length > 0) {
			t.controllers.forEach((miraConcept) => {
				summary.controllers.push(miraConcept.name);
			});
			t.controllers.sort();
		}
		allTemplates.push(summary);
	});
	return allTemplates;
};

export const collapseTemplates = (miraModel: MiraModel) => {
	const allTemplates: TemplateSummary[] = [];
	const uniqueTemplates: TemplateSummary[] = [];
	const scrubbingKeys = getContextKeys(miraModel);

	// 1. Roll back to "original name" by trimming off modifiers
	miraModel.templates.forEach((t) => {
		const scrubbedTemplate: TemplateSummary = {
			name: t.name,
			subject: '',
			outcome: '',
			controllers: []
		};

		if (t.subject) {
			scrubbedTemplate.subject = removeModifiers(t.subject.name, t.subject.context, scrubbingKeys);
		}
		if (t.outcome) {
			scrubbedTemplate.outcome = removeModifiers(t.outcome.name, t.outcome.context, scrubbingKeys);
		}

		// note controller and controllers are mutually exclusive
		if (t.controller) {
			scrubbedTemplate.controllers.push(
				removeModifiers(t.controller.name, t.controller.context, scrubbingKeys)
			);
		}
		if (t.controllers && t.controllers.length > 0) {
			t.controllers.forEach((miraConcept) => {
				scrubbedTemplate.controllers.push(
					removeModifiers(miraConcept.name, miraConcept.context, scrubbingKeys)
				);
			});
			t.controllers.sort();
		}
		allTemplates.push(scrubbedTemplate);
	});

	const templateMap = new Map<string, MiraTemplate>();
	miraModel.templates.forEach((t) => {
		templateMap.set(t.name, t);
	});

	// 2. Do post processing
	// - Reduce down to the unique templates-summary, there are used to render the graph
	// - Link unique template-summary back to the original MiraTemplates, this is for interaction
	let keyCounter = 0;
	const check = new Map<string, number>();
	const tempMatrixMap = new Map<string, MiraTemplate[]>();
	const matrixMap = new Map<string, MiraTemplate[]>();

	allTemplates.forEach((t) => {
		const key = `${t.subject}:${t.outcome}:${t.controllers.join('-')}`;
		if (!tempMatrixMap.has(key)) {
			tempMatrixMap.set(key, []);
		}
		const originalTemplate = templateMap.get(t.name);
		tempMatrixMap.get(key)?.push(originalTemplate as MiraTemplate);

		if (check.has(key)) return;

		uniqueTemplates.push(t);
		check.set(key, ++keyCounter);
	});

	// 3 Rename and sanitize everything
	uniqueTemplates.forEach((t) => {
		const key = `${t.subject}:${t.outcome}:${t.controllers.join('-')}`;
		t.name = `template-${check.get(key)}`;
	});
	tempMatrixMap.forEach((value, key) => {
		const name = `template-${check.get(key)}`;
		matrixMap.set(name, value);
	});

	return {
		templatesSummary: uniqueTemplates,
		matrixMap
	};
};

/**
 * Assumes one-to-one with cells
 *
 * */
export const createParameterMatrix = (
	miraModel: MiraModel,
	miraTemplateParams: MiraTemplateParams,
	param: string
) => {
	const paramMap = collapseParameters(miraModel, miraTemplateParams);
	const childrenParams = paramMap.get(param);
	if (!childrenParams) throw new Error(`Cannot map ${param}`);

	// Create map for mapping params to row/col of matrix
	//   param => [ [subject, outcome, controllers], [subject, outcome, controllers] ... ]
	const paramLocationMap = new Map<string, TemplateSummary[]>();
	const templateParams = Object.values(miraTemplateParams);
	templateParams.forEach((templateParam) => {
		const params = templateParam.params;
		params.forEach((paramName) => {
			if (!paramLocationMap.has(paramName)) paramLocationMap.set(paramName, []);

			paramLocationMap.get(paramName)?.push({
				name: templateParam.name,
				subject: templateParam.subject,
				outcome: templateParam.outcome,
				controllers: templateParam.controllers
			});
		});
	});

	// Create map for param values
	//   param => value
	const paramValueMap = new Map<string, any>();
	Object.values(miraModel.parameters).forEach((paramObj) => {
		paramValueMap.set(paramObj.name, paramObj.value);
	});

	// Find templates with expressions that contains one or more of the params
	const templates = miraModel.templates.filter((t) => {
		const miraTemplateParam = miraTemplateParams[t.name];
		const intersection = _.intersection(childrenParams, miraTemplateParam.params);
		return intersection.length > 0;
	});

	const subjectOutcome = extractSubjectOutcomeMatrix(
		templates,
		childrenParams,
		paramValueMap,
		paramLocationMap
	);
	const subjectControllers = extractSubjectControllersMatrix(
		templates,
		childrenParams,
		paramValueMap,
		paramLocationMap
	);
	const outcomeControllers = extractOutcomeControllersMatrix(
		templates,
		childrenParams,
		paramValueMap,
		paramLocationMap
	);

	// FIXME: check if we need to add outcomeController matrix
	return {
		subjectOutcome,
		subjectControllers,
		outcomeControllers
	};
};

// const genKey = (t: TemplateSummary) => `${t.subject}:${t.outcome}:${t.controllers.join('-')}`;

export const convertToIGraph = (templates: TemplateSummary[]) => {
	const graph: IGraph<any, any> = {
		nodes: [],
		edges: [],
		width: 500,
		height: 500,
		amr: null
	};

	const subjects = new Set<string>(templates.map((d) => d.subject));
	const outcomes = new Set<string>(templates.map((d) => d.outcome));
	// const nodeNames = [...new Set([...subjects, ...outcomes])];

	const controllers = new Set<string>(templates.map((d) => d.controllers).flat());
	const nodeNames = [...new Set([...subjects, ...outcomes, ...controllers])];

	// concepts
	nodeNames.forEach((nodeName) => {
		if (nodeName === '') return;
		graph.nodes.push({
			id: nodeName,
			label: nodeName,
			x: 0,
			y: 0,
			width: 50,
			height: 50,
			data: { type: 'state' },
			nodes: []
		});
	});

	// templates
	templates.forEach((t) => {
		graph.nodes.push({
			id: t.name,
			label: '',
			x: 0,
			y: 0,
			width: 50,
			height: 50,
			data: { type: 'transition' },
			nodes: []
		});
	});

	// Edges
	templates.forEach((t) => {
		if (t.subject !== '') {
			graph.edges.push({
				source: t.subject,
				target: t.name,
				points: [],
				data: {}
			});

			t.controllers.forEach((controllerName) => {
				graph.edges.push({
					source: controllerName,
					target: t.name,
					points: [],
					data: { isController: true }
				});
			});
		}
		if (t.outcome !== '') {
			graph.edges.push({
				source: t.name,
				target: t.outcome,
				points: [],
				data: {}
			});

			t.controllers.forEach((controllerName) => {
				graph.edges.push({
					source: t.name,
					target: controllerName,
					points: [],
					data: { isController: true }
				});
			});
		}
	});

	return graph;
};
