<template>
	<section>
		<Dropdown
			class="w-full"
			:options="datasets"
			option-label="name"
			v-model="selectedDataset"
			placeholder="Select a dataset"
		>
		</Dropdown>
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
	</section>
</template>

<script setup lang="ts">
import Dropdown from 'primevue/dropdown';
import { computed, ref, watch } from 'vue';
import { downloadRawFile } from '@/services/dataset';
import { CsvAsset, Dataset } from '@/types/Types';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import { DatasetOperation } from './dataset-operation';

defineProps<{
	datasets: Dataset[];
}>();

const emit = defineEmits(['append-output-port']);

const selectedDataset = ref<Dataset | null>(null);
const rawContent = ref<CsvAsset | null>(null);
const csvContent = computed(() => rawContent.value?.csv);
const csvHeaders = computed(() => rawContent.value?.headers);

watch(
	() => selectedDataset.value,
	async () => {
		if (selectedDataset.value) {
			rawContent.value = await downloadRawFile(selectedDataset.value.id.toString(), 10);
			emit('append-output-port', {
				type: DatasetOperation.outputs[0].type,
				label: selectedDataset.value.name,
				value: selectedDataset.value.id
			});
		}
	}
);
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
</style>
