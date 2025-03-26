<template>
	<tera-scenario-template :header="header" :scenario-instance="scenario" @save-workflow="emit('save-workflow')" />
	<img :src="blankCanvas" alt="Blank canvas template" />
</template>

<script setup lang="ts">
import blankCanvas from '@/assets/svg/template-images/blank-canvas-thumbnail.svg';
import TeraScenarioTemplate from '../tera-scenario-template.vue';
import { ScenarioHeader } from '../base-scenario';
import { PolicyDesignScenario } from './policy-design-scenario';

const header: ScenarioHeader = Object.freeze({
	title: 'Policy design template',
	question: 'Search for a policy/intervention that achieves a desired outcome',
	description:
		'Requires a desired future outcome state and a variable to intervene on. The system will attempt to optimize the variable to achieve the desired outcome state and provide an intervention for that variable (if possible). If there is data to calibrate against, the suggested intervention will be more accurate. This is a type of counterfactual analysis.',
	examples: [
		'What percentage vaccination is required to have hospitalizations below 5% of the population in three months?',
		'How much would gas taxes need to raise to reduce driving by 5% by October?',
		'How much would property taxes need to lower to increase investment by %5 in one year?'
	]
});
defineProps<{
	scenario: PolicyDesignScenario;
}>();
const emit = defineEmits(['save-workflow']);
</script>
