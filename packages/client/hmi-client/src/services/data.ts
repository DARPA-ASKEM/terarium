import { uniqBy } from 'lodash';
import { ResourceType, SearchParameters, SearchResults } from '@/types/common';
import { uncloak } from '@/utils/uncloak';
import { Model, MODEL_FILTER_FIELDS } from '../types/Model';
import {
	XDDArticle,
	XDDArtifact,
	XDDDictionary,
	XDDResult,
	XDDSearchParams,
	XDD_RESULT_DEFAULT_PAGE_SIZE
} from '../types/XDD';

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

const getModels = async (term: string) => {
	const finalModels: Model[] = [];

	//
	// fetch list of models data from the HMI server
	//
	const modelsList: Model[] = await uncloak('/api/models');

	// TEMP: add "type" field because it is needed to mark these resources as models
	// FIXME: dependecy on type model should be removed and another "sub-system" or "result-type"
	//        should be added for datasets and other resource types
	const allModels = modelsList.map((m) => ({ ...m, type: 'model' }));

	//
	// simulate applying filters to the model query
	//
	const ModelFilterAttributes = MODEL_FILTER_FIELDS;
	if (term.length > 0) {
		ModelFilterAttributes.forEach((modelAttr) => {
			const resultsAsModels = allModels;
			const items = resultsAsModels.filter((d) =>
				(d[modelAttr as keyof Model] as string).toLowerCase().includes(term)
			);
			finalModels.push(...items);
		});
	}

	return {
		results: term.length > 0 ? uniqBy(finalModels, 'id') : allModels,
		searchSubsystem: ResourceType.MODEL
	};
};

//
// fetch list of extractions data from the HMI server
//
const getXDDArtifacts = async (doc_doi: string) => {
	const url = `/api/xdd/extractions?doi=${doc_doi}`;

	// NOT SUPPORTED
	// if (xddSearchParam?.type) {
	// 	// restrict the type of object to search for
	// 	url += `&type=${xddSearchParam.type}`;
	// }
	// if (xddSearchParam?.ignoreBytes) {
	// 	// by default ignore including artifact bytes (e.g., figures base64 bytes)
	// 	url += `&ignore_bytes=${xddSearchParam.ignoreBytes}`;
	// }

	const rawdata: XDDResult = await await uncloak(url);

	if (rawdata.success) {
		const { data } = rawdata.success;
		const artifacts = data as XDDArtifact[];
		// TEMP: the following mapping is needed because the backend is returning raw xdd response
		return artifacts.map((a) => ({ ...a, askemClass: a.ASKEM_CLASS }));
	}

	return [] as XDDArtifact[];
};

const searchXDDArticles = async (term: string, xddSearchParam?: XDDSearchParams) => {
	const limitResultsCount = xddSearchParam?.perPage ?? XDD_RESULT_DEFAULT_PAGE_SIZE;

	// NOTE when true it disables ranking of results
	const enablePagination = xddSearchParam?.fullResults ?? false;

	// "full_results": "Optional. When this parameter is included (no value required),
	//  an overview of total number of matching articles is returned,
	//  with a scan-and-scroll cursor that allows client to step through all results page-by-page.
	//  NOTE: the "max" parameter will be ignored
	//  NOTE: results may not be ranked in this mode
	let url = `/api/xdd/documents?term=${term}`;

	if (xddSearchParam?.doi) {
		url += `&doi=${xddSearchParam.doi}`;
	}
	if (xddSearchParam?.title) {
		url += `&title=${xddSearchParam.title}`;
	}
	if (xddSearchParam?.dataset) {
		url += `&dataset=${xddSearchParam.dataset}`;
	}
	if (xddSearchParam?.dict && xddSearchParam?.dict.length > 0) {
		url += `&dict=${xddSearchParam.dict.join(',')}`;
	}
	if (enablePagination) {
		url += '&full_results';
	} else {
		// request results to be ranked
		url += '&include_score=true';
	}

	// what about doi and title

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
	const rawdata: XDDResult = await uncloak(url);

	if (rawdata.success) {
		const { data, hits, scrollId, nextPage } = rawdata.success;
		const articlesRaw = data as XDDArticle[];

		// TEMP: since the backend has a bug related to applying mapping, the field "abstractText"
		//       is not populated and instead the raw field name, abstract, is the one with data
		//       similarly, re-map the gddid field
		// eslint-disable-next-line no-underscore-dangle
		const articles = articlesRaw.map((a) => ({
			...a,
			abstractText: a.abstract,
			gddid: a._gddid,
			knownTerms: a.known_terms
		}));

		return {
			results: articles,
			searchSubsystem: ResourceType.XDD,
			hits,
			hasMore: scrollId !== null && scrollId !== '',
			nextPage
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
			resolve(getModels(term));
		} catch (err: any) {
			reject(new Error(`Error fetching models results: ${err}`));
		}
	});

	// fetch results from all search subsystems in parallel
	const responses = await Promise.all([promise1, promise2]);
	return responses as SearchResults[];
};

export { fetchData, getXDDSets, getXDDDictionaries, getXDDArtifacts };
