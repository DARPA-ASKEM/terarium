<template>
	<section>
		<tera-operator-placeholder :operation-type="node.operationType">
			<template v-if="!node.inputs[0].value">Attach a model</template>
		</tera-operator-placeholder>
		<Button
			v-if="node.inputs[0].value"
			@click="emit('open-drilldown')"
			label="Configure"
			severity="secondary"
			outlined
		/>
	</section>
</template>

<script setup lang="ts">
import { WorkflowNode } from '@/types/workflow';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import Button from 'primevue/button';
import { StratifyOperationStateMira } from './stratify-mira-operation';

const emit = defineEmits(['open-drilldown']);

defineProps<{
	node: WorkflowNode<StratifyOperationStateMira>;
}>();
</script>

<style scoped></style>
