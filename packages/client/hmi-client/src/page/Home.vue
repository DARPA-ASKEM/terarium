<template>
	<main>
		<!-- Top banner -->
		<div class="scrollable">
			<header>
				<!-- Welcome text -->
				<section class="w-full">
					<h3>From data to discovery</h3>
					<p>
						Accelerate scientific modeling and simulation using AI. Search available knowledge, enhance extracted models
						and data, and test scenarios to simulate real-world problems.
					</p>
					<!--Placeholder - button is disabled for now-->
					<!-- <Button
						label="Get started"
						icon="pi pi-play"
						icon-pos="right"
						outlined
						:disabled="true"
					/> -->
				</section>

				<!-- Video thumbnail image -->
				<section
					v-if="!showVideo"
					class="z-1 col-3 flex justify-content-end"
					@click="showVideo = true"
					@keypress.enter="showVideo = true"
					@keypress.space="showVideo = true"
				>
					<div class="video-thumbnail flex flex-column align-items-center">
						<span class="play-symbol mt-4 mb-4">▶</span>
						<span class="header mb-1">Terarium overview</span>
						<span class="sub-header"> A demonstration of basic features </span>
					</div>
				</section>

				<!-- Video container -->
				<section v-else class="col-3 flex justify-content-end">
					<video controls ref="introVideo" class="video-container" height="200px">
						<source src="https://videos.terarium.ai/terarium.mp4" type="video/mp4" />
						<track src="" kind="captions" srclang="en" label="English" />
						Your browser does not support the video tag.
					</video>
				</section>
			</header>

			<!-- Tab section: My projects, Public projects, Sample projects -->
			<section class="menu">
				<TabView @tab-change="tabChange" :active-index="activeTabIndex" :key="activeTabIndex">
					<TabPanel v-for="(tab, i) in TabTitles" :header="tab" :key="i">
						<section class="filter-and-sort">
							<div class="mr-3">
								<tera-input-text
									v-model="searchProjectsQuery"
									placeholder="Search for projects"
									id="searchProject"
									:icon="isSearchLoading ? 'pi pi-spin pi-spinner' : 'pi pi-search'"
								/>
							</div>
							<div>
								<span v-if="view === ProjectsView.Cards">
									<Dropdown v-model="selectedSort" :options="sortOptions" class="sort-options-dropdown" />
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
									v-if="!isEmpty(searchedAndFilterProjects)"
									:model-value="view"
									@change="selectChange"
									:options="viewOptions"
									option-value="value"
								>
									<template #option="slotProps">
										<span class="p-button-label">{{ slotProps.option.value }}</span>
									</template>
								</SelectButton>
								<Button
									icon="pi pi-upload"
									class="secondary-button"
									label="Upload project"
									@click="openUploadProjectModal"
								/>
								<Button icon="pi pi-plus" label="New project" @click="openCreateProjectModal" />
							</div>
						</section>
						<section class="projects">
							<div v-if="!isLoadingProjects && isEmpty(searchedAndFilterProjects)" class="no-projects">
								<Vue3Lottie :animationData="EmptySeed" :height="200" :width="200" />
								<p class="mt-4">
									<template v-if="tab === TabTitles.MyProjects">Get started by creating a new project</template>
									<template v-if="tab === TabTitles.SampleProjects">Sample projects coming soon</template>
									<template v-if="tab === TabTitles.PublicProjects">You don't have any shared projects</template>
								</p>
							</div>
							<ul v-else-if="view === ProjectsView.Cards" class="project-cards-grid">
								<template v-if="cloningProjects.length && !isLoadingProjects">
									<li v-for="item in cloningProjects" :key="item.id">
										<tera-project-card v-if="item.id" :project="item" :is-copying="true" />
									</li>
								</template>
								<template v-if="isLoadingProjects">
									<li v-for="i in 3" :key="i">
										<tera-project-card />
									</li>
								</template>
								<li v-else v-for="project in searchedAndFilterProjects" :key="project.id">
									<tera-project-card
										v-if="project.id"
										:project="project"
										@click="openProject(project.id)"
										@copied-project="tabChange({ index: 0 })"
									/>
								</li>
							</ul>
							<tera-project-table
								v-else-if="view === ProjectsView.Table"
								:projects="searchedAndFilterProjects"
								:selected-columns="selectedColumns"
								@open-project="openProject"
								class="project-table"
							/>
						</section>
					</TabPanel>
				</TabView>
			</section>

			<!-- Upload project modal -->
			<tera-upload-project-modal
				:visible="isUploadProjectModalVisible"
				@project-added="useProjects().getAll()"
				@close="isUploadProjectModalVisible = false"
			/>
		</div>
	</main>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import Button from 'primevue/button';
import TabView from 'primevue/tabview';
import TabPanel from 'primevue/tabpanel';
import { useRouter } from 'vue-router';
import { RouteName } from '@/router/routes';
import { debounce, isEmpty } from 'lodash';
import TeraProjectTable from '@/components/home/tera-project-table.vue';
import TeraProjectCard from '@/components/home/tera-project-card.vue';
import { useProjects } from '@/composables/project';
import Dropdown from 'primevue/dropdown';
import MultiSelect from 'primevue/multiselect';
import SelectButton from 'primevue/selectbutton';
import { useProjectMenu } from '@/composables/project-menu';
import { ClientEventType, ProgressState, Project, ProjectSearchResponse } from '@/types/Types';
import { Vue3Lottie } from 'vue3-lottie';
import EmptySeed from '@/assets/images/lottie-empty-seed.json';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import { useNotificationManager } from '@/composables/notificationManager';
import teraUploadProjectModal from '@/components/project/tera-upload-project-modal.vue';
import { findProjects } from '@/services/project';
import { useToastService } from '@/services/toast';

const { isProjectConfigDialogVisible, menuProject } = useProjectMenu();

const { notificationItems } = useNotificationManager();

const cloningProjects = computed(() => {
	const items: any = [];
	notificationItems.value.forEach((item) => {
		if (item.type === ClientEventType.CloneProject && item.status === ProgressState.Running) {
			const project = (useProjects().allProjects.value ?? []).find((p) => p.id === item.assetId);
			items.push(project);
		}
	});
	return items;
});

const activeTabIndex = ref(0);
const showVideo = ref(false);
const isUploadProjectModalVisible = ref(false);
const searchProjectsQuery = ref('');
const searchProjectsResults = ref<ProjectSearchResponse[]>([]);
const isSearchLoading = ref(false);

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
	{ value: ProjectsView.Cards, icon: 'pi pi-credit-card' },
	{ value: ProjectsView.Table, icon: 'pi pi-list' }
]);

function selectChange(event) {
	if (event.value) view.value = event.value;
}

function tabChange(event) {
	activeTabIndex.value = event.index;
}

const searchedAndFilterProjects = computed(() => {
	let tabProjects = useProjects().allProjects.value;
	if (!tabProjects) return [];

	if (activeTabIndex.value === 0) {
		tabProjects = tabProjects.filter(
			({ userPermission, publicProject }) =>
				// I can edit the project, or I can view a non-public project
				['creator', 'writer'].includes(userPermission ?? '') || (userPermission === 'reader' && !publicProject)
		);
	} else if (activeTabIndex.value === 1) {
		tabProjects = tabProjects.filter(({ publicProject }) => publicProject === true);
	} else if (activeTabIndex.value === 2) {
		tabProjects = [] as Project[]; // TODO - Sample projects
	}

	// If they are no search we can return the filtered and sorted projects
	if (isEmpty(searchProjectsResults.value)) {
		return filterAndSortProjects(tabProjects);
	}

	// If there is a search query, we need to filter the projects based on the search results
	// while keeping the order of the search results to the order of the projects
	return searchProjectsResults.value
		.map((result) => {
			const project = tabProjects.find(({ id }) => id === result.projectId);
			if (!project) return null;
			if (!project.metadata) project.metadata = {};

			// Add the scoring to the search
			project.metadata.score = result.score.toString();

			// Add the search scoring to the projectAsset
			result.hits.forEach((hit) => {
				project.metadata![hit.assetId] = hit.score.toString();
			});

			return project as Project;
		})
		.filter((project) => !!project); // Remove null values
});

function openCreateProjectModal() {
	isProjectConfigDialogVisible.value = true;
	menuProject.value = null;
}

function openUploadProjectModal() {
	isUploadProjectModalVisible.value = true;
}

type DateType = 'createdOn' | 'updatedOn' | 'deletedOn';

function sortProjectByDates(projects: Project[], dateType: DateType, sorting: 'ASC' | 'DESC') {
	return projects.sort((a, b) => {
		const dateValueA = a[dateType]?.toString();
		const dateValueB = b[dateType]?.toString();
		const dateA = dateValueA ? new Date(dateValueA) : new Date(0);
		const dateB = dateValueB ? new Date(dateValueB) : new Date(0);
		return sorting === 'ASC' ? dateA.getTime() - dateB.getTime() : dateB.getTime() - dateA.getTime();
	});
}

function filterAndSortProjects(projects: Project[]) {
	if (projects) {
		if (selectedSort.value === 'Alphabetical') {
			return projects.sort((a, b) => (a.name ?? '').toLowerCase().localeCompare((b.name ?? '').toLowerCase()));
		}
		if (selectedSort.value === 'Last updated (descending)') {
			return sortProjectByDates(projects, 'updatedOn', 'DESC');
		}
		if (selectedSort.value === 'Last updated (ascending)') {
			return sortProjectByDates(projects, 'updatedOn', 'ASC');
		}
		if (selectedSort.value === 'Creation date (descending)') {
			return sortProjectByDates(projects, 'createdOn', 'DESC');
		}
		if (selectedSort.value === 'Creation date (ascending)') {
			return sortProjectByDates(projects, 'createdOn', 'ASC');
		}
	}
	return [];
}

// Table view
const columns = ref([
	{ field: 'name', header: 'Project title' },
	{ field: 'description', header: 'Description' },
	{ field: 'userName', header: 'Author' },
	{ field: 'stats', header: 'Stats' },
	{ field: 'createdOn', header: 'Created on' },
	{ field: 'updatedOn', header: 'Last updated' }
]);

const selectedColumns = ref(columns.value);
const onToggle = (val) => {
	selectedColumns.value = columns.value.filter((col) => val.includes(col));
};

const router = useRouter();

const isLoadingProjects = computed(() => !useProjects().allProjects.value);

function openProject(projectId: string) {
	router.push({ name: RouteName.Project, params: { projectId } });
}

watch(cloningProjects, () => {
	if (cloningProjects.value.length === 0) {
		useProjects().getAll();
	}
});

function addRankingToColumns() {
	removeRankingFromColumns();
	columns.value.unshift({ field: 'searchRanking', header: 'Ranking' });
	selectedColumns.value.unshift({ field: 'searchRanking', header: 'Ranking' });
}

function removeRankingFromColumns() {
	columns.value = columns.value.filter((col) => col.field !== 'searchRanking');
	selectedColumns.value = selectedColumns.value.filter((col) => col.field !== 'searchRanking');
}

async function searchedProjects() {
	// If the search query is empty, show all projects
	if (isEmpty(searchProjectsQuery.value)) {
		searchProjectsResults.value = [];
		removeRankingFromColumns();
		return;
	}

	isSearchLoading.value = true;
	searchProjectsResults.value = await findProjects(searchProjectsQuery.value);

	// If no projects found, display a toast message
	if (isEmpty(searchProjectsResults.value)) {
		useToastService().info('No projects found', 'Try searching for something else', 5000);
	} else {
		// Display search results using the table view
		view.value = ProjectsView.Table;
		addRankingToColumns();
	}

	isSearchLoading.value = false;
}

watch(searchProjectsQuery, debounce(searchedProjects, 500));
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
	background-position:
		right 240px top -60px,
		100%;
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
	min-width: 17rem;
	display: flex;
	align-items: center;
	padding-left: 0.5rem;
}

.filter-and-sort {
	position: sticky;
	z-index: 1;
	background-color: #f4f4f4;
	border-top: 1px solid var(--surface-border-light);
	border-bottom: 1px solid var(--surface-border-light);
	padding: var(--gap-2) var(--gap-4);
	display: flex;
	justify-content: space-between;
	align-items: center;
	gap: 16px;
	/* Accommodate for height of projects tabs*/
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

.sort-options-dropdown {
	height: 40px;
	padding-left: 0.5rem;
}
.project-cards-grid {
	display: grid;
	grid-template-columns: repeat(auto-fill, minmax(16rem, 1fr));
	gap: 16px;
	padding: 16px;
	list-style: none;
}
:deep(.project-table) {
	margin: var(--gap-4);
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
.secondary-button {
	background-color: var(--text-color-secondary);
}
.secondary-button:hover {
	background-color: color-mix(in srgb, var(--text-color-secondary) 90%, var(--surface-0) 10%);
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

/* Video & Thumbnail */
.video-container {
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
}

.video-thumbnail {
	background-image: radial-gradient(circle, var(--primary-color), #004f3c), url('@/assets/images/video-thumbnail.png');
	background-blend-mode: multiply;
	background-size: cover;
	background-position: center;
	padding: 2rem;
	display: flex;
	justify-content: center;
	align-items: center;
	flex-direction: column;
	text-align: center;
	position: relative;
	cursor: pointer;
	border-radius: 6px;
	aspect-ratio: 16 / 9;
	height: 200px;
}

.video-thumbnail .header {
	font-size: 1.5rem;
	font-weight: bold;
	color: var(--surface-0);
}
.video-thumbnail .play-symbol {
	display: flex;
	padding-left: 5px;
	padding-top: 3px;
	align-items: center;
	justify-content: center;
	flex-direction: column;
	flex: 1;
	font-size: 2rem;
	border-radius: 50%;
	background-color: var(--surface-0);
	color: var(--primary-color);
	aspect-ratio: 1 / 1;
	transition: all 0.2s;
}

.video-thumbnail .play-symbol:hover {
	background-color: var(--primary-color-lighter);
}

.video-thumbnail .sub-header {
	color: var(--surface-50);
}

.video-player {
	border-radius: 6px;
	padding: 0;
}
</style>
