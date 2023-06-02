<template>
	<tera-asset
		:name="project?.name"
		:authors="project?.username"
		:publisher="`Last updated ${DateUtils.formatLong(project?.timestamp)}`"
		is-editable
		class="overview-banner"
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
		<section>
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
		</section>
	</tera-asset>
	<section class="content-container">
		<h5>Quick links</h5>
		<!-- Quick link buttons go here -->
		<section>
			<div class="quick-links">
				<Button
					label="Upload resources"
					size="large"
					icon="pi pi-cloud-upload"
					class="p-button p-button-secondary quick-link-button"
					@click="openImportModal"
				/>
				<Button
					label="New model"
					size="large"
					icon="pi pi-share-alt"
					class="p-button p-button-secondary quick-link-button"
				/>
				<Button
					size="large"
					class="p-button p-button-secondary quick-link-button"
					@click="emit('open-workflow')"
				>
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
					<span class="p-button-label">Compare models</span>
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
		<section class="drag-n-drop">
			<tera-modal
				v-if="isUploadResourcesModalVisible"
				class="modal"
				@modal-mask-clicked="isUploadResourcesModalVisible = false"
			>
				<template #header>
					<h4>Upload resources</h4>
				</template>
				<template #default>
					<p class="subheader">Add resources to your project here</p>
					<tera-drag-and-drop-importer
						:show-preview="true"
						:accept-types="[
							AcceptedTypes.PDF,
							AcceptedTypes.JPG,
							AcceptedTypes.JPEG,
							AcceptedTypes.PNG,
							AcceptedTypes.CSV
						]"
						:import-action="processFiles"
						@import-completed="importCompleted"
					></tera-drag-and-drop-importer>

					<section v-if="isUploadResourcesModalVisible">
						<Card v-for="(item, i) in results" :key="i" class="card">
							<template #title>
								<div class="card-img"></div>
							</template>
							<template #content>
								<div class="card-content">
									<div v-if="item.file" class="file-title">{{ item.file.name }}</div>
									<div v-if="item.response" class="file-content">
										<br />
										<div>Extracted Text</div>
										<div>{{ item.response.text }}</div>
										<br />
										<div v-if="item.response.images">Images Found</div>
										<div v-for="image in item.response.images" :key="image">
											<img :src="`data:image/jpeg;base64,${image}`" alt="" />
										</div>
										<br />
										<i class="pi pi-plus"></i>
									</div>
								</div>
							</template>
						</Card>
					</section>
				</template>
				<template #footer>
					<Button
						label="Upload"
						class="p-button-primary"
						@click="isUploadResourcesModalVisible = false"
						:disabled="!results"
					/>
					<Button
						label="Cancel"
						class="p-button-secondary"
						@click="isUploadResourcesModalVisible = false"
					/>
				</template>
			</tera-modal>
		</section>
	</section>
</template>

<script setup lang="ts">
import { IProject } from '@/types/Project';
import { nextTick, ref } from 'vue';
import InputText from 'primevue/inputtext';
import { update as updateProject } from '@/services/project';
import useResourcesStore from '@/stores/resources';
import Button from 'primevue/button';
import Menu from 'primevue/menu';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import * as DateUtils from '@/utils/date';
import { capitalize } from 'lodash';
import TeraAsset from '@/components/asset/tera-asset.vue';
import CompareModelsIcon from '@/assets/svg/icons/compare-models.svg?component';
import { AcceptedTypes, PDFExtractionResponseType } from '@/types/common';
import TeraModal from '@/components/widgets/tera-modal.vue';
import Card from 'primevue/card';
import TeraDragAndDropImporter from '@/components/extracting/tera-drag-n-drop-importer.vue';
import API, { Poller } from '@/api/api';
import { createNewDatasetFromCSV } from '@/services/dataset';

const props = defineProps<{
	project: IProject;
}>();
const emit = defineEmits(['open-workflow', 'update-project']);
const resources = useResourcesStore();
const isEditingProject = ref(false);
const inputElement = ref<HTMLInputElement | null>(null);
const newProjectName = ref<string>('');
const visible = ref(false);
const results = ref<
	{ file: File; error: boolean; response: { text: string; images: string[] } }[] | null
>(null);

async function getPDFContents(
	file: string | Blob,
	extractionMode: string,
	extractImages: string
): Promise<PDFExtractionResponseType> {
	const formData = new FormData();
	formData.append('file', file);

	const result = await API.post(`/extract/convertpdftask/`, formData, {
		params: {
			extraction_method: extractionMode,
			extract_images: extractImages
		},
		headers: {
			'Content-Type': 'multipart/form-data'
		}
	});

	if (result) {
		const taskID = result.data.task_id;

		const poller = new Poller<object>()
			.setInterval(2000)
			.setThreshold(90)
			.setPollAction(async () => {
				const response = await API.get(`/extract/task-result/${taskID}`);

				if (response.data.status === 'SUCCESS' && response.data.result) {
					return {
						data: response.data.result,
						progress: null,
						error: null
					};
				}
				return {
					data: null,
					progress: null,
					error: null
				};
			});
		const pollerResults = await poller.start();

		if (pollerResults.data) {
			return pollerResults.data as PDFExtractionResponseType;
		}
	}
	return { text: '', images: [] } as PDFExtractionResponseType;
}

async function processFiles(
	files: File[],
	extractionMode: string,
	extractImages: string,
	csvDescription: string
) {
	return files.map(async (file) => {
		if (file.type === AcceptedTypes.CSV) {
			const addedCSV = await createNewDatasetFromCSV(file, props.project.id, csvDescription);
			const text = addedCSV?.csv.join('\r\n');
			const images = [];

			return { file, error: false, response: { text, images } };
		}
		// PDF
		const resp = await getPDFContents(file, extractionMode, extractImages);
		const text = resp.text ? resp.text : '';
		const images = resp.images ? resp.images : [];
		return { file, error: false, response: { text, images } };
	});
}

async function openImportModal() {
	isUploadResourcesModalVisible.value = true;
	results.value = null;
}

function importCompleted(
	newResults: { file: File; error: boolean; response: { text: string; images: string[] } }[] | null
) {
	// This is a hacky override for dealing with CSVs
	if (newResults && newResults.length === 1 && newResults[0].file.type === AcceptedTypes.CSV) {
		results.value = null;
		emit('update-project', props.project.id);
		visible.value = false;
	} else {
		results.value = newResults;
	}
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

function showProjectMenu(event: any) {
	projectMenu.value.toggle(event);
}

const isUploadResourcesModalVisible = ref(false);
</script>

<style scoped>
a {
	text-decoration: underline;
}

.overview-banner {
	background: url('@/assets/svg/terarium-icon-transparent.svg') no-repeat right 10% center,
		linear-gradient(45deg, #d5e8e5 0%, #f0f4f0 100%) no-repeat;
	background-size: 25%, 100%;
	height: auto;
}
.content-container {
	padding: 1rem;
	background: var(--surface-0);
	flex: 1;
}

.description {
	margin: 1rem;
}

.contributors {
	flex-direction: row;
	align-items: center;
	gap: 0.75rem;
}

.summary-KPI-bar {
	display: flex;
	flex-direction: row;
	justify-content: space-evenly;
	margin: 1rem;
	padding: 1rem;
	background: rgba(255, 255, 255, 0.5);
	border-radius: var(--border-radius);
	backdrop-filter: blur(5px);
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
	margin-top: 1rem;
	margin-bottom: 1rem;
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
	padding-top: 0.5rem;
	padding-bottom: 0.5rem;
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

.file-title {
	font-size: 2em;
}

.subheader {
	margin-bottom: 1rem;
}
</style>
