<template>
	<tera-scenario-template :header="header" :scenario-instance="scenario" @save-workflow="emit('save-workflow')">
		<template #inputs>
			<label>Select a model</label>
			<Dropdown
				:model-value="selectedModelId"
				:options="models"
				option-label="assetName"
				option-value="assetId"
				placeholder="Select a model"
				@update:model-value="selectedModelId = $event"
				class="mb-3"
			/>
		</template>
	</tera-scenario-template>
</template>

<script setup lang="ts">
import Dropdown from 'primevue/dropdown';
import { computed, ref } from 'vue';
import { useProjects } from '@/composables/project';
import { AssetType } from '@/types/Types';
import { PolicyDesignScenario } from './policy-design-scenario';
import { ScenarioHeader } from '../base-scenario';
import TeraScenarioTemplate from '../tera-scenario-template.vue';

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
const props = defineProps<{
	scenario: PolicyDesignScenario;
}>();

const models = computed(() => useProjects().getActiveProjectAssets(AssetType.Model));
const selectedModelId = ref<string>();

const emit = defineEmits(['save-workflow']);
</script>
