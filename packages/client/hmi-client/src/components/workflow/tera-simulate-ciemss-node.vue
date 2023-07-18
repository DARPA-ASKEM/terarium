<template>
	<section v-if="!showSpinner" class="result-container">
		<div class="chart-container">
			<SimulateChart
				v-for="(cfg, index) of node.state.chartConfigs"
				:key="index"
				:run-results="renderedRuns"
				:chartConfig="cfg"
				:line-color-array="lineColorArray"
				:line-width-array="lineWidthArray"
				@configuration-change="configurationChange(index, $event)"
			/>
		</div>
		<div class="button-container">
			<Button
				class="add-chart"
				size="small"
				text
				@click="addChart"
				label="Add Chart"
				icon="pi pi-plus"
			></Button>
			<Button size="small" label="Run" @click="runSimulate" icon="pi pi-play"></Button>
		</div>
	</section>
	<section v-else>
		<div>loading...</div>
	</section>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref, watch, computed, onMounted } from 'vue';
import Button from 'primevue/button';

import {
	makeForecastJobCiemss as makeForecastJob,
	getSimulation,
	getRunResultCiemss
} from '@/services/models/simulation-service';
import { WorkflowNode } from '@/types/workflow';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';
import { workflowEventBus } from '@/services/workflow';

import SimulateChart from './tera-simulate-chart.vue';
import { SimulateCiemssOperation, SimulateCiemssOperationState } from './simulate-ciemss-operation';

const props = defineProps<{
	node: WorkflowNode;
}>();
const emit = defineEmits(['append-output-port']);
// const openedWorkflowNodeStore = useOpenedWorkflowNodeStore();

const showSpinner = ref(false);

const startedRunIdList = ref<string[]>([]);
const completedRunIdList = ref<string[]>([]);
const runResults = ref<RunResults>({});
const runConfigs = ref<{ [paramKey: string]: number[] }>({});
const renderedRuns = ref<RunResults>({});

const lineColorArray = computed(() => {
	const output = Array(Math.max(Object.keys(runResults.value).length ?? 0 - 1, 0)).fill(
		'#00000020'
	);
	output.push('#1b8073');
	return output;
});

const lineWidthArray = computed(() => {
	const output = Array(Math.max(Object.keys(runResults.value).length ?? 0 - 1, 0)).fill(1);
	output.push(5);
	return output;
});

const runSimulate = async () => {
	const modelConfigurationList = props.node.inputs[0].value;
	if (!modelConfigurationList?.length) return;

	const state = props.node.state as SimulateCiemssOperationState;

	const simulationRequests = modelConfigurationList.map(async (configId: string) => {
		const payload = {
			modelConfigId: configId,
			timespan: {
				start: state.currentTimespan.start,
				end: state.currentTimespan.end
			},
			extra: { num_samples: state.numSamples },
			engine: 'ciemss'
		};
		const response = await makeForecastJob(payload);
		return response.id;
	});

	startedRunIdList.value = await Promise.all(simulationRequests);
	getStatus();
	showSpinner.value = true;
};

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

// assume only one run for now
const watchCompletedRunList = async (runIdList: string[]) => {
	if (runIdList.length === 0) return;

	const output = await getRunResultCiemss(runIdList[0]);
	runResults.value = output.runResults;
	runConfigs.value = output.runConfigs;

	const port = props.node.inputs[0];
	emit('append-output-port', {
		type: SimulateCiemssOperation.outputs[0].type,
		label: `${port.label} Results`,
		value: runIdList
	});
};
watch(() => completedRunIdList.value, watchCompletedRunList, { immediate: true });

watch(
	() => runResults.value,
	(input) => {
		const runResult: RunResults = JSON.parse(JSON.stringify(input));

		// convert to array from array-like object
		const parsedSimProbData = Object.values(runResult);

		const numRuns = parsedSimProbData.length;
		if (!numRuns) {
			renderedRuns.value = runResult;
			return;
		}

		const numTimestamps = (parsedSimProbData as { [key: string]: number }[][])[0].length;
		const aggregateRun: { [key: string]: number }[] = [];

		for (let timestamp = 0; timestamp < numTimestamps; timestamp++) {
			for (let run = 0; run < numRuns; run++) {
				if (!aggregateRun[timestamp]) {
					aggregateRun[timestamp] = parsedSimProbData[run][timestamp];
					Object.keys(aggregateRun[timestamp]).forEach((key) => {
						aggregateRun[timestamp][key] = Number(aggregateRun[timestamp][key]) / numRuns;
					});
				} else {
					const datum = parsedSimProbData[run][timestamp];
					Object.keys(datum).forEach((key) => {
						aggregateRun[timestamp][key] += datum[key] / numRuns;
					});
				}
			}
		}

		renderedRuns.value = { ...runResult, [numRuns]: aggregateRun };
	},
	{ immediate: true, deep: true }
);

const configurationChange = (index: number, config: ChartConfig) => {
	const state: SimulateCiemssOperationState = _.cloneDeep(props.node.state);
	state.chartConfigs[index] = config;

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

const addChart = () => {
	const state: SimulateCiemssOperationState = _.cloneDeep(props.node.state);
	state.chartConfigs.push(_.last(state.chartConfigs) as ChartConfig);

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

onMounted(async () => {
	const port = props.node.outputs[0];
	if (!port) return;

	const runIdList = port.value as string[];
	const output = await getRunResultCiemss(runIdList[0]);
	runResults.value = output.runResults;
	runConfigs.value = output.runConfigs;
});
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
	margin: 1.5em 0em;
}

.add-chart {
	width: 9em;
}

.button-container {
	display: flex;
	justify-content: space-between;
}
</style>
