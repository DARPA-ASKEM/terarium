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

			<label>Select a model configuration</label>
			<Dropdown
				:model-value="props.scenario.getModelConfigId()"
				:options="allModelOptions"
				option-label="assetName"
				option-value="assetId"
				placeholder="Select a model"
				@update:model-value="props.scenario.setModelConfigId($event)"
				class="mb-3"
			/>

			<label>Select a dataset</label>
			<Dropdown
				:model-value="props.scenario.getDatasetId()"
				:options="allDatasetOptions"
				option-label="assetName"
				option-value="assetId"
				placeholder="Select a dataset"
				@update:model-value="props.scenario.setDatasetId($event)"
				class="mb-3"
			/>
		</template>
		<template #outputs> </template>
	</tera-scenario-template>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { AssetType, InterventionPolicy, Model, ModelConfiguration } from '@/types/Types';
import { useProjects } from '@/composables/project';
import Dropdown from 'primevue/dropdown';
import _ from 'lodash';
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
const allDatasetOptions = computed(() => useProjects().getActiveProjectAssets(AssetType.Dataset));
const selectedModelId = computed(() => props.scenario.getModelId());
const selectedModelConfigurationId = computed(() => props.scenario.getModelConfigId());
const isModelSelected = computed(() => !_.isEmpty(selectedModelId.value));
const model = ref<Model | null>(null);
const modelConfiguration = ref<ModelConfiguration>();
const allModelConfigOptions = ref<ModelConfiguration[]>([]);
const allInterventionPolicyOptions = ref<InterventionPolicy[]>([]);
const isFetchingModelData = ref<boolean>(false);
const modelStateAndObsOptions = ref<string[]>([]);

const emit = defineEmits(['save-workflow']);
</script>
