export enum ConceptFacetOptions { // These are the options for /concept/facets in data-service
	DATASETS = 'datasets',
	FEATURES = 'features',
	INTERMEDIATES = 'intermediates',
	MODEL_PARAMETERS = 'model_parameters',
	MODELS = 'models',
	PROJECTS = 'projects',
	DOCUMENTS = 'publications',
	QUALIFIERS = 'qualifiers',
	SIMULATION_PARAMETERS = 'simulation_parameters',
	SIMULATION_PLANS = 'simulation_plans',
	SIMLUATION_RUN = 'simulation_run'
}

export type ConceptFacetsType = {
	[ConceptFacetOptions.DATASETS]?: number;
	[ConceptFacetOptions.FEATURES]?: number;
	[ConceptFacetOptions.INTERMEDIATES]?: number;
	[ConceptFacetOptions.MODEL_PARAMETERS]?: number;
	[ConceptFacetOptions.MODELS]?: number;
	[ConceptFacetOptions.PROJECTS]?: number;
	[ConceptFacetOptions.DOCUMENTS]?: number;
	[ConceptFacetOptions.QUALIFIERS]?: number;
	[ConceptFacetOptions.SIMULATION_PARAMETERS]?: number;
	[ConceptFacetOptions.SIMULATION_PLANS]?: number;
	[ConceptFacetOptions.SIMLUATION_RUN]?: number;
};

export type Concept = {
	count: number;
	name: string | null;
};

export type ConceptFacetResult = {
	type: string; // one of the ConceptFacetsType
	id: number;
	curie: string; // concept key
	name: string;
};

export type ConceptFacets = {
	facets: {
		types: ConceptFacetsType;
		concepts: { [key: string]: Concept };
	};
	results: ConceptFacetResult[];
};

export const CONCEPT_FACETS_FIELD = 'concepts'; // @REVIEW: what about using 'id'
export const CONCEPT_FACETS_DISPLAY_NAME = 'Concepts';
