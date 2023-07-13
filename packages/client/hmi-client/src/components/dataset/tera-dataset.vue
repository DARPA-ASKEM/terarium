<template>
	<tera-asset
		v-if="dataset"
		:name="dataset?.name"
		:is-editable="isEditable"
		:stretch-content="datasetView === DatasetView.DATA"
		@close-preview="emit('close-preview')"
		ref="assetPanel"
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
			<section class="metadata data-row">
				<section>
					<header>Rows</header>
					<section>{{ csvContent?.length || '-' }}</section>
				</section>
				<section>
					<header>Columns</header>
					<section>{{ rawContent?.stats?.length || '-' }}</section>
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
			<section class="metadata data-row">
				<section>
					<header>Source Name</header>
					<section v-if="dataset.url === 'https://github.com/reichlab/covid19-forecast-hub/'">
						The Reich Lab at UMass-Amherst
					</section>
					<section v-else>{{ dataset?.source || '-' }}</section>
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
				@extracted-metadata="enriched = true"
				:publications="
					enriched && dataset.metadata.documents.length > 0
						? dataset.metadata.documents[0].title
						: ''
				"
			/>
			<Accordion :multiple="true" :activeIndex="[0, 1, 2]">
				<AccordionTab>
					<template #header>
						<header id="Description">Description</header>
					</template>
					<section v-if="enriched">
						<ul>
							<li>Dataset name: {{ dataset.name }}</li>
							<li>Dataset overview: {{ dataset.description }}</li>
							<li>Dataset URL: {{ dataset.source }}</li>
							<li>
								Data size: This dataset currently contains {{ csvContent?.length || '-' }} rows.
							</li>
						</ul>
					</section>
					<p v-else>
						No information available. Add resources to generate a description. Or click edit icon to
						edit this field directly.
					</p>
				</AccordionTab>
				<AccordionTab v-if="enriched">
					<template #header>
						<header id="Source">Source</header>
					</template>
					This data is sourced from {{ dataset.metadata.documents[0].title }}:
					<a :href="dataset.metadata.documents[0].url">{{ dataset.metadata.documents[0].url }}</a>
				</AccordionTab>
				<AccordionTab>
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
									'CONCEPTS',
									'EXTRACTIONS'
								]"
								:key="index"
							>
								{{ title }}
							</div>
						</div>
						<div
							v-for="(column, index) in editableRows"
							class="variables-row"
							:key="index"
							@click="
								() => {
									rowEditList[index] = true;
									setUnsavedRowValues();
								}
							"
							:active="rowEditList[index]"
						>
							<!-- id -->
							<InputText
								class="p-inputtext-sm"
								type="text"
								v-if="rowEditList[index]"
								v-model="column.name"
								@focus="setSuggestedValue(index, dataset.columns?.[index].name)"
							/>
							<div class="variables-value" v-else>{{ column.name }}</div>
							<!-- name - currently just a formatted id -->
							<InputText
								class="p-inputtext-sm"
								type="text"
								v-if="rowEditList[index]"
								v-model="column.name"
								@focus="setSuggestedValue(index, dataset.columns?.[index].name)"
							/>
							<div class="variables-value" v-else>{{ formatName(column.name) }}</div>
							<!-- data type -->
							<InputText
								class="p-inputtext-sm"
								type="text"
								v-if="rowEditList[index]"
								v-model="column.dataType"
								@focus="setSuggestedValue(index, dataset.columns?.[index].dataType)"
							/>
							<div class="variables-value" v-else>{{ column.dataType }}</div>
							<!-- units - field does not exist in tds yet -->
							<InputText
								class="p-inputtext-sm"
								type="text"
								v-if="rowEditList[index]"
								@focus="setSuggestedValue(index, '')"
							/>
							<div class="variables-value" v-else>-</div>
							<!-- grounding -->
							<InputText
								class="p-inputtext-sm"
								type="text"
								v-if="rowEditList[index]"
								v-model="groundingValues[index][0]"
								@focus="setSuggestedValue(index, originalGroundingValues[0])"
							/>
							<div class="variables-value" v-else>
								{{ groundingValues[index][0] }}
							</div>
							<!-- extractions - field does not exist in tds yet -->
							<InputText
								class="p-inputtext-sm"
								type="text"
								v-if="rowEditList[index]"
								@focus="setSuggestedValue(index, '')"
							/>
							<div class="variables-value" v-else></div>
							<div v-if="rowEditList[index]" class="row-edit-buttons">
								<Button
									text
									icon="pi pi-times"
									@click.stop="
										() => {
											rowEditList[index] = false;
											cancelRowEdits(index);
										}
									"
								/>
								<Button text icon="pi pi-check" @click.stop="rowEditList[index] = false" />
							</div>
							<!-- description -->
							<InputText
								class="p-inputtext-sm variables-description"
								type="text"
								v-if="rowEditList[index]"
								v-model="column.description"
								@focus="setSuggestedValue(index, dataset.columns?.[index].description)"
							/>
							<div class="variables-description variables-value" v-else>
								{{ column.description }}
							</div>
							<!-- suggested values -->
							<div v-if="rowEditList[index]" class="variables-suggested">
								<span>Suggested value</span>
								<div>
									<div class="suggested-value-source">
										<i class="pi pi-file" />{{ dataset.metadata.documents[0].title }}
									</div>
									<div class="suggested-value">{{ suggestedValues[index] }}</div>
								</div>
								<span>Other possible values</span>
								<div>
									<div class="suggested-value-source">
										<i class="pi pi-file" />{{ dataset.metadata.documents[0].title }}
									</div>
									<div class="suggested-value">
										<ul>
											<li v-for="(grounding, g) in groundingValues[index].slice(1, 5)" :key="g">
												{{ grounding }}
											</li>
										</ul>
									</div>
								</div>
							</div>
						</div>
					</div>
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
			<Suspense>
				<tera-dataset-jupyter-panel
					:asset-id="props.assetId"
					:project="props.project"
					:dataset="dataset"
					:show-kernels="showKernels"
					:show-chat-thoughts="showChatThoughts"
					@is-typing="updateScroll"
				/>
			</Suspense>
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
import { CsvAsset, Dataset, DatasetColumn } from '@/types/Types';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import TeraDatasetJupyterPanel from '@/components/dataset/tera-dataset-jupyter-panel.vue';
import TeraAsset from '@/components/asset/tera-asset.vue';
import { IProject } from '@/types/Project';
import Menu from 'primevue/menu';
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
	project?: IProject;
}>();

const emit = defineEmits(['close-preview', 'asset-loaded']);
const showKernels = ref(<boolean>false);
const showChatThoughts = ref(<boolean>false);
const menu = ref();
const newCsvContent: any = ref(null);
const newCsvHeader: any = ref(null);
const oldCsvHeaders: any = ref(null);
const dataset: Ref<Dataset | null> = ref(null);
const rawContent: Ref<CsvAsset | null> = ref(null);
const jupyterCsv: Ref<CsvAsset | null> = ref(null);

const assetPanel = ref({ assetContainer: HTMLElement });

const updateScroll = () => {
	const el = assetPanel.value.assetContainer;
	if (el) {
		// @ts-ignore
		el.scrollTop = el.scrollHeight;
	}
};

const toggleSettingsMenu = (event: Event) => {
	menu.value.toggle(event);
};

function formatName(name: string) {
	return (name.charAt(0).toUpperCase() + name.slice(1)).replace('_', ' ');
}

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

// temporary variable to allow user to click through related publications modal and simulate "getting" enriched data back
const enriched = ref(false);

const groundingValues = ref<string[][]>([]);
// groundingValuesUnsaved is set to the value of the grounding value being edited, and is the value that is reverted back to if the user does not save their changes
const groundingValuesUnsaved = ref<string[][]>([]);
// originaGroundingValues are displayed as the first suggested value for concepts
const originalGroundingValues = ref<string[]>([]);
const suggestedValues = ref<string[]>([]);

function setUnsavedRowValues() {
	editableRowsUnsaved.value = editableRows.value.map((row) => ({ ...row }));
	groundingValuesUnsaved.value = groundingValues.value.map((g) => [...g]);
}

// quick and dirty function to populate one suggested value per column, based on what column field user clicked; possible refactor
function setSuggestedValue(index: number, suggestedValue: string | undefined) {
	if (suggestedValues.value.length > index && suggestedValue) {
		suggestedValues.value[index] = suggestedValue;
	}
}

const rowEditList = ref<boolean[]>([]);
// editableRows is are the dataset columns that can be edited by the user; transient data
const editableRows = ref<DatasetColumn[]>([]);
// editableRowsUnsaved is set to the value of the row being edited, and is the value that is reverted back to if the user does not save their changes
const editableRowsUnsaved = ref<DatasetColumn[]>([]);

function cancelRowEdits(index: number) {
	if (dataset.value?.columns) {
		editableRows.value[index] = { ...editableRowsUnsaved.value[index] };
		groundingValues.value[index] = [...groundingValuesUnsaved.value[index]];
	}
}
const openDatesetChatTab = () => {
	datasetView.value = DatasetView.LLM;
	jupyterCsv.value = null;
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

li::after {
	content: ' | ';
}

li:last-child::after {
	content: '';
}

main :deep(.p-inputtext.p-inputtext-sm) {
	padding-left: 0.65rem;
}

.row-edit-buttons {
	display: flex;
	justify-content: space-evenly;
}
</style>
