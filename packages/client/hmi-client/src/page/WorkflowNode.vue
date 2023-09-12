<template>
	<template v-if="node">
		<tera-model-workflow-wrapper v-if="isNodeType(OperationType.MODEL)" :node="node" />
		<tera-stratify v-else-if="isNodeType(OperationType.STRATIFY)" :node="node" />
		<tera-dataset-transformer
			v-else-if="isNodeType(OperationType.DATASET_TRANSFORMER)"
			:node="node"
		/>
		<tera-dataset-workflow-wrapper v-else-if="isNodeType(OperationType.DATASET)" :node="node" />
		<tera-calibration-julia v-else-if="isNodeType(OperationType.CALIBRATION_JULIA)" :node="node" />
		<tera-simulate-julia v-else-if="isNodeType(OperationType.SIMULATE_JULIA)" :node="node" />
		<tera-calibration-ciemss
			v-else-if="isNodeType(OperationType.CALIBRATION_CIEMSS)"
			:node="node"
		/>
		<tera-calibrate-ensemble-ciemss
			v-else-if="isNodeType(OperationType.CALIBRATE_ENSEMBLE_CIEMSS)"
			:node="node"
		/>
		<tera-simulate-ciemss v-else-if="isNodeType(OperationType.SIMULATE_CIEMSS)" :node="node" />
		<tera-simulate-ensemble-ciemss
			v-else-if="isNodeType(OperationType.SIMULATE_ENSEMBLE_CIEMSS)"
			:node="node"
		/>
	</template>
	<template v-else>{{ node }}</template>
</template>

<script setup lang="ts">
import { watch, ref } from 'vue';
import { WorkflowNode, WorkflowOperationTypes as OperationType } from '@/types/workflow';
import * as workflowService from '@/services/workflow';
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

const props = defineProps<{ nodeId: string; workflowId: string }>();

const node = ref<WorkflowNode>();

function isNodeType(type: OperationType): boolean {
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
