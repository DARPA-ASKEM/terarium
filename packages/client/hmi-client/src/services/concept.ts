/**
 * Concept
 */

import API from '@/api/api';
import type { DKG, Model, Grounding } from '@/types/Types';
import { logger } from '@/utils/logger';
import { isEmpty } from 'lodash';
import { FIFOCache } from '@/utils/FifoCache';
import { Workflow, WorkflowNode } from '@/types/workflow';

const curieNameCache = new Map<string, string>();

const currieEntityCache = new FIFOCache<Promise<{ data: any; status: number }>>(400);
/**
 * Get DKG entities, either single ones or multiple at a time
 */
async function getCuriesEntities(curies: Array<string>): Promise<Array<DKG> | null> {
	try {
		const cacheKey = curies.toString();

		let promise = currieEntityCache.get(cacheKey);
		if (!promise) {
			promise = API.get(`/dkg/currie/${curies.toString()}`).then((res) => ({ data: res.data, status: res.status }));
			currieEntityCache.set(cacheKey, promise);
		}

		// If a rename function is defined, loop over the first row
		const response = await promise;
		if (response?.status === 200 && response?.data) return response.data;
		if (response?.status === 204) console.warn('No DKG entities found for curies:', curies);
		return null;
	} catch (err) {
		logger.error(err);
		return null;
	}
}

async function searchCuriesEntities(query: string): Promise<Array<DKG>> {
	try {
		const response = await API.get('/dkg/search', {
			params: {
				term: query
			}
		});
		if (response?.status !== 200) return [];
		return response?.data ?? [];
	} catch (error) {
		logger.error(`There was an issue searching for mira query: ${query}`, { showToast: false });
		return [];
	}
}

const getNameOfCurieCached = async (curie: string): Promise<string> => {
	if (isEmpty(curie)) return '';
	if (!curieNameCache.has(curie)) {
		const response = await getCuriesEntities([curie]);
		curieNameCache.set(curie, response?.[0]?.name ?? '');
	}
	return curieNameCache.get(curie) ?? '';
};

function getCurieFromGroundingIdentifier(identifier: Object | undefined): string {
	if (!!identifier && !isEmpty(identifier)) {
		const [key, value] = Object.entries(identifier)[0];
		return `${key}:${value}`;
	}
	return '';
}

async function getDKGFromGroundingIdentifier(identifier: Object): Promise<DKG> {
	const dkg: DKG = { name: '', curie: '', description: '' };
	dkg.curie = getCurieFromGroundingIdentifier(identifier);
	dkg.name = await getNameOfCurieCached(dkg.curie);
	if (isEmpty(dkg.name)) dkg.name = dkg.curie; // in case no name could be found, display the curie
	return dkg;
}

async function getDKGFromGroundingModifier(modifiers: Grounding['modifiers']): Promise<DKG[]> {
	if (isEmpty(modifiers)) return [] as DKG[];

	return Promise.all(
		Object.entries(modifiers)
			.filter(([_key, value]) => value.includes(':')) // Test if the value is a curie or a string
			.map(async ([key, value]) => {
				const dkg: DKG = { name: key, curie: value, description: '' };
				const newName = await getNameOfCurieCached(dkg.curie); // Resolve the name of curies properly
				if (!isEmpty(newName)) dkg.name = newName;
				return dkg;
			})
	);
}

function parseCurieToIdentifier(curie: string | undefined): { [key: string]: string } {
	if (!curie) return {};
	const [key, value] = curie.split(':');
	return { [key]: value };
}

function parseListDKGToGroundingModifiers(dkgList: DKG[]): { [index: string]: string } {
	const modifiers: Grounding['modifiers'] = {};
	dkgList.forEach((dkg) => {
		if (dkg.name.includes(':')) {
			const [key, value] = dkg.name.split(':');
			modifiers[key] = value;
		} else {
			modifiers[dkg.name] = dkg.curie;
		}
	});
	return modifiers;
}

/**
 * Compare multiple models and return the concepts that are common between them
 * @param modelIds - List of model ids to compare
 * @returns
 */
interface CompareModelsConceptsRequest {
	modelIds: Array<Model['id']>;
	workflowId?: Workflow['id'];
	nodeId?: WorkflowNode<any>['id'];
}
interface CompareModelsConceptsResponse {
	concept_context_comparison?: string;
	tabular_comparison?: { [key: string]: string };
	concept_graph_comparison?: { [key: string]: string };
}
async function getCompareModelConcepts(
	modelIds: Array<Model['id']>,
	workflowId?: Workflow['id'],
	nodeId?: WorkflowNode<any>['id']
) {
	const request: CompareModelsConceptsRequest = { modelIds, workflowId, nodeId };
	const response = await API.post('/mira/compare-models-concepts', request);
	if (response?.status !== 200) return null;
	return (response?.data.response as CompareModelsConceptsResponse) ?? null;
}

export {
	getCuriesEntities,
	searchCuriesEntities,
	getNameOfCurieCached,
	getCurieFromGroundingIdentifier,
	getDKGFromGroundingIdentifier,
	getDKGFromGroundingModifier,
	parseCurieToIdentifier,
	parseListDKGToGroundingModifiers,
	getCompareModelConcepts,
	type CompareModelsConceptsResponse
};
