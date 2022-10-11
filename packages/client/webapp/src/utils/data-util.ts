import { SearchResults } from '@/types/common';
import { Filters } from '@/types/Filter';
import { isEmpty } from 'lodash';
import { Model } from '@/types/Model';
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

const applyFiltersToModels = (models: Model[], filters: Filters) => {
	const { clauses } = filters;
	clauses.forEach((c: any) => {
		const filterField: string = c.field; // the field to filter on
		const filterValues = c.values; // array of values to filter upon
		const isNot = !c.isNot; // is the filter reversed?
		const filteredModels = models.filter(
			(model) =>
				// direct query against XDD Article fields
				filterValues.includes(model[filterField as keyof Model]) === isNot
		);
		// use splice to filter in place
		models.splice(0, models.length, ...filteredModels);
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
		if (resultType === 'model') {
			const modelResults = resultsObj.results as Model[];
			applyFiltersToModels(modelResults, filters);
		}
	}
};
