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
					<div class="flex flex-row align-items-center">
						{{
							getNameOfCurieCached(nameOfCurieCache, getCurieFromGroudingIdentifier(data.concept))
						}}
						<i class="pi pi-chevron-down pl-2 text-xs" />
						<a
							target="_blank"
							rel="noopener noreferrer"
							:href="getCurieUrl(getCurieFromGroudingIdentifier(data.concept))"
							@click.stop
							aria-label="Open Concept"
						>
							<i class="pi pi-external-link pl-2 text-xs" v-tooltip.top="'MIRA Epi Metaregistry'" />
						</a>
					</div>
				</template>
				<template v-else>--</template>
			</template>
			<template #editor="{ data, index }">
				<AutoComplete
					v-model="conceptSearchTerm.name"
					placeholder="Search for concepts"
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
				<section v-if="data.stats" class="">
					<!-- Top line -->
					<div class="flex flex-row justify-content-between mb-2">
						<div class="caption subdued">{{ data.stats.type.toUpperCase() }}</div>
						<div v-if="data.stats.type === 'numeric'" class="caption subdued">
							STD: {{ data.stats.std.toFixed(3) }}
						</div>
						<div v-if="data.stats.type === 'categorical'" class="caption subdued">
							{{ data.stats.num_unique_entries }} unique
						</div>
						<div class="caption subdued">{{ data.stats.num_null_entries }} nulls</div>
					</div>
					<!-- This draws a box-plot with the stats -->
					<div
						v-if="data.stats.type === 'numeric'"
						class="flex flex-row align-items-center tnum mb-1"
					>
						<div class="caption mr-1">{{ data.stats.min }}</div>
						<div
							class="line"
							:style="getBoxplotPartialWidth(data.stats.quantile_25, data.stats.max)"
						/>
						<div
							class="box-left"
							:style="
								getBoxplotPartialWidth(
									data.stats.quantile_50 - data.stats.quantile_25,
									data.stats.max
								)
							"
						/>
						<div class="caption box-middle">
							<div class="centered-text below">{{ data.stats.mean.toFixed(2) }}</div>
						</div>
						<div
							class="box-right"
							:style="
								getBoxplotPartialWidth(
									data.stats.quantile_75 - data.stats.quantile_25,
									data.stats.max
								)
							"
						></div>
						<div
							class="line"
							:style="
								getBoxplotPartialWidth(data.stats.max - data.stats.quantile_75, data.stats.max)
							"
						></div>
						<div class="caption ml-1">{{ data.stats.max }}</div>
					</div>

					<!-- This draws a list of the most common entries, sorted by value then by name -->
					<div v-if="data.stats.type === 'categorical'">
						<div class="flex flex-row flex-wrap">
							<span
								class="white-space-nowrap"
								v-for="(entry, index) in Object.entries(data.stats.most_common_entries).sort(
									(a, b) => {
										if (a[1] < b[1]) return -1;
										if (a[1] > b[1]) return 1;
										if (a[0] < b[0]) return -1;
										if (a[0] > b[0]) return 1;
										return 0;
									}
								)"
								:key="index"
							>
								{{ entry[0] }}<span class="caption subdued ml-1">({{ entry[1] }})</span
								>{{
									index !== Object.entries(data.stats.most_common_entries).length - 1 ? ',' : ''
								}}&nbsp;
							</span>
						</div>
					</div>
				</section>
			</template>
		</Column>
	</DataTable>
</template>

<script setup lang="ts">
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import type { Dataset, DatasetColumn, DKG } from '@/types/Types';
import { computed, ref } from 'vue';
import { cloneDeep, isEmpty } from 'lodash';
import {
	getCurieFromGroudingIdentifier,
	getCurieUrl,
	getNameOfCurieCached,
	parseCurie,
	searchCuriesEntities
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

function getBoxplotPartialWidth(n1: number, n2: number) {
	if (n2 === 0) return `width: 1%;`; // Prevent division by zero (n2 can be 0 if all values are the same)
	const width = n1 / n2;
	return `width: ${width * 100}%;`;
}
</script>

<style scoped>
.dataset-overview-table :deep(td:empty:before) {
	content: '--';
}

.p-datatable:deep(.p-datatable-thead > tr > th) {
	background-color: var(--surface-50);
}

.caption {
	font-size: var(--font-caption);
}
.subdued {
	color: var(--text-color-subdued);
}

.line {
	border-top: 2px solid var(--text-color-subdued);
}

.tnum {
	font-feature-settings: 'tnum';
}

.box-left {
	border-top: 2px solid var(--text-color-subdued);
	border-bottom: 2px solid var(--text-color-subdued);
	border-left: 2px solid var(--text-color-subdued);
	background-color: var(--surface-100);
	height: 18px;
}
.box-middle {
	border-top: 2px solid var(--text-color-subdued);
	border-bottom: 2px solid var(--text-color-subdued);
	background-color: var(--surface-100);
	width: 2px;
	height: 18px;
	text-align: center;
	position: relative;
}

.box-middle:before {
	content: '';
	position: absolute;
	top: 50%;
	left: 50%;
	width: 2px;
	height: 100%;
	background-color: var(--text-color-subdued);
	transform: translate(-50%, -50%);
}

.box-middle .centered-text {
	position: absolute;
	top: 50%;
	left: 50%;
	transform: translate(-50%, 65%);
	white-space: nowrap;
	color: var(--text-color-subdued);
}

.box-right {
	border-top: 2px solid var(--text-color-subdued);
	border-bottom: 2px solid var(--text-color-subdued);
	border-right: 2px solid var(--text-color-subdued);
	background-color: var(--surface-100);
	height: 18px;
}
</style>
