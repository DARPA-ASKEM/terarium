<script lang="ts">
import graphScaffolder, { IGraph } from '@graph-scaffolder/index';
import {
	parsePetriNet2IGraph,
	petriNetValidator,
	PetriNet,
	NodeData,
	EdgeData
} from '@/petrinet/petrinet-service';
import * as d3 from 'd3';
import _ from 'lodash';
import { defineComponent, ref } from 'vue';
import { fetchStratificationResult } from '@/services/models/stratification-service';
import { runDagreLayout, D3SelectionINode, D3SelectionIEdge } from '@/services/graph';
import API from '@/api/api';

enum NodeType {
	Species = 'S',
	Transition = 'T'
}

// Graphs
let g: IGraph<NodeData, EdgeData> = { width: 500, height: 500, nodes: [], edges: [] };
let g2: IGraph<NodeData, EdgeData> = { width: 500, height: 500, nodes: [], edges: [] };

export const pathFn = d3
	.line<{ x: number; y: number }>()
	.x((d) => d.x)
	.y((d) => d.y);

const MARKER_VIEWBOX = '-5 -5 10 10';
const ARROW = 'M 0,-3.25 L 5 ,0 L 0,3.25';

// Initialize petrinets
let modelA: PetriNet = { T: [], S: [], I: [], O: [] };

let selectedOntology;

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
		T: [
			{ tname: 'infect', ontology: 'inf' },
			{ tname: 'disease', ontology: 'disease' },
			{ tname: 'strata', ontology: 'strata' }
		],
		S: [{ sname: 'Pop', ontology: 'Population' }],
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
			(d) =>
				d.data.type === 'species' ||
				(d.data.type === NodeType.Species && d.data.ontology === undefined)
		);
		const typedSpecies = selection.filter(
			(d) =>
				d.data.type === 'species' ||
				(d.data.type === NodeType.Species && d.data.ontology !== undefined)
		);
		const transitions = selection.filter(
			(d) =>
				d.data.type === 'transition' ||
				(d.data.type === NodeType.Transition && d.data.ontology === undefined)
		);
		const typedTransitions = selection.filter(
			(d) =>
				d.data.type === 'transition' ||
				(d.data.type === NodeType.Transition && d.data.ontology !== undefined)
		);

		transitions
			.append('rect')
			.classed('shape', true)
			.attr('width', (d) => d.width)
			.attr('height', (d) => d.height)
			.style('fill', '#88A')
			.style('stroke', '#888');

		typedTransitions
			.append('rect')
			.classed('shape', true)
			.attr('width', (d) => d.width)
			.attr('height', (d) => d.height)
			.style('fill', '#88E')
			.style('stroke', '#888');

		species
			.append('circle')
			.classed('shape', true)
			.attr('cx', (d) => d.width * 0.5)
			.attr('cy', (d) => d.height * 0.5)
			.attr('r', (d) => d.width * 0.5)
			.attr('fill', '#f30');

		typedSpecies
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
let rendererOntology: SampleRenderer | null = null;

g = runDagreLayout(_.cloneDeep(g));
g2 = runDagreLayout(_.cloneDeep(g2));

let modelId = ''; // The session model
let source: any = null;
let target: any = null;
let typeMapping: string[] = [];

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
		const ontologyPlayground = document.getElementById('ontology-playground') as HTMLDivElement;
		rendererOntology = new SampleRenderer({
			el: ontologyPlayground ?? undefined,
			useAStarRouting: true,
			runLayout: runDagreLayout
		});

		renderer.on('node-click', (_evtName, evt, d) => {
			if (evt.altKey) {
				if (selectedOntology) {
					d.datum().data.ontology = selectedOntology;
					console.log(d.datum().label);
					typeMapping.push('{' + d.datum().label.toString() + ': ' + selectedOntology + '}');
					//Rerender
					renderer?.setData(g);
					renderer?.render();
					d3.select('#solution').text(typeMapping.join(', \n'));
					// let solutionOutput = g.nodes;
					// console.log("Solution output:");
					// console.log(solutionOutput);
					// d3.select('#solution').text(JSON.stringify(solutionOutput, null, 2));
				} else {
					console.log('No selected ontology found');
				}
			} else if (evt.shiftKey) {
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

		rendererOntology.on('node-click', (_evtName, evt, d) => {
			if (evt.altKey) {
				console.log(d.datum());
				selectedOntology = d.datum().data.ontology;
				console.log(selectedOntology);
			}
		});

		// Test
		const resp = await API.put('/model-service/models');
		modelId = resp.data.id;
	},
	setup() {
		const stratifyModelA = ref('');
		const stratifyModelB = ref('');
		const stratifyTypeModel = ref('');
		return {
			stratifyModelA,
			stratifyModelB,
			stratifyTypeModel
		};
	},
	methods: {
		async refresh() {
			await renderer?.setData(g);
			await renderer?.render();
			await rendererOntology?.setData(g2);
			await rendererOntology?.render();
			console.log(selectedOntology);
		},

		async jsonOutput() {
			const resp = await API.get(`model-service/models/${modelId}/json`);
			const output = await resp.data;
			console.log(output);

			if (petriNetValidator(output) === true) {
				modelA = output;
				this.refresh();
			}

			// d3.select('#output').text(JSON.stringify(output, null, 2));
			// d3.select('#solution').text(JSON.stringify(g, null, 2));
			d3.select('#solution').text(typeMapping.join(', \n'));
		},
		// eslint-disable-next-line

		async stratify() {
			console.log('Start stratify');
			try {
				const outputModel = await fetchStratificationResult(
					this.stratifyModelA,
					this.stratifyModelB,
					this.stratifyTypeModel
				);
				//TODO
				console.log(outputModel);
			} catch (e: any) {
				console.error(e.message);
			}
			d3.select('#solution').text('Hello'); //JSON.stringify(output, null, 2));
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
		clearA() {
			modelA = { T: [], S: [], I: [], O: [] };
			g = { width: 500, height: 500, nodes: [], edges: [] };
			g = runDagreLayout(_.cloneDeep(g));
			this.refresh();
		},
		loadOntology() {
			let model = petrinets[3];
			g2 = parsePetriNet2IGraph(model);
			g2 = runDagreLayout(_.cloneDeep(g2));
			this.refresh();
			this.jsonOutput();
			console.log(model);
		},
		typeModel() {
			console.log('Start typing model');
			//Model 1 =
			//Ontology Model =
			//Mapping =
			//Api Call
		},
		async testStratify() {
			console.log('Start stratify');
			try {
				const resp = await API.get(`model-service/models/testStratify`);
				const output = resp.data;
				console.log('Done Stratify');
				//TODO
				console.log(output);
			} catch (e: any) {
				console.error(e.message);
			}
		}
	}
});
</script>
<template>
	<section class="playground">
		<p>A playground for testing TA2 API integrations.</p>
		<!-- <button type="button" @click="createSampleModels">Create Sample Models</button> -->
		&nbsp;
		<form>
			<label for="stratify">
				<input v-model="stratifyModelA" type="text" placeholder="Model A ID" />
				<input v-model="stratifyModelB" type="text" placeholder="Model B" />
				<input v-model="stratifyTypeModel" type="textbox" placeholder="Connection JSON" />
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
				<button type="button" @click="loadOntology">Load Ontology</button>
				<button type="button" @click="typeModel">Type Model</button>
				<button type="button" @click="testStratify">Test Stratify</button>
			</div>
		</div>

		<div style="display: flex">
			<div id="playground" class="playground-panel"></div>
			<div id="ontology-playground" class="playground-panel"></div>
			<div id="solution" class="playground-panel"></div>
			<div id="output" class="playground-panel"></div>
		</div>
	</section>
</template>

<style scoped>
.playground {
	margin: 10px;
	overflow: auto;
}

.playground-panel {
	width: 500px;
	height: 500px;
	border: 1px solid #888;
	overflow: auto;
}

.model-titles {
	display: flex;
	font-weight: bold;
}

.model-titles div {
	width: 500px;
}
</style>
