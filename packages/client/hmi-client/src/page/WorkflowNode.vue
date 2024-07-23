<template>
	{{ activeProjectId }}
	<template v-if="node">
		<tera-model-workflow-wrapper v-if="isNodeOfOperationType(OperationType.MODEL)" :node="node" />
		<tera-stratify-mira v-else-if="isNodeOfOperationType(OperationType.STRATIFY_MIRA)" :node="node" />
		<tera-dataset-transformer v-else-if="isNodeOfOperationType(OperationType.DATASET_TRANSFORMER)" :node="node" />
		<tera-dataset-drilldown v-else-if="isNodeOfOperationType(OperationType.DATASET)" :node="node" />
		<tera-regridding-drilldown v-else-if="isNodeOfOperationType(OperationType.REGRIDDING)" :node="node" />
		<tera-calibrate-julia v-else-if="isNodeOfOperationType(OperationType.CALIBRATION_JULIA)" :node="node" />
		<tera-simulate-julia v-else-if="isNodeOfOperationType(OperationType.SIMULATE_JULIA)" :node="node" />
		<tera-calibrate-ciemss v-else-if="isNodeOfOperationType(OperationType.CALIBRATION_CIEMSS)" :node="node" />
		<tera-calibrate-ensemble-ciemss
			v-else-if="isNodeOfOperationType(OperationType.CALIBRATE_ENSEMBLE_CIEMSS)"
			:node="node"
		/>
		<tera-simulate-ciemss v-else-if="isNodeOfOperationType(OperationType.SIMULATE_CIEMSS)" :node="node" />
		<tera-simulate-ensemble-ciemss
			v-else-if="isNodeOfOperationType(OperationType.SIMULATE_ENSEMBLE_CIEMSS)"
			:node="node"
		/>
		<tera-funman v-else-if="isNodeOfOperationType(OperationType.FUNMAN)" :node="node" />
		<tera-code-asset-wrapper v-else-if="isNodeOfOperationType(OperationType.CODE)" :node="node" />
		<template v-else>
			<p>This OperationType is not know!</p>
			<pre>{{ node }}</pre>
		</template>
	</template>
	<p v-else>Loading...</p>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { Project } from '@/types/Types';
import { Workflow, WorkflowNode, WorkflowOperationTypes as OperationType } from '@/types/workflow';
import { getWorkflow } from '@/services/workflow';

import TeraModelWorkflowWrapper from '@/components/workflow/ops/model/tera-model-drilldown.vue';
import TeraDatasetDrilldown from '@/components/workflow/ops/dataset/tera-dataset-drilldown.vue';
import TeraRegriddingDrilldown from '@/components/workflow/ops/regridding/tera-regridding.vue';
import TeraDatasetTransformer from '@/components/workflow/ops/dataset-transformer/tera-dataset-transformer.vue';
import TeraCalibrateJulia from '@/components/workflow/ops/calibrate-julia/tera-calibrate-julia.vue';
import TeraSimulateJulia from '@/components/workflow/ops/simulate-julia/tera-simulate-julia.vue';
import TeraCalibrateCiemss from '@/components/workflow/ops/calibrate-ciemss/tera-calibrate-ciemss-drilldown.vue';
import TeraCalibrateEnsembleCiemss from '@/components/workflow/ops/calibrate-ensemble-ciemss/tera-calibrate-ensemble-ciemss.vue';
import TeraSimulateCiemss from '@/components/workflow/ops/simulate-ciemss/tera-simulate-ciemss.vue';
import TeraSimulateEnsembleCiemss from '@/components/workflow/ops/simulate-ensemble-ciemss/tera-simulate-ensemble-ciemss.vue';
import TeraFunman from '@/components/workflow/ops/funman/tera-funman.vue';
import teraStratifyMira from '@/components/workflow/ops/stratify-mira/tera-stratify-mira.vue';
import TeraCodeAssetWrapper from '@/components/workflow/ops/code-asset/tera-code-asset-wrapper.vue';
import { activeProjectId } from '../composables/activeProject';

const props = defineProps<{
	nodeId: WorkflowNode<any>['id'];
	projectId: Project['id'];
	workflowId: Workflow['id'];
}>();

const node = ref<WorkflowNode<any>>();

function isNodeOfOperationType(type: OperationType): boolean {
	return node.value?.operationType === type;
}

onMounted(async () => {
	const workflow = await getWorkflow(props.workflowId, props.projectId);
	const foundNode = workflow.nodes.find((n) => n.id === props.nodeId);
	if (foundNode) node.value = foundNode;
});
</script>
