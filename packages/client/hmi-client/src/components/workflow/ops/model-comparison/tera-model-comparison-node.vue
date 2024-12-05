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
import { type WorkflowNode, WorkflowPortStatus } from '@/types/workflow';
import { ModelComparisonOperationState } from './model-comparison-operation';

const emit = defineEmits(['append-input-port', 'open-drilldown']);

const props = defineProps<{
	node: WorkflowNode<ModelComparisonOperationState>;
}>();

const hasAtLeastTwoValues = computed(() => props.node.inputs.filter((input) => input.value).length >= 2);

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
