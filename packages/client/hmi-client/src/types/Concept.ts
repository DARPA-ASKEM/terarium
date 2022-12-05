export type ConceptFacetsType = {
	datasets?: number;
	features?: number;
	intermediates?: number;
	model_parameters?: number;
	models?: number;
	projects?: number;
	publications?: number;
	qualifiers?: number;
	simulation_parameters?: number;
	simulation_plans?: number;
	simulation_run?: number;
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
