<template>
	<div v-if="isEditing" v-bind="$attrs" class="flex flex-1 align-items-center gap-1">
		<tera-input-text
			ref="inputRef"
			class="w-full"
			v-model="newValue"
			@keydown.enter.stop="handleKeyDown"
			@keydown.esc.stop="handleKeyDown"
		/>
		<Button text icon="pi pi-times" @click="onCancel" />
		<Button text icon="pi pi-check" @click="onConfirm" />
	</div>
	<Button v-else :class="$attrs.class" class="text-to-edit" text @click="onEdit">
		<component class="btn-content" :is="tag">{{ modelValue }}</component>
		<i class="pi pi-pencil" />
	</Button>
</template>

<script setup lang="ts">
import { ref, nextTick, ComponentPublicInstance } from 'vue';
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

const emit = defineEmits(['update:model-value']);

const newValue = ref(props.modelValue);

const inputRef = ref<ComponentPublicInstance<typeof TeraInputText> | null>(null);
const isEditing = ref(false);

const onEdit = async () => {
	newValue.value = props.modelValue;
	isEditing.value = true;
	await nextTick();
	inputRef.value?.$el.querySelector('input')?.focus();
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
button.text-to-edit {
	display: flex;
	gap: var(--gap-3);
	max-width: fit-content;
	padding: var(--gap-2);
	text-align: left;

	& > .btn-content {
		color: var(--text-color);
	}

	& > .pi {
		color: var(--text-color-subdued);
	}
}
</style>
