import { Facets, SearchResults, FacetBucket } from '@/types/common';
import {
	Datacube,
	FACET_FIELDS as DATACUBE_FACET_FIELDS,
	DISPLAY_NAMES as DATACUBE_DISPLAY_NAMES
} from '@/types/Datacube';
import { Filters } from '@/types/Filter';
import {
	XDDArticle,
	FACET_FIELDS as XDD_FACET_FIELDS,
	DISPLAY_NAMES as XDD_DISPLAY_NAMES
} from '@/types/XDD';
import { groupBy } from 'lodash';

// FIXME:
// XDD does not support Facets natively, so we will perform aggregations on the fly to build facets from XDD data
// ideally, this should be done by the server side or at least cached as a hook or composoable
// eslint-disable-next-line @typescript-eslint/no-unused-vars
export const getXDDFacets = (articles: XDDArticle[], filters?: Filters) => {
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

// eslint-disable-next-line @typescript-eslint/no-unused-vars
export const getDatacubeFacets = (articles: Datacube[], filters?: Filters) => {
	const facets = {} as Facets;
	const aggField = (fieldName: string) => {
		const aggs: FacetBucket[] = [];
		const articlesMap = articles.map((art) => art[fieldName as keyof Datacube]);
		const grouped = groupBy(articlesMap);
		Object.keys(grouped).forEach((gKey) => {
			if (gKey !== '') {
				aggs.push({ key: gKey, value: grouped[gKey].length });
			}
		});
		return aggs;
	};

	DATACUBE_FACET_FIELDS.forEach((field) => {
		const facetForField = aggField(field);
		if (facetForField.length > 0) {
			facets[field] = facetForField;
		}
	});
	return facets;
};

export const getFacets = (results: SearchResults[], resultType: string, filters?: Filters) => {
	let facets = {} as Facets;
	if (results.length > 0) {
		const resultsObj = results.find((res) => res.searchSubsystem === resultType);
		if (resultsObj) {
			// extract facets based on the result type
			// because we would have different facets for different result types
			// e.g., XDD will have facets that leverage the XDD fields and stats
			if (resultType === 'xdd') {
				const xddResults = resultsObj.results as XDDArticle[];
				facets = getXDDFacets(xddResults, filters);
			}
			// TODO: add support for datacube and refactor
			if (resultType === 'datacube') {
				const datacubeResults = resultsObj.results as Datacube[];
				facets = getDatacubeFacets(datacubeResults, filters);
			}
		}
	}
	return facets;
};

export const getFacetsDisplayNames = (resultType: string, key: string) => {
	if (resultType === 'xdd') {
		return XDD_DISPLAY_NAMES[key];
	}
	if (resultType === 'datacube') {
		return DATACUBE_DISPLAY_NAMES[key];
	}
	return key;
};
