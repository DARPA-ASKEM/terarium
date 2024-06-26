<template>
	<section>
		<tera-operator-placeholder :operation-type="node.operationType" />
		<Button
			:label="isModelInputConnected ? 'Open' : 'Attach a model'"
			@click="emit('open-drilldown')"
			severity="secondary"
			outlined
			:disabled="!isModelInputConnected"
		/>
	</section>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { WorkflowNode, WorkflowPortStatus } from '@/types/workflow';
import Button from 'primevue/button';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { InterventionsState } from './tera-interventions-operation';

const emit = defineEmits(['open-drilldown']);
const props = defineProps<{
	node: WorkflowNode<InterventionsState>;
}>();

const modelInput = props.node.inputs.find((input) => input.type === 'modelId');
const isModelInputConnected = computed(() => modelInput?.status === WorkflowPortStatus.CONNECTED);
</script>

<style scoped></style>
