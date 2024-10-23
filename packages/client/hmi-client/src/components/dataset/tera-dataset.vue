<template>
	<tera-asset
		v-bind="$attrs"
		show-table-of-contents
		:feature-config="featureConfig"
		:id="assetId"
		:is-loading="isDatasetLoading"
		:is-naming-asset="isRenaming"
		:name="dataset?.name"
		:overflow-hidden="selectedTabIndex === 1"
		:selected-tab-index="selectedTabIndex"
		@close-preview="emit('close-preview')"
		@tab-change="(e) => (selectedTabIndex = e.index)"
	>
		<template #name-input>
			<tera-input-text
				v-if="isRenaming"
				v-model.lazy="newName"
				placeholder="Dataset name"
				@keyup.enter="updateDatasetName"
				@keyup.esc="updateDatasetName"
				auto-focus
			/>
			<Button v-if="isRenaming" icon="pi pi-check" rounded text @click="updateDatasetName" />
		</template>
		<template #edit-buttons v-if="!featureConfig.isPreview">
			<Button
				icon="pi pi-ellipsis-v"
				class="p-button-icon-only p-button-text p-button-rounded"
				@click="toggleOptionsMenu"
			/>
			<ContextMenu ref="optionsMenu" :model="optionsMenuItems" popup :pt="optionsMenuPt" />
			<div class="ml-auto flex gap-2">
				<tera-asset-enrichment :asset-type="AssetType.Dataset" :assetId="assetId" @finished-job="fetchDataset" />
				<Button label="Reset" severity="secondary" outlined @click="reset" />
				<Button label="Save as..." severity="secondary" outlined @click="showSaveModal = true" disabled />
				<Button label="Save" :disabled="isSaved" @click="updateDatasetContent" />
			</div>
		</template>
		<section>
			<Accordion multiple :active-index="currentActiveIndexes">
				<AccordionTab header="Description">
					<section class="description">
						<label class="p-text-secondary">Dataset ID</label>
						<p>{{ dataset?.id }}</p>
						<label class="p-text-secondary">Files names</label>
						<p>{{ dataset?.fileNames?.toString() }}</p>
						<label class="p-text-secondary">Description</label>
						<tera-show-more-text :text="description" :lines="5" />
						<template v-if="datasetType">
							<label class="p-text-secondary">Dataset type</label>
							<p>{{ datasetType }}</p>
						</template>
						<template v-if="author">
							<label class="p-text-secondary">Author</label>
							<p>{{ author }}</p>
						</template>
					</section>
				</AccordionTab>
				<AccordionTab header="Column information">
					<tera-column-info
						v-for="(column, index) in columnInformation"
						:key="index"
						class="column-info"
						:column="column"
						:feature-config="{ isPreview: false }"
						@update-column="updateColumn(index, $event.key, $event.value)"
					/>
				</AccordionTab>
				<AccordionTab header="Data" v-if="!isEmpty(dataset?.fileNames)">
					<tera-progress-spinner v-if="!rawContent" :font-size="2" is-centered />
					<tera-dataset-datatable v-else :rows="100" :raw-content="rawContent" />
				</AccordionTab>
			</Accordion>
		</section>
	</tera-asset>
</template>

<script setup lang="ts">
import { computed, PropType, ref, watch } from 'vue';
import { cloneDeep, isEmpty, isEqual } from 'lodash';
import {
	downloadRawFile,
	getClimateDataset,
	getClimateDatasetPreview,
	getDataset,
	getDownloadURL,
	updateDataset
} from '@/services/dataset';
import { AssetType, type CsvAsset, type Dataset, PresignedURL } from '@/types/Types';
import TeraAsset from '@/components/asset/tera-asset.vue';
import type { FeatureConfig } from '@/types/common';
import { DatasetSource } from '@/types/Dataset';
import { useProjects } from '@/composables/project';
import TeraInputText from '@/components/widgets/tera-input-text.vue';
import TeraShowMoreText from '@/components/widgets/tera-show-more-text.vue';
import ContextMenu from 'primevue/contextmenu';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Button from 'primevue/button';
import { logger } from '@/utils/logger';
import TeraAssetEnrichment from '@/components/widgets/tera-asset-enrichment.vue';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import TeraColumnInfo from '@/components/dataset/tera-column-info.vue';

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
		type: String as PropType<DatasetSource>,
		default: DatasetSource.TERARIUM
	}
});

const emit = defineEmits(['close-preview']);

const currentActiveIndexes = ref([1, 2, 3, 4]);
const dataset = ref<Dataset | null>(null);
const transientDataset = ref<Dataset | null>(null);
const newName = ref('');
const isRenaming = ref(false);
const showSaveModal = ref(false);
const rawContent = ref<CsvAsset | null>(null);
const isDatasetLoading = ref(false);
const selectedTabIndex = ref(0);

const optionsMenu = ref();
const optionsMenuPt = {
	submenu: {
		class: 'max-h-30rem overflow-y-scroll'
	}
};
const optionsMenuItems = ref([
	{
		icon: 'pi pi-pencil',
		label: 'Rename',
		command() {
			isRenaming.value = true;
			newName.value = dataset.value?.name ?? '';
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

const isSaved = computed(() => isEqual(dataset.value, transientDataset.value));
const columnInformation = computed(
	() =>
		transientDataset.value?.columns?.map((column) => ({
			symbol: column.name, // Uneditable
			description: column.description,
			grounding: column?.grounding,
			dataType: column.dataType,
			// Metadata
			name: column.metadata?.name ?? column.name,
			unit: column.metadata?.unit,
			stats: column.metadata?.column_stats // Uneditable
		})) ?? []
);

const datasetType = computed(() => card.value?.DATASET_TYPE ?? '');

const image = ref<string | undefined>(undefined);

const card = computed(() => {
	if (dataset.value?.metadata?.data_card) {
		const cardWithUnknowns = dataset.value.metadata?.data_card;
		const cardWithUnknownsArr = Object.entries(cardWithUnknowns);

		for (let i = 0; i < cardWithUnknownsArr.length; i++) {
			const key = cardWithUnknownsArr[i][0];
			if (cardWithUnknowns[key] === 'UNKNOWN') {
				cardWithUnknowns[key] = null;
			}
		}
		return cardWithUnknowns;
	}
	return null;
});
const description = computed(() => dataset.value?.description?.concat('\n', card.value?.DESCRIPTION ?? '') ?? '');
const author = computed(() => card.value?.AUTHOR_NAME ?? '');

function updateColumn(index: number, key: string, value: any) {
	if (!transientDataset.value?.columns?.[index]) return;
	if (key === 'unit' || key === 'name') {
		if (!transientDataset.value.columns[index].metadata) {
			transientDataset.value.columns[index].metadata = {};
		}
		transientDataset.value.columns[index].metadata[key] = value;
	} else if (key === 'concept') {
		// Only one identifier is supported for now
		if (!transientDataset.value.columns[index]?.grounding?.identifiers) {
			transientDataset.value.columns[index].grounding = { identifiers: [] };
		}
		// Replaces first element of identifiers' array
		transientDataset.value.columns[index].grounding?.identifiers?.shift();
		transientDataset.value.columns[index].grounding?.identifiers?.unshift(value);
	} else {
		transientDataset.value.columns[index][key] = value;
	}
}

const toggleOptionsMenu = (event) => {
	optionsMenu.value.toggle(event);
};

async function downloadFileFromDataset(): Promise<PresignedURL | null> {
	if (dataset.value) {
		const { id, fileNames } = dataset.value;
		if (id && fileNames && fileNames.length > 0 && !isEmpty(fileNames[0])) {
			return (await getDownloadURL(id, fileNames[0])) ?? null;
		}
	}
	return null;
}

async function updateDatasetContent() {
	if (!transientDataset.value) return;
	if (!useProjects().hasEditPermission()) {
		logger.error('You do not have permission to edit this dataset.'); // FIXME: Disable asset editing options if user does not have permission
		return;
	}
	await updateDataset(transientDataset.value);
	logger.info('Saved changes.');
	await useProjects().refresh();
	await fetchDataset();
}

async function updateDatasetName() {
	if (transientDataset.value && !isEmpty(newName.value)) {
		transientDataset.value.name = newName.value;
		await updateDatasetContent();
	}
	isRenaming.value = false;
}

function reset() {
	transientDataset.value = cloneDeep(dataset.value);
}

const fetchDataset = async () => {
	if (props.source === DatasetSource.TERARIUM) {
		dataset.value = await getDataset(props.assetId);
	} else if (props.source === DatasetSource.ESGF) {
		dataset.value = await getClimateDataset(props.assetId);
	}
	reset(); // Prepare transientDataset for editing

	if (dataset.value?.esgfId && !image.value) {
		image.value = await getClimateDatasetPreview(dataset.value.esgfId);
	}
};

function getRawContent() {
	// If it's an ESGF dataset or a NetCDF file, we don't want to download the raw content
	if (!dataset.value || dataset.value.esgfId || dataset.value.metadata?.format === 'netcdf') return;
	// We are assuming here there is only a single csv file.
	if (
		dataset.value.fileNames &&
		!isEmpty(dataset.value.fileNames) &&
		!isEmpty(dataset.value.fileNames[0]) &&
		dataset.value.fileNames[0].endsWith('.csv')
	) {
		downloadRawFile(props.assetId, dataset.value.fileNames[0]).then((res) => {
			rawContent.value = res;
		});
	}
}

// Whenever assetId changes, fetch dataset with that ID
watch(
	() => props.assetId,
	async () => {
		isRenaming.value = false;
		if (props.assetId) {
			// Empty the dataset and rawContent so previous data is not shown
			dataset.value = null;
			rawContent.value = null;
			isDatasetLoading.value = true;
			await fetchDataset();
			isDatasetLoading.value = false;
			if (dataset.value) {
				getRawContent(); // Whenever we change the dataset, we need to fetch the rawContent
			}
		}
	},
	{ immediate: true }
);
</script>

<style scoped>
.column-info {
	border-bottom: 1px solid var(--surface-border);
	margin-bottom: var(--gap-3);
	padding-bottom: var(--gap-3);
}

.description {
	margin-left: var(--gap-6);

	label + * {
		margin-bottom: var(--gap-4);
		margin-top: var(--gap-2);
	}
}
</style>
