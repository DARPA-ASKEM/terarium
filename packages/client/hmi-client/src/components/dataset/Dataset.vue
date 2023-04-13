<template>
	<section class="asset">
		<header>
			<div class="simulation" v-if="dataset?.simulation_run">Simulation run</div>
			<h4 v-html="dataset?.name" />
		</header>

		<section class="description">
			<span>{{ dataset?.description }}</span>
		</section>

		<section class="metadata data-row">
			<section>
				<header>Maintainer</header>
				<section>{{ dataset?.maintainer || '-' }}</section>
			</section>
			<section>
				<header>Quality</header>
				<section>{{ dataset?.quality || '-' }}</section>
			</section>
			<section>
				<header>URL</header>
				<section>
					<a :href="dataset?.url">{{ dataset?.url || '-' }}</a>
				</section>
			</section>
			<section>
				<header>Geospatial resolution</header>
				<section>{{ dataset?.geospatialResolution || '-' }}</section>
			</section>
			<section>
				<header>Temporal resolution</header>
				<section>{{ dataset?.temporalResolution || '-' }}</section>
			</section>
			<section>
				<header>Number of records</header>
				<section>{{ csvContent?.length }}</section>
			</section>
		</section>

		<Accordion :multiple="true" :activeIndex="showAccordion">
			<AccordionTab v-if="(annotations?.geo?.length || 0) + (annotations?.date?.length || 0) > 0">
				<template #header>
					Annotations<span class="artifact-amount"
						>({{ (annotations?.geo?.length || 0) + (annotations?.date?.length || 0) }})</span
					>
				</template>
				<section v-if="annotations?.geo">
					<header class="annotation-subheader">Geospatial annotations</header>
					<section class="annotation-group">
						<section
							v-for="annotation in annotations?.geo"
							:key="annotation.name"
							class="annotation-row data-row"
						>
							<section>
								<header>Name</header>
								<section class="value">{{ annotation.name }}</section>
							</section>
							<section>
								<header>Description</header>
								<section>{{ annotation.description }}</section>
							</section>
							<section>
								<header>GADM level</header>
								<section>{{ annotation.gadm_level }}</section>
							</section>
						</section>
					</section>
				</section>
				<section v-if="annotations?.date">
					<header class="annotation-subheader">Temporal annotations</header>
					<section class="annotation-group">
						<section
							v-for="annotation in annotations?.date"
							:key="annotation.name"
							class="annotation-row data-row"
						>
							<section>
								<header>Name</header>
								<section>{{ annotation.name }}</section>
							</section>
							<section>
								<header>Description</header>
								<section>{{ annotation.description }}</section>
							</section>
							<section>
								<header>Time format</header>
								<section>{{ annotation.time_format }}</section>
							</section>
						</section>
					</section>
				</section>
			</AccordionTab>
			<AccordionTab v-if="(annotations?.feature?.length || 0) > 0">
				<template #header>
					Features<span class="artifact-amount">({{ annotations?.feature?.length }})</span>
				</template>
				<ol class="numbered-list">
					<li v-for="(feature, index) of annotations?.feature" :key="index">
						<span>{{ feature.display_name || feature.name }}</span
						>:
						<span class="feature-type">{{ feature.feature_type }}</span>
					</li>
				</ol>
			</AccordionTab>
			<AccordionTab>
				<template #header>
					Data preview<span class="artifact-amount">({{ csvContent?.length }} rows)</span>
				</template>

				<Chart type="bar" :data="chartData" :options="chartOptions" class="h-30rem" />

				<!-- column summary charts go here -->
				<!-- <table class="summary-chart-table">
					<tr>
						<td
							v-for="colName of rawColumnNames"
							class="summary-chart-column"
							:key="colName"
							:field="colName"
							:header="colName"
						>
							<span class="histogram-label-min">min</span>
							<Chart
								type="bar"
								:height="800"
								:data="chartData"
								:options="chartOptions"
								class="histogram"
							/>
							<span class="histogram-label-max">max</span>
							<div
								v-if=" longestString(makeArrayFromColumn(colName, csvContent)) !== '' &&
										longestString(makeArrayFromColumn(colName, csvContent)).length > colName.length
									"
									class="longest-value"
								>
								{{ longestString(makeArrayFromColumn(colName, csvContent)) }}
							</div>
							<div v-else class="longest-value-fallback">{{ colName }}</div>
						</td>
					</tr>
				</table> -->

				<DataTable
					tableStyle="width:auto"
					class="p-datatable-sm"
					:value="csvContent"
					removableSort
					resizable-columns
					showGridlines
				>
					<Column
						v-for="colName of rawColumnNames"
						:key="colName"
						:field="colName"
						:header="colName"
						sortable
					>
					</Column>
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
import Chart from 'primevue/chart';

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

const CATEGORYPERCENTAGE = 0.98;
const BARPERCENTAGE = 0.98;

const dataset = ref<Dataset | null>(null);
const rawContent = ref<string | null>(null);

const csvContent = computed(() =>
	rawContent.value ? csvToRecords(rawContent.value) : ([] as Record[])
);
const rawColumnNames = computed(() =>
	csvContent.value ? getColumns(csvContent.value) : ([] as string[])
);
const chartData = ref();
const chartOptions = ref();

// Whenever assetId changes, fetch dataset with that ID
watch(
	() => [props.assetId],
	async () => {
		if (props.assetId !== '') {
			rawContent.value = await downloadRawFile(props.assetId);
			const datasetTemp = await getDataset(props.assetId);
			// console.log(rawContent.value);
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
const showAccordion = computed(() =>
	dataset.value?.annotations.annotations.date && dataset.value?.annotations.annotations.geo
		? [2]
		: [0]
);

watch(
	() => [csvContent.value],
	async () => {
		console.log(props.assetId);
		console.log(csvContent.value);
		// TOM TODO: Use the actual names, not hard coded infected
		chartData.value = setBarChartData(
			csvContent.value.map((col) => col.Infected),
			10
		);
		console.log(chartData.value);
		chartOptions.value = setChartOptions();
		console.log(setBarChartData([1, 1, 1, 1, 1, 1], 10));
		// console.log(setBarChartData([1,2,3,4,5,6,7,8,9,10], 10));
		// console.log(setBarChartData([-2,-1,8], 10));
		// console.log(setBarChartData([-1,1,2,3,4,8], 10));
		// console.log(setBarChartData([1,1,2,3,4,100], 10));
		console.log(
			setBarChartData(
				csvContent.value.map((col) => col.Infected),
				10
			)
		);
	}
);

// Given a column in the csv content throw these into X bins for the bar chart
// TOM TODO: What is stepsize = 0?
const setBarChartData = (anArray: any[], binCount: number) => {
	// if (Number(anArray[0]))  console.log(Number(anArray[0]));
	// else console.log("Not a number");

	// Set up array
	const numberArray = anArray.map((ele) => Number(ele));
	numberArray.sort((a, b) => a - b);

	// Set up bins:
	const bins: number[] = new Array(binCount).fill(0);
	const stepSize = (numberArray[numberArray.length - 1] - numberArray[0]) / (binCount - 1);

	// Fill bins:
	numberArray.forEach((number) => {
		const index = Math.abs(Math.floor((number - numberArray[0]) / stepSize));
		bins[index] += 1;
	});

	const dummyLabels: string[] = [];
	for (let i = 0; i < binCount; i++) {
		dummyLabels.push(i.toString());
	}
	const documentStyle = getComputedStyle(document.documentElement);
	return {
		labels: dummyLabels,
		datasets: [
			{
				categoryPercentage: CATEGORYPERCENTAGE,
				barPercentage: BARPERCENTAGE,
				label: 'Filler',
				backgroundColor: documentStyle.getPropertyValue('--blue-500'),
				borderColor: documentStyle.getPropertyValue('--blue-500'),
				data: bins
			}
		]
	};
};

// Get the top 3 most used string and get their usage %
// TODO
// function setChartStringData(anArray: String[]){
// 	return [0];
// }

// const setChartDataPrimevue = () => {
//     const documentStyle = getComputedStyle(document.documentElement);

//     return {
//         labels: ['January', 'February', 'March', 'April', 'May', 'June', 'July'],
//         datasets: [
//             {
// 				categoryPercentage: 1.0,
//            		barPercentage: 1.0,
//                 label: 'My First dataset',
//                 backgroundColor: documentStyle.getPropertyValue('--blue-500'),
//                 borderColor: documentStyle.getPropertyValue('--blue-500'),
//                 data: [43, 2, 1, 1, 1, 0, 1, 0, 0, 1]
//             }
//         ]
//     };
// };
const setChartOptions = () => {
	const documentStyle = getComputedStyle(document.documentElement);
	return {
		indexAxis: 'y',
		plugins: {
			legend: {
				labels: {
					display: false
				},
				display: false
			},
			tooltip: {
				enabled: false
			}
		},
		scales: {
			x: {
				ticks: {
					display: false
				},
				grid: {
					display: false,
					drawBorder: false
				}
			},
			y: {
				ticks: {
					display: false
				},
				grid: {
					display: false,
					drawBorder: false,
					borderColor: documentStyle.getPropertyValue('--surface-border-light')
				}
			}
		}
	};
};
</script>

<style scoped>
.metadata {
	margin: 1rem;
	margin-bottom: 0.5rem;
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	background-color: var(--gray-50);
	padding: 0.25rem;
	display: flex;
	flex-direction: row;
	justify-content: space-evenly;
}

.metadata > section {
	flex: 1;
	padding: 0.5rem;
}

.data-row > section > header {
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
}

.data-row > section > section:last-child {
	font-size: var(--font-body-small);
}

.annotation-row > section {
	flex: 1;
	padding: 0.5rem;
}

.numbered-list {
	list-style: numbered-list;
	margin-left: 2rem;
	list-style-position: outside;
}

ol.numbered-list li::marker {
	color: var(--text-color-subdued);
}

.feature-type {
	color: var(--text-color-subdued);
}

.description {
	padding: 1rem;
	padding-bottom: 0.5rem;
	max-width: var(--constrain-width);
}

.annotation-group {
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

.layout-topbar {
	top: 20px;
	background-color: red;
}
</style>
