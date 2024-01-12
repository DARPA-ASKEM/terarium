<template>
	<template v-if="node">
		<tera-model-workflow-wrapper v-if="isNodeType(OperationType.MODEL)" :node="node" />
		<tera-stratify-mira v-else-if="isNodeType(OperationType.STRATIFY_MIRA)" :node="node" />
		<tera-dataset-transformer
			v-else-if="isNodeType(OperationType.DATASET_TRANSFORMER)"
			:node="node"
		/>
		<tera-dataset-workflow-wrapper v-else-if="isNodeType(OperationType.DATASET)" :node="node" />
		<tera-calibrate-julia v-else-if="isNodeType(OperationType.CALIBRATION_JULIA)" :node="node" />
		<tera-simulate-julia v-else-if="isNodeType(OperationType.SIMULATE_JULIA)" :node="node" />
		<tera-calibrate-ciemss v-else-if="isNodeType(OperationType.CALIBRATION_CIEMSS)" :node="node" />
		<tera-calibrate-ensemble-ciemss
			v-else-if="isNodeType(OperationType.CALIBRATE_ENSEMBLE_CIEMSS)"
			:node="node"
		/>
		<tera-simulate-ciemss v-else-if="isNodeType(OperationType.SIMULATE_CIEMSS)" :node="node" />
		<tera-simulate-ensemble-ciemss
			v-else-if="isNodeType(OperationType.SIMULATE_ENSEMBLE_CIEMSS)"
			:node="node"
		/>
		<tera-funman v-else-if="isNodeType(OperationType.FUNMAN)" :node="node" />
		<tera-code-asset-wrapper v-else-if="isNodeType(OperationType.CODE)" :node="node" />
	</template>
	<template v-else>{{ node }}</template>
</template>

<script setup lang="ts">
import { watch, ref } from 'vue';
import { WorkflowNode, WorkflowOperationTypes as OperationType } from '@/types/workflow';
import * as workflowService from '@/services/workflow';

import TeraModelWorkflowWrapper from '@/workflow/ops/model/tera-model-workflow-wrapper.vue';
import TeraDatasetWorkflowWrapper from '@/workflow/ops/dataset/tera-dataset-workflow-wrapper.vue';
import TeraDatasetTransformer from '@/workflow/ops/dataset-transformer/tera-dataset-transformer.vue';
import TeraCalibrateJulia from '@/workflow/ops/calibrate-julia/tera-calibrate-julia.vue';
import TeraSimulateJulia from '@/workflow/ops/simulate-julia/tera-simulate-julia.vue';
import TeraCalibrateCiemss from '@/workflow/ops/calibrate-ciemss/tera-calibrate-ciemss.vue';
import TeraCalibrateEnsembleCiemss from '@/workflow/ops/calibrate-ensemble-ciemss/tera-calibrate-ensemble-ciemss.vue';
import TeraSimulateCiemss from '@/workflow/ops/simulate-ciemss/tera-simulate-ciemss.vue';
import TeraSimulateEnsembleCiemss from '@/workflow/ops/simulate-ensemble-ciemss/tera-simulate-ensemble-ciemss.vue';
import TeraFunman from '@/workflow/ops/funman/tera-funman.vue';
import teraStratifyMira from '@/workflow/ops/stratify-mira/tera-stratify-mira.vue';
import TeraCodeAssetWrapper from '@/workflow/ops/code-asset/tera-code-asset-wrapper.vue';

const props = defineProps<{ nodeId: string; workflowId: string }>();

const node = ref<WorkflowNode<any>>();

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
