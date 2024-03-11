import {
	AssetType,
	Dataset,
	Document,
	DocumentAsset,
	Model,
	XDDFacetsItemResponse
} from '@/types/Types';
import { ConceptFacets } from './Concept';
import { DatasetSearchParams } from './Dataset';
import { ModelSearchParams } from './Model';
import { XDDSearchParams } from './XDD';
import { ProjectPages } from './Project';

export interface FeatureConfig {
	isPreview: boolean;
}

export enum ParamType {
	CONSTANT,
	DISTRIBUTION,
	TIME_SERIES,
	MATRIX,
	EXPRESSION
}

export interface ModelConfigTableData {
	id: string;
	name: string;
	type: ParamType;
	activeType?: ParamType; // This is used to keep track of the active type used by the user
	value: any;
	source: string;
	visibility: boolean;
	tableFormattedMatrix?: ModelConfigTableData[];
	timeseries?: string;
}

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

export type ResultType = Model | Dataset | Document | DocumentAsset;

export type SearchResults = {
	results: ResultType[];
	facets?: { [p: string]: XDDFacetsItemResponse } | Facets;
	rawConceptFacets?: ConceptFacets | null;
	searchSubsystem?: string;
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

export type AssetRoute = {
	assetId: string;
	pageType: AssetType | ProjectPages;
};

export interface AssetItem extends AssetRoute {
	icon?: string;
	assetName?: string;
}

export type CodeRequest = {
	asset: AssetItem;
	code?: string;
};

// TODO this should come from the back end, and we should also have maps for the "categories" of types (artifacts, models, datasets, etc)
export enum AcceptedTypes {
	PDF = 'application/pdf',
	CSV = 'text/csv',
	TXT = 'text/plain',
	MD = 'text/markdown',
	PY = 'text/x-python-script',
	R = 'text/x-r',
	JL = 'application/julia',
	NC = 'application/x-netcdf',
	JSON = 'application/json',
	XML = 'application/xml',
	SBML = 'application/sbml+xml',
	MDL = `application/vnd.vensim.mdl`,
	XMILE = 'application/vnd.stella.xmile',
	ITMX = 'application/vnd.stella.itmx',
	STMX = 'application/vnd.stella.stmx'
}

export enum AcceptedExtensions {
	PDF = 'pdf',
	CSV = 'csv',
	TXT = 'txt',
	MD = 'md',
	PY = 'py',
	R = 'r',
	JL = 'jl',
	// NetCDF format
	NC = 'nc',
	// Model file extensions
	JSON = 'json',
	// SBML formats
	XML = 'xml',
	SBML = 'sbml',
	// Vensim format
	MDL = 'mdl',
	// Stella formats
	XMILE = 'xmile',
	ITMX = 'itmx',
	STMX = 'stmx'
}

export enum AMRSchemaNames {
	PETRINET = 'petrinet',
	REGNET = 'regnet',
	STOCKFLOW = 'stockflow',
	DECAPODES = 'decapodes'
}

export interface PDFExtractionResponseType {
	text: string;
	images: string[];
}

export interface Position {
	x: number;
	y: number;
}
export enum ModelServiceType {
	TA1 = 'SKEMA-MIT',
	TA4 = 'GoLLM'
}

export interface CompareModelsResponseType {
	response: string;
}
