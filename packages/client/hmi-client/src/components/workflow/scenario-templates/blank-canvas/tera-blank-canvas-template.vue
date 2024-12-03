<template>
	<tera-scenario-template
		ref="blankTemplate"
		:header="header"
		:scenario-instance="scenario"
		@save-workflow="emit('save-workflow')"
	/>
	<img :src="blankCanvas" alt="Blank canvas template" />
</template>

<script setup lang="ts">
import blankCanvas from '@/assets/svg/template-images/blank-canvas-thumbnail.svg';
import TeraScenarioTemplate from '../tera-scenario-template.vue';
import { BlankCanvasScenario } from './blank-canvas-scenario';
import { ScenarioHeader } from '../base-scenario';

const header: ScenarioHeader = Object.freeze({
	title: 'Blank canvas template'
});
defineProps<{
	scenario: BlankCanvasScenario;
}>();
const emit = defineEmits(['save-workflow']);
</script>
