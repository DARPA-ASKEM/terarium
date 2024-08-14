<template>
	<div v-if="isEditing" class="flex align-items-center">
		<tera-input-text v-bind="$attrs" :model-value="modelValue" @keydown="handleKeyDown" />
		<Button text icon="pi pi-times" @click="onCancel" />
		<Button text icon="pi pi-check" @click="onConfirm" />
	</div>
	<Button v-else class="read-content" text @click="onEdit">
		<component class="btn-content" :is="tag">{{ modelValue }}</component>
		<i class="pi pi-pencil" />
	</Button>
</template>
<script setup lang="ts">
import { ref } from 'vue';
import Button from 'primevue/button';
import TeraInputText from '@/components/widgets/tera-input-text.vue';

const props = withDefaults(
	defineProps<{
		modelValue: string;
		tag: 'h1' | 'h2' | 'h3' | 'h4' | 'h5' | 'h6' | 'p' | 'span';
	}>(),
	{
		tag: 'span'
	}
);

const newValue = ref();
const isEditing = ref(false);

const emit = defineEmits(['update:model-value']);

const onEdit = () => {
	isEditing.value = !isEditing.value;
	newValue.value = props.modelValue;
};

const onConfirm = () => {
	emit('update:model-value', newValue.value);
	isEditing.value = false;
};

const onCancel = () => {
	isEditing.value = false;
};

const handleKeyDown = (event: KeyboardEvent) => {
	if (event.key === 'Enter') onConfirm();
	else if (event.key === 'Escape') onCancel();
};
</script>

<style scoped>
button.read-content {
	display: flex;
	gap: var(--gap-3);
	width: fit-content;
	padding: var(--gap-2);

	& > .btn-content {
		color: var(--text-color);
	}

	& > .pi-pencil {
		color: var(--text-color-subdued);
	}
}
</style>
