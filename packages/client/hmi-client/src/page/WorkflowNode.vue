<template>
	<component :is="component" :node="node" />
</template>

<script setup lang="ts">
import { Component, computed, watch, ref } from 'vue';
import { WorkflowNode, WorkflowOperationTypes } from '@/types/workflow';
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

const props = defineProps<{ nodeId: string }>();

const node: WorkflowNode = ref<WorkflowNode>(null);
const updateNode = async () => {
	node.value = await workflowService.getWorkflow().getNodeById(props.nodeId);
};

watch(
	() => props.nodeId,
	() => updateNode
);

const component: Component = computed<Component>(() => {
	switch (node.value.operationType) {
		case WorkflowOperationTypes.MODEL:
			return TeraModelWorkflowWrapper;
		case WorkflowOperationTypes.DATASET:
			return TeraDatasetWorkflowWrapper;
		case WorkflowOperationTypes.DATASET_TRANSFORMER:
			return TeraDatasetTransformer;
		case WorkflowOperationTypes.CALIBRATION_JULIA:
			return TeraCalibrationJulia;
		case WorkflowOperationTypes.SIMULATE_JULIA:
			return TeraSimulateJulia;
		case WorkflowOperationTypes.CALIBRATION_CIEMSS:
			return TeraCalibrationCiemss;
		case WorkflowOperationTypes.CALIBRATE_ENSEMBLE_CIEMSS:
			return TeraCalibrateEnsembleCiemss;
		case WorkflowOperationTypes.SIMULATE_CIEMSS:
			return TeraSimulateCiemss;
		case WorkflowOperationTypes.SIMULATE_ENSEMBLE_CIEMSS:
			return TeraSimulateEnsembleCiemss;
		case WorkflowOperationTypes.STRATIFY:
			return TeraStratify;
		default:
			return null;
	}
});
</script>
