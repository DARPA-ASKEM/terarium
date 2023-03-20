import API from '@/api/api';
import { Model } from '@/types/Model';

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

export async function updateModel(model) {
	console.log(JSON.stringify(model.content));
	const response = await API.post(`/models/${model.id}`, {
		name: model.name,
		description: model.description,
		framework: model.framework,
		content: JSON.stringify(model.content)
	});
	return response?.data ?? null;
}

export async function createModel(model) {
	console.log(JSON.stringify(model.content));
	const response = await API.post(`/models`, {
		name: model.name,
		description: model.description,
		framework: model.framework,
		content: JSON.stringify(model.content)
	});
	return response?.data ?? null;
}
