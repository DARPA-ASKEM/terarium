<script setup lang="ts">
import { downloadRawFile, getDataset } from '@/services/dataset';
import { Dataset } from '@/types/Dataset';
import { csvToRecords, getColumns, Record } from '@/utils/csv';
import { computed, ref, watch } from 'vue';

const props = defineProps<{
	datasetId: string;
}>();

const dataset = ref<Dataset | null>(null);
const rawContent = ref<string | null>(null);

// Whenever datasetId changes, fetch dataset with that ID
watch(
	() => [props.datasetId],
	async () => {
		dataset.value = await getDataset(props.datasetId);
		rawContent.value = await downloadRawFile(props.datasetId);
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
	if (!features || features.length === 0) return [];
	return features.map((f) => (f.display_name !== '' ? f.display_name : f.name));
};
</script>

<template>
	<section class="dataset">
		<h3>{{ dataset?.name ?? '' }}</h3>
		<div><b>Description:</b> {{ dataset?.description ?? '' }}</div>
		<div><b>Maintainer:</b> {{ dataset?.maintainer ?? '' }}</div>
		<div><b>Quality:</b> {{ dataset?.quality ?? '' }}</div>
		<div><b>URL:</b> {{ dataset?.url ?? '' }}</div>
		<div><b>Geospatial Resolution:</b> {{ dataset?.geospatialResolution ?? '' }}</div>
		<div><b>Temporal Resolution:</b> {{ dataset?.temporalResolution ?? '' }}</div>
		<ul v-if="dataset !== null">
			Geo Annotations:
			<li v-for="annotation in dataset.annotations.annotations.geo" :key="annotation.name">
				<strong>{{ annotation.name }}</strong
				>: <strong>Description: </strong> {{ annotation.description }}
				<strong>GADM Level: </strong> {{ annotation.gadm_level }}
			</li>
		</ul>
		<ul v-if="dataset !== null">
			Temporal Annotations:
			<li v-for="annotation in dataset.annotations.annotations.date" :key="annotation.name">
				<strong>{{ annotation.name }}</strong
				>: <strong>Description: </strong> {{ annotation.description }}
				<strong>Time Format: </strong> {{ annotation.time_format }}
			</li>
		</ul>
		<h4>Features</h4>
		<ul v-if="dataset !== null">
			<li v-for="feature in formatFeatures(dataset)" :key="feature">
				{{ feature }}
			</li>
		</ul>
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

<style scoped>
.dataset {
	margin: 10px;
	display: flex;
	flex-direction: column;
	gap: 1rem;
	padding: 1rem;
	background: var(--un-color-body-surface-primary);
}

h3 {
	margin-bottom: 10px;
}

h4 {
	margin-top: 30px;
}

.description {
	max-height: 400px;
	overflow-y: auto;
}

li {
	display: block;
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
	height: 100%;
	width: 100%;
}
</style>
