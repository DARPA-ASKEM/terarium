<template>
	<div class="chart-settings">
		<h5 v-if="title">{{ title }}</h5>
		<!-- Ensemble chart options -->
		<template v-if="type === ChartSettingType.VARIABLE_ENSEMBLE">
			<tera-checkbox
				:disabled="_.isEmpty(selectedOptions)"
				label="Show individual models"
				:model-value="Boolean(ensembleChartOptions?.showIndividualModels)"
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

		<!-- Original chart control for non-comparison charts -->
		<tera-chart-control
			v-if="type !== ChartSettingType.VARIABLE_COMPARISON && type !== ChartSettingType.SENSITIVITY"
			:chart-config="{
				selectedVariable: selectedOptions ?? []
			}"
			:multi-select="true"
			:show-remove-button="false"
			:variables="selectOptions"
			@configuration-change="$emit('selection-change', $event.selectedVariable, type)"
		/>

		<slot></slot>

		<!-- Sensitivity analysis settings -->
		<tera-sensitivity-chart-settings
			v-if="type === ChartSettingType.SENSITIVITY && sensitivityOptions"
			:settings="settings"
			:selectOptions="selectOptions"
			:selectedOptions="selectedOptions"
			:sensitivityOptions="sensitivityOptions"
			@selection-change="$emit('selection-change', $event, type)"
			@sensitivity-selection-change="$emit('sensitivity-selection-change', $event)"
		/>
		<template v-if="type === ChartSettingType.VARIABLE_COMPARISON">
			<section>
				<tera-chart-settings-item-comparison
					:settings="settings"
					:type="type"
					:selectOptions="selectOptions"
					:selectedOptions="selectedOptions"
					:comparisonSelectedOptions="comparisonSelectedOptions"
					@comparison-selection-change="$emit('comparison-selection-change', $event.id, $event.value)"
					@open="$emit('open', $event)"
					@remove="$emit('remove', $event)"
				/>
			</section>
		</template>
		<template v-else>
			<tera-chart-settings-item
				v-for="s of targetSettings"
				:key="s.id"
				:settings="s"
				@open="$emit('open', s)"
				@remove="$emit('remove', s.id)"
			/>
		</template>
	</div>
</template>

<script setup lang="ts">
import TeraCheckbox from '@/components/widgets/tera-checkbox.vue';
import TeraChartSettingsItem from '@/components/widgets/tera-chart-settings-item.vue';
import TeraChartSettingsItemComparison from '@/components/widgets/tera-chart-settings-item-comparison.vue';
import TeraChartControl from '@/components/workflow/tera-chart-control.vue';
import { ChartSetting, ChartSettingType, SensitivityChartType, SensitivityMethod } from '@/types/common';
import { computed } from 'vue';
import { EnsembleVariableChartSettingOption, getEnsembleChartSettingOptions } from '@/services/chart-settings';
import _ from 'lodash';
import teraSensitivityChartSettings from './tera-sensitivity-chart-settings.vue';

const props = defineProps<{
	title?: string; // Optional title for the settings panel
	settings: ChartSetting[];
	type: ChartSettingType;
	/**
	 * Available dropdown select options.
	 */
	selectOptions: string[];
	/**
	 * Selected dropdown options.
	 */
	selectedOptions?: string[];
	/**
	 * Selected dropdown options for comparison charts
	 */
	comparisonSelectedOptions?: { [settingId: string]: string[] };
	isSimulateEnsembleSettings?: boolean;
	sensitivityOptions?: {
		inputOptions: string[];
		selectedInputVariables: string[];
		timepoint: number;
		chartType: SensitivityChartType;
		method: SensitivityMethod;
	};
}>();
const emits = defineEmits([
	'open',
	'remove',
	'selection-change',
	'toggle-ensemble-variable-setting-option',
	'sensitivity-selection-change',
	'comparison-selection-change'
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
.disabled {
	color: var(--gray-400);
}
.settings-item {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: var(--gap-2) var(--gap-3);
	background: var(--surface-0);
	border-left: 4px solid var(--primary-color);
	border-radius: var(--border-radius);
	margin-top: var(--gap-2);
}
.settings-item .content {
	background: transparent;
	border: transparent;
	width: 16rem;
}
.content {
	flex: 1;
}
.actions {
	display: flex;
	gap: var(--gap-2);
}
</style>
