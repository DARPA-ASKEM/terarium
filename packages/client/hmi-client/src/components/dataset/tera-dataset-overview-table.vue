<template>
	<DataTable
		class="dataset-overview-table"
		:value="tableData"
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
				<div v-if="data.concept" class="concept">
					<span>{{ data.concept }}<i class="pi pi-chevron-down pl-2 text-xs" /></span>
					<a target="_blank" rel="noopener noreferrer" :href="data.conceptURL" @click.stop aria-label="Open Concept">
						<i class="pi pi-external-link pl-4 text-xs" v-tooltip.top="'MIRA Epi Metaregistry'" />
					</a>
				</div>
			</template>
			<template #editor="{ data, index }">
				<AutoComplete
					v-model="query"
					placeholder="Search for concepts"
					:suggestions="curies"
					@complete="async () => (curies = await searchCuriesEntities(query))"
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
				<tera-boxplot v-if="data.stats" :stats="data.stats" />
			</template>
		</Column>
	</DataTable>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import type { Dataset, DKG } from '@/types/Types';
import { cloneDeep } from 'lodash';
import {
	getCurieFromGroundingIdentifier,
	getCurieUrl,
	getNameOfCurieCached,
	parseCurie,
	searchCuriesEntities
} from '@/services/concept';
import AutoComplete from 'primevue/autocomplete';
import TeraBoxplot from '@/components/widgets/tera-boxplot.vue';

const props = defineProps<{
	dataset: Dataset;
}>();
const emit = defineEmits(['update-dataset']);

const query = ref('');
const curies = ref<DKG[]>([]);
const tableData = ref<any[]>([]);

function formatName(name: string) {
	return (name.charAt(0).toUpperCase() + name.slice(1)).replace('_', ' ');
}

async function updateDataset(index: number, field: string, value: any) {
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
	query.value = '';
}

watch(
	() => props.dataset?.columns,
	async () => {
		// Create the list here of IDs
		// FIXME
		tableData.value = await Promise.all(
			props.dataset.columns?.map(async (col) => {
				const identifier = col.metadata?.groundings?.identifiers ?? parseCurie(col.grounding?.identifiers[0].curie);
				return {
					id: col.name,
					name: formatName(col.name),
					description: col.description,
					concept: await getNameOfCurieCached(getCurieFromGroundingIdentifier(identifier)),
					conceptURL: getCurieUrl(getCurieFromGroundingIdentifier(identifier)),
					unit: col.metadata?.unit,
					dataType: col.metadata?.column_stats?.type,
					stats: col.metadata?.column_stats,
					column: col
				};
			}) ?? []
		);
	},
	{ immediate: true }
);
</script>

<style scoped>
.dataset-overview-table :deep(td:empty:before) {
	content: '--';
}

.p-datatable:deep(.p-datatable-thead > tr > th) {
	background-color: var(--surface-50);
}

.concept,
.concept > span {
	display: flex;
	align-items: center;
	justify-content: space-between;
	align-items: center;
}
</style>
