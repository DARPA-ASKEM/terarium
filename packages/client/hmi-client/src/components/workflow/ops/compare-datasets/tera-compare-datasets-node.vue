<template>
	<section>
		<tera-operator-placeholder :node="node">Attach datasets/simulation outputs to compare</tera-operator-placeholder>
		<Button v-if="hasAtLeastTwoValues" label="Open" @click="emit('open-drilldown')" severity="secondary" outlined />
	</section>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue';
import { type WorkflowNode, WorkflowPortStatus } from '@/types/workflow';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import Button from 'primevue/button';
import { CompareDatasetsState } from './compare-datasets-operation';

const props = defineProps<{
	node: WorkflowNode<CompareDatasetsState>;
}>();

const emit = defineEmits(['append-input-port', 'open-drilldown']);

const hasAtLeastTwoValues = computed(() => props.node.inputs.filter((input) => input.value).length >= 2);

watch(
	() => props.node.inputs,
	() => {
		if (props.node.inputs.every((input) => input.status === WorkflowPortStatus.CONNECTED)) {
			emit('append-input-port', { type: 'datasetId', label: 'Dataset or Simulation result' });
		}
	},
	{ deep: true }
);
</script>
