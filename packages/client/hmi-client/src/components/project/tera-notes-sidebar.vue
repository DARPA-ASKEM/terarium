<template>
	<main>
		<ul>
			<li v-for="(annotation, idx) of annotations" :key="idx">
				<template v-if="isEditingNote && idx === selectedNoteIndex">
					<Dropdown
						placeholder="Unassigned"
						class="p-button p-button-text"
						:options="noteOptions"
						v-model="selectedNoteSection[idx]"
					/>
					<Textarea
						v-model="annotation.content"
						ref="annotationTextInput"
						rows="5"
						cols="30"
						aria-labelledby="annotation"
					/>
					<div class="save-cancel-buttons">
						<Button @click="isEditingNote = false" label="Cancel" class="p-button p-button-secondary" size="small" />
						<Button @click="updateNote" label="Save" class="p-button" size="small" />
					</div>
				</template>
				<template v-else>
					<header>
						<Dropdown
							disabled
							placeholder="Unassigned"
							class="p-button p-button-text"
							:options="noteOptions"
							v-model="selectedNoteSection[idx]"
						/>
						<Button
							icon="pi pi-ellipsis-v"
							class="p-button-rounded p-button-secondary"
							@click="
								($event) => {
									selectedNoteIndex = idx;
									toggle($event);
								}
							"
							aria-haspopup="true"
							aria-controls="overlay_tmenu"
						/>
						<Menu ref="menuRef" :model="menuItems" :popup="true" @click.stop />
					</header>
					<p>{{ annotation.content }}</p>
					<footer>
						{{ formatAuthorTimestamp(annotation.userId, annotation.timestampMillis) }}
					</footer>
				</template>
			</li>
		</ul>
		<section v-if="isAnnotationInputOpen">
			<Dropdown
				placeholder="Unassigned"
				class="p-button p-button-text"
				:options="noteOptions"
				v-model="newNoteSection"
			/>
			<Textarea v-model="annotationContent" ref="annotationTextInput" rows="5" cols="30" aria-labelledby="annotation" />
			<div class="save-cancel-buttons">
				<Button @click="toggleAnnotationInput" label="Cancel" class="p-button p-button-secondary" size="small" />
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
		<tera-modal
			v-if="isDeleteNoteModal"
			@modal-mask-clicked="isDeleteNoteModal = false"
			@modal-enter-press="deleteNote"
		>
			<template #header>
				<h4>Delete note</h4>
			</template>
			<template #default>
				<p>Are you sure you want to delete the following note?</p>
				<p class="note-to-delete">{{ annotations[selectedNoteIndex].content }}</p>
			</template>
			<template #footer>
				<Button label="Delete" class="p-button-danger" @click="deleteNote()" />
				<Button label="Cancel" class="p-button-secondary" @click="isDeleteNoteModal = false" />
			</template>
		</tera-modal>
	</main>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import Textarea from 'primevue/textarea';
import Menu from 'primevue/menu';
import { formatDdMmmYyyy, formatLocalTime, isDateToday } from '@/utils/date';
import { createAnnotation, deleteAnnotation, getAnnotations, updateAnnotation } from '@/services/models/annotations';
import TeraModal from '@/components/widgets/tera-modal.vue';
import { ProjectPages } from '@/types/Project';
import { AssetType, Annotation } from '@/types/Types';

const props = defineProps<{
	assetId: string;
	pageType: AssetType | ProjectPages;
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
		command: () => {
			isDeleteNoteModal.value = true;
		}
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
const menuRef = ref();
const isDeleteNoteModal = ref(false);

async function getAndPopulateAnnotations() {
	if (props.assetId && props.pageType) {
		annotations.value = await getAnnotations(props.assetId, props.pageType);
		selectedNoteSection.value = annotations.value?.map((note) => note.section);
	} else {
		// TODO: Overview page should have its own notes
		annotations.value = [];
		selectedNoteSection.value = [];
	}
}

function toggle(event) {
	menuRef.value[selectedNoteIndex.value].toggle(event);
}

const addNote = async () => {
	await createAnnotation(newNoteSection.value, annotationContent.value, props.assetId, props.pageType);
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
			: '';
	await updateAnnotation(noteToUpdate.id, section, noteToUpdate.content);
	await getAndPopulateAnnotations();
	isEditingNote.value = false;
}

async function deleteNote() {
	showDeletetionConfirmation.value = false;
	const noteToDelete: Annotation = annotations.value[selectedNoteIndex.value];
	await deleteAnnotation(noteToDelete.id);
	await getAndPopulateAnnotations();
	isDeleteNoteModal.value = false;
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

header {
	height: 2rem;
	display: flex;
	justify-content: space-between;
}

.p-dropdown {
	height: 2rem;
}

.p-dropdown:deep(span.p-dropdown-label.p-inputtext.p-placeholder) {
	height: 2rem;
	padding: 0rem;
}

.note-to-delete {
	margin-top: 1rem;
	padding: 0.5rem;
	border-radius: var(--border-radius);
	background-color: var(--surface-highlight);
}

footer {
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
	display: flex;
	justify-content: space-between;
	gap: 0.5rem;
	padding-top: var(--gap-1);
}

.p-inputtext {
	border-color: var(--surface-border);
	max-width: 100%;
	min-width: 100%;
}

.p-inputtext:enabled:hover {
	border-color: var(--primary-color);
}

.save-cancel-buttons {
	display: flex;
	gap: 0.5rem;
}

.save-cancel-buttons > button {
	flex: 1;
}
</style>
