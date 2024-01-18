import _, { cloneDeep, isEmpty, some } from 'lodash';
import API from '@/api/api';
import { IGraph } from '@graph-scaffolder/types';
import {
	Model,
	ModelConfiguration,
	PetriNetModel,
	PetriNetTransition,
	TypingSemantics
} from '@/types/Types';
import { updateModelConfiguration } from '@/services/model-configurations';
import { logger } from '@/utils/logger';
import { AxiosError } from 'axios';

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
		const foundRate = amr.semantics?.ode.rates.find((rate) => rate.target === transition.tname);

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

const DUMMY_VALUE = -999;
export const convertToAMRModel = (g: IGraph<NodeData, EdgeData>) => g.amr;

export const addState = (amr: Model, id: string, name: string) => {
	amr.model.states.push({
		id,
		name,
		description: ''
	});
	amr.semantics?.ode.initials?.push({
		target: id,
		expression: `${id}init`,
		expression_mathml: `<ci>${id}init</ci>`
	});
	amr.semantics?.ode.parameters?.push({
		id: `${id}init`,
		name: '',
		description: '',
		value: DUMMY_VALUE
	});
};

export const addTransition = (amr: Model, id: string, name: string, value?: number) => {
	amr.model.transitions.push({
		id,
		input: [],
		output: [],
		properties: {
			name,
			description: ''
		}
	});
	amr.semantics?.ode.rates?.push({
		target: id,
		expression: `${id}Param`,
		expression_mathml: `<ci>${id}Param</ci>`
	});
	amr.semantics?.ode.parameters?.push({
		id: `${id}Param`,
		name: '',
		description: '',
		value: value ?? DUMMY_VALUE
	});
};

export const removeState = (amr: Model, id: string) => {
	const model = amr.model as PetriNetModel;

	// Remove from AMR topology
	model.states = model.states.filter((d) => d.id !== id);
	model.transitions.forEach((t) => {
		_.remove(t.input, (d) => d === id);
		_.remove(t.output, (d) => d === id);
	});

	// Remove from semantics
	if (amr.semantics?.ode) {
		const ode = amr.semantics.ode;
		if (ode.initials) {
			_.remove(ode.initials, (d) => d.target === id);
		}
	}
};

export const removeTransition = (amr: Model, id: string) => {
	const model = amr.model as PetriNetModel;

	// Remove from AMR topology
	model.transitions = model.transitions.filter((d) => d.id !== id);

	// Remove from semantics
	if (amr.semantics?.ode) {
		const ode = amr.semantics.ode;
		if (ode.rates) {
			_.remove(ode.rates, (d) => d.target === id);
		}
	}
};

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
	const rate = amr.semantics?.ode.rates.find((d) => d.target === transition.id);
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

export const addEdge = (amr: Model, sourceId: string, targetId: string) => {
	const model = amr.model as PetriNetModel;
	const state = model.states.find((d) => d.id === sourceId);
	if (state) {
		// if source is a state then the target is a transition
		const transition = model.transitions.find((d) => d.id === targetId);
		if (transition) {
			transition.input.push(sourceId);
			updateRateExpression(amr, transition, '');
		}
	} else {
		// if source is a transition then the target is a state
		const transition = model.transitions.find((d) => d.id === sourceId);
		if (transition) {
			transition.output.push(targetId);
			updateRateExpression(amr, transition, '');
		}
	}
};

export const removeEdge = (amr: Model, sourceId: string, targetId: string) => {
	const model = amr.model as PetriNetModel;
	const state = model.states.find((d) => d.id === sourceId);
	if (state) {
		const transition = model.transitions.find((d) => d.id === targetId);
		if (!transition) return;

		let c = 0;
		transition.input = transition.input.filter((id) => {
			if (c === 0 && id === sourceId) {
				c++;
				return false;
			}
			return true;
		});
		updateRateExpression(amr, transition, '');
	} else {
		const transition = model.transitions.find((d) => d.id === sourceId);
		if (!transition) return;

		let c = 0;
		transition.output = transition.output.filter((id) => {
			if (c === 0 && id === targetId) {
				c++;
				return false;
			}
			return true;
		});
		updateRateExpression(amr, transition, '');
	}
};

export const updateState = (amr: Model, id: string, newId: string, newName: string) => {
	const model = amr.model as PetriNetModel;
	const state = model.states.find((d) => d.id === id);
	if (!state) return;

	state.id = newId;
	state.name = newName;

	const initial = amr.semantics?.ode.initials?.find((d) => d.target === id);
	if (!initial) return;
	initial.target = newId;

	model.transitions.forEach((transition) => {
		for (let i = 0; i < transition.input.length; i++) {
			if (transition.input[i] === id) transition.input[i] = newId;
		}
		for (let i = 0; i < transition.output.length; i++) {
			if (transition.output[i] === id) transition.output[i] = newId;
		}
	});

	model.transitions.forEach((t) => {
		updateRateExpression(amr, t, '');
	});
};

export const updateTransition = (
	amr: Model,
	id: string,
	newId: string,
	newName: string,
	newExpression: string
) => {
	const model = amr.model as PetriNetModel;
	const transition = model.transitions.find((d) => d.id === id);
	if (!transition) return;
	transition.id = newId;
	if (transition.properties) {
		transition.properties.name = newName;
	} else {
		transition.properties = {
			name: newName,
			description: newName
		};
	}

	const rate = amr.semantics?.ode.rates?.find((d) => d.target === id);
	if (!rate) return;
	rate.target = newId;

	model.transitions.forEach((t) => {
		if (t.id === id) updateRateExpression(amr, t, newExpression);
	});
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
		amr.semantics.ode.rates.forEach((rate) => {
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

// Replace typing semantics
export const addTyping = (amr: Model, typing: TypingSemantics) => {
	if (amr.semantics) {
		amr.semantics.typing = typing;
	}
};

// Add a reflexive transition loop to the state
// This is a special type of addTransition that creates a self loop
const DEFAULT_REFLEXIVE_PARAM_VALUE = 1.0;

export const addReflexives = (
	amr: Model,
	stateId: string,
	reflexiveId: string,
	numLoops: number = 1
) => {
	addTransition(amr, reflexiveId, reflexiveId, DEFAULT_REFLEXIVE_PARAM_VALUE);
	const transition = (amr.model as PetriNetModel).transitions.find((t) => t.id === reflexiveId);
	if (transition) {
		for (let i = 0; i < numLoops; i++) {
			transition.input.push(stateId);
			transition.output.push(stateId);
		}
	}
};

export const mergeMetadata = (amr: Model, amrOld: Model) => {
	console.log(amr, amrOld);
};

export const cloneModelWithExtendedTypeSystem = (amr: Model) => {
	const amrCopy = cloneDeep(amr);
	if (amrCopy.semantics?.typing) {
		const { name, description, modelSchema, modelVersion } = amrCopy.header;
		const typeSystem = {
			name,
			description,
			modelSchema,
			modelVersion,
			model: amrCopy.semantics?.typing?.system
		};
		amrCopy.semantics.typing.system = typeSystem;
	}
	return amrCopy;
};

function unifyModelTypeSystems(baseAMR: Model, strataAMR: Model) {
	// Entries in type system need to be in the same order for stratification
	// They should contain the same state and transition entries for both baseAMR and strataAMR, just in a different order
	// So just overwrite one with the other instead of sorting
	const typeSystem = baseAMR.semantics?.typing?.system;
	if (strataAMR.semantics?.typing?.system) {
		strataAMR.semantics.typing.system.model = typeSystem.model;
	}
}

export const stratify = async (baseModel: Model, strataModel: Model) => {
	unifyModelTypeSystems(baseModel, strataModel);
	const response = await API.post('/modeling-request/stratify', {
		baseModel,
		strataModel
	});
	return response.data as Model;
};

/// /////////////////////////////////////////////////////////////////////////////
// Stratification
/// /////////////////////////////////////////////////////////////////////////////

// Check if AMR is a stratified AMR
export const getStratificationType = (amr: Model) => {
	if (amr.semantics?.span && amr.semantics.span.length > 1) return StratifiedModel.Catlab;

	const hasModifiers = some(
		(amr.model as PetriNetModel).states,
		(s) =>
			s.grounding &&
			s.grounding.modifiers &&
			!isEmpty(Object.keys(s.grounding.modifiers)) &&
			// Temp hack to reject SBML type models with actual groundings, may not work
			// all the time, MIRA will move strata info to metadata section - Oct 2023
			s.id.includes('_')
	);
	if (hasModifiers) return StratifiedModel.Mira;

	return null;
};

export function newAMR(modelName: string) {
	const amr: Model = {
		header: {
			name: modelName,
			description: '',
			modelSchema:
				'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.5/petrinet/petrinet_schema.json',
			schemaName: 'petrinet',
			modelVersion: '0.1'
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
