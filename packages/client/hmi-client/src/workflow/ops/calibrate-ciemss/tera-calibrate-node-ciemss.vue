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
			Connect a model configuration and dataset
		</tera-operator-placeholder>
	</main>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { WorkflowNode } from '@/types/workflow';
import Button from 'primevue/button';
import { CalibrationOperationStateCiemss } from './calibrate-operation';

const props = defineProps<{
	node: WorkflowNode<CalibrationOperationStateCiemss>;
}>();

const areInputsFilled = computed(() => props.node.inputs[0].value && props.node.inputs[1].value);

const emit = defineEmits(['open-drilldown']);
</script>

<style scoped></style>
