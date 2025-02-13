<template>
	<div>
		<label @click.stop="switchModel = false">{{ labelFalse }}</label>
		<InputSwitch v-model="switchModel" />
		<label @click.stop="switchModel = true">{{ labelRight }}</label>
	</div>
</template>

<script setup lang="ts">
import { defineProps, ref, watch } from 'vue';
import InputSwitch from 'primevue/inputswitch';

const props = defineProps<{
	labelFalse: string;
	labelRight: string;
	modelValue: boolean;
}>();
const emit = defineEmits(['update:modelValue']);

// False == left, True == right
const switchModel = ref(false);

// Watch for changes and emit update event for v-model
watch(
	switchModel,
	(newValue) => {
		emit('update:modelValue', newValue);
	},
	{ immediate: true }
);

// Watch the prop value for changes from parent component
watch(
	() => props.modelValue,
	(newValue) => {
		switchModel.value = newValue;
	}
);
</script>

<style scoped>
div {
	align-items: center;
	color: var(--text-color);
	display: flex;
	gap: var(--gap-2);

	/* Override PrimeVue styles for slider to always stay neutral gray */
	&:deep(.p-inputswitch .p-inputswitch-slider),
	&:deep(.p-inputswitch:hover .p-inputswitch-slider) {
		background: var(--text-color-disabled);
	}
	&:deep(.p-inputswitch .p-inputswitch-slider::before) {
		background: var(--text-color-subdued);
		box-shadow: none !important;
	}

	label {
		cursor: pointer;
		position: relative;

		/* Increase the hit box for easier clicking */
		&::before {
			content: '';
			display: block;
			height: 2.5rem;
			width: calc(100% + 1.5rem);
			position: absolute;
			top: 50%;
			left: 50%;
			transform: translate(-50%, -50%);
		}
	}
}
</style>
