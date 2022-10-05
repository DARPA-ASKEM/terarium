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
	abstract: string; // description
	author: XDDArticleAuthor[];
	identifier: XDDArticleIdentifier[];
	journal: string;
	known_terms?: XDDArticleKnownTerms[];
	link: XDDArticleLink[];
	number: string;
	pages: string;
	publisher: string;
	title: string; // name
	type: string;
	volume: string;
	year: string;
	_gddid: string;
};

export type XDDResult = {
	success?: {
		data: XDDArticle[];
		// URL to fetch next page results
		// https://xdd.wisc.edu/api/articles?&include_score=true&per_page=100&term=abbott&publisher=USGS&full_results
		// "https://xdd.wisc.edu/api/articles?scroll_id=a5e403ac-76b9-4400-94bb-59c9e3e030d6"
		next_page?: string;
		scrollId?: string;
		hits?: number;
	};
	error?: {
		message: string;
	};
};
