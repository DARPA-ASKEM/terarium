<!--Should probably improve this to use our own checkbox rather than primevue's-->
<template>
	<div class="flex items-center">
		<Checkbox
			:inputId="inputId"
			:model-value="modelValue"
			@update:model-value="(val) => emit('update:model-value', val)"
			binary
			:disabled="disabled"
		/>
		<div class="flex flex-column ml-2">
			<label :for="inputId">{{ label }}</label>
			<sub>{{ subtext }}</sub>
		</div>
	</div>
</template>

<script setup lang="ts">
import Checkbox from 'primevue/checkbox';

defineProps<{
	modelValue: boolean;
	inputId?: string;
	label: string;
	subtext?: string;
	disabled?: boolean;
}>();

const emit = defineEmits(['update:model-value']);
</script>
