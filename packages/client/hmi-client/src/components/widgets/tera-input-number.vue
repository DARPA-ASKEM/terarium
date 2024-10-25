<template>
	<div class="tera-input">
		<label v-if="label" @click.self.stop="focusInput">{{ label }}</label>
		<main :class="{ error: getErrorMessage }" @click.self.stop="focusInput">
			<i v-if="icon" :class="icon" />
			<input
				:class="$attrs.class"
				@click.stop
				ref="inputField"
				:disabled="getDisabled"
				:value="displayValue()"
				@input="updateValue"
				:style="inputStyle"
				@blur="onBlur"
				@focus="isFocused = true"
				@focusout="$emit('focusout', $event)"
				type="text"
				:placeholder="placeholder"
			/>
		</main>
		<aside v-if="getErrorMessage"><i class="pi pi-exclamation-circle" /> {{ getErrorMessage }}</aside>
	</div>
</template>

<script setup lang="ts">
import { numberToNist } from '@/utils/number';
import { isNaN, toNumber } from 'lodash';
import { CSSProperties, computed, ref, watch } from 'vue';

const props = defineProps<{
	modelValue: number | undefined;
	label?: string;
	icon?: string;
	errorMessage?: string;
	disabled?: boolean;
	placeholder?: string;
	autoWidth?: boolean;
	invalidateNegative?: boolean;
}>();
const emit = defineEmits(['update:model-value', 'focusout']);
const inputField = ref<HTMLInputElement | null>(null);
const error = ref('');
const maskedValue = ref('');
const isFocused = ref(false);
const getDisabled = props.disabled ?? false;
const focusInput = () => {
	inputField.value?.focus();
};
// Computed property to dynamically adjust the input's style based on the autoWidth prop
const inputStyle = computed(() => {
	const style: CSSProperties = {};
	const value = displayValue()?.toString();
	if (props.autoWidth) {
		const textToMeasure = value || props.placeholder;
		// Estimate the width based on the length of the text to measure.
		const width = (textToMeasure?.length || 1) * 8 + 4; // 8px per character + 4px padding
		style.width = `${width}px`; // Dynamically set the width
		style['min-width'] = '20px'; // Ensure a minimum width
	}
	return style; // Return the combined style object
});
const getErrorMessage = computed(() => props.errorMessage || error.value);

const updateValue = (event: Event) => {
	const value = (event.target as HTMLInputElement).value;
	maskedValue.value = value;
	const numValue = toNumber(value);
	error.value = isNaN(numValue) || (props.invalidateNegative && numValue < 0) ? 'Invalid number' : '';
};

function displayValue() {
	// only format value if input is focused
	if (isFocused.value) {
		return maskedValue.value;
	}
	return formatValue(maskedValue.value);
}

function formatValue(value: string | undefined) {
	// format as number if value is a number
	if (!getErrorMessage.value) {
		return numberToNist(value?.toString() ?? '');
	}
	return value;
}

const onBlur = () => {
	const numValue = toNumber(maskedValue.value);
	if (!getErrorMessage.value && !isNaN(numValue)) {
		emit('update:model-value', maskedValue.value === '' ? Number.NaN : numValue);
	}
	isFocused.value = false;
};

watch(
	() => props.modelValue,
	(newValue) => {
		if (isNaN(newValue)) return;
		maskedValue.value = newValue?.toString() ?? '';
	},
	{ immediate: true }
);
</script>

<style scoped>
input {
	text-align: right;
}
</style>
