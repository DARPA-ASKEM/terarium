<template>
	<section>
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
		</template>
	</section>
</template>

<script setup lang="ts">
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { WorkflowNode } from '@/types/workflow';
import { FunmanOperationState } from '@/workflow/ops/funman/funman-operation';
import Button from 'primevue/button';

const emit = defineEmits(['open-drilldown']);

defineProps<{
	node: WorkflowNode<FunmanOperationState>;
}>();
</script>

<style scoped></style>
