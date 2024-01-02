<template>
	<main>
		<Button
			v-if="areInputsFilled"
			label="Configure"
			@click="emit('open-drilldown')"
			severity="secondary"
			outlined
		/>
		<tera-operator-placeholder v-else :operation-type="node.operationType">
			Connect a model configuration
		</tera-operator-placeholder>
	</main>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import Button from 'primevue/button';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { WorkflowNode } from '@/types/workflow';
import { SimulateCiemssOperationState } from './simulate-ciemss-operation';

const props = defineProps<{
	node: WorkflowNode<SimulateCiemssOperationState>;
}>();

const emit = defineEmits(['open-drilldown']);

const areInputsFilled = computed(() => props.node.inputs[0].value);
</script>

<style scoped></style>
