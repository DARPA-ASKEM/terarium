<template>
	<section v-if="!showSpinner">
		<Dropdown :options="runList" v-model="selectedRun" option-label="label" />
		<div class="chart-container">
			<tera-simulate-chart
				v-if="selectedRun"
				:key="selectedRun.idx"
				:run-results="runResults"
				:chartConfig="node.state.chartConfigs[selectedRun.idx]"
				has-mean-line
				@configuration-change="configurationChange(selectedRun.idx, $event)"
			/>
		</div>
		<div class="button-container">
			<Button
				class="add-chart"
				size="small"
				text
				@click="addChart"
				label="Add chart"
				icon="pi pi-plus"
			></Button>
			<Button size="small" label="Run" @click="runSimulate" icon="pi pi-play"></Button>
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
	querySimulationInProgress
} from '@/services/models/simulation-service';
import InputNumber from 'primevue/inputnumber';
import { ProgressState, WorkflowNode } from '@/types/workflow';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';
import { workflowEventBus } from '@/services/workflow';
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
const runResults = ref<RunResults>({});
const runConfigs = ref<{ [paramKey: string]: number[] }>({});
const progress = ref({ status: ProgressState.RETRIEVING, value: 0 });

const runList = computed(() =>
	props.node.state.chartConfigs.map((cfg: ChartConfig, idx: number) => ({
		label: `Output ${idx + 1} - ${cfg.selectedRun}`,
		idx
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
	console.log(props.node.state.chartConfigs);
	const runIds = querySimulationInProgress(props.node);
	if (runIds.length > 0) {
		getStatus(runIds);
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
	() => numSamples.value,
	() => {
		const state = _.cloneDeep(props.node.state);
		state.numSamples = numSamples.value;

		workflowEventBus.emitNodeStateChange({
			workflowId: props.node.workflowId,
			nodeId: props.node.id,
			state
		});
	}
);

watch(
	() => method.value,
	() => {
		const state = _.cloneDeep(props.node.state);
		state.method = method.value;

		workflowEventBus.emitNodeStateChange({
			workflowId: props.node.workflowId,
			nodeId: props.node.id,
			state
		});
	}
);

const configurationChange = (index: number, config: ChartConfig) => {
	const state = _.cloneDeep(props.node.state);
	console.log(index, state.chartConfigs);
	state.chartConfigs[index] = config;

	workflowEventBus.emitNodeStateChange({
		workflowId: props.node.workflowId,
		nodeId: props.node.id,
		state
	});
};

const addChart = () => {
	const state = _.cloneDeep(props.node.state);
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
	display: flex;
	justify-content: space-between;
}
</style>
