import { logger } from '@/utils/logger';
import API from '@/api/api';
import { Model, ModelSemantics, State, Transition } from '@/types/Types';

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

export function generateAgeStrataModel(stateNames: string[]): Model {
	const states: State[] = stateNames.map((name, index) => ({
		id: `A${index + 1}`,
		name,
		description: `Number of individuals relative to the total population that are in age group A${
			index + 1
		}.`,
		units: {
			expression: 'person',
			expression_mathml: '<ci>person</ci>'
		}
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
					description: 'Infective interaction between individuals.'
				}
			});
		})
	);
	const typeMap: string[][] = states
		.map((state) => [state.id, 'Pop'])
		.concat(transitions.map((transition) => [transition.id, 'Strata']));
	const semantics: ModelSemantics = {
		ode: {
			rates: []
		},
		typing: {
			type_system: {
				states: [
					{
						id: 'Pop',
						name: 'Pop',
						description: 'Compartment of individuals in a human population.'
					}
				],
				transitions: [
					{
						id: 'Strata',
						input: ['Pop'],
						output: ['Pop'],
						properties: {
							name: 'Strata',
							description:
								'1-to-1 process that represents a change in the demographic division of a human individual.'
						}
					}
				]
			},
			type_map: typeMap
		}
	};
	return {
		id: '',
		framework: '',
		name: 'Age-contact strata model',
		description: 'Age-contact strata model',
		schema:
			'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.1/petrinet/petrinet_schema.json',
		model_version: '0.1',
		model: {
			states,
			transitions
		},
		semantics,
		metadata: {
			processed_at: 0,
			processed_by: '',
			variable_statements: []
		}
	};
}

export function generateLocationStrataModel(stateNames: string[]) {
	const states: State[] = stateNames.map((name, index) => ({
		id: `L${index + 1}`,
		name,
		description: `Number of individuals relative to the total population that are in location L${
			index + 1
		}.`,
		units: {
			expression: 'person',
			expression_mathml: '<ci>person</ci>'
		}
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
						description: `Travel of an individual from location L${i + 1} and L${j + 1}.`
					}
				});
			}
		})
	);
	const typeMap: string[][] = states
		.map((state) => [state.id, 'Pop'])
		.concat(transitions.map((transition) => [transition.id, 'Strata']));
	const semantics: ModelSemantics = {
		ode: {
			rates: []
		},
		typing: {
			type_system: {
				states: [
					{
						id: 'Pop',
						name: 'Pop',
						description: 'Compartment of individuals in a human population.'
					}
				],
				transitions: [
					{
						id: 'Strata',
						input: ['Pop'],
						output: ['Pop'],
						properties: {
							name: 'Strata',
							description:
								'1-to-1 process that represents a change in the demographic division of a human individual.'
						}
					}
				]
			},
			type_map: typeMap
		}
	};
	return {
		id: '',
		framework: '',
		name: 'Location-travel strata model',
		description: 'Location-travel strata model',
		schema:
			'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.1/petrinet/petrinet_schema.json',
		model_version: '0.1',
		model: {
			states,
			transitions
		},
		semantics,
		metadata: {
			processed_at: 0,
			processed_by: '',
			variable_statements: []
		}
	};
}
