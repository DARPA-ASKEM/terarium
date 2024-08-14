<template>
	<div class="tera-input">
		<label v-if="label" @click.self.stop="focusInput">{{ label }}</label>
		<main :class="{ error: getErrorMessage }" @click.self.stop="focusInput">
			<i v-if="icon" :class="icon" />
			<input
				v-bind="$attrs"
				ref="inputField"
				:disabled="getDisabled"
				:placeholder="placeholder"
				:style="inputStyle"
				type="text"
				:value="displayValue()"
				@click.stop
				@focus="onFocus"
				@blur="onBlur"
				@input="updateValue"
			/>
		</main>
		<aside v-if="getErrorMessage"><i class="pi pi-exclamation-circle" /> {{ getErrorMessage }}</aside>
	</div>
</template>

<script setup lang="ts">
import { numberToNist } from '@/utils/number';
import { isString, toNumber, isNaN } from 'lodash';
import { CSSProperties, computed, onMounted, ref } from 'vue';

const props = defineProps<{
	modelValue: string | undefined;
	label?: string;
	icon?: string;
	errorMessage?: string;
	disabled?: boolean;
	placeholder?: string;
	autoWidth?: boolean;
	autoFocus?: boolean;
}>();

const emit = defineEmits(['update:model-value', 'blur', 'focus']);
const inputField = ref<HTMLInputElement | null>(null);
const getDisabled = props.disabled ?? false;
const isFocused = ref(false);

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
	if (isString(value) && !isNaN(toNumber(value))) {
		return numberToNist(value?.toString() ?? '');
	}
	return value;
}

const updateValue = (event: Event) => {
	const target = event.target as HTMLInputElement;
	const value = target.value;
	emit('update:model-value', value);
};

const onFocus = (event) => {
	isFocused.value = true;
	emit('focus', event);
};
const onBlur = (event) => {
	isFocused.value = false;
	emit('blur', event);
};

onMounted(() => {
	if (props.autoFocus) {
		focusInput();
	}
});
</script>
