/**
 * Concept
 */

import API from '@/api/api';
import { ConceptFacets } from '@/types/Concept';
import { ClauseValue } from '@/types/Filter';
import type {
	AssetType,
	Curies,
	DatasetColumn,
	DKG,
	EntitySimilarityResult,
	State
} from '@/types/Types';
import { logger } from '@/utils/logger';
import { isEmpty } from 'lodash';
import { CalibrateMap } from '@/services/calibrate-workflow';

interface Entity {
	id: string;
	groundings?: any[];
}

interface EntityMap {
	source: string;
	target: string;
}

/**
 * Get concept facets
 * @types string - filter returned facets to a given type.
 *        Available values : datasets, features, intermediates, model_parameters, models, projects, publications, qualifiers, simulation_parameters, simulation_plans, simulation_runs
 * @return ConceptFacets|null - the concept facets, or null if none returned by API
 */
async function getFacets(type: AssetType, curies?: ClauseValue[]): Promise<ConceptFacets | null> {
	try {
		let url = `/concepts/facets?types=${type}`;
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
	const response = await API.get(`/mira/currie/${curies.toString()}`);
	if (response?.status === 200 && response?.data) return response.data;
	if (response?.status === 204) console.warn('No DKG entities found for curies:', curies);
	return null;
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
		const response = await API.post('/mira/entity-similarity', { sources, targets } as Curies);
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

// Takes in 2 lists of generic {id, groundings} and returns the singular
// closest match for each element in list one
const autoEntityMapping = async (
	sourceEntities: Entity[],
	targetEntities: Entity[],
	acceptableDist?: number
) => {
	const result = [] as EntityMap[];
	const acceptableDistance = acceptableDist ?? 0.5;

	// join all source and target for 1 mira call
	const allSourceGroundings = sourceEntities.flatMap(({ groundings }) => groundings ?? []);
	const allTargetGroundings = targetEntities.flatMap(({ groundings }) => groundings ?? []);

	// Take out any duplicates
	const distinctSourceGroundings = [...new Set(allSourceGroundings)];
	const distinctTargetGroundings = [...new Set(allTargetGroundings)];
	const allSimilarity = await getEntitySimilarity(
		distinctSourceGroundings,
		distinctTargetGroundings
	);
	if (!allSimilarity) return result;
	// Filter out anything with a similarity too small
	const filteredSimilarity = allSimilarity.filter((ele) => ele.similarity >= acceptableDistance);
	const validSources: any[] = [];
	const validTargets: any[] = [];

	// Join results back with source and target
	filteredSimilarity.forEach((similarity) => {
		// Find all Sources assosiated with this similarity
		sourceEntities.forEach((source) => {
			if (source.groundings?.includes(similarity.source)) {
				validSources.push({
					sourceId: source.id,
					sourceKey: similarity.source,
					targetKey: similarity.target,
					distance: similarity.similarity
				});
			}
		});
		// Find all targets assosiated with this similarity
		targetEntities.forEach((target) => {
			if (target.groundings?.includes(similarity.target)) {
				validTargets.push({
					targetId: target.id,
					sourceKey: similarity.source,
					targetKey: similarity.target,
					distance: similarity.similarity
				});
			}
		});
	});

	// for each distinct source, find its highest matching target:
	const distinctSource = [...new Set(validSources.map((ele) => ele.sourceId))];
	distinctSource.forEach((distinctSourceId) => {
		let currentDistance = -Infinity;
		let currentTargetId = '';
		validSources.forEach((source) => {
			if (distinctSourceId === source.sourceId) {
				validTargets.forEach((target) => {
					// Note here we are using the source and target groundings as a key
					if (
						source.sourceKey === target.sourceKey &&
						source.targetKey === target.targetKey &&
						target.distance > currentDistance
					) {
						currentDistance = target.distance;
						currentTargetId = target.targetId;
					}
				});
			}
		});
		result.push({ source: distinctSourceId, target: currentTargetId });
	});

	return result;
};

// Takes in a list of states and a list of datasetcolumns.
// Transforms them into generic entities with {id, groundings}
// Uses autoEntityMapping to determine 1:1 mapping
// rewrites result in form {modelVariable, datasetVariable}
const autoCalibrationMapping = async (modelOptions: State[], datasetOptions: DatasetColumn[]) => {
	const result = [] as CalibrateMap[];
	const acceptableDistance = 0.5;
	const sourceEntities: Entity[] = [];
	const targetEntities: Entity[] = [];

	// Fill sourceEntities with modelOptions
	modelOptions.forEach((state) => {
		const groundings = state.grounding?.identifiers
			? Object.entries(state.grounding.identifiers).map((ele) => ele.join(':'))
			: undefined;
		sourceEntities.push({ id: state.id.toLowerCase(), groundings });
	});

	// Fill targetEntities with datasetOptions
	datasetOptions.forEach((col) => {
		const groundings = col.metadata?.groundings?.identifiers
			? Object.entries(col.metadata.groundings.identifiers).map((ele) => ele.join(':'))
			: undefined;
		targetEntities.push({ id: col.name, groundings });
	});

	const entityResult = await autoEntityMapping(sourceEntities, targetEntities, acceptableDistance);

	// rename result to CalibrateMap for users of this function
	entityResult.forEach((entity) => {
		result.push({ modelVariable: entity.source, datasetVariable: entity.target });
	});
	return result;
};

// Takes in two lists of states
// Transforms them into generic entities with {id, groundings}
// Uses autoEntityMapping to determine 1:1 mapping
// rewrites result in form {modelOneVariable, modelTwoVariable}
const autoModelMapping = async (modelOneOptions: State[], modelTwoOptions: State[]) => {
	const result = [] as any[];
	const acceptableDistance = 0.5;
	const sourceEntities: Entity[] = [];
	const targetEntities: Entity[] = [];

	// Fill sourceEntities with modelOneOptions
	modelOneOptions.forEach((state) => {
		const groundings = state.grounding?.identifiers
			? Object.entries(state.grounding.identifiers).map((ele) => ele.join(':'))
			: undefined;
		sourceEntities.push({ id: state.id, groundings });
	});

	// Fill targetEntities with datasetOptions
	modelTwoOptions.forEach((state) => {
		const groundings = state.grounding?.identifiers
			? Object.entries(state.grounding.identifiers).map((ele) => ele.join(':'))
			: undefined;
		targetEntities.push({ id: state.id, groundings });
	});

	const entityResult = await autoEntityMapping(sourceEntities, targetEntities, acceptableDistance);

	// rename result to CalibrateMap for users of this function
	entityResult.forEach((entity) => {
		result.push({ modelOneVariable: entity.source, modelTwoVariable: entity.target });
	});
	return result;
};

export {
	getCuriesEntities,
	getFacets,
	getEntitySimilarity,
	searchCuriesEntities,
	getNameOfCurieCached,
	getCurieFromGroudingIdentifier,
	getCurieUrl,
	parseCurie,
	autoModelMapping,
	autoCalibrationMapping,
	autoEntityMapping
};
