<template>
	<tera-scenario-template :scenario-class="SensitivityAnalysisScenario" :scenario-instance="scenario">
		<template #inputs>
			<label>Select a model</label>
			<Dropdown
				:model-value="scenario.modelSpec.id"
				:options="models"
				option-label="assetName"
				option-value="assetId"
				placeholder="Select a model"
				@update:model-value="scenario.setModelSpec($event)"
			/>

			<label>Select configuration representing best and generous estimates of the initial conditions</label>
			<Dropdown
				:model-value="scenario.modelConfigSpec.id"
				placeholder="Select a configuration"
				:options="modelConfigurations"
				option-label="name"
				option-value="id"
				@update:model-value="scenario.setModelConfigSpec($event)"
				:disabled="isEmpty(modelConfigurations) || isFetchingModelInformation"
			/>
		</template>
		<template #outputs>
			<label>Select an output metric</label>
			<MultiSelect
				:disabled="isEmpty(modelStateOptions) || isFetchingModelInformation"
				:model-value="scenario.simulateSpec.ids"
				placeholder="Select output metrics"
				option-label="name"
				option-value="id"
				:options="modelStateOptions"
				@update:model-value="scenario.setCalibrateSpec($event)"
				filter
			/>
			<img :src="simulate" alt="Simulate chart" />
		</template>
	</tera-scenario-template>
</template>

<script setup lang="ts">
import { SensitivityAnalysisScenario } from '@/components/workflow/scenario-templates/sensitivity-analysis/sensitivity-analysis-scenario';
import { useProjects } from '@/composables/project';
import { getModel, getModelConfigurationsForModel } from '@/services/model';
import { AssetType, ModelConfiguration } from '@/types/Types';
import { isEmpty } from 'lodash';
import { computed, ref, watch } from 'vue';
import Dropdown from 'primevue/dropdown';
import MultiSelect from 'primevue/multiselect';
import simulate from '@/assets/svg/template-images/calibration-thumbnail.svg';
import TeraScenarioTemplate from '../tera-scenario-template.vue';

// FIXME: need an image for this scenario, reusing the calivration image for now

const isFetchingModelInformation = ref(false);
const models = computed(() => useProjects().getActiveProjectAssets(AssetType.Model));

const modelConfigurations = ref<ModelConfiguration[]>([]);
const modelStateOptions = ref<any[]>([]);

const props = defineProps<{
	scenario: SensitivityAnalysisScenario;
}>();

watch(
	() => props.scenario.modelSpec.id,
	async (modelId) => {
		if (!modelId) return;
		isFetchingModelInformation.value = true;
		const model = await getModel(modelId);
		if (!model) return;
		modelConfigurations.value = await getModelConfigurationsForModel(modelId);

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
