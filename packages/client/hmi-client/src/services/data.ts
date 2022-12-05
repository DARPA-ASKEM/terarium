import { cloneDeep, uniq, uniqBy } from 'lodash';
import { Facets, ResourceType, SearchParameters, SearchResults } from '@/types/common';
import API from '@/api/api';
import { getDatasetFacets, getModelFacets } from '@/utils/facets';
import { applyFacetFiltersToDatasets, applyFacetFiltersToModels } from '@/utils/data-util';
import { DATASETS, MODELS } from '@/types/Project';
import { CONCEPT_FACETS_FIELD } from '@/types/Concept';
import { Clause, ClauseValue } from '@/types/Filter';
import { Dataset, DatasetSearchParams, DATASET_FILTER_FIELDS } from '@/types/Dataset';
import { ID, Model, ModelSearchParams, MODEL_FILTER_FIELDS } from '../types/Model';
import {
	XDDArticle,
	XDDArtifact,
	XDDDictionary,
	XDDResult,
	XDDSearchParams,
	XDD_RESULT_DEFAULT_PAGE_SIZE
} from '../types/XDD';
import { getFacets as getConceptFacets } from './concept';
import * as DatasetService from './dataset';
import { getAllModelDescriptions } from './model';

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
	const modelsList = (await getAllModelDescriptions()) || ([] as Model[]);

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

	const modelResults = term.length > 0 ? uniqBy(finalModels, ID) : allModels;

	let conceptFacets = await getConceptFacets([MODELS]);

	if (modelSearchParam && modelSearchParam.filters) {
		// modelSearchParam currently represent facets filters that can be applied
		//  to further refine the list of models

		// a special facet related to ontology/DKG concepts needs to be transformed into
		//  some form of filters that can filter the list of models.
		// In this case, each concept has an associated list of model IDs that can be used to filter models
		//  so, we need to map the facet filters from field "concepts" to "id"

		// Each clause of 'concepts' should have another corresponding one with 'id'
		const curies = [] as ClauseValue[];
		const idClauses = [] as Clause[];
		modelSearchParam.filters.clauses.forEach((clause) => {
			if (clause.field === CONCEPT_FACETS_FIELD) {
				const idClause = cloneDeep(clause);
				idClause.field = 'id';
				const clauseValues = [] as ClauseValue[];
				idClause.values.forEach((conceptNameOrCurie) => {
					// find the corresponding model IDs
					if (conceptFacets !== null) {
						const matching = conceptFacets.results.filter(
							(conceptResult) =>
								conceptResult.name === conceptNameOrCurie ||
								conceptResult.curie === conceptNameOrCurie
						);
						// update the clause value by mapping concept/curie to model id
						clauseValues.push(...matching.map((m) => m.id));
						curies.push(...matching.map((m) => m.curie));
					}
				});
				idClause.values = clauseValues;
				idClauses.push(idClause);
			}
		});
		// NOTE that we need to merge all concept filters into a single ID filter
		if (idClauses.length > 0) {
			const finalIdClause = cloneDeep(idClauses[0]);
			const allIdValues = idClauses.map((c) => c.values).flat();
			finalIdClause.values = uniq(allIdValues);
			modelSearchParam.filters.clauses.push(finalIdClause);
		}

		applyFacetFiltersToModels(modelResults, modelSearchParam.filters);

		// remove any previously added concept/id filters
		modelSearchParam.filters.clauses = modelSearchParam.filters.clauses.filter(
			(c) => c.field !== ID
		);

		// ensure that concepts are re-created following the current filtered list of model results
		// e.g., if the user has applied other facet filters, e.g. selected some model by name
		// then we need to find corresponding curies to filter the concepts accordingly
		if (conceptFacets !== null) {
			// FIXME:
			// This step won't be needed if the concept facets API is able to receive filters as well
			// to only provide concept aggregations based on a filtered set of models rather than the full list of models
			const finalModelIDs = modelResults.map((m) => m.id);
			conceptFacets.results.forEach((conceptFacetResult) => {
				if (finalModelIDs.includes(conceptFacetResult.id)) {
					curies.push(conceptFacetResult.curie);
				}
			});
		}

		// re-create the concept facets if the user has applyied any concept filters
		const uniqueCuries = uniq(curies);
		if (uniqueCuries.length > 0) {
			conceptFacets = await getConceptFacets([MODELS], uniqueCuries);
		}
	}

	// FIXME: this client-side computation of facets from "models" data should be done
	//        at the HMI server
	//
	// This is going to calculate facets aggregations from the list of results
	const modelFacets = await getModelFacets(modelResults, conceptFacets);

	return {
		results: modelResults,
		searchSubsystem: ResourceType.MODEL,
		facets: modelFacets,
		rawConceptFacets: conceptFacets
	};
};

const getDatasets = async (term: string, datasetSearchParam?: DatasetSearchParams) => {
	const finalDatasets: Dataset[] = [];

	//
	// fetch list of datasets from the HMI server
	//
	const datasetsList = (await DatasetService.getAll()) || ([] as Dataset[]);

	// TEMP: add "type" field because it is needed to mark these resources as datasets
	// FIXME: dependecy on type dataset should be removed and another "sub-system" or "result-type"
	//        should be added for other resource types
	const allDatasets = datasetsList.map((d) => ({
		...d,
		temporalResolution: d.temporal_resolution,
		geospatialResolution: d.geospatial_resolution,
		type: 'dataset'
	}));

	//
	// simulate applying filters to the dataset query
	//
	const DatasetFilterAttributes = DATASET_FILTER_FIELDS;
	if (term.length > 0) {
		DatasetFilterAttributes.forEach((datasetAttr) => {
			const resultsAsDatasets = allDatasets;
			const items = resultsAsDatasets.filter((d) =>
				(d[datasetAttr as keyof Dataset] as string).toLowerCase().includes(term)
			);
			finalDatasets.push(...items);
		});
	}

	const datasetResults = term.length > 0 ? uniqBy(finalDatasets, ID) : allDatasets;

	let conceptFacets = await getConceptFacets([DATASETS]);

	if (datasetSearchParam && datasetSearchParam.filters) {
		// datasetSearchParam currently represent facets filters that can be applied
		//  to further refine the list of datasets

		// a special facet related to ontology/DKG concepts needs to be transformed into
		//  some form of filters that can filter the list of datasets.
		// In this case, each concept has an associated list of dataset IDs that can be used to filter datasets
		//  so, we need to map the facet filters from field "concepts" to "id"

		// Each clause of 'concepts' should have another corresponding one with 'id'
		const curies = [] as ClauseValue[];
		const idClauses = [] as Clause[];
		datasetSearchParam.filters.clauses.forEach((clause) => {
			if (clause.field === CONCEPT_FACETS_FIELD) {
				const idClause = cloneDeep(clause);
				idClause.field = 'id';
				const clauseValues = [] as ClauseValue[];
				idClause.values.forEach((conceptNameOrCurie) => {
					// find the corresponding dataset IDs
					if (conceptFacets !== null) {
						const matching = conceptFacets.results.filter(
							(conceptResult) =>
								conceptResult.name === conceptNameOrCurie ||
								conceptResult.curie === conceptNameOrCurie
						);
						// update the clause value by mapping concept/curie to dataset id
						clauseValues.push(...matching.map((m) => m.id));
						curies.push(...matching.map((m) => m.curie));
					}
				});
				idClause.values = clauseValues;
				idClauses.push(idClause);
			}
		});
		// NOTE that we need to merge all concept filters into a single ID filter
		if (idClauses.length > 0) {
			const finalIdClause = cloneDeep(idClauses[0]);
			const allIdValues = idClauses.map((c) => c.values).flat();
			finalIdClause.values = uniq(allIdValues);
			datasetSearchParam.filters.clauses.push(finalIdClause);
		}

		applyFacetFiltersToDatasets(datasetResults, datasetSearchParam.filters);

		// remove any previously added concept/id filters
		datasetSearchParam.filters.clauses = datasetSearchParam.filters.clauses.filter(
			(c) => c.field !== ID
		);

		// ensure that concepts are re-created following the current filtered list of dataset results
		// e.g., if the user has applied other facet filters, e.g. selected some dataset by name
		// then we need to find corresponding curies to filter the concepts accordingly
		if (conceptFacets !== null) {
			// FIXME:
			// This step won't be needed if the concept facets API is able to receive filters as well
			// to only provide concept aggregations based on a filtered set of datasets rather than the full list of datasets
			const finalDatasetIDs = datasetResults.map((m) => m.id);
			conceptFacets.results.forEach((conceptFacetResult) => {
				if (finalDatasetIDs.includes(conceptFacetResult.id)) {
					curies.push(conceptFacetResult.curie);
				}
			});
		}

		// re-create the concept facets if the user has applyied any concept filters
		const uniqueCuries = uniq(curies);
		if (uniqueCuries.length > 0) {
			conceptFacets = await getConceptFacets([DATASETS], uniqueCuries);
		}
	}

	// FIXME: this client-side computation of facets from "dataset" data should be done
	//        at the HMI server
	//
	// This is going to calculate facets aggregations from the list of results
	const datasetFacets = await getDatasetFacets(datasetResults, conceptFacets);

	return {
		results: datasetResults,
		searchSubsystem: ResourceType.DATASET,
		facets: datasetFacets,
		rawConceptFacets: conceptFacets
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
const getRelatedDocuments = async (docid: string, dataset: string | null) => {
	if (docid === '' || dataset === null) {
		return [] as XDDArticle[];
	}

	// https://xdd.wisc.edu/sets/xdd-covid-19/doc2vec/api/similar?doi=10.1002/pbc.28600
	// dataset=xdd-covid-19
	// doi=10.1002/pbc.28600
	// docid=5ebd1de8998e17af826e810e
	const url = `/xdd/related/document?docid=${docid}&set=${dataset}`;

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

	if (xddSearchParam?.docid) {
		url += `&docid=${xddSearchParam.docid}`;
	}
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
	if (xddSearchParam?.includeHighlights) {
		url += '&include_highlights=true';
	}
	if (xddSearchParam?.inclusive) {
		url += '&inclusive=true';
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
			knownTerms: a.known_terms,
			// eslint-disable-next-line no-underscore-dangle
			highlight: a._highlight
		}));

		// process document highlights and style the search term differently in each highlight
		// FIXME: this styling of highlights with search term should be done automatically by XDD
		//        since the content is coming already styled and should not be done at the clinet side for performance reasons
		if (term !== '') {
			articles.forEach((article) => {
				if (article.highlight) {
					article.highlight = article.highlight.map((h) => h.replaceAll(term, `<b>${term}</b>`));
				}
			});
		}

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

const getDocumentById = async (docid: string) => {
	const searchParams: XDDSearchParams = {
		docid
	};
	const xddRes = await searchXDDArticles('', searchParams);
	if (xddRes) {
		const articles = xddRes.results as XDDArticle[];
		if (articles.length > 0) {
			return articles[0];
		}
	}
	return null;
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

	// models
	const promise2 = new Promise<SearchResults>((resolve, reject) => {
		try {
			resolve(getModels(term, searchParam?.model));
		} catch (err: any) {
			reject(new Error(`Error fetching model results: ${err}`));
		}
	});

	// datasets
	const promise3 = new Promise<SearchResults>((resolve, reject) => {
		try {
			resolve(getDatasets(term, searchParam?.model));
		} catch (err: any) {
			reject(new Error(`Error fetching dataset results: ${err}`));
		}
	});

	// fetch results from all search subsystems in parallel
	const responses = await Promise.all([promise1, promise2, promise3]);
	return responses as SearchResults[];
};

export {
	fetchData,
	getXDDSets,
	getXDDDictionaries,
	getXDDArtifacts,
	searchXDDArticles,
	getRelatedDocuments,
	getDocumentById
};
