import _ from 'lodash';
import dagre from 'dagre';
import { IGraph } from '../../src/types';
import { traverseGraph } from '../../src/core/traverse';

export const runLayout = <V, E>(graphData: IGraph<V, E>): IGraph<V, E> => {
	const g = new dagre.graphlib.Graph({ compound: true });
	g.setGraph({});
	g.setDefaultEdgeLabel(function () {
		return {};
	});

	traverseGraph(graphData, (node) => {
		if (node.width && node.height) {
			g.setNode(node.id, { label: node.id, width: node.width, height: node.height });
		} else {
			g.setNode(node.id, { label: node.id });
		}
		if (!_.isEmpty(node.nodes)) {
			for (const child of node.nodes) {
				g.setParent(child.id, node.id);
			}
		}
	});
	for (const edge of graphData.edges) {
		g.setEdge(edge.source, edge.target);
	}
	dagre.layout(g);

	g.nodes().forEach((n) => {
		const node = g.node(n);
		node.x -= node.width * 0.5;
		node.y -= node.height * 0.5;
	});

	traverseGraph(graphData, (node) => {
		const n = g.node(node.id);
		node.width = n.width;
		node.height = n.height;
		node.x = n.x;
		node.y = n.y;

		const pid = g.parent(node.id);
		if (pid) {
			node.x -= g.node(pid).x;
			node.y -= g.node(pid).y;
		}
	});
	for (const edge of graphData.edges) {
		const e = g.edge(edge.source, edge.target);
		edge.points = e.points;
	}

	return graphData;
};
