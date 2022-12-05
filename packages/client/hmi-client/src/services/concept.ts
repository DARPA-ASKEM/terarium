/**
 * Concept
 */

import API from '@/api/api';
import { ConceptFacets } from '@/types/Concept';
import { ClauseValue } from '@/types/Filter';

/**
 * Get concept facets
 * @types string - filter returned facets to a given type.
 *        Available values : datasets, features, intermediates, model_parameters, models, projects, publications, qualifiers, simulation_parameters, simulation_plans, simulation_runs
 * @return ConceptFacets|null - the concept facets, or null if none returned by API
 */
async function getFacets(types: string[], curies?: ClauseValue[]): Promise<ConceptFacets | null> {
	try {
		let url = '/concepts/facets/';
		if (types) {
			types.forEach((type, indx) => {
				url += `${indx === 0 ? '?' : '&'}types=${type}`;
			});
		}
		if (curies) {
			curies.forEach((curie) => {
				url += `&curies=${curie}`;
			});
		}
		const response = await API.get(url);
		const { status, data } = response;
		if (status !== 200) return null;
		return data ?? null;
	} catch (error) {
		console.error(error);
		return null;
	}
}

export { getFacets };
