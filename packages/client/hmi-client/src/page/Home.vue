<template>
	<main>
		<div class="scrollable">
			<header>
				<section>
					<h3>From data to discovery</h3>
					<p>
						Accelerate scientific modeling and simulation using AI. Search available knowledge,
						enhance extracted models and data, and test scenarios to simulate real-world problems.
					</p>
					<!--Placeholder - button is disabled for now-->
					<Button
						label="Show me around"
						icon="pi pi-play"
						icon-pos="right"
						outlined
						:disabled="true"
					/>
				</section>
			</header>
			<section class="menu">
				<TabView>
					<TabPanel v-for="(tab, i) in projectsTabs" :header="tab.title" :key="i">
						<section class="filter-and-sort">
							<div v-if="!isEmpty(tab.projects)">
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
									:selected-items-label="`{0} columns displayed`"
									optionLabel="header"
									@update:modelValue="onToggle"
									placeholder="Add or remove columns"
									class="p-inputtext-sm"
								/>
							</div>
							<div>
								<SelectButton
									v-if="!isEmpty(tab.projects)"
									:model-value="view"
									@change="if ($event.value) view = $event.value;"
									:options="viewOptions"
									option-value="value"
								>
									<template #option="slotProps">
										<i :class="`${slotProps.option.icon} p-button-icon-left`" />
										<span class="p-button-label">{{ slotProps.option.value }}</span>
									</template>
								</SelectButton>
								<Button
									icon="pi pi-plus"
									label="New project"
									@click="isNewProjectModalVisible = true"
								/>
							</div>
						</section>
						<section class="projects">
							<div v-if="!isLoadingProjects && isEmpty(tab.projects)" class="no-projects">
								<img src="@assets/svg/seed.svg" alt="" />
								<template v-if="tab.title === TabTitles.MyProjects">
									<p>
										Get started by creating a
										<Button
											label="new project"
											class="p-button-text new-project-button"
											@click="isNewProjectModalVisible = true"
										/>.
									</p>
									<p>Your projects will be displayed on this page.</p>
								</template>
								<template v-else-if="tab.title === TabTitles.PublicProjects">
									<h3>You don't have any shared projects</h3>
									<p>Shared projects will be displayed on this page</p>
								</template>
							</div>
							<ul v-else-if="view === ProjectsView.Cards" class="project-cards-grid">
								<template v-if="isLoadingProjects">
									<li v-for="i in 3" :key="i">
										<tera-project-card />
									</li>
								</template>
								<li v-else v-for="project in tab.projects" :key="project.id">
									<tera-project-card
										v-if="project.id"
										:project="project"
										@click="openProject(project.id)"
										@forked-project="(forkedProject) => openProject(forkedProject.id)"
									/>
								</li>
							</ul>
							<tera-project-table
								v-else-if="view === ProjectsView.Table"
								Y
								:projects="tab.projects"
								:selected-columns="selectedColumns"
								@open-project="openProject"
							/>
						</section>
					</TabPanel>
				</TabView>
			</section>
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
						<Button severity="secondary" outlined @click="isNewProjectModalVisible = false"
							>Cancel</Button
						>
					</template>
				</tera-modal>
			</Teleport>
			<Dialog
				:header="`Remove ${selectedMenuProject?.name}`"
				v-model:visible="isRemoveDialogVisible"
			>
				<p>
					You are about to remove project <em>{{ selectedMenuProject?.name }}</em
					>.
				</p>
				<p>Are you sure?</p>
				<template #footer>
					<Button
						label="Cancel"
						class="p-button-secondary"
						@click="isRemoveDialogVisible = false"
					/>
					<Button label="Remove project" @click="removeProject" />
				</template>
			</Dialog>
			<tera-share-project
				v-if="selectedMenuProject"
				v-model="isShareDialogVisible"
				:project="selectedMenuProject"
			/>
		</div>
	</main>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue';
import useQueryStore from '@/stores/query';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import Textarea from 'primevue/textarea';
import TabView from 'primevue/tabview';
import TabPanel from 'primevue/tabpanel';
import TeraModal from '@/components/widgets/tera-modal.vue';
import { useRouter } from 'vue-router';
import useAuthStore from '@/stores/auth';
import { RouteName } from '@/router/routes';
import { isEmpty } from 'lodash';
import TeraProjectTable from '@/components/home/tera-project-table.vue';
import TeraProjectCard from '@/components/home/tera-project-card.vue';
import TeraShareProject from '@/components/widgets/share-project/tera-share-project.vue';
import { useProjects } from '@/composables/project';
import Dropdown from 'primevue/dropdown';
import MultiSelect from 'primevue/multiselect';
import { logger } from '@/utils/logger';
import Dialog from 'primevue/dialog';
import { IProject } from '@/types/Project';
import { useProjectMenu } from '@/composables/project-menu';
import SelectButton from 'primevue/selectbutton';

const { isShareDialogVisible, isRemoveDialogVisible, selectedMenuProject } = useProjectMenu();

enum ProjectsView {
	Cards = 'Cards',
	Table = 'Table'
}

enum TabTitles {
	MyProjects = 'My projects',
	PublicProjects = 'Public projects',
	SampleProjects = 'Sample projects'
}

const selectedSort = ref('Last updated (descending)');
const sortOptions = [
	'Last updated (descending)',
	'Last updated (ascending)',
	'Creation date (descending)',
	'Creation date (ascending)',
	'Alphabetical'
];

const view = ref(ProjectsView.Cards);
const viewOptions = ref([
	{ value: ProjectsView.Cards, icon: 'pi pi-image' },
	{ value: ProjectsView.Table, icon: 'pi pi-list' }
]);

const myFilteredSortedProjects = computed(() => {
	const projects = useProjects().allProjects.value;
	if (!projects) return [];
	const myProjects = projects.filter(({ publicProject }) => publicProject === false);
	return filterAndSortProjects(myProjects);
});

const publicFilteredSortedProjects = computed(() => {
	const projects = useProjects().allProjects.value;
	if (!projects) return [];
	const publicProjects = projects.filter(({ publicProject }) => publicProject === true);
	return filterAndSortProjects(publicProjects);
});

function filterAndSortProjects(projects: IProject[]) {
	if (!projects) return [];

	if (selectedSort.value === 'Alphabetical') {
		projects.sort((a, b) => a.name.toLowerCase().localeCompare(b.name.toLowerCase()));
	}
	// FIXME: Last updated and creation date are the same at the moment
	else if (
		selectedSort.value === 'Last updated (descending)' ||
		selectedSort.value === 'Creation date (descending)'
	) {
		projects.sort((a, b) =>
			a.timestamp && b.timestamp
				? new Date(b.timestamp).valueOf() - new Date(a.timestamp).valueOf()
				: -1
		);
	} else if (
		selectedSort.value === 'Last updated (ascending)' ||
		selectedSort.value === 'Creation date (ascending)'
	) {
		projects.sort((a, b) =>
			a.timestamp && b.timestamp
				? new Date(a.timestamp).valueOf() - new Date(b.timestamp).valueOf()
				: -1
		);
	}
	return projects;
}

const projectsTabs = computed<{ title: string; projects: IProject[] }[]>(() => [
	{ title: TabTitles.MyProjects, projects: myFilteredSortedProjects.value },
	{ title: TabTitles.PublicProjects, projects: publicFilteredSortedProjects.value },
	{ title: TabTitles.SampleProjects, projects: [] }
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

const removeProject = async () => {
	if (!selectedMenuProject.value) return;
	const { name, id } = selectedMenuProject.value;
	const isDeleted = await useProjects().remove(id);
	isRemoveDialogVisible.value = false;
	if (isDeleted) {
		useProjects().getAll();
		logger.info(`The project ${name} was removed`, { showToast: true });
	} else {
		logger.error(`Unable to delete the project ${name}`, { showToast: true });
	}
};

const queryStore = useQueryStore();
const router = useRouter();
const auth = useAuthStore();

const isNewProjectModalVisible = ref(false);
const newProjectName = ref('');
const newProjectDescription = ref('');
const isLoadingProjects = computed(() => !useProjects().allProjects.value);

function openProject(projectId: string) {
	router.push({ name: RouteName.Project, params: { projectId } });
}

async function createNewProject() {
	const author = auth.user?.name ?? '';
	const project = await useProjects().create(
		newProjectName.value,
		newProjectDescription.value,
		author
	);
	if (project?.id) {
		isNewProjectModalVisible.value = false;
		openProject(project.id);
	}
}

onMounted(() => {
	// Clear all...
	queryStore.reset(); // Facets queries.
});
</script>

<style scoped>
main > .scrollable {
	width: 100%;
	display: flex;
	flex-direction: column;
	overflow: auto;
}

header {
	display: flex;
	align-items: center;
	padding: 1.5rem;
	min-height: 240px;
	background: url('@/assets/svg/terarium-logo-outline.svg'),
		radial-gradient(105.92% 916.85% at 101.3% -5.92%, #75d5c8 0%, white 100%);
	background-repeat: no-repeat;
	background-size: 25%, 100%;
	background-position: right 100px top -60px, 100%;
}

header h3 {
	font-size: 24px;
	margin-bottom: 1rem;
}

header p {
	max-width: 40%;
	line-height: 1.5;
}

header > section > button {
	margin-top: 2rem;
}

.menu {
	display: flex;
	flex-direction: column;
	flex: 1;
}

.p-tabview {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.p-tabview:deep(.p-tabview-nav-container) {
	position: sticky;
	top: 0;
	z-index: 1;
}

.p-tabview:deep(.p-tabview-nav li .p-tabview-nav-link:focus) {
	background-color: transparent;
}

.p-tabview:deep(.p-tabview-panels) {
	padding: 0;
	flex: 1;
	background-color: #f9f9f9;
}

.p-dropdown,
.p-multiselect {
	min-width: 15rem;
}

.p-multiselect:deep(.p-multiselect-label) {
	/* Matches exact size of small dropdown */
	font-size: 12.25px;
	padding: 0.875rem;
}

.filter-and-sort {
	position: sticky;
	z-index: 1;
	background-color: #e9e9e9;
	border-top: 1px solid var(--surface-border);
	border-bottom: 1px solid var(--surface-border);
	padding: 16px;
	display: flex;
	justify-content: space-between;
	align-items: center;
	gap: 16px;
	/*Accomodate for height of projects tabs*/
	top: 44px;
}

.filter-and-sort label {
	padding-right: 0.25rem;
	font-size: var(--font-caption);
}

.filter-and-sort > div {
	display: flex;
	gap: 16px;
	height: 40px;
}

.filter-and-sort > div:last-child {
	margin-left: auto;
}

.project-cards-grid {
	display: grid;
	grid-template-columns: repeat(auto-fill, minmax(16rem, 1fr));
	gap: 16px;
	padding: 16px;
	list-style: none;
}

header svg {
	color: var(--primary-color);
	margin-right: 0.5rem;
}
.no-projects {
	margin-top: 8rem;
	color: var(--text-color-subdued);
}
.no-projects > * {
	margin: auto;
	text-align: center;
}

.no-projects > img {
	height: 10rem;
	margin-bottom: 2rem;
}

a {
	color: var(--primary-color);
}

.new-project-button {
	padding: 0;
}

#new-project-name,
#new-project-description {
	border-color: var(--surface-border);
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
</style>
