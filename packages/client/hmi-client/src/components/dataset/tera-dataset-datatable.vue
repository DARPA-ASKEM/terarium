<!-- column summary charts below -->
<template>
	<DataTable
		tableStyle="width:auto"
		class="p-datatable-sm"
		:value="csvContent?.slice(1, csvContent.length)"
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
			<!-- column summary charts below -->
			<template #header>
				<div class="histogram">
					<div class="histogram-label-min">Min: {{ csvMinsToDisplay?.at(index) }}</div>
					<Chart type="bar" :height="800" :data="chartData?.at(index)" :options="chartOptions" />
					<div class="histogram-label-max">Max: {{ csvMaxsToDisplay?.at(index) }}</div>
					<div class="histogram-label-other">Mean: {{ csvMeansToDisplay?.at(index) }}</div>
					<div class="histogram-label-other">Median: {{ csvMedianToDisplay?.at(index) }}</div>
					<div class="histogram-label-other">SD: {{ csvSdToDisplay?.at(index) }}</div>
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
/* Histograms & Charts  */
.summary-chart-table {
	width: max-content;
	border-spacing: 0;
	border-collapse: collapse;
	/* position: sticky; */
	/* top: -1rem; */
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
</style>
