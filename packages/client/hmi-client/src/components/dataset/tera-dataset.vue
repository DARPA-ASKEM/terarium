<template>
	<tera-asset
		:name="dataset?.name"
		:feature-config="featureConfig"
		:is-naming-asset="isRenamingDataset"
		:stretch-content="view === DatasetView.DATA"
		@close-preview="emit('close-preview')"
		:is-loading="isDatasetLoading"
	>
		<TabView :active-index="selectedViewIndex" @tab-change="(e) => (selectedViewIndex = e.index)">
			<TabPanel header="Description">
				<tera-dataset-description :dataset="dataset" :raw-content="rawContent" />
			</TabPanel>
			<TabPanel header="Data">
				<Accordion :multiple="true" :activeIndex="[0, 1]">
					<AccordionTab>
						<template #header>
							Data preview<span class="artifact-amount">({{ csvContent?.length }} rows)</span>
						</template>
						<tera-dataset-datatable :rows="100" :raw-content="rawContent" />
					</AccordionTab>
				</Accordion>
			</TabPanel>
		</TabView>
	</tera-asset>
</template>
<script setup lang="ts">
import { computed, ref, watch, onUpdated, Ref, PropType } from 'vue';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
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

const csvContent = computed(() => rawContent.value?.csv);

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
.container {
	margin-left: 1rem;
	margin-right: 1rem;
	max-width: 70rem;
}

.metadata {
	margin: 1rem;
	display: flex;
	flex-direction: row;
	justify-content: space-evenly;
}

.metadata > section {
	flex: 1;
}

/* Datatable  */
.data-row > section > header {
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
}

.data-row > section > section:last-child {
	font-size: var(--font-body-small);
}

.feature-type {
	color: var(--text-color-subdued);
}

.description {
	padding: 1rem;
	padding-bottom: 0.5rem;
	max-width: var(--constrain-width);
}

main .annotation-group {
	padding: 0.25rem;
	border: solid 1px var(--surface-border-light);
	background-color: var(--gray-50);
	border-radius: var(--border-radius);
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
	margin-bottom: 1rem;
	max-width: var(--constrain-width);
}

.annotation-subheader {
	font-weight: var(--font-weight-semibold);
}

.annotation-row {
	display: flex;
	flex-direction: row;
	gap: 3rem;
}

.tera-dataset-datatable {
	width: 100%;
}

.kernel-status {
	margin-right: 10px;
}

.gpt-header {
	display: flex;
	width: 90%;
}

.variables-table {
	display: grid;
	grid-template-columns: 1fr;
}

.variables-table div {
	padding: 0.25rem;
}

.variables-header {
	display: grid;
	grid-template-columns: repeat(6, 1fr) 0.5fr;
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
}

.variables-row {
	display: grid;
	grid-template-columns: repeat(6, 1fr) 0.5fr;
	grid-template-rows: 1fr 1fr;
	border-top: 1px solid var(--surface-border);
}

.variables-row:hover {
	background-color: var(--surface-highlight);
}

.variables-row[active='true'] {
	background-color: var(--surface-highlight);
	grid-template-rows: 1fr 1fr 1fr 1fr 1fr;
}

.variables-description {
	grid-row: 2;
	grid-column: 1 / span 6;
	color: var(--text-color-subdued);
}

.variables-suggested {
	grid-row: 3 / span 3;
	grid-column: 1 / span 6;
}

.variables-suggested div {
	display: flex;
	white-space: nowrap;
	overflow: hidden;
}

.variables-suggested span {
	font-weight: bold;
}

.variables-suggested i {
	margin-right: 0.5rem;
}

.suggested-value-source {
	margin-right: 2rem;
}

.suggested-value {
	color: var(--text-color-subdued);
}

.variables-suggested .suggested-value ul {
	flex-direction: row;
}

main :deep(.p-inputtext.p-inputtext-sm) {
	padding-left: 0.65rem;
}

.row-edit-buttons {
	display: flex;
	justify-content: space-evenly;
}

.dataset-detail {
	display: flex;
	flex-wrap: wrap;
	justify-content: space-between;
	font-family: var(--font-family);
	transition: all 0.3s ease;
	padding: 20px;
}

.column,
.detail-list {
	margin-bottom: 20px;
}

.full-width {
	width: 100%;
}

.detail-list {
	padding: 10px;
	border-radius: 5px;
	box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
}

.detail-list ul {
	list-style: none;
	padding: 0;
}

.detail-list ul li {
	margin-bottom: 10px;
}

.title {
	font-size: 16px;
	margin-bottom: 20px;
}

.subtitle {
	font-size: 14px;
	margin-top: 20px;
	margin-bottom: 10px;
}

.content {
	font-size: 12px;
	margin-bottom: 10px;
}

.example-list {
	list-style-type: none;
	padding: 0;
}

.example-item {
	margin-bottom: 10px;
}

.example-item:hover {
	color: #f78c6c;
	transition: all 0.3s ease;
}

.tabs-view:deep(.p-tabview-nav-content) {
	padding: 0 1rem;
	background-color: var(--surface-disabled);
}
:deep(.p-tabview .p-tabview-nav) {
	background: none;
}
</style>
