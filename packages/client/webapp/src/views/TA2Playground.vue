<script lang="ts">
import { BasicRenderer, IGraph, INode, IEdge, traverseGraph } from 'graph-scaffolder';
import * as d3 from 'd3';
import _ from 'lodash';
import dagre from 'dagre';
import { defineComponent } from 'vue';

const runLayout = <V, E>(graphData: IGraph<V, E>): IGraph<V, E> => {
	const g = new dagre.graphlib.Graph({ compound: true });
	g.setGraph({});
	g.setDefaultEdgeLabel(() => ({}));

	traverseGraph(graphData, (node: INode<V>) => {
		if (node.width && node.height) {
			g.setNode(node.id, { label: node.id, width: node.width, height: node.height });
		} else {
			g.setNode(node.id, { label: node.id });
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

	// eslint-disable-next-line
	for (const edge of graphData.edges) {
		const e = g.edge(edge.source, edge.target);
		edge.points = e.points;
	}
	return graphData;
};

interface NodeData {
	type: string;
}

interface EdgeData {
	val: number;
}

type D3SelectionINode<T> = d3.Selection<d3.BaseType, INode<T>, null, any>;
type D3SelectionIEdge<T> = d3.Selection<d3.BaseType, IEdge<T>, null, any>;

let g: IGraph<NodeData, EdgeData> = {
	width: 500,
	height: 500,
	nodes: [
		{
			id: 'susceptible',
			label: 'susceptible',
			x: 0,
			y: 0,
			height: 50,
			width: 50,
			data: { type: 'species' },
			nodes: []
		},
		{
			id: 'infected',
			label: 'infected',
			x: 0,
			y: 0,
			height: 50,
			width: 50,
			data: { type: 'species' },
			nodes: []
		},
		{
			id: 'recovered',
			label: 'recovered',
			x: 0,
			y: 0,
			height: 50,
			width: 50,
			data: { type: 'species' },
			nodes: []
		},
		{
			id: 'infection',
			label: 'infection',
			x: 0,
			y: 0,
			height: 40,
			width: 40,
			data: { type: 'transition' },
			nodes: []
		},
		{
			id: 'recovery',
			label: 'recovery',
			x: 0,
			y: 0,
			height: 40,
			width: 40,
			data: { type: 'transition' },
			nodes: []
		}
	],
	edges: [
		{ id: '1', source: 'susceptible', target: 'infection', points: [], data: null },
		{ id: '2', source: 'infection', target: 'infected', points: [], data: { val: 2 } },
		{ id: '3', source: 'infected', target: 'recovery', points: [], data: null },
		{ id: '4', source: 'recovery', target: 'recovered', points: [], data: null },
		{ id: '5', source: 'infected', target: 'infection', points: [], data: null }
	]
};

export const pathFn = d3
	.line<{ x: number; y: number }>()
	.x((d) => d.x)
	.y((d) => d.y);

class SampleRenderer extends BasicRenderer<NodeData, EdgeData> {
	/*
	constructor(options: any) {
		super(options);
		this.on('node-drag-start', (e, evt) => {
			const shiftKey = evt.sourceEvent.shiftKey;
			if (shiftKey) return;
		});

		this.on('node-drag-move', (e, evt) => {
			const shiftKey = evt.sourceEvent.shiftKey;
			if (shiftKey) return;
		});
	}
	*/

	renderNodes(selection: D3SelectionINode<NodeData>) {
		const species = selection.filter((d) => d.data.type === 'species');
		const transitions = selection.filter((d) => d.data.type === 'transition');

		transitions
			.append('rect')
			.attr('width', (d) => d.width)
			.attr('height', (d) => d.height)
			.style('fill', '#88C')
			.style('stroke', '#888');

		species
			.append('circle')
			.attr('cx', (d) => d.width * 0.5)
			.attr('cy', (d) => d.height * 0.5)
			.attr('r', (d) => d.width * 0.5)
			.attr('fill', '#f80');

		selection
			.append('text')
			.attr('y', -5)
			.text((d) => d.label);
	}

	renderEdges(selection: D3SelectionIEdge<EdgeData>) {
		selection
			.append('path')
			.attr('d', (d) => pathFn(d.points))
			.style('fill', 'none')
			.style('stroke', '#000');
	}
}

let renderer: SampleRenderer | null = null;
g = runLayout(_.cloneDeep(g));

export default defineComponent({
	name: 'TA2Playground',
	async mounted() {
		console.log('TA2 Playground initialized');

		const playground = document.getElementById('playground');
		renderer = new SampleRenderer({
			el: playground,
			useAStarRouting: true,
			runLayout
		});

		// Test
		const test = await fetch('http://localhost:8888/api/models', { method: 'PUT' });
		const testData = await test.json();
		console.log('test data', testData);

		this.refresh();
	},
	methods: {
		async refresh() {
			await renderer.setData(g);
			await renderer.render();
		},
		addPlace() {
			console.log('add place');
			const now = `${Date.now()}`;
			g.nodes.push({
				id: now,
				label: now,
				x: Math.random() * 400,
				y: Math.random() * 400,
				height: 50,
				width: 50,
				data: { type: 'species' },
				nodes: []
			});
			this.refresh();
		},
		addTransition() {
			console.log('add transition');
			const now = `${Date.now()}`;
			g.nodes.push({
				id: now,
				label: now,
				x: Math.random() * 400,
				y: Math.random() * 400,
				height: 50,
				width: 50,
				data: { type: 'transition' },
				nodes: []
			});
			this.refresh();
		}
	}
});
</script>

<template>
	<button type="button" @click="addPlace">Add place</button>
	<button type="button" @click="addTransition">Add transition</button>
	<div id="playground" style="width: 400px; height: 400px; border: 1px solid #888"></div>
</template>
