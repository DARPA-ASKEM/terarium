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
		<tera-chart-settings-item
			v-for="s of targetSettings"
			:key="s.id"
			:settings="s"
			@open="$emit('open', s)"
			@remove="$emit('remove', s.id)"
		/>
		<template v-if="type === ChartSettingType.VARIABLE_ENSEMBLE">
			<tera-checkbox
				label="Show individual models"
				:model-value="Boolean(ensembleChartOptions.showIndividualModels)"
				@update:model-value="toggleEensembleChartOption('showIndividualModels', $event)"
			/>
			<tera-checkbox
				class="pl-5"
				label="Relative to ensemble"
				:model-value="Boolean(ensembleChartOptions.relativeToEnsemble)"
				@update:model-value="toggleEensembleChartOption('relativeToEnsemble', $event)"
			/>
			<tera-checkbox
				v-if="ensembleChartOptions.showIndividualModelsWithWeight !== undefined"
				label="Show individual models with weights"
				:model-value="ensembleChartOptions.showIndividualModelsWithWeight"
				@update:model-value="toggleEensembleChartOption('showIndividualModelsWithWeight', $event)"
			/>
		</template>
	</div>
</template>

<script setup lang="ts">
import TeraCheckbox from '@/components/widgets/tera-checkbox.vue';
import TeraChartSettingsItem from '@/components/widgets/tera-chart-settings-item.vue';
import TeraChartControl from '@/components/workflow/tera-chart-control.vue';
import { ChartSetting, ChartSettingEnsembleVariable, ChartSettingType } from '@/types/common';
import { computed } from 'vue';
import { isChartSettingEnsembleVariable } from '@/services/chart-settings';

const props = defineProps<{
	title: string;
	settings: ChartSetting[];
	type: ChartSettingType;
	// Dropdown select options
	selectOptions: string[];
	// Selected dropdown options
	selectedOptions: string[];
}>();
const emits = defineEmits(['open', 'remove', 'selection-change', 'toggle-ensemble-chart-option']);

// Settings of the same type that we want to interact with.
const targetSettings = computed(() => props.settings.filter((s) => s.type === props.type));

// ------------------- Ensemble chart options -------------------
// Extract ensemble variable chart options from the chart settings for each variable and merge into single option.
const ensembleChartOptions = computed(() => {
	// initial options
	const options: Partial<ChartSettingEnsembleVariable> = {
		showIndividualModels: false,
		relativeToEnsemble: false,
		showIndividualModelsWithWeight: undefined // only applicable for the simulate ensemble otherwise undefined
	};
	// Merge options from all ensemble variables since each variable setting has its own options but all controlled by the single UI.
	targetSettings.value.forEach((s) => {
		if (!isChartSettingEnsembleVariable(s)) return;
		options.showIndividualModels = options.showIndividualModels || s.showIndividualModels;
		options.relativeToEnsemble = options.relativeToEnsemble || s.relativeToEnsemble;
		options.showIndividualModelsWithWeight = options.showIndividualModelsWithWeight || s.showIndividualModelsWithWeight;
	});
	return options;
});
const toggleEensembleChartOption = (
	option: 'showIndividualModels' | 'relativeToEnsemble' | 'showIndividualModelsWithWeight',
	value: Boolean
) => {
	emits('toggle-ensemble-chart-option', props.type, option, value);
};
// ---------------------------------------------------------------
</script>
<style scoped>
.chart-settings {
	display: flex;
	flex-direction: column;
	gap: var(--gap-2);
}
</style>
