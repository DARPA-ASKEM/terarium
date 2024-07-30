<template>
	<div class="tera-input" :label="label" :title="title">
		<label v-if="label" @click.self.stop="focusInput">{{ label }}</label>
		<main :class="{ error: getErrorMessage }" @click.self.stop="focusInput">
			<i v-if="icon" :class="icon" />
			<input
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
	</div>
	<aside v-if="getErrorMessage"><i class="pi pi-exclamation-circle" /> {{ getErrorMessage }}</aside>
</template>

<script setup lang="ts">
import { numberToNist } from '@/utils/number';
import { isNaN, toNumber } from 'lodash';
import { CSSProperties, computed, ref, watch } from 'vue';

const props = defineProps<{
	modelValue: number | undefined;
	label?: string;
	title?: string;
	icon?: string;
	errorMessage?: string;
	disabled?: boolean;
	placeholder?: string;
	autoWidth?: boolean;
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
		// Estimate the width based on the length of the value. Adjust the multiplier as needed for your font.
		// Use the length of the text to measure as the width in ch units
		// Estimate the width based on the length of the text to measure. Adjust the multiplier as needed for your font.
		const width = (textToMeasure?.length || 1) * 8 + 4; // 8px per character + 4px padding
		style.width = `${width}px`; // Dynamically set the width
		style['min-width'] = '20px'; // Ensure a minimum width
	}
	return style; // Return the combined style object
});
const getErrorMessage = computed(() => props.errorMessage || error.value);
const updateValue = (event: Event) => {
	const target = event.target as HTMLInputElement;
	const value = target.value;
	maskedValue.value = value;
	if (!isNaN(toNumber(maskedValue.value))) {
		// update the model value only when the value is a valid nist
		error.value = '';
	} else {
		error.value = 'Invalid number';
	}
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
	// convert back to a number when finished
	if (!getErrorMessage.value && !isNaN(toNumber(maskedValue.value))) {
		if (maskedValue.value === '') {
			emit('update:model-value', Number.NaN);
		} else {
			emit('update:model-value', toNumber(maskedValue.value));
		}
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
