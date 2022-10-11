import { Facets, SearchResults, FacetBucket } from '@/types/common';
import {
	Model,
	FACET_FIELDS as MODEL_FACET_FIELDS,
	DISPLAY_NAMES as MODEL_DISPLAY_NAMES
} from '@/types/Model';
import {
	XDDArticle,
	FACET_FIELDS as XDD_FACET_FIELDS,
	DISPLAY_NAMES as XDD_DISPLAY_NAMES
} from '@/types/XDD';
import { groupBy } from 'lodash';

// FIXME:
// XDD does not support Facets natively, so we will perform aggregations on the fly to build facets from XDD data
// ideally, this should be done by the server side or at least cached as a hook or composoable
export const getXDDFacets = (articles: XDDArticle[]) => {
	const facets = {} as Facets;
	const aggField = (fieldName: string) => {
		const aggs: FacetBucket[] = [];
		const articlesMap = articles.map((art) => art[fieldName as keyof XDDArticle]);
		const grouped = groupBy(articlesMap);
		Object.keys(grouped).forEach((gKey) => {
			if (gKey !== '') {
				aggs.push({ key: gKey, value: grouped[gKey].length });
			}
		});
		return aggs;
	};

	XDD_FACET_FIELDS.forEach((field) => {
		const facetForField = aggField(field);
		if (facetForField.length > 0) {
			facets[field] = facetForField;
		}
	});
	return facets;
};

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

export const getFacets = (results: SearchResults[], resultType: string) => {
	let facets = {} as Facets;
	if (results.length > 0) {
		const resultsObj = results.find((res) => res.searchSubsystem === resultType);
		if (resultsObj) {
			// extract facets based on the result type
			// because we would have different facets for different result types
			// e.g., XDD will have facets that leverage the XDD fields and stats
			if (resultType === 'xdd') {
				const xddResults = resultsObj.results as XDDArticle[];
				facets = getXDDFacets(xddResults);
			}
			if (resultType === 'model') {
				const modelResults = resultsObj.results as Model[];
				facets = getModelFacets(modelResults);
			}
		}
	}
	return facets;
};

export const getFacetsDisplayNames = (resultType: string, key: string) => {
	if (resultType === 'xdd') {
		return XDD_DISPLAY_NAMES[key];
	}
	if (resultType === 'model') {
		return MODEL_DISPLAY_NAMES[key];
	}
	return key;
};
