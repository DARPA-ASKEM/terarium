<template>
	<main>
		<section class="menu">
			<section class="projects">
				<header>
					<h3>Projects</h3>
					<Button
						icon="pi pi-plus"
						label="New project"
						size="large"
						@click="isNewProjectModalVisible = true"
					></Button>
				</header>
				<TabView>
					<TabPanel header="My projects">
						<section v-if="projects && isEmpty(projects)" class="no-projects">
							<img src="@assets/svg/seed.svg" alt="" />
							<h3>Welcome to Terarium</h3>
							<div>
								Get started by creating a
								<Button
									label="new project"
									class="p-button-text new-project-button"
									@click="isNewProjectModalVisible = true"
								/>. Your projects will be displayed on this page.
							</div>
						</section>
						<div v-else class="carousel">
							<div class="chevron-left" @click="scroll('left', $event)">
								<i class="pi pi-chevron-left" />
							</div>
							<div class="chevron-right" @click="scroll('right', $event)">
								<i class="pi pi-chevron-right" />
							</div>
							<ul v-if="isLoadingProjects">
								<li v-for="i in [0, 1, 2]" :key="i">
									<tera-project-card />
								</li>
							</ul>
							<ul v-else>
								<li v-for="project in projects" :key="project.id">
									<tera-project-card
										:project="project"
										@click="openProject(project.id)"
										@removed="removeProject"
									/>
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
			<section class="papers" v-if="!(projects && isEmpty(projects))">
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
							<li v-for="(document, j) in project.relatedDocuments" :key="j">
								<tera-document-card :document="document" @click="selectDocument(document)" />
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
							<li v-for="i in [0, 1, 2, 3, 4, 5]" :key="i">
								<tera-document-card />
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
				<tera-selected-document-pane
					class="selected-document-pane"
					:selected-document="selectedDocument"
					@close="close()"
				/>
			</div>
		</div>
		<!-- New project modal -->
		<Teleport to="body">
			<tera-modal
				v-if="isNewProjectModalVisible"
				class="modal"
				@modal-mask-clicked="isNewProjectModalVisible = false"
			>
				<template #header>
					<h4>Create project</h4>
				</template>
				<template #default>
					<form>
						<label for="new-project-name">Name</label>
						<InputText
							id="new-project-name"
							type="text"
							v-model="newProjectName"
							placeholder="What do you want to call your project?"
						/>

						<label for="new-project-description">Description</label>
						<Textarea
							id="new-project-description"
							rows="5"
							v-model="newProjectDescription"
							placeholder="Add a short description"
						/>
					</form>
				</template>
				<template #footer>
					<Button @click="createNewProject">Create</Button>
					<Button class="p-button-secondary" @click="isNewProjectModalVisible = false"
						>Cancel</Button
					>
				</template>
			</tera-modal>
		</Teleport>
	</main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import TeraSelectedDocumentPane from '@/components/documents/tera-selected-document-pane.vue';
import { IProject } from '@/types/Project';
import { XDDSearchParams } from '@/types/XDD';
import { DocumentType } from '@/types/Document';
import { searchXDDDocuments } from '@/services/data';
import useResourcesStore from '@/stores/resources';
import useQueryStore from '@/stores/query';
import TeraDocumentCard from '@/components/home/tera-document-card.vue';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import Textarea from 'primevue/textarea';
import TabView from 'primevue/tabview';
import TabPanel from 'primevue/tabpanel';
import TeraModal from '@/components/widgets/tera-modal.vue';
import { useRouter } from 'vue-router';
import * as ProjectService from '@/services/project';
import useAuthStore from '@/stores/auth';
import { RouteName } from '@/router/routes';
import Skeleton from 'primevue/skeleton';
import { isEmpty } from 'lodash';
import TeraProjectCard from '@/components/home/tera-project-card.vue';

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

	projects.value = ((await ProjectService.home()) ?? []).slice().reverse();

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

function openProject(projectId: string) {
	router.push({ name: RouteName.ProjectRoute, params: { projectId } });
}

async function createNewProject() {
	const author = auth.name ?? '';
	const project = await ProjectService.create(
		newProjectName.value,
		newProjectDescription.value,
		author
	);
	if (project) {
		openProject(project.id);
		isNewProjectModalVisible.value = false;
	}
}

function listAuthorNames(authors) {
	return authors.map((author) => author.name).join(', ');
}

const removeProject = (projectId: IProject['id']) => {
	projects.value = projects.value?.filter((project) => project.id !== projectId);
};
</script>

<style scoped>
.menu {
	overflow-y: auto;
	overflow-x: hidden;
	flex: 1;
	padding: 0;
	display: flex;
	flex-direction: column;
}

section {
	background-color: var(--surface-section);
	color: var(--text-color-secondary);
	padding: 1rem;
}

.papers {
	background: linear-gradient(180deg, #8bd4af1a, #d5e8e5);
	padding: 1rem;
	border-top: 1px solid var(--gray-100);
	flex-grow: 1;
}

.papers p {
	color: var(--text-color-primary);
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
	padding: 0 0 0 0;
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
	top: 0.5rem;
	height: 22rem;
	border-radius: 0rem 10rem 10rem 0rem;
}

.chevron-right {
	right: -1rem;
	top: 0.5em;
	height: 22em;
	border-radius: 10rem 0rem 0rem 10rem;
}

.papers .chevron-left,
.papers .chevron-right {
	height: 22rem;
	top: 0.4rem;
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

.no-projects {
	background-color: var(--gray-0);
	background-image: radial-gradient(var(--gray-200) 10%, transparent 11%);
	background-size: 12px 12px;
	background-position: 0 0;
	background-repeat: repeat;
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
	border-radius: var(--border-radius-big);
	transition: background-color 0.2s ease, box-shadow 0.2s ease;
	cursor: pointer;
}

.new-project-card > p {
	text-align: center;
	color: var(--text-color-primary);
}

.new-project-card img {
	margin: auto;
}

.new-project-card:hover {
	background-color: var(--surface);
	box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.1);
}

.new-project-button {
	padding: 0;
}

#new-project-name,
#new-project-description {
	border-color: var(--surface-border);
}
</style>
