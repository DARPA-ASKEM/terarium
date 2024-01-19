<template>
	<template v-if="node">
		<tera-model-workflow-wrapper v-if="isNodeType(OperationType.Model)" :node="node" />
		<tera-stratify-mira v-else-if="isNodeType(OperationType.StratifyMira)" :node="node" />
		<tera-dataset-transformer
			v-else-if="isNodeType(OperationType.DatasetTransformer)"
			:node="node"
		/>
		<tera-dataset-drilldown v-else-if="isNodeType(OperationType.Dataset)" :node="node" />
		<tera-calibrate-julia v-else-if="isNodeType(OperationType.CalibrationJulia)" :node="node" />
		<tera-simulate-julia v-else-if="isNodeType(OperationType.SimulateJulia)" :node="node" />
		<tera-calibrate-ciemss v-else-if="isNodeType(OperationType.CalibrationCiemss)" :node="node" />
		<tera-calibrate-ensemble-ciemss
			v-else-if="isNodeType(OperationType.CalibrateEnsembleCiemss)"
			:node="node"
		/>
		<tera-simulate-ciemss v-else-if="isNodeType(OperationType.SimulateCiemss)" :node="node" />
		<tera-simulate-ensemble-ciemss
			v-else-if="isNodeType(OperationType.SimulateEnsembleCiemss)"
			:node="node"
		/>
		<tera-funman v-else-if="isNodeType(OperationType.Funman)" :node="node" />
		<tera-code-asset-wrapper v-else-if="isNodeType(OperationType.Code)" :node="node" />
	</template>
	<template v-else>{{ node }}</template>
</template>

<script setup lang="ts">
import { watch, ref } from 'vue';
import { WorkflowOperationTypes as OperationType } from '@/types/Types';
import type { WorkflowNode } from '@/types/Types';
import * as workflowService from '@/services/workflow';

import TeraModelWorkflowWrapper from '@/workflow/ops/model/tera-model-workflow-wrapper.vue';
import TeraDatasetDrilldown from '@/workflow/ops/dataset/tera-dataset-drilldown.vue';
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
