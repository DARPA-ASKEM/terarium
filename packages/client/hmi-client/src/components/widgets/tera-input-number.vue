<template>
	<InputText
		class="no-arrows"
		type="number"
		:value="modelValue"
		@input="$event.target && updateValue($event.target)"
	/>
</template>

<script setup lang="ts">
import InputText from 'primevue/inputtext';

defineProps({
	modelValue: {
		type: Number,
		default: 0
	},
	// TODO: Add support for these later
	minFractionDigits: {
		type: Number,
		default: null
	},
	maxFractionDigits: {
		type: Number,
		default: null
	}
});

const emit = defineEmits(['update:modelValue']);

function updateValue({ value }: any) {
	if (value) {
		emit('update:modelValue', parseFloat(value));
	}
}
</script>

<style scoped>
.no-arrows::-webkit-inner-spin-button,
.no-arrows::-webkit-outer-spin-button {
	-webkit-appearance: none;
	margin: 0;
}

.no-arrows {
	-moz-appearance: textfield;
	width: 100%;
}
</style>
