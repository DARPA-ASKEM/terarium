<template>
	<DataTable :value="summaries" tableStyle="min-width: 50rem">
		<Column field="username" header="Username" sortable></Column>
		<Column field="name" header="Name" sortable></Column>
		<Column field="task" header="Task" sortable></Column>
		<Column field="description" header="Description" sortable></Column>
		<Column field="notes" header="Notes" sortable></Column>
		<Column field="multipleUsers" header="Multiple Users" sortable></Column>
		<Column field="timestampMillis" header="Timestamp" sortable>
			<template #body="slotProps">
				{{ formatTimestamp(slotProps.data.timestampMillis) }}
			</template>
		</Column>
		<Column field="download" header="Download">
			<template #body="slotProps">
				<Button icon="pi pi-download" label="Download" size="small" @click="downloadCSV(slotProps.data)"></Button>
			</template>
		</Column>
	</DataTable>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import API from '@/api/api';
import Button from 'primevue/button';
import { formatTimestamp } from '@/utils/date';

const summaries = ref(null);

onMounted(async () => {
	summaries.value = (await API.get('/evaluation/scenarios')).data;
});

const downloadCSV = async (data) => {
	const userId = data.userId;
	const name = data.name;
	console.log(`Fetching ${userId}/${name}`);

	// Build the csv
	const response = await API.get(`/evaluation/download?userId=${userId}&name=${name}`);
	const csv = response.data;

	// Download the file
	const link = document.createElement('a');
	const file = new Blob([csv], { type: 'text/csv' });
	link.href = URL.createObjectURL(file);
	link.download = `${userId}-${name}.csv`;
	link.click();
	URL.revokeObjectURL(link.href);
};
</script>
<style scoped lang="scss"></style>
