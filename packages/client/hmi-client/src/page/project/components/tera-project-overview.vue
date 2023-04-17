<template>
	<tera-asset
		:name="project?.name"
		:authors="project?.username"
		:publisher="`Last updated ${DateUtils.formatLong(project?.timestamp)}`"
		is-editable
	>
		<template #name-input>
			<InputText
				v-model="newProjectName"
				ref="inputElement"
				class="project-name-input"
				@keyup.enter="updateProjectName"
				:class="{ isVisible: isEditingProject }"
			/>
		</template>
		<template #edit-buttons>
			<Button
				icon="pi pi-ellipsis-v"
				class="p-button-icon-only p-button-text p-button-rounded"
				@click="showProjectMenu"
			/>
			<Menu ref="projectMenu" :model="projectMenuItems" :popup="true" />
		</template>
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
				</div>
			</section>
			<!-- Project summary KPIs -->
			<section class="summary-KPI-bar">
				<div class="summary-KPI" v-for="(assets, type) of project?.assets" :key="type">
					<span class="summary-KPI-number">{{ assets.length ?? 0 }}</span>
					<span class="summary-KPI-label">{{ capitalize(type) }}</span>
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
					<Button size="large" class="p-button p-button-secondary quick-link-button">
						<vue-feather
							class="p-button-icon-left"
							type="git-merge"
							size="1.25rem"
							stroke="rgb(16, 24, 40)"
						/>
						<span class="p-button-label">New workflow</span>
					</Button>
					<Button size="large" class="p-button p-button-secondary quick-link-button">
						<compare-models-icon class="icon" />
						<span class="p-button-label">Compare Models</span>
					</Button>
					<Button
						label="New simulation"
						size="large"
						icon="pi pi-play"
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
	</tera-asset>
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
import * as DateUtils from '@/utils/date';
import { capitalize } from 'lodash';
import TeraAsset from '@/components/widgets/tera-asset.vue';
import CompareModelsIcon from '@/assets/svg/icons/compare-models.svg?component';

const props = defineProps<{
	project: IProject;
}>();

const resources = useResourcesStore();
const isEditingProject = ref(false);
const inputElement = ref<HTMLInputElement | null>(null);
const newProjectName = ref<string>('');

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
a {
	text-decoration: underline;
}

.content-container {
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

button .icon {
	scale: 1.25;
	color: var(--text-color-primary);
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
</style>
