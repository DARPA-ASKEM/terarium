<template>
	<label>What would you like to call this workflow?</label>
	<tera-input-text
		:model-value="state.workflowName"
		@update:model-value="
			emit('update-state', {
				workflowName: $event
			})
		"
	/>
</template>

<script setup lang="ts">
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import { BlankCanvasScenarioTemplate } from './blank-canvas-template';

const emit = defineEmits(['update-state']);
defineProps<{
	state: BlankCanvasScenarioTemplate;
}>();
</script>
