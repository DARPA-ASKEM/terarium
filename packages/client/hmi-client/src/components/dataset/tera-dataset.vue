<template>
	<tera-asset
		:name="dataset?.name"
		:feature-config="featureConfig"
		:is-naming-asset="isRenamingDataset"
		:stretch-content="view === DatasetView.DATA"
		@close-preview="emit('close-preview')"
		:is-loading="isDatasetLoading"
		:overflow-hidden="selectedViewIndex === 1"
	>
		<TabView :active-index="selectedViewIndex" @tab-change="(e) => (selectedViewIndex = e.index)">
			<TabPanel header="Description">
				<tera-dataset-description :dataset="dataset" :raw-content="rawContent" />
			</TabPanel>
			<TabPanel header="Data">
				<tera-dataset-datatable :rows="100" :raw-content="rawContent" />
			</TabPanel>
		</TabView>
	</tera-asset>
</template>
<script setup lang="ts">
import { ref, watch, onUpdated, Ref, PropType } from 'vue';
import * as textUtil from '@/utils/text';
import { isString } from 'lodash';
import { downloadRawFile, getDataset } from '@/services/dataset';
import { CsvAsset, Dataset, DatasetColumn } from '@/types/Types';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import TeraAsset from '@/components/asset/tera-asset.vue';
import { FeatureConfig } from '@/types/common';
import TabView from 'primevue/tabview';
import TabPanel from 'primevue/tabpanel';
import { enrichDataset } from './utils';
import TeraDatasetDescription from './tera-dataset-description.vue';

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
	}
});

const emit = defineEmits(['close-preview', 'asset-loaded']);
const newCsvContent: any = ref(null);
const newCsvHeader: any = ref(null);
const oldCsvHeaders: any = ref(null);
const dataset = ref<Dataset | null>(null);
const isRenamingDataset = ref(false);
const rawContent: Ref<CsvAsset | null> = ref(null);
const jupyterCsv: Ref<CsvAsset | null> = ref(null);
const isDatasetLoading = ref(false);
const selectedViewIndex = ref(0);

const view = ref(DatasetView.DESCRIPTION);

// Highlight strings based on props.highlight
function highlightSearchTerms(text: string | undefined): string {
	if (!!props.highlight && !!text) {
		return textUtil.highlight(text, props.highlight);
	}
	return text ?? '';
}
const groundingValues = ref<string[][]>([]);
// originaGroundingValues are displayed as the first suggested value for concepts
const originalGroundingValues = ref<string[]>([]);
const suggestedValues = ref<string[]>([]);

const rowEditList = ref<boolean[]>([]);
// editableRows is are the dataset columns that can be edited by the user; transient data
const editableRows = ref<DatasetColumn[]>([]);

const fetchDataset = async () => {
	const datasetTemp: Dataset | null = await getDataset(props.assetId);

	// We are assuming here there is only a single csv file. This may change in the future as the API allows for it.
	rawContent.value = await downloadRawFile(props.assetId, datasetTemp?.fileNames?.[0] ?? '');
	if (datasetTemp) {
		Object.entries(datasetTemp).forEach(([key, value]) => {
			if (isString(value)) {
				datasetTemp[key] = highlightSearchTerms(value);
			}
		});
		dataset.value = enrichDataset(datasetTemp);
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

watch(
	() => [jupyterCsv.value?.csv],
	() => {
		if (jupyterCsv.value?.csv) {
			oldCsvHeaders.value = newCsvHeader.value;
			newCsvContent.value = jupyterCsv.value.csv.slice(1, jupyterCsv.value.csv.length);
			newCsvHeader.value = jupyterCsv.value.headers;
		}
	}
);

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
.tabs-view:deep(.p-tabview-nav-content) {
	padding: 0 1rem;
	background-color: var(--surface-disabled);
}
:deep(.p-tabview .p-tabview-nav) {
	background: none;
}

:deep(.p-tabview.p-component) {
	display: flex;
	height: 100%;
	overflow: hidden;
	flex-direction: column;
}

:deep(.p-tabview-panels),
:deep(.p-tabview-panel) {
	overflow: hidden;
	/* display: flex; */
	/* height: 100%; */
	flex: 1;
	display: flex;
	flex-direction: column;
}
</style>
