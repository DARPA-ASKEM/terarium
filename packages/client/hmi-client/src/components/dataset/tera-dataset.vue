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
				:publications="[enriched ? dataset.metadata.documents[0].title ?? '' : '']"
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
							v-for="(column, index) in dataset.columns"
							class="variables-row"
							:key="index"
							@click="rowEditList[index] = true"
							:active="rowEditList[index]"
						>
							<!-- id -->
							<InputText
								class="p-inputtext-sm"
								type="text"
								v-if="rowEditList[index]"
								v-model="column.name"
								@focus="setSuggestedValue(index, column.name)"
							/>
							<div v-else>{{ column.name }}</div>
							<!-- name - currently just a formatted id -->
							<InputText
								class="p-inputtext-sm"
								type="text"
								v-if="rowEditList[index]"
								v-model="column.name"
								@focus="setSuggestedValue(index, column.name)"
							/>
							<div v-else>{{ formatName(column.name) }}</div>
							<!-- data type -->
							<InputText
								class="p-inputtext-sm"
								type="text"
								v-if="rowEditList[index]"
								v-model="column.dataType"
								@focus="setSuggestedValue(index, column.dataType)"
							/>
							<div v-else>{{ column.dataType }}</div>
							<!-- units - field does not exist in tds yet -->
							<InputText
								class="p-inputtext-sm"
								type="text"
								v-if="rowEditList[index]"
								@focus="setSuggestedValue(index, '')"
							/>
							<div v-else>-</div>
							<!-- grounding -->
							<InputText
								class="p-inputtext-sm"
								type="text"
								v-if="rowEditList[index]"
								v-model="groundingValues[index][0]"
								@focus="setSuggestedValue(index, groundingValues[index][0])"
							/>
							<div v-else>
								{{ column.grounding?.identifiers[Object.keys(column.grounding.identifiers)[0]] }}
							</div>
							<!-- extractions - field does not exist in tds yet -->
							<InputText
								class="p-inputtext-sm"
								type="text"
								v-if="rowEditList[index]"
								@focus="setSuggestedValue(index, '')"
							/>
							<div v-else></div>
							<div v-if="rowEditList[index]" class="row-edit-buttons">
								<Button text icon="pi pi-times" @click.stop="rowEditList[index] = false" />
								<Button text icon="pi pi-check" @click.stop="rowEditList[index] = false" />
							</div>
							<!-- description -->
							<InputText
								class="p-inputtext-sm variables-description"
								type="text"
								v-if="rowEditList[index]"
								v-model="column.description"
								@focus="setSuggestedValue(index, column.description)"
							/>
							<div class="variables-description" v-else>{{ column.description }}</div>
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
		<Accordion v-else-if="DatasetView.DATA" :activeIndex="0">
			<AccordionTab>
				<template #header>
					Data preview<span class="artifact-amount">({{ csvContent?.length }} rows)</span>
				</template>
				<tera-dataset-datatable :raw-content="rawContent" />
			</AccordionTab>
		</Accordion>
	</tera-asset>
</template>
<script setup lang="ts">
import { downloadRawFile, getDataset } from '@/services/dataset';
import { computed, ref, watch, onUpdated, Ref } from 'vue';
import Accordion from 'primevue/accordion';
import Button from 'primevue/button';
import AccordionTab from 'primevue/accordiontab';
import Message from 'primevue/message';
import * as textUtil from '@/utils/text';
import { isString } from 'lodash';
import { CsvAsset, Dataset } from '@/types/Types';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import TeraAsset from '@/components/asset/tera-asset.vue';
import InputText from 'primevue/inputtext';
import RelatedPublications from '../widgets/tera-related-publications.vue';

enum DatasetView {
	DESCRIPTION = 'description',
	DATA = 'data'
}

const props = defineProps<{
	assetId: string;
	isEditable: boolean;
	highlight?: string;
}>();

const emit = defineEmits(['close-preview', 'asset-loaded']);

// Highlight strings based on props.highlight
function highlightSearchTerms(text: string | undefined): string {
	if (!!props.highlight && !!text) {
		return textUtil.highlight(text, props.highlight);
	}
	return text ?? '';
}

const dataset = ref<Dataset | null>(null);
const rawContent: Ref<CsvAsset | null> = ref(null);
const datasetView = ref(DatasetView.DESCRIPTION);

const csvContent = computed(() => rawContent.value?.csv);

function formatName(name: string) {
	return (name.charAt(0).toUpperCase() + name.slice(1)).replace('_', ' ');
}

// temporary variable to allow user to click through related publications modal and simulate "getting" enriched data back
const enriched = ref(false);

const groundingValues = ref<string[][]>([]);
const suggestedValues = ref<string[]>([]);

// quick and dirty function to populate one suggestd value per column, based on what column field user clicked; possible refactor
function setSuggestedValue(index: number, suggestedValue: string | undefined) {
	if (suggestedValues.value.length > index && suggestedValue) {
		suggestedValues.value[index] = suggestedValue;
	}
}

const rowEditList = ref<boolean[]>([]);

onUpdated(() => {
	if (dataset.value) {
		emit('asset-loaded');
		if (dataset.value.columns) {
			rowEditList.value = dataset.value.columns.map(() => false);
			groundingValues.value = dataset.value.columns.map((column) => {
				const grounding = column.grounding;
				if (grounding) {
					const keys = Object.keys(grounding.identifiers);
					return keys.map((k) => grounding.identifiers[k]);
				}
				return [];
			});
			suggestedValues.value = dataset.value.columns.map(() => '');
		}
	}
});

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
				console.log(dataset.value);
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

.layout-topbar {
	top: 20px;
	background-color: red;
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
</style>
