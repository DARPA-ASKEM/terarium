<template>
	<main>Model coupling</main>
	<Button
		v-if="node.inputs[0].value"
		@click="emit('open-drilldown')"
		label="Configure"
		severity="secondary"
		outlined
	/>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import { WorkflowNode } from '@/types/workflow';

defineProps<{
	node: WorkflowNode<any>;
}>();
const emit = defineEmits(['open-drilldown']);
</script>
