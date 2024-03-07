import API from '@/api/api';
import { useProjects } from '@/composables/project';
import { newAMR } from '@/model-representation/petrinet/petrinet-service';
import * as EventService from '@/services/event';
import type { Model, ModelConfiguration } from '@/types/Types';
import { Artifact, AssetType, EventType } from '@/types/Types';
import { AMRSchemaNames, ModelServiceType } from '@/types/common';
import { fileToJson } from '@/utils/file';
import { logger } from '@/utils/logger';
import { isEmpty } from 'lodash';
import { modelCard } from './goLLM';
import { fetchExtraction, profileModel } from './knowledge';

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

export async function getModelConfigurations(modelId: Model['id']): Promise<ModelConfiguration[]> {
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
export async function addNewPetrinetModelToProject(modelName: string): Promise<string | null> {
	// 1. Load an empty AMR
	const amr = newAMR(modelName);
	(amr as any).id = undefined; // FIXME: id hack

	const response = await createModel(amr);
	const modelId = response?.id;

	return modelId ?? null;
}

export async function processAndAddModelToProject(artifact: Artifact): Promise<string | null> {
	const response = await API.post(`/mira/convert-and-create-model`, {
		artifactId: artifact.id
	});
	const modelId = response.data.id;
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

/**
 * Validates the provided file and returns the json object if it is a valid AMR
 * @param file file to validate
 * @returns json object if valid, null otherwise
 */
export async function validateAMRFile(file: File) {
	if (!file.name.endsWith('.json')) return null;
	const jsonObject = await fileToJson(file);
	if (!jsonObject) return null;
	if (!isValidAMR(jsonObject)) return null;
	return jsonObject;
}

/**
 * Checks if the provided json object is a valid AMR
 * @param json json object to validate
 * @returns boolean
 */
export function isValidAMR(json: Record<string, unknown>) {
	const schema: string = (json?.header as any)?.schema.toLowerCase();
	const schemaName: string = (json?.header as any)?.schema_name.toLowerCase();
	if (!schema || !schemaName) return false;
	if (!Object.values(AMRSchemaNames).includes(schemaName as AMRSchemaNames)) return false;
	if (!Object.values(AMRSchemaNames).some((name) => schema.includes(name))) return false;
	return true;
}

export async function profile(modelId: string, documentId: string): Promise<string | null> {
	const profileModelJobId = await profileModel(modelId, documentId);
	await fetchExtraction(profileModelJobId);
	return modelId;
}

/**
 * Generates a model card based on the provided document ID, model ID, and model service type.
 *
 * @param {string} documentId - The ID of the document.
 * @param {string} modelId - The ID of the model.
 * @param {ModelServiceType} modelServiceType - The type of the model service.
 */
export async function generateModelCard(
	documentId: string,
	modelId: string,
	modelServiceType: ModelServiceType
): Promise<void> {
	if (modelServiceType === ModelServiceType.TA1) {
		await profile(modelId, documentId);
	}

	if (modelServiceType === ModelServiceType.TA4) {
		await modelCard(documentId);
	}
}

// helper fucntion to get the model type, will always default to petrinet if the model is not found
export function getModelType(model: Model | null | undefined): AMRSchemaNames {
	const schemaName = model?.header?.schema_name?.toLowerCase();
	if (schemaName === 'regnet') {
		return AMRSchemaNames.REGNET;
	}
	if (schemaName === 'stockflow') {
		return AMRSchemaNames.STOCKFLOW;
	}
	return AMRSchemaNames.PETRINET;
}

// Converts a model into latex equation, either one of petrinet, stocknflow, or regnet;
export async function getModelEquation(model: Model) {
	const unSupportedFormats = ['decapodes'];
	if (unSupportedFormats.includes(model.header.schema_name as string)) {
		console.log(`getModelEquation: ${model.header.schema_name} not suported `);
		return '';
	}

	const id = model.id;
	const response = await API.get(`/transforms/model-to-latex/${id}`);
	const latex = response.data.latex;
	if (!latex) return '';

	return latex;
}
