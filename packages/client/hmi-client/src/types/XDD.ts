import { DocumentType, XDDArtifact } from '@/types/Document';

export enum XDDExtractionType {
	Document = 'Document',
	Table = 'Table',
	Figure = 'Figure',
	Equation = 'Equation',
	Section = 'Section', // Section
	URL = 'URL' // websites, URLs, links, etc.
}

export type XDDDictionary = {
	name: string;
	baseClassification: string;
	source: string;
	caseSensitive: boolean;
};

export type XDDFacetsItemResponse = {
	docCountErrorUpperBound: number;
	sumOtherDocCount: number;
	buckets: { key: string; doc_count: number }[];
};

export type XDDFacetsResponse = {
	[key: string]: XDDFacetsItemResponse;
};

export type XDDResult = {
	success?: {
		data: DocumentType[] | XDDDictionary[] | XDDArtifact[];
		facets: XDDFacetsResponse | null;
		// URL to fetch next page results
		// https://xdd.wisc.edu/api/articles?&include_score=true&per_page=100&term=abbott&publisher=USGS&full_results
		// "https://xdd.wisc.edu/api/articles?scroll_id=a5e403ac-76b9-4400-94bb-59c9e3e030d6"
		nextPage?: string;
		scrollId?: string;
		hits?: number;
	};
	error?: {
		message: string;
	};
	// XDD Sets
	description?: string;
	availableSets?: string[];
	// related docs
	data?: { bibjson: DocumentType; score: number }[];
};

export type XDDSearchParams = {
	docid?: string; // internal xdd document id
	doi?: string;
	term?: string;
	dict?: string[];
	dataset?: string | null;
	type?: XDDExtractionType;
	ignoreBytes?: boolean;
	fullResults?: boolean;
	inclusive?: boolean;
	includeScore?: boolean;
	includeHighlights?: boolean;
	facets?: boolean;
	max?: number;
	perPage?: number;
	min_published?: string; // Must be ISO date string e.g., "2020-01-01"
	max_published?: string; // Must be ISO date string e.g., "2020-01-01"
	pubname?: string;
	publisher?: string;
	match?: boolean; // If true, utilizes a \"match\" instead of a \"match_phrase\" query within Elaticsearch. This has the effect of finding documents which most frequently use the individial terms in the query, rather than looking for the exact phrase.
	additional_fields?: string; // Extend the query to include fields in addition to the full-text contents (example: abstract,title). The query logic is OR across the search fields.
	known_entities?: string; // Include known entities extracted via external tools. Current options: [drugs, emmaa, stratname_candidates, url_extractions]
	fields?: string; // return only fields of interest (passed as comma-separated string) instead of returing the full record in the xDD results
	related_search_enabled?: boolean; // if true, then perform a search by example by finding related artifacts
	similar_search_enabled?: boolean; // if true, then perform a search by example by finding similar documents
	related_search_id?: string | number;
	githubUrls?: string;
};

export const XDD_RESULT_DEFAULT_PAGE_SIZE = 100;

//
// XDD Field names
// Source: https://xdd.wisc.edu/api/articles
//
export const TYPE = 'type'; // Type of document (document, book, etc)
export const TITLE = 'title'; // Document title
export const JOURNAL = 'journal'; // The name of the journal
export const VOL = 'vol'; // Volume
export const NUMBER = 'number'; // Issue
export const AUTHORS = 'authors'; // An array of objects, each containing a key 'name' and a value equal to the name of one author
export const PUBLISHER = 'publisher'; // Publisher (or primary source) of the document (e.g. Elsevier, USGS)
export const DOCUMENT_NAME = 'pubname';
export const PAGES = 'pages'; // Document' page numbers within the issue
export const YEAR = 'year'; // Year of document
export const GITHUB_URL = 'githubUrls';

export const DISPLAY_NAMES: { [key: string]: string } = {
	[TYPE]: 'Document type',
	[TITLE]: 'Article title',
	[JOURNAL]: 'Journal name',
	[VOL]: 'Journal volume',
	[NUMBER]: 'Journal issue number',
	[AUTHORS]: 'Authors',
	[PUBLISHER]: 'Publisher',
	[DOCUMENT_NAME]: 'Document name',
	[PAGES]: 'Number of pages',
	[YEAR]: 'Publication year',
	[GITHUB_URL]: 'Github Repositories'
};

// Initail implementation of facets by XDD team only supports the following fields
export const FACET_FIELDS: string[] = [YEAR, PUBLISHER, JOURNAL, GITHUB_URL];
