<template>
	<p v-if="isLoading">Loading comparison table...</p>
	<p v-else-if="error">Error loading comparison table: {{ error }}</p>
	<DataTable
		v-else
		:size="'small'"
		showGridlines
		stripedRows
		removableSort
		:value="tableData"
		:scrollable="true"
		:scrollHeight="'flex'"
	>
		<ColumnGroup type="header">
			<Row>
				<Column header="" />
				<Column :colspan="columnModelHeaders.length" :header="columnModelName" />
			</Row>
			<Row>
				<Column :header="modelNames.rowModel" />
				<Column v-for="col in columnModelHeaders" :key="col" :header="col" />
			</Row>
		</ColumnGroup>

		<Column field="rowState" header="State" />
		<Column v-for="col in columnModelHeaders" :key="col" :field="col" />
	</DataTable>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import ColumnGroup from 'primevue/columngroup';
import Row from 'primevue/row';
import Papa from 'papaparse';

// Define props for the component
const props = defineProps({
	csvText: {
		type: String,
		required: true
	},
	tableName: {
		type: String,
		required: true
	}
});

const tableData = ref([]);
const isLoading = ref(true);
const error = ref(null);

// Extract model names from the table name
// Example: "0–SIDARTHE — 1–SEIR" -> { columnModel: "0–SIDARTHE", rowModel: "1–SEIR" }
const modelNames = computed(() => {
	const parts = props.tableName.split('—').map((part) => part.trim());
	return {
		columnModel: parts[0],
		rowModel: parts[1]
	};
});

const columnModelName = computed(() => modelNames.value.columnModel);
const columnModelHeaders = ref<string[]>([]);

function parseCSVToGroupedTable(csvText: string) {
	try {
		Papa.parse(csvText, {
			header: true,
			skipEmptyLines: true,
			complete: (results) => {
				// The first column is empty in the CSV, it represents row states
				// Get all column headers except the first one, which is empty
				const headers = results.meta.fields?.filter((h) => h !== '');
				columnModelHeaders.value = headers || [];

				// Build table data with row groups
				const data: any[] = [];

				results.data.forEach((row: any) => {
					// Get the row state name (first column, which has an empty header)
					const rowState = row[''] || '';

					// Skip empty rows
					if (!rowState) return;

					const rowData: any = {
						rowModel: modelNames.value.rowModel,
						rowState
					};

					// Add values for each column
					headers?.forEach((header) => {
						rowData[header] = row[header] === '=' ? '✓' : '';
					});

					data.push(rowData);
				});

				tableData.value = data;
				isLoading.value = false;
			},
			error: (err) => {
				error.value = err.message;
				isLoading.value = false;
			}
		});
	} catch (err: any) {
		error.value = err.message;
		isLoading.value = false;
	}
}

onMounted(() => {
	parseCSVToGroupedTable(props.csvText);
});
</script>

<style scoped>
.p-datatable {
	border: 1px solid var(--surface-border);
	border-radius: var(--border-radius);
}

:deep(th:first-of-type) {
	border-top-left-radius: var(--border-radius);
}

:deep(th:last-of-type) {
	border-top-right-radius: var(--border-radius);
}

:deep(tr:last-of-type td:first-of-type) {
	border-bottom-left-radius: var(--border-radius);
}

:deep(tr:last-of-type td:last-of-type) {
	border-bottom-right-radius: var(--border-radius);
}

:deep([role='columnheader']),
:deep(td) {
	text-align: center;
}

:deep(.p-column-title) {
	width: 100%;
}
</style>
