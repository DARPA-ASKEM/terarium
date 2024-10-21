<template>
	<div class="flex flex-column gap-2">
		<label>{{ label }}</label>
		<tera-input-number
			:model-value="modelValue"
			:disabled="disabled"
			@update:model-value="emit('update:model-value', $event)"
		/>
		<Calendar
			v-if="modelConfiguration.temporalContext"
			:model-value="date"
			showIcon
			iconDisplay="input"
			:view="calendarSettings?.view"
			:date-format="calendarSettings?.format"
			:disabled="disabled"
			@date-select="onDateSelect($event)"
		/>
	</div>
</template>

<script setup lang="ts">
import { Model, ModelConfiguration } from '@/types/Types';
import Calendar from 'primevue/calendar';
import {
	CalendarSettings,
	getCalendarSettingsFromModel,
	getEndDateFromTimestep,
	getTimestepFromDateRange
} from '@/utils/date';
import { computed, ref } from 'vue';
import teraInputNumber from './tera-input-number.vue';

const props = defineProps<{
	label: string;
	modelValue: number;
	model: Model;
	modelConfiguration: ModelConfiguration;
	disabled?: boolean;
}>();

const emit = defineEmits(['update:model-value']);

const calendarSettings = ref<CalendarSettings>(getCalendarSettingsFromModel(props.model));

const date = computed(() => {
	if (!props.modelConfiguration.temporalContext) return null;
	return getEndDateFromTimestep(
		props.modelConfiguration.temporalContext,
		props.modelValue,
		calendarSettings.value?.view ?? 'date'
	);
});

const onDateSelect = (newDate: Date) => {
	if (!props.modelConfiguration.temporalContext) return;
	emit(
		'update:model-value',
		getTimestepFromDateRange(props.modelConfiguration.temporalContext, newDate, calendarSettings.value?.view ?? 'date')
	);
};
</script>
