<template>
	<main>
		<ul>
			<li v-for="(annotation, idx) of annotations" :key="idx">
				<template v-if="isEditingNote && idx === selectedNoteIndex">
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
						<Button @click="updateNote" label="Save" class="p-button" size="small" />
					</div>
				</template>
				<template v-else>
					<div class="annotation-header">
						<Dropdown
							disabled
							placeholder="Unassigned"
							class="p-button p-button-text notes-dropdown-button"
							:options="noteOptions"
							v-model="selectedNoteSection[idx]"
						/>
						<Button
							icon="pi pi-ellipsis-v"
							class="p-button-rounded p-button-secondary"
							@click="selectedNoteIndex = idx"
						/>
					</div>
					<div>
						<p>{{ annotation.content }}</p>
						<div class="annotation-author-date">
							{{ formatAuthorTimestamp(annotation.username, annotation.timestampMillis) }}
						</div>
					</div>
				</template>
			</li>
			<TieredMenu :model="menuItems" :popup="true" @click.stop />
		</ul>
		<section v-if="isAnnotationInputOpen">
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
					@click="toggleAnnotationInput"
					label="Cancel"
					class="p-button p-button-secondary"
					size="small"
				/>
				<Button @click="addNote" label=" Save" class="p-button" size="small" />
			</div>
		</section>
		<Button
			v-else
			@click="toggleAnnotationInput"
			icon="pi pi-plus"
			label="Add note"
			class="p-button-text p-button-flat"
		/>
	</main>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import Textarea from 'primevue/textarea';
import TieredMenu from 'primevue/tieredmenu';
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

const menuItems = ref([
	{
		label: 'Edit',
		icon: 'pi pi-fw pi-file-edit',
		command: () => {
			isEditingNote.value = true;
			showDeletetionConfirmation.value = false;
		}
	},
	{
		label: 'Delete',
		icon: 'pi pi-fw pi-trash',
		items: [{ label: 'Yes, delete this note', command: () => deleteNote() }]
	},
	{
		label: 'Are you sure?'
	}
]);
const annotations = ref<Annotation[]>([]);
const annotationContent = ref<string>('');
const isAnnotationInputOpen = ref(false);
const selectedNoteIndex = ref();
const isEditingNote = ref(false);
const showDeletetionConfirmation = ref(false);
const selectedNoteSection = ref<string[]>([]);
const newNoteSection = ref();

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
	toggleAnnotationInput();
};

async function updateNote() {
	const noteToUpdate: Annotation = annotations.value[selectedNoteIndex.value];
	const section =
		selectedNoteSection.value.length >= selectedNoteIndex.value
			? selectedNoteSection.value[selectedNoteIndex.value]
			: null;
	await updateAnnotation(noteToUpdate.id, section, noteToUpdate.content);
	await getAndPopulateAnnotations();
	isEditingNote.value = false;
}

async function deleteNote() {
	showDeletetionConfirmation.value = false;
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
main {
	padding: 0 0.5rem 0 1rem;
	overflow-x: hidden;
	display: flex;
	flex-direction: column;
	gap: 1rem;
}

ul {
	list-style: none;
	display: flex;
	flex-direction: column;
	gap: 1rem;
}

.annotation-header {
	height: 2rem;
	display: flex;
	justify-content: space-between;
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

.p-inputtext {
	border-color: var(--surface-border);
	max-width: 100%;
	min-width: 100%;
}

.p-inputtext:hover {
	border-color: var(--primary-color) !important;
}

.save-cancel-buttons {
	display: flex;
	gap: 0.5rem;
}

.save-cancel-buttons > button {
	flex: 1;
}
</style>
