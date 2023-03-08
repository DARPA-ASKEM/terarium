<template>
	<main>
		<section class="menu">
			<section class="projects">
				<header>
					<h3>Projects</h3>
					<Button
						icon="pi pi-plus"
						label="New project"
						@click="isNewProjectModalVisible = true"
					></Button>
				</header>
				<TabView>
					<TabPanel header="My projects">
						<div class="carousel">
							<div class="chevron-left">
								<i class="pi pi-chevron-left" @click="scroll('left', $event)" />
							</div>
							<div class="chevron-right">
								<i class="pi pi-chevron-right" @click="scroll('right', $event)" />
							</div>
							<ul>
								<li
									v-for="(project, index) in projects.slice().reverse()"
									class="card"
									:key="index"
								>
									<project-card :project="project" @click="openProject(project)" />
								</li>
							</ul>
						</div>
					</TabPanel>
					<TabPanel header="Shared projects"></TabPanel>
				</TabView>
			</section>
			<section class="papers">
				<header>
					<h3>Papers related to your projects</h3>
				</header>

				<div v-for="(project, index) in projectsToDisplay" :key="index">
					<p>{{ project.name }}</p>
					<div class="carousel">
						<div class="chevron-left">
							<i class="pi pi-chevron-left" @click="scroll('left', $event)" />
						</div>
						<div class="chevron-right">
							<i class="pi pi-chevron-right" @click="scroll('right', $event)" />
						</div>
						<ul>
							<li v-for="(document, j) in project.relatedDocuments" :key="j" class="card">
								<DocumentCard :document="document" @click="selectDocument(document)" />
							</li>
						</ul>
					</div>
				</div>
			</section>
		</section>
		<!-- modal window for showing selected document -->
		<div
			v-if="selectedDocument !== undefined"
			class="selected-document-modal-mask"
			@click="close()"
		>
			<div class="selected-document-modal" @click.stop>
				<header class="modal-header">
					Document
					<i class="pi pi-times close-button" @click="close()" />
				</header>
				<div class="modal-subheader-text">
					<!-- TODO: Should change green text to be a link to search for this author's other work (XDD doesnt do this i dont think atm)-->
					<em>{{ selectedDocument.journal }}</em>
					{{ ', ' + selectedDocument.year + ' ' + selectedDocument.volume }}
				</div>
				<h3>{{ selectedDocument.title }}</h3>
				<!-- TODO: Should change green text to be a link to search for this author's other work (XDD doesnt do this i dont think atm)-->
				<div class="modal-subheader-text">
					<em> {{ listAuthorNames(selectedDocument.author) }} </em>
				</div>
				<selected-document-pane
					class="selected-document-pane"
					:selected-document="selectedDocument"
					@close="close()"
				/>
			</div>
		</div>
		<!-- New project modal -->
		<Teleport to="body">
			<Modal
				v-if="isNewProjectModalVisible"
				class="modal"
				@modal-mask-clicked="isNewProjectModalVisible = false"
			>
				<template #default>
					<form>
						<label for="new-project-name">Project Name</label>
						<InputText id="new-project-name" type="text" v-model="newProjectName" />

						<label for="new-project-description">Project Purpose</label>
						<Textarea id="new-project-description" rows="5" v-model="newProjectDescription" />
					</form>
				</template>
				<template #footer>
					<footer>
						<Button @click="createNewProject">Create Project</Button>
						<Button class="p-button-secondary" @click="isNewProjectModalVisible = false"
							>Cancel</Button
						>
					</footer>
				</template>
			</Modal>
		</Teleport>
	</main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import SelectedDocumentPane from '@/components/documents/selected-document-pane.vue';
import { IProject } from '@/types/Project';
import { XDDSearchParams } from '@/types/XDD';
import { DocumentType } from '@/types/Document';
import { searchXDDDocuments } from '@/services/data';
import useResourcesStore from '@/stores/resources';
import useQueryStore from '@/stores/query';
import API from '@/api/api';
import ProjectCard from '@/components/projects/ProjectCard.vue';
import DocumentCard from '@/components/documents/DocumentCard.vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import Textarea from 'primevue/textarea';
import TabView from 'primevue/tabview';
import TabPanel from 'primevue/tabpanel';
import Modal from '@/components/Modal.vue';
import { useRouter } from 'vue-router';
import * as ProjectService from '@/services/project';
import useAuthStore from '@/stores/auth';
import { RouteName } from '@/router/routes';

const projects = ref<IProject[]>([]);
// Only display projects with at least one related document
// Only display at most 5 projects
const projectsToDisplay = computed(() =>
	projects.value.filter((project) => project.relatedDocuments !== undefined).slice(0, 5)
);
const relevantDocuments = ref<DocumentType[]>([]);
const relevantSearchTerm = 'COVID-19';
const relevantSearchParams: XDDSearchParams = { perPage: 15 }; // , fields: "abstract,title" };
const selectedDocument = ref<DocumentType>();

const resourcesStore = useResourcesStore();
const queryStore = useQueryStore();
const router = useRouter();
const auth = useAuthStore();

const isNewProjectModalVisible = ref(false);
const newProjectName = ref('');
const newProjectDescription = ref('');

onMounted(async () => {
	// Clear all...
	resourcesStore.reset(); // Project related resources saved.
	queryStore.reset(); // Facets queries.

	projects.value = (await API.get('/home')).data as IProject[];

	// Get all relevant documents (latest on section)
	const allDocuments = await searchXDDDocuments(relevantSearchTerm, relevantSearchParams);
	if (allDocuments) {
		relevantDocuments.value = allDocuments.results;
	}
});

const selectDocument = (item: DocumentType) => {
	const itemID = item as DocumentType;
	selectedDocument.value = itemID;
};

const close = () => {
	selectedDocument.value = undefined;
};

const SCROLL_INCREMENT_IN_REM = 21.5 * 5; // (card width + margin) * number of cards to display at once
const scroll = (direction: 'right' | 'left', event: MouseEvent) => {
	const chevronElement = event.target as HTMLElement;
	const cardListElement =
		chevronElement.nodeName === 'svg'
			? chevronElement.parentElement?.querySelector('ul')
			: chevronElement.parentElement?.parentElement?.querySelector('ul');

	if (cardListElement === null || cardListElement === undefined) return;

	// Don't scroll if last element is already within viewport
	if (direction === 'right' && cardListElement.lastElementChild) {
		const parentBounds = cardListElement.parentElement?.getBoundingClientRect();
		const bounds = cardListElement.lastElementChild.getBoundingClientRect();
		if (bounds && parentBounds && bounds.x + bounds.width < parentBounds.x + parentBounds.width) {
			return;
		}
	}

	const marginLeftString =
		cardListElement.style.marginLeft === '' ? '0.5' : cardListElement.style.marginLeft;
	const currentMarginLeft = parseFloat(marginLeftString);
	const changeInRem = direction === 'right' ? -SCROLL_INCREMENT_IN_REM : SCROLL_INCREMENT_IN_REM;
	const newMarginLeft = currentMarginLeft + changeInRem;
	// Don't let the list scroll far enough left that we see space before the
	//	first card.
	cardListElement.style.marginLeft = `${newMarginLeft > 0 ? 0.5 : newMarginLeft}rem`;
};

async function createNewProject() {
	const author = auth.name ?? '';
	const project = await ProjectService.create(
		newProjectName.value,
		newProjectDescription.value,
		author
	);
	if (project) {
		router.push(`/projects/${project.id}`);
		isNewProjectModalVisible.value = false;
	}
}

function openProject(chosenProject: IProject) {
	router.push({ name: RouteName.ProjectRoute, params: { projectId: chosenProject.id } });
}

function listAuthorNames(authors) {
	return authors.map((author) => author.name).join(', ');
}
</script>

<style scoped>
.menu {
	overflow-y: auto;
	overflow-x: hidden;
	flex: 1;
}

section {
	background-color: var(--surface-section);
	color: var(--text-color-secondary);
	padding: 1rem;
}

.papers {
	background-color: var(--surface-secondary);
	padding: 1rem;
}

.papers p {
	margin: 1rem 0 1rem 0rem;
}

h3 {
	font-size: 24px;
	color: var(--text-color-primary);
}

.projects header {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.p-tabview:deep(.p-tabview-panels) {
	padding: 1rem 0 1rem 0;
}

header svg {
	color: var(--primary-color);
	margin-right: 0.5rem;
}

.carousel {
	position: relative;
	display: flex;
	align-items: flex-end;
}

.carousel ul {
	align-items: center;
	display: flex;
	margin: 0.5rem 0.5rem 0 0.5rem;
	padding-bottom: 0.5rem;
}

.chevron-left,
.chevron-right {
	width: 4rem;
	position: absolute;
	z-index: 2;
	cursor: pointer;
	height: 100%;
	display: flex;
	align-items: center;
	height: 443px;
}

.chevron-left {
	left: 0;
}

.chevron-right {
	right: 0;
}

.chevron-left:hover > .pi-chevron-left,
.chevron-right:hover > .pi-chevron-right {
	color: var(--primary-color);
	opacity: 100;
}

.pi-chevron-left,
.pi-chevron-right {
	margin: 0 1rem;
	font-size: 2rem;
	opacity: 0;
	transition: opacity 0.2s ease;
}

.pi-chevron-left:hover,
.pi-chevron-right:hover {
	color: var(--primary-color);
}

ul {
	align-items: center;
	display: inline-flex;
	gap: 1.5rem;
	transition: margin-left 0.2s;
}

li {
	list-style: none;
}

.card {
	z-index: 1;
	transition: 0.2s;
	max-width: 21rem;
}

.carousel:last-of-type {
	margin-bottom: 3rem;
}

.selected-document-modal-mask {
	position: fixed;
	z-index: 9998;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background-color: rgba(0, 0, 0, 0.5);
	display: flex;
	align-items: center;
}

.selected-document-modal {
	position: relative;
	width: 65%;
	margin: 0px auto;
	background-color: var(--surface-section);
	border-radius: 16px;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.33);
	overflow-y: auto;
	max-height: 75%;
	padding: 1rem;
}

.modal h3 {
	margin-bottom: 1em;
	font-weight: 400;
	font-size: 24px;
}

.modal label {
	display: block;
	margin-bottom: 0.5em;
}

.modal input,
.modal textarea {
	display: block;
	margin-bottom: 2rem;
	width: 100%;
}

.modal footer {
	display: flex;
	flex-direction: row-reverse;
	gap: 1rem;
	justify-content: end;
	margin-top: 2rem;
}

.modal header {
	font-weight: 500;
	font-size: 12px;
	line-height: 12px;
	color: var(--text-color-subdued);
}

.selected-document-modal header {
	display: flex;
	justify-content: space-between;
}
.modal-subheader-text {
	color: var(--text-color-subdued);
}
.modal-subheader-text em {
	color: var(--primary-color);
}
.close-button {
	width: 14px;
	height: 14px;
	cursor: pointer;
	opacity: 50%;
}

.close-button:hover {
	opacity: 100%;
}

.selected-document-pane {
	margin: 2rem 0;
}
</style>
