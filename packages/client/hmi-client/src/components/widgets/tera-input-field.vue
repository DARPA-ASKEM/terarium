<template>
	<div class="input-container" :class="{ error: errorMessage }" @click="focusInput">
		<label for="custom-input" class="input-label">Label</label>
		<input
			id="custom-input"
			class="input-field"
			ref="inputField"
			:value="displayValue"
			@input="updateValue"
		/>
	</div>
	<p v-if="isError" class="error-message">
		<i class="pi pi-exclamation-circle" /> {{ errorMessage }}
	</p>
</template>

<script setup lang="ts">
import { ref } from 'vue';

defineProps<{
	errorMessage?: string;
}>();
const inputField = ref<HTMLInputElement | null>(null);
const isError = ref(true);
const actualValue = ref(''); // This will store the actual value
const displayValue = ref(''); // This will store the display value

const focusInput = () => {
	inputField.value?.focus();
};

const updateValue = (event: Event) => {
	const value = (event.target as HTMLInputElement).value;
	actualValue.value = value; // Store the actual value
	displayValue.value = applyMask(value); // Store the masked value for display
	console.log(displayValue.value);
};

const applyMask = (num: string) => {
	num = parseFloat(num).toString();

	// Split the input by decimal point
	let [integerPart, decimalPart] = num.split('.');

	// Format the integer part
	integerPart = integerPart.replace(/\B(?=(\d{3})+(?!\d))/g, ' ');

	if (num.includes('.') && decimalPart) {
		decimalPart = decimalPart.replace(/(\d{3})/g, '$1 ').trim();
	}

	// Construct the formatted number
	let formattedNumber = integerPart;
	if (decimalPart) {
		formattedNumber += `.${decimalPart}`;
	}

	return formattedNumber;
};

// const checkInput = (event: KeyboardEvent) => {
//   const value = (event.target as HTMLInputElement).value + event.key;
//   const maskedValue = applyMask(value);
//   if (maskedValue !== value) {
//     event.preventDefault();
//   }
// };
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
	border-radius: 2px;
	cursor: text;
	&.error {
		background-color: var(--error-message-background);
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
	font-size: 12px;
	color: var(--error-message-color);
	word-wrap: break-word;
}
</style>
