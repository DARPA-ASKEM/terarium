import _ from 'lodash';
import { Filters, ClauseValue, ClauseField, ClauseNegation, ClauseOperand } from '@/types/Filter';

/**
 * Constructs search queries to pass through the API.
 *
 * Queries are made of 3 parts:
 * - filters: filters data
 * - pageStart/pageLimit: pagination
 * - orderBy: sort options
 *
 * Filters consist of an array of sub-filters, each of which are expected to define these attributes
 * - field: the logical-field to filter
 * - values: an array of matching values
 * - operand: specifies either 'and' or 'or' operations
 * - isNot: specifies whether the sub-filter should be negated, expect true|false
 *
 * Note: Currently there is a 'clauses' object, used to get around encoding issues. This is subjected
 * to change and so "clauses" should not be exposed outside of this module.
 *
 */

/**
 * Filters object.
 * @typedef {Object} Filters
 * @property {Filter[]} clauses - A list of filters
 */

/**
 * A filter object
 * @typedef {Object} Filter
 * @property {string} field - the logical-field to filter
 * @property {string[]|number[]} values - an array of matching values
 * @property {string} operand -  specifies either 'and' or 'or' operations
 * @property {boolean} isNot - specifies whether the sub-filter should be negated, expect true|false
 */

function isEmpty(filters: Filters | undefined): boolean {
	if (_.isNil(filters)) return true;
	return _.isEmpty(filters) || _.isEmpty(filters.clauses);
}

/**
 * Custom equality to check if two filters are the same, this is needed
 * as the clauses is an array of object and in some cases we cannot ensure
 * order.
 *
 * @param {Filters} a - first filter
 * @param {Filters} b - second filters
 */
function isEqual(a: Filters, b: Filters) {
	if (isEmpty(a) && isEmpty(b)) return true;
	if (isEmpty(a) && !isEmpty(b)) return false;
	if (!isEmpty(a) && isEmpty(b)) return false;
	if (a.clauses.length !== b.clauses.length) return false;

	for (let i = 0; i < a.clauses.length; i++) {
		const aClause = a.clauses[i];
		const foundItem = b.clauses.find((bClause) => _.isEqual(aClause, bClause));
		if (_.isNil(foundItem)) return false;
	}
	return true;
}

function findPositiveFacetClause(filters: Filters, field: ClauseField) {
	return _.find(filters.clauses, (clause) => clause.field === field && clause.isNot === false);
}

function removeClause(
	filters: Filters,
	field: ClauseField,
	operand: ClauseOperand,
	isNot: ClauseNegation
) {
	_.remove(
		filters.clauses,
		(clause) => clause.field === field && clause.operand === operand && clause.isNot === isNot
	);
}

function setClause(
	filters: Filters,
	field: ClauseField,
	values: ClauseValue[],
	operand: ClauseOperand,
	isNot: ClauseNegation
) {
	removeClause(filters, field, operand, isNot);

	if (!_.isEmpty(values)) {
		filters.clauses.push({
			field,
			operand,
			isNot,
			values
		});
	}
}

/**
 * @returns {Filters}
 */
function newFilters(): Filters {
	return { clauses: [] };
}

export default {
	findPositiveFacetClause,
	isEmpty,
	newFilters,
	removeClause,
	setClause,
	isEqual
};
