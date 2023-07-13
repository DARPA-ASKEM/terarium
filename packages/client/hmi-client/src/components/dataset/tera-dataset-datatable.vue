<!-- column summary charts below -->
<template>
	<DataTable
		:class="previewMode ? 'p-datatable-xsm' : 'p-datatable-sm'"
		:value="csvContent?.slice(1, csvContent.length)"
		:rows="props.rows"
		paginator
		:paginatorPosition="paginatorPosition ? paginatorPosition : `bottom`"
		:rowsPerPageOptions="[5, 10, 25, 50, 100]"
		paginatorTemplate="CurrentPageReport FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
		currentPageReportTemplate="{first} to {last} of {totalRecords}"
		removableSort
		showGridlines
		:tableStyle="tableStyle ? tableStyle : `width:auto`"
	>
		<Column
			v-for="(colName, index) of csvHeaders"
			:key="index"
			:field="index.toString()"
			:header="colName"
			:style="previousHeaders && !previousHeaders.includes(colName) ? 'border-color: green' : ''"
			sortable
			:frozen="index == 0"
		>
			<template #header>
				<!-- column summary charts below -->
				<div v-if="!previewMode && props.rawContent?.stats" class="column-summary">
					<div class="column-summary-row">
						<span class="column-summary-label">Max:</span>
						<span class="column-summary-value">{{ csvMaxsToDisplay?.at(index) }}</span>
					</div>
					<Chart
						class="histogram"
						type="bar"
						:height="480"
						:data="chartData?.at(index)"
						:options="chartOptions"
					/>
					<div class="column-summary-row max">
						<span class="column-summary-label">Min:</span>
						<span class="column-summary-value">{{ csvMinsToDisplay?.at(index) }}</span>
					</div>
					<div class="column-summary-row">
						<span class="column-summary-label">Mean:</span>
						<span class="column-summary-value">{{ csvMeansToDisplay?.at(index) }}</span>
					</div>
					<div class="column-summary-row">
						<span class="column-summary-label">Median:</span>
						<span class="column-summary-value">{{ csvMedianToDisplay?.at(index) }}</span>
					</div>
					<div class="column-summary-row">
						<span class="column-summary-label">SD:</span>
						<span class="column-summary-value">{{ csvSdToDisplay?.at(index) }}</span>
					</div>
				</div>
			</template>
		</Column>
	</DataTable>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Chart from 'primevue/chart';
import { CsvAsset } from '@/types/Types';

const props = defineProps<{
	rawContent: CsvAsset | null; // Temporary - this is also any in ITypeModel
	rows?: number;
	previewMode?: boolean;
	previousHeaders?: String[] | null;
	paginatorPosition?: 'bottom' | 'both' | 'top' | undefined;
	tableStyle?: String;
}>();

const CATEGORYPERCENTAGE = 0.9;
const BARPERCENTAGE = 1.0;
const MINBARLENGTH = 1;

const csvContent = computed(() => props.rawContent?.csv);
const csvHeaders = computed(() => props.rawContent?.headers);
const chartData = computed(() =>
	props.rawContent?.stats?.map((stat) => setBarChartData(stat.bins))
);
const csvMinsToDisplay = computed(() =>
	props.rawContent?.stats?.map((stat) => Math.round(stat.minValue * 1000) / 1000)
);
const csvMaxsToDisplay = computed(() =>
	props.rawContent?.stats?.map((stat) => Math.round(stat.maxValue * 1000) / 1000)
);
const csvMeansToDisplay = computed(() =>
	props.rawContent?.stats?.map((stat) => Math.round(stat.mean * 1000) / 1000)
);
const csvMedianToDisplay = computed(() =>
	props.rawContent?.stats?.map((stat) => Math.round(stat.median * 1000) / 1000)
);
const csvSdToDisplay = computed(() =>
	props.rawContent?.stats?.map((stat) => Math.round(stat.sd * 1000) / 1000)
);
const chartOptions = computed(() => setChartOptions());

// Given the bins for a column set up the object needed for the chart.
const setBarChartData = (bins: any[]) => {
	const documentStyle = getComputedStyle(document.documentElement);
	const dummyLabels: string[] = [];
	// reverse the bins so that the chart is displayed in the correct order
	bins.reverse();
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
</style>
