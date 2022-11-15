import { PetriNet } from '@/utils/petri-net-validator';
import { IGraph } from '@graph-scaffolder/types';

interface NodeData {
	type: string;
}

interface EdgeData {
	val: number;
}
enum NodeType {
	State = 'S',
	Transition = 'T'
}

const g: IGraph<NodeData, EdgeData> = {
	width: 500,
	height: 500,
	nodes: [],
	edges: []
};
function addNode(
	id: string,
	label: string,
	x: number,
	y: number,
	height: number,
	width: number,
	type: NodeType
) {
	g.nodes.push({
		id,
		label,
		x,
		y,
		height,
		width,
		data: { type },
		nodes: []
	});
}

function addEdgeID(sourceID: string, targetID: string) {
	let sourceX;
	let sourceY;
	let targetX;
	let targetY;
	let sourceLabel;
	let targetLabel;
	// Find source and target's locations
	// there has to be a better way to get the source and target locations
	for (let i = 0; i < g.nodes.length; i++) {
		if (sourceLabel && targetLabel) {
			break;
		}
		if (g.nodes[i].id === sourceID) {
			sourceLabel = g.nodes[i].label;
			sourceX = g.nodes[i].x + g.nodes[i].width * 0.5;
			sourceY = g.nodes[i].y + g.nodes[i].height * 0.5;
		}
		if (g.nodes[i].id === targetID) {
			targetLabel = g.nodes[i].label;
			targetX = g.nodes[i].x + g.nodes[i].width * 0.5;
			targetY = g.nodes[i].y + g.nodes[i].height * 0.5;
		}
	}
	g.edges.push({
		source: sourceLabel,
		target: targetLabel,
		points: [
			{
				x: sourceX, // + source.datum().width * 0.5,
				y: sourceY // + source.datum().height * 0.5
			},
			{
				x: targetX, // + target.datum().width * 0.5,
				y: targetY // + target.datum().height * 0.5
			}
		]
	});
}

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
		// console.log("A Node");
		// console.log(aNode);
		nodeX += 30;
		nodeY += 30;
		addNode(`s-${i + 1}`, aNode.sname, nodeX, nodeY, nodeHeight, nodeWidth, NodeType.State);
	}
	// Move Transitions 100 to the right of S
	nodeX = 100;
	nodeY = 0;
	for (let i = 0; i < model.T.length; i++) {
		const aTransition = model.T[i];
		nodeX += 30;
		nodeY += 30;
		addNode(
			`t-${i + 1}`,
			aTransition.tname.toString(),
			nodeX,
			nodeY,
			nodeHeight,
			nodeWidth,
			NodeType.Transition
		);
	} // end T

	// Edges
	for (let i = 0; i < model.I.length; i++) {
		const iEdges = model.I[i];
		const sourceID = `s-${iEdges.is}`;
		const transitionID = `t-${iEdges.it}`;
		addEdgeID(sourceID, transitionID);
	}
	for (let i = 0; i < model.O.length; i++) {
		const oEdges = model.O[i];
		const sourceID = `s-${oEdges.os}`;
		const transitionID = `t-${oEdges.ot}`;
		addEdgeID(transitionID, sourceID);
	}
	return g;
};
