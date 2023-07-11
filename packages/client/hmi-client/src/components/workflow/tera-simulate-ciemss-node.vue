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
			<Button size="small" label="Run" icon="pi pi-play"></Button>
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
import { csvParse } from 'd3';
// import { ModelConfiguration } from '@/types/Types';

// import { makeForecastJob, getSimulation, getRunResult } from '@/services/models/simulation-service';
import { WorkflowNode } from '@/types/workflow';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';
import { workflowEventBus } from '@/services/workflow';

import SimulateChart from './tera-simulate-chart.vue';
import { SimulateCiemssOperation, SimulateOperationState } from './simulate-ciemss-operation';

import SimulateProbabilisticData from './simulate-prob-data';

const props = defineProps<{
	node: WorkflowNode;
}>();
const emit = defineEmits(['append-output-port']);
// const openedWorkflowNodeStore = useOpenedWorkflowNodeStore();

const showSpinner = ref(false);

// const startedRunIdList = ref<string[]>([]);
const completedRunIdList = ref<string[]>([]);
const runResults = ref<RunResults>({});
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
	output.push(2);
	return output;
});

watch(
	() => runResults.value,
	(input) => {
		const runResult = JSON.parse(JSON.stringify(input));
		// renderedRuns.value = runResults.value;

		// convert to array from array-like object
		const parsedSimProbData = Object.values(runResult);

		const numRuns = parsedSimProbData.length;
		if (!numRuns) {
			renderedRuns.value = runResult;
			return;
		}

		const numTimestamps = (parsedSimProbData[0] as any[]).length;
		const aggregateRun: any = [];

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

onMounted(async () => {
	const parsedSimProbData = csvParse(SimulateProbabilisticData);

	// populate completedRunIdList
	completedRunIdList.value = new Array(
		Number(parsedSimProbData[parsedSimProbData.length - 1].sample_id) + 1
	)
		.fill('0')
		.map((_x, i) => i.toString());

	// initialize runResults
	for (let i = 0; i < completedRunIdList.value.length; i++) {
		runResults.value[i.toString()] = [];
	}

	// populate runResults
	parsedSimProbData.forEach((inputRow) => {
		const outputRow = { timestamp: inputRow.timepoint_id };
		Object.keys(inputRow).forEach((key) => {
			if (key.includes('_sol')) {
				outputRow[key.replace('_sol', '')] = inputRow[key];
			}
		});
		runResults.value[inputRow.sample_id as string].push(outputRow as any);
	});

	const port = props.node.inputs[0];
	emit('append-output-port', {
		type: SimulateCiemssOperation.outputs[0].type,
		label: `${port.label} Results`,
		value: completedRunIdList.value
	});

	// console.log(completedRunIdList.value);
	// console.log(runResults.value);
	// const node = props.node;
	// if (!node) return;

	// const port = node.outputs[0];
	// if (!port) return;

	// const runIdList = (port.value as any)[0].runIdList as string[];
	// await Promise.all(
	// 	runIdList.map(async (runId) => {
	// 		const resultCsv = await getRunResult(runId, 'result.csv');
	// 		const csvData = csvParse(resultCsv);
	// 		runResults.value[runId] = csvData as any;
	// 	})
	// );
});

const configurationChange = (index: number, config: ChartConfig) => {
	const state: SimulateOperationState = _.cloneDeep(props.node.state);
	state.chartConfigs[index] = config;

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

const addChart = () => {
	const state: SimulateOperationState = _.cloneDeep(props.node.state);
	state.chartConfigs.push(_.last(state.chartConfigs) as ChartConfig);

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};
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
