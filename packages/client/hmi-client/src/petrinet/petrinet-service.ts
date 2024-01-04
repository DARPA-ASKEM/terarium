import { IGraph } from '@graph-scaffolder/types';
import { AxiosError } from 'axios';
import { logger } from '@/utils/logger';
import API from '@/api/api';

export interface PetriNet {
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
	// Identifies the states connected by an edge going from state -> transition
	it: number; // Transition ID which is the target
	is: number; // State ID which is the source
}
interface Output {
	// Identifies the states connected by an edge going from transition -> state
	ot: number; // Transition ID which is the source
	os: number; // State ID which is the target
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

enum NodeType {
	State = 'S',
	Transition = 'T'
}

export interface NodeData {
	type: string;
	uid?: string | number;
}

export interface EdgeData {
	numEdges: number;
}

/**
 * Given a petrinet model convert to an IGraph representation g
 * for the renderer
 * First add each node found in S and T, then add each edge found in I and O
 */
interface PetriSizeConfig {
	S: {
		width: number;
		height: number;
	};
	T: {
		width: number;
		height: number;
	};
}
const defaultSizeConfig: PetriSizeConfig = {
	S: { width: 40, height: 40 },
	T: { width: 40, height: 40 }
};

// @deprecated
export const parsePetriNet2IGraph = (
	model: PetriNet,
	config: PetriSizeConfig = defaultSizeConfig
) => {
	const result: IGraph<NodeData, EdgeData> = {
		width: 500,
		height: 500,
		nodes: [],
		edges: []
	};

	// add each nodes in S
	for (let i = 0; i < model.S.length; i++) {
		const aNode = model.S[i];
		result.nodes.push({
			id: `s-${i + 1}`,
			label: aNode.sname,
			x: 0,
			y: 0,
			height: config.S.height,
			width: config.S.width,
			data: { type: NodeType.State, uid: aNode.uid },
			nodes: []
		});
	}

	// Add each node found in T
	for (let i = 0; i < model.T.length; i++) {
		const aTransition = model.T[i];
		result.nodes.push({
			id: `t-${i + 1}`,
			label: aTransition.tname,
			x: 0,
			y: 0,
			height: config.T.height,
			width: config.T.width,
			data: { type: NodeType.Transition, uid: aTransition.uid },
			nodes: []
		});
	} // end T

	// Edges found in I
	for (let i = 0; i < model.I.length; i++) {
		const iEdges = model.I[i];
		const sourceId = `s-${iEdges.is}`;
		const targetId = `t-${iEdges.it}`;

		// Collapse hyper edges
		const existingEdge = result.edges.find(
			(edge) => edge.source === sourceId && edge.target === targetId
		);
		if (existingEdge && existingEdge.data) {
			existingEdge.data.numEdges++;
			continue;
		}

		result.edges.push({
			source: sourceId,
			target: targetId,
			points: [],
			data: {
				numEdges: 1
			}
		});
	}
	// Edges found in O
	for (let i = 0; i < model.O.length; i++) {
		const oEdges = model.O[i];
		const sourceId = `t-${oEdges.ot}`;
		const targetId = `s-${oEdges.os}`;

		// Collapse hyper edges
		const existingEdge = result.edges.find(
			(edge) => edge.source === sourceId && edge.target === targetId
		);
		if (existingEdge && existingEdge.data) {
			existingEdge.data.numEdges++;
			continue;
		}

		result.edges.push({
			source: sourceId,
			target: targetId,
			points: [],
			data: {
				numEdges: 1
			}
		});
	}
	return result;
};
