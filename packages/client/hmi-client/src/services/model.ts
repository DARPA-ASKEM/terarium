import API from '@/api/api';
import { EventType, Model, ModelConfiguration } from '@/types/Types';
import { logger } from '@/utils/logger';
import * as ProjectService from '@/services/project';
import { ProjectAssetTypes } from '@/types/Project';
import useResourcesStore from '@/stores/resources';

// TODO - to be removed after July 2023 Hackathon
import { MATHMLMODEL } from '@/temp/models/mathml';
import * as EventService from '@/services/event';

export async function createModel(model): Promise<Model | null> {
	const response = await API.post(`/models`, model);
	return response?.data ?? null;
}

/**
 * Get Model from the data service
 * @return Model|null - the model, or null if none returned by API
 */
export async function getModel(modelId: string): Promise<Model | null> {
	// TODO - to be removed after July 2023 Hackathon
	if (modelId === 'mathml-model') return MATHMLMODEL;

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
	const response = await API.get('/models/descriptions');
	return response?.data ?? null;
}

export async function updateModel(model: Model) {
	const response = await API.put(`/models/${model.id}`, model);
	EventService.create(
		EventType.PersistModel,
		useResourcesStore().activeProject?.id,
		JSON.stringify({
			id: model.id
		})
	);
	return response?.data ?? null;
}

export async function addModelToProject(projectId: string, assetId: string) {
	const resp = await ProjectService.addAsset(projectId, ProjectAssetTypes.MODELS, assetId);

	if (resp) {
		const model = await getModel(assetId);
		if (model) {
			useResourcesStore().activeProject?.assets?.[ProjectAssetTypes.MODELS].push(model);
		} else {
			logger.warn(`Unable to find model id: ${assetId}`);
		}
	} else {
		logger.warn('Could not add new model to project.');
	}
}

export async function getModelConfigurations(modelId: string): Promise<ModelConfiguration[]> {
	const response = await API.get(`/models/${modelId}/model_configurations`);
	return response?.data ?? ([] as ModelConfiguration[]);
}
