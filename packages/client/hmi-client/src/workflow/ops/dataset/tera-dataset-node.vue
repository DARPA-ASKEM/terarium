<template>
	<main>
		<template v-if="dataset">
			<tera-operator-title>{{ dataset.name }}</tera-operator-title>
			<section v-if="csvContent">
				<div class="toolbar">
					<span>{{ csvContent[0].length }} columns</span>
					<!--TODO: May want to turn this feather icon button into its own component-->
					<div class="multiselect-btn">
						<Button class="p-button-icon-only" rounded text @click="columnSelect.show()">
							<span>
								<vue-feather type="columns" size="1.25rem" />
							</span>
						</Button>
						<MultiSelect
							ref="columnSelect"
							:modelValue="selectedColumns"
							:options="csvHeaders"
							:show-toggle-all="false"
							@update:modelValue="onToggle"
							:maxSelectedLabels="1"
							placeholder="Select columns"
						/>
					</div>
				</div>
				<DataTable class="p-datatable-xsm" :value="csvContent.slice(1, 6)">
					<Column
						v-for="(colName, index) of selectedColumns"
						:key="index"
						:field="index.toString()"
						:header="colName"
					/>
				</DataTable>
				<span>1 - 5 of {{ csvContent.length }} rows</span>
			</section>
		</template>
		<template v-else>
			<Dropdown
				class="w-full p-button-sm p-button-outlined"
				:options="datasets"
				option-label="name"
				v-model="dataset"
				placeholder="Select a dataset"
			/>
			<slot name="placeholder-graphic" />
		</template>
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
import Button from 'primevue/button';
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

const columnSelect = ref();
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
	flex-direction: column;
	max-width: 400px;
	gap: 0.5rem;
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
}

.toolbar {
	display: flex;
	flex-direction: row;
	justify-content: space-between;
}

.toolbar > span {
	display: flex;
	align-items: center;
}

.multiselect-btn {
	display: flex;
	flex-direction: column;
}

.p-multiselect {
	visibility: hidden;
	width: 0;
	height: 0;
}

.p-datatable.p-datatable-xsm:deep(.p-datatable-wrapper) {
	scrollbar-width: thin;
	scrollbar-color: #d9d9d9;
}

:deep(::-webkit-scrollbar) {
	height: 6px;
	background-color: var(--surface-section);
	outline: 1px solid var(--surface-section);
}

:deep(::-webkit-scrollbar-thumb) {
	background-color: #d9d9d9;
	border-radius: 0.5rem;
}

.p-datatable.p-datatable-xsm:deep(table) {
	background-color: var(--surface-50);
	padding: 0.5rem;
	border: 1px solid var(--surface-border-light);
	margin-bottom: 0.5rem;
}

.p-datatable.p-datatable-xsm:deep(.p-datatable-tbody > tr > td),
.p-datatable.p-datatable-xsm:deep(.p-datatable-thead > tr > th) {
	padding: 0 0.5rem;
	background-color: var(--surface-50);
}

.p-datatable.p-datatable-xsm:deep(.p-datatable-tbody > tr > td:not(:first-child)),
.p-datatable.p-datatable-xsm:deep(.p-datatable-thead > tr > th:not(:first-child)) {
	border-left: 1px solid var(--surface-border-light);
}

.p-button:deep(span) {
	margin-top: 0.25rem;
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
