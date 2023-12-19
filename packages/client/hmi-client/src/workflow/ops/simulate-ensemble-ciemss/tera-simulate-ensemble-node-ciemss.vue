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
				<Button
					class="add-chart"
					text
					:outlined="true"
					@click="addChart"
					label="Add chart"
					icon="pi pi-plus"
				/>
			</template>
			<Button
				label="Run"
				@click="runEnsemble"
				:disabled="disableRunButton"
				icon="pi pi-play"
				severity="secondary"
				outlined
			/>
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
// import { csvParse } from 'd3';
// import { ModelConfiguration } from '@/types/Types';
// import { getRunResult } from '@/services/models/simulation-service';
import { WorkflowNode } from '@/types/workflow';
// import { getModelConfigurationById } from '@/services/model-configurations';
import {
	EnsembleModelConfigs,
	EnsembleSimulationCiemssRequest,
	ProgressState,
	TimeSpan
} from '@/types/Types';
import {
	getRunResultCiemss,
	makeEnsembleCiemssSimulation,
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
const modelConfigIds = computed<string[]>(() => props.node.inputs[0].value as string[]);
const completedRunId = ref<string>();
const ensembleConfigs = computed<EnsembleModelConfigs[]>(() => props.node.state.mapping);
const disableRunButton = computed(() => !ensembleConfigs?.value[0]?.weight);
const timeSpan = computed<TimeSpan>(() => props.node.state.timeSpan);
const numSamples = ref<number>(props.node.state.numSamples);
const runResults = ref<RunResults>({});
const simulationIds: ComputedRef<any | undefined> = computed(
	<any | undefined>(() => props.node.outputs[0]?.value)
);
const progress = ref({ status: ProgressState.Retrieving, value: 0 });

const poller = new Poller();

onMounted(() => {
	const runIds = querySimulationInProgress(props.node);
	if (runIds.length > 0) {
		getStatus(runIds[0]);
	}
});

onUnmounted(() => {
	poller.stop();
});

const runEnsemble = async () => {
	const params: EnsembleSimulationCiemssRequest = {
		modelConfigs: ensembleConfigs.value,
		timespan: timeSpan.value,
		engine: 'ciemss',
		extra: { num_samples: numSamples.value }
	};
	const response = await makeEnsembleCiemssSimulation(params);
	if (response?.simulationId) {
		getStatus(response.simulationId);
	}
};

// Tom TODO: Make this generic, its copy paste from drilldown
const chartConfigurationChange = (index: number, config: ChartConfig) => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs[index] = config;

	emit('update-state', state);
};

// TODO: This is repeated every single node that uses a chart. Hope to refactor if the state manip allows for it easily
const addChart = () => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs.push({ selectedRun: '', selectedVariable: [] } as ChartConfig);

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
	addChart();
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

watch(
	() => modelConfigIds.value,
	async () => {
		if (modelConfigIds.value) {
			const mapping: EnsembleModelConfigs[] = [];
			// Init ensemble Configs:
			for (let i = 0; i < modelConfigIds.value.length; i++) {
				mapping[i] = {
					id: modelConfigIds.value[i],
					solutionMappings: {},
					weight: 0
				};
			}

			const state = _.cloneDeep(props.node.state);
			state.modelConfigIds = modelConfigIds.value;
			state.mapping = mapping;
			emit('update-state', state);
		}
	},
	{ immediate: true }
);

watch(
	() => simulationIds.value,
	async () => {
		if (!simulationIds.value) return;

		const output = await getRunResultCiemss(simulationIds.value[0].runId, 'result.csv');
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
