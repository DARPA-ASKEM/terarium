<template>
	<Accordion :multiple="true" :active-index="[0, 3]">
		<AccordionTab header="Mapping">
			<DataTable class="p-datatable-xsm" :value="openedWorkflowNodeStore.readOnlyMapping">
				<Column field="modelVariable" header="Model" />
				<Column field="datasetVariable" header="Dataset" />
			</DataTable>
		</AccordionTab>
		<AccordionTab header="Loss"></AccordionTab>
		<AccordionTab header="Parameters"></AccordionTab>
		<AccordionTab header="Variables">
			<tera-simulate-chart
				v-for="index in openedWorkflowNodeStore.calibrateNumCharts"
				:key="index"
				:run-results="openedWorkflowNodeStore.calibrateRunResults"
				:run-id-list="openedWorkflowNodeStore.calibrateRunIdList"
				:chart-idx="index"
			/>
		</AccordionTab>
	</Accordion>
</template>

<script setup lang="ts">
import { computed, shallowRef, watch } from 'vue';
import { downloadRawFile, getDataset } from '@/services/dataset';
import { WorkflowNode } from '@/types/workflow';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { useOpenedWorkflowNodeStore } from '@/stores/opened-workflow-node';
import { CsvAsset, Dataset } from '@/types/Types';
import TeraSimulateChart from './tera-simulate-chart.vue';
// import { ModelConfig } from '@/types/ModelConfig';
// import { calibrationParamExample } from '@/temp/calibrationExample';

const props = defineProps<{
	node: WorkflowNode;
}>();
// const modelConfig = computed(() => props.node.inputs[0].value?.[0] as ModelConfig | undefined);
const datasetId = computed(() => props.node.inputs[1].value?.[0] as number | undefined);
const openedWorkflowNodeStore = useOpenedWorkflowNodeStore();
// const runId = ref('');
// const datasetColumnNames = ref<string[]>();
const csvAsset = shallowRef<CsvAsset | undefined>(undefined);
// const datasetValue = ref();

// const emit = defineEmits(['append-output-port']);
// 			emit('append-output-port', {
// 				type: 'string',
// 				label: 'Calibration Job ID',
// 				value: runId.value
// 			});

watch(
	() => datasetId.value, // When dataset ID changes, update datasetColumnNames
	async () => {
		if (datasetId.value) {
			// Get dataset:
			const dataset: Dataset | null = await getDataset(datasetId.value.toString());
			// We are assuming here there is only a single csv file. This may change in the future as the API allows for it.
			csvAsset.value = (await downloadRawFile(
				datasetId.value.toString(),
				dataset?.fileNames?.[0] ?? ''
			)) as CsvAsset;
			// datasetColumnNames.value = csvAsset.value?.headers;
			// datasetValue.value = csvAsset.value?.csv.map((row) => row.join(',')).join('\n');
		}
	}
);
</script>

<style scoped>
.dropdown-button {
	width: 156px;
	height: 25px;
	border-radius: 6px;
}

:deep(.p-dropdown .p-dropdown-label.p-inputtext) {
	color: white;
}

:deep(.p-inputtext) {
	color: white;
}
</style>
