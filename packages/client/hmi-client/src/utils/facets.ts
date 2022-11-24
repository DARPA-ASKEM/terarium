import { Facets, SearchResults, FacetBucket, ResourceType } from '@/types/common';
import {
	Model,
	FACET_FIELDS as MODEL_FACET_FIELDS,
	DISPLAY_NAMES as MODEL_DISPLAY_NAMES
} from '@/types/Model';
import { DISPLAY_NAMES as XDD_DISPLAY_NAMES } from '@/types/XDD';
import { groupBy, mergeWith, isArray } from 'lodash';

// FIXME: this client-side computation of facets from "models" data should be done
//        at the HMI server
export const getModelFacets = (articles: Model[]) => {
	const facets = {} as Facets;
	const aggField = (fieldName: string) => {
		const aggs: FacetBucket[] = [];
		const articlesMap = articles.map((art) => art[fieldName as keyof Model]);
		const grouped = groupBy(articlesMap);
		Object.keys(grouped).forEach((gKey) => {
			if (gKey !== '') {
				aggs.push({ key: gKey, value: grouped[gKey].length });
			}
		});
		return aggs;
	};

	MODEL_FACET_FIELDS.forEach((field) => {
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

export const getFacets = (results: SearchResults[], resultType: string) => {
	let facets = {} as Facets;
	if (results.length > 0) {
		results.forEach((resultsObj) => {
			if (resultsObj.searchSubsystem === resultType || resultType === ResourceType.ALL) {
				// extract facets based on the result type
				// because we would have different facets for different result types
				// e.g., XDD will have facets that leverage the XDD fields and stats
				if (resultsObj.searchSubsystem === ResourceType.XDD) {
					const xddFacets = resultsObj.facets;
					facets = mergeWith(facets, xddFacets, mergeCustomizer);
				}
				if (resultsObj.searchSubsystem === ResourceType.MODEL) {
					const modelFacets = resultsObj.facets;
					facets = mergeWith(facets, modelFacets, mergeCustomizer);
				}
			}
		});
	}
	return facets;
};

export const getFacetsDisplayNames = (resultType: string, key: string) => {
	if (resultType === ResourceType.XDD) {
		return XDD_DISPLAY_NAMES[key];
	}
	if (resultType === ResourceType.MODEL) {
		return MODEL_DISPLAY_NAMES[key];
	}
	if (resultType === ResourceType.ALL) {
		// merge display names from all results types,
		//  exclude fields that exist in more than once (e.g., 'type' for models and XDD documents),
		//  and attempt to return the display-name based on the input key
		const allDisplayNames = [MODEL_DISPLAY_NAMES, XDD_DISPLAY_NAMES];
		let hits = 0;
		allDisplayNames.forEach((d) => {
			if (d[key] !== undefined) hits += 1;
		});
		if (hits === 1) {
			const displayName = { ...MODEL_DISPLAY_NAMES, ...XDD_DISPLAY_NAMES }[key];
			return displayName;
		}
	}
	return key;
};
