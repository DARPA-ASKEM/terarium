<template>
	<main>
		<template v-if="!inProgressSimulationId && simulationId && csvData">
			<tera-simulate-chart
				v-for="(config, index) of props.node.state.chartConfigs"
				:key="index"
				:run-results="{ [simulationId]: csvData }"
				:initial-data="csvAsset"
				:mapping="props.node.state.mapping as any"
				:run-type="RunType.Julia"
				:chartConfig="{
					selectedRun: simulationId.value,
					selectedVariable: config
				}"
				:size="{ width: 190, height: 120 }"
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
			Connect a model configuration and dataset
		</tera-operator-placeholder>
	</main>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { computed, watch, ref, shallowRef } from 'vue';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraSimulateChart from '@/components/workflow/tera-simulate-chart.vue';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import Button from 'primevue/button';
import { RunType } from '@/types/SimulateConfig';
import { getRunResultJulia, pollAction } from '@/services/models/simulation-service';
import { csvParse } from 'd3';
import { Poller, PollerState } from '@/api/api';
import { logger } from '@/utils/logger';
import { setupDatasetInput } from '@/services/calibrate-workflow';
import { chartActionsProxy } from '@/components/workflow/util';
import type { CsvAsset } from '@/types/Types';
import type { WorkflowNode } from '@/types/workflow';
import { CalibrationOperationStateJulia, CalibrationOperationJulia } from './calibrate-operation';
import {useProjects} from "@/composables/project";

const props = defineProps<{
	node: WorkflowNode<CalibrationOperationStateJulia>;
}>();

const areInputsFilled = computed(() => props.node.inputs[0].value && props.node.inputs[1].value);
const simulationId = ref<any>(null);
const emit = defineEmits(['open-drilldown', 'append-output', 'update-state']);
const csvData = ref<any>(null);
const inProgressSimulationId = computed(() => props.node.state.inProgressSimulationId);

const csvAsset = shallowRef<CsvAsset | undefined>(undefined);

const chartProxy = chartActionsProxy(props.node, (state: CalibrationOperationStateJulia) => {
	emit('update-state', state);
});

const poller = new Poller();
const pollResult = async (runId: string) => {
	poller
		.setInterval(3000)
		.setThreshold(300)
		.setPollAction(async () => pollAction(runId, useProjects().activeProjectId.value));

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

const processResult = (id: string) => {
	const state = _.cloneDeep(props.node.state);

	if (state.chartConfigs.length === 0) {
		chartProxy.addChart();
	}

	emit('append-output', {
		type: CalibrationOperationJulia.outputs[0].type,
		label: `Output - ${props.node.outputs.length + 1}`,
		value: [id],
		isSelected: false,
		state: {
			extra: state.extra,
			intermediateLoss: state.intermediateLoss
		}
	});
};

watch(
	() => props.node.state.inProgressSimulationId,
	async (id) => {
		if (!id || id === '') return;

		const response = await pollResult(id);
		if (response.state === PollerState.Done) {
			processResult(id);
			const state = _.cloneDeep(props.node.state);
			state.inProgressSimulationId = '';
			emit('update-state', state);
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

		// Dataset
		if (csvAsset.value) return;
		const datasetId = props.node.inputs[1]?.value?.[0];
		const { csv } = await setupDatasetInput(datasetId);
		csvAsset.value = csv;
	},
	{ immediate: true }
);
</script>

<style scoped></style>
