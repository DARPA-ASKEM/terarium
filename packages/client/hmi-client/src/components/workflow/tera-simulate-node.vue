<template>
	<section v-if="!showSpinner" class="result-container">
		<Button @click="runSimulate">Run</Button>
		<div class="chart-container">
			<SimulateChart
				v-for="index in openedWorkflowNodeStore.numCharts"
				:key="index"
				:run-results="runResults"
				:run-id-list="completedRunIdList"
				:chart-idx="index"
			/>
		</div>
		<Button
			class="add-chart"
			text
			@click="openedWorkflowNodeStore.appendChart"
			label="Add Chart"
			icon="pi pi-plus"
		></Button>
	</section>
	<section v-else>
		<div>loading...</div>
	</section>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import Button from 'primevue/button';
import { csvParse } from 'd3';
import { ModelConfiguration } from '@/types/Types';

import { makeForecastJob, getSimulation, getRunResult } from '@/services/models/simulation-service';
import { WorkflowNode } from '@/types/workflow';
import { RunResults } from '@/types/SimulateConfig';

import { useOpenedWorkflowNodeStore } from '@/stores/opened-workflow-node';
import { getModelConfigurationById } from '@/services/model-configurations';
import SimulateChart from './tera-simulate-chart.vue';
import { SimulateOperation } from './simulate-operation';

const props = defineProps<{
	node: WorkflowNode;
}>();
const emit = defineEmits(['append-output-port']);
const openedWorkflowNodeStore = useOpenedWorkflowNodeStore();

const showSpinner = ref(false);

const startedRunIdList = ref<string[]>([]);
const completedRunIdList = ref<string[]>([]);
const runResults = ref<RunResults>({});

const modelConfiguration = ref<ModelConfiguration | null>(null);
const modelConfigId = computed<string | undefined>(() => props.node.inputs[0].value?.[0]);

watch(
	() => props.node,
	(node) => openedWorkflowNodeStore.setNode(node ?? null),
	{ deep: true }
);

const runSimulate = async () => {
	const modelConfigurationList = props.node.inputs[0].value;
	if (!modelConfigurationList?.length) return;

	const simulationRequests = modelConfigurationList.map(async (configId: string) => {
		const payload = {
			modelConfigId: configId,
			timespan: { start: openedWorkflowNodeStore.tspan[0], end: openedWorkflowNodeStore.tspan[1] },
			extra: {
				// FIXME: need to use real value
				initials: {
					S: 100,
					I: 1,
					R: 0
				},
				params: {
					inf: 0.002,
					rec: 0.004
				}
			},
			engine: 'sciml'
		};
		const response = await makeForecastJob(payload);
		console.log(response.id, payload);
		return response.id;
	});

	startedRunIdList.value = await Promise.all(simulationRequests);
	getStatus();
	showSpinner.value = true;
};

// watch for changes in node input
// watch(
// 	() => props.node.inputs,
// 	async (inputList) => {
// 		const forecastOutputList = await Promise.all(
// 			inputList.map(({ value }) =>
// 				makeForecastJob({
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
	const requestList: any[] = [];
	startedRunIdList.value.forEach((id) => {
		requestList.push(getSimulation(id));
	});

	const currentSimulations = await Promise.all(requestList);
	const ongoingStatusList = ['running', 'queued'];

	if (currentSimulations.every(({ status }) => status === 'complete')) {
		completedRunIdList.value = startedRunIdList.value;
		showSpinner.value = false;
	} else if (currentSimulations.some(({ status }) => ongoingStatusList.includes(status))) {
		// recursively call until all runs retrieved
		setTimeout(getStatus, 3000);
	} else {
		// throw if there are any failed runs for now
		console.error('Failed', startedRunIdList.value);
		throw Error('Failed Runs');
	}
};

const watchCompletedRunList = async (runIdList: string[]) => {
	const newRunResults = {};
	await Promise.all(
		runIdList.map(async (runId) => {
			if (runResults.value[runId]) {
				newRunResults[runId] = runResults.value[runId];
			} else {
				const resultCsv = await getRunResult(runId, 'result.csv');
				const csvData = csvParse(resultCsv);
				newRunResults[runId] = csvData;
			}
		})
	);
	runResults.value = newRunResults;

	const port = props.node.inputs[0];
	emit('append-output-port', {
		type: SimulateOperation.outputs[0].type,
		label: `${port.label} Results`,
		value: {
			runIdList
		}
	});

	/* commented out, causing serialization issues. DC June 22
	const port = props.node.inputs[0];
	emit('append-output-port', {
		type: SimulateOperation.outputs[0].type,
		label: `${port.label} Results`,
		value: {
			runResults: runResults.value,
			runIdList,
			runConfigs: port.value
		}
	});
	*/
};

watch(
	() => modelConfigId.value,
	async () => {
		if (modelConfigId.value) {
			modelConfiguration.value = await getModelConfigurationById(modelConfigId.value);
		}
	}
);

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

.add-chart {
	width: 9em;
}
</style>
