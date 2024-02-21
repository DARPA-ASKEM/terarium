<template>
	<main>
		<tera-operator-placeholder :operation-type="node.operationType">
			<template v-if="!node.inputs[0].value"> Attach a model configuration </template>
		</tera-operator-placeholder>
		<template v-if="node.inputs[0].value">
			<div class="flex gap-2">
				<Button
					@click="emit('open-drilldown')"
					label="Edit"
					severity="secondary"
					outlined
					class="w-full"
				/>
				<!--TODO: Run button isn't functional-->
				<Button label="Run" severity="secondary" outlined disabled class="w-full" />
			</div>
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
