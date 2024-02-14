<template>
	<DataTable
		class="dataset-overview-table"
		:value="formattedData"
		dataKey="name"
		:rowsPerPageOptions="[10, 20, 50]"
	>
		<Column field="id" header="ID" sortable style="width: 10%" />
		<Column field="name" header="Name" sortable style="width: 10%" />
		<Column field="description" header="Description" sortable style="width: 30%" />
		<Column field="concept" header="Concept" sortable style="width: 10%">
			<template #body="{ data }">
				<template v-if="!isEmpty(data.concept)">
					{{ console.log(data.concept) }}
					{{ getNameOfCurieCached(nameOfCurieCache, getCurieFromGroudingIdentifier(data.concept)) }}

					<a
						target="_blank"
						rel="noopener noreferrer"
						:href="`http://34.230.33.149:8772/${getCurieFromGroudingIdentifier(data.concept)}`"
						@click.stop
						aria-label="Open Concept"
					>
						<i class="pi pi-external-link" />
					</a>
				</template>
			</template>
		</Column>
		<Column field="unit" header="Unit" sortable style="width: 10%" />
		<Column field="dataType" header="Datatype" sortable style="width: 10%" />
		<Column field="stats" header="Stats" style="width: 20%">
			<template #body="{ data }">
				<ul v-if="data.stats">
					<li v-for="(stat, index) in Object.keys(data.stats)" :key="index">
						{{ stat }}: {{ data.stats[stat] }}
					</li>
				</ul>
			</template>
		</Column>
	</DataTable>
</template>

<script setup lang="ts">
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import type { Dataset, DatasetColumn } from '@/types/Types';
import { computed, ref } from 'vue';
import { isEmpty } from 'lodash';
import { getNameOfCurieCached, getCurieFromGroudingIdentifier } from '@/services/concept';

const props = defineProps<{
	dataset: Dataset;
}>();

const nameOfCurieCache = ref(new Map<string, string>());

const formattedData = computed(() => {
	if (!props.dataset?.columns) return [];
	return formatData(props.dataset.columns);
});

function formatName(name: string) {
	return (name.charAt(0).toUpperCase() + name.slice(1)).replace('_', ' ');
}

function formatData(data: DatasetColumn[]) {
	return data.map((col) => ({
		id: col.name,
		name: formatName(col.name),
		description: col.description,
		concept: col.metadata?.groundings?.identifiers,
		unit: col.metadata?.unit,
		dataType: col.dataType,
		stats: col.metadata?.column_stats
	}));
}
</script>

<style scoped>
.dataset-overview-table :deep(td:empty:before) {
	content: '--';
}

.p-datatable:deep(.p-datatable-thead > tr > th) {
	background-color: var(--surface-50);
}
</style>
