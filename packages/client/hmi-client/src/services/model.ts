import API from '@/api/api';
import { EventType, Model, ModelConfiguration } from '@/types/Types';
import useResourcesStore from '@/stores/resources';
import * as EventService from '@/services/event';
import { IProject, ProjectAssetTypes } from '@/types/Project';
import router from '@/router';
import { RouteName } from '@/router/routes';
import * as ProjectService from '@/services/project';

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

export async function getModelConfigurations(modelId: string): Promise<ModelConfiguration[]> {
	const response = await API.get(`/models/${modelId}/model_configurations`);
	return response?.data ?? ([] as ModelConfiguration[]);
}

/**
 * Reconstruct an petrinet AMR's ode semantics
 */
export async function reconstructAMR(amr: any) {
	const response = await API.post('/mira/reconstruct_ode_semantics', amr);
	return response?.data;
}

export async function addNewModelToProject(modelName: string, project: IProject) {
	// 1. Load an empty AMR
	const amr = newAMR(modelName);
	(amr as any).id = undefined; // FIXME: id hack

	const response = await createModel(amr);
	const modelId = response?.id;

	// 2. Add the model to the project
	if (modelId) {
		await ProjectService.addAsset(project.id, ProjectAssetTypes.MODELS, modelId);
		// 3. Reroute
		router.push({
			name: RouteName.ProjectRoute,
			params: {
				assetName: 'Model',
				pageType: ProjectAssetTypes.MODELS,
				assetId: modelId
			}
		});
	}
}

function newAMR(modelName: string) {
	const amr: Model = {
		id: '',
		name: modelName,
		description: '',
		schema:
			'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.5/petrinet/petrinet_schema.json',
		schema_name: 'petrinet',
		model_version: '0.1',
		model: {
			states: [],
			transitions: []
		},
		semantics: {
			ode: {
				rates: [],
				initials: [],
				parameters: []
			}
		}
	};
	return amr;
}
