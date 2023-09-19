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
					/>
				</header>
				<TabView>
					<TabPanel v-for="(tab, i) in projectsTabs" :header="tab.title" :key="i">
						<section class="filter-and-sort">
							<!-- TODO: Add project search back in once we are ready
								<span class="p-input-icon-left">
								<i class="pi pi-filter" />
								<InputText
									v-model="searchQuery"
									size="small"
									class="p-inputtext-sm"
									placeholder="Filter by keyword"
								/>
							</span> -->
							<span v-if="view === ProjectsView.Cards"
								><label>Sort by:</label>
								<Dropdown
									v-model="selectedSort"
									:options="sortOptions"
									@update:model-value="tab.projects = myFilteredSortedProjects"
									class="p-inputtext-sm"
								/>
							</span>
							<MultiSelect
								v-if="view === ProjectsView.Table"
								:modelValue="selectedColumns"
								:options="columns"
								:maxSelectedLabels="1"
								optionLabel="header"
								@update:modelValue="onToggle"
								placeholder="Add or remove columns"
								class="p-inputtext-sm"
							/>
							<span class="p-buttonset">
								<Button
									class="p-button-secondary p-button-sm"
									label="Cards"
									icon="pi pi-image"
									@click="view = ProjectsView.Cards"
									:active="view === ProjectsView.Cards"
								/>
								<Button
									class="p-button-secondary p-button-sm"
									label="Table"
									icon="pi pi-list"
									@click="view = ProjectsView.Table"
									:active="view === ProjectsView.Table"
								/>
							</span>
						</section>
						<section class="list-of-projects">
							<div v-if="!isLoadingProjects && isEmpty(tab.projects)" class="no-projects">
								<img src="@assets/svg/seed.svg" alt="" />
								<template v-if="tab.title === 'My projects'">
									<h3>Welcome to Terarium</h3>
									<div>
										Get started by creating a
										<Button
											label="new project"
											class="p-button-text new-project-button"
											@click="isNewProjectModalVisible = true"
										/>. Your projects will be displayed on this page.
									</div>
								</template>
								<template v-else-if="tab.title === 'Shared projects'">
									<h3>You don't have any shared projects</h3>
									<p>Shared projects will be displayed on this page</p>
								</template>
							</div>
							<div v-else-if="view === ProjectsView.Cards" class="carousel">
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
									<li v-for="project in tab.projects" :key="project.id">
										<tera-project-card
											v-if="project.id"
											:project="project"
											:project-menu-items="projectMenuItems"
											@click="openProject(project.id)"
											@update-chosen-project-menu="selectedProjectMenu = project"
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
							<DataTable
								v-else-if="view === ProjectsView.Table"
								v-model:selection="selectedProject"
								:value="tab.projects"
								paginator
								:rows="10"
								selectionMode="single"
								dataKey="id"
								:rowsPerPageOptions="[10, 20, 50]"
							>
								<Column style="width: 0">
									<template #body="{ data }">
										<Button
											:icon="
												selectedProject === data ? 'pi pi-chevron-down' : 'pi pi-chevron-right'
											"
											class="p-button-icon-only p-button-text p-button-rounded"
											@click.stop="
												selectedProject === data
													? (selectedProject = null)
													: (selectedProject = data)
											"
										/>
									</template>
								</Column>
								<Column
									v-for="(col, index) in selectedColumns"
									:field="col.field"
									:header="col.header"
									:sortable="col.field !== 'stats'"
									:key="index"
									:style="`width: ${getColumnWidth(col.field)}%`"
								>
									<template v-if="col.field === 'name'" #body="{ data }">
										<a class="project-title-link" @click.stop="openProject(data.id)">{{
											data.name
										}}</a>
									</template>
									<template v-else-if="col.field === 'description'" #body="{ data }">
										<div
											:class="
												selectedProject === data
													? 'project-description-expanded'
													: 'project-description'
											"
										>
											{{ data.description }}
										</div>
									</template>
									<template v-else-if="col.field === 'stats'" #body="{ data }">
										<div class="stats">
											<span><i class="pi pi-user" />1</span>
											<span
												><i class="pi pi-file" /> {{ data.metadata?.['publications-count'] }}</span
											>
											<span>
												<dataset-icon fill="var(--text-color-secondary)" />
												{{ data.metadata?.['datasets-count'] }}
											</span>
											<span
												><i class="pi pi-share-alt" /> {{ data.metadata?.['models-count'] }}</span
											>
										</div>
									</template>
									<!--FIXME: There is no 'last updated' property in project yet-->
									<template v-else-if="col.field === 'timestamp'" #body="{ data }">
										{{ formatDdMmmYyyy(data.timestamp) }}
									</template>
								</Column>
								<Column style="width: 0">
									<template #body="{ data, index }">
										<Button
											icon="pi pi-ellipsis-v"
											class="project-options p-button-icon-only p-button-text p-button-rounded"
											@click.stop="(event) => toggleProjectMenu(event, index, data)"
										/>
										<Menu ref="projectMenu" :model="projectMenuItems" :popup="true" />
									</template>
								</Column>
							</DataTable>
							<Dialog
								:header="`Remove ${selectedProjectMenu?.name}`"
								v-model:visible="isRemoveDialog"
							>
								<p>
									You are about to remove project <em>{{ selectedProjectMenu?.name }}</em
									>.
								</p>
								<p>Are you sure?</p>
								<template #footer>
									<Button label="Cancel" class="p-button-secondary" @click="closeRemoveDialog" />
									<Button label="Remove project" @click="removeProject" />
								</template>
							</Dialog>
						</section>
					</TabPanel>
				</TabView>
			</section>
			<section class="papers">
				<header>
					<h3>Papers related to your projects</h3>
				</header>
				<div v-for="project in projectsWithRelatedDocuments" :key="project.name">
					<p>{{ project.name }}</p>
					<div class="carousel">
						<div class="chevron-left" @click="scroll('left', $event)">
							<i class="pi pi-chevron-left" />
						</div>
						<div class="chevron-right" @click="scroll('right', $event)">
							<i class="pi pi-chevron-right" />
						</div>
						<ul>
							<li v-for="document in project.relatedDocuments" :key="document.gddId">
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
				@modal-enter-press="createNewProject"
			>
				<template #header>
					<h4>Create project</h4>
				</template>
				<template #default>
					<form @submit.prevent>
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
import { computed, ref, onMounted, watch } from 'vue';
import TeraSelectedDocumentPane from '@/components/documents/tera-selected-document-pane.vue';
import { Document, Project } from '@/types/Types';
import { getRelatedDocuments } from '@/services/data';
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
import Dropdown from 'primevue/dropdown';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import MultiSelect from 'primevue/multiselect';
import { formatDdMmmYyyy } from '@/utils/date';
import DatasetIcon from '@/assets/svg/icons/dataset.svg?component';
import { logger } from '@/utils/logger';
import Dialog from 'primevue/dialog';
import Menu from 'primevue/menu';

enum ProjectsView {
	Cards,
	Table
}

const selectedSort = ref('Last updated (descending)');
const sortOptions = [
	'Last updated (descending)',
	'Last updated (ascending)',
	'Creation date (descending)',
	'Creation date (ascending)',
	'Alphabetical'
];

const projects = ref<Project[]>([]);
const view = ref(ProjectsView.Cards);

const myFilteredSortedProjects = computed(() => {
	const filtered = projects.value;
	if (!filtered) return [];

	if (selectedSort.value === 'Alphabetical') {
		filtered.sort((a, b) => a.name.toLowerCase().localeCompare(b.name.toLowerCase()));
	}
	// FIXME: Last updated and creation date are the same at the moment
	else if (
		selectedSort.value === 'Last updated (descending)' ||
		selectedSort.value === 'Creation date (descending)'
	) {
		filtered.sort((a, b) =>
			a.timestamp && b.timestamp
				? new Date(b.timestamp).valueOf() - new Date(a.timestamp).valueOf()
				: -1
		);
	} else if (
		selectedSort.value === 'Last updated (ascending)' ||
		selectedSort.value === 'Creation date (ascending)'
	) {
		filtered.sort((a, b) =>
			a.timestamp && b.timestamp
				? new Date(a.timestamp).valueOf() - new Date(b.timestamp).valueOf()
				: -1
		);
	}
	return filtered;
});

const projectsTabs = ref<{ title: string; projects: Project[] }[]>([
	{ title: 'My projects', projects: [] },
	{ title: 'Shared projects', projects: [] } // Keep shared projects empty for now
]);

// Table view
const columns = ref([
	{ field: 'name', header: 'Project title' },
	{ field: 'description', header: 'Description' },
	{ field: 'username', header: 'Author' },
	{ field: 'stats', header: 'Stats' },
	{ field: 'timestamp', header: 'Created' },
	{ field: 'lastUpdated', header: 'Last updated' } // Last update property doesn't exist yet
]);
const selectedColumns = ref(columns.value);
const onToggle = (val) => {
	selectedColumns.value = columns.value.filter((col) => val.includes(col));
};

/*
 * User Menu
 */
const selectedProject = ref<Project | null>(null);
const selectedProjectMenu = ref<Project | null>(null);

const isRemoveDialog = ref(false);
const openRemoveDialog = () => {
	isRemoveDialog.value = true;
};
const closeRemoveDialog = () => {
	isRemoveDialog.value = false;
};
const projectMenu = ref();
const projectMenuItems = ref([{ label: 'Remove', command: openRemoveDialog }]);
const toggleProjectMenu = (event, index: number, project: Project) => {
	projectMenu.value[index].toggle(event);
	selectedProjectMenu.value = project;
};

const removeProject = async () => {
	if (!selectedProjectMenu.value?.id) return;
	const { name, id } = selectedProjectMenu.value;

	const isDeleted = await ProjectService.remove(id);
	closeRemoveDialog();
	if (isDeleted) {
		// Should refetch instead - will finalize once project composable is merged
		projects.value = projects.value?.filter((project) => project.id !== id);
		logger.info(`The project ${name} was removed`, { showToast: true });
	} else {
		logger.error(`Unable to delete the project ${name}`, { showToast: true });
	}
};

function getColumnWidth(columnField: string) {
	switch (columnField) {
		case 'description':
			return 55;
		case 'name':
			return 25;
		default:
			return 5;
	}
}

/**
 * Display Related Documents for the latest 3 project with at least one publication.
 */
type RelatedDocumentFromProject = { name: Project['name']; relatedDocuments: Document[] };
const projectsWithRelatedDocuments = ref([] as RelatedDocumentFromProject[]);
async function updateProjectsWithRelatedDocuments(newProjects: Project[]) {
	projectsWithRelatedDocuments.value = await Promise.all(
		newProjects
			// filter out the ones with no publications
			?.filter((project) => parseInt(project?.metadata?.['publications-count'] ?? '0', 10) > 0)
			// get the first three project with a publication
			.slice(0, 3)
			// get the related documents for each project first publication
			.map(async (project) => {
				let relatedDocuments = [] as Document[];
				if (project.id) {
					// Fetch the publications for the project
					const publications = await ProjectService.getPublicationAssets(project.id);
					if (!isEmpty(publications)) {
						// Fetch the related documents for the first publication
						relatedDocuments = await getRelatedDocuments(publications[0].xdd_uri);
					}
				}
				return { name: project.name, relatedDocuments } as RelatedDocumentFromProject;
			}) ?? ([] as RelatedDocumentFromProject[])
	);
}
watch(projects, (newProjects) => newProjects && updateProjectsWithRelatedDocuments(newProjects));

const selectedDocument = ref<Document>();
const resourcesStore = useResourcesStore();
const queryStore = useQueryStore();
const router = useRouter();
const auth = useAuthStore();

const isNewProjectModalVisible = ref(false);
const newProjectName = ref('');
const newProjectDescription = ref('');
const isLoadingProjects = ref(true);
// const searchQuery = ref('');

onMounted(async () => {
	// Clear all...
	resourcesStore.reset(); // Project related resources saved.
	queryStore.reset(); // Facets queries.

	projects.value = (await ProjectService.getAll()) ?? [];
	projectsTabs.value[0].projects = myFilteredSortedProjects.value;
	isLoadingProjects.value = false;
});

const selectDocument = (item: Document) => {
	const itemID = item as Document;
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
		cardListElement.style.marginLeft === '' ? '0' : cardListElement.style.marginLeft;
	const currentMarginLeft = parseFloat(marginLeftString);
	const changeInRem = direction === 'right' ? -SCROLL_INCREMENT_IN_REM : SCROLL_INCREMENT_IN_REM;
	const newMarginLeft = currentMarginLeft + changeInRem;
	// Don't let the list scroll far enough left that we see space before the
	//	first card.
	cardListElement.style.marginLeft = `${newMarginLeft > 0 ? 0 : newMarginLeft}rem`;
};

function openProject(projectId: string) {
	router.push({ name: RouteName.Project, params: { projectId } });
}

async function createNewProject() {
	const author = auth.name ?? '';
	const project = await ProjectService.create(
		newProjectName.value,
		newProjectDescription.value,
		author
	);
	if (project?.id) {
		isNewProjectModalVisible.value = false;
		openProject(project.id);
	}
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
	padding: 0;
	display: flex;
	flex-direction: column;
}

.projects {
	background-color: var(--surface-section);
	color: var(--text-color-secondary);
	padding: 1rem;
}

.list-of-projects {
	min-height: 21rem;
}

.stats {
	display: flex;
	width: fit-content;
	gap: 0.75rem;
	font-size: var(--font-caption);
	vertical-align: bottom;
}

.stats span {
	display: flex;
	gap: 0.1rem;
	align-items: center;
	width: 2rem;
}

.p-dropdown,
.p-multiselect {
	min-width: 15rem;
}

.p-datatable {
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
}

.p-datatable:deep(.p-datatable-tbody > tr > td),
.p-datatable:deep(.p-datatable-tbody > tr .project-description),
.p-datatable:deep(.p-datatable-thead > tr > th) {
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
	vertical-align: top;
}

.p-datatable:deep(.p-datatable-tbody > tr .project-description-expanded) {
	white-space: wrap;
}

.p-datatable:deep(.p-datatable-thead > tr > th) {
	padding: 1rem 0.5rem;
	background-color: var(--surface-ground);
}

.p-datatable:deep(.p-datatable-tbody > tr:not(.p-highlight):focus) {
	background-color: transparent;
}

.p-datatable:deep(.p-datatable-tbody > tr > td) {
	color: var(--text-color-secondary);
	padding: 0.5rem;
	max-width: 32rem;
}

.p-datatable:deep(.p-datatable-tbody > tr > td:not(:first-child, :last-child)) {
	padding-top: 1rem;
}

.p-datatable:deep(.p-datatable-tbody > tr .project-options) {
	visibility: hidden;
}

.p-datatable:deep(.p-datatable-tbody > tr:hover .project-options) {
	visibility: visible;
}

.p-datatable:deep(.p-datatable-tbody > tr > td > a) {
	color: var(--text-color-primary);
	font-weight: var(--font-weight-semibold);
}

.p-datatable:deep(.p-datatable-tbody > tr > td > a) {
	color: var(--text-color-primary);
	font-weight: var(--font-weight-semibold);
	cursor: pointer;
}

.p-datatable:deep(.p-datatable-tbody > tr > td > a:hover) {
	color: var(--primary-color);
	text-decoration: underline;
}

.p-multiselect:deep(.p-multiselect-label) {
	/* Matches exact size of small dropdown */
	font-size: 12.25px;
	padding: 0.875rem;
}

.filter-and-sort {
	background-color: var(--surface-ground);
	border-radius: var(--border-radius);
	border: 1px solid var(--surface-border-light);
	padding: 0.75rem;
	display: flex;
	align-items: center;
	gap: 1rem;
}

.filter-and-sort label {
	padding-right: 0.25rem;
	font-size: var(--font-caption);
}

.p-buttonset {
	margin-left: auto;
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
	padding: 0;
}

.p-tabview:deep(.p-tabview-panel) {
	display: flex;
	flex-direction: column;
	gap: 1rem;
	margin: 1rem 0;
}

header svg {
	color: var(--primary-color);
	margin-right: 0.5rem;
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

.carousel {
	position: relative;
	display: flex;
}

.carousel ul {
	align-items: center;
	display: flex;
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
}

.chevron-left {
	left: -1rem;
	border-radius: 0rem 10rem 10rem 0rem;
}

.chevron-right {
	right: -1rem;
	border-radius: 10rem 0rem 0rem 10rem;
}

.carousel:hover .chevron-left,
.carousel:hover .chevron-right {
	background-color: var(--chevron-hover);
}

.carousel:hover .chevron-left > .pi-chevron-left,
.carousel:hover .chevron-right > .pi-chevron-right {
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
ul {
	gap: 1.5rem;
	transition: margin-left 0.8s;
}

li {
	list-style: none;
}

.selected-document-modal-mask {
	position: fixed;
	z-index: 998;
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
</style>
