<template>
	<DataTable
		class="dataset-overview-table"
		:value="formattedData"
		dataKey="id"
		:rowsPerPageOptions="[10, 20, 50]"
		edit-mode="cell"
		@cell-edit-complete="onCellEditComplete"
	>
		<Column field="id" header="ID" sortable style="width: 10%" />
		<Column field="name" header="Name" sortable style="width: 10%" />
		<Column field="description" header="Description" sortable style="width: 30%" />
		<Column field="concept" header="Concept" sortable style="width: 10%">
			<template #body="{ data }">
				<template v-if="!isEmpty(data.concept)">
					{{ getNameOfCurieCached(nameOfCurieCache, getCurieFromGroudingIdentifier(data.concept)) }}

					<a
						target="_blank"
						rel="noopener noreferrer"
						:href="getCurieUrl(getCurieFromGroudingIdentifier(data.concept))"
						@click.stop
						aria-label="Open Concept"
					>
						<i class="pi pi-external-link" />
					</a>
				</template>
				<template v-else>--</template>
			</template>
			<template #editor="{ data, index }">
				<AutoComplete
					v-model="conceptSearchTerm.name"
					:suggestions="curies"
					@complete="onSearch"
					@item-select="
						updateDataset(index, 'metadata', {
							...data.column?.metadata,
							groundings: {
								identifiers: parseCurie($event.value?.curie)
							}
						})
					"
					optionLabel="name"
				/>
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
import type { DKG, Dataset, DatasetColumn } from '@/types/Types';
import { computed, ref } from 'vue';
import { cloneDeep, isEmpty } from 'lodash';
import {
	getNameOfCurieCached,
	getCurieFromGroudingIdentifier,
	getCurieUrl,
	searchCuriesEntities,
	parseCurie
} from '@/services/concept';
import AutoComplete, { AutoCompleteCompleteEvent } from 'primevue/autocomplete';

const props = defineProps<{
	dataset: Dataset;
}>();
const emit = defineEmits(['update-dataset']);

const conceptSearchTerm = ref({
	curie: '',
	name: ''
});
const curies = ref<DKG[]>([]);

const nameOfCurieCache = ref(new Map<string, string>());

const formattedData = computed(() => {
	if (!props.dataset?.columns) return [];
	return formatData(props.dataset.columns);
});

function formatName(name: string) {
	return (name.charAt(0).toUpperCase() + name.slice(1)).replace('_', ' ');
}

async function onSearch(event: AutoCompleteCompleteEvent) {
	const query = event.query;
	if (query.length > 2) {
		const response = await searchCuriesEntities(query);
		curies.value = response;
	}
}

function updateDataset(index: number, field: string, value: any) {
	const datasetClone = cloneDeep(props.dataset);
	if (datasetClone.columns) {
		if (!datasetClone.columns[index][field]) {
			datasetClone.columns[index][field] = null;
		}
		datasetClone.columns[index][field] = value;
	}
	emit('update-dataset', datasetClone);
}

function onCellEditComplete() {
	conceptSearchTerm.value = { curie: '', name: '' };
}

function formatData(data: DatasetColumn[]) {
	return data.map((col) => ({
		id: col.name,
		name: formatName(col.name),
		description: col.description,
		concept: col.metadata?.groundings?.identifiers,
		unit: col.metadata?.unit,
		dataType: col.dataType,
		stats: col.metadata?.column_stats,
		column: col
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
