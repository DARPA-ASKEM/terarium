import { logger } from '@/utils/logger';
import API from '@/api/api';
import { State, Transition } from '@/types/Types';

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
		logger.error(err);
		return null;
	}
	// this.createModel(output, true);
}

export function generateAgeStrataModel(stateNames: string[]) {
	const states: State[] = stateNames.map((name, index) => ({
		id: `A${index + 1}`,
		name,
		description: `Number of individuals relative to the total population that are in age group A${
			index + 1
		}`
	}));
	const transitions: Transition[] = [];
	states.forEach((outerState, i) =>
		states.forEach((innerState, j) => {
			transitions.push({
				id: `c${i + 1}${j + 1}`,
				input: [outerState.id, innerState.id],
				output: [outerState.id, innerState.id],
				properties: {
					name: `c&#832${i + 1}&#832${j + 1}`,
					description: 'Infective interaction between individuals'
				}
			});
		})
	);
	return {
		name: 'Age-contact strata model',
		description: 'Age-contact strata model',
		model: {
			states,
			transitions
		}
	};
}

export function generateLocationStrataModel(stateNames: string[]) {
	const states: State[] = stateNames.map((name, index) => ({
		id: `L${index + 1}`,
		name,
		description: `Number of individuals relative to the total population that are in location L${
			index + 1
		}`
	}));
	const transitions: Transition[] = [];
	states.forEach((outerState, i) =>
		states.forEach((innerState, j) => {
			if (i !== j) {
				transitions.push({
					id: `t${i + 1}${j + 1}`,
					input: [outerState.id],
					output: [innerState.id],
					properties: {
						name: `t&#832${i + 1}&#832${j + 1}`,
						description: `Travel of an individual from location L${i + 1} and L${j + 1}`
					}
				});
			}
		})
	);
	return {
		name: 'Location-travel strata model',
		description: 'Location-travel strata model',
		model: {
			states,
			transitions
		}
	};
}
