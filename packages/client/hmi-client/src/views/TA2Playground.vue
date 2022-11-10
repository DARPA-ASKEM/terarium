<script lang="ts">
import _ from 'lodash';
import graphScaffolder, { IGraph } from '@graph-scaffolder/index';
import { runDagreLayout, D3SelectionINode, D3SelectionIEdge } from '@/services/graph';
import * as d3 from 'd3';
import { defineComponent } from 'vue';

interface NodeData {
	type: string;
}

interface EdgeData {
	val: number;
}

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
g = runDagreLayout(_.cloneDeep(g));

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
			runLayout: runDagreLayout
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
		}
	}
});
</script>

<template>
	<section class="playground">
		<p>A playground for testing TA2 API integrations.</p>
		<button type="button" @click="addPlace">Add place</button>
		<button type="button" @click="addTransition">Add transition</button>
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
