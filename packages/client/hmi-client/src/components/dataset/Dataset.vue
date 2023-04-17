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
				<!-- column summary charts go here -->
				<table class="summary-chart-table">
					<thead>
						<tr>
							<th
								v-for="(colName, index) of csvHeaders"
								class="summary-chart-column"
								:key="index"
								:field="index.toString()"
								:header="colName"
							>
								<div class="histogram-label-max">max</div>
								<Chart
									type="bar"
									:height="800"
									:data="chartData.at(index)"
									:options="chartOptions"
									class="histogram"
								/>
								<div class="histogram-label-min">min</div>
								<div class="longest-value">
									{{ colName }}
								</div>
							</th>
						</tr>
					</thead>
				</table>
				<DataTable
					tableStyle="width:auto"
					class="p-datatable-sm"
					:value="csvContent.slice(1, csvContent.length)"
					removableSort
					resizable-columns
					showGridlines
				>
					<Column
						v-for="(colName, index) of csvHeaders"
						:key="index"
						:field="index.toString()"
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

const CATEGORYPERCENTAGE = 0.9;
const BARPERCENTAGE = 1.0;
const MINBARLENGTH = 1;

const dataset = ref<Dataset | null>(null);
const rawContent = ref<any>(null);

const csvContent = computed(() => rawContent.value?.csv);
const csvHeaders = computed(() => rawContent.value?.headers);
// const chartData = ref<any[]>();
const chartData = computed(() => rawContent.value?.bins.map((bin) => setBarChartData(bin)));
const chartOptions = computed(() => setChartOptions());

// Whenever assetId changes, fetch dataset with that ID
watch(
	() => [props.assetId],
	async () => {
		if (props.assetId !== '') {
			rawContent.value = await downloadRawFile(props.assetId, 10);
			console.log(rawContent.value);
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

// Given the bins for a column set up the object needed for the chart.
// TOM TODO: What is stepsize = 0?
const setBarChartData = (bins: any[]) => {
	const documentStyle = getComputedStyle(document.documentElement);
	const dummyLabels: string[] = [];
	for (let i = 0; i < bins.length; i++) {
		dummyLabels.push(i.toString());
	}
	return {
		labels: dummyLabels,
		datasets: [
			{
				label: false,
				backgroundColor: documentStyle.getPropertyValue('--primary-color'),
				borderColor: documentStyle.getPropertyValue('--primary-color'),
				data: bins,
				categoryPercentage: CATEGORYPERCENTAGE,
				barPercentage: BARPERCENTAGE,
				minBarLength: MINBARLENGTH
			}
		]
	};
};

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
/* Histograms & Charts  */
.summary-chart-table {
	width: max-content;
	border-spacing: 0;
	border-collapse: collapse;
	position: sticky;
	top: -1rem;
	z-index: 1;
	background: var(--surface-0);
	border-bottom: 1px solid var(--surface-border-light);
}
.summary-chart-column {
	border-left: 1.111px solid transparent;
	border-right: 1.111px solid transparent;
	text-align: left;
	padding-bottom: 1rem;
}
.histogram {
	width: 40px;
}
.histogram-label-max {
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
	padding-left: 0.5rem;
}
.histogram-label-other {
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
	padding-left: 0.5rem;
	text-align: left;
}
.histogram-label-min {
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
	padding-left: 0.5rem;
	position: relative;
	top: -9px;
}
.longest-value {
	padding-left: 7px;
	padding-right: 10px;
	color: transparent;
	user-select: none;
	height: 0px;
}
.longest-value-fallback {
	padding-left: 7px;
	padding-right: 30px;
	text-transform: uppercase;
	font-size: var(--font-caption);
	color: transparent;
	user-select: none;
	height: 0px;
}
/* Datatable  */
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
