<script lang="ts">
import graphScaffolder, { IGraph } from '@graph-scaffolder/index';
import { parsePetriNet2IGraph, petriNetValidator, PetriNet } from '@/petrinet/petrinet-service';
import * as d3 from 'd3';
import _ from 'lodash';
import { defineComponent, ref } from 'vue';
import { fetchStratificationResult } from '@/services/models/stratification-service';
import { runDagreLayout, D3SelectionINode, D3SelectionIEdge } from '@/services/graph';
import API from '@/api/api';

interface NodeData {
	type: string;
}
interface EdgeData {
	val: number;
}
enum NodeType {
	Species = 'S',
	Transition = 'T'
}

// Graphs
let g: IGraph<NodeData, EdgeData> = { width: 500, height: 500, nodes: [], edges: [] };
let g2: IGraph<NodeData, EdgeData> = { width: 500, height: 500, nodes: [], edges: [] };
let g3: IGraph<NodeData, EdgeData> = { width: 500, height: 500, nodes: [], edges: [] };

export const pathFn = d3
	.line<{ x: number; y: number }>()
	.x((d) => d.x)
	.y((d) => d.y);

const MARKER_VIEWBOX = '-5 -5 10 10';
const ARROW = 'M 0,-3.25 L 5 ,0 L 0,3.25';

// Initialize petrinets
let modelA: PetriNet = { T: [], S: [], I: [], O: [] };
let modelB: PetriNet = { T: [], S: [], I: [], O: [] };
let mergedModel: PetriNet = { T: [], S: [], I: [], O: [] };

// Possible models to spawn
const petrinets: PetriNet[] = [
	// generic
	{
		T: [{ tname: 't-1' }, { tname: 't-2' }],
		S: [{ sname: 'p-1' }, { sname: 'p-2' }, { sname: 'p-3' }],
		I: [
			{ it: 1, is: 1 },
			{ it: 2, is: 2 }
		],
		O: [
			{ ot: 1, os: 2 },
			{ ot: 2, os: 1 },
			{ ot: 2, os: 3 }
		]
	},
	// generic2
	{
		T: [{ tname: 't-1' }, { tname: 't-2' }],
		S: [{ sname: 'p-1' }, { sname: 'p-2' }, { sname: 'p-3' }],
		I: [
			{ it: 1, is: 1 },
			{ it: 2, is: 1 }
		],
		O: [
			{ ot: 1, os: 2 },
			{ ot: 2, os: 3 }
		]
	},
	// QNotQModel
	{
		T: [{ tname: 'quarantine' }, { tname: 'unquarantine' }],
		S: [{ sname: 'Q' }, { sname: 'NQ' }],
		I: [
			{ it: 1, is: 2 },
			{ it: 2, is: 1 }
		],
		O: [
			{ ot: 1, os: 1 },
			{ ot: 2, os: 2 }
		]
	},
	// typeModel
	{
		T: [{ tname: 'infect' }, { tname: 'disease' }, { tname: 'strata' }],
		S: [{ sname: 'Pop' }],
		I: [
			{ it: 1, is: 1 },
			{ it: 1, is: 1 },
			{ it: 2, is: 1 },
			{ it: 3, is: 1 }
		],
		O: [
			{ ot: 1, os: 1 },
			{ ot: 1, os: 1 },
			{ ot: 2, os: 1 },
			{ ot: 3, os: 1 }
		]
	},
	// SIRD
	{
		T: [{ tname: 'inf' }, { tname: 'recover' }, { tname: 'death' }],
		S: [{ sname: 'S' }, { sname: 'I' }, { sname: 'R' }, { sname: 'D' }],
		I: [
			{ it: 1, is: 1 },
			{ it: 1, is: 2 },
			{ it: 2, is: 2 },
			{ it: 3, is: 2 }
		],
		O: [
			{ ot: 1, os: 2 },
			{ ot: 1, os: 2 },
			{ ot: 2, os: 3 },
			{ ot: 3, os: 4 }
		]
	}
];

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
		const species = selection.filter(
			(d) => d.data.type === 'species' || d.data.type === NodeType.Species
		);
		const transitions = selection.filter(
			(d) => d.data.type === 'transition' || d.data.type === NodeType.Transition
		);

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
let rendererB: SampleRenderer | null = null;
let rendererC: SampleRenderer | null = null;
g = runDagreLayout(_.cloneDeep(g));
g2 = runDagreLayout(_.cloneDeep(g2));
g3 = runDagreLayout(_.cloneDeep(g3));

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

		const playgroundB = document.getElementById('modelB') as HTMLDivElement;
		rendererB = new SampleRenderer({
			el: playgroundB ?? undefined,
			useAStarRouting: true,
			runLayout: runDagreLayout
		});

		const playgroundC = document.getElementById('merged-petrinets') as HTMLDivElement;
		rendererC = new SampleRenderer({
			el: playgroundC ?? undefined,
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
		const resp = await API.put('/model-service/models');
		modelId = resp.data.id;
	},
	setup() {
		const loadModelID = ref('');
		const stratifyModelA = ref('');
		const stratifyModelB = ref('');
		const stratifyTypeModel = ref('');
		const stateNamesA = ref('');
		const stateNamesB = ref('');
		return {
			loadModelID,
			stratifyModelA,
			stratifyModelB,
			stratifyTypeModel,
			stateNamesA,
			stateNamesB
		};
	},
	methods: {
		async refresh() {
			await renderer?.setData(g);
			await renderer?.render();
			await rendererB?.setData(g2);
			await rendererB?.render();
			await rendererC?.setData(g3);
			await rendererC?.render();
		},
		async LotkaVolterra() {
			const resp = await API.put('model-service/models');
			modelId = resp.data.id;

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

			g = runDagreLayout(_.cloneDeep(g));

			API.post(`model-service/models/${modelId}`, {
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
			});

			this.refresh();
			this.jsonOutput();
		},
		async jsonOutput() {
			const resp = await API.get(`model-service/models/${modelId}/json`);
			const output = await resp.data;
			console.log(output);

			if (petriNetValidator(output) === true) {
				modelA = output;
				this.refresh();
			}

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

			API.post(`model-service/models/${modelId}`, {
				edges: [
					{
						source: source.datum().id,
						target: target.datum().id
					}
				]
			});

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

			API.post(`model-service/models/${modelId}`, {
				nodes: [
					{
						name: id,
						type: 'S'
					}
				]
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

			API.post(`model-service/models/${modelId}`, {
				nodes: [
					{
						name: id,
						type: 'T'
					}
				]
			});
			this.jsonOutput();
		},
		async simulate() {
			numWolves = +(Math.random() * 100).toFixed();
			numRabbits = +(Math.random() * 100).toFixed();

			// Run a simulation on LotkaVolterra with random values
			const resp = await API.post(`model-service/models/${modelId}/simulate`, {
				variables: {
					rabbits: numRabbits,
					wolves: numWolves
				},
				parameters: {
					birth: 0.3,
					predation: 0.015,
					death: 0.7
				}
			});
			const output = resp.data;
			this.renderResult(output);
		},
		renderResult(result: any) {
			const el = d3.select('#solution');
			el.selectAll('*').remove();

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
			const stateNamesArrayA = this.stateNamesA.split(',');
			const stateNamesArrayB = this.stateNamesB.split(',');

			if (
				stateNamesArrayA.length !== stateNamesArrayB.length ||
				this.stateNamesA.length < 1 ||
				this.stateNamesB.length < 1
			) {
				console.log('Not enough states');
				return;
			}

			const statesToMerge: { modelA: string; modelB: string }[] = [];

			for (let i = 0; i < stateNamesArrayA.length; i++) {
				statesToMerge.push({
					modelA: stateNamesArrayA[i].trim(),
					modelB: stateNamesArrayB[i].trim()
				});
			}
			console.log(modelA);
			const resp = await API.post(`model-service/models/model-composition`, {
				modelA,
				modelB,
				statesToMerge
			});
			mergedModel = await resp.data;
			console.log(`Merged petrinet ${mergedModel}`);

			g3 = parsePetriNet2IGraph(mergedModel);
			g3 = runDagreLayout(_.cloneDeep(g3));
			this.refresh();
			this.jsonOutput();
		},
		// Pulls model ID from form and sends model to createModel function for the actual work
		async drawModel() {
			const resp = await API.get(`model-service/models/${this.loadModelID}/json`);
			const model: PetriNet = resp.data;
			this.createModel(model, false);
		},

		// Create a model provided if createFlag = True using model-service
		// draw provided model on first layout
		async createModel(model: PetriNet, createFlag = false) {
			// Flag is true so we need to call API PUT new model ID
			if (createFlag === true) {
				const resp = await API.put('/model-service/models');

				const modelData = await resp.data;
				modelId = modelData.id;
				console.log(`Model ID: ${modelId}`); // currently required for testing

				let modelServiceNodes: { name: string; type: string }[] = [];
				let modelServiceEdges: { source: string; target: string }[] = [];

				// add Nodes
				// toString() because post strat it can be an array
				const sNodes = model.S.map((node) => ({
					name: node.sname.toString(),
					type: NodeType.Species
				}));
				const tNodes = model.T.map((node) => ({
					name: node.tname.toString(),
					type: NodeType.Transition
				}));
				modelServiceNodes = [...sNodes, ...tNodes];

				// Add Edges
				// - 1 because modelService indexing
				// + S.length for all transitions as the S are added first to the modelServiceNodes
				// toString because post stratification the name can be a list instead of a string
				const iEdges = model.I.map((edge) => ({
					source: modelServiceNodes[edge.is - 1].name.toString(),
					target: modelServiceNodes[edge.it - 1 + model.S.length].name.toString()
				}));
				const oEdges = model.O.map((edge) => ({
					source: modelServiceNodes[edge.ot - 1 + model.S.length].name.toString(),
					target: modelServiceNodes[edge.os - 1].name.toString()
				}));
				modelServiceEdges = [...iEdges, ...oEdges];

				// Create model in model-service
				await API.post(`model-service/models/${modelId}`, {
					nodes: modelServiceNodes,
					edges: modelServiceEdges
				});
			}
			// Draw model to window
			g = await parsePetriNet2IGraph(model);
			g = runDagreLayout(_.cloneDeep(g));
			this.refresh();
			this.jsonOutput();
		},
		async stratify() {
			console.log('Start stratify');
			try {
				const outputModel = await fetchStratificationResult(
					this.stratifyModelA,
					this.stratifyModelB,
					this.stratifyTypeModel
				);
				console.log('Result');
				console.log(outputModel);
				this.createModel(outputModel, true);
			} catch (e: any) {
				logger.error(e.message);
			}
		},
		// Used to create sample models for stratifying tests
		// Will not be requried in the long run as we will be moving to storing these in DB
		async spawnModelA(e) {
			modelA = petrinets[e.target.value];
			g = await parsePetriNet2IGraph(modelA);
			g = runDagreLayout(_.cloneDeep(g));
			this.refresh();
			this.jsonOutput();
		},
		async spawnModelB(e) {
			modelB = petrinets[e.target.value];
			g2 = await parsePetriNet2IGraph(modelB);
			g2 = runDagreLayout(_.cloneDeep(g2));
			this.refresh();
			this.jsonOutput();
		},
		clearA() {
			modelA = { T: [], S: [], I: [], O: [] };
			g = { width: 500, height: 500, nodes: [], edges: [] };
			g = runDagreLayout(_.cloneDeep(g));
			this.refresh();
		},
		clearB() {
			modelB = { T: [], S: [], I: [], O: [] };
			g2 = { width: 500, height: 500, nodes: [], edges: [] };
			g2 = runDagreLayout(_.cloneDeep(g2));
			this.refresh();
		},
		async createSampleModels() {
			// TODO: Add Petri Net type to this when merged with other PR
			const SIRDModel: PetriNet = {
				T: [{ tname: 'inf' }, { tname: 'recover' }, { tname: 'death' }],
				S: [{ sname: 'S' }, { sname: 'I' }, { sname: 'R' }, { sname: 'D' }],
				I: [
					{ it: 1, is: 1 },
					{ it: 1, is: 2 },
					{ it: 2, is: 2 },
					{ it: 3, is: 2 }
				],
				O: [
					{ ot: 1, os: 2 },
					{ ot: 1, os: 2 },
					{ ot: 2, os: 3 },
					{ ot: 3, os: 4 }
				]
			};
			await this.createModel(SIRDModel, true);
			const QNotQModel: PetriNet = {
				T: [{ tname: 'quarantine' }, { tname: 'unquarantine' }],
				S: [{ sname: 'Q' }, { sname: 'NQ' }],
				I: [
					{ it: 1, is: 2 },
					{ it: 2, is: 1 }
				],
				O: [
					{ ot: 1, os: 1 },
					{ ot: 2, os: 2 }
				]
			};
			await this.createModel(QNotQModel, true);
			const typeModel: PetriNet = {
				T: [{ tname: 'infect' }, { tname: 'disease' }, { tname: 'strata' }],
				S: [{ sname: 'Pop' }],
				I: [
					{ it: 1, is: 1 },
					{ it: 1, is: 1 },
					{ it: 2, is: 1 },
					{ it: 3, is: 1 }
				],
				O: [
					{ ot: 1, os: 1 },
					{ ot: 1, os: 1 },
					{ ot: 2, os: 1 },
					{ ot: 3, os: 1 }
				]
			};
			await this.createModel(typeModel, true);
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
		<button type="button" @click="createSampleModels">Create Sample Models</button>
		&nbsp;
		<form>
			<label for="loadModel">
				<input v-model="loadModelID" type="text" placeholder="Model ID" />
			</label>
			<button type="button" @click="drawModel">Load Model</button>
		</form>
		<form>
			<label for="stratify">
				<input v-model="stratifyModelA" type="text" placeholder="Model A ID" />
				<input v-model="stratifyModelB" type="text" placeholder="Model B" />
				<input v-model="stratifyTypeModel" type="text" placeholder="Type Model" />
			</label>
			<button type="button" @click="stratify">Stratify</button>
		</form>
		<br />
		<div class="model-titles">
			<div>
				<label
					>Model A
					<select @change="spawnModelA($event)">
						<option :value="0">Choose model</option>
						<option :value="0">generic</option>
						<option :value="1">generic2</option>
						<option :value="2">QNotQModel</option>
						<option :value="3">typeModel</option>
						<option :value="4">SIRD</option>
					</select>
				</label>
				&nbsp;
				<button type="button" @click="clearA">Clear Model A</button>
			</div>
		</div>

		<div style="display: flex">
			<div id="playground" class="playground-panel"></div>
			<div id="solution" class="playground-panel"></div>
			<div id="output" class="playground-panel"></div>
		</div>
		<br />
		<br />
		<form>
			List states you want to merge eg: | p-1, p-2 | | s-1, s-2 | means p-1 will merge with s-1 and
			p-2 will merge with s-2
			<br />
			<label for="loadModel">
				<input type="text" placeholder="States to merge A" v-model="stateNamesA" />
				<input type="text" placeholder="States to merge B" v-model="stateNamesB" />
				<button type="button" @click="mergePetrinets">Merge petrinets</button>
			</label>
		</form>
		<br />
		<br />
		<div class="model-titles">
			<div>
				<label
					>Model B
					<select @change="spawnModelB($event)">
						<option :value="0">Choose model</option>
						<option :value="0">generic</option>
						<option :value="1">generic2</option>
						<option :value="2">QNotQModel</option>
						<option :value="3">typeModel</option>
						<option :value="4">SIRD</option>
					</select>
				</label>
				&nbsp;
				<button type="button" @click="clearB">Clear Model B</button>
			</div>
			<div>Merged Model</div>
		</div>
		<div style="display: flex">
			<div id="modelB" class="playground-panel"></div>
			<div id="merged-petrinets" class="playground-panel"></div>
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

.model-titles {
	display: flex;
	font-weight: bold;
}

.model-titles div {
	width: 500px;
}
</style>
