import { Facets, FacetBucket, ResourceType } from '@/types/common';
import type { Model, Dataset } from '@/types/Types';
import { FACET_FIELDS as DATASET_FACET_FIELDS, DISPLAY_NAMES as DATASET_DISPLAY_NAMES } from '@/types/Dataset';
import { FACET_FIELDS as MODEL_FACET_FIELDS, DISPLAY_NAMES as MODEL_DISPLAY_NAMES, ID } from '@/types/Model';

import { groupBy } from 'lodash';

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

export const getFacetsDisplayNames = (resultType: string, key: string) => {
	let hits = 0;

	switch (resultType) {
		case ResourceType.MODEL:
			return MODEL_DISPLAY_NAMES[key];
		case ResourceType.DATASET:
			return DATASET_DISPLAY_NAMES[key];
		case ResourceType.ALL:
			// merge display names from all results types,
			//  exclude fields that exist in more than once (e.g., 'type' for models and XDD documents),
			//  and attempt to return the display-name based on the input key
			[MODEL_DISPLAY_NAMES].forEach((d) => {
				if (d[key] !== undefined) hits += 1;
			});
			if (hits === 1) {
				const displayName = {
					...MODEL_DISPLAY_NAMES,
					...MODEL_DISPLAY_NAMES
				}[key];
				logger.info(displayName);
				return displayName;
			}
			return key;
		default:
			return key;
	}
};
