<template>
	<section v-if="!showSpinner">
		<template v-if="node.inputs[0].value">
			<Dropdown
				v-if="runList.length > 0"
				:options="runList"
				v-model="selectedRun"
				option-label="label"
				placeholder="Select a simulation run"
				@update:model-value="handleSelectedRunChange"
			/>
			<div v-if="runResults[selectedRun?.runId]">
				<tera-simulate-chart
					v-for="(cfg, idx) in node.state.simConfigs.chartConfigs"
					:key="idx"
					:run-results="{ [selectedRun.runId]: runResults[selectedRun.runId] }"
					:chartConfig="{ selectedRun: selectedRun.runId, selectedVariable: cfg }"
					@configuration-change="configurationChange(idx, $event)"
					:colorByRun="true"
				/>
				<Button
					class="add-chart"
					text
					@click="addChart"
					label="Add chart"
					icon="pi pi-plus"
				></Button>
			</div>
			<Button label="Run" @click="runSimulate" icon="pi pi-play" severity="secondary" outlined />
			<Button
				label="Simulation settings"
				@click="emit('open-drilldown')"
				severity="secondary"
				outlined
			/>
		</template>
		<tera-operator-placeholder v-else :operation-type="node.operationType">
			Connect a model configuration
		</tera-operator-placeholder>
	</section>
	<section v-else>
		<tera-progress-bar :value="progress.value" :status="progress.status" />
	</section>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref, watch, computed, onMounted, onUnmounted } from 'vue';
import TeraOperatorPlaceholder from '@/workflow/operator/tera-operator-placeholder.vue';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import { csvParse } from 'd3';
import { ModelConfiguration, SimulationRequest } from '@/types/Types';

import {
	makeForecastJob,
	getRunResult,
	simulationPollAction,
	querySimulationInProgress,
	getSimulation
} from '@/services/models/simulation-service';
import { ProgressState, WorkflowNode } from '@/types/workflow';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';

import { getModelConfigurationById } from '@/services/model-configurations';
import { Poller, PollerState } from '@/api/api';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
import TeraProgressBar from '@/workflow/tera-progress-bar.vue';
import { logger } from '@/utils/logger';
import { SimulateJuliaOperation, SimulateJuliaOperationState } from './simulate-julia-operation';

const emit = defineEmits(['append-output-port', 'update-state', 'open-drilldown']);

const props = defineProps<{
	node: WorkflowNode<SimulateJuliaOperationState>;
}>();

const showSpinner = ref(false);
const completedRunIdList = ref<string[]>([]);
const runResults = ref<RunResults>({});

const modelConfiguration = ref<ModelConfiguration | null>(null);
const modelConfigId = computed<string | undefined>(() => props.node.inputs[0].value?.[0]);
const progress = ref({ status: ProgressState.RETRIEVING, value: 0 });

const runList = computed(() =>
	Object.keys(props.node.state.simConfigs.runConfigs).map((runId: string, idx: number) => ({
		label: `Output ${idx + 1} - ${runId}`,
		runId
	}))
);
const selectedRun = ref();

const poller = new Poller();

onMounted(() => {
	const runIds = querySimulationInProgress(props.node);
	if (runIds.length > 0) {
		getStatus(runIds);
	}

	const runId = Object.values(props.node.state.simConfigs.runConfigs).find(
		(metadata) => metadata.active
	)?.runId;
	if (runId) {
		selectedRun.value = runList.value.find((run) => run.runId === runId);
	} else {
		selectedRun.value = runList.value.length > 0 ? runList.value[0] : undefined;
	}

	if (selectedRun.value?.runId) {
		lazyLoadRunResults(selectedRun.value.runId);
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

	const port = props.node.inputs[0];

	const state = _.cloneDeep(props.node.state);
	if (state.simConfigs.chartConfigs.length === 0) {
		state.simConfigs.chartConfigs.push([]);
	}

	const sim = await getSimulation(runIdList[0]);
	if (sim) {
		state.simConfigs.runConfigs[sim.id] = {
			runId: sim.id,
			active: true,
			configName: port.label,
			timeSpan: sim.executionPayload.timespan
		};
	}
	emit('update-state', state);

	emit('append-output-port', {
		type: SimulateJuliaOperation.outputs[0].type,
		label: `${port.label} - Output ${runList.value.length}`, // TODO: figure out more robust naming system
		value: runIdList
	});

	// show the latest run in the dropdown
	selectedRun.value = runList.value[runList.value.length - 1];
	// persist the selected run in the chart config
	handleSelectedRunChange();
};

const configurationChange = (index: number, config: ChartConfig) => {
	const state = _.cloneDeep(props.node.state);
	state.simConfigs.chartConfigs[index] = config.selectedVariable;

	emit('update-state', state);
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

const lazyLoadRunResults = async (runId: string) => {
	if (runResults.value[runId]) return;

	const resultCsv = await getRunResult(runId, 'result.csv');
	const csvData = csvParse(resultCsv);

	// there's only a single input config
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
};

const handleSelectedRunChange = () => {
	if (!selectedRun.value) return;

	lazyLoadRunResults(selectedRun.value.runId);

	const state = _.cloneDeep(props.node.state);
	// set the active status for the selected run in the run configs
	Object.keys(state.simConfigs.runConfigs).forEach((runId) => {
		state.simConfigs.runConfigs[runId].active = runId === selectedRun.value?.runId;
	});

	emit('update-state', state);
};

const addChart = () => {
	const state = _.cloneDeep(props.node.state);
	state.simConfigs.chartConfigs.push([]);

	emit('update-state', state);
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

.add-chart {
	width: 5rem;
}
</style>
