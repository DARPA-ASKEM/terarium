// Providing the ID of 3 Models (model A, model B, and the type Model)
// Create a new model of based off of the stratification

import { json } from 'd3';

// TODO: Better error handling (models not found for example)
//       Override function to also accept 2 typed models (need to chat with TA2 to fully understand)
export async function stratify(modelA, modelB, typeModel) {
	console.log('Start Stratify');

	if (!modelA || !modelB || !typeModel) {
		return json('An ID must be provided for each model');
	}
	console.log(`Stratifying: ${modelA} ${modelB} ${typeModel}`);
	const resp = await fetch(
		`http://localhost:8888/api/models/stratify/${modelA}/${modelB}/${typeModel}`,
		{
			method: 'GET'
		}
	);
	const output = await resp.json();
	console.log(output);
	return output;
	// this.createModel(output, true);
}
