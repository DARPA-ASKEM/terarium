<template>
	<tera-asset
		:id="assetId"
		:name="dataset?.name"
		:feature-config="featureConfig"
		:is-naming-asset="isRenamingDataset"
		:is-loading="isDatasetLoading"
		:overflow-hidden="selectedTabIndex === 1"
		:selected-tab-index="selectedTabIndex"
		show-table-of-contents
		@close-preview="emit('close-preview')"
		@tab-change="(e) => (selectedTabIndex = e.index)"
	>
		<template #name-input>
			<tera-input-text
				v-if="isRenamingDataset"
				v-model.lazy="newDatasetName"
				placeholder="Dataset name"
				@keyup.enter="updateDatasetName"
				@keyup.esc="updateDatasetName"
				auto-focus
			/>
			<div v-if="isRenamingDataset" class="flex flex-nowrap ml-1 mr-3">
				<Button icon="pi pi-check" rounded text @click="updateDatasetName" />
			</div>
		</template>
		<template #edit-buttons v-if="!featureConfig.isPreview">
			<Button
				icon="pi pi-ellipsis-v"
				class="p-button-icon-only p-button-text p-button-rounded"
				@click="toggleOptionsMenu"
			/>
			<ContextMenu ref="optionsMenu" :model="optionsMenuItems" :popup="true" :pt="optionsMenuPt" />
			<div class="btn-group">
				<Button label="Enrich metadata with AI assistant" severity="secondary" outlined />
				<Button label="Reset" severity="secondary" outlined />
				<Button label="Save as..." severity="secondary" outlined />
				<Button label="Save" />
			</div>
		</template>
		<section>
			<p>
				<span class="font-bold inline-block w-10rem">Dataset Id</span>
				<code class="inline">{{ datasetInfo.id }}</code>
			</p>
			<p>
				<span class="font-bold inline-block w-10rem">Dataset Filenames</span>
				{{ datasetInfo.fileNames }}<br />
			</p>
			<tera-dataset-description
				tabName="Description"
				:dataset="dataset"
				@fetch-dataset="fetchDataset"
				@update-dataset="(dataset: Dataset) => updateAndFetchDataset(dataset)"
			/>
			<template v-if="!isEmpty(datasetInfo.fileNames)">
				<tera-progress-spinner v-if="!rawContent" :font-size="2" is-centered />
				<tera-dataset-datatable v-else :rows="100" :raw-content="rawContent" />
			</template>
		</section>
	</tera-asset>
</template>
<script setup lang="ts">
import { computed, onUpdated, PropType, Ref, ref, watch } from 'vue';
import { cloneDeep, isEmpty } from 'lodash';
import { downloadRawFile, getClimateDataset, getDataset, getDownloadURL, updateDataset } from '@/services/dataset';
import { AssetType, type CsvAsset, type Dataset, type DatasetColumn, PresignedURL } from '@/types/Types';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import TeraAsset from '@/components/asset/tera-asset.vue';
import type { FeatureConfig } from '@/types/common';
import type { Source } from '@/types/search';
import { DatasetSource } from '@/types/search';
import { useProjects } from '@/composables/project';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import ContextMenu from 'primevue/contextmenu';
import Button from 'primevue/button';
import { logger } from '@/utils/logger';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import TeraDatasetDescription from './tera-dataset-description.vue';
import { enrichDataset } from './utils';

const props = defineProps({
	assetId: {
		type: String,
		required: true
	},
	featureConfig: {
		type: Object as PropType<FeatureConfig>,
		default: { isPreview: false } as FeatureConfig
	},
	source: {
		type: String as PropType<Source>,
		default: DatasetSource.TERARIUM
	}
});

const emit = defineEmits(['close-preview', 'asset-loaded']);
const dataset = ref<Dataset | null>(null);
const newDatasetName = ref('');
const isRenamingDataset = ref(false);
const rawContent: Ref<CsvAsset | null> = ref(null);
const isDatasetLoading = ref(false);
const selectedTabIndex = ref(0);

const datasetInfo = computed(() => {
	const information = {
		id: '',
		fileNames: ''
	};
	if (dataset.value) {
		information.id = dataset.value.id ?? '';
		information.fileNames = dataset.value.fileNames?.join(', ') ?? '';
	}
	return information;
});

const groundingValues = ref<string[][]>([]);
// originaGroundingValues are displayed as the first suggested value for concepts
const originalGroundingValues = ref<string[]>([]);
const suggestedValues = ref<string[]>([]);

const rowEditList = ref<boolean[]>([]);
// editableRows are the dataset columns that can be edited by the user; transient data
const editableRows = ref<DatasetColumn[]>([]);

const toggleOptionsMenu = (event) => {
	optionsMenu.value.toggle(event);
};

/**
 * Downloads the first file of the dataset from S3 directly
 * @param dataset
 */
async function downloadFileFromDataset(): Promise<PresignedURL | null> {
	if (dataset.value) {
		const { id, fileNames } = dataset.value;
		if (id && fileNames && fileNames.length > 0 && !isEmpty(fileNames[0])) {
			return (await getDownloadURL(id, fileNames[0])) ?? null;
		}
	}
	return null;
}

const optionsMenu = ref();
const optionsMenuItems = ref([
	{
		icon: 'pi pi-pencil',
		label: 'Rename',
		command() {
			isRenamingDataset.value = true;
			newDatasetName.value = dataset.value?.name ?? '';
		}
	},
	{
		icon: 'pi pi-plus',
		label: 'Add to project',
		items:
			useProjects()
				.allProjects.value?.filter((project) => project.id !== useProjects().activeProject.value?.id)
				.map((project) => ({
					label: project.name,
					command: async () => {
						const response = await useProjects().addAsset(AssetType.Dataset, props.assetId, project.id);
						if (response) logger.info(`Added asset to ${project.name}`);
					}
				})) ?? []
	},
	{
		icon: 'pi pi-download',
		label: 'Download',
		command: async () => {
			const presignedUrl: PresignedURL | null = await downloadFileFromDataset();
			if (presignedUrl) {
				window.open(presignedUrl.url, '_blank');
			}
			emit('close-preview');
		}
	}
]);
const optionsMenuPt = {
	submenu: {
		class: 'max-h-30rem overflow-y-scroll'
	}
};

// TODO - It's got to be a better way to do this
async function updateDatasetName() {
	if (dataset.value && newDatasetName.value !== '') {
		const datasetClone = cloneDeep(dataset.value);
		datasetClone.name = newDatasetName.value;
		await updateDataset(datasetClone);
		dataset.value = await getDataset(props.assetId);
		await useProjects().refresh();
		isRenamingDataset.value = false;
	}
}

async function updateAndFetchDataset(ds: Dataset) {
	await updateDataset(ds);
	fetchDataset();
}

const fetchDataset = async () => {
	if (props.source === DatasetSource.TERARIUM) {
		const datasetTemp = await getDataset(props.assetId);
		if (datasetTemp) {
			dataset.value = enrichDataset(datasetTemp);
		}
	} else if (props.source === DatasetSource.ESGF) {
		dataset.value = await getClimateDataset(props.assetId);
	}
};

onUpdated(() => {
	if (dataset.value) {
		emit('asset-loaded');

		// setting values related to editing rows in the variables table
		if (dataset.value.columns) {
			rowEditList.value = dataset.value.columns.map(() => false);
			suggestedValues.value = dataset.value.columns.map(() => '');
			editableRows.value = dataset.value.columns.map((c) => ({ ...c }));
			groundingValues.value = editableRows.value.map((row) => {
				const grounding = row.grounding;
				if (grounding) {
					const keys = Object.keys(grounding.identifiers);
					return keys.map((k) => grounding.identifiers[k]);
				}
				return [];
			});
			originalGroundingValues.value = groundingValues.value.map((g) => g[0]);
		}
	}
});

// Whenever assetId changes, fetch dataset with that ID
watch(
	() => [props.assetId],
	async () => {
		isRenamingDataset.value = false;
		if (props.assetId !== '') {
			isDatasetLoading.value = true;
			fetchDataset().then(() => {
				isDatasetLoading.value = false;
			});
		} else {
			dataset.value = null;
			rawContent.value = null;
		}
	},
	{ immediate: true }
);

// Whenever we change the dataset, we need to fetch the rawContent
watch(
	() => dataset.value,
	async () => {
		// If it's an ESGF dataset or a NetCDF file, we don't want to download the raw content
		if (!dataset.value || dataset.value.esgfId || dataset.value.metadata?.format === 'netcdf') return;

		// We are assuming here there is only a single csv file.
		if (
			dataset.value.fileNames &&
			dataset.value.fileNames.length > 0 &&
			!isEmpty(dataset.value.fileNames[0]) &&
			dataset.value.fileNames[0].endsWith('.csv')
		) {
			downloadRawFile(props.assetId, dataset.value.fileNames[0]).then((res) => {
				rawContent.value = res;
			});
		}
	}
);
</script>

<style scoped>
.btn-group {
	display: flex;
	align-items: center;
	gap: var(--gap-small);
	margin-left: auto;
}
</style>
