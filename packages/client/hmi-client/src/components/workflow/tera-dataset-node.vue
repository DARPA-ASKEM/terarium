<template>
	<template v-if="dataset">
		<h5>{{ dataset.name }}</h5>
		<Accordion>
			<AccordionTab header="Data preview">
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
			</AccordionTab>
		</Accordion>
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
import { DatasetOperation } from './dataset-operation';

const props = defineProps<{
	datasetId: string | null;
	datasets: Dataset[];
}>();

const emit = defineEmits(['append-output-port']);

const dataset = ref<Dataset | null>(null);
const rawContent = ref<CsvAsset | null>(null);
const csvContent = computed(() => rawContent.value?.csv);
const csvHeaders = computed(() => rawContent.value?.headers);

watch(
	() => dataset.value,
	async () => {
		if (dataset?.value?.id) {
			rawContent.value = await downloadRawFile(dataset.value.id.toString(), 10);
			emit('append-output-port', {
				type: DatasetOperation.outputs[0].type,
				label: dataset.value.name,
				value: dataset.value.id
			});
		}
	}
);

onMounted(async () => {
	if (props.datasetId) {
		dataset.value = await getDataset(props.datasetId);
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

span {
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
}

.p-button-sm.p-button-outlined {
	border: 1px solid var(--surface-border);
}
.p-button-sm.p-button-outlined:hover {
	border: 1px solid var(--surface-border-hover);
}
</style>
