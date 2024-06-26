import { Facets, SearchResults, FacetBucket, ResourceType } from '@/types/common';
import type { Model, Document, XDDFacetsItemResponse, Dataset } from '@/types/Types';
import {
	FACET_FIELDS as DATASET_FACET_FIELDS,
	DISPLAY_NAMES as DATASET_DISPLAY_NAMES
} from '@/types/Dataset';
import {
	FACET_FIELDS as MODEL_FACET_FIELDS,
	DISPLAY_NAMES as MODEL_DISPLAY_NAMES,
	ID
} from '@/types/Model';
import {
	DISPLAY_NAMES as XDD_DISPLAY_NAMES,
	FACET_FIELDS as DOCUMENT_FACET_FIELDS,
	GITHUB_URL
} from '@/types/XDD';
import { groupBy, mergeWith, isArray } from 'lodash';

import { logger } from '@/utils/logger';

// FIXME: this client-side computation of facets from "models" data should be done //////////////////no point in editing//////////////////
//        at the HMI server
export const getModelFacets = (models: Model[]) => {
	// utility function for manually calculating facet aggregation from model results
	const aggField = (fieldName: string) => {
		const aggs: FacetBucket[] = [];
		const modelsMap = models.map((model) => model[fieldName as keyof Model]);
		const grouped = groupBy(modelsMap);
		Object.keys(grouped).forEach((gKey) => {
			if (gKey !== '') {
				aggs.push({ key: gKey, value: grouped[gKey].length });
			}
		});
		return aggs;
	};

	const facets = {} as Facets;

	// create facets from specific model fields
	MODEL_FACET_FIELDS.forEach((field) => {
		// exclude model ID as a facet since it is created from mapping concepts
		if (field !== ID) {
			const facetForField = aggField(field);
			if (facetForField.length > 0) {
				facets[field] = facetForField;
			}
		}
	});

	return facets;
};

// FIXME: this client-side computation of facets from "datasets" data should be done //////////////////no point in editing//////////////////
//        at the HMI server
export const getDatasetFacets = (datasets: Dataset[]) => {
	// utility function for manually calculating facet aggregation from dataset results
	const aggField = (fieldName: string) => {
		const aggs: FacetBucket[] = [];
		const datasetsMap = datasets.map((model) => model[fieldName as keyof Dataset]);
		const grouped = groupBy(datasetsMap);
		Object.keys(grouped).forEach((gKey) => {
			if (gKey !== '') {
				aggs.push({ key: gKey, value: grouped[gKey].length });
			}
		});
		return aggs;
	};

	const facets = {} as Facets;

	// create facets from specific dataset fields
	DATASET_FACET_FIELDS.forEach((field) => {
		// exclude dataset ID as a facet since it is created from mapping concepts
		if (field !== ID) {
			const facetForField = aggField(field);
			if (facetForField.length > 0) {
				facets[field] = facetForField;
			}
		}
	});

	return facets;
};

// FIXME: this client-side computation of facets from "datasets" data should be done //////////////////no point in editing//////////////////
//        at the HMI server
export const getDocumentFacets = (documents: Document[]) => {
	// utility function for manually calculating facet aggregation from dataset results
	const aggField = (fieldName: string) => {
		const aggs: FacetBucket[] = [];
		const documentsMap = documents.map((document) => document[fieldName as keyof Document]);
		// convert lists inside these fields to individual entries
		const unListedDocumentsMap: any[] = [];
		documentsMap.forEach((entry) => {
			if (entry) {
				if (Array.isArray(entry)) {
					entry.forEach((subEntry) => {
						unListedDocumentsMap.push(subEntry);
					});
				} else {
					unListedDocumentsMap.push(entry);
				}
			}
		});

		const grouped = groupBy(unListedDocumentsMap);
		Object.keys(grouped).forEach((gKey) => {
			if (gKey !== '' && gKey !== 'undefined') {
				aggs.push({ key: gKey, value: grouped[gKey].length });
			}
		});
		return aggs;
	};

	const facets = {} as Facets;

	// create facets from specific dataset fields
	DOCUMENT_FACET_FIELDS.forEach((field) => {
		const facetForField = aggField(field);
		if (facetForField.length > 0) {
			facets[field] = facetForField;
		}
	});

	return facets;
};

// Merging facets who share the same key requires custom logic, e.g.,
//  XDD documents of "type" [fulltext] and Models of "type": [model, dataset]
//  should be merged into one facet representing the overall "type" of result
// @ts-ignore
// eslint-disable-next-line consistent-return
function mergeCustomizer(objValue: any, srcValue: any) {
	if (isArray(objValue)) {
		return objValue.concat(srcValue);
	}
	// return null;
}

export const getFacets = (results: SearchResults[], resultType: ResourceType | string) => {
	let facets = {} as { [index: string]: XDDFacetsItemResponse };
	if (results.length > 0) {
		results.forEach((resultsObj) => {
			if (resultsObj.searchSubsystem === resultType || resultType === ResourceType.ALL) {
				// extract facets based on the result type
				// because we would have different facets for different result types
				// e.g., XDD will have facets that leverage the XDD fields and stats
				if (
					resultsObj.searchSubsystem === ResourceType.XDD ||
					resultsObj.searchSubsystem === ResourceType.MODEL ||
					resultsObj.searchSubsystem === ResourceType.DATASET
				) {
					facets = mergeWith(facets, resultsObj.facets, mergeCustomizer);
				}
			}
		});
	}
	return facets;
};

export const getFacetNameFormatter: Function = (resultType: string, key: string) => {
	if (resultType === ResourceType.XDD && key === GITHUB_URL) {
		return function format(label: string) {
			// @ts-ignore
			// eslint-disable-next-line @typescript-eslint/no-unused-vars
			const [url, ...rest] = label.split('github.com/');
			return rest;
		};
	}

	return null;
};

export const getFacetsDisplayNames = (resultType: string, key: string) => {
	let hits = 0;

	switch (resultType) {
		case ResourceType.XDD:
			return XDD_DISPLAY_NAMES[key];
		case ResourceType.MODEL:
			return MODEL_DISPLAY_NAMES[key];
		case ResourceType.DATASET:
			return DATASET_DISPLAY_NAMES[key];
		case ResourceType.ALL:
			// merge display names from all results types,
			//  exclude fields that exist in more than once (e.g., 'type' for models and XDD documents),
			//  and attempt to return the display-name based on the input key
			[MODEL_DISPLAY_NAMES, XDD_DISPLAY_NAMES].forEach((d) => {
				if (d[key] !== undefined) hits += 1;
			});
			if (hits === 1) {
				const displayName = {
					...MODEL_DISPLAY_NAMES,
					...MODEL_DISPLAY_NAMES,
					...XDD_DISPLAY_NAMES
				}[key];
				logger.info(displayName);
				return displayName;
			}
			return key;
		default:
			return key;
	}
};
