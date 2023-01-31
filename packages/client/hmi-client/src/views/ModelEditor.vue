<template>
	<section class="container" style="max-height: 90vh; overflow-y: auto">
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
// import graphScaffolder, { IGraph } from '@graph-scaffolder/index';
import graphScaffolder from '@graph-scaffolder/index';
import { runDagreLayout2, D3SelectionINode, D3SelectionIEdge } from '@/services/graph';
import { onMounted, ref, computed, watch } from 'vue';
import { PetriNet } from '@/utils/petri-net-validator';
import { parsePetriNet2IGraph, NodeData, EdgeData, NodeType } from '@/services/model';
import Button from 'primevue/button';
import { useRouter } from 'vue-router';

const router = useRouter();

const variablesRef = ref<any[]>([]);
const stateVariables = computed(() => variablesRef.value.filter((d) => d.type === 'S'));
const paramVariables = computed(() => variablesRef.value.filter((d) => d.type === 'T'));

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
		const species = selection.filter((d) => d.data.type === 'S' || d.data.type === NodeType.State);
		const transitions = selection.filter(
			(d) => d.data.type === 'T' || d.data.type === NodeType.Transition
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

// FIXME: Hackathon
const MANFRED = {
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

const SIRD2 = {
	S: [
		{
			sname: 'Susceptible'
		},
		{
			sname: 'Exposed'
		},
		{
			sname: 'Infected'
		},
		{
			sname: 'Recovered'
		},
		{
			sname: 'Hospitalized'
		}
	],
	T: [
		{
			tname: 'beta'
		},
		{
			tname: 'epsilon'
		},
		{
			tname: 'gamma'
		},
		{
			tname: 'h'
		},
		{
			tname: 'r'
		}
	],
	I: [
		{
			is: 3,
			it: 1
		},
		{
			is: 1,
			it: 1
		},
		{
			is: 2,
			it: 2
		},
		{
			is: 3,
			it: 3
		},
		{
			is: 3,
			it: 4
		},
		{
			is: 5,
			it: 5
		}
	],
	O: [
		{
			os: 3,
			ot: 1
		},
		{
			os: 2,
			ot: 1
		},
		{
			os: 3,
			ot: 2
		},
		{
			os: 4,
			ot: 3
		},
		{
			os: 5,
			ot: 4
		},
		{
			os: 4,
			ot: 5
		}
	]
};

const TEST_METADATA = JSON.parse(
	'{"S": [{"sname": "S", "uid": 15}, {"sname": " I", "uid": 16}, {"sname": " D", "uid": 17}, {"sname": " A", "uid": 18}, {"sname": " R", "uid": 19}, {"sname": " T", "uid": 20}, {"sname": " H", "uid": 21}, {"sname": " E", "uid": 22}], "T": [{"tname": "alpha", "uid": 0}, {"tname": " beta", "uid": 1}, {"tname": " gamma", "uid": 2}, {"tname": " delta", "uid": 3}, {"tname": " epsilon", "uid": 4}, {"tname": " mu", "uid": 5}, {"tname": " zeta", "uid": 6}, {"tname": " lamb", "uid": 7}, {"tname": " eta", "uid": 8}, {"tname": " rho", "uid": 9}, {"tname": " theta", "uid": 10}, {"tname": " kappa", "uid": 11}, {"tname": " nu", "uid": 12}, {"tname": " xi", "uid": 13}, {"tname": " sigma", "uid": 14}, {"tname": " tau", "uid": 15}], "I": [], "O": []}'
);

const TEST_META = JSON.parse(
	'{"0": "Test metadata 0", "1": "Test metadata 1","2": "Test metadata 2","3": "Test metadata 3","4": "Test metadata 4","5": "Test metadata 5","6": "Test metadata 6","7": "Test metadata 7","8": "Test metadata 8","9": "Test metadata 9","10": "Test metadata 10","11": "Test metadata 11","12": "Test metadata 12","13": "Test metadata 13","14": "Test metadata 14","15": "Test metadata 15","16": "Test metadata 16","17": "Test metadata 17","18": "Test metadata 18","19": "Test metadata 19","20": "Test metadata 20","21": "Test metadata 21","22": "Test metadata 22"}'
);

/*
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
*/

// Tracking variables
let source: any = null;
let target: any = null;
let nameCounter: number = 0;

const runPetri = () => {
	localStorage.setItem('sim-input-data', JSON.stringify(MANFRED));
	router.push({ path: '/model-runner' });

	/*
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
	*/
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

	renderer.on('node-mouse-enter', (_evtName, _evt, el) => {
		const textContent = TEST_META[el.datum().data.uid];
		el.append('text').attr('x', 50).attr('y', 15).attr('popup', true).text(textContent);
	});
	renderer.on('node-mouse-leave', (_evtName, _evt, el) => {
		el.selectAll('text[popup=true]').remove();
	});

	document.addEventListener('keyup', async (event) => {
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

		// FIXME: Hackathon
		if (event.key === '2') {
			const g = parsePetriNet2IGraph(SIRD2);
			if (renderer) {
				renderer.isGraphDirty = true;
			}
			await renderer?.setData(g);
			await renderer?.render();

			variablesRef.value = [];
			(renderer as any).graph.nodes.forEach((n) => {
				variablesRef.value.push({
					id: n.id,
					name: n.label,
					type: n.data.type,
					description: '',
					concept: '',
					value: 0
				});
			});
			variablesRef.value.find((d) => d.name === 'Susceptible').value = 9979999.0;
			variablesRef.value.find((d) => d.name === 'Exposed').value = 20000.0;
			variablesRef.value.find((d) => d.name === 'Infected').value = 1.0;
			variablesRef.value.find((d) => d.name === 'Recovered').value = 0.0;
			variablesRef.value.find((d) => d.name === 'Hospitalized').value = 0.0;
			variablesRef.value.find((d) => d.name === 'beta').value = 0.833;
			variablesRef.value.find((d) => d.name === 'epsilon').value = 0.33333;
			variablesRef.value.find((d) => d.name === 'gamma').value = 0.125;
			variablesRef.value.find((d) => d.name === 'h').value = 0.0;
			variablesRef.value.find((d) => d.name === 'r').value = 0.0;
		} else if (event.key === '3') {
			const g = parsePetriNet2IGraph(TEST_METADATA);
			if (renderer) {
				renderer.isGraphDirty = true;
			}
			await renderer?.setData(g);
			await renderer?.render();
			variablesRef.value = [];
			(renderer as any).graph.nodes.forEach((n) => {
				variablesRef.value.push({
					id: n.id,
					name: n.label,
					type: n.data.type,
					description: '',
					concept: '',
					value: 0
				});
			});
		}
	});

	const g = parsePetriNet2IGraph(SIRD);
	await renderer.setData(g);
	await renderer.render();

	// FIXME: Hackathon
	if (renderer.graph.edges.length === 0) {
		let c = 0;
		renderer?.graph.nodes.forEach((n) => {
			n.x = 60 + Math.round(c / 5) * 50;
			n.y = 80 + 50 * (c % 5);
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

<style scoped>
#playground {
	width: 1000px;
	height: 350px;
	border: 1px solid #bbb;
}
</style>
