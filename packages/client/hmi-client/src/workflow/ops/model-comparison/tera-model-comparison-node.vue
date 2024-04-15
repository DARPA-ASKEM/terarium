<template>
	<section>
		<tera-operator-placeholder
			v-if="!node.inputs[0].value || !node.inputs[1].value"
			:operation-type="node.operationType"
		>
			Attach models to compare
		</tera-operator-placeholder>
		<Button v-else label="Open" @click="emit('open-drilldown')" severity="secondary" outlined />
	</section>
</template>

<script setup lang="ts">
import { watch } from 'vue';
import Button from 'primevue/button';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { WorkflowNode, WorkflowPortStatus } from '@/types/workflow';
import { ModelComparisonOperationState } from './model-comparison-operation';

const emit = defineEmits(['append-input-port', 'open-drilldown']);

const props = defineProps<{
	node: WorkflowNode<ModelComparisonOperationState>;
}>();

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
