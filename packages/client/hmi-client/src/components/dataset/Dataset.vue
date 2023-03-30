<template>
	<section class="asset">
		<header>
			<div class="simulation" v-if="dataset?.simulation_run">Simulation run</div>
			<h4 v-html="dataset?.name" />
		</header>

		<div class="description">
			<span v-html="dataset?.description" />
		</div>

		<div class="metadata">
			<div class="key-value-pair">
				<div class="key">Maintainer</div>
				<div class="value" v-html="dataset?.maintainer || '-'" />
			</div>
			<div class="key-value-pair">
				<div class="key">Quality</div>
				<div class="value" v-html="dataset?.quality || '-'" />
			</div>
			<div class="key-value-pair">
				<div class="key">URL</div>
				<div class="value" v-html="dataset?.url || '-'" />
			</div>
			<div class="key-value-pair">
				<div class="key">Geospatial resolution</div>
				<div class="value" v-html="dataset?.geospatialResolution || '-'" />
			</div>
			<div class="key-value-pair">
				<div class="key">Temporal resolution</div>
				<div class="value" v-html="dataset?.temporalResolution || '-'" />
			</div>
			<div class="key-value-pair">
				<div class="key">Number of records</div>
				<div class="value" v-html="csvContent.length" />
			</div>
		</div>

		<Accordion :multiple="true" :activeIndex="[2]">
			<AccordionTab v-if="annotations">
				<template #header>
					Annotations<span class="artifact-amount">({{ annotations.feature.length }})</span>
				</template>
				Geospatial annotations:
				<div v-for="annotation in annotations?.geo" :key="annotation.name">
					<strong v-html="annotation.name" />: <strong>Description:</strong>
					<span v-html="annotation.description" /> <strong>GADM Level: </strong>
					<span v-html="annotation.gadm_level" />
				</div>
				<br />Temporal annotations:
				<div v-for="annotation in annotations.date" :key="annotation.name">
					<strong v-html="annotation.name" />: <strong>Description:</strong>
					<span v-html="annotation.description" /> <strong>Time Format:</strong>
					<span v-html="annotation.time_format" />
				</div>
			</AccordionTab>
			<!-- <AccordionTab header="Concepts"></AccordionTab> -->
			<AccordionTab v-if="annotations">
				<template #header>
					Features<span class="artifact-amount"
						>({{ annotations.geo.length + annotations.feature.length }})</span
					>
				</template>
				<ol class="numbered-list">
					<li v-for="(feature, index) of annotations.feature" :key="index">
						<span v-html="feature.display_name || feature.name" /> :
						<span v-html="feature.feature_type" class="feature-type" />
					</li>
				</ol>
			</AccordionTab>
			<!-- <AccordionTab header="Associated Objects"></AccordionTab> -->
			<AccordionTab>
				<template #header>
					Data preview<span class="artifact-amount">({{ csvContent.length }} rows)</span>
				</template>
				<DataTable
					tableStyle="min-width: 50rem"
					class="p-datatable-sm"
					:value="csvContent"
					removableSort
					showGridlines
				>
					<Column
						v-for="colName of rawColumnNames"
						:key="colName"
						:field="colName"
						:header="colName"
						sortable
					></Column>
				</DataTable>
			</AccordionTab>
		</Accordion>
	</section>
</template>

<script setup lang="ts">
import { downloadRawFile, getDataset } from '@/services/dataset';
import { Dataset } from '@/types/Dataset';
import { csvToRecords, getColumns, Record } from '@/utils/csv';
import { computed, ref, watch } from 'vue';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import * as textUtil from '@/utils/text';
import { isString } from 'lodash';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';

const props = defineProps<{
	assetId: string;
	isEditable: boolean;
	highlight?: string;
}>();

// Highlight strings based on props.highlight
function highlightSearchTerms(text: string | undefined): string {
	if (!!props.highlight && !!text) {
		return textUtil.highlight(text, props.highlight);
	}
	return text ?? '';
}

const dataset = ref<Dataset | null>(null);
const rawContent = ref<string | null>(null);

const csvContent = computed(() =>
	rawContent.value ? csvToRecords(rawContent.value) : ([] as Record[])
);
const rawColumnNames = computed(() =>
	csvContent.value ? getColumns(csvContent.value) : ([] as string[])
);

// Whenever assetId changes, fetch dataset with that ID
watch(
	() => [props.assetId],
	async () => {
		if (props.assetId !== '') {
			rawContent.value = await downloadRawFile(props.assetId);
			const datasetTemp = await getDataset(props.assetId);
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

const annotations = computed(() => dataset.value?.annotations.annotations);
</script>

<style scoped>
.metadata {
	margin: 1rem;
	margin-bottom: 0.5rem;
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	background-color: var(--gray-50);
	padding: 1rem;
	display: flex;
	flex-direction: row;
	justify-content: space-between;
}

.numbered-list {
	list-style: numbered-list;
	margin-left: 1rem;
}

.feature-type {
	color: var(--text-color-subdued);
}
ol.numbered-list li::marker {
	color: var(--text-color-subdued);
}

.description {
	padding: 1rem;
}
.key-value-pair .key {
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
}
.key-value-pair .value {
	font-size: var(--font-body-small);
}

.p-sortable-column-icon {
	color: red !important;
}
</style>
