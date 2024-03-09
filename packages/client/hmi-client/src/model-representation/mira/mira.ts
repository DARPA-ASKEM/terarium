import _ from 'lodash';

/**
 * Collection of MMT related functions
 * */

interface MiraModel {
	templates: any[];
	parameters: { [key: string]: any };
	initials: { [key: string]: any };
	observables: { [key: string]: any };
	annotations: any;
	time: any;
}

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

const removeModifiers = (v: string, context: { [key: string]: string }) => {
	let result = v;
	const contextKeys = Object.keys(context);
	contextKeys.forEach((key) => {
		result = result.replace(`_${context[key]}`, '');
	});
	return result;
};

export const collapseTemplates = (miraModel: MiraModel) => {
	const allTemplates: any = [];
	const uniqueTemplates: any = [];

	// 1. Roll back to "original name" by trimming off modifiers
	miraModel.templates.forEach((t) => {
		const scrubbedTemplate = {
			subject: removeModifiers(t.subject.name, t.subject.context),
			outcome: removeModifiers(t.outcome.name, t.outcome.context),
			controller: removeModifiers(t.controller.name, t.controller.context)
		};
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
