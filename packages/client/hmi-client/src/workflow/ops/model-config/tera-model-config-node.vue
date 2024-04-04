<template>
	<section>
		<tera-operator-placeholder :operation-type="node.operationType" />
		<Button
			:label="allowOpen ? 'Attach a model' : 'Open'"
			@click="emit('open-drilldown')"
			severity="secondary"
			outlined
			:disabled="allowOpen"
		/>
	</section>
</template>

<script setup lang="ts">
import { WorkflowNode, WorkflowPortStatus } from '@/types/workflow';
import Button from 'primevue/button';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { computed, watch } from 'vue';
import { ModelConfigOperationState } from './model-config-operation';

const props = defineProps<{
	node: WorkflowNode<ModelConfigOperationState>;
}>();
const emit = defineEmits(['open-drilldown', 'append-input-port']);

const allowOpen = computed(() => {
	console.debug('allowOpen', props.node.inputs);
	return (
		props.node.inputs.find((input) => input.type === 'modelId')?.status !==
		WorkflowPortStatus.CONNECTED
	);
});

watch(
	() => props.node.inputs,
	() => {
		if (
			props.node.inputs
				.filter((input) => input.type === 'datasetId')
				.every((input) => input.status === WorkflowPortStatus.CONNECTED)
		) {
			emit('append-input-port', { type: 'datasetId', label: 'Dataset', isOptional: true });
		}
	},
	{ deep: true }
);
</script>
