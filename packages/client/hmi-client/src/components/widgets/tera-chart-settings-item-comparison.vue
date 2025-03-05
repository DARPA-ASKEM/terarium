<template>
	<section v-for="s of targetSettings" :key="s.id">
		<section class="settings-item">
			<div class="chart-control">
				<tera-chart-control
					:chart-config="{
						selectedRun: 'fixme',
						selectedVariable: (comparisonSelectedOptions ?? {})[s.id] ?? []
					}"
					:multi-select="true"
					:show-remove-button="false"
					:variables="selectOptions"
					@configuration-change="$emit('comparison-selection-change', { id: s.id, value: $event.selectedVariable })"
				/>
			</div>
			<div class="actions">
				<Button icon="pi pi-cog" text rounded @click="$emit('open', s)" />
				<Button icon="pi pi-times" text rounded @click="$emit('remove', s.id)" />
			</div>
		</section>
	</section>
</template>
<script setup lang="ts">
import TeraChartControl from '@/components/workflow/tera-chart-control.vue';
import { ChartSetting, ChartSettingType } from '@/types/common';
import { computed } from 'vue';
import Button from 'primevue/button';

const props = defineProps<{
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
}>();
defineEmits(['open', 'remove', 'comparison-selection-change']);

// Settings of the same type that we want to interact with.
const targetSettings = computed(() => props.settings.filter((s) => s.type === props.type));
</script>
<style scoped>
.settings-item {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: var(--gap-2) var(--gap-3);
	background: var(--surface-0);
	border-left: 4px solid var(--primary-color);
	margin-top: var(--gap-2);
}
.chart-control {
	flex: 1;
	width: 17.5rem;
	background: transparent;
	border: transparent;
	background: var(--surface-0);
}
.actions {
	display: flex;
	gap: var(--gap-1);
}
</style>
