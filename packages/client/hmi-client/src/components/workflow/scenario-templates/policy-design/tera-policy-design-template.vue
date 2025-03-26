<template>
	<tera-scenario-template :header="header" :scenario-instance="scenario" @save-workflow="emit('save-workflow')">
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

			<label>Select a model configuration</label>
			<Dropdown
				:disabled="!isModelSelected"
				:model-value="props.scenario.getModelConfigId()"
				:options="allModelConfigOptions"
				option-label="name"
				option-value="id"
				placeholder="Select a model configuration"
				@update:model-value="props.scenario.setModelConfigId($event)"
				class="mb-3"
			/>

			<label>Select an intervention</label>
			<Dropdown
				:disabled="!isModelSelected"
				:model-value="props.scenario.getInterventionPolicyId()"
				:options="allInterventionPolicyOptions"
				option-label="name"
				option-value="id"
				placeholder="Select an intervention for this model"
				@update:model-value="props.scenario.setInterventionPolicyId($event)"
				class="mb-3"
			/>

			<label>Select a dataset (optional)</label>
			<Dropdown
				:model-value="props.scenario.getDatasetId()"
				:options="allDatasetOptions"
				option-label="assetName"
				option-value="assetId"
				placeholder="Select a dataset (optional)"
				@update:model-value="props.scenario.setDatasetId($event)"
				class="mb-3"
			/>
		</template>
	</tera-scenario-template>
</template>

<script setup lang="ts">
import Dropdown from 'primevue/dropdown';
import { computed, ref, watch } from 'vue';
import { useProjects } from '@/composables/project';
import { AssetType, ModelConfiguration, InterventionPolicy } from '@/types/Types';
import { getInterventionPoliciesForModel, getModelConfigurationsForModel } from '@/services/model';
import _ from 'lodash';
import { PolicyDesignScenario } from './policy-design-scenario';
import { ScenarioHeader } from '../base-scenario';
import TeraScenarioTemplate from '../tera-scenario-template.vue';

const props = defineProps<{
	scenario: PolicyDesignScenario;
}>();

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

const allModelOptions = computed(() => useProjects().getActiveProjectAssets(AssetType.Model));
const allDatasetOptions = computed(() => useProjects().getActiveProjectAssets(AssetType.Dataset));
const selectedModelId = computed(() => props.scenario.getModelId());
const isModelSelected = computed(() => !_.isEmpty(selectedModelId.value));
const allModelConfigOptions = ref<ModelConfiguration[]>([]);
const allInterventionPolicyOptions = ref<InterventionPolicy[]>([]);

watch(
	() => selectedModelId,
	async () => {
		if (selectedModelId.value) {
			allModelConfigOptions.value = await getModelConfigurationsForModel(selectedModelId.value);
			allInterventionPolicyOptions.value = await getInterventionPoliciesForModel(selectedModelId.value);
			console.log(allInterventionPolicyOptions.value);
		}
	},
	{ deep: true }
);

const emit = defineEmits(['save-workflow']);
</script>
