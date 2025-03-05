<template>
	<div class="tera-input">
		<label v-if="label" @click.self.stop="focusInput">{{ label }}</label>
		<main :class="[{ error: getErrorMessage }, { empty: isEmptyError }]" @click.self.stop="focusInput">
			<i v-if="icon" :class="icon" class="px-1" />
			<input
				v-bind="$attrs"
				ref="inputField"
				:class="[{ 'empty-value': isEmptyError }]"
				:disabled="getDisabled"
				:placeholder="placeholder"
				:style="inputStyle"
				type="text"
				:value="displayValue()"
				@click.stop
				@focus="onFocus"
				@blur="onBlur"
				@input="updateValue"
				@keydown.space.stop
				autocomplete="off"
			/>
		</main>
		<aside v-if="getErrorMessage"><i class="pi pi-exclamation-circle" /> {{ getErrorMessage }}</aside>
	</div>
</template>

<script setup lang="ts">
import { numberToNist } from '@/utils/number';
import { isString, toNumber, isNaN, isEmpty } from 'lodash';
import { CSSProperties, computed, onMounted, ref } from 'vue';

const props = defineProps<{
	modelValue: string | undefined;
	label?: string;
	icon?: string;
	errorMessage?: string;
	errorEmpty?: boolean;
	disabled?: boolean;
	placeholder?: string;
	autoWidth?: boolean;
	autoFocus?: boolean;
	charactersToReject?: string[];
}>();

const emit = defineEmits(['update:model-value', 'blur', 'focus']);
const inputField = ref<HTMLInputElement | null>(null);
const getDisabled = computed(() => props.disabled ?? false);
const isFocused = ref(false);

const focusInput = () => {
	inputField.value?.focus();
};

const input = computed(() => displayValue()?.toString());

// Computed property to dynamically adjust the input's style based on the autoWidth prop
const inputStyle = computed(() => {
	const style: CSSProperties = {};
	if (props.autoWidth) {
		const textToMeasure = input.value || props.placeholder;
		// Estimate the width based on the length of the text to measure.
		const width = (textToMeasure?.length || 1) * 8 + 4; // 8px per character + 4px padding
		style.width = `${width}px`; // Dynamically set the width
		style['min-width'] = '20px'; // Ensure a minimum width
	}

	return style; // Return the combined style object
});

const getErrorMessage = computed(() => props.errorMessage);
const isEmptyError = computed(() => props.errorEmpty && isEmpty(input.value));

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
	if (props.charactersToReject && !isEmpty(props.charactersToReject)) {
		const start = target.selectionStart;
		const end = target.selectionEnd;
		target.value = target.value.replace(new RegExp(`[${props.charactersToReject.join('')}]`, 'g'), ''); // Create a regex pattern from charactersToReject to remove them
		target.setSelectionRange(start, end); // Maintain cursor position, is needed if we are entering in the middle of the input
	}
	emit('update:model-value', target.value);
};

const onFocus = (event) => {
	isFocused.value = true;
	emit('focus', event);
};
const onBlur = (event) => {
	isFocused.value = false;
	const target = event.target as HTMLInputElement;
	if (props.charactersToReject && !isEmpty(props.charactersToReject)) {
		const start = target.selectionStart;
		const end = target.selectionEnd;
		target.value = target.value.replace(new RegExp(`[${props.charactersToReject.join('')}]`, 'g'), ''); // Create a regex pattern from charactersToReject to remove them
		target.setSelectionRange(start, end); // Maintain cursor position, is needed if we are entering in the middle of the input
	}
	emit('blur', event.target.value);
};

onMounted(() => {
	if (props.autoFocus) {
		focusInput();
	}
});

defineExpose({
	focusInput
});
</script>
