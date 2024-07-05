<template>
	<div class="flex" :label="label">
		<label v-if="label" @click.self.stop="focusInput">{{ label }}</label>
		<main :class="{ error: getErrorMessage }" @click.self.stop="focusInput">
			<input
				@click.stop
				ref="inputField"
				:disabled="getDisabled"
				:value="getValue()"
				@input="updateValue"
				@onFocusOut="emit('on-focus-out')"
				:style="inputStyle"
				@blur="unmask"
				:type="getType"
				:placeholder="placeholder"
			/>
		</main>
	</div>
	<aside v-if="getErrorMessage"><i class="pi pi-exclamation-circle" /> {{ getErrorMessage }}</aside>
</template>

<script setup lang="ts">
import { nistToNumber, numberToNist, scrubAndParse } from '@/utils/number';
import { CSSProperties, InputTypeHTMLAttribute, computed, onMounted, ref, watch } from 'vue';

const props = defineProps<{
	modelValue: string | number | undefined;
	label?: string;
	errorMessage?: string;
	disabled?: boolean;
	type?: InputTypeHTMLAttribute | 'nist';
	placeholder?: string;
	autoWidth?: boolean;
}>();

const emit = defineEmits(['update:model-value', 'on-focus-out']);
const inputField = ref<HTMLInputElement | null>(null);
const error = ref('');
const maskedValue = ref('');

const isNistType = props.type === 'nist';
const getType = isNistType ? 'text' : props.type;
const getDisabled = props.disabled ?? false;

const focusInput = () => {
	inputField.value?.focus();
};

// Computed property to dynamically adjust the input's style based on the autoWidth prop
const inputStyle = computed(() => {
	const style: CSSProperties = {
		'text-align': props.type === 'number' || props.type === 'nist' ? 'right' : 'left' // Set textAlign based on type
	};

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

const getValue = () => (isNistType ? maskedValue.value : props.modelValue);

const updateValue = (event: Event) => {
	const target = event.target as HTMLInputElement;
	const value = target.value;

	if (isNistType) {
		maskedValue.value = value;
		if (scrubAndParse(maskedValue.value)) {
			// update the model value only when the value is a valid nist
			error.value = '';
			maskedValue.value = numberToNist(maskedValue.value);
		} else {
			error.value = 'Invalid number';
		}
	} else {
		emit('update:model-value', value);
	}
};

watch(
	() => props.modelValue,
	(newValue) => {
		if (isNistType) {
			maskedValue.value = numberToNist(newValue?.toString() ?? '');
		}
	}
);

onMounted(() => {
	if (isNistType) {
		maskedValue.value = numberToNist(props.modelValue?.toString() ?? '');
	}
});

const unmask = () => {
	// convert back to a number when finished
	if (isNistType && !getErrorMessage.value) {
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
	input {
		min-width: 0;
	}
}

label {
	background-color: none;
	color: var(--text-color-secondary);
	cursor: text;
	padding-right: var(--gap-xsmall);
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
