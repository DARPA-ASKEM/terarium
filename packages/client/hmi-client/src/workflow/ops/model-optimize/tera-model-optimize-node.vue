<template>
	<main>
		<tera-operator-placeholder :operation-type="node.operationType">
			<template v-if="!node.inputs[0].value"> Attach a model configuration </template>
		</tera-operator-placeholder>
		<template v-if="node.inputs[0].value">
			<Button
				@click="emit('open-drilldown')"
				label="Review sanity checks"
				severity="secondary"
				outlined
			/>
			<!--TODO: Run button isn't functional-->
			<Button label="Run" severity="secondary" outlined />
		</template>
	</main>
</template>

<script setup lang="ts">
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { WorkflowNode } from '@/types/workflow';
import Button from 'primevue/button';
import { ModelOptimizeOperationState } from './model-optimize-operation';

const emit = defineEmits(['open-drilldown']);

defineProps<{
	node: WorkflowNode<ModelOptimizeOperationState>;
}>();
</script>

<style scoped></style>
