<template>
	<section class="asset">
		<header>
			<h4 v-html="dataset?.name" />
		</header>
		<Accordion :multiple="true" class="accordian">
			<AccordionTab v-if="annotations" header="Description"
				><span v-html="dataset?.description"
			/></AccordionTab>
			<AccordionTab v-if="annotations" header="Maintainer"
				><span v-html="dataset?.maintainer"
			/></AccordionTab>
			<AccordionTab v-if="annotations" header="Quality"
				><span v-html="dataset?.quality"
			/></AccordionTab>
			<AccordionTab v-if="annotations" header="URL"><span v-html="dataset?.url" /></AccordionTab>
			<AccordionTab v-if="annotations" header="Geospatial Resolution"
				><span v-html="dataset?.geospatialResolution"
			/></AccordionTab>
			<AccordionTab v-if="annotations" header="Temporal Resolution"
				><span v-html="dataset?.temporalResolution" />
			</AccordionTab>
			<AccordionTab v-if="annotations" :header="`Annotations (${annotations.feature.length})`">
				<div>
					Geo Annotations:
					<div v-for="annotation in annotations?.geo" :key="annotation.name">
						<strong v-html="annotation.name" />: <strong>Description:</strong>
						<span v-html="annotation.description" /> <strong>GADM Level: </strong>
						<span v-html="annotation.gadm_level" />
					</div>
				</div>
				<div>
					Temporal Annotations:
					<div v-for="annotation in annotations.date" :key="annotation.name">
						<strong v-html="annotation.name" />: <strong>Description:</strong>
						<span v-html="annotation.description" /> <strong>Time Format:</strong>
						<span v-html="annotation.time_format" />
					</div>
				</div>
			</AccordionTab>
			<!-- <AccordionTab header="Concepts"></AccordionTab> -->
			<AccordionTab
				v-if="annotations"
				:header="`Features (${annotations.geo.length + annotations.date.length})`"
			>
				<div v-for="(feature, index) of annotations.feature" :key="index">
					<div>Name: <span v-html="feature.display_name || feature.name" /></div>
					<div>Type: <span v-html="feature.feature_type" /></div>
				</div>
			</AccordionTab>
			<!-- <AccordionTab header="Associated Objects"></AccordionTab> -->
		</Accordion>

		<!-- table preview of the data -->
		Dataset Records: {{ csvContent.length }}
		<div class="table-fixed-head">
			<table>
				<thead>
					<tr class="tr-item">
						<th v-for="colName in rawColumnNames" :key="colName">{{ colName }}</th>
					</tr>
				</thead>
				<tbody>
					<tr v-for="(row, rowIndex) in csvContent" :key="rowIndex.toString()" class="tr-item">
						<td
							v-for="(_, colIndex) in row"
							:key="colIndex.toString()"
							class="title-and-abstract-col"
						>
							<div class="title-and-abstract-layout">
								<div class="content">
									<div class="text-bold">{{ csvContent[rowIndex][colIndex] }}</div>
								</div>
							</div>
						</td>
					</tr>
				</tbody>
			</table>
		</div>
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
table {
	border-collapse: collapse;
	width: 100%;
	vertical-align: top;
}

td {
	padding: 8px 16px;
	background: var(--gray-0);
	vertical-align: top;
}

tbody tr {
	border-top: 2px solid var(--gray-300);
	cursor: pointer;
}

tbody tr:first-child {
	border-top-width: 0;
}

.table-fixed-head thead th {
	position: sticky;
	top: -1px;
	z-index: 1;
	background-color: aliceblue;
}

.tr-item {
	height: 50px;
}

.table-fixed-head {
	overflow-y: auto;
	overflow-x: auto;
	min-height: 200px;
	width: 100%;
}
</style>
