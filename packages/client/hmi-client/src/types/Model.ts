export type ModelContent = {
	S: { [key: string]: string };
	T: { [key: string]: string };
	I: { [key: string]: number };
	O: { [key: string]: number };
};

export type Model = {
	id: string;
	name: string;
	description: string;

	framework: string;
	concept: string;
	timestamp: string | Date;
	parameters: { [key: string]: string };
	content: ModelContent;

	type: string;
};

export type ModelSearchParams = {
	name?: string;
};

//
// Model Field names
//
export const NAME = 'name';
export const DESCRIPTION = 'description';

export const FRAMEWORK = 'type';
export const CONCEPT = 'source';

export const DISPLAY_NAMES: { [key: string]: string } = {
	[FRAMEWORK]: 'Model Framework',
	[CONCEPT]: 'Model Concept'
};

export const FACET_FIELDS: string[] = [FRAMEWORK];
export const MODEL_FILTER_FIELDS: string[] = [NAME, DESCRIPTION];
