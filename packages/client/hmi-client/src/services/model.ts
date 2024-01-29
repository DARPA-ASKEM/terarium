import API from '@/api/api';
import type { Model, ModelConfiguration } from '@/types/Types';
import { AssetType, EventType } from '@/types/Types';
import { useProjects } from '@/composables/project';
import { newAMR } from '@/model-representation/petrinet/petrinet-service';
import * as EventService from '@/services/event';
import { logger } from '@/utils/logger';
import { isEmpty } from 'lodash';

export async function createModel(model): Promise<Model | null> {
	const response = await API.post(`/models`, model);
	return response?.data ?? null;
}

/**
 * Get Model from the data service
 * @return Model|null - the model, or null if none returned by API
 */
export async function getModel(modelId: string): Promise<Model | null> {
	const response = await API.get(`/models/${modelId}`);
	return response?.data ?? null;
}

//
// Retrieve multiple datasets by their IDs
// FIXME: the backend does not support bulk fetch
//        so for now we are fetching by issueing multiple API calls
export async function getBulkModels(modelIDs: string[]) {
	const result: Model[] = [];
	const promiseList = [] as Promise<Model | null>[];
	modelIDs.forEach((modelId) => {
		promiseList.push(getModel(modelId));
	});
	const responsesRaw = await Promise.all(promiseList);
	responsesRaw.forEach((r) => {
		if (r) {
			result.push(r);
		}
	});
	return result;
}

/**
 * Get all models
 * @return Array<Model>|null - the list of all models, or null if none returned by API
 */
export async function getAllModelDescriptions(): Promise<Model[] | null> {
	const response = await API.get('/models/descriptions?page-size=500');
	return response?.data ?? null;
}

export async function updateModel(model: Model) {
	const response = await API.put(`/models/${model.id}`, model);
	EventService.create(
		EventType.PersistModel,
		useProjects().activeProject.value?.id,
		JSON.stringify({
			id: model.id
		})
	);
	return response?.data ?? null;
}

export async function getModelConfigurations(
	modelId: string | undefined
): Promise<ModelConfiguration[]> {
	const response = await API.get(`/models/${modelId}/model-configurations`);
	return response?.data ?? ([] as ModelConfiguration[]);
}

/**
 * Reconstruct an petrinet AMR's ode semantics
 *
 * @deprecated moving to mira-stratify
 */
export async function reconstructAMR(amr: any) {
	const response = await API.post('/mira/reconstruct_ode_semantics', amr);
	return response?.data;
}

// function adds model to project, returns modelId if successful otherwise null
export async function addNewModelToProject(modelName: string): Promise<string | null> {
	// 1. Load an empty AMR
	const amr = newAMR(modelName);
	(amr as any).id = undefined; // FIXME: id hack

	const response = await createModel(amr);
	const modelId = response?.id;

	return modelId ?? null;
}

// A helper function to check if a model is empty.
export function isModelEmpty(model: Model) {
	if (model.header.schema_name === 'petrinet') {
		return isEmpty(model.model?.states) && isEmpty(model.model?.transitions);
	}
	// TODO: support different frameworks' version of empty
	return false;
}

// A helper function to check if a model name already exists
export function validateModelName(name: string): boolean {
	const existingModelNames: string[] = useProjects()
		.getActiveProjectAssets(AssetType.Model)
		.map((item) => item.assetName ?? '');

	if (name.trim().length === 0) {
		logger.info('Model name cannot be empty - please enter a different name');
		return false;
	}
	if (existingModelNames.includes(name.trim())) {
		logger.info('Duplicate model name - please enter a different name');
		return false;
	}

	return true;
}
