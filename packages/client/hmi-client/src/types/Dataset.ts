import { CONCEPT_FACETS_DISPLAY_NAME, CONCEPT_FACETS_FIELD } from './Concept';
import { Filters } from './Filter';

export type DatasetSearchParams = {
	filters?: Filters;
	related_search_id?: string;
	related_search_enabled?: boolean; // if true, then perform a search by example by finding related datasets
};

//
// Dataset Field names
//
export const ID = 'id';
export const NAME = 'name';
export const DESCRIPTION = 'description';
export const SIMULATION_RUN = 'simulation_run';

export const DISPLAY_NAMES: { [key: string]: string } = {
	[NAME]: 'Dataset Name',
	[SIMULATION_RUN]: 'Simulation Run',
	[CONCEPT_FACETS_FIELD]: CONCEPT_FACETS_DISPLAY_NAME
};

export const FACET_FIELDS: string[] = [ID, SIMULATION_RUN]; // fields to show facets for
export const DATASET_FILTER_FIELDS: string[] = [NAME, DESCRIPTION]; // when applying non-facet filters, search these fields
