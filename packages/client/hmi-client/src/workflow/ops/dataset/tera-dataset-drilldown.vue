<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<section tabName="Description">
			<tera-drilldown-section :is-loading="fetchingDataset">
				<div>
					<tera-dataset-description :dataset="dataset" :raw-content="rawContent" />
				</div>
			</tera-drilldown-section>
		</section>
		<section tabName="Data">
			<tera-drilldown-section :is-loading="fetchingDataset">
				<div class="datatable-container">
					<tera-dataset-datatable :rows="100" :raw-content="rawContent" />
				</div>
			</tera-drilldown-section>
		</section>
	</tera-drilldown>
</template>

<script setup lang="ts">
// Proxy to use tera-dataset via a workflow context

import { WorkflowNode } from '@/types/workflow';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import { CsvAsset, Dataset } from '@/types/Types';
import { onMounted, ref } from 'vue';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import TeraDatasetDescription from '@/components/dataset/tera-dataset-description.vue';
import { downloadRawFile, getDataset } from '@/services/dataset';
import { enrichDataset } from '@/components/dataset/utils';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import { DatasetOperationState } from './dataset-operation';

const dataset = ref<Dataset | null>(null);
const rawContent = ref<CsvAsset | null>(null);
const fetchingDataset = ref(false);
const props = defineProps<{
	node: WorkflowNode<DatasetOperationState>;
}>();
const emit = defineEmits(['close']);

onMounted(async () => {
	fetchingDataset.value = true;
	await fetchDataset();
	fetchingDataset.value = false;
});

const fetchDataset = async () => {
	if (!props.node.state?.datasetId) return;
	const datasetTemp: Dataset | null = await getDataset(props.node.state.datasetId);

	// We are assuming here there is only a single csv file. This may change in the future as the API allows for it.
	rawContent.value = await downloadRawFile(
		props.node.state.datasetId,
		datasetTemp?.fileNames?.[0] ?? ''
	);
	if (datasetTemp) {
		dataset.value = enrichDataset(datasetTemp);
	}
};
</script>

<style scoped>
.datatable-container {
	display: flex;
	flex-direction: column;
	flex: 1;
	overflow: hidden;
}
</style>
