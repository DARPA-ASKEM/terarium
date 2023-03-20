<template>
	<main>
		<table>
			<tr class="parameters_header">
				<th>Label</th>
				<th>Name</th>
				<th>Units</th>
				<th>Concept</th>
				<th>Definition</th>
			</tr>
		</table>
		<ul>
			<li v-for="parameterRow in parameters" :key="parameterRow.id">
				<model-parameter-list-item
					class="model-parameter-list-item"
					:initial-param-row="parameterRow"
					:draggable="true"
					@dragstart="handleDragStart($event)"
					@dragenter="handleDragEnter($event)"
					@dragleave="handleDragLeave($event)"
					@dragend="handleDragEnd($event)"
					@drop="handleDrop($event)"
					@dragover.prevent
				/>
			</li>
			<footer>
				<Button label="Add parameter" icon="pi pi-plus" />
				<Button label="Extract from a dataset" icon="pi pi-file-export" />
			</footer>
		</ul>
	</main>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Button from 'primevue/button';
import ModelParameterListItem from '@/components/models/model-parameter-list-item.vue';
import { useDragEvent } from '@/services/drag-drop';

const { setDragData, getDragData } = useDragEvent();

defineProps<{
	parameters: any;
}>();

const draggedListItem = ref();

function handleDrop(e) {
	if (e.stopPropagation) {
		e.stopPropagation();
	}
	if (
		draggedListItem.value !== e.target &&
		e.target.classList.contains('model-parameter-list-item')
	) {
		draggedListItem.value.innerHTML = e.target.innerHTML;
		e.target.innerHTML = getDragData('dragged-list-item');
	}
	return false;
}

function handleDragStart(e) {
	draggedListItem.value = e.target;
	e.dataTransfer.effectAllowed = 'move';
	setDragData('dragged-list-item', draggedListItem.value.innerHTML);
	// draggedListItem.value.style.opacity = '0.4';
}

function handleDragEnter(e) {
	e.target.classList.add('over');
}

function handleDragLeave(e) {
	e.target.classList.remove('over');
}

function handleDragEnd(e) {
	console.log(e);
	// this.style.opacity = '1';
}
</script>

<style scoped>
ul {
	background-color: var(--surface-ground);
	border-radius: 0.75rem;
	padding: 0.75rem;
}

table {
	margin: 0.5rem 2rem;
	text-transform: uppercase;
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
}

table tr {
	display: flex;
	gap: 10rem;
}

table tr th {
	font-weight: normal;
}

.over {
	border: 1px dotted #666;
}

footer {
	display: flex;
	gap: 1rem;
	margin-left: 1rem;
}

footer .p-button {
	color: var(--primary-color);
	background-color: transparent;
}

footer .p-button:hover,
footer .p-button:focus {
	color: var(--primary-color);
	background-color: var(--surface-hover);
}
</style>
