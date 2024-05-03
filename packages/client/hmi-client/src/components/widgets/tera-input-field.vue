<template>
	<div :class="{ error: errorMessage }" @click.self.stop="focusInput">
		<label @click.self.stop="focusInput">{{ label }}</label>
		<input
			v-bind="attrs"
			ref="inputField"
			:value="modelValue"
			@input="updateValue"
			:style="{ 'text-align': textAlign }"
		/>
	</div>
	<aside v-if="errorMessage"><i class="pi pi-exclamation-circle" /> {{ errorMessage }}</aside>
</template>

<script setup lang="ts">
import { computed, ref, useAttrs } from 'vue';

const props = defineProps<{
	modelValue: string;
	label?: string;
	errorMessage?: string;
	mask?: (value: any) => string;
}>();

const emit = defineEmits(['update:modelValue']);
const inputField = ref<HTMLInputElement | null>(null);
const attrs = useAttrs();

const textAlign = computed(() =>
	attrs.type === 'number' || attrs.type === 'sci' ? 'right' : 'left'
);
const focusInput = () => {
	inputField.value?.focus();
};

const updateValue = (event: Event) => {
	const value = (event.target as HTMLInputElement).value;
	if (props.mask) {
		const maskedValue = props.mask(value);
		inputField.value!.value = maskedValue;
		emit('update:modelValue', inputField.value!.value);
		return;
	}
	emit('update:modelValue', value);
};
</script>

<style scoped>
div {
	display: flex;
	justify-content: space-between;
	align-items: center;
	width: 100%;
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
