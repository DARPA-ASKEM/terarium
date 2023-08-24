<template>
	<main :class="isEditingStyle">
		<section class="controls" v-if="isEditable">
			<Button
				v-if="isEditing"
				@click="emit('cancel-edit')"
				label="Cancel"
				class="p-button-sm p-button-outlined edit-button"
			/>
			<Button
				v-if="isEditing"
				@click="emit('update-model-from-equation')"
				label="Update model"
				class="p-button-sm"
			/>
			<Button
				v-else
				@click="emit('start-editing')"
				label="Edit equation"
				class="p-button-sm p-button-outlined edit-button"
			/>
		</section>
		<section class="math-editor-container">
			<slot name="math-editor" />
			<Button
				v-if="isEditing"
				class="p-button-sm add-equation-button"
				icon="pi pi-plus"
				label="Add equation"
				@click="emit('add-equation')"
				text
			/>
		</section>
	</main>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import Button from 'primevue/button';

const props = defineProps<{
	isEditing: boolean;
	isEditable: boolean;
	disableSave?: boolean;
}>();

const emit = defineEmits([
	'cancel-edit',
	'add-equation',
	'start-editing',
	'update-model-from-equation'
]);

const isEditingStyle = computed(() => (props.isEditing ? 'is-editing' : ''));
</script>

<style scoped>
main {
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	display: flex;
	flex-direction: column;
}

main.is-editing {
	box-shadow: inset 0 0 0 1px #1b8073, inset 0 0 0 1px #1b8073, inset 0 0 0 1px #1b8073,
		inset 0 0 0 1px var(--primary-color);
	border: 2px solid var(--primary-color);
	border-radius: var(--border-radius);
}

.edit-button {
	margin-left: 5px;
	margin-right: 5px;
}

.add-equation-button {
	width: 10rem;
	margin-left: 1rem;
	margin-top: 0.5rem;
	margin-bottom: 1rem;
	border: none;
	outline: none;
}

.controls {
	display: flex;
	flex-direction: row;
	margin: 0rem 1rem;
	justify-content: flex-end;
	position: relative;
	z-index: 20;
}

section math-editor {
	justify-content: center;
}
.math-editor-container {
	display: flex;
	flex-direction: column;
	border: 4px solid transparent;
	border-radius: var(--border-radius);
	position: relative;
	top: -1rem;
}
</style>
