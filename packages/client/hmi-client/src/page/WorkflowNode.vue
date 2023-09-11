<template>
	<tera-model-workflow-wrapper v-if="isNodeType(Operation.MODEL)" :node="node" />
	<tera-stratify v-else-if="isNodeType(Operation.STRATIFY)" :node="node" />
	<tera-dataset-transformer v-else-if="isNodeType(Operation.DATASET_TRANSFORMER)" :node="node" />
	<tera-dataset-workflow-wrapper v-else-if="isNodeType(Operation.DATASET)" :node="node" />
	<tera-calibration-julia v-else-if="isNodeType(Operation.CALIBRATION_JULIA)" :node="node" />
	<tera-simulate-julia v-else-if="isNodeType(Operation.SIMULATE_JULIA)" :node="node" />
	<tera-calibration-ciemss v-else-if="isNodeType(Operation.CALIBRATION_CIEMSS)" :node="node" />
	<tera-calibrate-ensemble-ciemss
		v-else-if="isNodeType(Operation.CALIBRATE_ENSEMBLE_CIEMSS)"
		:node="node"
	/>
	<tera-simulate-ciemss v-else-if="isNodeType(Operation.SIMULATE_CIEMSS)" :node="node" />
	<tera-simulate-ensemble-ciemss
		v-else-if="isNodeType(Operation.SIMULATE_ENSEMBLE_CIEMSS)"
		:node="node"
	/>
	<template v-else>{{ node }}</template>
</template>

<script setup lang="ts">
import { watch, ref } from 'vue';
import { WorkflowNode, WorkflowOperationTypes as Operation } from '@/types/workflow';
import TeraModelWorkflowWrapper from '@/components/workflow/tera-model-workflow-wrapper.vue';
import TeraDatasetWorkflowWrapper from '@/components/workflow/tera-dataset-workflow-wrapper.vue';
import TeraDatasetTransformer from '@/components/workflow/tera-dataset-transformer.vue';
import TeraCalibrationJulia from '@/components/workflow/tera-calibration-julia.vue';
import TeraSimulateJulia from '@/components/workflow/tera-simulate-julia.vue';
import TeraCalibrationCiemss from '@/components/workflow/tera-calibration-ciemss.vue';
import TeraCalibrateEnsembleCiemss from '@/components/workflow/tera-calibrate-ensemble-ciemss.vue';
import TeraSimulateCiemss from '@/components/workflow/tera-simulate-ciemss.vue';
import TeraSimulateEnsembleCiemss from '@/components/workflow/tera-simulate-ensemble-ciemss.vue';
import TeraStratify from '@/components/workflow/tera-stratify.vue';
import * as workflowService from '@/services/workflow';

const props = defineProps<{ nodeId: string; workflowId: string }>();

const node = ref<WorkflowNode>();

function isNodeType(type: Operation): boolean {
	return node.value?.operationType === type;
}

async function updateNode() {
	const workflow = await workflowService.getWorkflow(props.workflowId);
	const foundNode = workflow.nodes.find((n) => n.id === props.nodeId);
	if (foundNode) node.value = foundNode;
}

watch(
	() => props.nodeId,
	() => updateNode(),
	{ immediate: true }
);
</script>
