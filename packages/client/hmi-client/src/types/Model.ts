import { CONCEPT_FACETS_DISPLAY_NAME, CONCEPT_FACETS_FIELD } from './Concept';
import { Filters } from './Filter';

export type ConcreteModel<T> = {
	id: string | number;
	name: string;
	description: string;

	framework: string;
	concept: string;
	timestamp: string | Date;
	/* parameters: { [key: string]: string }; */
	parameters: any; // FIXME
	content: T;

	type: string;
};

// Generic Model
export type Model = ConcreteModel<any>;

export type ModelSearchParams = {
	filters?: Filters;
	related_search_id?: string | number;
	related_search_enabled?: boolean; // if true, then perform a search by example by finding related models
};

//
// Model Field names
//
export const ID = 'id';
export const NAME = 'name';
export const DESCRIPTION = 'description';
export const FRAMEWORK = 'framework';

export const DISPLAY_NAMES: { [key: string]: string } = {
	[NAME]: 'Model Name',
	[FRAMEWORK]: 'Model Framework',
	[CONCEPT_FACETS_FIELD]: CONCEPT_FACETS_DISPLAY_NAME
};

export const FACET_FIELDS: string[] = [ID, FRAMEWORK]; // fields to show facets for
export const MODEL_FILTER_FIELDS: string[] = [NAME, DESCRIPTION]; // when applying non-facet filters, search these fields
