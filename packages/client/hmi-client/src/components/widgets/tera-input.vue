<template>
	<div :class="{ error: getErrorMessage }" @click.self.stop="focusInput">
		<label @click.self.stop="focusInput">{{ label }}</label>
		<input
			v-bind="attrs"
			ref="inputField"
			:value="getValue()"
			@input="updateValue"
			:style="{ 'text-align': textAlign }"
			@blur="unmask"
			:type="getType"
		/>
	</div>
	<aside v-if="getErrorMessage"><i class="pi pi-exclamation-circle" /> {{ getErrorMessage }}</aside>
</template>

<script setup lang="ts">
import { nistToNumber, numberToNist, scrubAndParse } from '@/utils/number';
import { InputTypeHTMLAttribute, computed, onMounted, ref, useAttrs, watch } from 'vue';

const props = defineProps<{
	modelValue: string;
	label?: string;
	errorMessage?: string;
}>();

const emit = defineEmits(['update:modelValue']);
const inputField = ref<HTMLInputElement | null>(null);
const attrs = useAttrs();
const error = ref('');
const maskedValue = ref('');

const isNistType = attrs.type === 'nist';
const textAlign = attrs.type === 'number' || isNistType ? 'right' : 'left';
const getType = isNistType ? 'text' : (attrs.type as InputTypeHTMLAttribute);

const focusInput = () => {
	inputField.value?.focus();
};

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
		emit('update:modelValue', value);
	}
};

watch(
	() => props.modelValue,
	(newValue) => {
		if (isNistType) {
			maskedValue.value = numberToNist(newValue);
		}
	}
);

onMounted(() => {
	if (isNistType) {
		maskedValue.value = numberToNist(props.modelValue);
	}
});

const unmask = () => {
	// convert back to a number when finished
	if (isNistType && !getErrorMessage.value) {
		emit('update:modelValue', nistToNumber(maskedValue.value));
	}
};
</script>

<style scoped>
div {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: var(--gap-xsmall) var(--gap-small);
	background-color: var(--surface-section);
	border: 1px solid var(--surface-border-alt);
	border-radius: var(--border-radius-small);
	cursor: text;
	margin-bottom: var(--gap-small);
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
	padding-right: var(--gap-small);
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
</style>
