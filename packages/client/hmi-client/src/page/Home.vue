<template>
	<main>
		<section class="menu">
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
							:selected-items-label="`{0} columns displayed`"
							optionLabel="header"
							@update:modelValue="onToggle"
							placeholder="Add or remove columns"
							class="p-inputtext-sm"
						/>
						<SelectButton
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
					</section>
					<section class="list-of-projects">
						<div v-if="!isLoadingProjects && isEmpty(tab.projects)" class="no-projects">
							<img src="@assets/svg/seed.svg" alt="" />
							<template v-if="tab.title === TabTitles.MyProjects">
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
							<template v-else-if="tab.title === TabTitles.PublicProjects">
								<h3>You don't have any shared projects</h3>
								<p>Shared projects will be displayed on this page</p>
							</template>
						</div>
						<ul v-else-if="view === ProjectsView.Cards" class="project-cards-grid">
							<li v-for="project in tab.projects" :key="project.id">
								<tera-project-card
									v-if="project.id"
									:project="project"
									@click="openProject(project.id)"
									@forked-project="(forkedProject) => openProject(forkedProject.id)"
								/>
							</li>
						</ul>
						<!--
							:is-loading="isLoadingProjects"
							:amount-of-cards="tab.projects.length"
						-->
						<tera-project-table
							v-else-if="view === ProjectsView.Table"
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
		<Dialog :header="`Remove ${selectedMenuProject?.name}`" v-model:visible="isRemoveDialogVisible">
			<p>
				You are about to remove project <em>{{ selectedMenuProject?.name }}</em
				>.
			</p>
			<p>Are you sure?</p>
			<template #footer>
				<Button label="Cancel" class="p-button-secondary" @click="isRemoveDialogVisible = false" />
				<Button label="Remove project" @click="removeProject" />
			</template>
		</Dialog>
		<tera-share-project
			v-if="selectedMenuProject"
			v-model="isShareDialogVisible"
			:project="selectedMenuProject"
		/>
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
.menu {
	overflow-y: auto;
	overflow-x: hidden;
	flex: 1;
	padding: 0;
	display: flex;
	flex-direction: column;
}

.list-of-projects {
	flex: 1;
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
	background-color: #e9e9e9;
	border: 1px solid var(--surface-border);
	padding: 16px;
	display: flex;
	align-items: center;
	gap: 16px;
}

.filter-and-sort label {
	padding-right: 0.25rem;
	font-size: var(--font-caption);
}

.filter-and-sort > * {
	height: 40px;
}

.p-buttonset {
	margin-left: auto;
}

.project-cards-grid {
	background-color: #f9f9f9;
	display: grid;
	grid-template-columns: repeat(auto-fill, minmax(16rem, 1fr));
	gap: 16px;
	padding: 16px;
	list-style: none;
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
