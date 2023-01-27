<template>
	<section class="container">
		<div style="display: flex">
			<div id="playground"></div>
			<div id="parameters"></div>
		</div>
		<Button @click="runPetri()">Run simulation</Button>
		<Button @click="addVariable('S')">Add state</Button>
		<Button @click="addVariable('T')">Add transition</Button>

		<div>States</div>
		<table>
			<thead>
				<th>Name</th>
				<th>Value</th>
				<th>Description</th>
				<th>Concept</th>
			</thead>
			<tr v-for="v of stateVariables" :key="v.id">
				<td>
					<input v-model="v.name" />
				</td>
				<td>
					<input v-model.number="v.value" style="text-align: end" />
				</td>
				<td>
					<input placeholder="Description..." v-model="v.description" />
				</td>
				<td>
					<input placeholder="Concept..." v-model="v.concept" />
				</td>
			</tr>
		</table>

		<div>Parameters</div>
		<table>
			<thead>
				<th>Name</th>
				<th>Value</th>
				<th>Description</th>
				<th>Concept</th>
			</thead>
			<tr v-for="v of paramVariables" :key="v.id">
				<td>
					<input v-model="v.name" />
				</td>
				<td>
					<input v-model.number="v.value" style="text-align: end" />
				</td>
				<td>
					<input placeholder="Description..." el="v.description" />
				</td>
				<td>
					<input placeholder="Concept..." v-model="v.concept" />
				</td>
			</tr>
		</table>
	</section>
</template>

<script setup lang="ts">
import * as d3 from 'd3';
import _ from 'lodash';
import graphScaffolder, { IGraph } from '@graph-scaffolder/index';
import { runDagreLayout2, D3SelectionINode, D3SelectionIEdge } from '@/services/graph';
import { onMounted, ref, computed, watch } from 'vue';
import { PetriNet } from '@/utils/petri-net-validator';
import { parsePetriNet2IGraph } from '@/services/model';
import Button from 'primevue/button';

const variablesRef = ref<any[]>([]);

const stateVariables = computed(() => variablesRef.value.filter((d) => d.type === 'S'));

const paramVariables = computed(() => variablesRef.value.filter((d) => d.type === 'T'));

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

const pathFn = d3
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
			.style('fill', '#89BEFF')
			.style('stroke', '#888');

		species
			.append('circle')
			.classed('shape', true)
			.attr('cx', (d) => d.width * 0.5)
			.attr('cy', (d) => d.height * 0.5)
			.attr('r', (d) => d.width * 0.8)
			.attr('fill', '#FF90A9');

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

	addEdge(source: any, target: any) {
		this.graph.edges.push({
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
		this.render();
	}
}

let renderer: SampleRenderer | null = null;

// const emptyModel: PetriNet = { T: [], S: [], I: [], O: [] };

// SIRD
const SIRD: PetriNet = {
	// ['dSdt', 'dEdt', 'dIdt', 'dRdt', 'dDdt', 'dNdt']  ['y', 't', 'N0', 'alpha', 'beta', 'gamma', 'epsilon', 'mu']
	S: [
		{ sname: 'dSdt' },
		{ sname: 'dEdt' },
		{ sname: 'dIdt' },
		{ sname: 'dRdt' },
		{ sname: 'dDdt' },
		{ sname: 'dNdt' }
	],
	T: [
		{ tname: 'y' },
		{ tname: 't' },
		{ tname: 'N0' },
		{ tname: 'apha' },
		{ tname: 'beta' },
		{ tname: 'epsilon' },
		{ tname: 'mu' }
	],
	I: [],
	O: []

	/*
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
	*/
};

const graph2petri = (graph: IGraph<NodeData, EdgeData>) => {
	const petri: PetriNet = {
		S: [],
		T: [],
		I: [],
		O: []
	};

	const nodes = graph.nodes;
	for (let i = 0; i < nodes.length; i++) {
		const node = nodes[i];
		if (node.data.type === 'S') {
			petri.S.push({ sname: node.id });
		} else {
			petri.T.push({ tname: node.id });
		}
	}

	const edges = graph.edges;
	for (let i = 0; i < edges.length; i++) {
		const edge = edges[i];
		const source = edge.source;
		const target = edge.target;

		const S = petri.S.map((s) => s.sname);
		const T = petri.T.map((t) => t.tname);

		if (S.includes(source)) {
			const is = S.indexOf(source) + 1;
			const it = T.indexOf(target) + 1;
			petri.I.push({ is, it });
		} else {
			const ot = T.indexOf(source) + 1;
			const os = S.indexOf(target) + 1;
			petri.O.push({ os, ot });
		}
	}
	petri.S = [];
	petri.T = [];

	for (let i = 0; i < nodes.length; i++) {
		const node = nodes[i];
		if (node.data.type === 'S') {
			petri.S.push({ sname: node.label });
		} else {
			petri.T.push({ tname: node.label });
		}
	}
	return petri;
};

// Tracking variables
let source: any = null;
let target: any = null;
let nameCounter: number = 0;

const runPetri = () => {
	const petri = graph2petri(renderer?.graph as any);
	const parameters = {};
	const initials = {};

	stateVariables.value.forEach((s) => {
		initials[s.name] = s.value;
	});
	paramVariables.value.forEach((s) => {
		parameters[s.name] = s.value;
	});

	const final = {
		petri,
		payload: {
			inital_values: initials,
			parameters
		}
	};
	console.log('final', final);
	console.log('final', JSON.stringify(final));
	return final;
};

const addVariable = (vType: string) => {
	if (!renderer) return;
	nameCounter++;

	const id = `state:${nameCounter}`;

	renderer.graph.nodes.push({
		id,
		label: id,
		x: 200,
		y: 200,
		width: 20,
		height: 20,
		data: {
			type: vType
		},
		nodes: []
	});

	variablesRef.value.push({
		id,
		name: id,
		type: vType,
		description: '',
		concept: '',
		value: 0
	});
	renderer.render();
};

watch(
	variablesRef,
	() => {
		variablesRef.value.forEach((v) => {
			const updated = renderer?.chart?.selectAll('.node-ui').filter((d: any) => d.id === v.id);
			updated?.each((d: any) => {
				d.label = v.name;
			});
			updated?.select('text').text(v.name);
		});
	},
	{
		immediate: true,
		deep: true
	}
);
// Entry point
onMounted(async () => {
	const playground = document.getElementById('playground') as HTMLDivElement;
	renderer = new SampleRenderer({
		el: playground,
		useAStarRouting: true,
		runLayout: runDagreLayout2,
		useStableZoomPan: true
	});

	renderer.on('background-click', () => {
		source = null;
		target = null;
		renderer?.render();
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
			renderer?.addEdge(source, target);
			source = null;
			target = null;
		}
	});

	document.addEventListener('keyup', (event) => {
		if (event.key === 'Backspace' && renderer) {
			if (source) {
				_.remove(
					renderer.graph.edges,
					(e) => e.source === source.datum().id || e.target === source.datum().id
				);
			}
			_.remove(renderer.graph.nodes, (n) => n.id === source.datum().id);

			variablesRef.value = variablesRef.value.filter((v) => v.id !== source.datum().id);

			renderer.render();
			source = null;
			target = null;
		}
	});

	const g = parsePetriNet2IGraph(SIRD);
	await renderer.setData(g);
	await renderer.render();
	if (renderer.graph.edges.length === 0) {
		let c = 0;
		renderer?.graph.nodes.forEach((n) => {
			n.x = 60 + Math.round(c / 5) * 50;
			n.y = 50 * (c % 5);
			c++;
		});
		await renderer.render();
	}

	renderer.graph.nodes.forEach((n) => {
		variablesRef.value.push({
			id: n.id,
			name: n.label,
			type: n.data.type,
			description: '',
			concept: '',
			value: 0
		});
	});
});
</script>

<style>
#playground {
	width: 1000px;
	height: 450px;
	border: 1px solid #bbb;
}
</style>
