import _ from 'lodash';
import {
	Filters,
	Clause,
	ClauseValue,
	ClauseField,
	ClauseNegation,
	ClauseOperand
} from '@/types/Filter';

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

function isEmptyClause(clause: Clause | undefined): boolean {
	if (_.isNil(clause)) return true;
	return _.isEmpty(clause) || _.isEmpty(clause.values);
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

/**
 * Custom equality to check if clause in two different filters are the same,
 * this is needed as we sometimes care if only a specific clause in filter has updated and
 * don't care about updates that occur to rest of the clauses
 *
 * @param {Filters} a - first filter
 * @param {Filters} b - second filters
 * @param {string} field - field name
 * @param {boolean} isNot
 */
function isClauseEqual(a: Filters, b: Filters, field: ClauseField, isNot: ClauseNegation) {
	const aClause = _.find(a.clauses, (clause) => clause.field === field && clause.isNot === isNot);
	const bClause = _.find(b.clauses, (clause) => clause.field === field && clause.isNot === isNot);
	return _.isEqual(aClause, bClause);
}

function findPositiveFacetClause(filters: Filters, field: ClauseField) {
	return _.find(filters.clauses, (clause) => clause.field === field && clause.isNot === false);
}

function findNegativeFacetClause(filters: Filters, field: ClauseField) {
	return _.find(filters.clauses, (clause) => clause.field === field && clause.isNot === true);
}

function addSearchTerm(
	filters: Filters,
	field: ClauseField,
	term: ClauseValue,
	operand: ClauseOperand,
	isNot: ClauseNegation
) {
	const existingClause = _.find(
		filters.clauses,
		(clause) => clause.field === field && clause.operand === operand && clause.isNot === isNot
	);

	if (!_.isNil(existingClause)) {
		existingClause.values.push(term);
	} else {
		filters.clauses.push({
			field,
			operand,
			isNot,
			values: [term]
		});
	}
}

function removeSearchTerm(
	filters: Filters,
	field: ClauseField,
	term: ClauseValue,
	operand: ClauseOperand,
	isNot: ClauseNegation
) {
	const existingClause = _.find(
		filters.clauses,
		(clause) => clause.field === field && clause.operand === operand && clause.isNot === isNot
	);

	if (!_.isNil(existingClause)) {
		_.remove(existingClause.values, (v) => _.isEqual(v, term));

		// If now empty remove clause
		if (_.isEmpty(existingClause.values)) {
			_.remove(
				filters.clauses,
				(clause) => clause.field === field && clause.operand === operand && clause.isNot === isNot
			);
		}
	}
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

function removeAllClausesByFacet(filters: Filters, field: ClauseField) {
	_.remove(filters.clauses, (clause) => clause.field === field);
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
	findNegativeFacetClause,
	addSearchTerm,
	removeSearchTerm,
	isEmpty,
	isEmptyClause,
	newFilters,
	removeClause,
	removeAllClausesByFacet,
	setClause,
	isEqual,
	isClauseEqual
};
