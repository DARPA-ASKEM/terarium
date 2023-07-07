<template>
	<tera-asset
		v-if="dataset"
		:name="dataset?.name"
		:is-editable="isEditable"
		:stretch-content="datasetView === DatasetView.DATA"
		@close-preview="emit('close-preview')"
	>
		<template #edit-buttons>
			<span class="p-buttonset">
				<Button
					class="p-button-secondary p-button-sm"
					label="Description"
					icon="pi pi-list"
					@click="datasetView = DatasetView.DESCRIPTION"
					:active="datasetView === DatasetView.DESCRIPTION"
				/>
				<Button
					class="p-button-secondary p-button-sm"
					label="Data"
					icon="pi pi-file"
					@click="datasetView = DatasetView.DATA"
					:active="datasetView === DatasetView.DATA"
				/>
				<Button
					v-if="isEditable"
					class="p-button-secondary p-button-sm"
					label="Transform"
					icon="pi pi-sync"
					@click="openDatesetChatTab"
					:active="datasetView === DatasetView.LLM"
				/>
			</span>
			<span v-if="datasetView === DatasetView.LLM && isEditable">
				<i class="pi pi-cog" @click="toggleSettingsMenu" />
				<Menu ref="menu" id="overlay_menu" :model="items" :popup="true" />
			</span>
		</template>
		<template v-if="datasetView === DatasetView.DESCRIPTION">
			<div class="container">
				<Message class="inline-message" icon="none"
					>This page describes the dataset. Use the content switcher above to see the data table and
					transformation tools.</Message
				>
			</div>
			<section class="metadata data-row" v-if="!metadata">
				<section>
					<header>Rows</header>
					<section>{{ csvContent?.length || '-' }}</section>
				</section>
				<section>
					<header>Columns</header>
					<section>{{ rawContent?.stats?.length || '-' }}</section>
					<header>Metadata</header>
					<section>{{ dataset?.metadata || '-' }}</section>
				</section>
				<section>
					<header>Date uploaded</header>
					<section>
						{{ new Date(dataset?.timestamp as Date).toLocaleString('en-US') || '-' }}
					</section>
				</section>
				<section>
					<header>Uploaded by</header>
					<section>{{ dataset?.username || '-' }}</section>
				</section>
			</section>
			<section class="metadata data-row" v-if="!metadata">
				<section>
					<header>Source Name</header>
					<section>{{ dataset?.source || '-' }}</section>
				</section>
				<section>
					<header>Source URL</header>
					<section>
						<a v-if="dataset?.url" :href="dataset?.url">{{ dataset?.url || '-' }}</a>
						<span v-else>-</span>
					</section>
				</section>
			</section>
			<RelatedPublications
				@extracted-metadata="(extract) => (metadata = extract)"
				:publications="[metadata?.source]"
			/>
			<Accordion :multiple="true" :activeIndex="showAccordion">
				<AccordionTab>
					<template #header>
						<header id="Description">Description</header>
					</template>
					<section v-if="metadata">
						<ul>
							<li>Dataset name: {{ metadata.name }}</li>
							<li>Dataset overview: {{ metadata.description }}</li>
							<li>Dataset URL: {{ metadata.source }}</li>
							<li>
								Data size: This dataset currently contains {{ csvContent?.length || '-' }} rows.
							</li>
						</ul>
					</section>
					<p v-else v-html="dataset?.description" />
				</AccordionTab>
				<AccordionTab v-if="metadata">
					<template #header>
						<header id="Source">Source</header>
					</template>
					This data is sourced from {{ metadata.source }}
				</AccordionTab>
				<AccordionTab v-if="metadata">
					<template #header>
						<header id="Variables">Variables</header>
					</template>
					<div class="variables-table">
						<div class="variables-header">
							<div
								v-for="(title, index) in [
									'ID',
									'NAME',
									'DATA TYPE',
									'UNITS',
									'GROUNDING',
									'EXTRACTIONS'
								]"
								:key="index"
							>
								{{ title }}
							</div>
						</div>
						<div v-for="(column, index) in metadata.columns" class="variables-row" :key="index">
							<div>{{ column.name }}</div>
							<div>{{ formatName(column.name) }}</div>
							<div>{{ column.data_type }}</div>
							<div>-</div>
							<div>
								{{ column.grounding.identifiers[Object.keys(column.grounding.identifiers)[0]] }}
							</div>
							<div></div>
							<div class="variables-description">{{ column.description }}</div>
						</div>
					</div>
				</AccordionTab>
				<AccordionTab v-if="(annotations?.length || 0) > 0">
					<template #header>
						<header id="Annotations">
							Annotations
							<span class="artifact-amount"> ({{ annotations?.length || 0 }}) </span>
						</header>
					</template>
					<section v-if="annotations">
						<header class="annotation-subheader">Annotations</header>
						<section class="annotation-group">
							<section
								v-for="name in annotations.map((annotation) => annotation['name'])"
								:key="name"
								class="annotation-row data-row"
							>
								<section>
									<header>Name</header>
									<section>{{ name }}</section>
								</section>
								<section>
									<header>Description</header>
									<section>{{ annotations[name] }}</section>
								</section>
							</section>
						</section>
					</section>
				</AccordionTab>
			</Accordion>
			<Accordion :multiple="true" :activeIndex="[0, 1]">
				<AccordionTab v-if="(annotations?.['feature']?.length || 0) > 0">
					<template #header>
						<header id="Variables">
							Variables<span class="artifact-amount">({{ annotations?.['feature']?.length }})</span>
						</header>
					</template>
					<DataTable :value="annotations?.['feature']">
						<Column field="name" header="Name"></Column>
						<Column field="featureType" header="Type"></Column>
						<Column field="description" header="Definition"></Column>
						<Column field="units" header="Units"></Column>
						<Column field="concept" header="Concept"></Column>
					</DataTable>
				</AccordionTab>
			</Accordion>
		</template>
		<template v-else-if="datasetView === DatasetView.DATA">
			<Accordion :multiple="true" :activeIndex="[0, 1]">
				<AccordionTab>
					<template #header>
						Data preview<span class="artifact-amount">({{ csvContent?.length }} rows)</span>
					</template>
					<tera-dataset-datatable :rows="100" :raw-content="rawContent" />
				</AccordionTab>
			</Accordion>
		</template>
		<template v-else-if="datasetView === DatasetView.LLM && isEditable">
			<tera-dataset-jupyter-panel
				:asset-id="props.assetId"
				:project="props.project"
				:dataset="dataset"
			/>
		</template>
	</tera-asset>
</template>
<script setup lang="ts">
import { computed, ref, watch, onUpdated, Ref } from 'vue';
import Accordion from 'primevue/accordion';
import Button from 'primevue/button';
import AccordionTab from 'primevue/accordiontab';
import Message from 'primevue/message';
import * as textUtil from '@/utils/text';
import { isString } from 'lodash';
import { downloadRawFile, getDataset } from '@/services/dataset';
import { CsvAsset, Dataset } from '@/types/Types';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import TeraDatasetJupyterPanel from '@/components/dataset/tera-dataset-jupyter-panel.vue';
import TeraAsset from '@/components/asset/tera-asset.vue';
import { IProject } from '@/types/Project';
import Menu from 'primevue/menu';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import RelatedPublications from '../widgets/tera-related-publications.vue';

enum DatasetView {
	DESCRIPTION = 'description',
	DATA = 'data',
	LLM = 'llm'
}
const props = defineProps<{
	assetId: string;
	isEditable: boolean;
	highlight?: string;
	project: IProject;
}>();

const emit = defineEmits(['close-preview', 'asset-loaded']);
const metadata = ref();
const showKernels = ref(<boolean>false);
const showChatThoughts = ref(<boolean>false);
const menu = ref();
const newCsvContent: any = ref(null);
const newCsvHeader: any = ref(null);
const oldCsvHeaders: any = ref(null);
const dataset: Ref<Dataset | null> = ref(null);
const rawContent: Ref<CsvAsset | null> = ref(null);
const jupyterCsv: Ref<CsvAsset | null> = ref(null);

const toggleSettingsMenu = (event: Event) => {
	menu.value.toggle(event);
};

const datasetView = ref(DatasetView.DESCRIPTION);

const chatThoughtLabel = computed(() =>
	showChatThoughts.value ? 'Auto hide chat thoughts' : 'Do not auto hide chat thoughts'
);

const kernelSettingsLabel = computed(() =>
	showKernels.value ? 'Hide Kernel Settings' : 'Show Kernel Settings'
);

const csvContent = computed(() => rawContent.value?.csv);

const items = ref([
	{
		label: 'Chat Options',
		items: [
			{
				label: kernelSettingsLabel,
				command: () => {
					showKernels.value = !showKernels.value;
				}
			},
			{
				label: chatThoughtLabel,
				command: () => {
					showChatThoughts.value = !showChatThoughts.value;
					console.log(showChatThoughts);
				}
			}
		]
	}
]);

// Highlight strings based on props.highlight
function highlightSearchTerms(text: string | undefined): string {
	if (!!props.highlight && !!text) {
		return textUtil.highlight(text, props.highlight);
	}
	return text ?? '';
}

const openDatesetChatTab = () => {
	datasetView.value = DatasetView.LLM;
	jupyterCsv.value = null;
};

onUpdated(() => {
	if (dataset.value) {
		emit('asset-loaded');
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
		if (props.assetId !== '') {
			const datasetTemp: Dataset | null = await getDataset(props.assetId);

			// We are assuming here there is only a single csv file. This may change in the future as the API allows for it.
			rawContent.value = await downloadRawFile(props.assetId, datasetTemp?.fileNames?.[0] ?? '');
			if (datasetTemp) {
				Object.entries(datasetTemp).forEach(([key, value]) => {
					if (isString(value)) {
						datasetTemp[key] = highlightSearchTerms(value);
					}
				});
				dataset.value = datasetTemp;
			}
		} else {
			dataset.value = null;
			rawContent.value = null;
		}
	},
	{ immediate: true }
);

const annotations = computed(() => dataset.value?.columns?.map((column) => column.annotations));
const showAccordion = computed(() => {
	if (metadata.value) {
		return [0, 1, 2];
	}
	if (dataset.value?.columns) {
		return dataset.value?.columns?.map((column) => column?.annotations ?? 0)?.length > 0
			? [1]
			: [0];
	}

	return [0];
});
</script>

<style scoped>
.container {
	margin-left: 1rem;
	margin-right: 1rem;
	max-width: 70rem;
}
.inline-message:deep(.p-message-wrapper) {
	padding-top: 0.5rem;
	padding-bottom: 0.5rem;
	background-color: var(--surface-highlight);
	color: var(--text-color-primary);
	border-radius: var(--border-radius);
	border: 4px solid var(--primary-color);
	border-width: 0px 0px 0px 6px;
}
.p-buttonset {
	white-space: nowrap;
	margin-left: 0.5rem;
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

.data-transform-container {
	display: flex;
	flex-direction: column;
	padding: 0.5rem;
	margin: 0.5rem;
	max-height: 90%;
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
	grid-template-columns: repeat(6, 1fr);
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
}

.variables-row {
	display: grid;
	grid-template-columns: repeat(6, 1fr);
	grid-template-rows: 1fr 1fr;
	border-top: 1px solid var(--surface-border);
}

.variables-row:hover {
	background-color: var(--surface-highlight);
}

.variables-description {
	grid-row: 2;
	grid-column: 1 / span 6;
	color: var(--text-color-subdued);
}
</style>
