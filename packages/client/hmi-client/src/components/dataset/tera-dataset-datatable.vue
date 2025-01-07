<template>
	<div class="datatable-container">
		<!-- Toggle histograms & column summary charts -->
		<div class="datatable-toolbar">
			<span class="datatable-toolbar-item">
				{{ columns.length || 'No' }} columns | {{ rowCount || 'No' }} rows
				{{ rowCount != rawContent.csv.length ? '(' + rawContent.csv.length + ' showing)' : '' }}
			</span>
			<span class="datatable-toolbar-item" style="margin-left: auto">
				Show column summaries<InputSwitch v-model="showSummaries" />
			</span>
			<Button
				class="download-button datatable-toolbar-item"
				label="Download"
				icon="pi pi-download"
				text
				outlined
				disabled
			/>
			<span class="datatable-toolbar-item">
				<MultiSelect
					:model-value="selectedColumns"
					:options="rawContent.headers"
					@update:model-value="selectedColumns = rawContent.headers.filter((col) => $event.includes(col))"
					:maxSelectedLabels="1"
					placeholder="Select columns"
				>
					<template #value>
						<span class="columns-dropdown datatable-toolbar-item">
							<vue-feather type="columns" size="1.25rem" />
							<span>Columns</span>
						</span>
					</template>
				</MultiSelect>
			</span>
		</div>
		<!-- Datable -->
		<DataTable
			:class="previewMode ? 'p-datatable-xsm' : 'p-datatable-sm'"
			:value="rawContent.csv"
			:rows="rows"
			paginator
			:paginatorPosition="paginatorPosition ? paginatorPosition : `bottom`"
			:rowsPerPageOptions="[5, 10, 25, 50, 100]"
			paginatorTemplate="CurrentPageReport FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
			currentPageReportTemplate="{first} to {last} of {totalRecords}"
			removableSort
			showGridlines
			scrollable
			scroll-height="flex"
			:tableStyle="tableStyle ? tableStyle : `width:auto`"
		>
			<Column
				v-for="(colName, index) of rawContent.headers"
				:key="index"
				:field="index.toString()"
				:header="colName"
				:style="previousHeaders && !previousHeaders.includes(colName) ? 'border-color: green' : ''"
				sortable
				:frozen="index == 0"
				:hidden="selectedColumns.includes(colName) ? false : true"
			>
				<template #header v-if="!previewMode && !isEmpty(headerStats) && showSummaries">
					<!-- column summary charts below -->
					<div class="column-summary">
						<div class="column-summary-row">
							<span class="column-summary-label">Max:</span>
							<span class="column-summary-value">{{ headerStats?.[index].maxValue }}</span>
						</div>
						<Chart
							v-if="headerStats?.[index].chartData"
							class="histogram"
							type="bar"
							:height="480"
							:data="headerStats?.[index].chartData"
							:options="CHART_OPTIONS"
						/>
						<div class="column-summary-row max">
							<span class="column-summary-label">Min:</span>
							<span class="column-summary-value">{{ headerStats?.[index].minValue }}</span>
						</div>
						<div class="column-summary-row">
							<span class="column-summary-label">Mean:</span>
							<span class="column-summary-value">{{ headerStats?.[index].mean }}</span>
						</div>
						<div class="column-summary-row">
							<span class="column-summary-label">Median:</span>
							<span class="column-summary-value">{{ headerStats?.[index].median }}</span>
						</div>
						<div class="column-summary-row">
							<span class="column-summary-label">SD:</span>
							<span class="column-summary-value">{{ headerStats?.[index].sd }}</span>
						</div>
					</div>
				</template>
			</Column>
		</DataTable>
	</div>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { ref, watch, nextTick } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import type { CsvAsset, DatasetColumn } from '@/types/Types';
import MultiSelect from 'primevue/multiselect';
import Button from 'primevue/button';
import Chart from 'primevue/chart';
import InputSwitch from 'primevue/inputswitch';

type ChartData = {
	labels: string[];
	datasets: {
		label: string;
		backgroundColor: string;
		hoverBackgroundColor: string;
		data: number[];
		categoryPercentage: number;
		barPercentage: number;
		minBarLength: number;
	}[];
};

const props = defineProps<{
	rawContent: CsvAsset; // Temporary - this is also any in ITypeModel
	rows?: number;
	previewMode?: boolean;
	previousHeaders?: String[] | null;
	paginatorPosition?: 'bottom' | 'both' | 'top' | undefined;
	tableStyle?: String;
	columns: DatasetColumn[];
	rowCount: number;
}>();

const CATEGORYPERCENTAGE = 1.0;
const BARPERCENTAGE = 1.0;
const MINBARLENGTH = 1;
const CHART_OPTIONS = {
	indexAxis: 'y',
	plugins: {
		legend: {
			labels: {
				display: false
			},
			display: false
		},
		tooltip: {
			enabled: true,
			position: 'nearest',
			displayColors: false,
			beforeTitle: 'Count:',
			backgroundColor: '#666666dd'
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
				borderColor: getComputedStyle(document.documentElement).getPropertyValue('--surface-border-light')
			}
		}
	}
};

const showSummaries = ref(true);
const selectedColumns = ref<string[]>(props.rawContent.headers);
const headerStats = ref<
	{
		minValue: number;
		maxValue: number;
		mean: number;
		median: number;
		sd: number;
		chartData: ChartData;
	}[]
>([]);

// Given the bins for a column set up the object needed for the chart.
const setBarChartData = (bins: number[]): ChartData => {
	const documentStyle = getComputedStyle(document.documentElement);
	const dummyLabels: string[] = [];
	// reverse the bins so that the chart is displayed in the correct order
	bins.reverse();
	for (let i = 0; i < bins.length; i++) {
		dummyLabels.push(i.toString());
	}
	return {
		labels: ['Bin 1', 'Bin 2', 'Bin 3', 'Bin 4', 'Bin 5', 'Bin 6', 'Bin 7', 'Bin 8', 'Bin 9', 'Bin 10'].reverse(),
		datasets: [
			{
				label: 'Count',
				backgroundColor: documentStyle.getPropertyValue('--primary-color'),
				hoverBackgroundColor: documentStyle.getPropertyValue('--primary-color-light'),
				data: bins,
				categoryPercentage: CATEGORYPERCENTAGE,
				barPercentage: BARPERCENTAGE,
				minBarLength: MINBARLENGTH
			}
		]
	};
};

// TODO: We should be using a formatter from number.ts, not sure why we are formatting it like this (ask Yohann when he's back)
function roundStat(stat: number) {
	return Math.round(stat * 1000) / 1000;
}

watch(
	() => props.rawContent,
	async () => {
		await nextTick();
		headerStats.value =
			props.rawContent.stats?.map((stat) => ({
				minValue: roundStat(stat.minValue),
				maxValue: roundStat(stat.maxValue),
				mean: roundStat(stat.mean),
				median: roundStat(stat.median),
				sd: roundStat(stat.sd),
				chartData: setBarChartData(stat.bins)
			})) ?? [];
		// eslint-disable-next-line
		props.rawContent.headers.sort();
	},
	{ immediate: true }
);
</script>

<style scoped>
.datatable-toolbar {
	display: flex;
	flex-direction: row;
	gap: var(--gap-4);
	padding-bottom: var(--gap-2);
}
.datatable-toolbar-item {
	display: flex;
	flex-direction: row;
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
	align-items: center;
	gap: var(--gap-2);
}

.datatable-toolbar:deep(.p-multiselect .p-multiselect-label) {
	padding: var(--gap-2);
	font-size: var(--font-caption);
}

.p-datatable:deep(.p-column-header-content) {
	display: grid;
	grid-template-areas:
		'name sortIcon'
		'columnSummary columnSummary';
}

.p-datatable:deep(.p-column-title) {
	grid-area: name;
	padding-left: 0.5rem;
}
.p-datatable:deep(.p-sortable-column-icon) {
	grid-area: sortIcon;
}

.p-datatable.p-datatable-sm:deep(.p-datatable-thead > tr > th) {
	padding: 0.5rem 0;
	background-color: var(--surface-50);
}

.p-datatable.p-datatable-sm:deep(.p-datatable-tbody > tr > td) {
	padding: var(--gap-1) 0.5rem;
}
/* Histograms & Charts  */
.column-summary {
	position: relative;
	width: 100%;
	margin-top: 0.5rem;
	grid-area: columnSummary;
}
.column-summary-row {
	display: flex;
	flex-direction: row;
	justify-content: space-between;
	padding-left: 0.5rem;
	padding-right: 0.5rem;
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
	font-weight: lighter;
}
.column-summary-row.max {
	position: relative;
	top: -0.5rem;
}

.histogram {
	height: 100px;
}

.p-datatable-flex-scrollable {
	overflow: hidden;
}
:deep(.download-button.p-button.p-button-text),
.columns-dropdown {
	color: var(--text-color-primary);
}
.datatable-container {
	display: flex;
	flex-direction: column;
	flex: 1;
	overflow: hidden;
}
</style>
