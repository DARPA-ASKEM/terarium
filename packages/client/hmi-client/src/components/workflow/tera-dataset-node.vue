<template>
	<section v-if="dataset">
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
import { computed, ref, onMounted } from 'vue';
import { CsvAsset, Dataset } from '@/types/Types';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import { downloadRawFile, getDataset } from '@/services/dataset';
import { DatasetOperation } from './dataset-operation';

const props = defineProps<{
	datasetId: string;
}>();

const emit = defineEmits(['append-output-port']);

const dataset = ref<Dataset | null>(null);
const rawContent = ref<CsvAsset | null>(null);
const csvContent = computed(() => rawContent.value?.csv);
const csvHeaders = computed(() => rawContent.value?.headers);

onMounted(async () => {
	dataset.value = await getDataset(props.datasetId);
	if (dataset.value) {
		rawContent.value = await downloadRawFile(props.datasetId, 10);
		emit('append-output-port', {
			type: DatasetOperation.outputs[0].type,
			label: dataset.value.name,
			value: props.datasetId
		});
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
</style>
