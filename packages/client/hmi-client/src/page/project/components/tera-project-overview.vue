<template>
	<main>
		<tera-asset
			:name="useProjects().activeProject.value?.name"
			:authors="useProjects().activeProject.value?.username"
			:is-naming-asset="isRenamingProject"
			:publisher="`Last updated ${DateUtils.formatLong(
				useProjects().activeProject.value?.timestamp
			)}`"
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
								{{ useProjects().activeProject.value?.description }}
							</p>
						</section>
					</div>
				</section>
				<!-- Project summary KPIs -->
				<section class="summary-KPI-bar">
					<div
						class="summary-KPI"
						v-for="(assets, type) of useProjects().activeProject.value?.assets"
						:key="type"
					>
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
						@click="isUploadResourcesModalVisible = true"
					/>
					<Button
						label="New model"
						size="large"
						icon="pi pi-share-alt"
						class="p-button p-button-secondary quick-link-button"
						@click="emit('open-new-asset', AssetType.Models)"
					/>
					<Button
						size="large"
						class="p-button p-button-secondary quick-link-button"
						@click="emit('open-new-asset', AssetType.Workflows)"
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
						<InputText v-model="searchTable" placeholder="Keyword search" class="keyword-search" />
					</span>
				</div>
				<!-- resource list data table -->
				<DataTable
					v-model:selection="selectedResources"
					dataKey="assetId"
					tableStyle="min-width: 50rem"
					:value="assets"
					row-hover
					:row-class="() => 'p-selectable-row'"
					@update:selection="onRowSelect"
				>
					<Column selection-mode="multiple" headerStyle="width: 3rem" />
					<Column field="assetName" header="Name" sortable style="width: 75%">
						<template #body="slotProps">
							<div class="asset-button" @click="openResource(slotProps.data)">
								<vue-feather
									v-if="typeof getAssetIcon(slotProps.data.pageType ?? null) === 'string'"
									:type="getAssetIcon(slotProps.data.pageType ?? null)"
									size="1rem"
									stroke="rgb(16, 24, 40)"
									class="p-button-icon-left icon"
								/>
								<component
									v-else
									:is="getAssetIcon(slotProps.data.pageType ?? null)"
									class="p-button-icon-left icon"
								/>
								<span class="p-button-label">{{ slotProps.data.assetName }}</span>
							</div>
						</template>
					</Column>
					<Column field="" header="Modified" sortable style="width: 25%"></Column>
					<!-- <Column field="tags" header="Tags" style="width: 25%"></Column> -->
					<Column header="Type" style="width: 25%" sortable field="pageType">
						<template #body="slotProps">
							{{ slotProps.data.pageType }}
						</template>
					</Column>
					<Column
						headerStyle="width: 3rem; text-align: center"
						bodyStyle="text-align: center; overflow: visible"
					>
						<template #body="slotProps">
							<Button
								class="row-action-button"
								icon="pi pi-ellipsis-v"
								plain
								text
								rounded
								@click.stop="(e) => showRowActions(e, slotProps.data)"
							/>
							<Menu ref="rowActionMenu" :model="rowActionMenuItems" :popup="true" />
						</template>
					</Column>
					<template #empty>
						<div class="explorer-status">
							<img src="@assets/svg/seed.svg" alt="Seed" />
							<h4 class="no-results-found">This is an empty project</h4>
							<span class="no-results-found-message"
								>Add resources to your project with the quick link buttons, or use the explorer to
								find documents, models and datasets of interest.</span
							>
						</div>
					</template>
				</DataTable>
			</section>
			<section class="drag-n-drop">
				<tera-upload-resources-modal
					:visible="isUploadResourcesModalVisible"
					@close="isUploadResourcesModalVisible = false"
				/>
			</section>
		</section>
		<tera-multi-select-modal
			:is-visible="showMultiSelect"
			:selected-resources="selectedResources"
			:buttons="multiSelectButtons"
		></tera-multi-select-modal>
	</main>
</template>

<script setup lang="ts">
import { isProjectAssetTypes } from '@/types/Project';
import { computed, nextTick, onMounted, ref, toRaw } from 'vue';
import InputText from 'primevue/inputtext';
import Button from 'primevue/button';
import Menu from 'primevue/menu';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import * as DateUtils from '@/utils/date';
import TeraAsset from '@/components/asset/tera-asset.vue';
import CompareModelsIcon from '@/assets/svg/icons/compare-models.svg?component';
import { Tab } from '@/types/common';
import { capitalize, isEmpty } from 'lodash';
import { AssetType } from '@/types/Types';
import { useRouter } from 'vue-router';
import { RouteName } from '@/router/routes';
import TeraMultiSelectModal from '@/components/widgets/tera-multi-select-modal.vue';
import { useTabStore } from '@/stores/tabs';
import { useProjects } from '@/composables/project';
import { getAssetIcon } from '@/services/project';
import TeraUploadResourcesModal from './tera-upload-resources-modal.vue';

const emit = defineEmits(['open-asset', 'open-new-asset']);
const router = useRouter();
const isRenamingProject = ref(false);
const inputElement = ref<HTMLInputElement | null>(null);
const newProjectName = ref<string>('');
const selectedResources = ref();

const openedRow = ref(null);

const tabStore = useTabStore();

const multiSelectButtons = [
	{
		label: 'Open',
		callback: () => {
			selectedResources.value.forEach((resource) => {
				const { activeProject } = useProjects();
				if (activeProject.value?.id) {
					tabStore.addTab(activeProject.value.id, toRaw(resource), false);
				}
			});
		}
	}
];

const searchTable = ref('');
const showMultiSelect = ref<boolean>(false);

const assets = computed(() => {
	const tabs = new Map<AssetType, Set<Tab>>();

	const projectAssets = useProjects().activeProject.value?.assets;
	if (!projectAssets) return tabs;

	const result = <any>[];
	// Run through all the assets type within the project
	Object.keys(projectAssets).forEach((type) => {
		if (isProjectAssetTypes(type) && !isEmpty(projectAssets[type])) {
			const projectAssetType: AssetType = type as AssetType;
			const typeAssets = projectAssets[projectAssetType]
				.map((asset) => {
					let assetName = (asset?.name || asset?.title || asset?.id)?.toString();

					// FIXME should unify upstream via a summary endpoint
					if (asset.header && asset.header.name) {
						assetName = asset.header.name;
					}

					const pageType = asset?.type ?? projectAssetType;
					const assetId = asset?.id ?? '';
					return { assetName, pageType, assetId };
				})
				.filter((asset) => {
					if (!searchTable.value?.trim()) {
						return true;
					}
					const searchTermLower = searchTable.value?.trim().toLowerCase();
					return asset.assetName.toLowerCase().includes(searchTermLower);
				});
			result.push(...typeAssets);
		}
	});
	return result;
});

const onRowSelect = (selectedRows) => {
	// show multi select modal when there are selectedRows otherwise hide
	showMultiSelect.value = selectedRows.length !== 0;
};

async function editProject() {
	newProjectName.value = useProjects().activeProject.value?.name ?? '';
	isRenamingProject.value = true;
	await nextTick();
	// @ts-ignore
	inputElement.value?.$el.focus();
}

async function openResource(data) {
	router.push({ name: RouteName.Project, params: data });
}

async function updateProjectName() {
	const { activeProject } = useProjects();
	if (activeProject.value) {
		isRenamingProject.value = false;
		const updatedProject = activeProject.value;
		updatedProject.name = newProjectName.value;
		await useProjects().update(updatedProject);
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

/* hacky way of listening to row hover events to display/hide the action button, prime vue unfortunately doesn't have the capability to listen to row hover */
function setRowHover() {
	const tableRows = document.querySelectorAll('.p-selectable-row');
	tableRows.forEach((tableRow) => {
		const buttonBar = tableRow.querySelector('.row-action-button .pi-ellipsis-v') as HTMLElement;
		buttonBar.style.display = 'none';
		tableRow.addEventListener('mouseover', () => {
			buttonBar.style.removeProperty('display');
		});
		tableRow.addEventListener('mouseleave', () => {
			buttonBar.style.display = 'none';
		});
	});
}

/* Row Action Menu */
const rowActionMenu = ref();
const rowActionMenuItems = ref([
	{ label: 'Open', command: () => openResource(openedRow.value) }

	// TODO add the follow commands
	// { label: 'Rename' },
	// { label: 'Make a copy' },
	// { label: 'Delete' },
	// { label: 'Download' }
]);
const showRowActions = (event, data) => {
	openedRow.value = data;
	rowActionMenu.value.toggle(event);
};

const isUploadResourcesModalVisible = ref(false);

onMounted(() => {
	setRowHover();
});
</script>

<style scoped>
main {
	overflow-y: auto;
	-ms-overflow-style: none;
	/* IE and Edge */
	scrollbar-width: none;
	/* Firefox */
}

main::-webkit-scrollbar {
	display: none;
}

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
	display: flex;
	flex-direction: column;
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

/* TODO: Create a proper secondary outline button in PrimeVue theme */
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

.keyword-search:enabled:hover {
	border-color: var(--surface-border);
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

.asset-button {
	display: flex;
	flex-direction: row;
	align-items: center;
	gap: 0.75rem;
}

:deep(.asset-button.p-button) {
	display: inline-flex;
	overflow: hidden;
	padding: 0;
	padding: 0.375rem 1rem;
}

:deep(.p-datatable .p-datatable-thead > tr > th) {
	background: var(--gray-100);
	padding: 1rem;
}

:deep(.p-datatable .p-datatable-tbody > tr > td) {
	padding: 0.5rem 1rem;
}

:deep(.p-datatable .p-datatable-thead > tr > th:first-child .p-column-header-content) {
	justify-content: center;
}

:deep(.asset-button.p-button[active='true']) {
	background-color: var(--surface-highlight);
}

:deep(.asset-button.p-button .p-button-label) {
	overflow: hidden;
	text-align: left;
}

:deep(.asset-button .p-button-icon-left) {
	overflow: visible;
}

.resource-list-button {
	margin-left: auto;
}

:deep(.resource-list-button .p-button.p-button-icon-only) {
	color: #ffffff;
}

.explorer-status {
	display: flex;
	flex-direction: column;
	justify-content: center;
	gap: 1rem;
	align-items: center;
	margin-top: 8rem;
	margin-bottom: 12rem;
	flex-grow: 1;
	font-size: var(--font-body-small);
	color: var(--text-color-subdued);
}

.no-results-found-message {
	text-align: center;
	width: 40%;
}
</style>
