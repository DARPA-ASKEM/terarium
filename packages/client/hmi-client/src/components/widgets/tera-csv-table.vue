<template>
	<div class="csv-viewer">
		<!-- Loading State -->
		<div v-if="isLoading" class="loading">Loading CSV...</div>

		<!-- Error State -->
		<div v-else-if="error" class="error">Error loading CSV: {{ error }}</div>

		<!-- PrimeVue DataTable -->
		<DataTable v-else size="small" :value="csvData" :globalFilter="true" :sortMode="'multiple'">
			<!-- Dynamically generate columns -->
			<Column v-for="col in columns" :key="col.field" :field="col.field" :header="col.header" :sortable="true" />
		</DataTable>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Papa from 'papaparse';

// Define props for the component
const props = defineProps({
	csvText: {
		type: String,
		required: true
	},
	hasHeaders: {
		type: Boolean,
		default: true
	}
});

// Reactive state to store CSV data
const csvData = ref([]);
const columns = ref([]);
const isLoading = ref(true);
const error = ref(null);

// Method to fetch and parse CSV
const loadCSV = () => {
	try {
		// Parse CSV using Papaparse
		Papa.parse(props.csvText, {
			header: props.hasHeaders,
			dynamicTyping: true,
			complete: (results) => {
				if (props.hasHeaders) {
					// If headers exist, use them to create columns
					columns.value = results.meta.fields.map((field) => ({
						field,
						header: field
					}));
					csvData.value = results.data;
				} else {
					// If no headers, generate default column names
					columns.value = results.data[0].map((_, index) => ({
						field: `col${index}`,
						header: `Column ${index + 1}`
					}));
					csvData.value = results.data.slice(1); // Remove the first row used as headers
				}
				isLoading.value = false;
			},
			error: (err) => {
				error.value = err.message;
				isLoading.value = false;
			}
		});
	} catch (fetchError) {
		error.value = fetchError.message;
		isLoading.value = false;
	}
};

// Load CSV when component is mounted
onMounted(loadCSV);
</script>

<style scoped>
.csv-viewer {
	border: 1px solid var(--surface-border);
	width: 100%;
}

.loading,
.error {
	padding: 20px;
	text-align: center;
	color: #666;
}
</style>
