<template>
	<main>
		<template v-if="dataset">
			<tera-operator-title>{{ dataset.name }}</tera-operator-title>
			<section class="mb-2">
				{{ formattedColumnList }}
			</section>
			<!--
			  -- Hide the section for the moment as this is taking too much memory in the hmi-server
			<section v-if="csvContent">
				<div class="toolbar">
					<span>{{ csvContent[0].length }} columns</span>
					<div class="multiselect-btn">
						<Button
							class="p-button-icon-only"
							rounded
							text
							@click="columnSelect.show()"
							v-tooltip="columnSelectTooltip"
						>
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
							filter
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
			-->
			<Button label="Open" @click="emit('open-drilldown')" severity="secondary" outlined />
		</template>
		<template v-else>
			<Dropdown
				class="w-full p-dropdown-sm"
				:options="datasets"
				option-label="assetName"
				placeholder="Select a dataset"
				@update:model-value="onDatasetChange"
			/>
			<tera-operator-placeholder :node="node" />
		</template>
	</main>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { isEmpty } from 'lodash';
import { AssetType } from '@/types/Types';
import type { Dataset, ProjectAsset } from '@/types/Types';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import { getDataset } from '@/services/dataset';
import { canPropagateResource } from '@/services/workflow';
import { WorkflowNode } from '@/types/workflow';
import TeraOperatorTitle from '@/components/operator/tera-operator-title.vue';
import { useProjects } from '@/composables/project';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { DatasetOperationState } from './dataset-operation';

const props = defineProps<{
	node: WorkflowNode<DatasetOperationState>;
}>();

const emit = defineEmits(['append-output', 'update-state', 'open-drilldown']);

const datasets = computed(() => useProjects().getActiveProjectAssets(AssetType.Dataset));
const dataset = ref<Dataset | null>(null);

/* Hide the CSV preview for now as it is taking too much memory in the hmi-server */
/*
	const rawContent = ref<CsvAsset | null>(null);
	const csvHeaders = computed(() => rawContent.value?.headers);
	let selectedColumns = ref(csvHeaders?.value);
	const csvContent = computed(() => rawContent.value?.csv);
	const columnSelect = ref();
	const onToggle = (val) => {
		selectedColumns.value = csvHeaders?.value?.filter((col) => val.includes(col));
	};
	const columnSelectTooltip = 'Select columns to display';
*/

async function getDatasetById(id: string) {
	dataset.value = await getDataset(id);

	if (dataset.value && dataset.value?.id) {
		// Once a dataset is selected the output is assigned here,
		const outputs = props.node.outputs;
		if (canPropagateResource(outputs)) {
			emit('update-state', {
				datasetId: dataset.value.id
			});

			emit('append-output', {
				type: 'datasetId',
				label: dataset.value.name,
				value: [dataset.value.id]
			});
		}

		// Fetch the CSV file from the dataset for preview purposes
		/*
		if (dataset.value?.fileNames) {
			const filenames = dataset.value.fileNames;
			if (filenames.length > 0 && filenames[0].endsWith('.csv')) {
				downloadRawFile(id, filenames[0]).then((res) => {
					rawContent.value = res;
					selectedColumns = ref(csvHeaders?.value);
				});
			} else {
				console.debug('No CSV file found in dataset: ', dataset.value.name);
			}
		}
		*/
	}
}

function onDatasetChange(chosenProjectDataset: ProjectAsset) {
	getDatasetById(chosenProjectDataset.assetId).then();
}

onMounted(async () => {
	if (props.node.state.datasetId) getDatasetById(props.node.state.datasetId).then();
});

const formattedColumnList = computed(() =>
	dataset.value?.columns
		?.filter((column) => !isEmpty(column.name))
		.map((column) => column.name)
		.join(', ')
);
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

/* Hide the CSV preview for now as it is taking too much memory in the hmi-server */
/*
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
	white-space: nowrap;
	max-width: 10rem;
	overflow: hidden;
	text-overflow: ellipsis;
}

.p-datatable.p-datatable-xsm:deep(.p-datatable-tbody > tr > td:not(:first-child)),
.p-datatable.p-datatable-xsm:deep(.p-datatable-thead > tr > th:not(:first-child)) {
	border-left: 1px solid var(--surface-border-light);
}

.p-button:deep(span) {
	margin-top: 0.25rem;
}
*/
</style>
