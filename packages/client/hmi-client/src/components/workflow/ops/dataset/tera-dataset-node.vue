<template>
	<main>
		<template v-if="dataset">
			<tera-operator-title>{{ dataset.name }}</tera-operator-title>
			<tera-show-more-text :text="formattedColumnList" :lines="5" />
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
		<tera-operator-status v-if="operatorStatus" :status="operatorStatus" />
	</main>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { isEmpty } from 'lodash';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';

import { AssetType, ClientEventType, type Dataset, type ProjectAsset } from '@/types/Types';
import { OperatorStatus, WorkflowNode } from '@/types/workflow';
import { getDataset } from '@/services/dataset';
import { canPropagateResource } from '@/services/workflow';
import TeraOperatorTitle from '@/components/operator/tera-operator-title.vue';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import TeraOperatorStatus from '@/components/operator/tera-operator-status.vue';
import TeraShowMoreText from '@/components/widgets/tera-show-more-text.vue';
import { useProjects } from '@/composables/project';
import { createEnrichClientEventHandler, useClientEvent } from '@/composables/useClientEvent';

import { DatasetOperationState } from './dataset-operation';

const props = defineProps<{
	node: WorkflowNode<DatasetOperationState>;
}>();

const emit = defineEmits(['append-output', 'open-drilldown', 'finished-job']);

const datasets = computed(() => useProjects().getActiveProjectAssets(AssetType.Dataset));
const dataset = ref<Dataset | null>(null);
const operatorStatus = ref<OperatorStatus>();

useClientEvent(
	ClientEventType.TaskGollmEnrichDataset,
	createEnrichClientEventHandler(operatorStatus, props.node.state.datasetId, emit)
);

async function getDatasetById(id: string) {
	dataset.value = await getDataset(id);

	if (dataset.value && dataset.value?.id) {
		// Once a dataset is selected the output is assigned here,
		const outputs = props.node.outputs;
		if (canPropagateResource(outputs)) {
			emit(
				'append-output',
				{
					type: 'datasetId',
					label: dataset.value.name,
					value: [dataset.value.id]
				},
				{
					datasetId: dataset.value.id
				}
			);
		}
	}
}

function onDatasetChange(chosenProjectDataset: ProjectAsset) {
	getDatasetById(chosenProjectDataset.assetId).then();
}

onMounted(async () => {
	if (props.node.state.datasetId) getDatasetById(props.node.state.datasetId).then();
});

const formattedColumnList = computed(
	() =>
		dataset.value?.columns
			?.filter((column) => !isEmpty(column.name))
			.map((column) => column.name)
			.join(', ') ?? ''
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
</style>
