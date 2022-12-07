import API from '@/api/api';

// Providing the ID of 3 Models (model A, model B, and the type Model)
// Create a new model of based off of the stratification
// TODO: Better error handling (models not found for example)
//       Override function to also accept 2 typed models (need to chat with TA2 to fully understand)
export async function fetchStratificationResult(modelA: string, modelB: string, typeModel: string) {
	if (!modelA || !modelB || !typeModel) {
		throw new Error('An ID must be provided for each model');
	}

	try {
		const resp = await API.get(`model-service/models/stratify/${modelA}/${modelB}/${typeModel}`);
		const output = resp.data;
		return output;
	} catch (err) {
		console.log(err);
		return null;
	}
	// this.createModel(output, true);
}
