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
import { computed, watch, ref } from 'vue';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
import { WorkflowNode } from '@/types/workflow';
import Button from 'primevue/button';
import { RunType } from '@/types/SimulateConfig';
import { getRunResultJulia } from '@/services/models/simulation-service';
import { csvParse } from 'd3';
import { CalibrationOperationStateJulia } from './calibrate-operation';

const props = defineProps<{
	node: WorkflowNode<CalibrationOperationStateJulia>;
}>();

const areInputsFilled = computed(() => props.node.inputs[0].value && props.node.inputs[1].value);
const simulationId = ref<any>(null);
const emit = defineEmits(['open-drilldown']);
const csvData = ref<any>(null);

watch(
	() => props.node.active,
	async () => {
		const active = props.node.active;
		if (!active) return;

		// FIXME: outdated, don't need tos upport multiple runs
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
