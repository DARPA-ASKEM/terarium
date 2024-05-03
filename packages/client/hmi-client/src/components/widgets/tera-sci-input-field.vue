<template>
	<tera-input-field
		:model-value="maskToNistNumber(modelValue)"
		@update:modelValue="onUpdateValue"
		v-bind="attrs"
		type="sci"
		:error-message="errorMessage"
	/>
</template>

<script setup lang="ts">
import teraInputField from '@/components/widgets/tera-input-field.vue';
import { ref, useAttrs } from 'vue';
import { maskToNistNumber } from '@/utils/number';

const attrs = useAttrs();
const errorMessage = ref('');
const onUpdateValue = (value: string) => {
	// errorMessage.value = '';
	// if(!isNistNumber(value)) {
	// 	errorMessage.value = 'Please follow proper format for scientific notation';
	// }
	emit('update:modelValue', value);
};
const emit = defineEmits(['update:modelValue']);
defineProps<{
	modelValue: string;
}>();
</script>
