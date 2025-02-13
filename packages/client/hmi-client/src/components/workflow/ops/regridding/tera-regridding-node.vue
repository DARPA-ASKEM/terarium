<template>
	<section>
		<tera-operator-placeholder v-if="!node.inputs[0].value" :node="node">
			Attach one or more resources
		</tera-operator-placeholder>
		<Button v-else label="Open" @click="emit('open-drilldown')" severity="secondary" outlined />
	</section>
</template>

<script setup lang="ts">
import { watch } from 'vue';
import { WorkflowNode, WorkflowPortStatus } from '@/types/workflow';
import Button from 'primevue/button';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import type { RegriddingOperationState } from './regridding-operation';

const emit = defineEmits(['append-input-port', 'open-drilldown']);

const props = defineProps<{
	node: WorkflowNode<RegriddingOperationState>;
}>();

watch(
	// add another input port when all inputs are connected, we want to add as many datasets as we can
	() => props.node.inputs,
	() => {
		const inputs = props.node.inputs;
		if (inputs.every((input) => input.status === WorkflowPortStatus.CONNECTED)) {
			emit('append-input-port', { type: 'datasetId', labe: 'Dataset' });
		}
	},
	{ deep: true }
);
</script>

<style scoped></style>
