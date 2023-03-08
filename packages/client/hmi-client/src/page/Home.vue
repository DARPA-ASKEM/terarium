<template>
	<main>
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
					<section v-if="projects && projects?.length < 1" class="no-projects">
						<img src="@assets/svg/seed.svg" alt="" />
						<h3>Welcome to Terarium</h3>
						<p>
							Get started by creating a <a @click="isNewProjectModalVisible = true">new project</a>.
							Your projects will be displayed on this page.
						</p>
					</section>
					<div v-else class="carousel">
						<div class="chevron-left" @click="scroll('left', $event)">
							<i class="pi pi-chevron-left" />
						</div>
						<div class="chevron-right" @click="scroll('right', $event)">
							<i class="pi pi-chevron-right" />
						</div>
						<ul v-if="isLoadingProjects">
							<li v-for="(i, index) in [0, 1, 2, 3, 4, 5]" class="card" :key="index">
								<project-card />
							</li>
						</ul>
						<ul v-else>
							<li v-for="(project, index) in projects?.slice().reverse()" class="card" :key="index">
								<project-card :project="project" @click="openProject(project)" />
							</li>
							<li>
								<section class="new-project-card" @click="isNewProjectModalVisible = true">
									<div>
										<img src="@assets/svg/plus.svg" alt="" />
									</div>
									<p>New project</p>
								</section>
							</li>
						</ul>
					</div>
				</TabPanel>
				<TabPanel header="Shared projects">
					<section class="no-projects">
						<img src="@assets/svg/plants.svg" alt="" />
						<h3>You don't have any shared projects</h3>
						<p>Shared projects will be displayed on this page</p>
					</section>
				</TabPanel>
			</TabView>
		</section>
		<section class="papers" v-if="!(projects && projects?.length < 1)">
			<header>
				<h3>Papers related to your projects</h3>
			</header>

			<div v-for="(project, index) in projectsToDisplay" :key="index">
				<p>{{ project.name }}</p>
				<div class="carousel">
					<div class="chevron-left" @click="scroll('left', $event)">
						<i class="pi pi-chevron-left" />
					</div>
					<div class="chevron-right" @click="scroll('right', $event)">
						<i class="pi pi-chevron-right" />
					</div>
					<ul>
						<li v-for="(document, j) in project.relatedDocuments" :key="j" class="card">
							<DocumentCard :document="document" @click="selectDocument(document)" />
						</li>
					</ul>
				</div>
			</div>
			<div v-if="isLoadingProjects">
				<p>
					<Skeleton width="6rem" />
				</p>
				<div class="carousel">
					<ul>
						<li v-for="(i, index) in [0, 1, 2, 3, 4, 5]" class="card" :key="index">
							<DocumentCard />
						</li>
					</ul>
				</div>
			</div>
		</section>
	</main>
	<!-- modal window for showing selected document -->
	<div v-if="selectedDocument !== undefined" class="selected-document-modal-mask" @click="close()">
		<div class="selected-document-modal" @click.stop>
			<div class="modal-header">
				<h4>{{ selectedDocument.title }}</h4>
				<IconClose32 class="close-button" @click="close()" />
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
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import SelectedDocumentPane from '@/components/documents/selected-document-pane.vue';
import IconClose32 from '@carbon/icons-vue/es/close/16';
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
import Skeleton from 'primevue/skeleton';

const projects = ref<IProject[]>();
// Only display projects with at least one related document
// Only display at most 5 projects
const projectsToDisplay = computed(() =>
	projects.value?.filter((project) => project.relatedDocuments !== undefined).slice(0, 5)
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
const isLoadingProjects = computed(() => !projects.value);

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

const SCROLL_INCREMENT_IN_REM = 18.5 * 6; // (card width + margin) * number of cards to display at once
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
</script>

<style scoped>
main {
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

.modal h4 {
	margin-bottom: 1em;
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

header svg {
	color: var(--primary-color);
	margin-right: 0.5rem;
}

.carousel {
	position: relative;
	display: flex;
	height: 319px;
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
	left: -1rem;
	top: 1.4rem;
	height: 279.5px;
}

.chevron-right {
	right: -1rem;
	top: 1.4rem;
	height: 279.5px;
}

.chevron-left:hover,
.chevron-right:hover {
	background-color: var(--chevron-hover);
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
	transition: margin-left 0.8s;
}

li {
	list-style: none;
}

.card {
	z-index: 1;
	transition: 0.2s;
	max-width: 21rem;
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
	width: 500px;
	margin: 0px auto;
	background-color: var(--surface-section);
	border-radius: 2px;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.33);
	overflow-y: auto;
	max-height: 75%;
	padding: 1rem;
}

.modal-header {
	display: flex;
	justify-content: space-between;
}

.close-button {
	width: 2rem;
	height: 2rem;
	cursor: pointer;
	opacity: 50%;
}

.close-button:hover {
	opacity: 100%;
}

.selected-document-pane {
	margin: 2rem 0;
}

.no-projects > * {
	margin: auto;
	margin-top: 1rem;
	text-align: center;
}

.no-projects > img {
	height: 203px;
}

a {
	color: var(--primary-color);
}

.new-project-card {
	width: 17rem;
	height: 20rem;
	display: flex;
	flex-direction: column;
	justify-content: center;
	gap: 1rem;
	border-radius: 4px;
	transition: background-color 0.2s ease, box-shadow 0.2s ease;
}

.new-project-card > p {
	text-align: center;
	color: var(--primary-color);
}

.new-project-card img {
	margin: auto;
}

.new-project-card:hover {
	background-color: var(--surface-hover);
	box-shadow: 0 2px 1px -1px rgb(0 0 0 / 20%), 0 1px 1px 0 rgb(0 0 0 / 14%),
		0 1px 3px 0 rgb(0 0 0 / 12%);
}
</style>
