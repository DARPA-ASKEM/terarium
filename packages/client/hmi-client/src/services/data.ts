import API from '@/api/api';
import { DATASET_FILTER_FIELDS, DatasetSearchParams } from '@/types/Dataset';
import {
	Dataset,
	Document,
	DocumentAsset,
	DocumentsResponseOK,
	Extraction,
	Model,
	ProvenanceType,
	XDDFacetsItemResponse
} from '@/types/Types';
import {
	XDDExtractionType,
	XDDResult,
	XDDSearchParams,
	XDD_RESULT_DEFAULT_PAGE_SIZE
} from '@/types/XDD';
import {
	Facets,
	FullSearchResults,
	ResourceType,
	ResultType,
	SearchParameters,
	SearchResults
} from '@/types/common';
import { DatasetSource } from '@/types/search';
import { applyFacetFilters, isDataset, isDocument, isModel } from '@/utils/data-util';
import { getDatasetFacets, getModelFacets } from '@/utils/facets';
import { logger } from '@/utils/logger';
import { isEmpty, uniqBy } from 'lodash';
import { ID, MODEL_FILTER_FIELDS, ModelSearchParams } from '../types/Model';
import * as DatasetService from './dataset';
import { getAllModelDescriptions } from './model';
// eslint-disable-next-line import/no-cycle
import { getRelatedArtifacts } from './provenance';

/**
 * fetch list of extractions data from the HMI server
 */
const getXDDArtifacts = async (
	term: string,
	extractionTypes?: XDDExtractionType[]
): Promise<Extraction[]> => {
	let url = '/document/extractions?';
	url += `term=${term}`;
	url += '&include_highlights=true';

	if (extractionTypes) {
		url += '&ASKEM_CLASS=';
		for (let i = 0; i < extractionTypes.length; i++) {
			url += `${extractionTypes[i]},`;
		}
	}

	const res = await API.get(url);
	if (res) {
		const rawdata: XDDResult = res.data;
		if (rawdata && rawdata.success) return rawdata.success.data as Extraction[];
	}

	return [] as Extraction[];
};

const searchXDDDocuments = async (
	term: string,
	xddSearchParam?: XDDSearchParams
): Promise<DocumentsResponseOK | undefined> => {
	const limitResultsCount = xddSearchParam?.perPage ?? XDD_RESULT_DEFAULT_PAGE_SIZE;

	// NOTE when true it disables ranking of results
	const enablePagination = xddSearchParam?.fullResults ?? false;

	// "full_results": "Optional. When this parameter is included (no value required),
	//  an overview of total number of matching documents is returned,
	//  with a scan-and-scroll cursor that allows client to step through all results page-by-page.
	//  NOTE: the "max" parameter will be ignored
	//  NOTE: results may not be ranked in this mode
	let searchParams = `term=${term}`;
	const url = '/documents?';

	if (xddSearchParam?.docid) {
		searchParams += `&docid=${xddSearchParam.docid}`;
	}
	if (xddSearchParam?.doi) {
		searchParams += `&doi=${xddSearchParam.doi}`;
	}
	if (xddSearchParam?.dataset) {
		searchParams += `&dataset=${xddSearchParam.dataset}`;
	}
	if (xddSearchParam?.fields) {
		searchParams += `&fields=${xddSearchParam.fields}`;
	}
	if (xddSearchParam?.dict && !isEmpty(xddSearchParam.dict)) {
		searchParams += `&dict=${xddSearchParam.dict.join(',')}`;
	}
	if (xddSearchParam?.min_published) {
		searchParams += `&min_published=${xddSearchParam.min_published}`;
	}
	if (xddSearchParam?.max_published) {
		searchParams += `&max_published=${xddSearchParam.max_published}`;
	}
	if (xddSearchParam?.pubname) {
		searchParams += `&pubname=${xddSearchParam.pubname}`;
	}
	if (xddSearchParam?.publisher) {
		searchParams += `&publisher=${xddSearchParam.publisher}`;
	}
	if (xddSearchParam?.includeHighlights) {
		searchParams += '&include_highlights=true';
	}
	if (xddSearchParam?.inclusive) {
		searchParams += '&inclusive=true';
	}

	if (xddSearchParam?.facets) {
		searchParams += '&facets=true';
	}

	if (xddSearchParam?.githubUrls) {
		searchParams += `&github_url=${xddSearchParam.githubUrls}`;
	}

	// search title and abstract when performing term-based search if requested
	if (term !== '' && xddSearchParam?.additional_fields) {
		searchParams += `&additional_fields=${xddSearchParam?.additional_fields}`;
	}

	// utilize ES improved matching
	if (term !== '' && xddSearchParam?.match) {
		searchParams += '&match=true';
	}

	if (xddSearchParam?.known_entities) {
		searchParams += `&known_entities=${xddSearchParam?.known_entities}`;
	}

	if (xddSearchParam?.similar_to) {
		searchParams += `&similar_to=${xddSearchParam?.similar_to}`;
	}
	if (enablePagination) {
		searchParams += '&full_results';
	} else {
		// request results to be ranked
		searchParams += '&include_score=true';
	}
	//
	// "max": "Maximum number of documents to return (default is all)",
	searchParams += `&max=${limitResultsCount}`;

	// "per_page": "Maximum number of results to include in one response.
	//  Applies to full_results pagination or single-page requests.
	//  NOTE: Due to internal mechanisms, actual number of results will be this parameter,
	//        floor rounded to a multiple of 25."
	searchParams += `&per_page=${limitResultsCount}`;

	// url = 'https://xdd.wisc.edu/api/articles?&include_score=true&max=25&term=abbott&publisher=USGS&full_results';

	// this will give error if "max" param is not included since the result is too large
	//  either set "max"
	//  or use the "full_results" which automatically sets a default of 500 per page (per_page)
	// url = 'https://xdd.wisc.edu/api/articles?dataset=xdd-covid-19&term=covid&include_score=true&full_results'

	const res = await API.get(url + searchParams);

	return res?.data?.success ?? null;
};

const filterAssets = (allAssets: ResultType[], resourceType: ResourceType, term: string) => {
	if (term.length > 0) {
		// simulate applying filters
		const AssetFilterAttributes: string[] =
			resourceType === ResourceType.MODEL ? MODEL_FILTER_FIELDS : DATASET_FILTER_FIELDS; // maybe turn into switch case when other resource types have to go through here

		let finalAssets: ResultType[] = [];

		AssetFilterAttributes.forEach((attribute) => {
			finalAssets = allAssets.filter((d) => {
				const searchTarget = resourceType === ResourceType.MODEL ? (d as Model).header : d;

				if (searchTarget[attribute])
					return (searchTarget[attribute] as string).toLowerCase().includes(term.toLowerCase());
				return '';
			});
		});

		return uniqBy(finalAssets, ID);
	}
	return allAssets;
};

interface GetAssetsParams {
	term: string;
	resourceType: ResourceType;
	searchParam: ModelSearchParams & DatasetSearchParams & XDDSearchParams;
}

const getAssets = async (params: GetAssetsParams) => {
	// Get paramaters as an interface allowing us to provide optional fields accurately with names rather than positions
	const term = params.term;
	const resourceType = params.resourceType;
	const searchParam = params.searchParam;

	const results = {} as FullSearchResults;

	// fetch list of model or datasets data from the HMI server
	let assetList: Model[] | Dataset[] | Document[] = [];
	let xddResults: DocumentsResponseOK | undefined;
	let hits: number | undefined;

	switch (resourceType) {
		case ResourceType.MODEL:
			assetList = (await getAllModelDescriptions()) ?? ([] as Model[]);
			break;
		case ResourceType.DATASET:
			if (searchParam.source === DatasetSource.TERARIUM)
				assetList = (await DatasetService.getAll()) ?? ([] as Dataset[]);
			else if (searchParam.source === DatasetSource.ESGF)
				assetList = (await DatasetService.searchClimateDatasets(term)) ?? ([] as Dataset[]);
			break;
		case ResourceType.XDD:
			xddResults = await searchXDDDocuments(term, searchParam);
			if (xddResults) {
				assetList = xddResults.data;
				hits = xddResults.hits;
			}
			break;
		default:
			return results; // error or make new resource type compatible
	}

	// needed?
	const allAssets: ResultType[] = assetList.map(
		(a: Model | Dataset | Document | DocumentAsset) => ({
			...a
		})
	);

	// FIXME: this client-side computation of facets from "models" data should be done
	//        at the HMI server
	//
	// This is going to calculate facets aggregations from the list of results

	const assetResults: ResultType[] =
		resourceType === ResourceType.XDD ||
		(resourceType === ResourceType.DATASET && searchParam.source === DatasetSource.ESGF)
			? allAssets
			: filterAssets(allAssets, resourceType, term);

	// TODO: xdd facets are now driven by the back end, however our other facets are not (and are also unused?)
	let assetFacets: { [index: string]: XDDFacetsItemResponse } | Facets;
	switch (resourceType) {
		case ResourceType.MODEL:
			assetFacets = getModelFacets(assetResults as Model[]); // will be moved to HMI server - keep this for now
			break;
		case ResourceType.DATASET:
			assetFacets = getDatasetFacets(assetResults as Dataset[]); // will be moved to HMI server - keep this for now
			break;
		case ResourceType.XDD:
			assetFacets = xddResults?.facets ?? {};
			break;
		default:
			return results; // error or make new resource type compatible
	}

	results.allData = {
		results: assetResults,
		searchSubsystem: resourceType,
		facets: assetFacets,
		hits
	};

	// apply facet filters
	if (resourceType === ResourceType.MODEL || resourceType === ResourceType.DATASET) {
		// Filtering for model/dataset data
		if (searchParam && searchParam.filters && !isEmpty(searchParam?.filters?.clauses)) {
			applyFacetFilters(assetResults, searchParam.filters, resourceType);

			// remove any previously added concept/id filters
			searchParam.filters.clauses = searchParam.filters.clauses.filter((c) => c.field !== ID);

			// FIXME: this client-side computation of facets from "models" data should be done
			//        at the HMI server
			//
			// This is going to calculate facets aggregations from the list of results
			let assetFacetsFiltered: Facets;
			switch (resourceType) {
				case ResourceType.MODEL:
					assetFacetsFiltered = getModelFacets(assetResults as Model[]);
					break;
				case ResourceType.DATASET:
					assetFacetsFiltered = getDatasetFacets(assetResults as Dataset[]);
					break;
				default:
					return results; // error or make new resource type compatible
			}

			results.allDataFilteredWithFacets = {
				results: assetResults,
				searchSubsystem: resourceType,
				facets: assetFacetsFiltered
			};
		} else {
			results.allDataFilteredWithFacets = results.allData;
		}
	} else if (resourceType === ResourceType.XDD) {
		// Set values
		const newFacets: { [p: string]: XDDFacetsItemResponse } = xddResults ? xddResults.facets : {};
		results.allDataFilteredWithFacets = {
			results: xddResults ? xddResults.data : [],
			searchSubsystem: resourceType,
			facets: newFacets
		};
	} else {
		results.allDataFilteredWithFacets = results.allData;
	}

	return results;
};

/**
 * fetch list of related documented based on the given document ID
 */
const getRelatedDocuments = async (docid: string): Promise<Document[]> => {
	if (docid !== '') {
		const { status, data } = await API.get(`/documents?max=8&similar_to=${docid}`);
		if (status === 200 && data) {
			return data?.success?.data ?? ([] as Document[]);
		}
		if (status === 204) {
			logger.error('Request received successfully, but there are no documents');
		}
	}
	return [] as Document[];
};

async function getRelatedTerms(query?: string, dataset?: string | null): Promise<string[]> {
	if (!query) {
		return [];
	}
	const params = new URLSearchParams({ set: dataset ?? 'xdd-covid-19', word: query });
	const response = await API.get(`/document/related/word?${params}`);
	const data = response?.data?.data;
	return data ? data.map((tuple) => tuple[0]).slice(0, 5) : [];
}

const getAutocomplete = async (searchTerm: string) => {
	const url = `/document/extractions/askem-autocomplete/${searchTerm}`;
	const response = await API.get(url);
	if (response.status === 204) return [];
	return response.data ?? [];
};

const getDocumentById = async (docid: string): Promise<Document | null> => {
	const searchParams: XDDSearchParams = {
		docid,
		known_entities: 'askem_object,url_extractions,summaries'
	};
	const xddRes: DocumentsResponseOK | undefined = await searchXDDDocuments('', searchParams);
	if (xddRes) {
		const documents: Document[] = xddRes.data;
		if (documents.length > 0) {
			return documents[0];
		}
	}
	return null;
};

const getBulkXDDDocuments = async (docIDs: string[]) => {
	const result: Document[] = [];
	const promiseList = [] as Promise<Document | null>[];
	docIDs.forEach((docId) => {
		promiseList.push(getDocumentById(docId));
	});
	const responsesRaw = await Promise.all(promiseList);
	responsesRaw.forEach((r) => {
		if (r) {
			result.push(r);
		}
	});
	return result;
};

const fetchResource = async (
	term: string,
	resourceType: ResourceType,
	searchParamWithFacetFilters?: SearchParameters
): Promise<FullSearchResults> =>
	// eslint-disable-next-line no-async-promise-executor
	new Promise<FullSearchResults>(async (resolve, reject) => {
		try {
			resolve(
				getAssets({
					term,
					resourceType,
					searchParam: searchParamWithFacetFilters?.[resourceType]
				})
			);
		} catch (err: any) {
			reject(new Error(`Error fetching ${resourceType} results: ${err}`));
		}
	});

const fetchData = async (
	term: string,
	searchParam?: SearchParameters,
	searchParamWithFacetFilters?: SearchParameters,
	resourceType?: string
) => {
	const finalResponse = {
		allData: [],
		allDataFilteredWithFacets: []
	} as {
		allData: SearchResults[];
		allDataFilteredWithFacets: SearchResults[];
	};

	//
	// normal search flow continue here
	//
	const promiseList = [] as Promise<FullSearchResults>[];

	if (resourceType) {
		if (
			searchParam?.xdd?.similar_search_enabled ||
			searchParam?.xdd?.related_search_enabled ||
			searchParam?.model?.related_search_enabled ||
			searchParam?.dataset?.related_search_enabled
		) {
			let relatedArtifacts: ResultType[] = [];
			//
			// search by example
			//
			// FIXME: no facets support when search by example is executed
			// FIXME: no concepts support when search by example is executed
			// xDD does not provide facets data when using doc2vec API for fetching related documents!

			// are we executing a search-by-example
			// (i.e., to find similar documents or related artifacts for a given document)?
			if (searchParam.xdd && searchParam?.xdd.dataset) {
				if (searchParam?.xdd.similar_search_enabled && searchParam?.xdd.related_search_id) {
					const response = await fetchResource('', resourceType as ResourceType, {
						...searchParamWithFacetFilters,
						xdd: {
							...searchParamWithFacetFilters?.xdd,
							similar_to: searchParam.xdd.related_search_id as string,
							max: 100,
							perPage: 100
						}
					});
					finalResponse.allData.push(response.allData);
					finalResponse.allDataFilteredWithFacets.push(response.allDataFilteredWithFacets);
				}
			}

			// are we executing a search-by-example
			// (i.e., to find related artifacts for a given model)?
			if (searchParam?.model && searchParam?.model.related_search_id) {
				relatedArtifacts = await getRelatedArtifacts(
					searchParam?.model.related_search_id,
					ProvenanceType.Model
				);
			}

			// are we executing a search-by-example
			// (i.e., to find related artifacts for a given dataset)?
			if (searchParam?.dataset && searchParam?.dataset.related_search_id) {
				relatedArtifacts = await getRelatedArtifacts(
					searchParam?.dataset.related_search_id,
					ProvenanceType.Dataset
				);
			}

			// parse retrieved related artifacts and make them ready for consumption by the explorer
			if (relatedArtifacts.length > 0) {
				//
				// models
				//
				const relatedModels = relatedArtifacts.filter((a) => isModel(a));
				const relatedModelsSearchResults: SearchResults = {
					results: relatedModels,
					searchSubsystem: ResourceType.MODEL
				};
				finalResponse.allData.push(relatedModelsSearchResults);
				finalResponse.allDataFilteredWithFacets.push(relatedModelsSearchResults);

				//
				// datasets
				//
				const relatedDatasets = relatedArtifacts.filter((a) => isDataset(a));
				const relatedDatasetSearchResults: SearchResults = {
					results: relatedDatasets,
					searchSubsystem: ResourceType.DATASET
				};
				finalResponse.allData.push(relatedDatasetSearchResults);
				finalResponse.allDataFilteredWithFacets.push(relatedDatasetSearchResults);

				//
				// Documents
				//
				const relatedDocuments = relatedArtifacts.filter((a) => isDocument(a));
				const relatedDocumentsSearchResults: SearchResults = {
					results: relatedDocuments,
					searchSubsystem: ResourceType.XDD
				};
				finalResponse.allData.push(relatedDocumentsSearchResults);
				finalResponse.allDataFilteredWithFacets.push(relatedDocumentsSearchResults);
			}

			return finalResponse;
		}

		//
		// normal search flow continue here
		//
		if (resourceType === ResourceType.ALL) {
			Object.entries(ResourceType).forEach(async ([key]) => {
				if (ResourceType[key] !== ResourceType.ALL) {
					promiseList.push(fetchResource(term, ResourceType[key], searchParamWithFacetFilters));
				}
			});
		} else if ((<any>Object).values(ResourceType).includes(resourceType)) {
			promiseList.push(
				fetchResource(term, resourceType as ResourceType, searchParamWithFacetFilters)
			);
		}
	}

	// fetch results from all search subsystems in parallel
	const responses = await Promise.all(promiseList);
	finalResponse.allData = responses.map((r) => r.allData);
	finalResponse.allDataFilteredWithFacets = responses.map((r) => r.allDataFilteredWithFacets);

	return finalResponse;
};

export {
	fetchData,
	getAssets,
	getAutocomplete,
	getBulkXDDDocuments,
	getDocumentById,
	getRelatedDocuments,
	getRelatedTerms,
	getXDDArtifacts,
	searchXDDDocuments
};
