/**
 * Concept
 */

import API from '@/api/api';
import { ConceptFacets } from '@/types/Concept';
import { ClauseValue } from '@/types/Filter';
import type { Curies, DKG, EntitySimilarityResult } from '@/types/Types';
import { logger } from '@/utils/logger';
import { isEmpty } from 'lodash';

/**
 * Get concept facets
 * @types string - filter returned facets to a given type.
 *        Available values : datasets, features, intermediates, model_parameters, models, projects, publications, qualifiers, simulation_parameters, simulation_plans, simulation_runs
 * @return ConceptFacets|null - the concept facets, or null if none returned by API
 */
async function getFacets(types: string[], curies?: ClauseValue[]): Promise<ConceptFacets | null> {
	try {
		let url = '/concepts/facets';
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

/**
 * Get DKG entities, either single ones or multiple at a time
 */
async function getCuriesEntities(curies: Array<string>): Promise<Array<DKG> | null> {
	try {
		const response = await API.get(`/mira/${curies.toString()}`);
		if (response?.status !== 200) return null;
		return response?.data ?? null;
	} catch (error) {
		logger.error(error, { showToast: false });
		return null;
	}
}

async function searchCuriesEntities(query: string): Promise<Array<DKG>> {
	try {
		const response = await API.get('/mira/search', {
			params: {
				q: query
			}
		});
		if (response?.status !== 200) return [];
		return response?.data ?? [];
	} catch (error) {
		logger.error(`There was an issue searching for mira query: ${query}`, { showToast: false });
		return [];
	}
}

function getCurieUrl(curie: string): string {
	return `http://34.230.33.149:8772/${curie}`;
}

/**
 * Hit MIRA to get pairwise similarities between elements referenced by CURIEs in the first list and second list.
 * @input a List of curies (strings) for each source, and target.
 * @return EntitySimilarityResult[] - The source and target curies and their corresponding cosine_distance
  Sample:
	"sources": ["ido:0000514"],
  "targets": ["doid:0081014", "cido:0000180"]

  Output:
	[
		{
			"source": "ido:0000514",
			"target": "doid:0081014",
			"distance": 1.3786096032625137
		},
		{
			"source": "ido:0000514",
			"target": "cido:0000180",
			"distance": 0.8597939628230753
		}
	]
 */
async function getEntitySimilarity(
	sources: string[],
	targets: string[]
): Promise<Array<EntitySimilarityResult> | null> {
	try {
		const response = await API.post('/mira/entity_similarity', { sources, targets } as Curies);
		if (response?.status !== 200) return null;
		return response?.data ?? null;
	} catch (error) {
		logger.error(error, { showToast: false });
		return null;
	}
}

const getNameOfCurieCached = (cache: Map<string, string>, curie: string): string => {
	if (!cache.has(curie)) {
		getCuriesEntities([curie]).then((response) => cache.set(curie, response?.[0].name ?? ''));
	}
	return cache.get(curie) ?? '';
};

function getCurieFromGroudingIdentifier(identifier: Object | undefined): string {
	if (!!identifier && !isEmpty(identifier)) {
		const [key, value] = Object.entries(identifier)[0];
		return `${key}:${value}`;
	}
	return '';
}

function parseCurie(curie: string) {
	const key = curie.split(':')[0];
	const value = curie.split(':')[1];
	return { [key]: value };
}

export {
	getCuriesEntities,
	getFacets,
	getEntitySimilarity,
	searchCuriesEntities,
	getNameOfCurieCached,
	getCurieFromGroudingIdentifier,
	getCurieUrl,
	parseCurie
};
