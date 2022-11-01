import { IGraph, IEdge } from 'graph-scaffolder';

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
	const edges: IEdge[] = graph.edges;
	let isValidPetrinet: boolean = true;

	if (edges.length < 1) isValidPetrinet = false; // Requires at least one edge

	if (isValidPetrinet) {
		const transitionNodeNames: string[] = petrinet.T.map((transition) => transition.tname);
		const stateNodeNames: string[] = petrinet.S.map((state) => state.sname);

		/* ----- Make sure there aren't multiple petrinet bodies -----*/
		const statesSurroundingTransitions: string[][] = [];
		// Save all the states where the current transition is a source or a target
		for (let i = 0; i < transitionNodeNames.length; i++) {
			const statesSurroundingTransition: string[] = [];
			const edgesWithCommonTransition = edges.filter(
				(edge) => edge.source === transitionNodeNames[i] || edge.target === transitionNodeNames[i]
			);

			for (let j = 0; j < edgesWithCommonTransition.length; j++) {
				const { source, target } = edgesWithCommonTransition[j];
				statesSurroundingTransition.push(source.charAt(0) === 'p' ? source : target);
			}
			statesSurroundingTransitions.push(statesSurroundingTransition);
		}
		console.log(statesSurroundingTransitions);

		// const statesToMerge: string[][] = [];
		const connectedStates: string[] = [];
		// Merge all the arrays in statesSurroundingTransitions that have common values
		for (let i = 0; i < statesSurroundingTransitions.length; i++) {
			const statesToMerge: string[] = statesSurroundingTransitions[i];

			for (let j = 0; j < statesSurroundingTransitions.length; j++) {
				if (statesToMerge.some((anyPlace) => statesSurroundingTransitions[j].includes(anyPlace))) {
					statesToMerge.push(...statesSurroundingTransitions[j]);
					console.log(statesToMerge);
				}
			}

			// connectedStates.push(...statesToMerge);// condition
			console.log([...new Set(statesToMerge)], stateNodeNames, 11111);
			if ([...new Set(statesToMerge)].length === stateNodeNames.length) {
				console.log('break');
				break;
			}
		}
		console.log([...new Set(connectedStates)]);
		if ([...new Set(connectedStates)].length !== stateNodeNames.length) isValidPetrinet = false; // every state node name should be in connected states

		/* ----- Check that every node is at least either a source or a target - could be redundant due to the check above -----*/
		if (isValidPetrinet) {
			const nodeNames: string[] = [...transitionNodeNames, ...stateNodeNames];
			const edgeSourcesAndTargets: string[] = [
				...edges.map((edge) => [edge.source, edge.target])
			].flat();
			for (let i = 0; i < nodeNames.length; i++) {
				if (!edgeSourcesAndTargets.includes(nodeNames[i])) {
					isValidPetrinet = false;
					break;
				}
			}
		}

		/* ----- Check if it's a bipartite graph (place -> transition -> place) -----*/
		if (isValidPetrinet) {
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

		/* ----- Check if every transition node is bounded by state nodes -----*/
		if (isValidPetrinet && isBoundedPetrinet) {
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

	// console.table(graph.nodes);
	console.table(edges);

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
