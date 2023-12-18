<template>
	<section>
		<tera-operator-placeholder :operation-type="node.operationType">
			<template v-if="!node.inputs[0].value"> Attach a model configuration </template>
		</tera-operator-placeholder>
		<template v-if="node.inputs[0].value">
			<Button @click="emit('open-drilldown')" label="Edit" severity="secondary" outlined />
		</template>
	</section>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { WorkflowNode } from '@/types/workflow';
import { ModelEditOperationState } from '@/workflow/ops/model-edit/model-edit-operation';

const emit = defineEmits(['open-drilldown']);

defineProps<{
	node: WorkflowNode<ModelEditOperationState>;
}>();
</script>

<style scoped></style>
