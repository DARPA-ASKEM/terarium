<template>
	<main>
		<tera-simulate-chart
			v-if="runResults && csvAsset"
			:run-results="runResults"
			:chartConfig="props.node.state.chartConfigs[0]"
			:mapping="props.node.state.mapping as any"
			:initial-data="csvAsset"
			:size="{ width: 190, height: 120 }"
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
			Connect a model configuration and dataset
		</tera-operator-placeholder>
	</main>
</template>

<script setup lang="ts">
import { computed, watch, ref, shallowRef } from 'vue';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
import { WorkflowNode } from '@/types/workflow';
import Button from 'primevue/button';
import { getRunResultCiemss } from '@/services/models/simulation-service';
import { RunResults } from '@/types/SimulateConfig';
import { setupDatasetInput } from '@/services/calibrate-workflow';
import type { CsvAsset } from '@/types/Types';
import { CalibrationOperationStateCiemss } from './calibrate-operation';

const props = defineProps<{
	node: WorkflowNode<CalibrationOperationStateCiemss>;
}>();

const runResults = ref<RunResults>({});
const csvAsset = shallowRef<CsvAsset | undefined>(undefined);

const areInputsFilled = computed(() => props.node.inputs[0].value && props.node.inputs[1].value);

const emit = defineEmits(['open-drilldown']);

watch(
	() => props.node.active,
	async () => {
		const active = props.node.active;
		const state = props.node.state;
		if (!active) return;
		if (!state.simulationId) return;

		const simulationId = state.simulationId;

		// Simulate
		const result = await getRunResultCiemss(simulationId, 'result.csv');
		runResults.value = result.runResults;

		// Dataset used to calibrate
		const datasetId = props.node.inputs[1]?.value?.[0];
		const { csv } = await setupDatasetInput(datasetId);
		csvAsset.value = csv;
		console.log('csv', csvAsset.value);
	},
	{ immediate: true }
);
</script>

<style scoped></style>
