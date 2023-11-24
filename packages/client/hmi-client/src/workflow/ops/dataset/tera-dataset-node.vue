<template>
	<main>
		<template v-if="dataset">
			<tera-operator-title>{{ dataset.name }}</tera-operator-title>
			<section v-if="csvContent">
				<div class="datatable-toolbar">
					<span class="datatable-toolbar-item"
						>{{ `${csvContent[0].length} columns | ${csvContent.length} rows` }}
					</span>
					<span class="datatable-toolbar-item">
						<MultiSelect
							:modelValue="selectedColumns"
							:options="csvHeaders"
							@update:modelValue="onToggle"
							:maxSelectedLabels="1"
							placeholder="Select columns"
						/>
					</span>
				</div>

				<DataTable class="p-datatable-xsm" :value="csvContent.slice(1, 6)">
					<Column
						v-for="(colName, index) of selectedColumns"
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
	</main>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { computed, ref, onMounted, watch } from 'vue';
import { CsvAsset, Dataset } from '@/types/Types';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Dropdown from 'primevue/dropdown';
import { downloadRawFile, getDataset } from '@/services/dataset';
import { WorkflowNode } from '@/types/workflow';
import MultiSelect from 'primevue/multiselect';
import TeraOperatorTitle from '@/workflow/operator/tera-operator-title.vue';
import { useProjects } from '@/composables/project';
import { DatasetOperationState } from './dataset-operation';

const props = defineProps<{
	node: WorkflowNode<DatasetOperationState>;
}>();

const emit = defineEmits(['append-output-port']);

const datasets = computed<Dataset[]>(
	() => useProjects().activeProject.value?.assets?.datasets ?? []
);

const dataset = ref<Dataset | null>(null);
const rawContent = ref<CsvAsset | null>(null);
const csvContent = computed(() => rawContent.value?.csv);
const csvHeaders = computed(() => rawContent.value?.headers);

let selectedColumns = ref(csvHeaders?.value);
const onToggle = (val) => {
	selectedColumns.value = csvHeaders?.value?.filter((col) => val.includes(col));
};

watch(
	() => dataset.value,
	async () => {
		if (dataset?.value?.id && dataset?.value?.fileNames && dataset?.value?.fileNames?.length > 0) {
			rawContent.value = await downloadRawFile(dataset.value.id, dataset.value?.fileNames[0] ?? '');
			selectedColumns = ref(csvHeaders?.value);

			// Once a dataset is selected the output is assigned here, if there is already an output do not reassign
			if (isEmpty(props.node.outputs)) {
				emit('append-output-port', {
					type: 'datasetId',
					label: dataset.value.name,
					value: [dataset.value.id]
				});
			}
		}
	}
);

onMounted(async () => {
	if (props.node.state.datasetId) {
		dataset.value = await getDataset(props.node.state.datasetId);
	}
});
</script>

<style scoped>
section {
	display: flex;
	justify-content: center;
	flex-direction: column;
	max-width: 400px;
}

.datatable-toolbar {
	display: flex;
	flex-direction: row;
	justify-content: space-between;
}
.datatable-toolbar-item {
	display: flex;
	flex-direction: row;
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
	align-items: center;
}
.datatable-toolbar:deep(.p-multiselect .p-multiselect-label) {
	padding: 0.5rem;
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
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
