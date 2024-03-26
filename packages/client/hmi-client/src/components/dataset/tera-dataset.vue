<template>
	<tera-asset
		:name="dataset?.name"
		:feature-config="featureConfig"
		:is-naming-asset="isRenamingDataset"
		:stretch-content="view === DatasetView.DATA"
		@close-preview="emit('close-preview')"
		:is-loading="isDatasetLoading"
		:overflow-hidden="selectedTabIndex === 1"
		:selected-tab-index="selectedTabIndex"
		@tab-change="(e) => (selectedTabIndex = e.index)"
	>
		<template #name-input>
			<InputText
				v-if="isRenamingDataset"
				v-model.lazy="newDatasetName"
				placeholder="Dataset name"
				@keyup.enter="updateDatasetName"
			/>
		</template>
		<template #edit-buttons>
			<template v-if="!featureConfig.isPreview">
				<Button
					icon="pi pi-ellipsis-v"
					class="p-button-icon-only p-button-text p-button-rounded"
					@click="toggleOptionsMenu"
				/>
				<ContextMenu ref="optionsMenu" :model="optionsMenuItems" :popup="true" />
			</template>
		</template>

		<template #tabs>
			<section class="tab" tabName="Description">
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
					@update-dataset="(dataset: Dataset) => updateAndFetchDataset(dataset)"
				/>
			</section>
			<section class="tab data-tab" tabName="Data" v-if="rawContent">
				<tera-dataset-datatable :rows="100" :raw-content="rawContent" />
			</section>
		</template>
	</tera-asset>
</template>
<script setup lang="ts">
import { computed, onUpdated, PropType, Ref, ref, watch } from 'vue';
import * as textUtil from '@/utils/text';
import { cloneDeep, isEmpty, isString } from 'lodash';
import {
	downloadFile,
	downloadRawFile,
	getClimateDataset,
	getDataset,
	updateDataset
} from '@/services/dataset';
import { AssetType, type CsvAsset, type Dataset, type DatasetColumn } from '@/types/Types';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import TeraAsset from '@/components/asset/tera-asset.vue';
import { FeatureConfig } from '@/types/common';
import { useProjects } from '@/composables/project';
import InputText from 'primevue/inputtext';
import ContextMenu from 'primevue/contextmenu';
import Button from 'primevue/button';
import { logger } from '@/utils/logger';
import { DatasetSource } from '@/types/Dataset';
import TeraDatasetDescription from './tera-dataset-description.vue';
import { enrichDataset } from './utils';

enum DatasetView {
	DESCRIPTION = 'Description',
	DATA = 'Data',
	LLM = 'LLM'
}

const props = defineProps({
	assetId: {
		type: String,
		required: true
	},
	featureConfig: {
		type: Object as PropType<FeatureConfig>,
		default: { isPreview: false } as FeatureConfig
	},
	highlight: {
		type: String,
		default: null
	},
	datasetSource: {
		type: String as PropType<DatasetSource>,
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
const view = ref(DatasetView.DESCRIPTION);

// Highlight strings based on props.highlight
function highlightSearchTerms(text: string | undefined): string {
	if (!!props.highlight && !!text) {
		return textUtil.highlight(text, props.highlight);
	}
	return text ?? '';
}

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
// editableRows is are the dataset columns that can be edited by the user; transient data
const editableRows = ref<DatasetColumn[]>([]);

const toggleOptionsMenu = (event) => {
	optionsMenu.value.toggle(event);
};

/**
 * Downloads the first file of the dataset from S3 directly
 * @param dataset
 */
async function downloadFileFromDataset(): Promise<string> {
	if (dataset.value) {
		const { id, fileNames } = dataset.value;
		if (id && fileNames && fileNames.length > 0 && !isEmpty(fileNames[0])) {
			return (await downloadFile(id, fileNames[0])) ?? '';
		}
	}
	return '';
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
				.allProjects.value?.filter(
					(project) => project.id !== useProjects().activeProject.value?.id
				)
				.map((project) => ({
					label: project.name,
					command: async () => {
						const response = await useProjects().addAsset(
							AssetType.Dataset,
							props.assetId,
							project.id
						);
						if (response) logger.info(`Added asset to ${project.name}`);
					}
				})) ?? []
	},
	{
		icon: 'pi pi-download',
		label: 'Download',
		command: async () => {
			const url = await downloadFileFromDataset();
			if (url && !isEmpty(url)) {
				window.open(url, '_blank');
			}
			emit('close-preview');
		}
	}
	// ,{ icon: 'pi pi-trash', label: 'Remove', command: deleteDataset }
]);

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
	switch (props.datasetSource) {
		case DatasetSource.TERARIUM: {
			const datasetTemp = await getDataset(props.assetId);
			if (datasetTemp) {
				if (datasetTemp.esgfId || datasetTemp.metadata?.format === 'netcdf') {
					rawContent.value = null;
				} else {
					// We are assuming here there is only a single csv file. This may change in the future as the API allows for it.
					// TODO = Temporary solution to avoid downloading raw NetCDF files, which can be massive
					// A better solution would be to check the size of an asset before downloading it, and/or
					// downloading a small subset of it for presentation purposes.
					if (datasetTemp.metadata?.format !== 'netcdf' || !datasetTemp.esgfId) {
						rawContent.value = await downloadRawFile(
							props.assetId,
							datasetTemp?.fileNames?.[0] ?? ''
						);
					}
					Object.entries(datasetTemp).forEach(([key, value]) => {
						if (isString(value)) {
							datasetTemp[key] = highlightSearchTerms(value);
						}
					});
				}
				dataset.value = enrichDataset(datasetTemp);
			}
			break;
		}
		case DatasetSource.ESGF: {
			dataset.value = await getClimateDataset(props.assetId);
			break;
		}
		default:
			break;
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
			await fetchDataset();
			isDatasetLoading.value = false;
		} else {
			dataset.value = null;
			rawContent.value = null;
		}
	},
	{ immediate: true }
);
</script>

<style scoped>
.tab {
	padding: var(--gap);

	&.data-tab {
		display: flex;
		height: 100%;
		overflow: hidden;
		flex-direction: column;
	}
}
</style>
