import { IGraph } from 'graph-scaffolder';

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

export const petrinetValidator = (
	graph: IGraph,
	petrinet: Petrinet,
	isBoundedPetrinet: boolean = true
) => {
	const { edges } = graph;
	let isValidPetrinet: boolean = true;

	// Needs at least 2 edges to point to a state after one transition
	if ((edges.length < 2 && isBoundedPetrinet) || edges.length === 0) isValidPetrinet = false;

	if (isValidPetrinet && isBoundedPetrinet) {
		const transitionNodeNames: string[] = petrinet.T.map((transition) => transition.tname);
		const transitionEdgeSources: string[] = edges
			.filter((edge) => edge.source.charAt(0) === 't')
			.map((node) => node.source);

		// Check if every transition node is pointing at at least one state node
		for (let i = 0; i < transitionNodeNames.length; i++) {
			// If a transition is recognized as a source, it is pointing at another node
			if (!transitionEdgeSources.includes(transitionNodeNames[i])) {
				isValidPetrinet = false;
				break;
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

			// Check if all nodes are connected somehow
		}
	}

	console.log(graph);
	console.table(graph.nodes);
	console.table(graph.edges);

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
