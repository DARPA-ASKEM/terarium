import { IGraph } from '@graph-scaffolder/types';
import { PetriNetModel, PetriNetState, PetriNetTransition, Model } from '@/types/Types';
import { parseIGraph2PetriNet } from '@/petrinet/petrinet-service';
import { logger } from '@/utils/logger';

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

export interface NodeData {
	type: string;
	uid?: string | number;
}

export interface EdgeData {
	numEdges: number;
}
export enum NodeType {
	State = 'S',
	Transition = 'T'
}

export const convertToGraph = (petri: PetriNetModel) => {
	const result: IGraph<PetriNetState | PetriNetTransition, EdgeData> = {
		width: 500,
		height: 500,
		parameters: petri.parameters,
		metadata: petri.metadata,
		nodes: [],
		edges: []
	};

	petri.states.forEach((state) => {
		result.nodes.push({
			id: state.id,
			label: state.id,
			type: 'state',
			x: 0,
			y: 0,
			width: 100,
			height: 100,
			data: state,
			nodes: []
		});
	});

	petri.transitions.forEach((transition) => {
		result.nodes.push({
			id: transition.id,
			label: transition.id,
			type: 'transition',
			x: 0,
			y: 0,
			width: 100,
			height: 100,
			data: transition,
			nodes: []
		});
	});

	petri.transitions.forEach((transition) => {
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

	petri.transitions.forEach((transition) => {
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

export const convertToPetriNetModel = (g: IGraph<PetriNetState | PetriNetTransition, EdgeData>) => {
	const result: PetriNetModel = {
		states: [],
		transitions: [],
		metadata: undefined,
		parameters: undefined
	};

	// Copy
	result.metadata = g.metadata;
	result.parameters = g.parameters;
	result.states = g.nodes.filter((d) => d.type === 'state').map((d) => d.data) as PetriNetState[];
	result.transitions = g.nodes
		.filter((d) => d.type === 'transition')
		.map((d) => d.data) as PetriNetTransition[];
	return result;
};

// Used to traffic control AMRS.
// Totally useless right now as there is only 1 type of AMR (petrinet)
export const parseAMR2IGraph = (amr: Model, config: PetriSizeConfig = defaultSizeConfig) => {
	if (amr.schema.toLowerCase().includes('petri')) return parseAMRPetriNet2IGraph(amr, config);

	logger.warn(`Schema not recognized${amr}`);
	const emptyGraph: IGraph<NodeData, EdgeData> = {
		width: 500,
		height: 500,
		nodes: [],
		edges: []
	};
	return emptyGraph;
};

// Convert an AMR Petrinet -> IGraph
export const parseAMRPetriNet2IGraph = (
	amr: Model,
	config: PetriSizeConfig = defaultSizeConfig
) => {
	const result: IGraph<NodeData, EdgeData> = {
		width: 500,
		height: 500,
		nodes: [],
		edges: []
	};

	// add each nodes in S
	for (let i = 0; i < amr.model.states.length; i++) {
		const aNode = amr.model.states[i];
		result.nodes.push({
			id: aNode.id,
			label: aNode.name,
			x: 0,
			y: 0,
			height: config.S.height,
			width: config.S.width,
			data: { type: NodeType.State, uid: aNode.grounding.identifiers.ido },
			nodes: []
		});
	}

	// Add each node found in T
	for (let i = 0; i < amr.model.transitions.length; i++) {
		const aTransition = amr.model.transitions[i];
		// Add the node for this transition
		result.nodes.push({
			id: aTransition.id,
			label: aTransition.properties.name,
			x: 0,
			y: 0,
			height: config.T.height,
			width: config.T.width,
			data: { type: NodeType.Transition, uid: undefined },
			nodes: []
		});

		// Add input edge(s) for this transition:
		for (let j = 0; j < aTransition.input.length; j++) {
			const sourceId = aTransition.input[j];
			const targetId = aTransition.id;

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
		// Add output edge(s) for this transition:
		for (let j = 0; j < aTransition.output.length; j++) {
			const sourceId = aTransition.id;
			const targetId = aTransition.output[j];

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
	} // end T

	return result;
};

// This is a temp helper function.
// This is just used to go from new format -> old format as we calibration, simulation, and latex converter all need this old format while the switch over is happening.
export const AMRToPetri = (model: Model) => {
	const tempGraph = parseAMR2IGraph(model);
	return parseIGraph2PetriNet(tempGraph);
};
