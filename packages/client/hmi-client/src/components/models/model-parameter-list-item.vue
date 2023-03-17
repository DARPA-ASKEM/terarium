<template>
	<main :draggable="isDraggable" @dragstart="handleDragStart" @dragend="handleDragEnd">
		<section>
			<i
				class="pi pi-sort-alt grab"
				@mouseover="isDraggable = true"
				@focusin="isDraggable = true"
				@mouseleave="isDraggable = false"
				@focusout="isDraggable = false"
			/>
			<table>
				<tr>
					<td>
						<InputText
							v-if="isEditing"
							v-model="paramRow.label"
							type="text"
							class="p-inputtext-sm"
						/>
						<span v-else>{{ paramRow.label }}</span>
					</td>
					<td>
						<InputText
							v-if="isEditing"
							v-model="paramRow.name"
							type="text"
							class="p-inputtext-sm"
						/>
						<span v-else>{{ paramRow.name }}</span>
					</td>
					<td>
						<InputText
							v-if="isEditing"
							v-model="paramRow.units"
							type="text"
							class="p-inputtext-sm"
						/>
						<span v-else>{{ paramRow.units }}</span>
					</td>
					<td>
						<InputText
							v-if="isEditing"
							v-model="paramRow.concept"
							type="text"
							class="p-inputtext-sm"
						/>
						<span v-else>{{ paramRow.concept }}</span>
					</td>
					<td>
						<InputText
							v-if="isEditing"
							v-model="paramRow.definition"
							type="text"
							class="p-inputtext-sm"
						/>
						<span v-else>{{ paramRow.definition }}</span>
					</td>
				</tr>
			</table>
			<template v-if="!isEditing">
				<Chip label="extractions" />
				<Button icon="pi pi-eye" class="p-button-icon-only p-button-text p-button-rounded" />
			</template>
			<template v-else>
				<Button icon="pi pi-times" class="p-button-icon-only p-button-text p-button-rounded" />
				<Button icon="pi pi-check" class="p-button-icon-only p-button-text p-button-rounded" />
			</template>
			<Button
				icon="pi pi-ellipsis-v"
				class="p-button-icon-only p-button-text p-button-rounded"
				@click="toggleEditMode"
			/>
		</section>
		<section class="tile-container"></section>
	</main>
</template>
<script setup lang="ts">
import { ref } from 'vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import Chip from 'primevue/chip';
// import { useDragEvent } from '@/services/drag-drop';

// const { setDragData } = useDragEvent();

const props = defineProps<{
	initialParamRow: {
		label: string;
		name: string;
		units: string;
		concept: string;
		definition: string;
	};
}>();

const isEditing = ref(false);
const isDraggable = ref(false);
const paramRow = ref(props.initialParamRow);

// function onRowEditSave() {
//     console.log(0);
// }

function toggleEditMode() {
	isEditing.value = !isEditing.value;
}

function handleDragStart(event, item) {
	console.log(item);
	event.dataTransfer.dropEffect = 'move';
	event.dataTransfer.effectAllowed = 'move';
}

function handleDragEnd(e) {
	console.log(e);
	// this.style.opacity = '1';
}

// function updateParamRow() {

// }
</script>
<style scoped>
main {
	background-color: var(--surface-section);
	border-radius: 0.5rem;
	padding: 0.5rem;
}

section {
	display: flex;
	gap: 0.5rem;
	margin: 0 0 0.5rem 0;
}

.grab {
	cursor: grab;
}

.grab:active {
	cursor: grabbing;
}

table {
	width: 100%;
}

table td span:empty:before {
	content: '--';
}

table:has(.parameters_header) {
	margin-left: 3rem;
	width: 80%;
}

.tile-container {
	height: 6rem;
	background-color: var(--surface-ground);
	border: 1px solid var(--surface-border);
	border-radius: 0.75rem;
	margin: 0 0.5rem;
}
</style>
