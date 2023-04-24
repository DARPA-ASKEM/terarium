<template>
	<section class="container" style="max-height: 90vh; overflow-y: auto">
		<div style="display: flex">
			<div id="playground"></div>
			<metadata-table :data="currentNodeMetadata" />
		</div>
		<div>
			<Button style="margin-left: 5px" @click="runPetri()">Run simulation</Button>
			<Button style="margin-left: 5px" @click="addVariable('S')">Add state</Button>
			<Button style="margin-left: 5px" @click="addVariable('T')">Add transition</Button>
			<Button style="margin-left: 5px" @click="onDownload()">Download Petri</Button>
		</div>

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
import _ from 'lodash';
import { IGraph } from '@graph-scaffolder/index';
import { runDagreLayout } from '@/services/graph';
import { onMounted, ref, computed, watch } from 'vue';
import {
	parsePetriNet2IGraph,
	PetriNet,
	NodeData,
	EdgeData,
	mathmlToPetri,
	petriToLatex
} from '@/petrinet/petrinet-service';
import { PetrinetRenderer } from '@/petrinet/petrinet-renderer';
import Button from 'primevue/button';
import { useRouter } from 'vue-router';
import MetadataTable from '@/temp/MetadataTable.vue';

const router = useRouter();

const variablesRef = ref<any[]>([]);
const stateVariables = computed(() => variablesRef.value.filter((d) => d.type === 'S'));
const paramVariables = computed(() => variablesRef.value.filter((d) => d.type === 'T'));
const currentNodeMetadata = ref<any | null>(null);

let renderer: PetrinetRenderer | null = null;

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

const TEST_METADATA = {
	S: [
		{ sname: 'S', uid: 15 },
		{ sname: ' I', uid: 16 },
		{ sname: ' D', uid: 17 },
		{ sname: ' A', uid: 18 },
		{ sname: ' R', uid: 19 },
		{ sname: ' T', uid: 20 },
		{ sname: ' H', uid: 21 },
		{ sname: ' E', uid: 22 }
	],
	T: [
		{ tname: 'alpha', uid: 0 },
		{ tname: ' beta', uid: 1 },
		{ tname: ' gamma', uid: 2 },
		{ tname: ' delta', uid: 3 },
		{ tname: ' epsilon', uid: 4 },
		{ tname: ' mu', uid: 5 },
		{ tname: ' zeta', uid: 6 },
		{ tname: ' lamda', uid: 7 },
		{ tname: ' eta', uid: 8 },
		{ tname: ' rho', uid: 9 },
		{ tname: ' theta', uid: 10 },
		{ tname: ' kappa', uid: 11 },
		{ tname: ' nu', uid: 12 },
		{ tname: ' xi', uid: 13 },
		{ tname: ' sigma', uid: 14 },
		{ tname: ' tau', uid: 23 }
	],
	I: [],
	O: []
};

const TEST_META = {
	'15': {
		type: 'variable',
		name: 'S',
		id: 'v0',
		text_annotations: [' Susceptible (uninfected)'],
		dkg_annotations: [
			['ncit:C171133', 'COVID-19 Infection'],
			['ido:0000514', 'susceptible population']
		],
		equation_annotations: { '\\delta(t)=-S(t)(a l(t)+\\theta D(t)+r A(t)+\\delta t(t)': ['S'] },
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'16': {
		type: 'variable',
		name: 'I',
		id: 'v1',
		text_annotations: [' Infected (asymptomatic or pauci-symptomatic infected, undetected)'],
		dkg_annotations: [
			['ido:0000511', 'infected population'],
			['ncit:C171133', 'COVID-19 Infection']
		],
		equation_annotations: {
			'{\\dot{A}}(t)=\\zeta I(t)-(\\theta+\\mu+\\kappa)A(t)': ['I'],
			'{\\hat{D}}(t)=\\varepsilon I(t)-(\\eta+\\rho)D(t)': ['I']
		},
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'17': {
		type: 'variable',
		name: 'D',
		id: 'v2',
		text_annotations: [' Diagnosed (asymptomatic infected, detected)'],
		dkg_annotations: [
			['ido:0000511', 'infected population'],
			['ncit:C171133', 'COVID-19 Infection']
		],
		equation_annotations: {
			'{\\hat{D}}(t)=\\varepsilon I(t)-(\\eta+\\rho)D(t)': ['D'],
			'\\delta(t)=-S(t)(a l(t)+\\theta D(t)+r A(t)+\\delta t(t)': ['D']
		},
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'18': {
		type: 'variable',
		name: 'A',
		id: 'v3',
		text_annotations: [' Ailing (symptomatic infected, undetected)'],
		dkg_annotations: [
			['ido:0000511', 'infected population'],
			['ncit:C171133', 'COVID-19 Infection']
		],
		equation_annotations: {
			'{\\dot{A}}(t)=\\zeta I(t)-(\\theta+\\mu+\\kappa)A(t)': ['A'],
			'\\delta(t)=-S(t)(a l(t)+\\theta D(t)+r A(t)+\\delta t(t)': ['A'],
			'{\\hat{T}}(t)=\\mu A(t)+\\nu R(t)-(\\sigma+\\tau){\\hat{T}}(t)': ['A']
		},
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'19': {
		type: 'variable',
		name: 'R',
		id: 'v4',
		text_annotations: [' Recognized (symptomatic infected, detected)'],
		dkg_annotations: [
			['ncit:C171133', 'COVID-19 Infection'],
			['ncit:C28554', 'Dead']
		],
		equation_annotations: { '{\\hat{T}}(t)=\\mu A(t)+\\nu R(t)-(\\sigma+\\tau){\\hat{T}}(t)': [] },
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'20': {
		type: 'variable',
		name: 'T',
		id: 'v5',
		text_annotations: [' Threatened (infected with life-threatening symptoms, detected)'],
		dkg_annotations: [
			['ido:0000511', 'infected population'],
			['ncit:C171133', 'COVID-19 Infection']
		],
		equation_annotations: { '{\\hat{T}}(t)=\\mu A(t)+\\nu R(t)-(\\sigma+\\tau){\\hat{T}}(t)': [] },
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'21': {
		type: 'variable',
		name: 'H',
		id: 'v6',
		text_annotations: [' Healed (recovered)'],
		dkg_annotations: [
			['ncit:C171133', 'COVID-19 Infection'],
			['ncit:C28554', 'Dead']
		],
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'22': {
		type: 'variable',
		name: 'E',
		id: 'v7',
		text_annotations: [' Extinct (dead)'],
		dkg_annotations: [
			['ido:0000511', 'infected population'],
			['ncit:C171133', 'COVID-19 Infection']
		],
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'0': {
		type: 'variable',
		name: '\u03b1',
		id: 'v8',
		text_annotations: [
			' Transmission rate (the probability of disease transmission in a single contact multiplied by the average number of contacts per person)'
		],
		dkg_annotations: [],
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'1': {
		type: 'variable',
		name: '\u03b2',
		id: 'v9',
		text_annotations: [
			' Transmission rate (the probability of disease transmission in a single contact multiplied by the average number of contacts per person)'
		],
		dkg_annotations: [
			['doid:0080928', 'dialysis-related amyloidosis'],
			['vo:0005114', '\u03b2-propiolactone-inactivated SARS-CoV vaccine']
		],
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'2': {
		type: 'variable',
		name: '\u03b3',
		id: 'v10',
		text_annotations: [
			' Transmission rate (the probability of disease transmission in a single contact multiplied by the average number of contacts per person)'
		],
		dkg_annotations: [
			['askemo:0000013', 'recovery rate'],
			['vo:0004915', 'vaccine specific interferon-\u03b3 immune response']
		],
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'3': {
		type: 'variable',
		name: '\u03b4',
		id: 'v11',
		text_annotations: [
			' Transmission rate (the probability of disease transmission in a single contact multiplied by the average number of contacts per person)\u03b5'
		],
		dkg_annotations: [
			['askemo:0000011', 'progression rate'],
			['vo:0005123', 'VSV\u0394G-MERS vaccine']
		],
		equation_annotations: {
			'\\delta(t)=-S(t)(a l(t)+\\theta D(t)+r A(t)+\\delta t(t)': ['\u03b4']
		},
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'4': { file: '41591_2020_Article_883.pdf', doi: 'https://doi.org/10.1038/s41591-020-0883-7' },
	'5': { file: '41591_2020_Article_883.pdf', doi: 'https://doi.org/10.1038/s41591-020-0883-7' },
	'6': {
		type: 'variable',
		name: '\u03b6',
		id: 'v13',
		text_annotations: [
			' probability rate at which an infected subject not aware of being infected develops clinically relevant symptoms'
		],
		dkg_annotations: [],
		equation_annotations: { '{\\dot{A}}(t)=\\zeta I(t)-(\\theta+\\mu+\\kappa)A(t)': ['\u03b6'] },
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'7': {
		type: 'variable',
		name: '\u03bb',
		id: 'v17',
		text_annotations: [' Rate of recovery for infected subjects'],
		dkg_annotations: [],
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'8': {
		type: 'variable',
		name: '\u03b7',
		id: 'v14',
		text_annotations: [
			' probability rate at which an infected subject aware of being infected develops clinically relevant symptoms\u03bc'
		],
		dkg_annotations: [],
		equation_annotations: { 'R(t)=\\eta D(t)+\\theta A(t)-(\\nu+\\xi)R(t)': ['\u03b7'] },
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'9': {
		type: 'variable',
		name: '\u03c1',
		id: 'v20',
		text_annotations: [' Rate of recovery for infected subjects'],
		dkg_annotations: [],
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'10': {
		type: 'variable',
		name: '\u03b8',
		id: 'v12',
		text_annotations: [' probability rate of detection relative to symptomatic cases'],
		dkg_annotations: [],
		equation_annotations: {
			'{\\dot{A}}(t)=\\zeta I(t)-(\\theta+\\mu+\\kappa)A(t)': ['\u03b8'],
			'H(t)=i I(t)+\\theta b(t)+\\kappa d(t)+i R(t)+\\sigma T(t)': ['\u03b8'],
			'R(t)=\\eta D(t)+\\theta A(t)-(\\nu+\\xi)R(t)': ['\u03b8'],
			'\\delta(t)=-S(t)(a l(t)+\\theta D(t)+r A(t)+\\delta t(t)': ['\u03b8']
		},
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'11': {
		type: 'variable',
		name: '\u03ba',
		id: 'v18',
		text_annotations: [' Rate of recovery for infected subjects'],
		dkg_annotations: [],
		equation_annotations: {
			'{\\dot{A}}(t)=\\zeta I(t)-(\\theta+\\mu+\\kappa)A(t)': ['\u03ba'],
			'H(t)=i I(t)+\\theta b(t)+\\kappa d(t)+i R(t)+\\sigma T(t)': ['\u03ba']
		},
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'12': {
		type: 'variable',
		name: '\u03bd',
		id: 'v15',
		text_annotations: [
			' Rate at which detected infected subjects develop life-threatening symptoms'
		],
		dkg_annotations: [],
		equation_annotations: {
			'R(t)=\\eta D(t)+\\theta A(t)-(\\nu+\\xi)R(t)': ['\u03bd'],
			'{\\hat{T}}(t)=\\mu A(t)+\\nu R(t)-(\\sigma+\\tau){\\hat{T}}(t)': ['\u03bd']
		},
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'13': {
		type: 'variable',
		name: '\u03be',
		id: 'v19',
		text_annotations: [' Rate of recovery for infected subjects'],
		dkg_annotations: [],
		equation_annotations: { 'R(t)=\\eta D(t)+\\theta A(t)-(\\nu+\\xi)R(t)': ['\u03be'] },
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'14': {
		type: 'variable',
		name: '\u03c3',
		id: 'v21',
		text_annotations: [' Rate of recovery for infected subjectsNone'],
		dkg_annotations: [],
		equation_annotations: {
			'H(t)=i I(t)+\\theta b(t)+\\kappa d(t)+i R(t)+\\sigma T(t)': ['\u03c3'],
			'{\\hat{T}}(t)=\\mu A(t)+\\nu R(t)-(\\sigma+\\tau){\\hat{T}}(t)': ['\u03c3']
		},
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	},
	'23': {
		type: 'variable',
		name: '\u03c4',
		id: 'v16',
		text_annotations: [' Mortality rate for infected subjects with life-threatening symptoms'],
		dkg_annotations: [],
		equation_annotations: {
			'{\\hat{T}}(t)=\\mu A(t)+\\nu R(t)-(\\sigma+\\tau){\\hat{T}}(t)': ['\u03c4']
		},
		file: '41591_2020_Article_883.pdf',
		doi: 'https://doi.org/10.1038/s41591-020-0883-7'
	}
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
// let source: any = null;
// let target: any = null;
let nameCounter: number = 0;

const runPetri = () => {
	const petri = graph2petri((renderer as any).graph as any);
	localStorage.setItem('sim-input-data', JSON.stringify(petri));
	router.push({ path: '/model-runner' });

	/*
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
	logger.info(`final ${final}`);
	logger.info(`final ${JSON.stringify(final)}`);
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
		width: 40,
		height: 40,
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

// If no edges at all, grid whatever data we have
const gridData = async () => {
	const nodeLayoutSpacing = 140;
	// FIXME: Hackathon
	if (renderer?.graph.edges.length === 0) {
		let c = 0;
		renderer?.graph.nodes.forEach((n) => {
			n.x = 60 + nodeLayoutSpacing * Math.round(c / 5);
			n.y = 80 + nodeLayoutSpacing * (c % 5);
			c++;
		});
		await renderer.render();
	}
};

const onDownload = () => {
	const petri = graph2petri((renderer as any).graph as any);

	const pom = document.createElement('a');
	pom.setAttribute(
		'href',
		`data:text/plain;charset=utf-8,${encodeURIComponent(JSON.stringify(petri, null, 2))}`
	);
	pom.setAttribute('download', 'petri.json');

	if (document.createEvent) {
		const event = document.createEvent('MouseEvents');
		event.initEvent('click', true, true);
		pom.dispatchEvent(event);
	} else {
		pom.click();
	}
	pom.remove();
};

// Entry point
onMounted(async () => {
	// Testing
	mathmlToPetri([
		'<math display="block" style="display:inline-block;"><mrow><mfrac><mrow><mi>d</mi><mi>S</mi></mrow><mrow><mi>d</mi><mi>t</mi></mrow></mfrac><mo>=</mo><mo>−</mo><mi>β</mi><mi>S</mi><mi>I</mi></mrow></math>',
		'<math display="block" style="display:inline-block;"><mrow><mfrac><mrow><mi>d</mi><mi>I</mi></mrow><mrow><mi>d</mi><mi>t</mi></mrow></mfrac><mo>=</mo><mi>β</mi><mi>S</mi><mi>I</mi><mo>−</mo><mi>γ</mi><mi>I</mi></mrow></math>',
		'<math display="block" style="display:inline-block;"><mrow><mfrac><mrow><mi>d</mi><mi>R</mi></mrow><mrow><mi>d</mi><mi>t</mi></mrow></mfrac><mo>=</mo><mi>γ</mi><mi>I</mi></mrow></math>'
	]).then((data) => {
		console.log('!!!!!!!!!!!!! MathML to Petri !!!!!!!!!!!!!!!!!!!');
		console.log(data);
		console.log('!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!');
		console.log('');
	});

	petriToLatex({
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
	}).then((data) => {
		console.log('!!!!!!!!!!!!! Petri to Latex !!!!!!!!!!!!!!!!!!!');
		console.log(data);
		console.log('!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!');
		console.log('');
	});

	const playground = document.getElementById('playground') as HTMLDivElement;
	renderer = new PetrinetRenderer({
		el: playground,
		useAStarRouting: false, // People get distracted with squiggly connectors - Jan 2023
		runLayout: runDagreLayout,
		useStableZoomPan: true,
		dragSelector: 'no-drag'
	});

	renderer.on('background-click', () => {
		// source = null;
		// target = null;
		renderer?.render();
		currentNodeMetadata.value = null;
	});

	renderer.on('node-mouse-enter', (_evtName, _evt, el) => {
		const metadataObj = TEST_META[el.datum().data.uid];
		// el.append('text').attr('x', 50).attr('y', 15).attr('popup', true).text(metadataObj);
		currentNodeMetadata.value = metadataObj;
	});

	renderer.on('add-edge', (_evtName, _evt, _selection, d) => {
		renderer?.addEdge(d.source, d.target);
	});

	document.addEventListener('keyup', async (event) => {
		// if (event.key === 'Backspace' && renderer) {
		// 	if (source) {
		// 		_.remove(
		// 			renderer.graph.edges,
		// 			(e) => e.source === source.datum().id || e.target === source.datum().id
		// 		);
		// 	}
		// 	_.remove(renderer.graph.nodes, (n) => n.id === source.datum().id);
		// 	variablesRef.value = variablesRef.value.filter((v) => v.id !== source.datum().id);
		// 	renderer.render();
		// 	source = null;
		// 	// target = null;
		// }

		if (event.key === 'Backspace' && renderer) {
			if (renderer.nodeSelection) {
				const nodeData = renderer.nodeSelection.datum();
				_.remove(renderer.graph.edges, (e) => e.source === nodeData.id || e.target === nodeData.id);
				_.remove(renderer.graph.nodes, (n) => n.id === nodeData.id);
				variablesRef.value = variablesRef.value.filter((v) => v.id !== nodeData.id);
				renderer.render();
			}

			if (renderer.edgeSelection) {
				const edgeData = renderer.edgeSelection.datum();
				_.remove(
					renderer.graph.edges,
					(e) => e.source === edgeData.source || e.target === edgeData.target
				);
				renderer.render();
			}
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
			gridData();
		}
	});

	const g = parsePetriNet2IGraph(SIRD);
	await renderer.setData(g);
	await renderer.render();

	gridData();

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
section {
	display: flex;
	flex-direction: column;
}

#playground {
	width: 800px;
	height: 350px;
	border: 1px solid #bbb;
}
</style>
