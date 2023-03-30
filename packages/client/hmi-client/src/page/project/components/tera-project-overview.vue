<template>
	<div class="flex-container">
		<header class="overview-header">
			<Button
				icon="pi pi-ellipsis-v"
				class="p-button-rounded menu-button"
				@click="showProjectMenu"
			/>
			<Menu ref="projectMenu" :model="projectMenuItems" :popup="true" />
			<InputText
				v-model="newProjectName"
				ref="inputElement"
				class="project-name-input"
				@keyup.enter="updateProjectName"
				:class="{ isVisible: isEditingProject }"
			>
			</InputText>
			<h3 :class="{ isVisible: !isEditingProject }">
				{{ project?.name }}
			</h3>
			<!-- Last updated -->
			<p class="secondary-text">{{ formatTimeStamp(project?.timestamp) }}</p>
		</header>
		<section class="content-container">
			<section class="summary">
				<!-- This div is so that child elements will automatically collapse margins -->
				<div>
					<!-- Description & Contributors -->
					<section class="description">
						<p>
							{{ project?.description }}
						</p>
					</section>
					<section class="contributors">
						<span class="pi pi-user"></span>
						{{ project?.username }}
						<Button icon="pi pi-plus" label="Add contributor" text />
					</section>
				</div>
			</section>

			<!-- Project summary KPIs go here -->
			<section class="summary-KPI-bar">
				<!-- This is placeholder code for styling purposes. This section should be built by a real programmer -->
				<div class="summary-KPI">
					<span class="summary-KPI-number">#</span>
					<span class="summary-KPI-label">Papers</span>
				</div>
				<div class="summary-KPI">
					<span class="summary-KPI-number">#</span>
					<span class="summary-KPI-label">Models</span>
				</div>
				<div class="summary-KPI">
					<span class="summary-KPI-number">#</span>
					<span class="summary-KPI-label">Datasets</span>
				</div>
				<div class="summary-KPI">
					<span class="summary-KPI-number">#</span>
					<span class="summary-KPI-label">Workflows</span>
				</div>
				<div class="summary-KPI">
					<span class="summary-KPI-number">#</span>
					<span class="summary-KPI-label">Simulations</span>
				</div>
			</section>

			<!-- Quick link buttons go here -->
			<section>
				<div class="quick-links">
					<Button
						label="Upload resources"
						size="large"
						icon="pi pi-cloud-upload"
						class="p-button p-button-secondary quick-link-button"
					/>
					<Button
						label="New model"
						size="large"
						icon="pi pi-share-alt"
						class="p-button p-button-secondary quick-link-button"
					/>
					<Button
						label="New workflow"
						size="large"
						icon="pi pi-sitemap"
						class="p-button p-button-secondary quick-link-button"
					/>
					<Button
						label="Compare models"
						size="large"
						icon="pi pi-share-alt"
						class="p-button p-button-secondary quick-link-button"
					/>
					<Button
						label="New simulation"
						size="large"
						icon="pi pi-chart-line"
						class="p-button p-button-secondary quick-link-button"
					/>
				</div>
			</section>
			<!-- Resources list table goes here -->
			<section class="resource-list">
				<div class="resource-list-section-header">
					<h4>File manager</h4>
					<span class="p-input-icon-left">
						<i class="pi pi-search" />
						<InputText placeholder="Keyword search" class="keyword-search" />
					</span>
				</div>
				<!-- resource list data table -->
				<DataTable dataKey="id" tableStyle="min-width: 50rem">
					<Column selectionMode="multiple" headerStyle="width: 3rem"></Column>
					<Column field="name" header="Name" sortable style="width: 45%"></Column>
					<Column field="modified" header="Modified" sortable style="width: 15%"></Column>
					<Column field="tags" header="Tags"></Column>
				</DataTable>
			</section>
		</section>
	</div>
</template>

<script setup lang="ts">
import { IProject } from '@/types/Project';
import { ref, nextTick } from 'vue';
import InputText from 'primevue/inputtext';
import { update as updateProject } from '@/services/project';
import useResourcesStore from '@/stores/resources';
import Button from 'primevue/button';
import Menu from 'primevue/menu';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';

const props = defineProps<{
	project: IProject;
}>();

const resources = useResourcesStore();
const isEditingProject = ref(false);
const inputElement = ref<HTMLInputElement | null>(null);
const newProjectName = ref<string>('');

function formatTimeStamp(timestamp) {
	const formattedDate = new Date(timestamp).toLocaleDateString(undefined, {
		weekday: 'long',
		year: 'numeric',
		month: 'long',
		day: 'numeric'
	});
	return `Last updated ${formattedDate}`;
}

async function editProject() {
	newProjectName.value = props.project.name;
	isEditingProject.value = true;
	await nextTick();
	// @ts-ignore
	inputElement.value?.$el.focus();
}

async function updateProjectName() {
	isEditingProject.value = false;
	const updatedProject = props.project;
	updatedProject.name = newProjectName.value;
	const id = await updateProject(updatedProject);
	if (id) {
		resources.setActiveProject(updatedProject);
	}
}

const projectMenu = ref();
const projectMenuItems = ref([
	{
		label: 'Edit',
		command: editProject
	}
]);

function showProjectMenu(event) {
	projectMenu.value.toggle(event);
}
</script>

<style scoped>
.flex-container {
	display: flex;
	flex-direction: column;
	flex: 1;
	background: var(--surface-section);
}

a {
	text-decoration: underline;
}

.content-container {
	flex-direction: column;
	margin-left: 1rem;
	margin-right: 1rem;
}

.overview-header {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
	margin: 1rem;
}

header {
	margin: 1rem;
}

section {
	display: flex;
	flex-direction: column;
}

.description {
	margin-top: 0.5rem;
	margin-bottom: 0.5rem;
}
.contributors {
	flex-direction: row;
	align-items: center;
	gap: 0.75rem;
}

.summary-KPI-bar {
	display: flex;
	flex-direction: row;
	justify-content: space-between;
	border: 1px solid var(--surface-border);
	border-radius: var(--border-radius);
	padding: 1.5rem 5rem 1.5rem 3rem;
	background-color: var(--gray-50);
	margin-top: 1rem;
	margin-bottom: 1rem;
}

.summary-KPI {
	display: flex;
	flex-direction: row;
	gap: 0.75rem;
	align-items: center;
}

.summary-KPI-number {
	font-size: 2.5rem;
}
.summary-KPI-label {
	font-size: 1.15rem;
}

.quick-links {
	display: flex;
	flex-direction: row;
	justify-content: space-between;
	margin-top: 0.5rem;
	margin-bottom: 0.5rem;
	gap: 1rem;
}

.quick-links .p-button.p-button-secondary {
	background-color: var(--surface);
	color: var(--text-color-primary);
	border: 1px solid var(--surface-border);
	width: 100%;
	font-size: var(--font-body-small);
}
.resource-list {
	margin-top: 1rem;
}

.resource-list-section-header {
	display: flex;
	flex-direction: row;
	align-items: baseline;
	gap: 1rem;
}

.resource-list-section-header h4 {
	font-size: var(--font-body-medium);
}
.keyword-search {
	border-color: transparent;
	padding-top: 0.75rem;
	padding-bottom: 0.75rem;
}
.keyword-search:hover {
	border-color: var(--surface-border) !important;
}

.related {
	font-weight: var(--font-weight-semibold);
}

h3 {
	font-size: 24px;
	display: inline;
	visibility: hidden;
}

.project-name-input {
	font-size: 24px;
	position: absolute;
	font-weight: 600;
	padding: 0 0 0 1rem;
	margin-left: -1rem;
	border: 0;
	visibility: hidden;
	width: 33%;
}

.secondary-text {
	color: var(--text-color-secondary);
}

.isVisible {
	visibility: visible;
}

ul {
	list-style: none;
	display: inline-flex;
}

.item,
.item:enabled:hover,
.item:enabled:focus {
	background-color: var(--surface-secondary);
	color: var(--text-color-secondary);
	padding: 0 0.5rem 0 0.5rem;
	margin: 0.5rem;
}

.menu-button {
	position: absolute;
	right: 0;
}
</style>
