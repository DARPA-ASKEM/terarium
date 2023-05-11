import { XDDFacetsItemResponse, Document, Extraction } from '@/types/Types';
import { ConceptFacets } from './Concept';
import { Dataset, DatasetSearchParams } from './Dataset';
import { Model, ModelSearchParams } from './Model';
import { XDDSearchParams } from './XDD';
import { ProjectAssetTypes } from './Project';

export type Annotation = {
	id: string;
	artifact_id: string;
	artifact_type: string;
	content: string;
	timestampMillis: number;
	username: number;
	section: string;
};

export enum ViewType {
	LIST = 'list',
	MATRIX = 'matrix',
	GRAPH = 'graph'
}

export enum ResourceType {
	XDD = 'xdd',
	MODEL = 'model',
	DATASET = 'dataset',
	ALL = 'all'
}

export type SearchParameters = {
	[ResourceType.XDD]?: XDDSearchParams;
	[ResourceType.MODEL]?: ModelSearchParams;
	[ResourceType.DATASET]?: DatasetSearchParams;
};

export type ResultType = Model | Dataset | Document;

export type SearchResults = {
	results: ResultType[];
	facets?: { [p: string]: XDDFacetsItemResponse } | Facets;
	rawConceptFacets?: ConceptFacets | null;
	xddExtractions?: Extraction[]; // the result from searching XDD artifacts against a given search term
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
	backwardCitation: boolean;
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
	assetName: string;
	icon?: string;
	assetId?: string;
	assetType?: ProjectAssetTypes | 'overview' | 'workflow' | '';
};

export enum AcceptedTypes {
	PDF = 'application/pdf',
	JPG = 'image/jpg',
	JPEG = 'image/jpeg',
	PNG = 'image/png',
	CSV = 'text/csv'
}

export interface PDFExtractionResponseType {
	text: string;
	images: string[];
}
