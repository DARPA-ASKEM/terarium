import { Model, ModelSearchParams } from './Model';
import { XDDArticle, XDDSearchParams } from './XDD';

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
};

export type ResultType = Model | XDDArticle;

export type SearchResults = {
	results: ResultType[];
	facets?: Facets;
	searchSubsystem: string;
	hits?: number;
	hasMore?: boolean;
	nextPage?: string;
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
