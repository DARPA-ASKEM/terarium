import { ResourceType, ResultType, SearchResults } from '@/types/common';
import { Filters } from '@/types/Filter';
import { isEmpty } from 'lodash';
import { Model } from '@/types/Model';
import { XDDArticle } from '@/types/XDD';
import IconDocument20 from '@carbon/icons-vue/es/document/20';
import IconDocumentBlank20 from '@carbon/icons-vue/es/document--blank/20';
import IconMachineLearningModel20 from '@carbon/icons-vue/es/machine-learning-model/20';
import IconTableSplit20 from '@carbon/icons-vue/es/table--split/20';

// source: https://www.crossref.org/blog/dois-and-matching-regular-expressions/
const DOI_VALIDATION_PATTERN = /^10.\d{4,9}\/[-._;()/:A-Z0-9]+$/i;

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
	results.forEach((resultsObj) => {
		if (resultsObj.searchSubsystem === resultType || resultType === ResourceType.ALL) {
			if (resultsObj.searchSubsystem === ResourceType.XDD) {
				const xddResults = resultsObj.results as XDDArticle[];
				applyFiltersToArticles(xddResults, filters);
			}
			if (resultsObj.searchSubsystem === ResourceType.MODEL) {
				const modelResults = resultsObj.results as Model[];
				applyFiltersToModels(modelResults, filters);
			}
		}
	});
};

export const getResourceTypeIcon = (type: string) => {
	switch (type) {
		case ResourceType.MODEL:
			return IconMachineLearningModel20;
		case ResourceType.DATASET:
			return IconTableSplit20;
		case ResourceType.XDD:
			return IconDocumentBlank20;
		default:
			return IconDocument20;
	}
};

export function isModel(item: ResultType): item is Model {
	return (<Model>item).content !== undefined;
}

export function isXDDArticle(item: ResultType): item is XDDArticle {
	return (<XDDArticle>item).publisher !== undefined;
}

export function getResourceID(item: ResultType) {
	if (isXDDArticle(item)) {
		return (item as XDDArticle).gddid;
	}
	return item.id;
}

/**
 * Validate that the input string is valid.
 *
 * Uses DOI pattern described here: https://www.crossref.org/blog/dois-and-matching-regular-expressions/
 *
 * @param possibleDOI
 * @returns true if DOI is valid
 */
export function validate(possibleDOI?: string): boolean {
	if (!possibleDOI) return false;
	return possibleDOI.match(DOI_VALIDATION_PATTERN) !== null;
}
