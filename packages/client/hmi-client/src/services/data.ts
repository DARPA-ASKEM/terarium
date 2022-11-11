import { uniqBy } from 'lodash';
import { ResourceType, SearchParameters, SearchResults } from '@/types/common';
import { uncloak } from '@/utils/uncloak';
import { Model, ModelSearchParams } from '../types/Model';
import {
	XDDArticle,
	XDDArtifact,
	XDDDictionary,
	XDDResult,
	XDDSearchParams,
	XDD_RESULT_DEFAULT_PAGE_SIZE
} from '../types/XDD';

const HMI_SERVER_BASE_API_URL = 'http://localhost:8078/api';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const XDD_API_KEY = ''; // COSMOS_API_KEY
const ARTICLES_API_BASE = 'https://xdd.wisc.edu/api/articles';
const DATASET_API_URL = 'https://xdd.wisc.edu/sets';
const DICTIONARY_API_URL = 'https://xdd.wisc.edu/api/dictionaries?all';

// A unified method to execute an XDD fetch passing the API key and other header params as needed
const fetchXDD = async (url: string) => {
	const headers = new Headers();
	headers.append('Content-Type', 'application/json');
	return fetch(url, {
		// mode: 'no-cors',
		headers
	});
};

const getXDDSets = async () => {
	const response = await fetchXDD(DATASET_API_URL);
	const rawdata = await response.json();
	return rawdata.available_sets;
};

const getXDDDictionaries = async () => {
	const response = await fetchXDD(DICTIONARY_API_URL);
	const rawdata: XDDResult = await response.json();
	if (rawdata.success) {
		const { data } = rawdata.success;
		return data;
	}
	return [] as XDDDictionary[];
};

// eslint-disable-next-line no-unused-vars, @typescript-eslint/no-unused-vars
const getModels = async (term: string, _modelSearchParam?: ModelSearchParams) => {
	const finalModels: Model[] = [];

	//
	// fetch list of models data from the HMI server
	//
	const allModels: Model[] = await uncloak(`${HMI_SERVER_BASE_API_URL}/model`);

	//
	// simulate applying filters to the model query
	//
	const ModelFilterAttributes = ['name', 'description'];
	if (term.length > 0) {
		ModelFilterAttributes.forEach((modelAttr) => {
			const resultsAsModels = allModels;
			const items = resultsAsModels.filter((d) =>
				d[modelAttr as keyof Model].toLowerCase().includes(term)
			);
			finalModels.push(...items);
		});
	}

	return {
		results: term.length > 0 ? uniqBy(finalModels, 'id') : allModels,
		searchSubsystem: ResourceType.MODEL
	};
};

const getXDDArtifacts = async (doc_doi?: string, xddSearchParam?: XDDSearchParams) => {
	// COSMOS API URL starts similarly to the DATASET base URL
	let url = `${DATASET_API_URL}`;
	if (xddSearchParam?.dataset) {
		url += `/${xddSearchParam.dataset}/`;
	}
	// COSMOS API part
	url += 'cosmos/api/v3_beta/search?';
	// since COSMOS is a protected API, we MUST specify the api key
	url += `api_key=${XDD_API_KEY}`;

	// restrict the type of object to search for
	if (xddSearchParam?.type) {
		url += `&type=${xddSearchParam?.type}`;
	}

	// by default ignore including artifact bytes (e.g., figures base64 bytes)
	if (xddSearchParam?.ignore_bytes) {
		url += '&ignore_bytes';
	}

	// search against a specific document doi
	if (doc_doi) {
		url += `&doi=${doc_doi}`;
	}

	const response = await fetchXDD(url);
	const rawdata = await response.json();

	const { objects } = rawdata as { objects: XDDArtifact[] };
	return objects || ([] as XDDArtifact[]);
};

const searchXDDArticles = async (term: string, xddSearchParam?: XDDSearchParams) => {
	const limitResultsCount = xddSearchParam?.pageSize ?? XDD_RESULT_DEFAULT_PAGE_SIZE;

	// NOTE when true it disables ranking of results
	const enablePagination = xddSearchParam?.enablePagination ?? false;

	// "full_results": "Optional. When this parameter is included (no value required),
	//  an overview of total number of matching articles is returned,
	//  with a scan-and-scroll cursor that allows client to step through all results page-by-page.
	//  NOTE: the "max" parameter will be ignored
	//  NOTE: results may not be ranked in this mode
	let url = `${ARTICLES_API_BASE}?term=${term}`;
	if (xddSearchParam?.dataset) {
		url += `&dataset=${xddSearchParam.dataset}`;
	}
	if (xddSearchParam?.dict_names && xddSearchParam?.dict_names.length > 0) {
		url += `&dict=${xddSearchParam.dict_names.join(',')}`;
	}
	if (enablePagination) {
		url += '&full_results';
	} else {
		// request results to be ranked
		url += '&include_score=true';
	}

	// "max": "Maximum number of articles to return (default is all)",
	url += `&max=${limitResultsCount}`;

	// "per_page": "Maximum number of results to include in one response.
	//  Applies to full_results pagination or single-page requests.
	//  NOTE: Due to internal mechanisms, actual number of results will be this parameter,
	//        floor rounded to a multiple of 25."
	url += `&per_page=${limitResultsCount}`;

	// url = 'https://xdd.wisc.edu/api/articles?&include_score=true&max=25&term=abbott&publisher=USGS&full_results';

	// this will give error if "max" param is not included since the result is too large
	//  either set "max"
	//  or use the "full_results" which automatically sets a default of 500 per page (per_page)
	// url = 'https://xdd.wisc.edu/api/articles?dataset=xdd-covid-19&term=covid&include_score=true&full_results'

	// full_results
	const response = await fetchXDD(url);
	const rawdata: XDDResult = await response.json();

	if (rawdata.success) {
		// eslint-disable-next-line camelcase, @typescript-eslint/naming-convention
		const { data, hits, scrollId, next_page } = rawdata.success;
		return {
			results: data as XDDArticle[],
			searchSubsystem: ResourceType.XDD,
			hits,
			hasMore: scrollId !== null && scrollId !== '',
			// eslint-disable-next-line camelcase
			nextPage: next_page
		};
	}

	return {
		results: [] as XDDArticle[],
		searchSubsystem: ResourceType.XDD,
		hits: 0
	};
};

const fetchData = async (term: string, searchParam?: SearchParameters) => {
	//
	// call the different search sub-systems to retrieve results
	// ideally, all such subsystems should be registered in an array, which will force refactoring of the following code
	//

	// xdd
	const promise1 = new Promise<SearchResults>((resolve, reject) => {
		try {
			resolve(searchXDDArticles(term, searchParam?.xdd));
		} catch (err: any) {
			reject(new Error(`Error fetching XDD results: ${err}`));
		}
	});

	// models (e.g., for models)
	const promise2 = new Promise<SearchResults>((resolve, reject) => {
		try {
			resolve(getModels(term, searchParam?.model));
		} catch (err: any) {
			reject(new Error(`Error fetching models results: ${err}`));
		}
	});

	// fetch results from all search subsystems in parallel
	const responses = await Promise.all([promise1, promise2]);
	return responses as SearchResults[];
};

export { fetchData, getXDDSets, getXDDDictionaries, getXDDArtifacts };
