<template>
	<tera-asset
		:name="project?.name"
		:authors="project?.username"
		:is-naming-asset="isRenamingProject"
		:publisher="`Last updated ${DateUtils.formatLong(project?.timestamp)}`"
		is-editable
		class="overview-banner"
	>
		<template #name-input>
			<InputText
				v-if="isRenamingProject"
				v-model="newProjectName"
				ref="inputElement"
				@keyup.enter="updateProjectName"
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
					@click="emit('new-model')"
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
				<h4>Resource Manager</h4>
				<span class="p-input-icon-left">
					<i class="pi pi-search" />
					<InputText placeholder="Keyword search" class="keyword-search" />
				</span>
			</div>
			<!-- resource list data table -->
			<DataTable
				v-model:selection="selectedResources"
				dataKey="id"
				tableStyle="min-width: 50rem"
				:value="assets"
			>
				<Column selection-mode="multiple" headerStyle="width: 3rem" />
				<Column field="assetName" header="Name" sortable style="width: 45%">
					<template #body="slotProps">
						<Button
							:title="slotProps.data.assetName"
							class="asset-button"
							plain
							text
							size="small"
							@click="router.push({ name: RouteName.ProjectRoute, params: slotProps.data })"
						>
							<span class="p-button-label">{{ slotProps.data.assetName }}</span>
						</Button>
					</template>
				</Column>
				<Column field="" header="Modified" sortable style="width: 15%"></Column>
				<Column field="tags" header="Tags"></Column>
				<Column header="Resource Type" sortable>
					<template #body="slotProps">
						<Tag :value="slotProps.data.pageType" />
					</template>
				</Column>
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
							AcceptedTypes.CSV,
							AcceptedTypes.TXT,
							AcceptedTypes.MD
						]"
						:import-action="processFiles"
						:progress="progress"
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
import { IProject, ProjectAssetTypes, isProjectAssetTypes } from '@/types/Project';
import { nextTick, Ref, ref, computed } from 'vue';
import InputText from 'primevue/inputtext';
import Tag from 'primevue/tag';
import * as ProjectService from '@/services/project';
import useResourcesStore from '@/stores/resources';
import Button from 'primevue/button';
import Menu from 'primevue/menu';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import * as DateUtils from '@/utils/date';
import TeraAsset from '@/components/asset/tera-asset.vue';
import CompareModelsIcon from '@/assets/svg/icons/compare-models.svg?component';
import { Tab, AcceptedTypes } from '@/types/common';
import TeraModal from '@/components/widgets/tera-modal.vue';
import Card from 'primevue/card';
import TeraDragAndDropImporter from '@/components/extracting/tera-drag-n-drop-importer.vue';
import { createNewDatasetFromCSV } from '@/services/dataset';
import { capitalize, isEmpty } from 'lodash';
import { CsvAsset } from '@/types/Types';
import { useRouter } from 'vue-router';
import { RouteName } from '@/router/routes';
import { logger } from '@/utils/logger';
import { uploadArtifactToProject } from '@/services/artifact';

const props = defineProps<{
	project: IProject;
}>();
const emit = defineEmits(['open-workflow', 'open-asset', 'new-model']);
const router = useRouter();
const isRenamingProject = ref(false);
const inputElement = ref<HTMLInputElement | null>(null);
const newProjectName = ref<string>('');
const progress: Ref<number> = ref(0);
const results = ref<
	{ file: File; error: boolean; response: { text: string; images: string[] } }[] | null
>(null);
const selectedResources = ref();

const assets = computed(() => {
	const tabs = new Map<ProjectAssetTypes, Set<Tab>>();

	const projectAssets = props.project?.assets;
	if (!projectAssets) return tabs;

	const result = <any>[];
	// Run through all the assets type within the project
	Object.keys(projectAssets).forEach((type) => {
		if (isProjectAssetTypes(type) && !isEmpty(projectAssets[type])) {
			const projectAssetType: ProjectAssetTypes = type as ProjectAssetTypes;
			const typeAssets = projectAssets[projectAssetType].map((asset) => {
				const assetName = (asset?.name || asset?.title || asset?.id)?.toString();
				const pageType = asset?.type ?? projectAssetType;
				const assetId = asset?.id ?? '';
				return { assetName, pageType, assetId };
			});
			result.push(...typeAssets);
		}
	});
	return result;
});

async function processFiles(files: File[], csvDescription: string) {
	return files.map(async (file) => {
		if (file.type === AcceptedTypes.CSV) {
			const addedCSV: CsvAsset | null = await createNewDatasetFromCSV(
				progress,
				file,
				props.project.username ?? '',
				props.project.id,
				csvDescription
			);

			if (addedCSV !== null) {
				const text: string = addedCSV?.csv?.join('\r\n') ?? '';
				const images = [];

				return { file, error: false, response: { text, images } };
			}
			return { file, error: true, response: { text: '', images: [] } };
		}

		// This is pdf, txt, md files
		const response = await uploadArtifactToProject(
			progress,
			file,
			props.project.username ?? '',
			props.project.id,
			''
		);
		if (response?.data) return { file, error: false, response: { text: '', images: [] } };
		return { file, error: true, response: { text: '', images: [] } };
	});
}

async function openImportModal() {
	isUploadResourcesModalVisible.value = true;
	results.value = null;
}

async function importCompleted(
	newResults: { file: File; error: boolean; response: { text: string; images: string[] } }[] | null
) {
	// This is a hacky override for dealing with CSVs
	if (
		newResults &&
		newResults.length === 1 &&
		(newResults[0].file.type === AcceptedTypes.CSV ||
			newResults[0].file.type === AcceptedTypes.TXT ||
			newResults[0].file.type === AcceptedTypes.MD)
	) {
		if (newResults[0].error) {
			logger.error('Failed to upload file. Is it too large?', { showToast: true });
		}
		results.value = null;
		isUploadResourcesModalVisible.value = false;

		// TODO: See about getting rid of this - this refresh should preferably be within a service
		useResourcesStore().setActiveProject(await ProjectService.get(props.project.id, true));
	} else {
		results.value = newResults;
	}
}

async function editProject() {
	newProjectName.value = props.project.name;
	isRenamingProject.value = true;
	await nextTick();
	// @ts-ignore
	inputElement.value?.$el.focus();
}

async function updateProjectName() {
	isRenamingProject.value = false;
	const updatedProject = props.project;
	updatedProject.name = newProjectName.value;
	await ProjectService.update(updatedProject);
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
	background: url('@/assets/svg/terarium-icon-transparent.svg') no-repeat right 20% center,
		linear-gradient(45deg, #8bd4af1a, #d5e8e5 100%) no-repeat;
	background-size: 25%, 100%;
	height: auto;
}

.content-container {
	overflow-y: auto;
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

.modal:deep(main) {
	width: 50rem;
}

:deep(.asset-button.p-button) {
	display: inline-flex;
	overflow: hidden;
	padding: 0;
}

:deep(.asset-button.p-button > span) {
	display: inline-flex;
	width: 100%;
	padding: 0.375rem 1rem;
	overflow: hidden;
}

:deep(.asset-button.p-button[active='true']) {
	background-color: var(--surface-highlight);
}

:deep(.asset-button.p-button .p-button-label) {
	overflow: hidden;
	text-align: left;
	text-overflow: ellipsis;
	white-space: nowrap;
}
</style>
