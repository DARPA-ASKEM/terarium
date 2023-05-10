<template>
	<section class="result-container">
		<Button @click="runSimulate()">Run</Button>
		<div class="options">
			<div class="dropdown-group">
				<span>Select variables to plot</span>
				<MultiSelect
					v-model="selectedVariable"
					:options="stateVariablesList"
					optionLabel="code"
					placeholder="Select a State Variable"
				/>
			</div>
		</div>
		<div class="result">
			<Chart type="line" :data="chartData" :options="chartOptions" />
		</div>
	</section>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { PetriNet } from '@/petrinet/petrinet-service';
import { ITypedModel } from '@/types/Model';
import Button from 'primevue/button';
import { csvParse } from 'd3';

import MultiSelect from 'primevue/multiselect';
import Chart from 'primevue/chart';

import { makeForecast, getRunStatus, getRunResult } from '@/services/models/simulation-service';
import { WorkflowNode } from '@/types/workflow';

const props = defineProps<{
	node: WorkflowNode;
}>();

const startedRunIdList = ref([] as number[]);
const completedRunIdList = ref([] as number[]);
let runResults = {};

// data for rendering ui
let stateVariablesList = [] as any[];
const selectedVariable = ref<{ code: string }[]>([]);
let runList = [] as any[];
const selectedRun = ref<null | { code: string }>(null);

const chartData = ref({});
const chartOptions = {
	maintainAspectRatio: false,
	pointStyle: false,
	plugins: {
		legend: {
			labels: {
				color: '#000'
			}
		}
	},
	scales: {
		x: {
			ticks: {
				color: '#000'
			},
			grid: {
				color: '#AAA'
			}
		},
		y: {
			ticks: {
				color: '#000'
			},
			grid: {
				color: '#AAA'
			}
		}
	}
};

// FIXME: adapt to new model representation
// FIXME: adapt to new simulation-service id-based API
const scrubModel = (model: ITypedModel<PetriNet>) => {
	const cleanedModel: PetriNet = {
		S: [],
		T: [],
		I: [],
		O: []
	};
	if (model) {
		cleanedModel.S = model.content.S.map((s) => ({ sname: s.sname }));
		cleanedModel.T = model.content.T.map((t) => ({ tname: t.tname }));
		cleanedModel.I = model.content.I;
		cleanedModel.O = model.content.O;
	}
	return JSON.stringify(cleanedModel);
};

const runSimulate = async () => {
	const port = props.node.inputs[0];
	if (port && port.value) {
		const payload = {
			model: scrubModel(port.value.model),
			initials: port.value.initialValues,
			params: port.value.parameterValues,
			tspan: [0, 100]
		};

		const response = await makeForecast(payload);
		startedRunIdList.value = [response.id];

		// start polling for run status
		getStatus();
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
	} else if (currentRunStatus.some(({ status }) => status === 'queuing')) {
		// recursively call until all runs retrieved
		setTimeout(getStatus, 3000);
	} else {
		// throw if there are any failed runs for now
		console.error('Failed', startedRunIdList.value);
		throw Error('Failed Runs');
	}
};

watch(
	() => completedRunIdList.value,
	async (runIdList) => {
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
		stateVariablesList = Object.keys(runResults[Object.keys(runResults)[0]][0])
			.filter((key) => key !== 'timestep')
			.map((key) => ({ code: key }));
		selectedVariable.value = [stateVariablesList[0]];
		runList = runIdList.map((runId, index) => ({ code: runId, index }));
		selectedRun.value = runList[0];
	}
);

const renderGraph = ([selectedVarList]) => {
	const datasets: any[] = [];
	selectedVarList.forEach(({ code }) =>
		completedRunIdList.value
			.map((runId) => runResults[runId])
			.forEach((run, runIdx) => {
				const dataset = {
					data: run.map(
						(datum: any) => datum[code] // - runResults[selectedRun.value.code][timeIdx][code]
					),
					label: `${completedRunIdList.value[runIdx]} - ${code}`,
					fill: false,
					tension: 0.4
				};
				datasets.push(dataset);
			})
	);
	chartData.value = {
		labels: runResults[Object.keys(runResults)[0]].map((datum) => Number(datum.timestep)),
		datasets
	};
};
watch(() => [selectedVariable.value, selectedRun.value] as any, renderGraph);
</script>

<style scoped>
.result-container {
	display: flex;
	flex-direction: column;
	width: 100%;
	padding: 10px;
	background: var(--surface-overlay);
}

.result {
	width: 100%;
	height: 100%;
	display: flex;
	flex-direction: row;
}

.p-chart {
	width: 100%;
}

.options {
	display: flex;
	margin-bottom: 10px;
}

.dropdown-group {
	flex-grow: 1;
	flex-basis: 0%;
}

.p-multiselect {
	width: 50%;
}
</style>
