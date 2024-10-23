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
			v-for="s of settings.filter((s) => s.type === type)"
			:key="s.id"
			:settings="s"
			@open="$emit('open', s)"
			@remove="$emit('remove', s.id)"
		/>
	</div>
</template>

<script setup lang="ts">
import TeraChartSettingsItem from '@/components/widgets/tera-chart-settings-item.vue';
import TeraChartControl from '@/components/workflow/tera-chart-control.vue';
import { ChartSetting, ChartSettingType } from '@/types/common';

defineProps<{
	title: string;
	settings: ChartSetting[];
	type: ChartSettingType;
	// Dropdown select options
	selectOptions: string[];
	// Selected dropdown options
	selectedOptions: string[];
}>();
defineEmits(['open', 'remove', 'selection-change']);
</script>
<style scoped>
.chart-settings {
	display: flex;
	flex-direction: column;
	gap: var(--gap-2);
}
</style>
