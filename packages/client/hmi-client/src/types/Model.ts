import { PetriNet } from '@/utils/petri-net-validator';
import { CONCEPT_FACETS_DISPLAY_NAME, CONCEPT_FACETS_FIELD } from './Concept';
import { Filters } from './Filter';

// FIXME: other model content types will be supported depending on
//	Model.framework
export type ModelContent = PetriNet;

export type Model = {
	id: string | number;
	name: string;
	description: string;

	framework: string;
	concept: string;
	timestamp: string | Date;
	parameters: { [key: string]: string };
	content: PetriNet;

	type: string;
};

export type ModelSearchParams = {
	filters?: Filters;
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

export const FACET_FIELDS: string[] = [ID, FRAMEWORK, NAME]; // fields to show facets for
export const MODEL_FILTER_FIELDS: string[] = [NAME, DESCRIPTION]; // when applying non-facet filters, search these fields
