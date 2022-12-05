export enum XDDExtractionType {
	Document = 'Document',
	Table = 'Table',
	Figure = 'Figure',
	Equation = 'Equation',
	Body = 'Body Text' // Section
}

export type XDDArticleAuthor = {
	name: string;
};

export type XDDArticleIdentifier = {
	type: string;
	id: string;
};

export type XDDArticleLink = {
	type: string;
	url: string;
};

export type XDDArticleKnownTerms = {
	[term: string]: string[];
};

export type XDDArticle = {
	// REVIEW: FIXME: server should provide proper field names
	//         also, reove the temp workaround in the client data service
	abstractText: string; // mapped from abstract
	abstract: string;
	author: XDDArticleAuthor[];
	identifier: XDDArticleIdentifier[];
	journal: string;
	// eslint-disable-next-line no-underscore-dangle
	known_terms?: XDDArticleKnownTerms[]; // TEMP
	knownTerms?: XDDArticleKnownTerms[]; // mapped from known_terms
	link: XDDArticleLink[];
	number: string;
	pages: string;
	publisher: string;
	title: string; // name
	type: string;
	volume: string;
	year: string;
	gddid: string; // mapped from _gddid
	// eslint-disable-next-line no-underscore-dangle
	_highlight: string[];
	highlight: string[]; // TEMP: mapped from _highlight
	// eslint-disable-next-line no-underscore-dangle
	_gddid: string; // TEMP
	// additional-client-side fields
	relatedDocuments?: XDDArticle[];
};

export type PublicationAsset = {
	id?: string;
	xdd_uri: string; // this is the internal XDD id known as "docid" NOT "doi"
	title: string;
};

export type XDDArtifactProperties = {
	title: string;
	DOI: string;
	trustScore: string;
	abstractText: string;
	xddId: string;
	documentId: string;
	documentTitle: string;
	contentText: string;
	indexInDocument: number;
	contentJSON: { [key: string]: { [key: string]: string } };
	image: string;
	relevantSentences: string;
	sectionID: string;
	sectionTitle: string;
	caption: string;
};

// XDD extraction object, which should match Extraction.java at the backend
export type XDDArtifact = {
	ASKEM_CLASS: string; // mapped from askemClass
	askemClass: string;
	properties: XDDArtifactProperties;
	askemId: string;
	xddCreated: Date;
	xddRegistrant: number;
};

export type XDDDictionary = {
	name: string;
	base_classification: string;
	source: string;
	case_sensitive: boolean;
};

export type XDDFacetsItemResponse = {
	doc_count_error_upper_bound: number;
	sum_other_doc_count: number;
	buckets: { key: string; doc_count: number }[];
};

export type XDDFacetsResponse = {
	[key: string]: XDDFacetsItemResponse;
};

export type XDDResult = {
	success?: {
		data: XDDArticle[] | XDDDictionary[] | XDDArtifact[];
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
	available_sets?: string[];
	// related docs
	data?: { bibjson: XDDArticle; score: number }[];
};

export type XDDSearchParams = {
	docid?: string; // internal xdd document id
	doi?: string;
	title?: string;
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
};

export const XDD_RESULT_DEFAULT_PAGE_SIZE = 100;

//
// XDD Field names
// Source: https://xdd.wisc.edu/api/articles
//
export const TYPE = 'type'; // Type of publication (article, book, etc)
export const TITLE = 'title'; // Article title
export const JOURNAL = 'journal'; // The name of the journal
export const VOL = 'vol'; // Volume
export const NUMBER = 'number'; // Issue
export const AUTHORS = 'authors'; // An array of objects, each containing a key 'name' and a value equal to the name of one author
export const PUBLISHER = 'publisher'; // Publisher (or primary source) of the article (e.g. Elsevier, USGS)
export const PUBLICATION_NAME = 'pubname';
export const PAGES = 'pages'; // Articles' page numbers within the issue
export const YEAR = 'year'; // Year of publication

export const DISPLAY_NAMES: { [key: string]: string } = {
	[TYPE]: 'Publication Type',
	[TITLE]: 'Article Title',
	[JOURNAL]: 'Journal Name',
	[VOL]: 'Journal Volume',
	[NUMBER]: 'Journal Issue Number',
	[AUTHORS]: 'Authors',
	[PUBLISHER]: 'Publisher',
	[PUBLICATION_NAME]: 'Publication Name',
	[PAGES]: 'Number of Pages',
	[YEAR]: 'Publication Year'
};

// Initail implementation of facets by XDD team only supports the following fields
export const FACET_FIELDS: string[] = [PUBLISHER, YEAR, PUBLICATION_NAME];
