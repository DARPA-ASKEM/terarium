<template>
	<div class="flex flex-column gap-2">
		<label>{{ label }}</label>
		<tera-input-number
			:model-value="modelValue"
			:disabled="disabled"
			@update:model-value="emit('update:model-value', $event)"
		/>
		<Calendar
			v-if="startDate"
			:model-value="date"
			showIcon
			iconDisplay="input"
			:view="calendarView"
			:date-format="calendarSettings?.format"
			:disabled="disabled"
			@date-select="onDateSelect($event)"
		/>
	</div>
</template>

<script setup lang="ts">
import Calendar from 'primevue/calendar';
import { CalendarSettings, getEndDateFromTimestep, getTimestepFromDateRange } from '@/utils/date';
import { computed } from 'vue';
import { CalendarDateType } from '@/types/common';
import teraInputNumber from './tera-input-number.vue';

const props = defineProps<{
	label: string;
	modelValue: number;
	startDate: Date | undefined;
	calendarSettings?: CalendarSettings;
	disabled?: boolean;
}>();

const emit = defineEmits(['update:model-value']);

const date = computed(() => {
	if (!props.startDate) return null;
	return getEndDateFromTimestep(
		props.startDate,
		props.modelValue,
		props.calendarSettings?.view ?? CalendarDateType.DATE
	);
});

const calendarView = computed(() => {
	if (props.calendarSettings?.view === CalendarDateType.WEEK) {
		return CalendarDateType.DATE;
	}
	return props.calendarSettings?.view;
});

const onDateSelect = (newDate: Date) => {
	if (!props.startDate) return;
	emit(
		'update:model-value',
		getTimestepFromDateRange(props.startDate, newDate, props.calendarSettings?.view ?? CalendarDateType.DATE)
	);
};
</script>
