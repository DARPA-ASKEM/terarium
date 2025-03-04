<template>
	<p v-if="isLoading">Loading comparison table...</p>
	<p v-else-if="error">Error loading comparison table: {{ error }}</p>
	<DataTable
		v-else
		:size="'small'"
		showGridlines
		stripedRows
		removableSort
		:value="csvData"
		:scrollable="true"
		:scrollHeight="'flex'"
	>
		<ColumnGroup type="header">
			<Row>
				<Column />
				<Column :colspan="columns.length" :header="modelNames.columnModel" />
			</Row>
			<Row>
				<Column
					v-for="(col, index) in columns"
					:key="col.field as string"
					:header="index == 0 ? modelNames.rowModel : col.header"
				/>
			</Row>
		</ColumnGroup>

		<Column v-for="col in columns" :key="col.field as string" :field="col.field" />
	</DataTable>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import DataTable from 'primevue/datatable';
import Column, { ColumnProps } from 'primevue/column';
import ColumnGroup from 'primevue/columngroup';
import Row from 'primevue/row';
import Papa from 'papaparse';
import { isEmpty } from 'lodash';

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

const csvData = ref([]);
const columns = ref<ColumnProps[]>([]);
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

function rowReplaceEmptyKey(row: any) {
	Object.keys(row).forEach((key) => {
		if (isEmpty(key)) {
			row.empty = row[key];
			delete row[key];
		}
	});
	return row;
}

function onComplete(results: any) {
	// If the CSV has an empty header, just use 'empty' as the field name
	columns.value = results.meta.fields.map((field: string) => ({
		field: isEmpty(field) ? 'empty' : field,
		header: field
	}));
	csvData.value = results.data.map(rowReplaceEmptyKey);
	isLoading.value = false;
}

function onError(err: any) {
	error.value = err.message;
	isLoading.value = false;
}

// Configuration for PapaParse
// https://www.papaparse.com/docs#config
const config = {
	header: true,
	dynamicTyping: true,
	skipEmptyLines: true,
	complete: onComplete,
	error: onError
};

// Load CSV when component is mounted
onMounted(() => {
	try {
		Papa.parse(props.csvText, config);
	} catch (fetchError) {
		onError(fetchError);
	}
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
