<template>
	<tera-progress-spinner v-if="!rawContent" :font-size="2" is-centered />
	<div v-else class="datatable-container">
		<!-- Toggle histograms & column summary charts -->
		<div class="datatable-toolbar">
			<span class="datatable-toolbar-item">
				{{ csvHeaders?.length || 'No' }} columns | {{ csvContent?.length || 'No' }} rows
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
					:modelValue="selectedColumns"
					:options="csvHeaders"
					@update:modelValue="onToggle"
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
			:value="csvContent?.slice(1, csvContent.length)"
			:rows="props.rows"
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
				v-for="(colName, index) of selectedColumns"
				:key="index"
				:field="index.toString()"
				:header="colName"
				:style="previousHeaders && !previousHeaders.includes(colName) ? 'border-color: green' : ''"
				sortable
				:frozen="index == 0"
			>
				<template #header>
					<!-- column summary charts below -->
					<div v-if="!previewMode && props.rawContent?.stats && showSummaries" class="column-summary">
						<div class="column-summary-row">
							<span class="column-summary-label">Max:</span>
							<span class="column-summary-value">{{ csvMaxsToDisplay?.at(index) }}</span>
						</div>
						<Chart class="histogram" type="bar" :height="480" :data="chartData?.at(index)" :options="chartOptions" />
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
	</div>
</template>

<script setup lang="ts">
import { computed, ComputedRef, ref, watch } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import type { CsvAsset } from '@/types/Types';
import MultiSelect from 'primevue/multiselect';
import Button from 'primevue/button';
import Chart from 'primevue/chart';
import InputSwitch from 'primevue/inputswitch';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';

const props = defineProps<{
	rawContent: CsvAsset | null; // Temporary - this is also any in ITypeModel
	rows?: number;
	previewMode?: boolean;
	previousHeaders?: String[] | null;
	paginatorPosition?: 'bottom' | 'both' | 'top' | undefined;
	tableStyle?: String;
}>();

const CATEGORYPERCENTAGE = 1.0;
const BARPERCENTAGE = 1.0;
const MINBARLENGTH = 1;

const showSummaries = ref(true);

const csvContent: ComputedRef<string[][] | undefined> = computed(() => props.rawContent?.csv);
const csvHeaders = computed(() => props.rawContent?.headers);
const chartData = computed(() => props.rawContent?.stats?.map((stat) => setBarChartData(stat.bins)));

const selectedColumns = ref(csvHeaders?.value);

const csvMinsToDisplay = computed(() =>
	props.rawContent?.stats?.map((stat) => Math.round(stat.minValue * 1000) / 1000)
);
const csvMaxsToDisplay = computed(() =>
	props.rawContent?.stats?.map((stat) => Math.round(stat.maxValue * 1000) / 1000)
);
const csvMeansToDisplay = computed(() => props.rawContent?.stats?.map((stat) => Math.round(stat.mean * 1000) / 1000));
const csvMedianToDisplay = computed(() =>
	props.rawContent?.stats?.map((stat) => Math.round(stat.median * 1000) / 1000)
);
const csvSdToDisplay = computed(() => props.rawContent?.stats?.map((stat) => Math.round(stat.sd * 1000) / 1000));
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
					borderColor: documentStyle.getPropertyValue('--surface-border-light')
				}
			}
		}
	};
};

const onToggle = (val) => {
	selectedColumns.value = csvHeaders?.value?.filter((col) => val.includes(col));
};

watch(
	() => props.rawContent,
	() => {
		selectedColumns.value = csvHeaders?.value;
	}
);
</script>

<style scoped>
.datatable-toolbar {
	display: flex;
	flex-direction: row;
	gap: var(--gap);
	padding-bottom: var(--gap);
}
.datatable-toolbar-item {
	display: flex;
	flex-direction: row;
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
	align-items: center;
	gap: var(--gap-small);
}

.datatable-toolbar:deep(.p-multiselect .p-multiselect-label) {
	padding: var(--gap-small);
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
