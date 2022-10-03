import { Datacube } from './Datacube';
import { XDDArticle } from './XDD';

export type XDDSearchParams = {
	known_terms?: string[];
	dataset?: string | null;
};

export type DatacubeSearchParams = {
	country?: string;
};

export type SearchParameters = {
	xdd?: XDDSearchParams;
	datacubes?: DatacubeSearchParams;
};

export type ResultType = Datacube | XDDArticle;

export type SearchResults = {
	results: ResultType[];
	searchSubsystem: string;
};
