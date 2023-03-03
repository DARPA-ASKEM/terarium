import { ConceptFacets } from './Concept';
import { Dataset, DatasetSearchParams } from './Dataset';
import { Model, ModelSearchParams } from './Model';
import { XDDSearchParams } from './XDD';
import { XDDArtifact, DocumentType } from './Document';

export type Annotation = {
	artifact_id: string;
	artifact_type: string;
	content: string;
	timestampMillis: number;
	username: number;
};

export enum ViewType {
	LIST = 'list',
	MATRIX = 'matrix',
	GRAPH = 'graph'
}

export enum AssetType {
	DOCUMENT = 'document',
	MODEL = 'model',
	DATASET = 'dataset',
	INTERMEDIATE = 'intermediate',
	SIMULATION_PLAN = 'simulation plan',
	SIMULATION_RUN = 'simulation run',
	CODE = 'code',
	ALL = 'all'
}

export type SearchParameters = {
	[AssetType.DOCUMENT]?: XDDSearchParams;
	[AssetType.MODEL]?: ModelSearchParams;
	[AssetType.DATASET]?: DatasetSearchParams;
};

export type ResultType = Model | Dataset | DocumentType;

export type SearchResults = {
	results: ResultType[];
	facets?: Facets;
	rawConceptFacets?: ConceptFacets | null;
	xddExtractions?: XDDArtifact[]; // the result from searching XDD artifacts against a given search term
	searchSubsystem: string;
	hits?: number;
	hasMore?: boolean;
	nextPage?: string;
};

export type FullSearchResults = {
	allData: SearchResults;
	allDataFilteredWithFacets: SearchResults;
};

export type SearchByExampleOptions = {
	similarContent: boolean;
	forwardCitation: boolean;
	bakcwardCitation: boolean;
	relatedContent: boolean;
};

//
// Facets
//
export type FacetBucket = {
	key: string;
	value: number;
};

export type Facets = {
	[key: string]: FacetBucket[];
};

// Side panel
export type SidePanelTab = {
	name: string;
	icon?: string;
	imgSrc?: string;
	isGreyscale?: string;
	badgeCount?: number;
};

// Tabs
export type Tab = {
	label: string;
	icon: string;
	assetId?: string | number;
	assetType?: AssetType;
};
