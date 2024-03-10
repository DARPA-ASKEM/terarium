import _ from 'lodash';
import {
	extractSubjectControllerMatrix,
	extractSubjectOutcomeMatrix,
	removeModifiers
} from './mira-util';
import type { MiraModel, MiraTemplateParams } from './mira-common';

/**
 * Collection of MMT related functions
 * */
export const collapseParameters = (miraModel: MiraModel) => {
	const map = new Map<string, string[]>();
	const keys = Object.keys(miraModel.parameters);

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

/**
 * Assumes one-to-one with cells
 *
 * */
export const createParameterMatrix = (
	miraModel: MiraModel,
	miraTempateParams: MiraTemplateParams,
	param: string
) => {
	const paramMap = collapseParameters(miraModel);

	const childrenParams = paramMap.get(param);
	if (!childrenParams) throw new Error(`Cannot map ${param}`);

	// Create map
	//   param => [ [subject, outcome, controller], [subject, outcome, controller] ... ]
	const paramLocationMap = new Map<string, any>();
	const templateParams = Object.values(miraTempateParams);
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

	// Create map
	//   param => value
	const paramValueMap = new Map<string, any>();
	Object.values(miraModel.parameters).forEach((paramObj) => {
		paramValueMap.set(paramObj.name, paramObj.value);
	});

	// Find templates with expressions that contains one or more of the params
	const templates = miraModel.templates.filter((t) => {
		const miraTemplateParam = miraTempateParams[t.name];

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

	console.log('!!!!!!!!!!!!!!!!!!!!!!!!!!!1');
	console.log(subjectController.rowNames);
	console.log(subjectController.colNames);
	console.log(subjectController.matrix);

	return {
		subjectOutcome,
		subjectController
	};
};
