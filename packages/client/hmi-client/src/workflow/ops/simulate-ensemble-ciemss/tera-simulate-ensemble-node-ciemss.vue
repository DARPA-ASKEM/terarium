<template>
	<section v-if="!inProgressSimulationId && runResults[selectedRunId]">
		<tera-simulate-chart
			v-for="(cfg, index) of node.state.chartConfigs"
			:key="index"
			:run-results="runResults[selectedRunId]"
			:chartConfig="cfg"
			has-mean-line
			@configuration-change="chartConfigurationChange(index, $event)"
			:size="{ width: 190, height: 120 }"
		/>
	</section>

	<Button
		v-if="node.inputs[0].value"
		label="Edit"
		@click="emit('open-drilldown')"
		severity="secondary"
		outlined
	/>
	<tera-operator-placeholder v-else :operation-type="node.operationType">
		Connect a model configuration
	</tera-operator-placeholder>

	<tera-progress-spinner
		v-if="inProgressSimulationId"
		:font-size="2"
		is-centered
		style="height: 100%"
	/>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { ref, computed, watch } from 'vue';
import { WorkflowNode } from '@/types/workflow';
import { getRunResultCiemss, pollAction } from '@/services/models/simulation-service';
import Button from 'primevue/button';
import { ChartConfig, RunResults } from '@/types/SimulateConfig';
import { Poller, PollerState } from '@/api/api';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
import { logger } from '@/utils/logger';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import {
	SimulateEnsembleCiemssOperation,
	SimulateEnsembleCiemssOperationState
} from './simulate-ensemble-ciemss-operation';

const props = defineProps<{
	node: WorkflowNode<SimulateEnsembleCiemssOperationState>;
}>();
const emit = defineEmits(['append-output', 'update-state', 'open-drilldown']);

// const runResults = ref<RunResults>({});
const runResults = ref<{ [runId: string]: RunResults }>({});
const inProgressSimulationId = computed(() => props.node.state.inProgressSimulationId);
const selectedRunId = ref<string>('');

const poller = new Poller();

// Tom TODO: Make this generic, its copy paste from drilldown
const chartConfigurationChange = (index: number, config: ChartConfig) => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs[index] = config;

	emit('update-state', state);
};

const getStatus = async (simulationId: string) => {
	poller
		.setInterval(3000)
		.setThreshold(300)
		.setPollAction(async () => pollAction(simulationId));
	const pollerResults = await poller.start();

	if (pollerResults.state === PollerState.Cancelled) {
		return pollerResults;
	}

	if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		// throw if there are any failed runs for now
		logger.error(`Simulate Ensemble: ${simulationId} has failed`, {
			toastTitle: 'Error - Pyciemss'
		});
		throw Error('Failed Runs');
	}
	return pollerResults;
};

const processResult = async (simulationId: string) => {
	const portLabel = props.node.inputs[0].label;
	const state = _.cloneDeep(props.node.state);

	emit('append-output', {
		type: SimulateEnsembleCiemssOperation.outputs[0].type,
		label: `${portLabel} Result`,
		value: [simulationId],
		state: {
			mapping: state.mapping,
			timeSpan: state.timeSpan,
			numSamples: state.numSamples
		},
		isSelected: false
	});
};

watch(
	() => props.node.state.inProgressSimulationId,
	async (id) => {
		if (!id || id === '') return;

		const response = await getStatus(id);
		if (response?.state === PollerState.Done) {
			processResult(id);
		}
		const state = _.cloneDeep(props.node.state);
		state.inProgressSimulationId = '';
		emit('update-state', state);
	},
	{ immediate: true }
);

watch(
	() => props.node.active,
	async () => {
		const active = props.node.active;
		if (!active) return;

		selectedRunId.value = props.node.outputs.find((o) => o.id === active)?.value?.[0];
		if (!selectedRunId.value) return;

		const output = await getRunResultCiemss(selectedRunId.value, 'result.csv');
		runResults.value[selectedRunId.value] = output.runResults;
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

.simulate-chart {
	margin: 1em 0em;
}

.add-chart {
	width: 9em;
}
</style>
