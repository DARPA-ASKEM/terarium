<template>
	<main>
		<tera-operator-placeholder-graphic :operation-type="node.operationType" />
	</main>
</template>

<script setup lang="ts">
import TeraOperatorPlaceholderGraphic from '@/workflow/operator/tera-operator-placeholder-graphic.vue';
import { WorkflowNode } from '@/types/workflow';
import { CalibrationOperationStateCiemss } from './calibrate-operation';

defineProps<{
	node: WorkflowNode<CalibrationOperationStateCiemss>;
}>();
</script>

<style scoped></style>
