import { IGraph, IEdge /* traverseGraph */ } from 'graph-scaffolder';

interface Petrinet {
	S: State[];
	T: Transition[];
	I: Input[];
	O: Output[];
}

interface State {
	sname: string;
}
interface Transition {
	tname: string;
}
interface Input {
	it: number;
	is: number;
}
interface Output {
	ot: number;
	os: number;
}

const extractEdgesAttribute = (edges: IEdge[], transitionOrState: string, sourceOrTarget: string) =>
	edges
		.filter((edge) => edge[sourceOrTarget].charAt(0) === transitionOrState)
		.map((node) => node[sourceOrTarget]);

export const petrinetValidator = (
	graph: IGraph,
	petrinet: Petrinet,
	isBoundedPetrinet: boolean = true
) => {
	const { edges } = graph;
	let isValidPetrinet: boolean = true;

	// Requires at least one edge
	if (edges.length < 1) isValidPetrinet = false;

	if (isValidPetrinet) {
		const transitionNodeNames: string[] = petrinet.T.map((transition) => transition.tname);
		const nodeNames: string[] = [...transitionNodeNames, ...petrinet.S.map((state) => state.sname)]; // Append state names
		const edgeSourcesAndTargets: string[] = [
			...edges.map((edge) => [edge.source, edge.target])
		].flat();

		// Check that every node is at least either a source or a target
		for (let i = 0; i < nodeNames.length; i++) {
			if (!edgeSourcesAndTargets.includes(nodeNames[i])) {
				isValidPetrinet = false;
				break;
			}
		}

		if (isValidPetrinet && isBoundedPetrinet) {
			// Check if every transition node is bounded by state nodes
			const transitionEdgeSources = extractEdgesAttribute(edges, 't', 'source');
			const transitionEdgeTargets = extractEdgesAttribute(edges, 't', 'target');

			for (let i = 0; i < transitionNodeNames.length; i++) {
				// If a transition is recognized as a source and a target it is bounded
				if (
					!transitionEdgeSources.includes(transitionNodeNames[i]) ||
					!transitionEdgeTargets.includes(transitionNodeNames[i])
				) {
					isValidPetrinet = false;
					break;
				}
			}
		}
	}

	if (isValidPetrinet) {
		// Check if it's a bipartite graph (place -> transition -> place)
		for (let i = 0; i < edges.length; i++) {
			const { source, target } = edges[i];

			if (
				(source.charAt(0) === 'p' && target.charAt(0) !== 't') ||
				(source.charAt(0) === 't' && target.charAt(0) !== 'p')
			) {
				isValidPetrinet = false;
				break;
			}
		}
	}

	// Make sure there aren't multiple petrinet bodies - try traversal?
	// traverseGraph(graph);

	// console.log(graph);
	// console.table(graph.nodes);
	// console.table(graph.edges);

	// console.log("states")
	// console.table(petrinet.S);
	// console.log("transitions")
	// console.table(petrinet.T);
	// console.log("inputs")
	// console.table(petrinet.I);
	// console.log("outputs")
	// console.table(petrinet.O);

	if (isValidPetrinet) console.log('Valid Petrinet');
	else console.log('Invalid Petrinet');
};
