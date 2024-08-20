<template>
	<section>
		<tera-operator-placeholder :node="node">Attach models to compare</tera-operator-placeholder>
		<Button v-if="hasAtLeastTwoValues" label="Open" @click="emit('open-drilldown')" severity="secondary" outlined />
	</section>
</template>

<script setup lang="ts">
import { watch, computed } from 'vue';
import Button from 'primevue/button';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { WorkflowNode, WorkflowPortStatus, OperatorStatus } from '@/types/workflow';
import { ModelComparisonOperationState } from './model-comparison-operation';

const emit = defineEmits(['append-input-port', 'open-drilldown', 'update-status']);

const props = defineProps<{
	node: WorkflowNode<ModelComparisonOperationState>;
}>();

const hasAtLeastTwoValues = computed(() => {
	const flag = props.node.inputs.filter((input) => input.value).length >= 2;
	// This is a custom way of granting a default status to the operator, since it has no output
	if (flag) emit('update-status', OperatorStatus.DEFAULT);
	else emit('update-status', OperatorStatus.INVALID);
	return flag;
});

watch(
	() => props.node.inputs,
	() => {
		if (props.node.inputs.every((input) => input.status === WorkflowPortStatus.CONNECTED)) {
			emit('append-input-port', { type: 'modelId', label: 'Model' });
		}
	},
	{ deep: true }
);
</script>
