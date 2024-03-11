import _ from 'lodash';
import { IGraph } from '@graph-scaffolder/types';
import {
	extractSubjectControllerMatrix,
	extractSubjectOutcomeMatrix,
	removeModifiers
} from './mira-util';
import type { MiraModel, MiraTemplateParams } from './mira-common';

/**
 * Collection of MMT related functions
 * */
export const getContextKeys = (miraModel: MiraModel) => {
	const modifierKeys = new Set<string>();

	miraModel.templates.forEach((t) => {
		if (t.subject && t.subject.context) {
			Object.keys(t.subject.context).forEach((key) => modifierKeys.add(key));
		}
		if (t.outcome && t.outcome.context) {
			Object.keys(t.outcome.context).forEach((key) => modifierKeys.add(key));
		}
		if (t.controller && t.controller.context) {
			Object.keys(t.controller.context).forEach((key) => modifierKeys.add(key));
		}
	});
	return [...modifierKeys];
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

export const collapseTemplates = (miraModel: MiraModel) => {
	const allTemplates: any = [];
	const uniqueTemplates: any = [];

	// 1. Roll back to "original name" by trimming off modifiers
	miraModel.templates.forEach((t) => {
		const scrubbedTemplate = {
			subject: '',
			outcome: '',
			controller: ''
		};
		if (t.subject) {
			scrubbedTemplate.subject = removeModifiers(t.subject.name, t.subject.context);
		}
		if (t.outcome) {
			scrubbedTemplate.outcome = removeModifiers(t.outcome.name, t.outcome.context);
		}
		if (t.controller) {
			scrubbedTemplate.controller = removeModifiers(t.controller.name, t.controller.context);
		}
		allTemplates.push(scrubbedTemplate);
	});

	// 2. Remove duplicated templates
	const check = new Set<string>();
	allTemplates.forEach((t) => {
		const key = `${t.subject}:${t.outcome}:${t.controller}`;
		if (check.has(key)) return;

		uniqueTemplates.push(t);
		check.add(key);
	});
	return uniqueTemplates;
};

interface ParamLocation {
	subject: string;
	outcome: string;
	controllers: string[];
}

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
	//   param => [ [subject, outcome, controller], [subject, outcome, controller] ... ]
	const paramLocationMap = new Map<string, ParamLocation[]>();
	const templateParams = Object.values(miraTemplateParams);
	templateParams.forEach((templateParam) => {
		const params = templateParam.params;
		params.forEach((paramName) => {
			if (!paramLocationMap.has(paramName)) paramLocationMap.set(paramName, []);

			paramLocationMap.get(paramName)?.push({
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
	const subjectController = extractSubjectControllerMatrix(
		templates,
		childrenParams,
		paramValueMap,
		paramLocationMap
	);

	// FIXME: check if we need to add outcomeController matrix
	return {
		subjectOutcome,
		subjectController
	};
};

export const converToIGraph = (templates: any[]) => {
	const graph: IGraph<any, any> = {
		nodes: [],
		edges: [],
		width: 500,
		height: 500,
		amr: null
	};

	const subjects = new Set<string>(templates.map((d) => d.subject));
	const outcomes = new Set<string>(templates.map((d) => d.outcome));
	const controllers = new Set<string>(templates.map((d) => d.controller));
	const nodeNames = [...new Set([...subjects, ...outcomes, ...controllers])];

	// States
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

	// Transitions
	templates.forEach((t) => {
		const key = `${t.subject}:${t.outcome}:${t.controller}`;
		graph.nodes.push({
			id: key,
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
	// FIXME: controller edges
	templates.forEach((t) => {
		const key = `${t.subject}:${t.outcome}:${t.controller}`;
		graph.edges.push({
			source: t.subject,
			target: key,
			points: []
		});
		graph.edges.push({
			source: key,
			target: t.outcome,
			points: []
		});
	});

	return graph;
};
