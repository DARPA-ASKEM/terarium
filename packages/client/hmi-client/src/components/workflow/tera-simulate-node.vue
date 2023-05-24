<template>
	<section v-if="!showSpinner" class="result-container">
		<Button @click="runSimulate()">Run</Button>
		<div class="multiselect-title">Select variables to plot</div>
		<MultiSelect
			v-model="selectedVariable"
			:options="stateVariablesList"
			optionLabel="code"
			placeholder="Select a State Variable"
		>
			<template v-slot:value>
				<span
					class="selected-label-item"
					v-for="variable in selectedVariable"
					:key="variable.code"
					:style="{ color: getVariableColor(variable.code) }"
				>
					{{ variable.code }}
				</span>
			</template>
		</MultiSelect>
		<Chart type="line" :data="chartData" :options="chartOptions" />
	</section>
	<section v-else>
		<div>loading...</div>
	</section>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import Button from 'primevue/button';
import { csvParse } from 'd3';
import { shimPetriModel } from '@/services/models/petri-shim';

import MultiSelect from 'primevue/multiselect';
import Chart from 'primevue/chart';

import { makeForecast, getRunStatus, getRunResult } from '@/services/models/simulation-service';
import { WorkflowNode } from '@/types/workflow';

type DatasetType = {
	data: number[];
	label: string;
	fill: boolean;
	tension: number;
};

const VIRIDIS_14 = [
	'#fde725',
	'#cde11d',
	'#98d83e',
	'#67cc5c',
	'#40bd72',
	'#25ac82',
	'#1f998a',
	'#24878e',
	'#2b748e',
	'#34618d',
	'#3d4d8a',
	'#453581',
	'#481c6e',
	'#440154'
];

const props = defineProps<{
	node: WorkflowNode;
}>();

const showSpinner = ref(false);

const startedRunIdList = ref<number[]>([]);
const completedRunIdList = ref<number[]>([]);
let runResults = {};

// data for rendering ui
let stateVariablesList: { code: string }[] = [];
const selectedVariable = ref<{ code: string }[]>([]);
let runList = [] as any[];
const selectedRun = ref<null | { code: string }>(null);

const chartData = ref({});
const chartOptions = {
	devicePixelRatio: 4,
	maintainAspectRatio: false,
	pointStyle: false,
	plugins: {
		legend: {
			display: false
		}
	},
	scales: {
		x: {
			ticks: {
				color: '#aaa',
				maxTicksLimit: 5,
				includeBounds: true
			},
			grid: {
				color: '#fff',
				borderColor: '#fff'
			}
		},
		y: {
			ticks: {
				color: '#aaa',
				maxTicksLimit: 3,
				includeBounds: true
			},
			grid: {
				color: '#fff',
				borderColor: '#fff'
			}
		}
	}
};

const runSimulate = async () => {
	const port = props.node.inputs[0];
	if (port && port.value) {
		const payload = {
			model: shimPetriModel(port.value.model),
			initials: port.value.initialValues,
			params: port.value.parameterValues,
			tspan: [0, 100]
		};

		const response = await makeForecast(payload);
		startedRunIdList.value = [response.id];

		// start polling for run status
		getStatus();
		showSpinner.value = true;
	}
};

// watch for changes in node input
// watch(
// 	() => props.node.inputs,
// 	async (inputList) => {
// 		const forecastOutputList = await Promise.all(
// 			inputList.map(({ value }) =>
// 				makeForecast({
// 					model: value.model.id,
// 					initials: value.initialValues,
// 					params: value.parameterValues,
// 					tspan: [0.0, 90.0] // hardcoded timespan
// 				})
// 			)
// 		);
// 		startedRunIdList.value = forecastOutputList.map((forecastOutput) => forecastOutput.id);
//
// 		// start polling for run status
// 		getStatus();
// 	},
// 	{ deep: true }
// );

// Retrieve run ids
// FIXME: Replace with API.poller
const getStatus = async () => {
	const currentRunStatus = await Promise.all(startedRunIdList.value.map(getRunStatus));

	if (currentRunStatus.every(({ status }) => status === 'done')) {
		completedRunIdList.value = startedRunIdList.value;
		showSpinner.value = false;
	} else if (currentRunStatus.some(({ status }) => status === 'queuing')) {
		// recursively call until all runs retrieved
		setTimeout(getStatus, 3000);
	} else {
		// throw if there are any failed runs for now
		console.error('Failed', startedRunIdList.value);
		throw Error('Failed Runs');
	}
};

const watchCompletedRunList = async (runIdList: number[]) => {
	const newRunResults = {};
	await Promise.all(
		runIdList.map(async (runId) => {
			if (runResults[runId]) {
				newRunResults[runId] = runResults[runId];
			} else {
				const resultCsv = await getRunResult(runId);
				newRunResults[runId] = csvParse(resultCsv);
			}
		})
	);
	runResults = newRunResults;
	// process data retrieved

	// assume that the state variables for all runs will be identical
	// take first run and parse it for state variables
	if (!stateVariablesList.length) {
		stateVariablesList = Object.keys(runResults[Object.keys(runResults)[0]][0])
			.filter((key) => key !== 'timestep')
			.map((key) => ({ code: key }));
	}
	selectedVariable.value = [stateVariablesList[0]];
	runList = runIdList.map((runId, index) => ({ code: runId, index }));
	selectedRun.value = runList[0];
};
watch(() => completedRunIdList.value, watchCompletedRunList);

const getVariableColor = (variableCode: string) => {
	const codeIdx = selectedVariable.value.findIndex(({ code }) => code === variableCode);
	return VIRIDIS_14[Math.floor((codeIdx / selectedVariable.value.length) * VIRIDIS_14.length)];
};

const renderGraph = (params) => {
	const datasets: DatasetType[] = [];
	params[0].forEach(({ code }) =>
		completedRunIdList.value
			.map((runId) => runResults[runId])
			.forEach((run, runIdx) => {
				const dataset = {
					data: run.map(
						(datum: { [key: string]: number }) => datum[code] // - runResults[selectedRun.value.code][timeIdx][code]
					),
					label: `${completedRunIdList.value[runIdx]} - ${code}`,
					fill: false,
					tension: 0.4,
					borderColor: getVariableColor(code)
				};
				datasets.push(dataset);
			})
	);
	chartData.value = {
		labels: runResults[Object.keys(runResults)[0]].map((datum) => Number(datum.timestep)),
		datasets
	};
};
watch(() => [selectedVariable.value, selectedRun.value], renderGraph);
</script>

<style scoped>
section {
	display: flex;
	flex-direction: column;
	width: 100%;
	padding: 10px;
	background: var(--surface-overlay);
}

.multiselect-title {
	font-size: smaller;
	font-weight: 700;
}

.selected-label-item::after {
	color: black;
	content: ', ';
}
.selected-label-item:last-child::after {
	content: '';
}

.p-chart {
	width: 100%;
	height: 200px;
	margin-top: 0.5em;
}

.p-multiselect {
	width: 100%;
	border-color: lightgrey;
}
</style>
