<template>
	<div class="mad-libs-container">
		Consider how sensitive the
		<Dropdown
			:disabled="_.isEmpty(selectedOptions)"
			placeholder="Select method"
			:model-value="sensitivityOptions.method"
			:options="Object.values(SensitivityMethod)"
			@change="onUpdateSensitivitySettings('method', $event.value, sensitivityOptions)"
		/>
		<tera-input-number
			v-if="sensitivityOptions.method === SensitivityMethod.TIMEPOINT"
			:disabled="_.isEmpty(selectedOptions)"
			:model-value="sensitivityOptions.timepoint"
			auto-width
			@update:model-value="onUpdateSensitivitySettings('timepoint', $event, sensitivityOptions)"
		/>
		of the outcome(s) of interest
		<tera-chart-control
			class="overflow-hidden"
			:chart-config="{
				selectedVariable: selectedOptions ?? []
			}"
			:multi-select="true"
			:show-remove-button="false"
			:variables="selectOptions"
			@configuration-change="$emit('selection-change', $event.selectedVariable)"
		/>
		is to the model parameter(s)
		<MultiSelect
			class="overflow-hidden"
			:disabled="_.isEmpty(selectedOptions)"
			placeholder="Select parameters"
			:model-value="sensitivityOptions.selectedInputVariables"
			:options="sensitivityOptions.inputOptions"
			@change="onUpdateSensitivitySettings('selectedInputVariables', $event.value, sensitivityOptions)"
			filter
		>
			<template v-slot:value>
				<template v-for="(variable, index) in sensitivityOptions.selectedInputVariables" :key="index">
					<template v-if="index > 0">,&nbsp;</template>
					<span> {{ variable }} </span>
				</template>
			</template>
		</MultiSelect>
	</div>
	<div v-for="option in sensitivityChartOptions" class="flex align-items-center gap-2 mt-1" :key="option.value">
		<RadioButton
			:disabled="_.isEmpty(selectedOptions)"
			:model-value="sensitivityOptions.chartType"
			:value="option.value"
			name="sensitivityChartTypes"
			@change="onUpdateSensitivitySettings('chartType', option.value, sensitivityOptions)"
		/>
		<label :class="{ disabled: _.isEmpty(selectedOptions) }" :for="option.value">{{ option.label }}</label>
	</div>
</template>

<script setup lang="ts">
import { ChartSetting, SensitivityChartType, SensitivityMethod } from '@/types/common';
import _ from 'lodash';
import Dropdown from 'primevue/dropdown';
import MultiSelect from 'primevue/multiselect';
import RadioButton from 'primevue/radiobutton';
import TeraChartControl from '@/components/workflow/tera-chart-control.vue';
import TeraInputNumber from './tera-input-number.vue';

const props = defineProps<{
	settings: ChartSetting[];
	selectOptions: string[];
	selectedOptions?: string[];
	sensitivityOptions: {
		inputOptions: string[];
		selectedInputVariables: string[];
		timepoint: number;
		chartType: SensitivityChartType;
		method: SensitivityMethod;
	};
}>();

const emits = defineEmits(['selection-change', 'sensitivity-selection-change']);

const sensitivityChartOptions = [
	{ label: 'Scatter', value: SensitivityChartType.SCATTER },
	{ label: 'Heatmap', value: SensitivityChartType.HEATMAP }
];

const onUpdateSensitivitySettings = (key: string, value: any, sensitivityOptions: typeof props.sensitivityOptions) => {
	emits('sensitivity-selection-change', {
		...sensitivityOptions,
		[key]: value
	});
};
</script>

<style scoped>
.mad-libs-container {
	display: flex;
	flex-wrap: wrap;
	align-items: center;
	gap: var(--gap-2);
	border: 1px solid var(--gray-300);
	border-radius: var(--border-radius);
	padding: var(--gap-2);
	border-left: 4px solid var(--primary-color);
	background: var(--surface-0);
	box-shadow: 0 2px 4px -1px rgba(0, 0, 0, 0.08);
	overflow: hidden;
}

:deep(.p-multiselect),
:deep(.p-dropdown) {
	.p-multiselect-label,
	.p-dropdown-label {
		padding: var(--gap-1);
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
	}
}
</style>
