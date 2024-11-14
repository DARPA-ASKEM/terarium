<template>
	<main :class="isEditingStyle">
		<section v-if="isEditable" class="controls">
			<Button v-if="isEditing" class="p-button-sm p-button-outlined" @click="emit('cancel-edit')" label="Cancel" />
			<Button
				v-if="isEditing"
				class="p-button-sm"
				:disabled="disableSave"
				@click="emit('update-model-from-equation')"
				label="Save"
			/>
			<Button
				v-else
				class="p-button-sm p-button-outlined"
				@click="emit('start-editing')"
				:loading="!!isUpdating"
				label="Edit"
			/>
		</section>
		<section class="math-editor-container">
			<slot name="math-editor" />
			<Button
				v-if="isEditing"
				class="p-button-sm add-equation-button"
				:label="`Add ${equationType}`"
				icon="pi pi-plus"
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
	equationType?: string;
	isUpdating?: boolean;
}>();

const emit = defineEmits(['cancel-edit', 'add-equation', 'start-editing', 'update-model-from-equation']);

const equationType = computed(() => props.equationType ?? 'equation');
const isEditingStyle = computed(() => (props.isEditing ? 'is-editing' : ''));
const mathContainerStyle = computed(() => (props.isEditable ? '-1rem' : '0rem'));
</script>

<style scoped>
main {
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	display: flex;
	flex-direction: column;
	max-height: 35rem;
	overflow: auto;
}

main.is-editing {
	box-shadow:
		inset 0 0 0 1px #1b8073,
		inset 0 0 0 1px #1b8073,
		inset 0 0 0 1px #1b8073,
		inset 0 0 0 1px var(--primary-color);
	border: 2px solid var(--primary-color);
	border-radius: var(--border-radius);
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
	gap: 0.5rem;
	z-index: 20;
}

.math-editor-container {
	display: flex;
	flex-direction: column;
	border: 4px solid transparent;
	border-radius: var(--border-radius);
	position: relative;
	top: v-bind('mathContainerStyle');
}
</style>
