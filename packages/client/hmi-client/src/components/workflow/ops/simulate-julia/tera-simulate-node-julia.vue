<template>
	<main>
		<template v-if="runResults[selectedRunId]">
			<tera-simulate-chart
				v-for="(config, idx) of props.node.state.chartConfigs"
				:key="idx"
				:run-results="{ [selectedRunId]: runResults[selectedRunId] }"
				:chartConfig="{
					selectedRun: selectedRunId,
					selectedVariable: config
				}"
				:size="{ width: 180, height: 120 }"
				@configuration-change="chartProxy.configurationChange(idx, $event)"
			/>
		</template>

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
import { ref, computed, watch } from 'vue';
import Button from 'primevue/button';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraSimulateChart from '@/components/workflow/tera-simulate-chart.vue';
import type { WorkflowNode } from '@/types/workflow';
import type { RunResults } from '@/types/SimulateConfig';
import { getRunResult, pollAction } from '@/services/models/simulation-service';
import { csvParse } from 'd3';
import { Poller, PollerState } from '@/api/api';
import { logger } from '@/utils/logger';
import { chartActionsProxy, nodeOutputLabel } from '@/components/workflow/util';
import { SimulateJuliaOperation, SimulateJuliaOperationState } from './simulate-julia-operation';

const props = defineProps<{
	node: WorkflowNode<SimulateJuliaOperationState>;
}>();

const emit = defineEmits(['open-drilldown', 'append-output', 'update-state']);
const runResults = ref<RunResults>({});

const areInputsFilled = computed(() => props.node.inputs[0].value);

const selectedRunId = ref<any>(null);

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
		logger.error(`Simulate: ${runId} has failed`, {
			toastTitle: 'Error - Sciml'
		});
		throw Error('Failed Runs');
	}
	return pollerResults;
};

const chartProxy = chartActionsProxy(props.node, (state: SimulateJuliaOperationState) => {
	emit('update-state', state);
});

const processResult = async (runId: string) => {
	const state = _.cloneDeep(props.node.state);
	if (state.chartConfigs.length === 0) {
		addChart();
	}

	emit('append-output', {
		type: SimulateJuliaOperation.outputs[0].type,
		label: nodeOutputLabel(props.node, 'Output'),
		value: [runId],
		state: {
			currentTimespan: state.currentTimespan
		},
		isSelected: false
	});
};

const addChart = () => {
	const state = _.cloneDeep(props.node.state);
	state.chartConfigs.push([]);
	emit('update-state', state);
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

		const resultCsv = await getRunResult(selectedRunId.value, 'result.csv');
		const csvData = csvParse(resultCsv);
		runResults.value[selectedRunId.value] = csvData as any;
	},
	{ immediate: true }
);
</script>

<style scoped></style>
