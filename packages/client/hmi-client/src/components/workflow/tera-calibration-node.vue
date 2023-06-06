<template>
	<Accordion :multiple="true" :active-index="[0, 1, 2, 3]">
		<AccordionTab header="Mapping"> </AccordionTab>
		<AccordionTab header="Loss"> </AccordionTab>
		<AccordionTab header="Parameters"> </AccordionTab>
		<AccordionTab header="Variables"> </AccordionTab>
	</Accordion>
</template>

<script setup lang="ts">
import { computed, ref, shallowRef, watch } from 'vue';
import { CsvAsset } from '@/types/Types';
import { ModelConfig } from '@/types/ModelConfig';
import { downloadRawFile } from '@/services/dataset';
import { WorkflowNode } from '@/types/workflow';
// import DataTable from 'primevue/datatable';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
// import { calibrationParamExample } from '@/temp/calibrationExample';

const props = defineProps<{
	node: WorkflowNode;
}>();
const modelConfig = computed(() => props.node.inputs[0].value?.[0] as ModelConfig | undefined);
const datasetId = computed(() => props.node.inputs[1].value?.[0] as number | undefined);

// const runId = ref('');
const datasetColumnNames = ref<string[]>();
const modelColumnNames = computed(() =>
	modelConfig.value?.model.content.S.map((state) => state.sname)
);
const csvAsset = shallowRef<CsvAsset | undefined>(undefined);
const datasetValue = ref();
const featureMap = ref();

// const emit = defineEmits(['append-output-port']);
// 			emit('append-output-port', {
// 				type: 'string',
// 				label: 'Calibration Job ID',
// 				value: runId.value
// 			});

watch(
	() => modelConfig.value,
	async () => {
		if (modelConfig.value) {
			// initialize featureMap
			featureMap.value = modelColumnNames.value.map((stateName) => ['', stateName]);
		}
	}
);

watch(
	() => datasetId.value, // When dataset ID changes, update datasetColumnNames
	async () => {
		if (datasetId.value) {
			// Get dataset:
			csvAsset.value = (await downloadRawFile(datasetId.value.toString())) as CsvAsset;
			datasetColumnNames.value = csvAsset.value?.headers;
			datasetValue.value = csvAsset.value?.csv.map((row) => row.join(',')).join('\n');
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
