<template>
	<div class="chart-settings">
		<h5>{{ title }}</h5>
		<tera-chart-control
			:chart-config="{
				selectedRun: 'fixme',
				selectedVariable: selectedOptions
			}"
			:multi-select="true"
			:show-remove-button="false"
			:variables="selectOptions"
			@configuration-change="$emit('selection-change', $event.selectedVariable, type)"
		/>
		<template v-if="type === ChartSettingType.SENSITIVITY && sensitivityOptions">
			<!--FIXME: It might be better to move these inside the panel so that they can be controlled at an individual chart settings level -->
			<label>Select parameter(s) of interest</label>
			<MultiSelect
				:disabled="_.isEmpty(selectedOptions)"
				placeholder="Select parameters"
				:model-value="sensitivityOptions.selectedInputOptions"
				:options="sensitivityOptions.inputOptions"
				@change="
					$emit('sensitivity-selection-change', {
						selectedInputVariables: $event.value,
						timepoint: sensitivityOptions.timepoint
					})
				"
			/>

			<label>Select time slice of interest</label>
			<tera-input-number
				:disabled="_.isEmpty(selectedOptions)"
				:model-value="sensitivityOptions.timepoint"
				@update:model-value="
					$emit('sensitivity-selection-change', {
						selectedInputVariables: sensitivityOptions.selectedInputOptions,
						timepoint: $event
					})
				"
			/>
		</template>
		<tera-chart-settings-item
			v-for="s of targetSettings"
			:key="s.id"
			:settings="s"
			@open="$emit('open', s)"
			@remove="$emit('remove', s.id)"
		/>
		<template v-if="type === ChartSettingType.VARIABLE_ENSEMBLE">
			<tera-checkbox
				:disabled="selectedOptions.length === 0"
				label="Show individual models"
				:model-value="Boolean(ensembleChartOptions.showIndividualModels)"
				@update:model-value="toggleEnsembleChartOption('showIndividualModels', $event)"
			/>
			<!-- Disabling following two checkboxes for now since their functionalities aren't implemented yet -->
			<!-- <tera-checkbox
				class="pl-5"
				:disabled="selectedOptions.length === 0"
				label="Relative to ensemble"
				:model-value="Boolean(ensembleChartOptions.relativeToEnsemble)"
				@update:model-value="toggleEnsembleChartOption('relativeToEnsemble', $event)"
			/>
			<tera-checkbox
				v-if="isSimulateEnsembleSettings"
				label="Show individual models with weights"
				:disabled="selectedOptions.length === 0"
				:model-value="Boolean(ensembleChartOptions.showIndividualModelsWithWeight)"
				@update:model-value="toggleEnsembleChartOption('showIndividualModelsWithWeight', $event)"
			/> -->
		</template>
	</div>
</template>

<script setup lang="ts">
import TeraCheckbox from '@/components/widgets/tera-checkbox.vue';
import TeraChartSettingsItem from '@/components/widgets/tera-chart-settings-item.vue';
import TeraChartControl from '@/components/workflow/tera-chart-control.vue';
import MultiSelect from 'primevue/multiselect';
import { ChartSetting, ChartSettingType } from '@/types/common';
import { computed } from 'vue';
import { EnsembleVariableChartSettingOption, getEnsembleChartSettingOptions } from '@/services/chart-settings';
import _ from 'lodash';
import TeraInputNumber from './tera-input-number.vue';

const props = defineProps<{
	title: string;
	settings: ChartSetting[];
	type: ChartSettingType;
	// Dropdown select options
	selectOptions: string[];
	// Selected dropdown options
	selectedOptions: string[];
	isSimulateEnsembleSettings?: boolean;
	sensitivityOptions?: {
		inputOptions: string[];
		selectedInputOptions: string[];
		timepoint: number;
	};
}>();
const emits = defineEmits([
	'open',
	'remove',
	'selection-change',
	'toggle-ensemble-variable-setting-option',
	'sensitivity-selection-change'
]);

// Settings of the same type that we want to interact with.
const targetSettings = computed(() => props.settings.filter((s) => s.type === props.type));

// ------------------- Ensemble chart options -------------------
const ensembleChartOptions = computed(() => getEnsembleChartSettingOptions(targetSettings.value));
const toggleEnsembleChartOption = (option: EnsembleVariableChartSettingOption, value: boolean) => {
	emits('toggle-ensemble-variable-setting-option', option, value);
};
</script>
<style scoped>
.chart-settings {
	display: flex;
	flex-direction: column;
	gap: var(--gap-2);
}
</style>
