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
	documentBibjson: DocumentType; // the embedded document metadata wherein this artifact is extracted
};

// XDD extraction object, which should match Extraction.java at the backend
export type XDDArtifact = {
	askemClass: string;
	properties: XDDArtifactProperties;
	askemId: string;
	xddCreated: Date;
	xddRegistrant: number;
	highlight: string[];
};

export type XDDUrlExtraction = {
	url: string;
	resourceTitle: string;
	extractedFrom: string[];
};

export type DocumentAuthor = {
	name: string;
};

export type DocumentIdentifier = {
	type: string;
	id: string;
};

export type DocumentLink = {
	type: string;
	url: string;
};

export type DocumentKnownTerms = {
	[term: string]: string[];
};

export type DocumentKnownEntity = {
	urlExtractions: XDDUrlExtraction[];
	summaries: {
		sections: { [key: string]: string };
	};
};

export type DocumentType = {
	abstractText: string; // mapped from abstract
	abstract: string;
	author: DocumentAuthor[];
	identifier: DocumentIdentifier[];
	journal: string;
	knownTerms?: DocumentKnownTerms[];
	link: DocumentLink[];
	number: string;
	pages: string;
	publisher: string;
	title: string; // name
	type: string;
	volume: string;
	year: string;
	gddId: string;
	highlight: string[];
	githubUrls: string[];
	knownEntities?: DocumentKnownEntity;

	// We don't know exactly what is in citationList - DC Jan 2023
	citationList: { [key: string]: string }[];

	// additional-client-side fields
	relatedDocuments?: Document[];
	relatedExtractions?: XDDArtifact[];
};
