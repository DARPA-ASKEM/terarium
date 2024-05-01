<template>
	<div
		class="input-container"
		:class="{ error: errorMessage, disabled: disabled, focused: focused }"
		@click="focusInput"
	>
		<label for="custom-input" class="input-label">{{ label }}</label>
		<input
			id="custom-input"
			class="input-field"
			ref="inputField"
			:value="modelValue"
			@input="updateValue"
			:disabled="disabled"
			@focus="onFocus"
			@blur="onBlur"
		/>
	</div>
	<p v-if="errorMessage" class="error-message">
		<i class="pi pi-exclamation-circle" /> {{ errorMessage }}
	</p>
</template>

<script setup lang="ts">
import { ref } from 'vue';

defineProps<{
	modelValue: string;
	label?: string;
	errorMessage?: string;
	disabled?: boolean;
}>();

const emit = defineEmits(['update:modelValue']);
const inputField = ref<HTMLInputElement | null>(null);
const focused = ref(false);

const focusInput = () => {
	inputField.value?.focus();
};

const onFocus = () => {
	focused.value = true;
};

const onBlur = () => {
	focused.value = false;
};

const updateValue = (event: Event) => {
	const value = (event.target as HTMLInputElement).value;
	emit('update:modelValue', value);
};
</script>

<style scoped>
.input-container {
	display: flex;
	justify-content: space-between;
	align-items: center;
	width: 100%;
	padding: var(--gap-xsmall) var(--gap-small);
	background-color: #fff;
	border: 1px solid var(--surface-border-alt);
	border-radius: var(--border-radius-small);
	cursor: text;
	margin-bottom: var(--gap-small);
	transition: border-color 0.3s ease-in-out;

	&.disabled {
		opacity: 0.5;
	}

	&.focused {
		border-color: var(--primary-color);
	}
	&.error {
		border-color: var(--error-border-color);
	}
	input {
		min-width: 0;
	}
}

.input-label {
	background-color: transparent;
	color: var(--text-color-secondary);
	cursor: text;
	padding-right: var(--gap-small);
}

.input-field {
	text-align: right;
	flex-grow: 1;
	border: none;
	background-color: transparent;
}

.error-message {
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
