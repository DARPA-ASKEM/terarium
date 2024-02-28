<template>
	<main>
		<tera-simulate-chart
			v-if="hasData"
			:run-results="{ [selectedRunId]: runResults[selectedRunId] }"
			:chartConfig="{
				selectedRun: selectedRunId,
				selectedVariable: props.node.state.chartConfigs[0]
			}"
			:size="{ width: 180, height: 120 }"
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
import { ref, computed, watch } from 'vue';
import Button from 'primevue/button';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
import { WorkflowNode } from '@/types/workflow';
import { RunResults } from '@/types/SimulateConfig';
import { getRunResult } from '@/services/models/simulation-service';
import { csvParse } from 'd3';
import { SimulateJuliaOperationState } from './simulate-julia-operation';

const props = defineProps<{
	node: WorkflowNode<SimulateJuliaOperationState>;
}>();

const emit = defineEmits(['open-drilldown']);
const runResults = ref<RunResults>({});
const hasData = ref(false);

const areInputsFilled = computed(() => props.node.inputs[0].value);

const selectedOutputId = ref<string>(props.node.active as string);
const selectedRunId = computed(
	() => props.node.outputs.find((o) => o.id === selectedOutputId.value)?.value?.[0]
);

watch(
	() => selectedRunId.value,
	async () => {
		if (!selectedRunId.value) return;

		const resultCsv = await getRunResult(selectedRunId.value, 'result.csv');
		const csvData = csvParse(resultCsv);
		runResults.value[selectedRunId.value] = csvData as any;
		hasData.value = true;
	},
	{ immediate: true }
);
</script>

<style scoped></style>
