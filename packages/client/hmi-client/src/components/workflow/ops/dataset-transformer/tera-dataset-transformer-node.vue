<template>
	<section>
		<tera-operator-placeholder v-if="!node.inputs[0].value" :node="node">
			Attach one or more resources
		</tera-operator-placeholder>
		<template v-else>
			<tera-beaker-code-cell-output
				v-for="m in node.state.selectedOutputs?.[0]?.messages ?? []"
				:key="m.header.msg_id"
				:jupyter-message="m"
			/>
			<Button label="Open" @click="emit('open-drilldown')" severity="secondary" outlined />
		</template>
	</section>
</template>

<script setup lang="ts">
import { watch } from 'vue';
import { WorkflowNode, WorkflowPortStatus } from '@/types/workflow';
import Button from 'primevue/button';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraBeakerCodeCellOutput from '@/components/llm/tera-beaker-code-cell-output.vue';
import { DatasetTransformerState } from './dataset-transformer-operation';

const emit = defineEmits(['append-input-port', 'open-drilldown']);

const props = defineProps<{
	node: WorkflowNode<DatasetTransformerState>;
}>();

watch(
	// add another input port when all inputs are connected, we want to add as many datasets as we can
	() => props.node.inputs,
	() => {
		const inputs = props.node.inputs;
		if (inputs.every((input) => input.status === WorkflowPortStatus.CONNECTED)) {
			emit('append-input-port', { type: 'datasetId|simulationId', label: 'Dataset or Simulation' });
		}
	},
	{ deep: true }
);
</script>

<style scoped></style>
