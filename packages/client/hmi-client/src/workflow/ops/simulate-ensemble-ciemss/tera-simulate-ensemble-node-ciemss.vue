<template>
	<section v-if="!showSpinner">
		<template v-if="node.inputs[0].value">
			<template v-if="simulationIds">
				<tera-simulate-chart
					v-for="(cfg, index) of node.state.chartConfigs"
					:key="index"
					:run-results="runResults"
					:chartConfig="cfg"
					has-mean-line
					@configuration-change="chartConfigurationChange(index, $event)"
				/>
			</template>
			<Button label="Open" @click="emit('open-drilldown')" severity="secondary" outlined />
		</template>
		<tera-operator-placeholder v-else :operation-type="node.operationType">
			Connect a model configuration
		</tera-operator-placeholder>
		<!--TODO: Consider adding status as another attribute to the placeholder
			A different image/message would appear depending on the node status. Currently it just depends on the type of operator.

			<div class="invalid-block" v-if="node.status === OperatorStatus.INVALID">
				<img class="image" src="@assets/svg/plants.svg" alt="" />
				<p class="helpMessage">Configure in side panel</p>
			</div> -->
	</section>
	<section v-else>
		<tera-progress-bar :value="progress.value" :status="progress.status" />
	</section>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, ComputedRef, onMounted, onUnmounted, ref, watch } from 'vue';
import { WorkflowNode } from '@/types/workflow';
import { ProgressState } from '@/types/Types';
import {
	getRunResultCiemss,
	querySimulationInProgress,
	simulationPollAction
} from '@/services/models/simulation-service';
import Button from 'primevue/button';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';
import { Poller, PollerState } from '@/api/api';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
import TeraProgressBar from '@/workflow/tera-progress-bar.vue';
import { logger } from '@/utils/logger';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import {
	SimulateEnsembleCiemssOperation,
	SimulateEnsembleCiemssOperationState
} from './simulate-ensemble-ciemss-operation';

const props = defineProps<{
	node: WorkflowNode<SimulateEnsembleCiemssOperationState>;
}>();
const emit = defineEmits(['append-output-port', 'update-state', 'open-drilldown']);

const showSpinner = ref(false);
const completedRunId = ref<string>();
const runResults = ref<RunResults>({});
const simulationIds: ComputedRef<any | undefined> = computed(
	<any | undefined>(() => props.node.outputs[0]?.value)
);
const progress = ref({ status: ProgressState.Retrieving, value: 0 });

const poller = new Poller();

// Tom TODO: Make this generic, its copy paste from drilldown
const chartConfigurationChange = (index: number, config: ChartConfig) => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs[index] = config;

	emit('update-state', state);
};

const getStatus = async (simulationId: string) => {
	showSpinner.value = true;
	if (!simulationId) return;

	const runIds = [simulationId];
	poller
		.setInterval(3000)
		.setThreshold(300)
		.setPollAction(async () => simulationPollAction(runIds, props.node, progress, emit));
	const pollerResults = await poller.start();

	if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		// throw if there are any failed runs for now
		showSpinner.value = false;
		logger.error(`Simulate Ensemble: ${simulationId} has failed`, {
			toastTitle: 'Error - Pyciemss'
		});
		throw Error('Failed Runs');
	}
	completedRunId.value = simulationId;
	updateOutputPorts(completedRunId);
	showSpinner.value = false;
};

const updateOutputPorts = async (runId) => {
	const portLabel = props.node.inputs[0].label;
	emit('append-output-port', {
		type: SimulateEnsembleCiemssOperation.outputs[0].type,
		label: `${portLabel} Result`,
		value: { runId }
	});
};

onMounted(() => {
	// FIXME: clean up to use just the active
	const runIds = querySimulationInProgress(props.node);
	if (runIds.length > 0) {
		getStatus(runIds[0]);
	}
});

onUnmounted(() => {
	poller.stop();
});

watch(
	() => simulationIds.value,
	async () => {
		if (!simulationIds.value) return;

		const output = await getRunResultCiemss(simulationIds.value[0], 'result.csv');
		runResults.value = output.runResults;
	},
	{ immediate: true }
);
</script>

<style scoped>
section {
	display: flex;
	flex-direction: column;
	width: 100%;
	padding: 10px;
	background: var(--surface-overlay);
}

.helpMessage {
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
}
.image {
	height: 8.75rem;
	margin-bottom: 0.5rem;
	background-color: var(--surface-ground);
	border-radius: 1rem;
	background-color: rgb(0, 0, 0, 0);
}

.invalid-block {
	display: contents;
}

.simulate-chart {
	margin: 1em 0em;
}

.add-chart {
	width: 9em;
}
</style>
