<template>
	<DataTable>
		<Column
			v-for="(col, index) in selectedColumns"
			:field="col.field"
			:header="col.header"
			:sortable="col.field !== 'stats'"
			:key="index"
		>
		</Column>
	</DataTable>
</template>

<script setup lang="ts">
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';

defineProps<{
	selectedColumns: { field: string; header: string }[];
}>();
</script>
