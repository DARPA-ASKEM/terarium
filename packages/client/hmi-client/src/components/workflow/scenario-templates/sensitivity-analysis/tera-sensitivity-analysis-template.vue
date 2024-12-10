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
				:loading="isFetchingModelInformation"
			/>
			<label>Select uncertain parameters of interest and adjust ranges to be explored if needed</label>
			<template v-for="(parameter, i) in scenario.parameters" :key="i">
				<div class="flex">
					<Dropdown
						class="flex-1"
						:model-value="parameter?.referenceId"
						:options="modelParameters"
						option-label="referenceId"
						option-value="referenceId"
						placeholder="Select a parameter"
						:disabled="!selectedModelConfiguration"
						:loading="isFetchingModelConfiguration || isFetchingModelInformation"
						@update:model-value="onParameterSelect($event, i)"
					>
						<template #option="slotProps">
							<span>{{ displayParameter(slotProps.option.referenceId) }}</span>
						</template>

						<template #value="slotProps">
							<span v-if="displayParameter(slotProps.value)">{{ displayParameter(slotProps.value) }}</span>
							<span v-else>{{ slotProps.placeholder }}</span>
						</template>
					</Dropdown>
					<Button v-if="scenario.parameters.length > 1" icon="pi pi-trash" text @click="scenario.removeParameter(i)" />
				</div>
				<div v-if="parameter" class="distribution-container">
					<label class="p-0">Min:</label>
					<tera-input-number class="m-0" v-model="parameter.distribution.parameters.minimum" />
					<label class="p-0 ml-2">Max:</label>
					<tera-input-number class="m-0" v-model="parameter.distribution.parameters.maximum" />
				</div>
			</template>
			<div>
				<Button
					:disabled="!scenario.modelSpec.id"
					class="mt-2"
					label="Add parameter"
					icon="pi pi-plus"
					text
					@click="scenario.addParameter()"
				/>
			</div>
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
				:loading="isFetchingModelInformation"
				filter
			/>
			<!-- <img :src="simulate" alt="Simulate chart" /> -->
		</template>
	</tera-scenario-template>
</template>

<script setup lang="ts">
import { SensitivityAnalysisScenario } from '@/components/workflow/scenario-templates/sensitivity-analysis/sensitivity-analysis-scenario';
import { useProjects } from '@/composables/project';
import { getModel, getModelConfigurationsForModel } from '@/services/model';
import { AssetType, ModelConfiguration, ParameterSemantic } from '@/types/Types';
import _, { isEmpty } from 'lodash';
import { computed, ref, watch } from 'vue';
import Dropdown from 'primevue/dropdown';
import MultiSelect from 'primevue/multiselect';
import Button from 'primevue/button';
import { getModelConfigurationById, getParameter, getParameters } from '@/services/model-configurations';
import { DistributionType } from '@/services/distribution';
import TeraInputNumber from '@/components/widgets/tera-input-number.vue';
import { ScenarioHeader } from '../base-scenario';
import TeraScenarioTemplate from '../tera-scenario-template.vue';

const header: ScenarioHeader = Object.freeze({
	title: 'Sensitivity analysis template',
	question: 'Which parameters introduces the most uncertainty in the outcomes of interest?',
	description:
		'Configure the model with parameter distributions that reflect all the sources of uncertainty, then simulate into the near future.',
	examples: ['Unknown severity of new variant.', 'Unknown speed of waning immunity.']
});

const isFetchingModelInformation = ref(false);
const isFetchingModelConfiguration = ref(false);

const models = computed(() => useProjects().getActiveProjectAssets(AssetType.Model));

const modelConfigurations = ref<ModelConfiguration[]>([]);
const modelStateOptions = ref<any[]>([]);

const selectedModelConfiguration = ref<ModelConfiguration | null>(null);
const modelParameters = ref<ParameterSemantic[]>([]);

const props = defineProps<{
	scenario: SensitivityAnalysisScenario;
}>();

const emit = defineEmits(['save-workflow']);

const onParameterSelect = (parameterId: string, index: number) => {
	if (!selectedModelConfiguration.value) return;
	const parameter = _.cloneDeep(getParameter(selectedModelConfiguration.value, parameterId));
	if (!parameter) return;
	props.scenario.setParameter(parameter, index);
};

const displayParameter = (parameterName: string) => {
	let value = '';
	const parameter = modelParameters.value.find((p) => p.referenceId === parameterName);
	switch (parameter?.distribution.type) {
		case DistributionType.Constant:
			value = `${parameter.distribution.parameters.value}`;
			break;
		case DistributionType.Uniform:
			value = `${parameter.distribution.parameters.minimum} - ${parameter.distribution.parameters.maximum}`;
			break;
		default:
			return '';
	}

	return `${parameterName}  [${value}]`;
};

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

watch(
	() => props.scenario.modelConfigSpec.id,
	async (modelConfigId) => {
		if (!modelConfigId) {
			selectedModelConfiguration.value = null;
			return;
		}
		isFetchingModelConfiguration.value = true;
		selectedModelConfiguration.value = await getModelConfigurationById(modelConfigId);
		if (!selectedModelConfiguration.value) return;
		modelParameters.value = getParameters(selectedModelConfiguration.value);
		isFetchingModelConfiguration.value = false;
	}
);
</script>

<style scoped>
.distribution-container {
	display: flex;
	align-items: center;
	padding: var(--gap-2) var(--gap-1);
	margin: var(--gap-0-5) 0;
	background-color: var(--surface-100);
}
</style>
