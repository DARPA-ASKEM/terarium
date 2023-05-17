import { IGraph } from '@graph-scaffolder/types';
import { PetriNetModel, PetriNetState, PetriNetTransition } from '@/types/Types';

interface EdgeData {
	numEdges: number;
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
