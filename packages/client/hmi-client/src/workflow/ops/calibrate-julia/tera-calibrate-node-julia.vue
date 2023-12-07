<template>
	<main>
		<tera-operator-placeholder-graphic :operation-type="node.operationType" />
	</main>
</template>

<script setup lang="ts">
import TeraOperatorPlaceholderGraphic from '@/workflow/operator/tera-operator-placeholder-graphic.vue';
import { WorkflowNode } from '@/types/workflow';
import { CalibrationOperationStateJulia } from './calibrate-operation';

// TODO: FIGURE OUT WHAT TO SHOW IN THIS WORKFLOW NODE WHILE CALIBRATION IS RUNNING FROM THE DRILLDOWN
defineProps<{
	node: WorkflowNode<CalibrationOperationStateJulia>;
}>();
</script>

<style scoped></style>
