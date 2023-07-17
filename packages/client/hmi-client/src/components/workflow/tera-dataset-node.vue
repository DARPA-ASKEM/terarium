<template>
	<template v-if="dataset">
		<h5>{{ dataset.name }}</h5>
		<section v-if="csvContent">
			<span>{{ `${csvContent[0].length} columns | ${csvContent.length} rows` }} </span>
			<DataTable class="p-datatable-xsm" :value="csvContent.slice(1, 6)">
				<Column
					v-for="(colName, index) of csvHeaders"
					:key="index"
					:field="index.toString()"
					:header="colName"
				/>
			</DataTable>
			<span>Showing first 5 rows</span>
		</section>
	</template>
	<Dropdown
		v-else
		class="w-full p-button-sm p-button-outlined"
		:options="datasets"
		option-label="name"
		v-model="dataset"
		placeholder="Select a dataset"
	/>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue';
import { CsvAsset, Dataset } from '@/types/Types';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Dropdown from 'primevue/dropdown';
import { downloadRawFile, getDataset } from '@/services/dataset';
import { WorkflowNode } from '@/types/workflow';

const props = defineProps<{
	node: WorkflowNode;
	datasets: Dataset[];
	datasetId: null | string;
}>();

const emit = defineEmits(['select-dataset']);

const dataset = ref<Dataset | null>(null);
const rawContent = ref<CsvAsset | null>(null);
const csvContent = computed(() => rawContent.value?.csv);
const csvHeaders = computed(() => rawContent.value?.headers);

watch(
	() => dataset.value,
	async () => {
		if (dataset?.value?.id && dataset?.value?.fileNames && dataset?.value?.fileNames?.length > 0) {
			rawContent.value = await downloadRawFile(dataset.value.id, dataset.value?.fileNames[0] ?? '');
			emit('select-dataset', { id: dataset.value.id, name: dataset.value.name });
		}
	}
);

onMounted(async () => {
	if (props.node.state.datasetId) {
		dataset.value = await getDataset(props.node.state.datasetId);
	}

	// If dataset is drag and dropped from resource panel
	if (props.datasetId) await getDataset(props.datasetId);
});
</script>

<style scoped>
section {
	display: flex;
	justify-content: center;
	flex-direction: column;
	max-width: 400px;
}

.p-datatable-xsm {
	margin-top: 0.5rem;
	margin-bottom: 0.25rem;
}

span {
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
}

.p-button-sm.p-button-outlined {
	border: 1px solid var(--surface-border);
	padding-top: 0rem;
	padding-bottom: 0rem;
}

.p-button-sm.p-button-outlined:deep(.p-dropdown-label) {
	padding: 0.5rem;
}

.p-button-sm.p-button-outlined:hover {
	border: 1px solid var(--surface-border-hover);
}
</style>
