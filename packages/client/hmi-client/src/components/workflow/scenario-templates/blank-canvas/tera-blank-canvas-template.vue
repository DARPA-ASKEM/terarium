<template>
	<tera-scenario-template ref="blankTemplate" :scenario-class="BlankCanvasScenario" :scenario-instance="scenario" />
	<img :src="blankCanvas" alt="Blank canvas template" />
</template>

<script setup lang="ts">
import blankCanvas from '@/assets/svg/template-images/blank-canvas-thumbnail.svg';
import TeraScenarioTemplate from '../tera-scenario-template.vue';
import { BlankCanvasScenario } from './blank-canvas-scenario';

defineProps<{
	scenario: BlankCanvasScenario;
}>();
</script>
