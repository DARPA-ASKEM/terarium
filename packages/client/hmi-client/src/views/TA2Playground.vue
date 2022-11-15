<script lang="ts">
import graphScaffolder, { IEdge, IGraph, INode } from '@graph-scaffolder/index';
import { petriNetValidator, PetriNet } from '@/utils/petri-net-validator';
import * as d3 from 'd3';
import _ from 'lodash';
import dagre from 'dagre';
import { defineComponent } from 'vue';

const runLayout = <V, E>(graphData: IGraph<V, E>): IGraph<V, E> => {
	const g = new dagre.graphlib.Graph({ compound: true });
	g.setGraph({});
	g.setDefaultEdgeLabel(() => ({}));

	graphScaffolder.traverseGraph(graphData, (node: INode<V>) => {
		if (node.width && node.height) {
			g.setNode(node.id, {
				label: node.id,
				width: node.width,
				height: node.height,
				x: node.x,
				y: node.y
			});
		} else {
			g.setNode(node.id, { label: node.id, x: node.x, y: node.y });
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
	nodes: [],
	edges: []
};

export const pathFn = d3
	.line<{ x: number; y: number }>()
	.x((d) => d.x)
	.y((d) => d.y);

const MARKER_VIEWBOX = '-5 -5 10 10';
const ARROW = 'M 0,-3.25 L 5 ,0 L 0,3.25';

class SampleRenderer extends graphScaffolder.BasicRenderer<NodeData, EdgeData> {
	setupDefs() {
		const svg = d3.select(this.svgEl);

		// Clean up
		svg.select('defs').selectAll('.edge-marker-end').remove();

		// Arrow defs
		svg
			.select('defs')
			.append('marker')
			.classed('edge-marker-end', true)
			.attr('id', 'arrowhead')
			.attr('viewBox', MARKER_VIEWBOX)
			.attr('refX', 2)
			.attr('refY', 0)
			.attr('orient', 'auto')
			.attr('markerWidth', 15)
			.attr('markerHeight', 15)
			.attr('markerUnits', 'userSpaceOnUse')
			.attr('xoverflow', 'visible')
			.append('svg:path')
			.attr('d', ARROW)
			.style('fill', '#000')
			.style('stroke', 'none');
	}

	renderNodes(selection: D3SelectionINode<NodeData>) {
		const species = selection.filter((d) => d.data.type === 'species');
		const transitions = selection.filter((d) => d.data.type === 'transition');

		transitions
			.append('rect')
			.classed('shape', true)
			.attr('width', (d) => d.width)
			.attr('height', (d) => d.height)
			.style('fill', '#88C')
			.style('stroke', '#888');

		species
			.append('circle')
			.classed('shape', true)
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
			.style('stroke', '#000')
			.style('stroke-width', 2)
			.attr('marker-end', 'url(#arrowhead)');
	}
}

let renderer: SampleRenderer | null = null;
g = runLayout(_.cloneDeep(g));

let placeCounter = 0;
let transitionCounter = 0;
let modelId = ''; // The session model
let source: any = null;
let target: any = null;

let numRabbits = 100;
let numWolves = 10;

export default defineComponent({
	name: 'TA2Playground',
	async mounted() {
		console.log('TA2 Playground initialized');

		const playground = document.getElementById('playground') as HTMLDivElement;
		renderer = new SampleRenderer({
			el: playground ?? undefined,
			useAStarRouting: true,
			runLayout
		});

		renderer.on('node-click', (_evtName, evt, d) => {
			if (evt.shiftKey) {
				if (source) {
					target = d;
					target.select('.shape').style('stroke', '#000').style('stroke-width', 4);
				} else {
					source = d;
					source.select('.shape').style('stroke', '#000').style('stroke-width', 4);
				}
			} else {
				if (source) {
					source.select('.shape').style('stroke', null).style('stroke-width', null);
				}
				if (target) {
					target.select('.shape').style('stroke', null).style('stroke-width', null);
				}
				source = null;
				target = null;
			}

			if (source && target) {
				this.addEdge(source, target);
				source = null;
				target = null;
			}
		});

		// Test
		const test = await fetch('http://localhost:8888/api/models', { method: 'PUT' });
		const testData = await test.json();
		modelId = testData.id;

		this.refresh();
		this.jsonOutput();
	},
	methods: {
		async refresh() {
			await renderer?.setData(g);
			await renderer?.render();
		},
		async LotkaVolterra() {
			const test = await fetch('http://localhost:8888/api/models', { method: 'PUT' });
			const testData = await test.json();
			modelId = testData.id;

			console.log('modle id is', modelId);

			// Reset
			g.nodes = [];
			g.edges = [];

			g.nodes.push({
				id: 'rabbits',
				label: 'rabbits',
				x: 0,
				y: 0,
				height: 50,
				width: 50,
				nodes: [],
				data: { type: 'species' }
			});
			g.nodes.push({
				id: 'wolves',
				label: 'wolves',
				x: 0,
				y: 0,
				height: 50,
				width: 50,
				nodes: [],
				data: { type: 'species' }
			});
			g.nodes.push({
				id: 'death',
				label: 'death',
				x: 0,
				y: 0,
				height: 50,
				width: 50,
				nodes: [],
				data: { type: 'transition' }
			});
			g.nodes.push({
				id: 'birth',
				label: 'birth',
				x: 0,
				y: 0,
				height: 50,
				width: 50,
				nodes: [],
				data: { type: 'transition' }
			});
			g.nodes.push({
				id: 'predation',
				label: 'predation',
				x: 0,
				y: 0,
				height: 50,
				width: 50,
				nodes: [],
				data: { type: 'transition' }
			});

			g.edges.push({ id: '1', source: 'wolves', target: 'death', points: [], data: { val: 1 } });
			g.edges.push({
				id: '2',
				source: 'predation',
				target: 'wolves',
				points: [],
				data: { val: 1 }
			});
			g.edges.push({
				id: '3',
				source: 'wolves',
				target: 'predation',
				points: [],
				data: { val: 1 }
			});
			g.edges.push({
				id: '4',
				source: 'rabbits',
				target: 'predation',
				points: [],
				data: { val: 1 }
			});
			g.edges.push({ id: '5', source: 'rabbits', target: 'birth', points: [], data: { val: 1 } });
			g.edges.push({ id: '6', source: 'birth', target: 'rabbits', points: [], data: { val: 1 } });

			g = runLayout(_.cloneDeep(g));

			await fetch(`http://localhost:8888/api/models/${modelId}`, {
				method: 'POST',
				headers: {
					Accept: 'application/json',
					'Content-Type': 'application/json'
				},
				body: JSON.stringify({
					nodes: [
						{ name: 'rabbits', type: 'S' },
						{ name: 'wolves', type: 'S' },
						{ name: 'birth', type: 'T' },
						{ name: 'death', type: 'T' },
						{ name: 'predation', type: 'T' }
					],
					edges: [
						{ source: 'wolves', target: 'death' },
						{ source: 'predation', target: 'wolves' },
						{ source: 'predation', target: 'wolves' },
						{ source: 'wolves', target: 'predation' },
						{ source: 'rabbits', target: 'predation' },
						{ source: 'rabbits', target: 'birth' },
						{ source: 'birth', target: 'rabbits' },
						{ source: 'birth', target: 'rabbits' }
					]
				})
			});

			this.refresh();
			this.jsonOutput();
		},
		async jsonOutput() {
			const resp = await fetch(`http://localhost:8888/api/models/${modelId}/json`, {
				method: 'GET'
			});
			const output = await resp.json();
			console.log(petriNetValidator(output));
			console.log(output);
			d3.select('#output').text(JSON.stringify(output, null, 2));
		},
		// eslint-disable-next-line
		async addEdge(source: any, target: any) {
			g.edges.push({
				id: `${source.datum().id}_${target.datum().id}`,
				source: source.datum().id,
				target: target.datum().id,
				points: [
					{
						x: source.datum().x + source.datum().width * 0.5,
						y: source.datum().y + source.datum().height * 0.5
					},
					{
						x: target.datum().x + target.datum().width * 0.5,
						y: target.datum().y + target.datum().height * 0.5
					}
				],
				data: { val: 1 }
			});

			await fetch(`http://localhost:8888/api/models/${modelId}`, {
				method: 'POST',
				headers: {
					Accept: 'application/json',
					'Content-Type': 'application/json'
				},
				body: JSON.stringify({
					edges: [
						{
							source: source.datum().id,
							target: target.datum().id
						}
					]
				})
			});

			// g = runLayout(_.cloneDeep(g));
			this.refresh();
			this.jsonOutput();
		},
		async addPlace() {
			console.log('add place');
			placeCounter++;
			const id = `p-${placeCounter}`;

			g.nodes.push({
				id,
				label: id,
				x: Math.random() * 500,
				y: Math.random() * 500,
				height: 50,
				width: 50,
				data: { type: 'species' },
				nodes: []
			});
			this.refresh();

			await fetch(`http://localhost:8888/api/models/${modelId}`, {
				method: 'POST',
				headers: {
					Accept: 'application/json',
					'Content-Type': 'application/json'
				},
				body: JSON.stringify({
					nodes: [
						{
							name: id,
							type: 'S'
						}
					]
				})
			});
			this.jsonOutput();
		},
		async addTransition() {
			console.log('add transition');
			transitionCounter++;
			const id = `t-${transitionCounter}`;

			g.nodes.push({
				id,
				label: id,
				x: Math.random() * 500,
				y: Math.random() * 500,
				height: 50,
				width: 50,
				data: { type: 'transition' },
				nodes: []
			});
			this.refresh();

			await fetch(`http://localhost:8888/api/models/${modelId}`, {
				method: 'POST',
				headers: {
					Accept: 'application/json',
					'Content-Type': 'application/json'
				},
				body: JSON.stringify({
					nodes: [
						{
							name: id,
							type: 'T'
						}
					]
				})
			});
			this.jsonOutput();
		},
		async simulate() {
			numWolves = +(Math.random() * 100).toFixed();
			numRabbits = +(Math.random() * 100).toFixed();

			// Run a simulation on LotkaVolterra with random values
			const resp = await fetch(`http://localhost:8888/api/models/${modelId}/simulate`, {
				method: 'POST',
				headers: {
					Accept: 'application/json',
					'Content-Type': 'application/json'
				},
				body: JSON.stringify({
					variables: {
						rabbits: numRabbits,
						wolves: numWolves
					},
					parameters: {
						birth: 0.3,
						predation: 0.015,
						death: 0.7
					}
				})
			});
			const output = await resp.json();
			this.renderResult(output);
		},
		renderResult(result: any) {
			const el = d3.select('#solution');
			el.selectAll('*').remove();
			console.log(result);

			const svg = el.append('svg').style('width', '100%').style('height', '100%');

			svg
				.append('text')
				.attr('x', 20)
				.attr('y', 35)
				.style('stroke', null)
				.style('fill', 'red')
				.text(`rabbits: ${numRabbits}`);

			svg
				.append('text')
				.attr('x', 120)
				.attr('y', 35)
				.style('stroke', null)
				.style('fill', 'blue')
				.text(`wolves: ${numWolves}`);

			for (let idx = 0; idx < result.u.length; idx++) {
				const bars = result.u[idx];

				svg
					.append('circle')
					.attr('cx', 6 * idx)
					.attr('cy', 200 - bars[0])
					.attr('r', 3)
					.style('stroke', null)
					.style('fill', 'red');

				svg
					.append('circle')
					.attr('cx', 6 * idx)
					.attr('cy', 200 - bars[1])
					.attr('r', 3)
					.style('stroke', null)
					.style('fill', 'blue');
			}
		},
		async mergePetrinets() {
			const modelA: PetriNet = {
				T: [
					{
						tname: 't-1'
					},
					{
						tname: 't-2'
					}
				],
				S: [
					{
						sname: 'p-1'
					},
					{
						sname: 'p-2'
					},
					{
						sname: 'p-3'
					}
				],
				I: [
					{
						it: 1,
						is: 1
					},
					{
						it: 2,
						is: 2
					}
				],
				O: [
					{
						ot: 1,
						os: 2
					},
					{
						ot: 2,
						os: 1
					},
					{
						ot: 2,
						os: 3
					}
				]
			};
			const modelB: PetriNet = {
				T: [
					{
						tname: 't-1'
					},
					{
						tname: 't-2'
					}
				],
				S: [
					{
						sname: 'p-1'
					},
					{
						sname: 'p-2'
					},
					{
						sname: 'p-3'
					}
				],
				I: [
					{
						it: 1,
						is: 1
					},
					{
						it: 2,
						is: 1
					}
				],
				O: [
					{
						ot: 1,
						os: 2
					},
					{
						ot: 2,
						os: 3
					}
				]
			};
			const modelC: PetriNet = {
				T: [
					{
						tname: 't-1'
					},
					{
						tname: 't-2'
					},
					{
						tname: 't-1' // 3
					},
					{
						tname: 't-2' // 4
					}
				],
				S: [
					{
						sname: 'p-1'
					},
					{
						sname: 'p-2'
					},
					{
						sname: 'p-3p-3'
					},
					{
						sname: 'p-1' // 4
					},
					{
						sname: 'p-2' // 5
					}
				],
				I: [
					{
						it: 1,
						is: 1
					},
					{
						it: 2,
						is: 2
					},
					{
						it: 3,
						is: 4
					},
					{
						it: 4,
						is: 4
					}
				],
				O: [
					{
						ot: 1,
						os: 2
					},
					{
						ot: 2,
						os: 1
					},
					{
						ot: 2,
						os: 3
					},
					{
						ot: 4,
						os: 3
					},
					{
						ot: 3,
						os: 5
					}
				]
			};
			const commonStates = [
				{ modelA: 'p-3', modelB: 'p-3' },
				{ modelA: 'p-2', modelB: 'p-1' }
			];
			// const commonStates = [{ modelA: 'p-3', modelB: 'p-3' }];
			// const commonStates = []
			console.log(modelA);
			console.log(modelB);
			console.log('To match', modelC);

			const resp = await fetch(`http://localhost:8888/api/models/${modelId}/model-composition`, {
				method: 'POST',
				headers: {
					Accept: 'application/json',
					'Content-Type': 'application/json'
				},
				body: JSON.stringify({
					modelA,
					modelB,
					commonStates
				})
			});
			const output = await resp.json();
			console.log(output);
		}
	}
});
</script>
<template>
	<section class="playground">
		<p>A playground for testing TA2 API integrations.</p>
		<button type="button" @click="addPlace">Add place</button>
		<button type="button" @click="addTransition">Add transition</button>
		<button type="button" @click="mergePetrinets">Merge</button>
		&nbsp;
		<button type="button" @click="LotkaVolterra">LotkaVolterra</button>
		<button type="button" @click="simulate">Simulate</button>
		<div style="display: flex">
			<div id="playground" class="playground-panel"></div>
			<div id="solution" class="playground-panel"></div>
			<div id="output" class="playground-panel"></div>
		</div>
	</section>
</template>

<style scoped>
.playground {
	margin: 10px;
}

.playground-panel {
	width: 500px;
	height: 500px;
	border: 1px solid #888;
}
</style>
