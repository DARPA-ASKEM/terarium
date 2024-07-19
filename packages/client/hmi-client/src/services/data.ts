import { DATASET_FILTER_FIELDS, DatasetSearchParams } from '@/types/Dataset';
import { Dataset, DocumentAsset, Model, ProvenanceType } from '@/types/Types';
import { FullSearchResults, ResourceType, ResultType, SearchParameters, SearchResults } from '@/types/common';
import { DatasetSource } from '@/types/search';
import { applyFacetFilters, isDataset, isModel } from '@/utils/data-util';
import { isEmpty, uniqBy } from 'lodash';
import { ID, MODEL_FILTER_FIELDS, ModelSearchParams } from '../types/Model';
import * as DatasetService from './dataset';
import { getAllModelDescriptions } from './model';
// eslint-disable-next-line import/no-cycle
import { getRelatedArtifacts } from './provenance';

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
	searchParam: ModelSearchParams & DatasetSearchParams;
}

const getAssets = async (params: GetAssetsParams) => {
	// Get paramaters as an interface allowing us to provide optional fields accurately with names rather than positions
	const term = params.term;
	const resourceType = params.resourceType;
	const searchParam = params.searchParam;

	const results = {} as FullSearchResults;

	// fetch list of model or datasets data from the HMI server
	let assetList: Model[] | Dataset[] | Document[] = [];

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
		default:
			return results; // error or make new resource type compatible
	}

	// needed?
	const allAssets: ResultType[] = assetList.map((a: Model | Dataset | DocumentAsset) => ({
		...a
	}));

	// FIXME: this client-side computation of facets from "models" data should be done
	//        at the HMI server
	//
	// This is going to calculate facets aggregations from the list of results

	const assetResults: ResultType[] =
		resourceType === ResourceType.DATASET && searchParam.source === DatasetSource.ESGF
			? allAssets
			: filterAssets(allAssets, resourceType, term);

	// TODO: xdd facets are now driven by the back end, however our other facets are not (and are also unused?)
	/* let assetFacets:Facets;
	switch (resourceType) {
		case ResourceType.MODEL:
			assetFacets = getModelFacets(assetResults as Model[]); // will be moved to HMI server - keep this for now
			break;
		case ResourceType.DATASET:
			assetFacets = getDatasetFacets(assetResults as Dataset[]); // will be moved to HMI server - keep this for now
			break;
		default:
			return results; // error or make new resource type compatible
	}

	results.allData = {
		results: assetResults,
		searchSubsystem: resourceType,
		hits
	}; */

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

			/* let assetFacetsFiltered: Facets;
			switch (resourceType) {
				case ResourceType.MODEL:
					assetFacetsFiltered = getModelFacets(assetResults as Model[]);
					break;
				case ResourceType.DATASET:
					assetFacetsFiltered = getDatasetFacets(assetResults as Dataset[]);
					break;
				default:
					return results; // error or make new resource type compatible
			} */

			results.allDataFilteredWithFacets = {
				results: assetResults,
				searchSubsystem: resourceType
			};
		} else {
			results.allDataFilteredWithFacets = results.allData;
		}
	} else {
		results.allDataFilteredWithFacets = results.allData;
	}

	return results;
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
		if (searchParam?.model?.related_search_enabled || searchParam?.dataset?.related_search_enabled) {
			let relatedArtifacts: ResultType[] = [];
			//
			// search by example
			//
			// FIXME: no facets support when search by example is executed
			// FIXME: no concepts support when search by example is executed

			// are we executing a search-by-example
			// (i.e., to find related artifacts for a given model)?
			if (searchParam?.model && searchParam?.model.related_search_id) {
				relatedArtifacts = await getRelatedArtifacts(searchParam?.model.related_search_id, ProvenanceType.Model);
			}

			// are we executing a search-by-example
			// (i.e., to find related artifacts for a given dataset)?
			if (searchParam?.dataset && searchParam?.dataset.related_search_id) {
				relatedArtifacts = await getRelatedArtifacts(searchParam?.dataset.related_search_id, ProvenanceType.Dataset);
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
			promiseList.push(fetchResource(term, resourceType as ResourceType, searchParamWithFacetFilters));
		}
	}

	// fetch results from all search subsystems in parallel
	const responses = await Promise.all(promiseList);
	finalResponse.allData = responses.map((r) => r.allData);
	finalResponse.allDataFilteredWithFacets = responses.map((r) => r.allDataFilteredWithFacets);

	return finalResponse;
};

export { fetchData, getAssets };
