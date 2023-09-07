import { logger } from '@/utils/logger';
import API from '@/api/api';
import {
	Initial,
	Model,
	ModelParameter,
	ModelSemantics,
	PetriNetTransition,
	Rate,
	State,
	Transition
} from '@/types/Types';
import { updateRateExpressionWithParam } from '@/model-representation/petrinet/petrinet-service';

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
	const states: State[] = stateNames.map((name) => ({
		id: name,
		name,
		description: `Number of individuals relative to the total population that are in age group ${name}.`,
		units: {
			expression: 'person',
			expression_mathml: '<ci>person</ci>'
		}
	}));
	const transitions: Transition[] = [];
	const parameters: ModelParameter[] = [];
	states.forEach((outerState, i) => {
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
		});
	});
	states.forEach((outerState, i) => {
		states.forEach((innerState, j) => {
			parameters.push({
				id: `beta${i + 1}${j + 1}`,
				description: `infection rate from age group '${outerState.name}' to '${innerState.name}'`,
				value: 1 / transitions.length
			});
		});
	});
	states.forEach((state, i) => {
		parameters.push({
			id: `${states[i].id}init`,
			description: `Proportion of population in age group '${state.name}' at timestep 0`,
			value: 1 / states.length
		});
	});
	const typeMap: string[][] = states
		.map((state) => [state.id, 'Pop'])
		.concat(transitions.map((transition) => [transition.id, 'Infect']));
	const typeSystem = {
		states: [
			{
				id: 'Pop',
				name: 'Pop',
				description: 'Compartment of individuals in a human population.'
			}
		],
		transitions: [
			{
				id: 'Infect',
				input: ['Pop', 'Pop'],
				output: ['Pop', 'Pop'],
				properties: {
					name: 'Infect',
					description:
						'2-to-2 interaction that represents infectious contact between two human individuals.'
				}
			}
		]
	};
	const rates: Rate[] = [];
	transitions.forEach((t) =>
		rates.push({
			target: t.id,
			expression: '',
			expression_mathml: ''
		})
	);
	const initials: Initial[] = [];
	states.forEach((s) =>
		initials.push({
			target: s.id,
			expression: `${s.id}init`,
			expression_mathml: `<ci>${s.id}init</ci>`
		})
	);
	const semantics: ModelSemantics = {
		ode: {
			rates,
			initials,
			parameters
		},
		typing: {
			system: {
				name: 'Age-contact strata model',
				description: 'Age-contact strata model',
				schema:
					'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.1/petrinet/petrinet_schema.json',
				model_version: '0.1',
				model: typeSystem
			},
			map: typeMap
		}
	};
	const model: Model = {
		id: 'age-contact',
		header: {
			name: 'Age-contact strata model',
			description: 'Age-contact strata model',
			schema:
				'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.1/petrinet/petrinet_schema.json',
			schema_name: 'petrinet',
			model_version: '0.1'
		},
		model: {
			states,
			transitions
		},
		semantics,
		metadata: {
			processed_at: 0,
			processed_by: '',
			variable_statements: [],
			annotations: {},
			attributes: []
		}
	};
	transitions.forEach((t, i) =>
		updateRateExpressionWithParam(model, t as PetriNetTransition, parameters[i].id, '')
	);
	return model;
}

export function generateLocationStrataModel(stateNames: string[]): Model {
	const states: State[] = stateNames.map((name) => ({
		id: name,
		name,
		description: `Number of individuals relative to the total population that are in location ${name}.`,
		units: {
			expression: 'person',
			expression_mathml: '<ci>person</ci>'
		}
	}));
	const transitions: Transition[] = [];
	const parameters: ModelParameter[] = [];
	states.forEach((outerState, i) =>
		states.forEach((innerState, j) => {
			if (i !== j) {
				transitions.push({
					id: `t${i + 1}${j + 1}`,
					input: [outerState.id],
					output: [innerState.id],
					properties: {
						name: `t&#832${i + 1}&#832${j + 1}`,
						description: `Travel of an individual from location ${states[i].name} and ${states[j].name}.`
					}
				});
			}
		})
	);
	states.forEach((outerState, i) => {
		states.forEach((innerState, j) => {
			if (i !== j) {
				parameters.push({
					id: `tau${i + 1}${j + 1}`,
					description: `travel rate from location '${outerState.name}' to '${innerState.name}'`,
					value: 1 / transitions.length
				});
			}
		});
	});
	states.forEach((state, i) => {
		parameters.push({
			id: `${states[i].id}init`,
			description: `Proportion of population in location '${state.name}' at timestep 0`,
			value: 1 / states.length
		});
	});
	const typeMap: string[][] = states
		.map((state) => [state.id, 'Pop'])
		.concat(transitions.map((transition) => [transition.id, 'Strata']));
	const rates: Rate[] = [];
	transitions.forEach((t) =>
		rates.push({
			target: t.id,
			expression: '',
			expression_mathml: ''
		})
	);
	const initials: Initial[] = [];
	states.forEach((s) =>
		initials.push({
			target: s.id,
			expression: `${s.id}init`,
			expression_mathml: `<ci>${s.id}init</ci>`
		})
	);
	const typeSystem = {
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
	};
	const semantics: ModelSemantics = {
		ode: {
			rates,
			initials,
			parameters
		},
		typing: {
			system: {
				name: 'Location-travel strata model',
				description: 'Location-travel strata model',
				schema:
					'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.1/petrinet/petrinet_schema.json',
				model_version: '0.1',
				model: typeSystem
			},
			map: typeMap
		}
	};
	const model: Model = {
		id: 'location-travel',
		header: {
			name: 'Location-travel strata model',
			description: 'Location-travel strata model',
			schema:
				'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.1/petrinet/petrinet_schema.json',
			schema_name: 'petrinet',
			model_version: '0.1'
		},
		model: {
			states,
			transitions
		},
		semantics,
		metadata: {
			processed_at: 0,
			processed_by: '',
			variable_statements: [],
			annotations: {},
			attributes: []
		}
	};
	transitions.forEach((t, i) =>
		updateRateExpressionWithParam(model, t as PetriNetTransition, parameters[i].id, '')
	);
	return model;
}

export function generateTypeState(amr: Model, stateId: string, typeId: string): State | null {
	const states = amr.semantics?.typing?.system.states ?? [];
	const existingState = states.find((s) => s.id === stateId);
	if (existingState) {
		return null;
	}
	return {
		id: typeId,
		name: typeId,
		description: typeId
	};
}

/*
	Return a Transition with inferred inputs and outputs based on a partially typed amr.
	Return null if type inference cannot be completed for whatever reason.
	*/
export function generateTypeTransition(
	amr: Model,
	transitionId: string,
	typeId: string
): Transition | null {
	/* Get inputs and outputs of transition from transitionId
	FIterate through typeMap and get the corresponding type id for each transition id in input/output, where the first value of each element in typeMap
	is the transition id, and the second element is the type id. */
	const typeMap: string[][] | undefined = amr.semantics?.typing?.map;
	const transitions: Transition[] = amr.semantics?.typing?.system.transitions ?? [];
	const existingTransition = transitions.find((t) => t.id === transitionId);
	if (existingTransition) {
		return null;
	}
	if (!typeMap) {
		return null;
	}
	const transition: Transition = amr.model.transitions.find(
		(t) => t.id === transitionId
	) as Transition;
	if (!transition) {
		return null;
	}
	const inputs: string[] = transition.input;
	const outputs: string[] = transition.output;

	const typeInputs: string[] = [];
	const typeOutputs: string[] = [];
	inputs.forEach((i) => {
		const map = typeMap.find((m) => m[0] === i);
		if (map && map.length === 2) {
			typeInputs.push(map[1]);
		}
	});
	outputs.forEach((o) => {
		const map = typeMap.find((m) => m[0] === o);
		if (map && map.length === 2) {
			typeOutputs.push(map[1]);
		}
	});

	/* typeInput and typeOutput should be 1:1 mappings of inputs and outputs, respectively, therefore lengths should be the same.
	If they are not, that means no mapping was found for at least one of the elements of inputs/outputs, meaning the type system
	in the amr was invalid. */
	if (inputs.length !== typeInputs.length || outputs.length !== typeInputs.length) {
		return null;
	}

	return {
		id: typeId,
		input: typeInputs,
		output: typeOutputs,
		properties: {
			name: typeId,
			description: typeId
		}
	};
}
