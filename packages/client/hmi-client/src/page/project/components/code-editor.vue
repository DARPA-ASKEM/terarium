<template>
	<div class="code">
		<div class="controls">
			<Button
				label="Extract Model"
				:class="`p-button ${selectedText.length === 0 ? 'p-disabled' : ''}`"
				@click="onExtractModel"
			/>
			<FileUpload
				name="demo[]"
				:customUpload="true"
				@uploader="onFileOpen"
				mode="basic"
				auto
				chooseLabel="Load File"
			/>
			<Button
				label="Extract petri net"
				:class="`p-button ${selectedText.length === 0 ? 'p-disabled' : ''}`"
				@click="onExtractGraph"
				:loading="extractPetrinetLoading"
			></Button>
		</div>
		<v-ace-editor
			v-model:value="code"
			@init="initialize"
			lang="python"
			theme="chrome"
			style="height: 100%; width: 100%"
		/>
	</div>
	<Dialog
		v-model:visible="codeExtractionDialogVisible"
		modal
		header="Confirm extraction"
		:style="{ width: '50vw' }"
	>
		<div ref="graphElement" class="graph-element" />
		<h6>
			Terarium can extract metadata about this model from related papers. Select the papers you
			would like to use.
		</h6>
		<DataTable v-model:selection="selectedPaper" :value="paperList" dataKey="id">
			<Column selectionMode="multiple"></Column>
			<Column field="title" header="Title"></Column>
			<Column field="authors" header="Authors"></Column>
			<Column field="year" header="Year"></Column>
		</DataTable>
		<template #footer>
			<Button label="Cancel" @click="codeExtractionDialogVisible = false" text />
			<Button label="Create model" @click="createModelFromCode()" :loading="createModelLoading" />
		</template>
	</Dialog>
</template>

<script setup lang="ts">
import { VAceEditor } from 'vue3-ace-editor';
import FileUpload from 'primevue/fileupload';
import Button from 'primevue/button';
import '@node_modules/ace-builds/src-noconflict/mode-python';
import '@node_modules/ace-builds/src-noconflict/theme-chrome';
import { ref, watch } from 'vue';
import { logger } from '@/utils/logger';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import API from '@/api/api';
import Dialog from 'primevue/dialog';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';

import { runDagreLayout } from '@/services/graph';
import { PetrinetRenderer } from '@/petrinet/petrinet-renderer';
import { parsePetriNet2IGraph, PetriNet, NodeData, EdgeData } from '@/petrinet/petrinet-service';
import { IGraph } from '@graph-scaffolder/index';
import { createModel } from '@/services/model';
import { Model } from '@/types/Model';

const props = defineProps({
	initialCode: {
		type: String,
		default: '# Paste some python code here or import from the controls above'
	}
});

const code = ref(props.initialCode);
const editor = ref<VAceEditorInstance['_editor'] | null>(null);
const selectedText = ref('');
const codeExtractionDialogVisible = ref(false);
const acset = ref<PetriNet | null>(null);
const graphElement = ref<HTMLDivElement | null>(null);
// Render graph whenever a new model is fetched or whenever the HTML element
//	that we render the graph to changes.
watch([graphElement], async () => {
	if (acset.value === null || graphElement.value === null) return;
	// Convert petri net into a graph
	const g: IGraph<NodeData, EdgeData> = parsePetriNet2IGraph(acset.value);
	// Create renderer
	const renderer = new PetrinetRenderer({
		el: graphElement.value as HTMLDivElement,
		useAStarRouting: true,
		runLayout: runDagreLayout,
		dragSelector: 'no-drag'
	});

	// Render graph
	await renderer?.setData(g);
	await renderer?.render();
});
const emit = defineEmits(['on-model-created']);
const paperList = ref([
	{
		id: '1',
		title: 'Paper name paper name',
		authors: 'Author name author name',
		year: '2023'
	}
]);
const selectedPaper = ref();
const createModelLoading = ref(false);
const extractPetrinetLoading = ref(false);
// @ts-ignore
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const mockAcset = {
	S: [
		{ sname: 'S', uid: 1 },
		{ sname: 'I', uid: 2 },
		{ sname: 'D', uid: 3 },
		{ sname: 'A', uid: 4 },
		{ sname: 'R', uid: 5 },
		{ sname: 'T', uid: 6 },
		{ sname: 'H', uid: 7 },
		{ sname: 'E', uid: 8 }
	],
	T: [
		{ tname: 'alpha', uid: 10 },
		{ tname: ' beta', uid: 11 },
		{ tname: ' gamma', uid: 12 },
		{ tname: ' delta', uid: 13 },
		{ tname: ' epsilon', uid: 14 },
		{ tname: ' mu', uid: 15 },
		{ tname: ' zeta', uid: 16 },
		{ tname: ' lamda', uid: 17 },
		{ tname: ' eta', uid: 18 },
		{ tname: ' rho', uid: 19 },
		{ tname: ' theta', uid: 20 },
		{ tname: ' kappa', uid: 21 },
		{ tname: ' nu', uid: 22 },
		{ tname: ' xi', uid: 23 },
		{ tname: ' sigma', uid: 24 },
		{ tname: ' tau', uid: 25 }
	],
	I: [
		{ it: 1, is: 1 },
		{ it: 2, is: 2 },
		{ it: 3, is: 2 },
		{ it: 4, is: 2 },
		{ it: 5, is: 3 },
		{ it: 6, is: 4 },
		{ it: 7, is: 4 },
		{ it: 8, is: 5 },
		{ it: 9, is: 6 },
		{ it: 10, is: 2 },
		{ it: 11, is: 3 },
		{ it: 12, is: 4 },
		{ it: 13, is: 5 },
		{ it: 14, is: 6 }
	],
	O: [
		{ ot: 1, os: 2 },
		{ ot: 2, os: 3 },
		{ ot: 3, os: 4 },
		{ ot: 4, os: 5 },
		{ ot: 5, os: 8 },
		{ ot: 6, os: 5 },
		{ ot: 7, os: 6 },
		{ ot: 8, os: 7 },
		{ ot: 9, os: 7 },
		{ ot: 10, os: 7 },
		{ ot: 11, os: 7 },
		{ ot: 12, os: 7 },
		{ ot: 13, os: 7 },
		{ ot: 14, os: 8 }
	]
};
// @ts-ignore
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const mockPaperText =
	'Graphical scheme representing the interactions among different stages of infection in the mathematical model SIDARTHE: S,' +
	'susceptible (uninfected); I, infected (asymptomatic or pauci-symptomatic infected, undetected); D, diagnosed (asymptomatic infected, detected); A,' +
	'ailing (symptomatic infected, undetected); R, recognized (symptomatic infected, detected); T, threatened (infected with life-threatening symptoms,' +
	'detected); H, healed (recovered); E, extinct (dead).' +
	'α, β, γ and δ respectively denote the transmission rate (the probability of' +
	'disease transmission in a single contact multiplied by the average number' +
	'of contacts per person) due to contacts between a susceptible subject and an' +
	'infected, a diagnosed, an ailing or a recognized subject. Typically, α is larger' +
	'than γ (assuming that people tend to avoid contacts with subjects showing' +
	'symptoms, even though diagnosis has not been made yet), which in turn is' +
	'larger than β and δ (assuming that subjects who have been diagnosed are' +
	'properly isolated). Tese parameters can be modifed by social-distancing' +
	'policies (for example, closing schools, remote working, lockdown). Te risk' +
	'of contagion due to threatened subjects, treated in proper ICUs, is assumed' +
	'negligible.' +
	'ε and θ capture the probability rate of detection, relative to asymptomatic and' +
	'symptomatic cases, respectively. Tese parameters, also modifable, refect' +
	'the level of attention on the disease and the number of tests performed over' +
	'the population: they can be increased by enforcing a massive contact tracing' +
	'and testing campaign28. Note that θ is typically larger than ε, as a symptomatic' +
	'individual is more likely to be tested.' +
	'ζ and η denote the probability rate at which an infected subject, respectively' +
	'not aware and aware of being infected, develops clinically relevant symptoms,' +
	'and are comparable in the absence of specifc treatment. Tese parameters are' +
	'disease-dependent, but may be partially reduced by improved therapies and' +
	'acquisition of immunity against the virus.' +
	'µ and ν respectively denote the rate at which undetected and detected infected' +
	'subjects develop life-threatening symptoms; they are comparable if there is' +
	'no known specifc treatment that is efective against the disease, otherwise µ' +
	'may be larger. Conversely, ν may be larger because infected individuals with' +
	'more acute symptoms, who have a higher risk of worsening, are more likely to' +
	'have been diagnosed. Tese parameters can be reduced by means of improved' +
	'therapies and acquisition of immunity against the virus.' +
	'τ denotes the mortality rate (for infected subjects with life-threatening symptoms) and can be reduced by means of improved therapies.' +
	'λ, κ, ξ, ρ and σ denote the rate of recovery for the fve classes of infected subjects; they may difer signifcantly if an appropriate treatment for the disease' +
	'is known and adopted for diagnosed patients, but are probably comparable' +
	'otherwise. Tese parameters can be increased thanks to improved treatments' +
	'and acquisition of immunity against the virus';

const mockAnnotations = [
	{
		type: 'variable',
		name: 'S',
		id: 'v0',
		text_annotations: [' Susceptible (uninfected)'],
		dkg_annotations: [['']]
	},
	{
		type: 'variable',
		name: 'I',
		id: 'v1',
		text_annotations: [' Infected (asymptomatic or pauci-symptomatic infected, undetected)'],
		dkg_annotations: [['']]
	},
	{
		type: 'variable',
		name: 'D',
		id: 'v2',
		text_annotations: [' Diagnosed (asymptomatic infected, detected)'],
		dkg_annotations: [['']]
	},
	{
		type: 'variable',
		name: 'A',
		id: 'v3',
		text_annotations: [' Ailing (symptomatic infected, undetected)'],
		dkg_annotations: [['']]
	},
	{
		type: 'variable',
		name: 'R',
		id: 'v4',
		text_annotations: [' Recognized (symptomatic infected, detected)'],
		dkg_annotations: [['']]
	},
	{
		type: 'variable',
		name: 'T',
		id: 'v5',
		text_annotations: [' Threatened (infected with life-threatening symptoms, detected)'],
		dkg_annotations: [['']]
	},
	{
		type: 'variable',
		name: 'H',
		id: 'v6',
		text_annotations: [' Healed (recovered)'],
		dkg_annotations: [['']]
	},
	{
		type: 'variable',
		name: 'E',
		id: 'v7',
		text_annotations: [' Extinct (dead)'],
		dkg_annotations: [['']]
	},
	{
		type: 'variable',
		name: 'α',
		id: 'v8',
		text_annotations: [
			' Transmission rate (the probability of disease transmission in a single contact multiplied by the average number of contacts per person)'
		],
		dkg_annotations: []
	},
	{
		type: 'variable',
		name: 'β',
		id: 'v9',
		text_annotations: [
			' Transmission rate (the probability of disease transmission in a single contact multiplied by the average number of contacts per person)'
		],
		dkg_annotations: [
			['doid:0080928', 'dialysis-related amyloidosis'],
			['vo:0005114', 'β-propiolactone-inactivated SARS-CoV vaccine']
		]
	},
	{
		type: 'variable',
		name: 'γ',
		id: 'v10',
		text_annotations: [
			' Transmission rate (the probability of disease transmission in a single contact multiplied by the average number of contacts per person)'
		],
		dkg_annotations: [
			['askemo:0000013', 'recovery rate'],
			['vo:0004915', 'vaccine specific interferon-γ immune response']
		]
	},
	{
		type: 'variable',
		name: 'δ',
		id: 'v11',
		text_annotations: [
			' Transmission rate (the probability of disease transmission in a single contact multiplied by the average number of contacts per person)ε'
		],
		dkg_annotations: [
			['askemo:0000011', 'progression rate'],
			['vo:0005123', 'VSVΔG-MERS vaccine']
		]
	},
	{
		type: 'variable',
		name: 'θ',
		id: 'v12',
		text_annotations: [' probability rate of detection relative to symptomatic cases'],
		dkg_annotations: []
	},
	{
		type: 'variable',
		name: 'ζ',
		id: 'v13',
		text_annotations: [
			' probability rate at which an infected subject not aware of being infected develops clinically relevant symptoms'
		],
		dkg_annotations: []
	},
	{
		type: 'variable',
		name: 'η',
		id: 'v14',
		text_annotations: [
			' probability rate at which an infected subject aware of being infected develops clinically relevant symptomsμ'
		],
		dkg_annotations: []
	},
	{
		type: 'variable',
		name: 'ν',
		id: 'v15',
		text_annotations: [
			' Rate at which detected infected subjects develop life-threatening symptoms'
		],
		dkg_annotations: []
	},
	{
		type: 'variable',
		name: 'τ',
		id: 'v16',
		text_annotations: [' Mortality rate for infected subjects with life-threatening symptoms'],
		dkg_annotations: []
	},
	{
		type: 'variable',
		name: 'λ',
		id: 'v17',
		text_annotations: [' Rate of recovery for infected subjects'],
		dkg_annotations: []
	},
	{
		type: 'variable',
		name: 'κ',
		id: 'v18',
		text_annotations: [' Rate of recovery for infected subjects'],
		dkg_annotations: []
	},
	{
		type: 'variable',
		name: 'ξ',
		id: 'v19',
		text_annotations: [' Rate of recovery for infected subjects'],
		dkg_annotations: []
	},
	{
		type: 'variable',
		name: 'ρ',
		id: 'v20',
		text_annotations: [' Rate of recovery for infected subjects'],
		dkg_annotations: []
	},
	{
		type: 'variable',
		name: 'σ',
		id: 'v21',
		text_annotations: [' Rate of recovery for infected subjectsNone'],
		dkg_annotations: []
	}
];

/**
 * File open/add event handler.  Immediately render the contents of the file to the editor
 * content
 * @param event	the input event when a file is added
 */
async function onFileOpen(event) {
	const reader = new FileReader();
	reader.readAsText(event.files[0], 'UTF-8');
	reader.onload = (evt) => {
		code.value = evt?.target?.result?.toString() ?? props.initialCode;
	};
}

async function onExtractGraph() {
	extractPetrinetLoading.value = true;
	// const response = await API.post(`code/to_acset?code=${selectedText.value}`);
	extractPetrinetLoading.value = false;
	// acset.value = response.data;
	acset.value = mockAcset;
	codeExtractionDialogVisible.value = true;
}

/**
 * Send the selected contents of the editor to the backend for persistence and model extraction
 * via TA1
 */
async function onExtractModel() {
	logger.info(`Transforming: ${selectedText.value}`);
	const response = await API.post('/code', selectedText.value);
	// eslint-disable-next-line
	alert(JSON.stringify(response.data));
}

/**
 * Event handler for selected text change in the code editor
 */
function onSelectedTextChange() {
	selectedText.value = editor.value?.getSelectedText() ?? '';
}

/**
 * Editor initialization function
 * @param editorInstance	the Ace editor instance
 */
async function initialize(editorInstance) {
	editor.value = editorInstance;
	editorInstance.session.selection.on('changeSelection', onSelectedTextChange);
	editorInstance.setShowPrintMargin(false);
}

async function createModelFromCode() {
	if (selectedPaper.value) {
		createModelLoading.value = true;
		// const response = await API.post(`/code/annotation/find_text_vars?text=${mockPaperText}`);
		createModelLoading.value = false;
	}
	const modelName = 'New model';
	const newModel = {
		name: 'New model',
		framework: 'Petri Net',
		content: JSON.stringify({ ...acset.value, metadata: mockAnnotations })
	} as Model;
	const model = await createModel(newModel);
	if (model) {
		emit('on-model-created', model.id, modelName);
	}
}
</script>

<style>
.code {
	display: flex;
	flex-direction: column;
	flex: 1;
	padding-top: 1rem;
}

.controls {
	margin-left: 40px;
	margin-bottom: 10px;
	display: flex;
	gap: 10px;
}

.graph-element {
	flex: 1;
	height: 20rem;
	width: 100%;
	border: 1px solid var(--surface-border);
	overflow: hidden;
	border-radius: 0.25rem;
}

.p-dialog .p-dialog-content h6 {
	margin: 1rem 0 1rem 0;
}
</style>
