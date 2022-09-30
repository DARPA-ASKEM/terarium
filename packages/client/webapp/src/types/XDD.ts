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
	journal: string; //
	known_terms: XDDArticleKnownTerms[]; //
	link: XDDArticleLink[];
	number: string;
	pages: string;
	publisher: string; //
	title: string; // name
	type: string;
	volume: string;
	year: string;
	_gddid: string;
};

export type XDDResult = {
	success: { data: XDDArticle[] };
};
