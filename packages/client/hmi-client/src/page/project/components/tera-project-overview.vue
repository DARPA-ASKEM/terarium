<template>
	<tera-asset
		:name="useProjects().activeProject.value?.name"
		:authors="useProjects().activeProject.value?.username"
		:is-naming-asset="isRenamingProject"
		:publisher="`Last updated ${DateUtils.formatLong(
			useProjects().activeProject.value?.timestamp
		)}`"
		:is-loading="useProjects().projectLoading.value"
	>
		<template #edit-buttons>
			<tera-project-menu :project="useProjects().activeProject.value" />
		</template>
		<template #overview-summary>
			<!-- Description & Contributors -->
			<p>
				{{ useProjects().activeProject.value?.description }}
			</p>
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
		</template>
		<section class="content-container">
			<h5>Quick links</h5>
			<!-- Quick link buttons go here -->
			<section class="quick-links">
				<Button
					label="Upload resources"
					size="large"
					icon="pi pi-cloud-upload"
					severity="secondary"
					outlined
					@click="isUploadResourcesModalVisible = true"
				/>
				<Button
					label="New model"
					size="large"
					icon="pi pi-share-alt"
					severity="secondary"
					outlined
					@click="emit('open-new-asset', AssetType.Model)"
				/>
				<Button
					size="large"
					severity="secondary"
					outlined
					@click="emit('open-new-asset', AssetType.Workflow)"
				>
					<vue-feather
						class="p-button-icon-left"
						type="git-merge"
						size="1.25rem"
						stroke="rgb(16, 24, 40)"
					/>
					<span class="p-button-label">New workflow</span>
				</Button>
				<Button size="large" severity="secondary" outlined>
					<compare-models-icon class="icon" />
					<span class="p-button-label">Compare models</span>
				</Button>
				<Button
					label="New simulation"
					size="large"
					icon="pi pi-play"
					severity="secondary"
					outlined
				/>
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
					:value="assetItems"
					row-hover
					:row-class="() => 'p-selectable-row'"
					@update:selection="onRowSelect"
				>
					<Column selection-mode="multiple" headerStyle="width: 3rem" />
					<Column field="assetName" header="Name" sortable style="width: 75%">
						<template #body="slotProps">
							<div
								class="asset-button"
								@click="
									openAsset({ pageType: slotProps.data.pageType, assetId: slotProps.data.assetId })
								"
							>
								<tera-asset-icon :assetType="slotProps.data.pageType" />
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
								@click.stop="
									(e) =>
										showAssetActions(e, {
											pageType: slotProps.data.pageType,
											assetId: slotProps.data.assetId
										})
								"
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
			<tera-upload-resources-modal
				:visible="isUploadResourcesModalVisible"
				@close="isUploadResourcesModalVisible = false"
			/>
		</section>
	</tera-asset>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { generateProjectAssetsMap } from '@/utils/map-project-assets';
import InputText from 'primevue/inputtext';
import Button from 'primevue/button';
import Menu from 'primevue/menu';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import * as DateUtils from '@/utils/date';
import TeraAsset from '@/components/asset/tera-asset.vue';
import CompareModelsIcon from '@/assets/svg/icons/compare-models.svg?component';
import { capitalize } from 'lodash';
import { AssetType } from '@/types/Types';
import { useRouter } from 'vue-router';
import { RouteName } from '@/router/routes';
import { useProjects } from '@/composables/project';
import { AssetRoute } from '@/types/common';
import TeraAssetIcon from '@/components/widgets/tera-asset-icon.vue';
import TeraProjectMenu from '@/components/home/tera-project-menu.vue';
import TeraUploadResourcesModal from './tera-upload-resources-modal.vue';

const emit = defineEmits(['open-asset', 'open-new-asset']);
const router = useRouter();
const isRenamingProject = ref(false);
const selectedResources = ref();

const assetRouteToOpen = ref<AssetRoute | null>(null);

const searchTable = ref('');
const showMultiSelect = ref<boolean>(false);

const assetItems = computed(() =>
	Array.from(generateProjectAssetsMap(searchTable.value).values())
		.map((set) => Array.from(set))
		.flat()
);

const onRowSelect = (selectedRows) => {
	// show multi select modal when there are selectedRows otherwise hide
	showMultiSelect.value = selectedRows.length !== 0;
};

function openAsset(assetRoute: AssetRoute) {
	router.push({ name: RouteName.Project, params: assetRoute });
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
	{
		label: 'Open',
		command: () => assetRouteToOpen.value && openAsset(assetRouteToOpen.value)
	}
	// TODO add the follow commands
	// { label: 'Rename' },
	// { label: 'Make a copy' },
	// { label: 'Delete' },
	// { label: 'Download' }
]);
const showAssetActions = (event, assetRoute: AssetRoute) => {
	assetRouteToOpen.value = assetRoute;
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

.content-container {
	overflow-y: auto;
	padding: 1rem;
	background: var(--surface-0);
	display: flex;
	flex-direction: column;
}

.contributors {
	flex-direction: row;
	align-items: center;
	gap: 0.75rem;
}

p,
.summary-KPI-bar {
	color: var(--text-color-primary);
}

.summary-KPI-bar {
	display: flex;
	justify-content: space-evenly;
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

.p-button.p-button-secondary {
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
