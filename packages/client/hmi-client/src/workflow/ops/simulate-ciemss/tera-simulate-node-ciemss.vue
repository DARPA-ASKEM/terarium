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
import { computed, ref, watch } from 'vue';
import Button from 'primevue/button';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraSimulateChart from '@/workflow/tera-simulate-chart.vue';
import { WorkflowNode } from '@/types/workflow';
import { RunResults } from '@/types/SimulateConfig';
import { getRunResultCiemss } from '@/services/models/simulation-service';
import { SimulateCiemssOperationState } from './simulate-ciemss-operation';

const props = defineProps<{
	node: WorkflowNode<SimulateCiemssOperationState>;
}>();

const emit = defineEmits(['open-drilldown']);
const runResults = ref<{ [runId: string]: RunResults }>({});

const selectedOutputId = ref<string>(props.node.active as string);
const selectedRunId = computed(
	() => props.node.outputs.find((o) => o.id === selectedOutputId.value)?.value?.[0]
);

const areInputsFilled = computed(() => props.node.inputs[0].value);

const hasData = ref(false);

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
