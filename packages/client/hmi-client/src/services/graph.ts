/**
 * Provides graph rendering utilities for computational-like graphs
 */
import _ from 'lodash';
import * as d3 from 'd3';
import dagre from 'dagre';
import graphScaffolder, { IGraph, INode, IEdge } from '@graph-scaffolder/index';

export type D3SelectionINode<T> = d3.Selection<d3.BaseType, INode<T>, null, any>;
export type D3SelectionIEdge<T> = d3.Selection<d3.BaseType, IEdge<T>, null, any>;

const MARKER_VIEWBOX = '-5 -5 10 10';
const ARROW_PATH = 'M 0,-3.25 L 5 ,0 L 0,3.25';

export const pathFn = d3
	.line<{ x: number; y: number }>()
	.x((d) => d.x)
	.y((d) => d.y);

/**
 * BasicRenderer with predefined arrow-head markers
 */
export abstract class BaseComputionGraph<V, E> extends graphScaffolder.BasicRenderer<V, E> {
	readonly EDGE_ARROW_ID = 'edge-arrowhead';

	setupDefs() {
		const svg = d3.select(this.svgEl);

		// Clean up
		svg.select('defs').selectAll('.edge-marker-end').remove();

		// Arrow defs
		svg
			.select('defs')
			.append('marker')
			.classed('edge-marker-end', true)
			.attr('id', this.EDGE_ARROW_ID)
			.attr('viewBox', MARKER_VIEWBOX)
			.attr('refX', 2)
			.attr('refY', 0)
			.attr('orient', 'auto')
			.attr('markerWidth', 15)
			.attr('markerHeight', 15)
			.attr('markerUnits', 'userSpaceOnUse')
			.attr('xoverflow', 'visible')
			.append('svg:path')
			.attr('d', ARROW_PATH)
			.style('fill', '#000')
			.style('stroke', 'none');
	}
}

export const runDagreLayout = <V, E>(
	graphData: IGraph<V, E>,
	lr: boolean = false
): IGraph<V, E> => {
	const g = new dagre.graphlib.Graph({ compound: true, multigraph: true });
	g.setGraph({});
	g.setDefaultEdgeLabel(() => ({}));

	graphScaffolder.traverseGraph(graphData, (node: INode<V>) => {
		if (node.width && node.height) {
			g.setNode(node.id, {
				label: node.label,
				width: node.width,
				height: node.height,
				x: node.x,
				y: node.y
			});
		} else {
			g.setNode(node.id, { label: node.label, x: node.x, y: node.y });
		}
		if (!_.isEmpty(node.nodes)) {
			// eslint-disable-next-line
			for (const child of node.nodes) {
				g.setParent(child.id, node.id);
			}
		}
	});

	// eslint-disable-next-line
	for (const edge of graphData.edges) {
		g.setEdge(edge.source, edge.target);
	}

	// FIXME: Hackathon show-n-tell, remove
	if (lr === true) {
		g.graph().rankDir = 'LR';
		g.graph().nodesep = 100;
		g.graph().ranksep = 100;
	}

	dagre.layout(g);

	g.nodes().forEach((n) => {
		const node = g.node(n);
		node.x -= node.width * 0.5;
		node.y -= node.height * 0.5;
	});

	graphScaffolder.traverseGraph(graphData, (node) => {
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

	// eslint-disable-next-line
	for (const edge of graphData.edges) {
		const e = g.edge(edge.source, edge.target);
		edge.points = _.cloneDeep(e.points);
	}

	// HACK: multi-edges
	const dupe: Set<string> = new Set();
	for (let idx = 0; idx < graphData.edges.length; idx++) {
		const edge = graphData.edges[idx];
		const hash = `${edge.source};${edge.target}`;
		if (dupe.has(hash)) {
			if (edge.points.length > 2) {
				for (let i = 1; i < edge.points.length - 1; i++) {
					edge.points[i].y -= 25;
				}
			}
		}
		dupe.add(hash);
	}

	return graphData;
};

export const runDagreLayout2 = <V, E>(graphData: IGraph<V, E>): IGraph<V, E> =>
	runDagreLayout(graphData, true);
