<template>
	<label>What would you like to call this workflow?</label>
	<tera-input-text
		ref="nameInput"
		:model-value="scenario.workflowName"
		@update:model-value="scenario.setWorkflowName($event)"
		auto-focus
		@keydown.enter.stop.prevent="emit('save-workflow')"
	/>

	<img :src="blankCanvas" alt="Blank canvas template" />
</template>

<script setup lang="ts">
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import blankCanvas from '@/assets/svg/template-images/blank-canvas-thumbnail.svg';
import { BlankCanvasScenario } from './blank-canvas-scenario';

const emit = defineEmits(['save-workflow']);
defineProps<{
	scenario: BlankCanvasScenario;
}>();
</script>
