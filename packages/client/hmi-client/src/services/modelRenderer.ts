import { PetriNet } from '@/utils/petri-net-validator';
import { IGraph } from '@graph-scaffolder/types';

export interface NodeData {
	type: string;
}

export interface EdgeData {
	val: number;
}
export enum NodeType {
	State = 'S',
	Transition = 'T'
}

const g: IGraph<NodeData, EdgeData> = {
	width: 500,
	height: 500,
	nodes: [],
	edges: []
};

/**
 * Given a simulation plan wiring diagram, convert to an IGraph representation
 * for the renderer
 */
export const parsePetriNet2IGraph = (model: PetriNet) => {
	const nodeHeight = 20;
	const nodeWidth = 20;
	let nodeX = 0;
	let nodeY = 10;
	// Nodes
	for (let i = 0; i < model.S.length; i++) {
		const aNode = model.S[i];
		nodeX += 30;
		nodeY += 30;
		g.nodes.push({
			id: `s-${i + 1}`,
			label: aNode.sname,
			x: nodeX,
			y: nodeY,
			height: nodeHeight,
			width: nodeWidth,
			data: { type: NodeType.State },
			nodes: []
		});
	}
	// Move Transitions 100 to the right of S
	nodeX = 100;
	nodeY = 0;
	for (let i = 0; i < model.T.length; i++) {
		const aTransition = model.T[i];
		nodeX += 30;
		nodeY += 30;
		g.nodes.push({
			id: `t-${i + 1}`,
			label: aTransition.tname,
			x: nodeX,
			y: nodeY,
			height: nodeHeight,
			width: nodeWidth,
			data: { type: NodeType.Transition },
			nodes: []
		});
	} // end T

	// Edges
	for (let i = 0; i < model.I.length; i++) {
		const iEdges = model.I[i];
		const sourceID = `s-${iEdges.is}`;
		const transitionID = `t-${iEdges.it}`;
		g.edges.push({
			source: sourceID,
			target: transitionID,
			points: []
		});
	}
	for (let i = 0; i < model.O.length; i++) {
		const oEdges = model.O[i];
		const sourceID = `t-${oEdges.ot}`;
		const transitionID = `s-${oEdges.os}`;
		g.edges.push({
			source: sourceID,
			target: transitionID,
			points: []
		});
	}

	return g;
};
