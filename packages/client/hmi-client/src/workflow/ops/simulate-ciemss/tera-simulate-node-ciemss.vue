<template>
	<section v-if="!showSpinner">
		<div class="chart-container">
			<tera-simulate-chart
				v-for="(cfg, index) of node.state.chartConfigs"
				:key="index"
				:run-results="runResults"
				:chartConfig="cfg"
				has-mean-line
				@configuration-change="configurationChange(index, $event)"
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
import { ref, watch, onMounted, onUnmounted } from 'vue';
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
import { useToastService } from '@/services/toast';
import { SimulateCiemssOperation, SimulateCiemssOperationState } from './simulate-ciemss-operation';

const props = defineProps<{
	node: WorkflowNode;
}>();
const emit = defineEmits(['append-output-port', 'update-state']);

const toast = useToastService();
const showSpinner = ref(false);
// EXTRA section
const numSamples = ref(props.node.state.numSamples);
const method = ref(props.node.state.method);
const ciemssMethodOptions = ref(['dopri5', 'euler']);

const completedRunIdList = ref<string[]>([]);
const runResults = ref<RunResults>({});
const runConfigs = ref<{ [paramKey: string]: number[] }>({});
const progress = ref({ status: ProgressState.RETRIEVING, value: 0 });

const poller = new Poller();

const runSimulate = async () => {
	const modelConfigurationList = props.node.inputs[0].value;
	if (!modelConfigurationList?.length) return;

	const state = props.node.state as SimulateCiemssOperationState;

	const simulationRequests = modelConfigurationList.map(async (configId: string) => {
		const payload: SimulationRequest = {
			modelConfigId: configId,
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
		return response.id;
	});

	const response = await Promise.all(simulationRequests);
	getStatus(response);
};

onMounted(() => {
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
		console.error('Failed', runIds);
		showSpinner.value = false;
		toast.error('', `Simulation: ${runIds} has failed`);
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
		const state: SimulateCiemssOperationState = _.cloneDeep(props.node.state);
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
		const state: SimulateCiemssOperationState = _.cloneDeep(props.node.state);
		state.method = method.value;

		workflowEventBus.emitNodeStateChange({
			workflowId: props.node.workflowId,
			nodeId: props.node.id,
			state
		});
	}
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
