import { Filters } from './Filter';

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
	filters?: Filters;
};

//
// Model Field names
//
export const NAME = 'name';
export const DESCRIPTION = 'description';
export const FRAMEWORK = 'framework';
export const CONCEPT = 'source';

export const DISPLAY_NAMES: { [key: string]: string } = {
	[NAME]: 'Model Name',
	[FRAMEWORK]: 'Model Framework',
	[CONCEPT]: 'Model Concept'
};

export const FACET_FIELDS: string[] = [FRAMEWORK, NAME]; // fields to show facets for
export const MODEL_FILTER_FIELDS: string[] = [NAME, DESCRIPTION]; // when applying non-facet filters, search these fields
