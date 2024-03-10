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
	names.sort();
	return names;
};
