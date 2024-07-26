<template>
	<div>
		<tera-operator-placeholder v-if="!node.inputs[0].value" :node="node">
			Attach a decapode model
		</tera-operator-placeholder>
		<Button label="Open" @click="emit('open-drilldown')" severity="secondary" outlined />
	</div>
</template>

<script setup lang="ts">
import { WorkflowNode } from '@/types/workflow';
import Button from 'primevue/button';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { DecapodesOperationState } from './decapodes-operation';

defineProps<{
	node: WorkflowNode<DecapodesOperationState>;
}>();

const emit = defineEmits(['open-drilldown']);
</script>
