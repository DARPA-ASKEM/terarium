<template>
	<div class="tera-input" :label="label" :title="title">
		<label v-if="label" @click.self.stop="focusInput">{{ label }}</label>
		<main :class="{ error: getErrorMessage }" @click.self.stop="focusInput">
			<i v-if="icon" :class="icon" />
			<input
				ref="inputField"
				:disabled="getDisabled"
				:placeholder="placeholder"
				:style="inputStyle"
				type="text"
				:value="displayValue()"
				@click.stop
				@focus="isFocused = true"
				@focusout="$emit('focusout', $event)"
				@blur="onBlur"
				@input="updateValue"
			/>
		</main>
	</div>
	<aside v-if="getErrorMessage"><i class="pi pi-exclamation-circle" /> {{ getErrorMessage }}</aside>
</template>

<script setup lang="ts">
import { numberToNist } from '@/utils/number';
import { isNumber, isString, toNumber, isNaN } from 'lodash';
import { CSSProperties, computed, ref } from 'vue';

const props = defineProps<{
	modelValue: string | number | undefined;
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
const getDisabled = props.disabled ?? false;
const isFocused = ref(false);

const focusInput = () => {
	inputField.value?.focus();
};

// Computed property to dynamically adjust the input's style based on the autoWidth prop
const inputStyle = computed(() => {
	const style: CSSProperties = {};

	const value = props.modelValue?.toString();
	if (props.autoWidth && value) {
		const textToMeasure = value.length > 0 ? value : props.placeholder;
		// Estimate the width based on the length of the value. Adjust the multiplier as needed for your font.
		// Use the length of the text to measure as the width in ch units
		// Estimate the width based on the length of the text to measure. Adjust the multiplier as needed for your font.
		const width = (textToMeasure?.length || 1) * 8 + 4; // 8px per character + 4px padding
		style.width = `${width}px`; // Dynamically set the width
		style['min-width'] = '20px'; // Ensure a minimum width
	}

	return style; // Return the combined style object
});

const getErrorMessage = computed(() => props.errorMessage);

function displayValue() {
	// only format value if input is focused
	if (isFocused.value) {
		return props.modelValue;
	}
	return formatValue(props.modelValue);
}

function formatValue(value: string | number | undefined) {
	// format as number if value is a number
	if (isNumber(value) || (isString(value) && !isNaN(toNumber(value)))) {
		return numberToNist(value?.toString() ?? '');
	}
	return value;
}

const updateValue = (event: Event) => {
	const target = event.target as HTMLInputElement;
	const value = target.value;

	console.log(value);
	emit('update:model-value', value);
};

const onBlur = () => {
	isFocused.value = false;
};
</script>
