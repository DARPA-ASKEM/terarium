<template>
	<main>
		<tera-simulate-chart
			v-if="simulationId && csvData"
			:run-results="{ [simulationId]: csvData }"
			:mapping="props.node.state.mapping as any"
			:run-type="RunType.Julia"
			:chartConfig="{
				selectedRun: simulationId.value,
				selectedVariable: props.node.state.chartConfigs[0]
			}"
			:size="{ width: 190, height: 120 }"
		/>

		<Button
			v-if="areInputsFilled"
			label="Edit"
			@click="emit('open-drilldown')"
			severity="secondary"
			outlined
		/>
		<tera-operator-placeholder v-else :operation-type="node.operationType">
			Connect a model configuration and dataset
		</tera-operator-placeholder>
	</main>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, watch, ref } from 'vue';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
import Button from 'primevue/button';
import { RunType } from '@/types/SimulateConfig';
import { getRunResultJulia, pollAction } from '@/services/models/simulation-service';
import { csvParse } from 'd3';
import { Poller, PollerState } from '@/api/api';
import { logger } from '@/utils/logger';
import type { WorkflowNode } from '@/types/workflow';
import { CalibrationOperationStateJulia, CalibrationOperationJulia } from './calibrate-operation';

const props = defineProps<{
	node: WorkflowNode<CalibrationOperationStateJulia>;
}>();

const areInputsFilled = computed(() => props.node.inputs[0].value && props.node.inputs[1].value);
const simulationId = ref<any>(null);
const emit = defineEmits(['open-drilldown', 'append-output', 'update-state']);
const csvData = ref<any>(null);

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
		logger.error(`SCIML Calibrate: ${runId} has failed`, {
			toastTitle: 'Error - Julia'
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

const processResult = (id: string) => {
	const state = _.cloneDeep(props.node.state);

	if (state.chartConfigs.length === 0) {
		addChart();
	}

	emit('append-output', {
		type: CalibrationOperationJulia.outputs[0].type,
		label: `Output - ${props.node.outputs.length + 1}`,
		value: [id],
		isSelected: false,
		state: {
			extra: state.extra,
			simulationsInProgress: state.simulationsInProgress,
			intermediateLoss: state.intermediateLoss
		}
	});
};

watch(
	() => props.node.state.inProgressSimulationId,
	async (id) => {
		if (!id || id === '') return;

		console.log('poll....');
		const response = await pollResult(id);
		if (response.state === PollerState.Done) {
			setTimeout(() => {
				processResult(id);

				const state = _.cloneDeep(props.node.state);
				state.inProgressSimulationId = '';
				emit('update-state', state);
			}, 4000);
		}
	},
	{ immediate: true }
);

watch(
	() => props.node.active,
	async () => {
		const active = props.node.active;
		if (!active) return;

		simulationId.value = props.node.outputs.find((d) => d.id === active)?.value?.[0];
		if (!simulationId.value) return;

		const result = await getRunResultJulia(simulationId.value, 'result.json');
		if (!result) return;

		csvData.value = csvParse(result.csvData);
	},
	{ immediate: true }
);
</script>

<style scoped></style>
