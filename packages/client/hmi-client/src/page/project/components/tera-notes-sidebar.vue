<template>
	<section class="annotation-panel-container">
		<div v-for="(annotation, idx) of annotations" :key="idx">
			<div v-if="isEditingNote && idx === selectedNoteIndex" class="annotation-input-container">
				<div class="annotation-header">
					<Dropdown
						placeholder="Unassigned"
						class="p-button p-button-text notes-dropdown-button"
						:options="noteOptions"
						v-model="selectedNoteSection[idx]"
					/>
				</div>
				<Textarea
					v-model="annotation.content"
					ref="annotationTextInput"
					rows="5"
					cols="30"
					aria-labelledby="annotation"
				/>
				<div class="save-cancel-buttons">
					<Button
						@click="isEditingNote = false"
						label="Cancel"
						class="p-button p-button-secondary"
						size="small"
					/>
					<Button
						@click="
							updateNote();
							isEditingNote = false;
						"
						label=" Save"
						class="p-button"
						size="small"
					/>
				</div>
			</div>
			<div v-else>
				<div class="annotation-header">
					<!-- TODO: Dropdown menu is for selecting which section to assign the note to: Unassigned, Abstract, Methods, etc. -->
					<Dropdown
						disabled
						placeholder="Unassigned"
						class="p-button p-button-text notes-dropdown-button"
						:options="noteOptions"
						v-model="selectedNoteSection[idx]"
					/>
					<!-- TODO: Ellipsis button should open a menu with options to: Edit note & Delete note -->
					<Button
						icon="pi pi-ellipsis-v"
						class="p-button-rounded p-button-secondary"
						@click="
							(event) => {
								toggleAnnotationMenu(event);
								selectedNoteIndex = idx;
							}
						"
					/>
				</div>
				<div>
					<p>{{ annotation.content }}</p>
					<div class="annotation-author-date">
						<div>
							{{ formatAuthorTimestamp(annotation.username, annotation.timestampMillis) }}
						</div>
					</div>
				</div>
			</div>
		</div>
		<Menu
			ref="annotationMenu"
			:model="annotationMenuItems"
			:popup="true"
			@hide="onHide"
			@click.stop
		/>
	</section>
	<div class="annotation-input-box">
		<div v-if="isAnnotationInputOpen" class="annotation-input-container">
			<div class="annotation-header">
				<Dropdown
					placeholder="Unassigned"
					class="p-button p-button-text notes-dropdown-button"
					:options="noteOptions"
					v-model="newNoteSection"
				/>
			</div>
			<Textarea
				v-model="annotationContent"
				ref="annotationTextInput"
				rows="5"
				cols="30"
				aria-labelledby="annotation"
			/>
			<div class="save-cancel-buttons">
				<Button
					@click="toggleAnnotationInput()"
					label="Cancel"
					class="p-button p-button-secondary"
					size="small"
				/>
				<Button
					@click="
						addNote();
						toggleAnnotationInput();
					"
					label=" Save"
					class="p-button"
					size="small"
				/>
			</div>
		</div>
		<div v-else>
			<Button
				@click="toggleAnnotationInput()"
				icon="pi pi-plus"
				label="Add note"
				class="p-button-text p-button-flat"
			/>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import Textarea from 'primevue/textarea';
import Menu from 'primevue/menu';
import { Annotation } from '@/types/common';
import { formatDdMmmYyyy, formatLocalTime, isDateToday } from '@/utils/date';
import {
	createAnnotation,
	deleteAnnotation,
	getAnnotations,
	updateAnnotation
} from '@/services/models/annotations';
import { ProjectAssetTypes, ProjectPages } from '@/types/Project';

const props = defineProps<{
	assetId?: string;
	pageType?: ProjectAssetTypes | ProjectPages;
}>();

enum NoteSection {
	Unassigned = 'Unassigned',
	Abstract = 'Abstract',
	Intro = 'Intro',
	Methods = 'Methods',
	Results = 'Results',
	Discussion = 'Discussion',
	References = 'References'
}
const noteOptions = ref([
	NoteSection.Unassigned,
	NoteSection.Abstract,
	NoteSection.Intro,
	NoteSection.Methods,
	NoteSection.Results,
	NoteSection.Discussion,
	NoteSection.References
]);

const noteDeletionConfirmationMenuItem = {
	label: 'Are you sure?',
	icon: '',
	items: [{ label: 'Yes, delete this note', command: () => deleteNote() }]
};
const annotationMenuItems = ref([
	{
		label: 'Edit',
		icon: 'pi pi-fw pi-file-edit',
		command: () => {
			isEditingNote.value = true;
		}
	},
	{
		label: 'Delete',
		icon: 'pi pi-fw pi-trash',
		command: () => {
			if (!isNoteDeletionConfirmation.value) {
				// @ts-ignore
				annotationMenuItems.value.push(noteDeletionConfirmationMenuItem);
				isNoteDeletionConfirmation.value = true;
			}
		}
	}
]);

const annotations = ref<Annotation[]>([]);
const annotationContent = ref<string>('');
const isAnnotationInputOpen = ref(false);
const annotationMenu = ref();
const menuOpenEvent = ref();
const selectedNoteIndex = ref();
const isEditingNote = ref(false);
const isNoteDeletionConfirmation = ref(false);
const selectedNoteSection = ref<string[]>([]);
const newNoteSection = ref();

function onHide() {
	if (isNoteDeletionConfirmation.value) {
		annotationMenu.value.show(menuOpenEvent.value);
		isNoteDeletionConfirmation.value = false;
	} else if (annotationMenuItems.value.length > 2) {
		annotationMenuItems.value.pop();
	}
}

const toggleAnnotationMenu = (event) => {
	// Fake object to allow the Menu component to not hide after clicking an item.
	// Actually I am immediately showing it again after it automatically hides.
	// I need to save and pass event.currentTarget in a ref to use when I want to manually show the menu.
	// Kind of a hack/workaround due to the restrictive nature of this component.
	menuOpenEvent.value = {
		currentTarget: event.currentTarget
	};
	annotationMenu.value.toggle(event);
};

async function getAndPopulateAnnotations() {
	if (props.assetId && props.pageType) {
		annotations.value = await getAnnotations(props.assetId, props.pageType);
		selectedNoteSection.value = annotations.value?.map((note) => note.section);
	} else {
		selectedNoteSection.value = [];
	}
}

const addNote = async () => {
	await createAnnotation(
		newNoteSection.value,
		annotationContent.value,
		props.assetId,
		props.pageType
	);
	annotationContent.value = '';
	newNoteSection.value = NoteSection.Unassigned;
	await getAndPopulateAnnotations();
};

async function updateNote() {
	const noteToUpdate: Annotation = annotations.value[selectedNoteIndex.value];
	const section =
		selectedNoteSection.value.length >= selectedNoteIndex.value
			? selectedNoteSection.value[selectedNoteIndex.value]
			: null;
	await updateAnnotation(noteToUpdate.id, section, noteToUpdate.content);
	await getAndPopulateAnnotations();
}

async function deleteNote() {
	const noteToDelete: Annotation = annotations.value[selectedNoteIndex.value];
	await deleteAnnotation(noteToDelete.id);
	await getAndPopulateAnnotations();
}

function toggleAnnotationInput() {
	isAnnotationInputOpen.value = !isAnnotationInputOpen.value;
	if (isAnnotationInputOpen.value === false) {
		annotationContent.value = '';
	}
}

function formatAuthorTimestamp(username, timestamp) {
	if (isDateToday(timestamp)) {
		return `${username} at ${formatLocalTime(timestamp)} today`;
	}
	return `${username} on ${formatDdMmmYyyy(timestamp)}`;
}

onMounted(() => getAndPopulateAnnotations());

watch(
	() => [props.assetId, props.pageType],
	() => {
		getAndPopulateAnnotations();
	}
);
</script>

<style scoped>
.annotation-header {
	display: flex;
	justify-content: space-between;
}

.annotation-header .p-button.p-button-secondary {
	background-color: var(--surface-section);
}

.annotation-panel-container {
	display: flex;
	flex-direction: column;
	gap: 16px;
	padding: 0 16px 0 16px;
}

.notes-dropdown-button {
	color: var(--text-color-subdued);
	padding: 0rem;
}

.annotation-panel .p-dropdown:not(.p-disabled).p-focus {
	box-shadow: none;
	background-color: var(--surface-hover);
}

.annotation-author-date {
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
	display: flex;
	justify-content: space-between;
	gap: 0.5rem;
	padding-top: 0.25rem;
	padding-right: 1rem;
}

.annotation-content {
	padding: 0rem 0.5rem 0rem 0.5rem;
}

.annotation-input-container {
	display: flex;
	flex-direction: column;
	gap: 8px;
}

.annotation-input-box {
	padding: 16px 16px 8px 16px;
}

.annotation-input-box .p-inputtext {
	border-color: var(--surface-border);
	max-width: 100%;
	min-width: 100%;
}

.annotation-input-box .p-inputtext:hover {
	border-color: var(--primary-color) !important;
}

.annotation-header {
	height: 2rem;
}

.save-cancel-buttons {
	display: flex;
	gap: 0.5rem;
}

.save-cancel-buttons > button {
	flex: 1;
}
</style>
