import { IGraph } from '@graph-scaffolder/types';

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

/*
Validates petrinet - check #2 must come before check #3 to avoid an infinite loop
Returns an string explaining the invalidity or a true boolean if it's valid
*/
export const petriNetValidator = (petrinet: PetriNet): string | true => {
	const { S, T, I, O } = petrinet;
	// console.log(petrinet);

	/* ----- 1. Requires at least one edge ----- */
	if (I.length < 1 && O.length < 1) return 'Invalid petri net: Requires at least one edge';

	/* ----- 2. Check that every node is at least either a source or a target ----- */
	const checkIfSourceOrTarget = (linkedIDs: number[], lastNodeID: number): boolean => {
		for (let id = 1; id < lastNodeID; id++) if (!linkedIDs.includes(id)) return false;
		return true;
	};
	const linkedTransitionIDs: number[] = [
		...new Set([...I.map((input) => input.it), ...O.map((output) => output.ot)])
	];
	const linkedStateIDs: number[] = [
		...new Set([...I.map((input) => input.is), ...O.map((output) => output.os)])
	];
	if (
		!checkIfSourceOrTarget(linkedTransitionIDs, T.length + 1) ||
		!checkIfSourceOrTarget(linkedStateIDs, S.length + 1)
	)
		return 'Invalid petri net: Every node should be at least either a source or a target';

	/* ----- 3. Make sure there aren't multiple petrinet bodies ----- */
	const statesSurroundingTransitions: number[][] = [];
	for (let id = 1; id < T.length + 1; id++) {
		// Save all the states where the current transition is a source or a target
		statesSurroundingTransitions.push([
			...I.filter((input) => input.it === id).map((input) => input.is),
			...O.filter((output) => output.ot === id).map((output) => output.os)
		]);
	}
	// console.log(statesSurroundingTransitions)

	const connectedStates: number[] = statesSurroundingTransitions[0];
	let potentialConnections: number[][] = [];
	// Merge all the arrays in statesSurroundingTransitions that have common values
	do {
		const statesToMerge: number[][] =
			potentialConnections.length > 0 ? potentialConnections : statesSurroundingTransitions;
		potentialConnections = [];

		for (let i = 0; i < statesToMerge.length; i++) {
			if (connectedStates.some((anyPlace) => statesToMerge[i].includes(anyPlace))) {
				connectedStates.push(...statesToMerge[i]);
			} else {
				potentialConnections.push(statesToMerge[i]);
			}
		}
		// console.log([...new Set(connectedStates)]);
		// console.log(potentialConnections);

		// If the potential connections from the last iteration are the exact same then there is more than one petrinet body
		if (statesToMerge.length === potentialConnections.length)
			return 'Invalid petri net: There are multiple petri net bodies';
	} while (potentialConnections.length > 0);

	return true; // All checks have been successfully passed
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

/**
 * Given a petrinet model convert to an IGraph representation g
 * for the renderer
 * First add each node found in S and T, then add each edge found in I and O
 */
export const parsePetriNet2IGraph = (model: PetriNet) => {
	const result: IGraph<NodeData, EdgeData> = {
		width: 500,
		height: 500,
		nodes: [],
		edges: []
	};

	const nodeHeight = 40;
	const nodeWidth = 40;

	// add each nodes in S
	for (let i = 0; i < model.S.length; i++) {
		const aNode = model.S[i];
		result.nodes.push({
			id: `s-${i + 1}`,
			label: aNode.sname,
			x: 0,
			y: 0,
			height: nodeHeight,
			width: nodeWidth,
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
			height: nodeHeight,
			width: nodeWidth,
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
