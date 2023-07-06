import { IGraph } from '@graph-scaffolder/types';
import { PetriNetModel, Model } from '@/types/Types';
import { PetriNet } from '@/petrinet/petrinet-service';

export interface NodeData {
	type: string;
}

export interface EdgeData {
	numEdges: number;
}
export enum NodeType {
	State = 'S',
	Transition = 'T'
}

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

	return result;
};

export const convertToIGraph = (amr: Model) => {
	const result: IGraph<NodeData, EdgeData> = {
		width: 500,
		height: 500,
		amr,
		nodes: [],
		edges: []
	};

	const petrinetModel = amr.model as PetriNetModel;

	petrinetModel.states.forEach((state) => {
		result.nodes.push({
			id: state.id,
			label: state.id,
			type: 'state',
			x: 0,
			y: 0,
			width: 100,
			height: 100,
			data: { type: 'state' },
			nodes: []
		});
	});

	petrinetModel.transitions.forEach((transition) => {
		result.nodes.push({
			id: transition.id,
			label: transition.id,
			type: 'transition',
			x: 0,
			y: 0,
			width: 100,
			height: 100,
			data: { type: 'transition' },
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

// FIXME AMR todo
export const convertToAMRModel = (g: IGraph<NodeData, EdgeData>) => g.amr;

export const addState = (amr: Model, id: string, name: string) => {
	amr.model.states.push({
		id,
		name,
		description: ''
	});
	amr.semantics?.ode.initials?.push({
		target: id,
		expression: '',
		expression_mathml: ''
	});
};

export const addTransition = (amr: Model, id: string, name: string) => {
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
		expression: '',
		expression_mathml: ''
	});
};

// export const addEdge = (amr: Model, sourceId: string, targetId: string) => {
// };
