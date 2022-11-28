// Providing the ID of 3 Models (model A, model B, and the type Model)
// Create a new model of based off of the stratification
// TODO: Better error handling (models not found for example)
//       Override function to also accept 2 typed models (need to chat with TA2 to fully understand)
export async function fetchStratificationResult(modelA: string, modelB: string, typeModel: string) {
	if (!modelA || !modelB || !typeModel) {
		throw new Error('An ID must be provided for each model');
	}
	const resp = await fetch(
		`http://localhost:8888/api/models/stratify/${modelA}/${modelB}/${typeModel}`,
		{
			method: 'GET'
		}
	);
	const output = await resp.json();
	return output;
}

export async function fetchStratificationWithTypedModels(modelA: string, modelB: string) {
	if (!modelA || !modelB) {
		throw new Error('An ID must be provided for each model');
	}
	const resp = await fetch(
		`http://localhost:8888/api/models/stratifyWithTypedModels/${modelA}/${modelB}`,
		{
			method: 'GET'
		}
	);
	const output = await resp.json();
	return output;
}
