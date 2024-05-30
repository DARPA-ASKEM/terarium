import { defineStore } from 'pinia';
import { cloneDeep, isEmpty, isNil } from 'lodash';

import FiltersUtil from '@/utils/filters-util';
import { Clause, Filters } from '@/types/Filter';

/**
 * Main store used for data explorer search query
 */
const useQueryStore = defineStore('query', {
	state: () => ({
		filters: null as Filters | null
	}),
	getters: {
		clientFilters: (state) => (isEmpty(state.filters) ? FiltersUtil.newFilters() : state.filters)
	},
	actions: {
		setSearchClause({ field, values, operand, isNot }: Clause) {
			const filters = cloneDeep(this.clientFilters);

			// eslint-disable-next-line no-param-reassign
			if (isNil(operand)) operand = 'or';
			// eslint-disable-next-line no-param-reassign
			if (isNil(isNot)) isNot = false;

			FiltersUtil.setClause(filters, field, values, operand, isNot);

			this.filters = filters;
		},
		reset() {
			this.filters = null;
		}
	}
});

export default useQueryStore;
