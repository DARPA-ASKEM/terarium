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

const extractEdgesAttribute = (
	edges: IEdge[],
	transitionOrState: string,
	sourceOrTarget: string
): string[] =>
	edges
		.filter((edge) => edge[sourceOrTarget].charAt(0) === transitionOrState)
		.map((node) => node[sourceOrTarget]);

/*
    Validates petrinet - check #2 must come before check #5 to avoid an infinite loop
*/
export const petrinetValidator = (
	graph: IGraph,
	petrinet: Petrinet,
	isBoundedPetrinet: boolean = true
): boolean => {
	const edges: IEdge[] = graph.edges;

	// console.table(graph.nodes);
	// console.table(edges);
	// console.log("states")
	// console.table(petrinet.S);
	// console.log("transitions")
	// console.table(petrinet.T);

	/* ----- 1. Requires at least one edge ----- */
	if (edges.length < 1) return false;

	const transitionNames: string[] = petrinet.T.map((transition) => transition.tname);
	const stateNames: string[] = petrinet.S.map((state) => state.sname);

	/* ----- 2. Check that every node is at least either a source or a target ----- */
	const nodeNames: string[] = [...transitionNames, ...stateNames];
	const edgeSourcesAndTargets: string[] = [
		...edges.map((edge) => [edge.source, edge.target])
	].flat();
	for (let i = 0; i < nodeNames.length; i++) {
		if (!edgeSourcesAndTargets.includes(nodeNames[i])) return false;
	}

	/* ----- 3. Check if it's a bipartite graph (place -> transition -> place) ----- */
	for (let i = 0; i < edges.length; i++) {
		const { source, target } = edges[i];

		if (
			(source.charAt(0) === 'p' && target.charAt(0) !== 't') ||
			(source.charAt(0) === 't' && target.charAt(0) !== 'p')
		)
			return false;
	}

	/* ----- 4. Check if every transition node is bounded by state nodes ----- */
	if (isBoundedPetrinet) {
		const transitionEdgeSources: string[] = extractEdgesAttribute(edges, 't', 'source');
		const transitionEdgeTargets: string[] = extractEdgesAttribute(edges, 't', 'target');

		for (let i = 0; i < transitionNames.length; i++) {
			// If a transition is recognized as a source and a target it is bounded
			if (
				!transitionEdgeSources.includes(transitionNames[i]) ||
				!transitionEdgeTargets.includes(transitionNames[i])
			)
				return false;
		}
	}

	/* ----- 5. Make sure there aren't multiple petrinet bodies ----- */
	const statesSurroundingTransitions: string[][] = [];
	// Save all the states where the current transition is a source or a target
	for (let i = 0; i < transitionNames.length; i++) {
		const statesSurroundingTransition: string[] = [];
		const edgesWithCommonTransition: IEdge[] = edges.filter(
			(edge) => edge.source === transitionNames[i] || edge.target === transitionNames[i]
		);

		for (let j = 0; j < edgesWithCommonTransition.length; j++) {
			const { source, target } = edgesWithCommonTransition[j];
			statesSurroundingTransition.push(source.charAt(0) === 'p' ? source : target);
		}
		statesSurroundingTransitions.push(statesSurroundingTransition);
	}
	console.log(statesSurroundingTransitions);

	const connectedStates: string[] = statesSurroundingTransitions[0];
	let potentialConnections: string[][] = [];
	// Merge all the arrays in statesSurroundingTransitions that have common values
	do {
		const statesToMerge: string[][] =
			potentialConnections.length > 0 ? potentialConnections : statesSurroundingTransitions;
		potentialConnections = [];

		for (let i = 0; i < statesToMerge.length; i++) {
			if (connectedStates.some((anyPlace) => statesToMerge[i].includes(anyPlace))) {
				connectedStates.push(...statesToMerge[i]);
			} else {
				potentialConnections.push(statesToMerge[i]);
			}
		}

		console.log([...new Set(connectedStates)]);
		console.log(potentialConnections);

		// If the potential connections from the last iteration are the exact same then there is more than one petrinet body
		if (statesToMerge.length === potentialConnections.length) return false;
	} while (potentialConnections.length > 0);

	return true; // All checks have been successfully passed
};
