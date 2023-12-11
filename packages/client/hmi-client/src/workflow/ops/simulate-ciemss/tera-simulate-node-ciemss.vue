<template>
	<section v-if="!showSpinner">
		<div class="button-container">
			<Button size="small" label="Run" @click="runSimulate" icon="pi pi-play"></Button>
		</div>
		<Dropdown
			v-if="runList.length > 0"
			:options="runList"
			v-model="selectedRun"
			option-label="label"
			placeholder="Select a simulation run"
			@update:model-value="handleSelectedRunChange"
		/>
		<div class="chart-container" v-if="runResults[selectedRun?.runId]">
			<tera-simulate-chart
				v-for="(cfg, idx) in node.state.simConfigs.chartConfigs"
				:key="idx"
				:run-results="runResults[selectedRun.runId]"
				:chartConfig="{ selectedRun: selectedRun.runId, selectedVariable: cfg }"
				has-mean-line
				@configuration-change="configurationChange(idx, $event)"
			/>
			<div class="button-container">
				<Button
					class="add-chart"
					size="small"
					text
					@click="addChart"
					label="Add chart"
					icon="pi pi-plus"
				>
				</Button>
			</div>
		</div>
		<Accordion :multiple="true" :active-index="[0]">
			<AccordionTab header="EXTRAS">
				<span class="extras">
					<label>num_samples</label>
					<InputNumber v-model.lazy="numSamples" />
					<label>method</label>
					<Dropdown :options="ciemssMethodOptions" v-model="method" />
				</span>
			</AccordionTab>
		</Accordion>
	</section>
	<section v-else>
		<tera-progress-bar :value="progress.value" :status="progress.status" />
	</section>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref, watch, onMounted, onUnmounted, computed } from 'vue';
import Button from 'primevue/button';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Dropdown from 'primevue/dropdown';
import {
	makeForecastJobCiemss as makeForecastJob,
	getRunResultCiemss,
	simulationPollAction,
	querySimulationInProgress,
	getSimulation
} from '@/services/models/simulation-service';
import InputNumber from 'primevue/inputnumber';
import { ProgressState, WorkflowNode } from '@/types/workflow';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';
import { Poller, PollerState } from '@/api/api';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
import TeraProgressBar from '@/workflow/tera-progress-bar.vue';
import { SimulationRequest } from '@/types/Types';
import { logger } from '@/utils/logger';
import { SimulateCiemssOperation, SimulateCiemssOperationState } from './simulate-ciemss-operation';

const props = defineProps<{
	node: WorkflowNode<SimulateCiemssOperationState>;
}>();
const emit = defineEmits(['append-output-port', 'update-state']);

const showSpinner = ref(false);
// EXTRA section
const numSamples = ref(props.node.state.numSamples);
const method = ref(props.node.state.method);
const ciemssMethodOptions = ref(['dopri5', 'euler']);

const completedRunIdList = ref<string[]>([]);
const runResults = ref<{ [runId: string]: RunResults }>({});
const progress = ref({ status: ProgressState.RETRIEVING, value: 0 });

const runList = computed(() =>
	Object.keys(props.node.state.simConfigs.runConfigs).map((runId: string, idx: number) => ({
		label: `Output ${idx + 1} - ${runId}`,
		runId
	}))
);
const selectedRun = ref();

const poller = new Poller();

const runSimulate = async () => {
	const modelConfigurationList = props.node.inputs[0].value;
	if (!modelConfigurationList?.length) return;

	// Since we've disabled multiple configs to a simulation node, we can assume only one config
	const modelConfigId = modelConfigurationList[0];

	const state = props.node.state;

	const payload: SimulationRequest = {
		modelConfigId,
		timespan: {
			start: state.currentTimespan.start,
			end: state.currentTimespan.end
		},
		extra: {
			num_samples: state.numSamples,
			method: state.method
		},
		engine: 'ciemss'
	};
	const response = await makeForecastJob(payload);
	getStatus([response.id]);
};

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
		logger.error(`Simulation: ${runIds} has failed`, {
			toastTitle: 'Error - Pyciemss'
		});
		throw Error('Failed Runs');
	}
	completedRunIdList.value = runIds;
	showSpinner.value = false;
};

// assume only one run for now
const watchCompletedRunList = async (runIdList: string[]) => {
	if (runIdList.length === 0) return;

	await lazyLoadRunResults(runIdList[0]);

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
			numSamples: sim.executionPayload.extra.num_samples,
			method: sim.executionPayload.extra.method,
			timeSpan: sim.executionPayload.timespan
		};
	}

	emit('update-state', state);

	emit('append-output-port', {
		type: SimulateCiemssOperation.outputs[0].type,
		label: `${port.label} - Output ${runList.value.length}`, // TODO: figure out more robust naming system
		value: runIdList
	});

	// show the latest run in the dropdown
	selectedRun.value = runList.value[runList.value.length - 1];
	// persist the selected run in the chart config
	handleSelectedRunChange();
};
watch(() => completedRunIdList.value, watchCompletedRunList, { immediate: true });

watch(
	() => numSamples.value,
	() => {
		const state = _.cloneDeep(props.node.state);
		state.numSamples = numSamples.value;

		emit('update-state', state);
	}
);

watch(
	() => method.value,
	() => {
		const state = _.cloneDeep(props.node.state);
		state.method = method.value;

		emit('update-state', state);
	}
);

const configurationChange = (index: number, config: ChartConfig) => {
	const state = _.cloneDeep(props.node.state);
	state.simConfigs.chartConfigs[index] = config.selectedVariable;

	emit('update-state', state);
};

const lazyLoadRunResults = async (runId: string) => {
	if (runResults.value[runId]) return;

	const output = await getRunResultCiemss(runId);
	runResults.value[runId] = output.runResults;
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

.simulate-chart {
	margin: 1.5em 0em;
}

.add-chart {
	width: 9em;
}

.extras {
	display: grid;
}

.button-container {
	padding-bottom: 10px;
}
</style>
