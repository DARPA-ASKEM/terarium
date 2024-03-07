<template>
	<main>
		<tera-simulate-chart
			v-if="hasData"
			:run-results="runResults[selectedRunId]"
			:chartConfig="{
				selectedRun: selectedRunId,
				selectedVariable: props.node.state.chartConfigs[0]
			}"
			:size="{ width: 180, height: 120 }"
			has-mean-line
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
import { WorkflowNode } from '@/types/workflow';
import { RunResults } from '@/types/SimulateConfig';
import { getRunResultCiemss, pollAction } from '@/services/models/simulation-service';
import { Poller, PollerState } from '@/api/api';
import { logger } from '@/utils/logger';
import { SimulateCiemssOperationState, SimulateCiemssOperation } from './simulate-ciemss-operation';

const props = defineProps<{
	node: WorkflowNode<SimulateCiemssOperationState>;
}>();

const emit = defineEmits(['open-drilldown', 'update-state', 'append-output']);
const runResults = ref<{ [runId: string]: RunResults }>({});

const selectedOutputId = ref<string>(props.node.active as string);
const selectedRunId = computed(
	() => props.node.outputs.find((o) => o.id === selectedOutputId.value)?.value?.[0]
);

const areInputsFilled = computed(() => props.node.inputs[0].value);

const hasData = ref(false);

const poller = new Poller();
const pollResult = async (runId: string) => {
	poller
		.setInterval(3000)
		.setThreshold(300)
		.setPollAction(async () => pollAction(runId));
	const pollerResults = await poller.start();

	if (pollerResults.state === PollerState.Cancelled) {
		return pollerResults;
	}
	if (pollerResults.state !== PollerState.Done || !pollerResults.data) {
		// throw if there are any failed runs for now
		logger.error(`Simulation: ${runId} has failed`, {
			toastTitle: 'Error - Pyciemss'
		});
		throw Error('Failed Runs');
	}
	return pollerResults;
};

const addChart = () => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs.push([]);

	emit('update-state', state);
};

const processResult = (runId: string) => {
	const state = _.cloneDeep(props.node.state);
	if (state.chartConfigs.length === 0) {
		addChart();
	}

	emit('append-output', {
		type: SimulateCiemssOperation.outputs[0].type,
		label: `Output - ${props.node.outputs.length + 1}`,
		value: runId,
		state: {
			currentTimespan: state.currentTimespan,
			numSamples: state.numSamples,
			method: state.method,
			inProgressSimulationId: state.inProgressSimulationId
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
	() => selectedRunId.value,
	async () => {
		if (!selectedRunId.value) return;
		const output = await getRunResultCiemss(selectedRunId.value);
		runResults.value[selectedRunId.value] = output.runResults;

		hasData.value = true;
	},
	{ immediate: true }
);
</script>

<style scoped></style>
