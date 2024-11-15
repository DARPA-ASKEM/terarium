<template>
	<label>What would you like to call this workflow?</label>
	<tera-input-text :model-value="scenario.workflowName" @update:model-value="scenario.setWorkflowName($event)" />
</template>

<script setup lang="ts">
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import { BlankCanvasScenario } from './blank-canvas-scenario';

defineProps<{
	scenario: BlankCanvasScenario;
}>();
</script>
