<template>
	<div>
		<Button label="Open" @click="emit('open-drilldown')" severity="secondary" outlined />
	</div>
</template>

<script setup lang="ts">
import { WorkflowNode } from '@/types/workflow';
import Button from 'primevue/button';
import { DecapodesOperationState } from './decapodes-operation';

defineProps<{
	node: WorkflowNode<DecapodesOperationState>;
}>();

const emit = defineEmits(['update-state', 'append-output', 'open-drilldown']);
</script>

<style scoped>
/* Your styles go here */
</style>
