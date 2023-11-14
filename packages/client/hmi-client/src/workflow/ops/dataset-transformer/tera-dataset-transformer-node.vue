<template>
	<div>
		<!--TODO add some templating for the trasformer node-->
	</div>
</template>

<script setup lang="ts">
import { watch } from 'vue';
import { WorkflowNode, WorkflowPortStatus } from '@/types/operator';
import { DatasetTransformerState } from './dataset-transformer-operation';

const props = defineProps<{
	node: WorkflowNode<DatasetTransformerState>;
}>();
const emit = defineEmits(['append-input-port']);

watch(
	// add another input port when all inputs are connected, we want to add as many datasets as we can
	() => props.node.inputs,
	() => {
		if (props.node.inputs.every((input) => input.status === WorkflowPortStatus.CONNECTED)) {
			emit('append-input-port', { type: 'datasetId', label: 'Dataset' });
		}
	},
	{ deep: true }
);
</script>

<style scoped></style>
