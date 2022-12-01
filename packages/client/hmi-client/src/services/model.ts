import API from '@/api/api';
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
 * Given a petrinet model convert to an IGraph representation g
 * for the renderer
 * First add each node found in S and T, then add each edge found in I and O
 */
export const parsePetriNet2IGraph = (model: PetriNet) => {
	// Reset current Graph.
	g.nodes = [];
	g.edges = [];
	const nodeHeight = 20;
	const nodeWidth = 20;
	let nodeX = 10;
	let nodeY = 10;
	// add each nodes in S
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
	nodeX = 100; // Move Transitions 100 to the right of S. This is a very poor way to display graphs but will have to do for now.
	nodeY = 10;
	// Add each node found in T
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

	// Edges found in I
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
	// Edges found in O
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

	return { ...g };
};

export const getModel = async (modelId: string) => API.get(`/models/${modelId}`);
