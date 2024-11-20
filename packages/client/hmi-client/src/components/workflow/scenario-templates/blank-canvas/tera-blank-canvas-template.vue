<template>
	<label>What would you like to call this workflow?</label>
	<tera-input-text
		ref="nameInput"
		:model-value="scenario.workflowName"
		@update:model-value="scenario.setWorkflowName($event)"
		auto-focus
		@keydown.enter.stop.prevent="emit('save-workflow')"
	/>
</template>

<script setup lang="ts">
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import { BlankCanvasScenario } from './blank-canvas-scenario';

const emit = defineEmits(['save-workflow']);
defineProps<{
	scenario: BlankCanvasScenario;
}>();
</script>
