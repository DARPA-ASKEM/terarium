<template>
	<section v-if="!showSpinner" class="result-container">
		<Button @click="runSimulate">Run</Button>
		<div class="chart-container">
			<SimulateChart
				v-for="index in openedWorkflowNodeStore.numCharts"
				:key="index"
				:run-results="runResults"
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
import { ref, watch, computed, onMounted } from 'vue';
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

	const modelConfigurationObj = modelConfiguration.value as any;
	const semantics = modelConfigurationObj.amrConfiguration.semantics;
	const ode = semantics.ode;

	// FIXME: Dummy up the payload to make things work, but not correct results
	const initials = ode.initials.map((d) => d.target);
	const rates = ode.rates.map((d) => d.target);
	const initialsObj = {};
	const paramsObj = {};

	initials.forEach((d) => {
		initialsObj[d] = Math.random() * 100;
	});
	rates.forEach((d) => {
		paramsObj[d] = Math.random() * 0.05;
	});

	const simulationRequests = modelConfigurationList.map(async (configId: string) => {
		const payload = {
			modelConfigId: configId,
			timespan: { start: openedWorkflowNodeStore.tspan[0], end: openedWorkflowNodeStore.tspan[1] },
			extra: {
				initials: initialsObj,
				params: paramsObj
			},
			engine: 'sciml'
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

const watchCompletedRunList = async (runIdList: string[]) => {
	if (runIdList.length === 0) return;

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
};

watch(
	() => modelConfigId.value,
	async () => {
		if (modelConfigId.value) {
			modelConfiguration.value = await getModelConfigurationById(modelConfigId.value);
		}
	},
	{ immediate: true }
);

watch(() => completedRunIdList.value, watchCompletedRunList, { immediate: true });

onMounted(async () => {
	const node = props.node;
	if (!node) return;

	const port = node.outputs[0];
	if (!port) return;

	const runIdList = (port.value as any)[0].runIdList as string[];
	await Promise.all(
		runIdList.map(async (runId) => {
			const resultCsv = await getRunResult(runId, 'result.csv');
			const csvData = csvParse(resultCsv);
			runResults.value[runId] = csvData as any;
		})
	);
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
	margin: 1em 0em;
}

.add-chart {
	width: 9em;
}
</style>
