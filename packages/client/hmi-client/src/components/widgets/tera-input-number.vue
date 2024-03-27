<template>
	<InputText
		class="no-arrows"
		type="number"
		:value="modelValue"
		@input="emit('update:modelValue', $event.target.value)"
		@keypress="handleKeypress"
	/>
</template>

<script setup lang="ts">
import { watch } from 'vue';
import InputText from 'primevue/inputtext';

const props = defineProps<{
	modelValue: number;
	minFractionDigits?: number;
	maxFractionDigits?: number;
}>();

const emit = defineEmits(['update:modelValue']);

watch(
	() => props.modelValue,
	(newValue) => {
		if (props.minFractionDigits && props.maxFractionDigits) {
			const floatValue = parseFloat(newValue);
			const fractionDigits = Math.min(
				Math.max(newValue.split('.')[1]?.length || 0, props.minFractionDigits || 0),
				props.maxFractionDigits || Infinity
			);
			const adjustedValue = floatValue.toFixed(fractionDigits);
			if (adjustedValue !== newValue) {
				emit('update:modelValue', adjustedValue);
			}
		}
	}
);
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
