import { CONCEPT_FACETS_DISPLAY_NAME, CONCEPT_FACETS_FIELD } from './Concept';
import { Filters } from './Filter';

export type ModelSearchParams = {
	filters?: Filters;
	related_search_id?: string;
	related_search_enabled?: boolean; // if true, then perform a search by example by finding related models
};

// These are common properties among states, parameters, transitions, observables, etc
// Useful for UI components that need to display these properties
export type ModelVariable = {
	id: string;
	name?: string;
	description?: string;
	grounding?: any;
	unitExpression?: string;
	expression?: string;
	expression_mathml?: string;
};

export enum StratifiedMatrix {
	Initials = 'initials',
	Parameters = 'parameters',
	Rates = 'rates'
}

//
// Model Field names
//
export const ID = 'id';
export const NAME = 'name';
export const DESCRIPTION = 'description';
export const FRAMEWORK = 'schema';

export const DISPLAY_NAMES: { [key: string]: string } = {
	[NAME]: 'Model Name',
	[FRAMEWORK]: 'Model Framework',
	[CONCEPT_FACETS_FIELD]: CONCEPT_FACETS_DISPLAY_NAME
};

export const FACET_FIELDS: string[] = [ID, FRAMEWORK]; // fields to show facets for
export const MODEL_FILTER_FIELDS: string[] = [NAME, DESCRIPTION]; // when applying non-facet filters, search these fields
