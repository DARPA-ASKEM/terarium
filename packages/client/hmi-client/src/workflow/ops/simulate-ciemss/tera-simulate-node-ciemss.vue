<template>
	<main>
		<template v-if="selectedRunId && runResults[selectedRunId]">
			<tera-simulate-chart
				v-for="(config, idx) of props.node.state.chartConfigs"
				:key="idx"
				:run-results="runResults[selectedRunId]"
				:chartConfig="{
					selectedRun: selectedRunId,
					selectedVariable: config
				}"
				:size="{ width: 180, height: 120 }"
				has-mean-line
				@configuration-change="chartProxy.configurationChange(idx, $event)"
			/>
		</template>
		<tera-progress-spinner
			v-if="inProgressSimulationId"
			:font-size="2"
			is-centered
			style="height: 100%"
		/>
		<Button
			v-if="areInputsFilled"
			label="Edit"
			@click="emit('open-drilldown')"
			severity="secondary"
			outlined
		/>
		<tera-operator-placeholder v-else :operation-type="node.operationType">
			Connect a model configuration
		</tera-operator-placeholder>
	</main>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, ref, watch } from 'vue';
import Button from 'primevue/button';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import {
	getRunResultCiemss,
	pollAction,
	getSimulation
} from '@/services/models/simulation-service';
import { Poller, PollerState } from '@/api/api';
import { logger } from '@/utils/logger';
import { chartActionsProxy } from '@/workflow/util';

import type { WorkflowNode } from '@/types/workflow';
import type { RunResults } from '@/types/SimulateConfig';
import { SimulateCiemssOperationState, SimulateCiemssOperation } from './simulate-ciemss-operation';

const props = defineProps<{
	node: WorkflowNode<SimulateCiemssOperationState>;
}>();

const emit = defineEmits(['open-drilldown', 'update-state', 'append-output']);
const runResults = ref<{ [runId: string]: RunResults }>({});

const selectedRunId = ref<string>();
const inProgressSimulationId = computed(() => props.node.state.inProgressSimulationId);
const areInputsFilled = computed(() => props.node.inputs[0].value);

const poller = new Poller();
const pollResult = async (runId: string) => {
	selectedRunId.value = undefined;

	poller
		.setInterval(3000)
		.setThreshold(300)
		.setPollAction(async () => pollAction(runId));
	const pollerResults = await poller.start();
	let state = _.cloneDeep(props.node.state);
	state.errorMessage = { name: '', value: '', traceback: '' };

	if (pollerResults.state === PollerState.Cancelled) {
		state.inProgressSimulationId = '';
		poller.stop();
	} else if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		// throw if there are any failed runs for now
		logger.error(`Simulation: ${runId} has failed`, {
			toastTitle: 'Error - Pyciemss'
		});
		const simulation = await getSimulation(runId);
		if (simulation?.status && simulation?.statusMessage) {
			state = _.cloneDeep(props.node.state);
			state.inProgressSimulationId = '';
			state.errorMessage = {
				name: runId,
				value: simulation.status,
				traceback: simulation.statusMessage
			};
			emit('update-state', state);
		}
		throw Error('Failed Runs');
	}
	emit('update-state', state);
	return pollerResults;
};

const chartProxy = chartActionsProxy(props.node, (state: SimulateCiemssOperationState) => {
	emit('update-state', state);
});

const processResult = (runId: string) => {
	const state = _.cloneDeep(props.node.state);
	if (state.chartConfigs.length === 0) {
		chartProxy.addChart();
	}

	emit('append-output', {
		type: SimulateCiemssOperation.outputs[0].type,
		label: `Output - ${props.node.outputs.length + 1}`,
		value: [runId],
		state: {
			currentTimespan: state.currentTimespan,
			numSamples: state.numSamples,
			method: state.method
		},
		isSelected: false
	});
};

watch(
	() => props.node.state.inProgressSimulationId,
	async (id) => {
		if (!id || id === '') return;

		const response = await pollResult(id);
		if (response.state === PollerState.Done) {
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

		const output = await getRunResultCiemss(selectedRunId.value);
		runResults.value[selectedRunId.value] = output.runResults;
	},
	{ immediate: true }
);
</script>

<style scoped></style>
