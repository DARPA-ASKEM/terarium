<script setup lang="ts">
import { downloadRawFile, getDataset } from '@/services/dataset';
import { Dataset } from '@/types/Dataset';
import { csvToRecords, getColumns, Record } from '@/utils/csv';
import { computed, ref, watch } from 'vue';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';

const props = defineProps<{
	assetId: string;
}>();

const dataset = ref<Dataset | null>(null);
const rawContent = ref<string | null>(null);

// Whenever assetId changes, fetch dataset with that ID
watch(
	() => [props.assetId],
	async () => {
		if (props.assetId !== '') {
			dataset.value = await getDataset(props.assetId);
			rawContent.value = await downloadRawFile(props.assetId);
		}
	},
	{ immediate: true }
);

const csvContent = computed(() => {
	if (rawContent.value !== null) {
		const records = csvToRecords(rawContent.value);
		return records;
	}
	return [] as Record[];
});

const rawColumnNames = computed(() => {
	if (csvContent.value !== null) {
		return getColumns(csvContent.value);
	}
	return [] as string[];
});

const formatFeatures = (d: Dataset) => {
	const features = d.annotations.annotations.feature ?? [];
	return features;
};
</script>

<template>
	<section class="dataset">
		<template v-if="dataset !== null">
			<h4 class="title">{{ dataset?.name ?? '' }}</h4>
			<div><b>Description:</b> {{ dataset?.description ?? '' }}</div>
			<div><b>Maintainer:</b> {{ dataset?.maintainer ?? '' }}</div>
			<div><b>Quality:</b> {{ dataset?.quality ?? '' }}</div>
			<div><b>URL:</b> {{ dataset?.url ?? '' }}</div>
			<div><b>Geospatial Resolution:</b> {{ dataset?.geospatialResolution ?? '' }}</div>
			<div><b>Temporal Resolution:</b> {{ dataset?.temporalResolution ?? '' }}</div>

			<Accordion :multiple="true" class="accordian">
				<AccordionTab header="Annotations">
					<div>
						Geo Annotations:
						<div v-for="annotation in dataset.annotations.annotations.geo" :key="annotation.name">
							<strong>{{ annotation.name }}</strong
							>: <strong>Description: </strong> {{ annotation.description }}
							<strong>GADM Level: </strong> {{ annotation.gadm_level }}
						</div>
					</div>
					<div>
						Temporal Annotations:
						<div v-for="annotation in dataset.annotations.annotations.date" :key="annotation.name">
							<strong>{{ annotation.name }}</strong
							>: <strong>Description: </strong> {{ annotation.description }}
							<strong>Time Format: </strong> {{ annotation.time_format }}
						</div>
					</div>
				</AccordionTab>
				<AccordionTab header="Concepts"> </AccordionTab>
				<AccordionTab header="Features">
					<div v-for="feature of formatFeatures(dataset)" :key="feature.name">
						<div>Name: {{ feature.display_name || feature.name }}</div>
						<div>Type: {{ feature.feature_type }}</div>
					</div>
				</AccordionTab>
				<AccordionTab header="Associated Objects"> </AccordionTab>
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
		</template>
	</section>
</template>

<style scoped>
.dataset {
	margin: 10px;
	display: flex;
	flex-direction: column;
	height: calc(100vh - 50px);
	gap: 1rem;
	padding: 1rem;
	overflow: auto;
	background: var(--surface-section);
}

.description {
	max-height: 400px;
	overflow-y: auto;
}

strong {
	font-weight: bold;
}

table {
	border-collapse: collapse;
	width: 100%;
	vertical-align: top;
}

td {
	padding: 8px 16px;
	background: var(--background-light-1);
	vertical-align: top;
}

tbody tr {
	border-top: 2px solid var(--separator);
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
