import API from '@/api/api';
import { updateModelConfiguration } from '@/services/model-configurations';
import { Model, ModelConfiguration, PetriNetModel, PetriNetTransition } from '@/types/Types';
import { logger } from '@/utils/logger';
import { IGraph } from '@graph-scaffolder/types';
import { AxiosError } from 'axios';
import _ from 'lodash';

// deprecated section - this is the acset representation, we should do the conversion on model-service
interface PetriNet {
	S: State[]; // List of state names
	T: Transition[]; // List of transition names
	I: Input[]; // List of inputs
	O: Output[]; // List of outputs
}
interface State {
	sname: string;
	uid?: string | number;
}
interface Transition {
	tname: string;
	uid?: string | number;
}
interface Input {
	it: number;
	is: number;
}
interface Output {
	ot: number;
	os: number;
}
// end deprecated section

export interface NodeData {
	type: string;
	strataType?: string;
	expression?: string;
}

export interface EdgeData {
	numEdges: number;
}

export enum StratifiedModel {
	Mira = 'mira',
	Catlab = 'catlab'
}

// Transform list of mathML strings to a petrinet ascet
export const mathmlToPetri = async (mathml: string[]) => {
	try {
		const resp = await API.post('/transforms/mathml-to-acset', mathml);

		if (resp && resp.status === 200 && resp.data) {
			return resp.data;
		}
		logger.error('mathmlToPetri: Server did not provide a correct response', { showToast: false });
	} catch (error: unknown) {
		if ((error as AxiosError).isAxiosError) {
			const axiosError = error as AxiosError;
			logger.error('mathmlToPetri Error: ', axiosError.response?.data || axiosError.message, {
				showToast: false
			});
		} else {
			logger.error(error, { showToast: false });
		}
	}
	return null;
};

// Transform a petrinet into latex
export const petriToLatex = async (petri: PetriNet): Promise<string | null> => {
	try {
		const payloadPetri = {
			S: petri.S.map((s) => ({ sname: s.sname })),
			T: petri.T.map((t) => ({ tname: t.tname })),
			I: petri.I,
			O: petri.O
		};

		const resp = await API.post('/transforms/acset-to-latex', payloadPetri);

		if (resp && resp.status === 200 && resp.data && typeof resp.data === 'string') {
			return resp.data;
		}
		if (resp && resp.status === 204) return null;

		logger.error('[Model Service] petriToLatex: Server did not provide a correct response', {
			showToast: false,
			toastTitle: 'Model Service'
		});
	} catch (error: unknown) {
		if ((error as AxiosError).isAxiosError) {
			const axiosError = error as AxiosError;
			logger.error('petriToLatex Error:', axiosError.response?.data || axiosError.message, {
				showToast: false
			});
		} else {
			logger.error(error, { showToast: false });
		}
	}
	return null;
};

// Used to derive equations
// AMR => ACSet => ODE => Equation => Latex
export const convertAMRToACSet = (amr: Model) => {
	const result: PetriNet = {
		S: [],
		T: [],
		I: [],
		O: []
	};

	const petrinetModel = amr.model as PetriNetModel;

	petrinetModel.states.forEach((s) => {
		result.S.push({ sname: s.id });
	});

	petrinetModel.transitions.forEach((t) => {
		result.T.push({ tname: t.id });
	});

	petrinetModel.transitions.forEach((transition) => {
		transition.input.forEach((input) => {
			result.I.push({
				is: result.S.findIndex((s) => s.sname === input) + 1,
				it: result.T.findIndex((t) => t.tname === transition.id) + 1
			});
		});
	});

	petrinetModel.transitions.forEach((transition) => {
		transition.output.forEach((output) => {
			result.O.push({
				os: result.S.findIndex((s) => s.sname === output) + 1,
				ot: result.T.findIndex((t) => t.tname === transition.id) + 1
			});
		});
	});

	// post processing to use expressions rather than ids
	result.T = result.T.map((transition) => {
		const foundRate = amr.semantics?.ode?.rates?.find((rate) => rate.target === transition.tname);

		// default to the id if there is a case where there is no expression
		return { tname: foundRate ? `(${foundRate!.expression})` : transition.tname };
	});

	return result;
};

export const convertToIGraph = (amr: Model) => {
	const result: IGraph<NodeData, EdgeData> = {
		width: 500,
		height: 500,
		amr: _.cloneDeep(amr),
		nodes: [],
		edges: []
	};

	const petrinetModel = amr.model as PetriNetModel;

	petrinetModel.states.forEach((state) => {
		// The structure of map is an array of arrays, where each inner array has 2 elements.
		// The first element is a state or transition id, the second element is the type id.
		// Find the inner array that matches the current state / transition that we are iterating on
		// Get the second element of that array, which is the id of its type
		const typeMap = amr.semantics?.typing?.map.find(
			(map) => map.length === 2 && state.id === map[0]
		);
		const strataType = typeMap?.[1] ?? '';
		result.nodes.push({
			id: state.id,
			label: state.name ?? state.id,
			type: 'state',
			x: 0,
			y: 0,
			width: 100,
			height: 100,
			data: { type: 'state', strataType },
			nodes: []
		});
	});

	petrinetModel.transitions.forEach((transition) => {
		// The structure of map is an array of arrays, where each inner array has 2 elements.
		// The first element is a state or transition id, the second element is the type id.
		// Find the inner array that matches the current state / transition that we are iterating on
		// Get the second element of that array, which is the id of its type
		const typeMap = amr.semantics?.typing?.map.find(
			(map) => map.length === 2 && transition.id === map[0]
		);

		const strataType = typeMap?.[1] ?? '';
		result.nodes.push({
			id: transition.id,
			label: transition.id,
			type: 'transition',
			x: 0,
			y: 0,
			width: 40,
			height: 40,
			data: { type: 'transition', strataType },
			nodes: []
		});
	});

	petrinetModel.transitions.forEach((transition) => {
		transition.input.forEach((input) => {
			const key = `${input}:${transition.id}`;

			// Collapse hyper edges
			const existingEdge = result.edges.find((edge) => edge.id === key);
			if (existingEdge && existingEdge.data) {
				existingEdge.data.numEdges++;
				return;
			}

			result.edges.push({
				id: key,
				source: input,
				target: transition.id,
				points: [],
				data: { numEdges: 1 }
			});
		});
	});

	petrinetModel.transitions.forEach((transition) => {
		transition.output.forEach((output) => {
			const key = `${transition.id}:${output}`;

			// Collapse hyper edges
			const existingEdge = result.edges.find((edge) => edge.id === key);
			if (existingEdge && existingEdge.data) {
				existingEdge.data.numEdges++;
				return;
			}

			result.edges.push({
				id: key,
				source: transition.id,
				target: output,
				points: [],
				data: { numEdges: 1 }
			});
		});
	});
	return result;
};

export const convertToAMRModel = (g: IGraph<NodeData, EdgeData>) => g.amr;

// Update a transition's expression and expression_mathml fields based on
// mass-kinetics
export const updateRateExpression = (
	amr: Model,
	transition: PetriNetTransition,
	transitionExpression: string
) => {
	const param = amr.semantics?.ode?.rates?.find((d) => d.target === transition.id);
	if (!param) return;

	updateRateExpressionWithParam(amr, transition, `${param.target}Param`, transitionExpression);
};

export const updateRateExpressionWithParam = (
	amr: Model,
	transition: PetriNetTransition,
	parameterId: string,
	transitionExpression: string
) => {
	const rate = amr.semantics?.ode?.rates?.find((d) => d.target === transition.id);
	if (!rate) return;

	let expression = '';
	let expressionMathml = '';

	if (transitionExpression === '') {
		const param = amr.semantics?.ode?.parameters?.find((d) => d.id === parameterId);
		const inputStr = transition.input.map((d) => `${d}`);
		if (!param) return;
		// eslint-disable-next-line
		expression = inputStr.join('*') + '*' + param.id;
		// eslint-disable-next-line
		expressionMathml =
			`<apply><times/>${inputStr.map((d) => `<ci>${d}</ci>`).join('')}<ci>${param.id}</ci>` +
			`</apply>`;
	} else {
		expression = transitionExpression;
		expressionMathml = transitionExpression;
	}

	rate.target = transition.id;
	rate.expression = expression;
	rate.expression_mathml = expressionMathml;
};

const replaceExactString = (str: string, wordToReplace: string, replacementWord: string): string =>
	str.trim() === wordToReplace.trim() ? str.replace(wordToReplace, replacementWord) : str;

const replaceValuesInExpression = (
	expression: string,
	wordToReplace: string,
	replaceWord: string
): string => {
	let expressionBuilder = '';
	let isOperator = false;
	let content = '';

	[...expression].forEach((c) => {
		// not sure if this is an exhaustive list of operators or if it includes any operators it shouldn't
		if ([',', '(', ')', '+', '-', '*', '/', '^'].includes(c)) {
			isOperator = true;
		} else {
			isOperator = false;
		}

		if (isOperator) {
			expressionBuilder += replaceExactString(content, wordToReplace, replaceWord);
			content = '';
			expressionBuilder += c;
		}
		if (!isOperator) {
			content += c;
		}
	});

	// if we reach the end of an expression and it doesn't end with an operator, we need to add the updated content
	if (!isOperator) {
		expressionBuilder += replaceExactString(content, wordToReplace, replaceWord);
	}

	return expressionBuilder;
};

// function to replace the content inside the tags of a mathml expression
const replaceValuesInMathML = (
	mathmlExpression: string,
	wordToReplace: string,
	replaceWord: string
): string => {
	let expressionBuilder = '';
	let isTag = false;
	let content = '';

	[...mathmlExpression].forEach((c) => {
		if (!isTag && c === '<') {
			isTag = true;
		}
		if (isTag && c === '>') {
			isTag = false;
		}

		if (isTag) {
			expressionBuilder += replaceExactString(content, wordToReplace, replaceWord);
			content = '';
			expressionBuilder += c;
		}
		if (!isTag) {
			// this only works if there is no '>' literal in the non-tag content
			if (c !== '>') {
				content += c;
			} else {
				expressionBuilder += c;
			}
		}
	});

	return expressionBuilder;
};

export const updateParameterId = (amr: Model, id: string, newId: string) => {
	if (amr.semantics?.ode.parameters) {
		amr.semantics.ode.parameters.forEach((param) => {
			if (param.id === id) {
				param.id = newId;
			}
		});

		// update the expression and expression_mathml fields
		amr.semantics.ode?.rates?.forEach((rate) => {
			rate.expression = replaceValuesInExpression(rate.expression, id, newId);
			if (rate.expression_mathml) {
				rate.expression_mathml = replaceValuesInMathML(rate.expression_mathml, id, newId);
			}
		});

		// if there's a timeseries field with the old parameter id then update it to the new id
		if (amr.metadata?.timeseries && amr.metadata.timeseries[id]) {
			amr.metadata.timeseries[newId] = amr.metadata.timeseries[id];
			delete amr.metadata.timeseries[id];
		}
	}
};

export const updateConfigFields = async (
	modelConfigs: ModelConfiguration[],
	id: string,
	newId: string
) => {
	modelConfigs.forEach((config) => {
		updateParameterId(config.configuration, id, newId);
		// note that this is making an async call but we don't need to wait for it to finish
		// since we don't immediately need the updated configs
		updateModelConfiguration(config);
	});
};

export const mergeMetadata = (amr: Model, amrOld: Model) => {
	console.log(amr, amrOld);
};

/**
 * Stratification
 * */

// Heuristic to get the straitified modifier mappings, we assume that
// - if there is a single unique value for modifier-key then it is not user initiated stratification
// - if the modifier value starts with 'ncit:' then it is not a user initiated stratification
export const getModifierMap = (amr: Model) => {
	const modifierMap: Map<string, Set<string>> = new Map();
	(amr.model as PetriNetModel).states.forEach((s) => {
		if (s.grounding && s.grounding.modifiers) {
			const modifiers = s.grounding.modifiers;
			const keys: string[] = Object.keys(modifiers);
			keys.forEach((key) => {
				if (!modifierMap.has(key)) {
					modifierMap.set(key, new Set());
				}
				const modifier = modifiers[key];
				if (!modifier.startsWith('ncit:')) {
					modifierMap.get(key)?.add(modifiers[key]);
				}
			});
		}
	});
	return modifierMap;
};

// Check if AMR is a stratified AMR
export const getStratificationType = (amr: Model) => {
	const modifierMap = getModifierMap(amr);
	// eslint-disable-next-line
	for (const ele of modifierMap) {
		if (ele[1].size > 1) {
			return StratifiedModel.Mira;
		}
	}
	return null;

	// if (amr.semantics?.span && amr.semantics.span.length > 1) return StratifiedModel.Catlab;
	//
	// const hasModifiers = some(
	// 	(amr.model as PetriNetModel).states,
	// 	(s) =>
	// 		s.grounding &&
	// 		s.grounding.modifiers &&
	// 		!isEmpty(Object.keys(s.grounding.modifiers)) &&
	// 		// Temp hack to reject SBML type models with actual groundings, may not work
	// 		// all the time, MIRA will move strata info to metadata section - Oct 2023
	// 		s.id.includes('_')
	// );
	// if (hasModifiers) return StratifiedModel.Mira;
	// return null;
};

export function newAMR(modelName: string) {
	const amr: Model = {
		header: {
			name: modelName,
			description: '',
			schema:
				'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.5/petrinet/petrinet_schema.json',
			schema_name: 'petrinet',
			model_version: '0.1'
		},
		id: '',
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
