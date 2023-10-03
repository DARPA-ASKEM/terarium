<template>
	<section v-if="!showSpinner" class="result-container">
		<Button @click="runSimulate">Run</Button>
		<Dropdown :options="runList" v-model="selectedRun" option-label="label" />
		<div class="chart-container" v-if="runResults">
			<tera-simulate-chart
				v-if="selectedRun"
				:key="selectedRun.idx"
				:run-results="runResults"
				:chartConfig="node.state.chartConfigs[selectedRun.idx]"
				@configuration-change="configurationChange(selectedRun.idx, $event)"
				:colorByRun="true"
			/>
		</div>
		<Button class="add-chart" text @click="addChart" label="Add chart" icon="pi pi-plus"></Button>
	</section>
	<section v-else>
		<tera-progress-bar :value="progress.value" :status="progress.status" />
	</section>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref, watch, computed, onMounted, onUnmounted } from 'vue';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import { csvParse } from 'd3';
import { ModelConfiguration, SimulationRequest } from '@/types/Types';

import {
	makeForecastJob,
	getRunResult,
	simulationPollAction,
	querySimulationInProgress
} from '@/services/models/simulation-service';
import { ProgressState, WorkflowNode } from '@/types/workflow';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';

import { getModelConfigurationById } from '@/services/model-configurations';
import { workflowEventBus } from '@/services/workflow';
import { Poller, PollerState } from '@/api/api';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
import TeraProgressBar from '@/workflow/tera-progress-bar.vue';
import { logger } from '@/utils/logger';
import { SimulateJuliaOperation, SimulateJuliaOperationState } from './simulate-julia-operation';

const props = defineProps<{
	node: WorkflowNode<SimulateJuliaOperationState>;
}>();
const emit = defineEmits(['append-output-port', 'update-state']);

const showSpinner = ref(false);
const completedRunIdList = ref<string[]>([]);
const runResults = ref<RunResults>({});

const modelConfiguration = ref<ModelConfiguration | null>(null);
const modelConfigId = computed<string | undefined>(() => props.node.inputs[0].value?.[0]);
const progress = ref({ status: ProgressState.RETRIEVING, value: 0 });

const runList = computed(() =>
	props.node.state.chartConfigs.map((cfg: ChartConfig, idx: number) => ({
		label: `Output ${idx + 1} - ${cfg.selectedRun}`,
		idx
	}))
);
const selectedRun = ref(runList.value.length > 0 ? runList.value[0] : undefined);

const poller = new Poller();

onMounted(() => {
	const runIds = querySimulationInProgress(props.node);
	if (runIds.length > 0) {
		getStatus(runIds);
	}
});

onUnmounted(() => {
	poller.stop();
});

const runSimulate = async () => {
	const modelConfigurationList = props.node.inputs[0].value;
	if (!modelConfigurationList?.length) return;

	// Since we've disabled multiple configs to a simulation node, we can assume only one config
	const configId = modelConfigurationList[0];

	const state = props.node.state;

	const payload: SimulationRequest = {
		modelConfigId: configId,
		timespan: {
			start: state.currentTimespan.start,
			end: state.currentTimespan.end
		},
		extra: {},
		engine: 'sciml'
	};
	const response = await makeForecastJob(payload);
	getStatus([response.id]);
};

const getStatus = async (runIds: string[]) => {
	showSpinner.value = true;
	poller
		.setInterval(3000)
		.setThreshold(300)
		.setPollAction(async () => simulationPollAction(runIds, props.node, progress, emit));
	const pollerResults = await poller.start();

	if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		// throw if there are any failed runs for now
		showSpinner.value = false;
		logger.error(`Simulate: ${runIds} has failed`, {
			toastTitle: 'Error - Julia'
		});
		throw Error('Failed Runs');
	}
	completedRunIdList.value = runIds;
	showSpinner.value = false;
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

	console.log(runResults.value);

	// TODO: outport ports should only show the port for the run selected from the dropdown
	const port = props.node.inputs[0];
	emit('append-output-port', {
		type: SimulateJuliaOperation.outputs[0].type,
		label: `${port.label} Results`,
		value: runIdList
	});
	// show the latest run in the dropdown
	selectedRun.value = runList.value[runList.value.length - 1];
};

const configurationChange = (index: number, config: ChartConfig) => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs[index] = config;

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
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

	console.log('PORT', port);

	const runIdList = port.value as string[];
	await Promise.all(
		runIdList.map(async (runId) => {
			const resultCsv = await getRunResult(runId, 'result.csv');
			const csvData = csvParse(resultCsv);

			const configId = props.node.inputs[0].value?.[0];
			if (configId) {
				const modelConfig = await getModelConfigurationById(configId);
				const parameters = modelConfig.configuration.semantics.ode.parameters;
				csvData.forEach((row) =>
					parameters.forEach((parameter) => {
						row[parameter.id] = parameter.value;
					})
				);
			}
			runResults.value[runId] = csvData as any;
		})
	);
});

const addChart = () => {
	const state = _.cloneDeep(props.node.state);
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
	margin: 1em 0em;
}

.add-chart {
	width: 5rem;
}
</style>
