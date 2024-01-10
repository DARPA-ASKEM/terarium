<template>
	<!-- Toggle histograms & column summary charts -->
	<div class="datatable-toolbar">
		<span class="datatable-toolbar-item">
			{{ csvHeaders?.length || 'No' }} columns | {{ csvContent?.length || 'No' }} rows
		</span>
		<Button
			class="datatable-toolbar-item"
			label="Download"
			icon="pi pi-download"
			text
			outlined
			style="margin-left: auto"
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
					<span class="datatable-toolbar-item">
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
		:value="props.rawContent?.data ? csvContent : csvContent?.slice(1, csvContent.length)"
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
			:field="props.rawContent?.data ? colName : index.toString()"
			:header="colName"
			:style="previousHeaders && !previousHeaders.includes(colName) ? 'border-color: green' : ''"
			sortable
			:frozen="index == 0"
		>
		</Column>
	</DataTable>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import { CsvAsset } from '@/types/Types';
import MultiSelect from 'primevue/multiselect';
import Button from 'primevue/button';

const props = defineProps<{
	rawContent: CsvAsset | null; // Temporary - this is also any in ITypeModel
	rows?: number;
	previewMode?: boolean;
	previousHeaders?: String[] | null;
	paginatorPosition?: 'bottom' | 'both' | 'top' | undefined;
	tableStyle?: String;
}>();

const csvContent = computed(() => props.rawContent?.data || props.rawContent?.csv);
const csvHeaders = computed(() => props.rawContent?.headers);

const selectedColumns = ref(csvHeaders?.value);
const onToggle = (val) => {
	selectedColumns.value = csvHeaders?.value?.filter((col) => val.includes(col));
};
</script>

<style scoped>
.datatable-toolbar {
	display: flex;
	flex-direction: row;
	gap: 1rem;
}
.datatable-toolbar-item {
	display: flex;
	flex-direction: row;
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
	align-items: center;
	gap: 0.5rem;
}

.datatable-toolbar:deep(.p-multiselect .p-multiselect-label) {
	padding: 0.5rem;
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
	padding: 0.25rem 0.5rem;
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

.p-datatable-flex-scrollable {
	overflow: hidden;
}
</style>
