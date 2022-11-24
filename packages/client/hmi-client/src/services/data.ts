import { uniqBy } from 'lodash';
import { Facets, ResourceType, SearchParameters, SearchResults } from '@/types/common';
import API from '@/api/api';
import { getModelFacets } from '@/utils/facets';
import { applyFacetFiltersToModels } from '@/utils/data-util';
import { Model, ModelSearchParams, MODEL_FILTER_FIELDS } from '../types/Model';
import {
	XDDArticle,
	XDDArtifact,
	XDDDictionary,
	XDDResult,
	XDDSearchParams,
	XDD_RESULT_DEFAULT_PAGE_SIZE
} from '../types/XDD';

const getXDDSets = async () => {
	const res = await API.get('/xdd/sets');
	const response: XDDResult = res.data;
	return response.available_sets || ([] as string[]);
};

const getXDDDictionaries = async () => {
	const res = await API.get('/xdd/dictionaries');
	const rawdata: XDDResult = res.data;
	if (rawdata.success) {
		const { data } = rawdata.success;
		return data;
	}
	return [] as XDDDictionary[];
};

const getModels = async (term: string, modelSearchParam?: ModelSearchParams) => {
	const finalModels: Model[] = [];

	//
	// fetch list of models data from the HMI server
	//
	const res = await API.get('/models');
	const modelsList: Model[] = res.data;

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

	const modelResults = term.length > 0 ? uniqBy(finalModels, 'id') : allModels;

	if (modelSearchParam && modelSearchParam.filters) {
		// modelSearchParam currently represent facets filters that can be applied
		//  to further refine the list of models
		applyFacetFiltersToModels(modelResults, modelSearchParam.filters);
	}

	// FIXME: this client-side computation of facets from "models" data should be done
	//        at the HMI server
	const modelFacets = getModelFacets(modelResults);

	return {
		results: modelResults,
		searchSubsystem: ResourceType.MODEL,
		facets: modelFacets
	};
};

//
// fetch list of extractions data from the HMI server
//
const getXDDArtifacts = async (doc_doi: string) => {
	const url = `/xdd/extractions?doi=${doc_doi}`;

	// NOT SUPPORTED
	// if (xddSearchParam?.type) {
	// 	// restrict the type of object to search for
	// 	url += `&type=${xddSearchParam.type}`;
	// }
	// if (xddSearchParam?.ignoreBytes) {
	// 	// by default ignore including artifact bytes (e.g., figures base64 bytes)
	// 	url += `&ignore_bytes=${xddSearchParam.ignoreBytes}`;
	// }

	const res = await await API.get(url);
	const rawdata: XDDResult = res.data;

	if (rawdata.success) {
		const { data } = rawdata.success;
		const artifacts = data as XDDArtifact[];
		// TEMP: the following mapping is needed because the backend is returning raw xdd response
		return artifacts.map((a) => ({ ...a, askemClass: a.ASKEM_CLASS }));
	}

	return [] as XDDArtifact[];
};

//
// fetch list of related documented utilizing
//  semantic similarity (i.e., document embedding) from XDD via the HMI server
//
const getRelatedDocuments = async (doc_doi: string, dataset: string | null) => {
	if (doc_doi === '' || dataset === null) {
		return [] as XDDArticle[];
	}

	// https://xdd.wisc.edu/sets/xdd-covid-19/doc2vec/api/similar?doi=10.1002/pbc.28600
	// dataset=xdd-covid-19
	// doi=10.1002/pbc.28600
	const url = `/xdd/related/document?doi=${doc_doi}&set=${dataset}`;

	const res = await await API.get(url);
	const rawdata: XDDResult = res.data;

	if (rawdata.data) {
		const articlesRaw = rawdata.data.map((a) => a.bibjson);

		// TEMP: since the backend has a bug related to applying mapping, the field "abstractText"
		//       is not populated and instead the raw field name, abstract, is the one with data
		//       similarly, re-map the gddid field
		const articles = articlesRaw.map((a) => ({
			...a,
			abstractText: a.abstract,
			// eslint-disable-next-line no-underscore-dangle
			gddid: a._gddid,
			knownTerms: a.known_terms
		}));

		return articles;
	}
	return [] as XDDArticle[];
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
	let url = `/xdd/documents?term=${term}`;

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
	if (xddSearchParam?.min_published) {
		url += `&min_published=${xddSearchParam.min_published}`;
	}
	if (xddSearchParam?.max_published) {
		url += `&max_published=${xddSearchParam.max_published}`;
	}
	if (xddSearchParam?.pubname) {
		url += `&pubname=${xddSearchParam.pubname}`;
	}
	if (xddSearchParam?.publisher) {
		url += `&publisher=${xddSearchParam.publisher}`;
	}
	if (enablePagination) {
		url += '&full_results';
	} else {
		// request results to be ranked
		url += '&include_score=true';
	}
	if (xddSearchParam?.facets) {
		url += '&facets=true';
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

	const res = await API.get(url);
	const rawdata: XDDResult = res.data;

	if (rawdata.success) {
		const { data, hits, scrollId, nextPage, facets } = rawdata.success;
		const articlesRaw = data as XDDArticle[];

		// TEMP: since the backend has a bug related to applying mapping, the field "abstractText"
		//       is not populated and instead the raw field name, abstract, is the one with data
		//       similarly, re-map the gddid field
		const articles = articlesRaw.map((a) => ({
			...a,
			abstractText: a.abstract,
			// eslint-disable-next-line no-underscore-dangle
			gddid: a._gddid,
			knownTerms: a.known_terms
		}));

		const formattedFacets: Facets = {};
		if (facets) {
			// we receive facets data, so make sure it is in the proper format
			const facetKeys = Object.keys(facets);
			facetKeys.forEach((facetKey) => {
				formattedFacets[facetKey] = facets[facetKey].buckets.map((e) => ({
					key: e.key,
					value: e.doc_count
				}));
			});
		}

		return {
			results: articles,
			facets: formattedFacets,
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
			resolve(getModels(term, searchParam?.model));
		} catch (err: any) {
			reject(new Error(`Error fetching models results: ${err}`));
		}
	});

	// fetch results from all search subsystems in parallel
	const responses = await Promise.all([promise1, promise2]);
	return responses as SearchResults[];
};

export {
	fetchData,
	getXDDSets,
	getXDDDictionaries,
	getXDDArtifacts,
	searchXDDArticles,
	getRelatedDocuments
};
