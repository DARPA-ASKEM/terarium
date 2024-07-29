<template>
	<div class="flex" :label="label" :title="title">
		<label v-if="label" @click.self.stop="focusInput">{{ label }}</label>
		<main :class="{ error: getErrorMessage }" @click.self.stop="focusInput">
			<i v-if="icon" :class="icon" />
			<input
				@click.stop
				ref="inputField"
				:disabled="getDisabled"
				:value="maskedValue"
				@input="updateValue"
				:style="inputStyle"
				@blur="unmask"
				@focusout="$emit('focusout', $event)"
				type="text"
				:placeholder="placeholder"
			/>
		</main>
	</div>
	<aside v-if="getErrorMessage"><i class="pi pi-exclamation-circle" /> {{ getErrorMessage }}</aside>
</template>

<script setup lang="ts">
import { nistToNumber, numberToNist, scrubAndParse } from '@/utils/number';
import { CSSProperties, computed, onMounted, ref, watch } from 'vue';

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
const error = ref('');
const maskedValue = ref('');
const getDisabled = props.disabled ?? false;
const focusInput = () => {
	inputField.value?.focus();
};
// Computed property to dynamically adjust the input's style based on the autoWidth prop
const inputStyle = computed(() => {
	const style: CSSProperties = {};
	if (props.autoWidth) {
		const textToMeasure = maskedValue.value?.length > 0 ? maskedValue.value : props.placeholder;
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
	if (scrubAndParse(maskedValue.value)) {
		// update the model value only when the value is a valid nist
		error.value = '';
		maskedValue.value = numberToNist(maskedValue.value);
	} else {
		error.value = 'Invalid number';
	}
};
watch(
	() => props.modelValue,
	(newValue) => {
		maskedValue.value = numberToNist(newValue?.toString() ?? '');
	}
);
onMounted(() => {
	maskedValue.value = numberToNist(props.modelValue?.toString() ?? '');
});
const unmask = () => {
	// convert back to a number when finished
	if (!getErrorMessage.value) {
		emit('update:model-value', nistToNumber(maskedValue.value));
	}
};
</script>

<style scoped>
main {
	display: flex;
	flex: 1 1 0;
	justify-content: space-between;
	align-items: center;
	padding: var(--gap-xsmall) var(--gap-small);
	background-color: var(--surface-section);
	border: 1px solid var(--surface-border-alt);
	border-radius: var(--border-radius);
	cursor: text;
	transition: border-color 0.3s ease-in-out;
	font-family: var(--font-family);
	&:has(*:disabled) {
		opacity: 0.5;
	}
	&:has(*:focus) {
		border-color: var(--primary-color);
	}
	&.error {
		border-color: var(--error-border-color);
	}
	& > i {
		margin-right: var(--gap-1);
	}
	& > input {
		min-width: 0;
	}
}
label {
	background-color: none;
	color: var(--text-color-secondary);
	cursor: text;
	padding-right: var(--gap-1-5);
	font-size: var(--font-caption);
}
input {
	font-family: var(--font-family);
	font-feature-settings: 'tnum';
	flex-grow: 1;
	border: none;
	background-color: none;
	&::-webkit-inner-spin-button,
	&::-webkit-outer-spin-button {
		-webkit-appearance: none;
		margin: 0;
	}
	text-align: right;
}
aside {
	color: var(--error-message-color);
	font-size: var(--font-caption);
	word-wrap: break-word;
	display: flex;
	align-items: center;
	i {
		margin-right: var(--gap-small);
	}
}

label {
	align-self: center;
}
</style>
