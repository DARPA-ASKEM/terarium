import { IGraph, INode, IEdge } from 'graph-scaffolder';

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
export const petrinetValidator = (graph: IGraph, isBoundedPetrinet: boolean = true): boolean => {
	const { nodes, edges }: { nodes: INode[]; edges: IEdge[] } = graph;

	// console.log(graph);
	// console.table(nodes);
	// console.table(edges);

	/* ----- 1. Requires at least one edge ----- */
	if (edges.length < 1) {
		console.log('#1');
		return false;
	}

	// transitionNames, stateNames are used across most of the checks
	const transitionNames: string[] = nodes
		.filter((node) => node.id.charAt(0) === 't')
		.map((node) => node.id);
	const stateNames: string[] = nodes
		.filter((node) => node.id.charAt(0) === 'p')
		.map((node) => node.id);

	/* ----- 2. Check that every node is at least either a source or a target ----- */
	const nodeNames: string[] = [...transitionNames, ...stateNames];
	const edgeSourcesAndTargets: string[] = [
		...edges.map((edge) => [edge.source, edge.target])
	].flat();
	for (let i = 0; i < nodeNames.length; i++) {
		if (!edgeSourcesAndTargets.includes(nodeNames[i])) {
			console.log('#2');
			return false;
		}
	}

	/* ----- 3. Check if it's a bipartite graph (place -> transition -> place) ----- */
	for (let i = 0; i < edges.length; i++) {
		const { source, target } = edges[i];

		if (
			(source.charAt(0) === 'p' && target.charAt(0) !== 't') ||
			(source.charAt(0) === 't' && target.charAt(0) !== 'p')
		) {
			console.log('#3');
			return false;
		}
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
			) {
				console.log('#4');
				return false;
			}
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
	// console.log(statesSurroundingTransitions);

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

		// console.log([...new Set(connectedStates)]);
		// console.log(potentialConnections);

		// If the potential connections from the last iteration are the exact same then there is more than one petrinet body
		if (statesToMerge.length === potentialConnections.length) {
			console.log('#5');
			return false;
		}
	} while (potentialConnections.length > 0);

	return true; // All checks have been successfully passed
};
