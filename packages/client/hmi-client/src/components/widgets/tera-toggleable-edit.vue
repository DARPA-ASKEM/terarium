<template>
	<div class="flex align-items-center">
		<template v-if="isEditing">
			<tera-input v-model="newValue" />
			<Button text icon="pi pi-times" @click="onCancel" />
			<Button text icon="pi pi-check" @click="onConfirm" />
		</template>
		<template v-else>
			<component :is="tag">
				{{ modelValue }}
			</component>
			<Button text icon="pi pi-pencil" @click="onEdit" />
		</template>
	</div>
</template>
<script setup lang="ts">
import { ref } from 'vue';
import Button from 'primevue/button';
import TeraInput from './tera-input.vue';

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
</script>
