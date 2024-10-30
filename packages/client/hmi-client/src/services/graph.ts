/**
 * Provides graph rendering utilities for computational-like graphs
 */
import _ from 'lodash';
import * as d3 from 'd3';
import dagre from 'dagre';
import graphScaffolder, { IGraph, INode, IEdge } from '@graph-scaffolder/index';
import type { Position } from '@/types/common';
import OrthogonalConnector, { Side } from '@/utils/ortho-router';

export type D3SelectionINode<T> = d3.Selection<d3.BaseType, INode<T>, null, any>;
export type D3SelectionIEdge<T> = d3.Selection<d3.BaseType, IEdge<T>, null, any>;

export enum NodeType {
	State = 'state',
	Transition = 'transition',
	Observable = 'observable'
}

export const pathFn = d3
	.line<{ x: number; y: number }>()
	.x((d) => d.x)
	.y((d) => d.y);

function interpolatePointsForCurve(a: Position, b: Position): Position[] {
	const controlXOffset = 50;
	return [a, { x: a.x + controlXOffset, y: a.y }, { x: b.x - controlXOffset, y: b.y }, b];
}

// Stand-alone edge rerouting given a set of node positions and their
// connections.
interface ExtRect {
	left: number;
	top: number;
	width: number;
	height: number;
	cx: number;
	cy: number;
}
export const rerouteEdges = (nodes: INode<any>[], edges: IEdge<any>[]) => {
	const nodeMap: Map<string, ExtRect> = new Map();
	nodes.forEach((node) => {
		nodeMap.set(node.id, {
			left: node.x - 0.5 * node.width,
			top: node.y - 0.5 * node.height,
			width: node.width,
			height: node.height,

			// extras for side calculation
			cx: node.x,
			cy: node.y
		});
	});

	const obstacles = nodes.map((node) => nodeMap.get(node.id) as any);

	// Figure out how many edges extends out of each given nodes for edge placement.
	// eg if there are 3 edges coming out of node-A, then the placement of these
	// edges are (edge-i/num+1) => [1/4, 2/4, 3/4]
	const edgeSourceMap: Map<string, number> = new Map();
	edges.forEach((edge: IEdge<any>) => {
		if (edge.data.isObservable) return;
		if (!edgeSourceMap.has(edge.source)) {
			edgeSourceMap.set(edge.source, 0);
		}
		edgeSourceMap.set(edge.source, 1 + (edgeSourceMap.get(edge.source) as number));
	});

	const sourceNodeCounter: Map<string, number> = new Map();
	edges.forEach((edge: IEdge<any>) => {
		if (edge.data.isObservable) return;
		if (!sourceNodeCounter.has(edge.source)) {
			sourceNodeCounter.set(edge.source, 0);
		}
		let counter = sourceNodeCounter.get(edge.source) as number;
		const denominator = edgeSourceMap.get(edge.source) as number;
		counter++;

		let sideA: Side = 'right';
		let sideB: Side = 'left';

		const threshold = 40;
		const shapeA = nodeMap.get(edge.source) as ExtRect;
		const shapeB = nodeMap.get(edge.target) as ExtRect;
		if (Math.abs(shapeB.cx - shapeA.cx) > threshold && Math.abs(shapeB.cy - shapeA.cy) > threshold) {
			if (shapeB.cx <= shapeA.cx && shapeB.cy <= shapeA.cy) {
				sideA = 'top';
				sideB = 'right';
			} else if (shapeB.cx > shapeA.cx && shapeB.cy <= shapeA.cy) {
				sideA = 'top';
				sideB = 'left';
			} else if (shapeB.cx <= shapeA.cx && shapeB.cy >= shapeA.cy) {
				sideA = 'bottom';
				sideB = 'right';
			} else {
				sideA = 'bottom';
				sideB = 'left';
			}
		}

		const path = OrthogonalConnector.route({
			pointA: { shape: shapeA, side: sideA, distance: counter / (denominator + 1) },
			pointB: { shape: shapeB, side: sideB, distance: 0.5 },
			shapeMargin: 10,
			globalBoundsMargin: 10,
			globalBounds: { left: -5000, top: -5000, width: 5000, height: 5000 },
			obstacles
		});
		sourceNodeCounter.set(edge.source, counter);

		edge.points = path;
	});
};

export const runDagreLayout = <V, E>(graphData: IGraph<V, E>, lr: boolean = true): IGraph<V, E> => {
	const g = new dagre.graphlib.Graph({ compound: true });
	g.setGraph({});
	g.setDefaultEdgeLabel(() => ({}));

	let observableAmount = 0;
	graphScaffolder.traverseGraph(graphData, (node: INode<any>) => {
		if (node.data?.type === NodeType.Observable) observableAmount++;
		g.setNode(node.id, {
			label: node.label,
			width: node.width ?? 50,
			height: node.height ?? 50
		});
		node.nodes.forEach((child) => g.setParent(child.id, node.id));
	});
	// Set state/transitions edges
	graphData.edges.forEach((edge: IEdge<any>) => {
		if (edge.data?.isObservable) return;
		g.setEdge(edge.source, edge.target);
	});

	if (lr === true) {
		g.graph().rankDir = 'LR';
		g.graph().nodesep = 100;
		g.graph().ranksep = 125;
	}

	dagre.layout(g);

	let mostRightNodeX = 0;
	let lowestNodeY = 0;
	let highestNodeY = 0;
	let currentObservableY = 0;
	let isAddingObservables = false;
	graphScaffolder.traverseGraph(graphData, (node: INode<any>) => {
		let n = g.node(node.id);
		const pid = g.parent(node.id);
		// Determine bounds from state and transition nodes
		// Observables are added to the end graphData.nodes array in convertToIGraph so assume that's the order
		if (node.data?.type !== NodeType.Observable) {
			if (n.x > mostRightNodeX) mostRightNodeX = n.x;
			if (n.y < lowestNodeY) lowestNodeY = n.y;
			if (n.y > highestNodeY) highestNodeY = n.y;
		}
		// Determine observable node (custom) placement
		else {
			if (!isAddingObservables) {
				isAddingObservables = true;
				mostRightNodeX += 150;
				const midPointY = (highestNodeY + lowestNodeY) / 2;
				const observablesHeight = observableAmount * n.height;
				currentObservableY = midPointY - observablesHeight / 2;
			}
			g.setNode(node.id, {
				x: mostRightNodeX,
				y: currentObservableY,
				width: node.width,
				height: node.height
			});
			n = g.node(node.id);
			currentObservableY += 100;
		}
		// Place node
		node.x = n.x;
		node.y = n.y;
		if (pid) {
			node.x -= g.node(pid).x;
			node.y -= g.node(pid).y;
		}
	});

	graphData.edges.forEach((edge: IEdge<any>) => {
		// Set observable (custom) edges here
		if (edge.data?.isObservable) {
			const sourceNode = g.node(edge.source);
			const targetNode = g.node(edge.target);
			if (!sourceNode || !targetNode) return;
			g.setEdge(edge.source, edge.target, {
				points: interpolatePointsForCurve(sourceNode, targetNode)
			});
		}
		const e = g.edge(edge.source, edge.target);
		if (e?.points) edge.points = _.cloneDeep(e.points);
	});

	// HACK: multi-edges
	const dupe: Set<string> = new Set();
	for (let idx = 0; idx < graphData.edges.length; idx++) {
		const edge = graphData.edges[idx];
		const hash = `${edge.source};${edge.target}`;
		if (dupe.has(hash) && edge.points.length > 2) {
			for (let i = 1; i < edge.points.length - 1; i++) {
				edge.points[i].y -= 25;
			}
		}
		dupe.add(hash);
	}

	// Find new width and height
	if (graphData.nodes.length > 0) {
		let minX = Number.MAX_VALUE;
		let maxX = Number.MIN_VALUE;
		let minY = Number.MAX_VALUE;
		let maxY = Number.MIN_VALUE;
		graphData.nodes.forEach((node) => {
			if (node.x - 0.5 * node.width < minX) minX = node.x - 0.5 * node.width;
			if (node.x + 0.5 * node.width > maxX) maxX = node.x + 0.5 * node.width;
			if (node.y - 0.5 * node.height < minY) minY = node.y - 0.5 * node.height;
			if (node.y + 0.5 * node.height > maxY) maxY = node.y + 0.5 * node.height;
		});

		rerouteEdges(graphData.nodes, graphData.edges);

		// Give the bounds a little extra buffer
		const buffer = 10;
		maxX += buffer;
		maxY += buffer;
		minX -= buffer;
		minY -= buffer;

		graphData.width = Math.abs(maxX - minX);
		graphData.height = Math.abs(maxY - minY);
	}
	return graphData;
};
