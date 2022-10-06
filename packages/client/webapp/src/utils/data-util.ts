import { SearchResults } from '@/types/common';
import { Filters } from '@/types/Filter';
import { isEmpty } from 'lodash';
import { Datacube } from '@/types/Datacube';
import { XDDArticle } from '@/types/XDD';

const applyFiltersToArticles = (articles: XDDArticle[], filters: Filters) => {
	const { clauses } = filters;
	clauses.forEach((c: any) => {
		const filterField: string = c.field; // the field to filter on
		const filterValues = c.values; // array of values to filter upon
		const isNot = !c.isNot; // is the filter reversed?
		const filteredArticles = articles.filter(
			(article) =>
				// direct query against XDD Article fields
				filterValues.includes(article[filterField as keyof XDDArticle]) === isNot
		);
		// use splice to filter in place
		articles.splice(0, articles.length, ...filteredArticles);
	});
};

const applyFiltersToDatacubes = (datacubes: Datacube[], filters: Filters) => {
	const { clauses } = filters;
	clauses.forEach((c: any) => {
		const filterField: string = c.field; // the field to filter on
		const filterValues = c.values; // array of values to filter upon
		const isNot = !c.isNot; // is the filter reversed?
		const filteredDatacubes = datacubes.filter(
			(datacube) =>
				// direct query against XDD Article fields
				filterValues.includes(datacube[filterField as keyof Datacube]) === isNot
		);
		// use splice to filter in place
		datacubes.splice(0, datacubes.length, ...filteredDatacubes);
	});
};

// Apply filter to data in place
export const applyFacetFiltersToData = (
	results: SearchResults[],
	resultType: string,
	filters: Filters
) => {
	if (isEmpty(filters) || isEmpty(results)) {
		return;
	}

	const resultsObj = results.find((res) => res.searchSubsystem === resultType);
	if (resultsObj) {
		// extract facets based on the result type
		// because we would have different facets for different result types
		// e.g., XDD will have facets that leverage the XDD fields and stats
		if (resultType === 'xdd') {
			const xddResults = resultsObj.results as XDDArticle[];
			applyFiltersToArticles(xddResults, filters);
		}
		if (resultType === 'datacube') {
			const datacubeResults = resultsObj.results as Datacube[];
			applyFiltersToDatacubes(datacubeResults, filters);
		}
	}
};
