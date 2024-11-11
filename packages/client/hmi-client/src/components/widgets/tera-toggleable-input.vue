<template>
	<div v-if="isEditing" v-bind="$attrs" class="flex flex-1 align-items-center gap-1">
		<tera-input-text
			ref="inputRef"
			class="w-full"
			:placeholder="placeholder"
			:label="label"
			:characters-to-reject="charactersToReject"
			:model-value="modelValue"
			@update:model-value="emit('update:model-value', $event)"
			@keydown="handleKeyDown"
		/>
		<Button text icon="pi pi-times" @click="onCancel" />
		<Button text icon="pi pi-check" @click="onConfirm" />
	</div>
	<Button v-else :class="$attrs.class" class="text-to-edit" text @click="onEdit">
		<component class="btn-content" :is="tag ?? 'span'">{{ modelValue }}</component>
		<span v-if="isEmpty(modelValue)">{{ placeholder }}</span>
		<i class="pi pi-pencil" />
	</Button>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { ref, nextTick, ComponentPublicInstance } from 'vue';
import Button from 'primevue/button';
import TeraInputText from '@/components/widgets/tera-input-text.vue';

const props = defineProps<{
	modelValue: string;
	tag?: 'h1' | 'h2' | 'h3' | 'h4' | 'h5' | 'h6' | 'p' | 'span';
	// tera-input props, add them as needed
	placeholder?: string;
	label?: string;
	charactersToReject?: string[];
}>();

const emit = defineEmits(['update:model-value']);

let initialValue = '';

const inputRef = ref<ComponentPublicInstance<typeof TeraInputText> | null>(null);
const isEditing = ref(false);

const onEdit = async () => {
	initialValue = props.modelValue;
	isEditing.value = true;
	await nextTick();
	inputRef.value?.$el.querySelector('input')?.focus();
};

const onConfirm = () => {
	isEditing.value = false;
};

const onCancel = () => {
	emit('update:model-value', initialValue); // Revert changes
	isEditing.value = false;
};

const handleKeyDown = (event: KeyboardEvent) => {
	if (event.key === 'Enter') onConfirm();
	else if (event.key === 'Escape') onCancel();
};
</script>

<style scoped>
button.text-to-edit {
	display: flex;
	gap: var(--gap-3);
	max-width: fit-content;
	padding: var(--gap-2);

	& > .btn-content {
		color: var(--text-color);
	}

	& > .pi {
		color: var(--text-color-subdued);
	}
}
</style>
