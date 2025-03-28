<template>
	<tera-scenario-template
		ref="blankTemplate"
		:header="header"
		:scenario-instance="scenario"
		@save-workflow="emit('save-workflow')"
	>
		<template #inputs>
			<label>Select a model</label>
			<Dropdown
				:model-value="props.scenario.getModelId()"
				:options="allModelOptions"
				option-label="assetName"
				option-value="assetId"
				placeholder="Select a model"
				@update:model-value="props.scenario.setModelId($event)"
				class="mb-3"
			/>
		</template>
		<template #outputs> </template>
	</tera-scenario-template>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { AssetType } from '@/types/Types';
import { useProjects } from '@/composables/project';
import Dropdown from 'primevue/dropdown';
import { ScenarioHeader } from '../base-scenario';
import { CounterfactualScenario } from './counterfactual-scenario';
import TeraScenarioTemplate from '../tera-scenario-template.vue';

const props = defineProps<{
	scenario: CounterfactualScenario;
}>();

const header: ScenarioHeader = Object.freeze({
	title: 'Necessary or sufficient analysis template',
	question: 'How likely would we arrive in a different state, if specific choices had been made differently?',
	description:
		'Requires data to calibrate against, intervention points in the past and binary test about the end state. This is a type of counterfactual analysis.',
	examples: [
		'Would we have 10% fewer cases now if vaccinations had been 5% higher a month ago?',
		'Would the storm winds have been at least 10mph less if the ocean temperature were 1 degree lower?'
	]
});

const allModelOptions = computed(() => useProjects().getActiveProjectAssets(AssetType.Model));

const emit = defineEmits(['save-workflow']);
</script>
