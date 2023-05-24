<template>
	<section v-if="!showSpinner" class="result-container">
		<Button @click="runSimulate()">Run</Button>
		<div class="chart-container">
			<SimulateChart
				v-for="index in numCharts"
				:key="index"
				:run-results="runResults"
				:run-id-list="completedRunIdList"
			/>
		</div>
		<Button text @click="numCharts++" label="Add Chart" icon="pi pi-plus"></Button>
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

import { makeForecast, getRunStatus, getRunResult } from '@/services/models/simulation-service';
import { WorkflowNode } from '@/types/workflow';

import SimulateChart from './tera-simulate-chart.vue';

const props = defineProps<{
	node: WorkflowNode;
}>();

const showSpinner = ref(false);
const numCharts = ref(1);

const startedRunIdList = ref<number[]>([]);
const completedRunIdList = ref<number[]>([]);
let runResults = {};

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
};
watch(() => completedRunIdList.value, watchCompletedRunList);
</script>

<style scoped>
section {
	display: flex;
	flex-direction: column;
	width: 100%;
	padding: 10px;
	background: var(--surface-overlay);
}

.simulate-chart {
	margin: 1em 0em;
}
</style>
