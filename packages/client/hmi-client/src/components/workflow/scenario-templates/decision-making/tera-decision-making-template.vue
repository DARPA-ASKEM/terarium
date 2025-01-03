<template>
	<tera-scenario-template :header="header" :scenario-instance="scenario" @save-workflow="emit('save-workflow')">
		<template #inputs>
			<label>Select a model</label>
			<Dropdown
				:model-value="scenario.modelSpec.id"
				:options="models"
				option-label="assetName"
				option-value="assetId"
				placeholder="Select a model"
				@update:model-value="scenario.setModelSpec($event)"
				class="mb-3"
			/>

			<label>Select configuration representing best starting point pre-interventions</label>
			<Dropdown
				class="mb-3"
				:model-value="scenario.modelConfigSpec.id"
				placeholder="Select a configuration"
				:options="modelConfigurations"
				option-label="name"
				option-value="id"
				@update:model-value="scenario.setModelConfigSpec($event)"
				:disabled="isEmpty(modelConfigurations) || isFetchingModelInformation"
				:loading="isFetchingModelInformation"
			/>

			<template v-for="(intervention, i) in scenario.interventionSpecs" :key="intervention">
				<label>Select intervention policy {{ i + 1 }}</label>
				<div class="flex">
					<Dropdown
						class="flex-1 mb-3"
						:model-value="intervention.id"
						placeholder="Select an intervention policy"
						:options="interventionPolicies"
						option-label="name"
						option-value="id"
						@update:model-value="scenario.setInterventionSpecs($event, i)"
						:disabled="isEmpty(interventionPolicies) || isFetchingModelInformation"
						:loading="isFetchingModelInformation"
					/>
					<Button
						v-if="scenario.interventionSpecs.length > 1"
						text
						icon="pi pi-trash"
						size="small"
						@click="scenario.removeInterventionSpec(i)"
					/>
				</div>
			</template>
			<div>
				<Button
					class="my-2"
					text
					icon="pi pi-plus"
					label="Add a new intervention"
					size="small"
					@click="scenario.addInterventionSpec()"
				/>
			</div>
		</template>

		<template #outputs>
			<label>Select an output metric</label>
			<MultiSelect
				:disabled="isEmpty(modelStateOptions) || isFetchingModelInformation"
				:model-value="scenario.simulateSpec.ids"
				placeholder="Select output metrics"
				option-label="id"
				option-value="id"
				:options="modelStateOptions"
				@update:model-value="scenario.setSimulateSpec($event)"
				:loading="isFetchingModelInformation"
				filter
			/>
		</template>
	</tera-scenario-template>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { AssetType, InterventionPolicy, ModelConfiguration } from '@/types/Types';
import { useProjects } from '@/composables/project';
import { getInterventionPoliciesForModel, getModel, getModelConfigurationsForModel } from '@/services/model';
import { isEmpty } from 'lodash';
import Dropdown from 'primevue/dropdown';
import MultiSelect from 'primevue/multiselect';
import Button from 'primevue/button';
import { ScenarioHeader } from '../base-scenario';
import { DecisionMakingScenario } from './decision-making-scenario';
import TeraScenarioTemplate from '../tera-scenario-template.vue';

const header: ScenarioHeader = Object.freeze({
	title: 'Decision Making Template',
	question: 'What is the impact of different interventions?',
	description:
		'Runs a simulation for the baseline (no intervention) and each intervention policy and then shows the relative impact of each intervention policy relative to the baseline.',
	examples: [
		'What is the impact of several combinations of vaccination and NPI levels?',
		'Which is better: implement an intervention in all locations, select locations, or not at all?'
	]
});
const isFetchingModelInformation = ref(false);
const models = computed(() => useProjects().getActiveProjectAssets(AssetType.Model));

const modelConfigurations = ref<ModelConfiguration[]>([]);
const interventionPolicies = ref<InterventionPolicy[]>([]);
const modelStateOptions = ref<any[]>([]);

const props = defineProps<{
	scenario: DecisionMakingScenario;
}>();

const emit = defineEmits(['save-workflow']);

watch(
	() => props.scenario.modelSpec.id,
	async (modelId) => {
		if (!modelId) return;
		isFetchingModelInformation.value = true;
		const model = await getModel(modelId);
		if (!model) return;

		await Promise.all([getModelConfigurationsForModel(modelId), getInterventionPoliciesForModel(modelId)]).then(
			([mc, ip]) => {
				modelConfigurations.value = mc;
				interventionPolicies.value = ip;
			}
		);

		// Set the first model configuration as the default
		if (!isEmpty(modelConfigurations.value)) {
			props.scenario.setModelConfigSpec(modelConfigurations.value[0].id!);
		}

		const modelOptions: any[] = model.model.states;

		model.semantics?.ode.observables?.forEach((o) => {
			modelOptions.push(o);
		});
		modelStateOptions.value = modelOptions;
		isFetchingModelInformation.value = false;
	},
	{ immediate: true }
);
</script>
