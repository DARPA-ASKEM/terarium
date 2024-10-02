<template>
	<template v-if="node">
		<tera-model-workflow-wrapper v-if="isNodeType(OperationType.MODEL)" :node="node" />
		<tera-stratify-mira v-else-if="isNodeType(OperationType.STRATIFY_MIRA)" :node="node" />
		<tera-dataset-transformer v-else-if="isNodeType(OperationType.DATASET_TRANSFORMER)" :node="node" />
		<tera-dataset-drilldown v-else-if="isNodeType(OperationType.DATASET)" :node="node" />
		<tera-regridding-drilldown v-else-if="isNodeType(OperationType.REGRIDDING)" :node="node" />
		<tera-calibrate-ciemss v-else-if="isNodeType(OperationType.CALIBRATION_CIEMSS)" :node="node" />
		<tera-calibrate-ensemble-ciemss-drilldown
			v-else-if="isNodeType(OperationType.CALIBRATE_ENSEMBLE_CIEMSS)"
			:node="node"
		/>
		<tera-simulate-ciemss-drilldown v-else-if="isNodeType(OperationType.SIMULATE_CIEMSS)" :node="node" />
		<tera-simulate-ensemble-ciemss-drilldown
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
import TeraModelWorkflowWrapper from '@/components/workflow/ops/model/tera-model-drilldown.vue';
import TeraDatasetDrilldown from '@/components/workflow/ops/dataset/tera-dataset-drilldown.vue';
import TeraRegriddingDrilldown from '@/components/workflow/ops/regridding/tera-regridding.vue';
import TeraDatasetTransformer from '@/components/workflow/ops/dataset-transformer/tera-dataset-transformer.vue';
import TeraCalibrateCiemss from '@/components/workflow/ops/calibrate-ciemss/tera-calibrate-ciemss-drilldown.vue';
import TeraCalibrateEnsembleCiemssDrilldown from '@/components/workflow/ops/calibrate-ensemble-ciemss/tera-calibrate-ensemble-ciemss-drilldown.vue';
import TeraSimulateCiemssDrilldown from '@/components/workflow/ops/simulate-ciemss/tera-simulate-ciemss-drilldown.vue';
import TeraSimulateEnsembleCiemssDrilldown from '@/components/workflow/ops/simulate-ensemble-ciemss/tera-simulate-ensemble-ciemss-drilldown.vue';
import TeraFunman from '@/components/workflow/ops/funman/tera-funman-drilldown.vue';
import TeraStratifyMira from '@/components/workflow/ops/stratify-mira/tera-stratify-drilldown.vue';
import TeraCodeAssetWrapper from '@/components/workflow/ops/code-asset/tera-code-asset-wrapper.vue';

const props = defineProps<{ nodeId: string; workflowId: string }>();

const node = ref<WorkflowNode<any>>();

function isNodeType(type: string): boolean {
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
