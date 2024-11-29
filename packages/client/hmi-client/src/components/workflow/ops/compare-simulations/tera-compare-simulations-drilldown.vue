<template>
	<tera-drilldown
		:node="node"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		TBD
	</tera-drilldown>
</template>

<script setup lang="ts">
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import { WorkflowNode } from '@/types/workflow';
import { CompareSimulationsState } from './compare-simulations-operation';

// const props =
defineProps<{
	node: WorkflowNode<CompareSimulationsState>;
}>();

const emit = defineEmits(['update-state', 'update-status', 'close']);
</script>

<style scoped></style>
