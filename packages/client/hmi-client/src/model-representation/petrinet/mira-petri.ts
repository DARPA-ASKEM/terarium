import _ from 'lodash';
import { Model, PetriNetTransition } from '@/types/Types';
import { createMatrix1D } from '@/utils/pivot';
import { StratifiedMatrix } from '@/types/Model';
import { createParameterMatrix, createTransitionMatrix } from './petri-matrix-service';

/**
 * Note "id" and "base" used for building the compact graph, they should not be used as strata dimensions
 */
export const getStates = (amr: Model) => {
	const model = amr.model;
	const lookup = new Map();
	const matrixData: any[] = [];

	const dupe: Set<string> = new Set();
	const uniqueStates: any[] = [];

	for (let i = 0; i < model.states.length; i++) {
		const state = model.states[i];
		const grounding = state.grounding;

		const obj: any = {};
		obj.id = state.id;
		obj.name = state.name;

		if (grounding && grounding.modifiers) {
			const modifierKeys = Object.keys(grounding.modifiers);
			let str = state.id;
			modifierKeys.forEach((key) => {
				str = str.replace(`_${grounding.modifiers[key]}`, '');
				obj[key] = [grounding.modifiers[key]];
			});
			obj.base = str;

			lookup.set(state.id, str);
			if (!dupe.has(str)) {
				uniqueStates.push({
					id: str,
					name: str,
					grounding: { identifiers: grounding.identifiers }
				});
			}
			dupe.add(str);
		} else {
			obj.base = state.id;
			lookup.set(state.id, state.id);
			if (!dupe.has(state.id)) {
				uniqueStates.push({
					id: state.id,
					name: state.id,
					grounding: { identifiers: grounding.identifiers }
				});
			}
			dupe.add(state.id);
		}
		matrixData.push(obj);
	}
	return { uniqueStates, lookup, matrixData };
};

/**
 * Note "id" and "base" used for building the compact graph, they should not be used as strata dimensions
 */
export const getTransitions = (amr: Model, lookup: Map<string, string>) => {
	const model = amr.model;
	const uniqueTransitions: Partial<PetriNetTransition>[] = [];
	const matrixData: any[] = [];

	// Cache state-modifiers for faster fetch
	const stateModifierMap = new Map();
	model.states.forEach((state) => {
		stateModifierMap.set(state.id, state.grounding.modifiers);
	});

	const isSameTransition = (t1: Partial<PetriNetTransition>, t2: Partial<PetriNetTransition>) =>
		_.isEqual(t1.input, t2.input) && _.isEqual(t1.output, t2.output);

	let c = 0;

	// Rebuild transitions by contracting the connecting states and fnding the
	// unique set at the end
	for (let i = 0; i < model.transitions.length; i++) {
		const obj: any = {};
		const transition = model.transitions[i];
		const input = transition.input.map((d: any) => lookup.get(d));
		const output = transition.output.map((d: any) => lookup.get(d));
		const newTransition = { id: '', input, output };

		// Build matrixData array
		obj.id = transition.id;
		transition.input.forEach((sid: string) => {
			const modifiers = stateModifierMap.get(sid);
			if (modifiers) {
				Object.keys(modifiers).forEach((k) => {
					if (obj[k] && !obj[k].includes(modifiers[k])) {
						obj[k].push(modifiers[k]);
					} else {
						obj[k] = [modifiers[k]];
					}
				});
			}
		});
		transition.output.forEach((sid: string) => {
			const modifiers = stateModifierMap.get(sid);
			if (modifiers) {
				Object.keys(modifiers).forEach((k) => {
					if (obj[k] && !obj[k].includes(modifiers[k])) {
						obj[k].push(modifiers[k]);
					} else {
						obj[k] = [modifiers[k]];
					}
				});
			}
		});

		const existingTransition = uniqueTransitions.find((d) => isSameTransition(d, newTransition));

		if (!existingTransition) {
			const newId = `T${c++}`;
			newTransition.id = newId;
			uniqueTransitions.push(_.cloneDeep(newTransition));
			obj.base = newId;
		} else {
			obj.base = existingTransition.id;
		}
		matrixData.push(obj);
	}
	return { uniqueTransitions, matrixData };
};

export const extractNestedStratas = (matrixData: any[], stratas: string[]) => {
	if (stratas.length === 0) {
		return {};
	}
	const strataKey = stratas[0];
	let result: any = _.groupBy(matrixData, stratas[0]);

	const nextStratas = _.clone(stratas);
	nextStratas.shift();

	// Bake in strata-type
	if (!_.isEmpty(result)) {
		result._key = strataKey;
	}

	Object.keys(result).forEach((key) => {
		if (key === '_key') return;

		if (key === 'undefined') {
			// No result, skip and start on the next
			result = extractNestedStratas(matrixData, nextStratas);
		} else {
			// Go down to the next depth
			result[key] = extractNestedStratas(result[key], nextStratas);
		}
	});

	return result;
};

/**
 * Given an amr, find the unstratified/root parameters.
 *
 * This requires some heuristics go backwards, may not work all the time.
 * This works poorly if the parameter ids starts off with underscores.
 *
 * For example "beta_1_1", "beta_1_2" will collapse into "beta": ["beta_1_1", "beta_1_2"]
 */
export const getUnstratifiedParameters = (amr: Model) => {
	const parameters = amr.semantics?.ode.parameters || [];
	const map = new Map<string, string[]>();
	parameters.forEach((p) => {
		const rootName = _.first(p.id.split('_')) as string;
		const name = p.name ? p.name : rootName;
		if (map.has(name)) {
			map.get(name)?.push(p.id);
		} else {
			map.set(name, [p.id]);
		}
	});
	return map;
};

export const getUnstratifiedInitials = (amr: Model) => {
	const initials = amr.semantics?.ode.initials || [];
	const map = new Map<string, string[]>();
	initials.forEach((i) => {
		const rootName = _.first(i.target.split('_')) as string;
		if (map.has(rootName)) {
			map.get(rootName)?.push(i.target);
		} else {
			map.set(rootName, [i.target]);
		}
	});
	return map;
};

// Holds all points that have the parameter
export const filterParameterLocations = (
	amr: Model,
	transitionMatrixData: any[],
	parameterIds: string[]
) =>
	transitionMatrixData.filter((d) => {
		// Check if the transition's expression include the usage
		const rate = amr.semantics?.ode.rates.find((r) => r.target === d.id);
		if (!rate) return false;
		// FIXME: should check through sympy to be more accurate
		for (let i = 0; i < parameterIds.length; i++) {
			if (rate.expression.includes(parameterIds[i])) return true;
		}
		return false;
	});

/**
 * Given an MIRA AMR, extract and compute a presentation-layer data format
 */
export const getMiraAMRPresentationData = (amr: Model) => {
	const statesData = getStates(amr);
	const transitionsData = getTransitions(amr, statesData.lookup);

	const compactModel = {
		model: {
			states: statesData.uniqueStates,
			transitions: transitionsData.uniqueTransitions
		},
		semantics: {
			ode: {}
		}
	};

	return {
		compactModel,
		stateMatrixData: statesData.matrixData,
		transitionMatrixData: transitionsData.matrixData
	};
};

export const generateMatrix = (amr: Model, id: string, stratifiedMatrixType: StratifiedMatrix) => {
	const { stateMatrixData, transitionMatrixData } = getMiraAMRPresentationData(amr);

	// Get only the states/transitions that are mapped to the base model
	let matrixData: any[] = [];
	let childParameterIds: string[] = [];

	if (stratifiedMatrixType === StratifiedMatrix.Initials) {
		matrixData = stateMatrixData.filter(({ base }) => base === id);
	} else if (stratifiedMatrixType === StratifiedMatrix.Parameters) {
		const paramsMap = getUnstratifiedParameters(amr);
		if (!paramsMap.has(id)) return null;

		// IDs to find within the rates
		childParameterIds = paramsMap.get(id) as string[];
		// Holds all points that have the parameter
		matrixData = filterParameterLocations(amr, transitionMatrixData, [...childParameterIds, id]);
	} else if (stratifiedMatrixType === StratifiedMatrix.Rates) {
		matrixData = transitionMatrixData.filter(({ base }) => base === id);
	}

	if (_.isEmpty(matrixData)) return null;

	let matrix: any[] = [];

	if (stratifiedMatrixType === StratifiedMatrix.Initials) {
		matrix = createMatrix1D(matrixData).matrix;
	} else if (stratifiedMatrixType === StratifiedMatrix.Parameters) {
		matrix = createParameterMatrix(amr, matrixData, childParameterIds).matrix;
	} else if (stratifiedMatrixType === StratifiedMatrix.Rates) {
		matrix = createTransitionMatrix(amr, matrixData).matrix;
	}
	return matrix;
};
