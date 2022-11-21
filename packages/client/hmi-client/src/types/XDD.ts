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

export const ArticleFilterAttributes = ['title', 'publisher'];

export type XDDArticle = {
	// REVIEW: FIXME: server should provide proper field names
	//         also, reove the temp workaround in the client data service
	abstractText: string; // mapped from abstract
	abstract: string;
	author: XDDArticleAuthor[];
	identifier: XDDArticleIdentifier[];
	journal: string;
	knownTerms?: XDDArticleKnownTerms[];
	link: XDDArticleLink[];
	number: string;
	pages: string;
	publisher: string;
	title: string; // name
	type: string;
	volume: string;
	year: string;
	gddid: string; // mapped from _gddid
	_gddid: string;
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

export type XDDResult = {
	success?: {
		data: XDDArticle[] | XDDDictionary[] | XDDArtifact[];
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
};

export type XDDSearchParams = {
	dict_names?: string[];
	dataset?: string | null;
	enablePagination?: boolean;
	pageSize?: number;
	type?: XDDExtractionType;
	ignoreBytes?: boolean;
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
	[PAGES]: 'Number of Pages',
	[YEAR]: 'Publication Year'
};

export const FACET_FIELDS: string[] = [TYPE, JOURNAL, PUBLISHER, YEAR];
