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
				<Menu ref="optionsMenu" :model="optionsMenuItems" :popup="true" />
			</template>
		</template>

		<template #tabs>
			<section class="tab" tabName="Description">
				<tera-dataset-description
					tabName="Description"
					:dataset="dataset"
					:raw-content="rawContent"
				/>
			</section>
			<section class="tab data-tab" tabName="Data">
				<tera-dataset-datatable :rows="100" :raw-content="rawContent" />
			</section>
		</template>
	</tera-asset>
</template>
<script setup lang="ts">
import { ref, watch, onUpdated, Ref, PropType } from 'vue';
import * as textUtil from '@/utils/text';
import { cloneDeep, isString } from 'lodash';
import { downloadRawFile, getDataset, updateDataset } from '@/services/dataset';
import type { CsvAsset, Dataset, DatasetColumn } from '@/types/Types';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import TeraAsset from '@/components/asset/tera-asset.vue';
import { FeatureConfig } from '@/types/common';
import { useProjects } from '@/composables/project';
import InputText from 'primevue/inputtext';
import Menu from 'primevue/menu';
import Button from 'primevue/button';
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

const optionsMenu = ref();
const optionsMenuItems = ref([
	{
		icon: 'pi pi-pencil',
		label: 'Rename',
		command() {
			isRenamingDataset.value = true;
			newDatasetName.value = dataset.value?.name ?? '';
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
		useProjects().refresh();
		isRenamingDataset.value = false;
	}
}

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
