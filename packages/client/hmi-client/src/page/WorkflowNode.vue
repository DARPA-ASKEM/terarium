<template>
	<tera-model-workflow-wrapper
		v-if="workflowNode.operationType === WorkflowOperationTypes.MODEL"
		:project="project"
		:node="workflowNode"
	/>
	<tera-stratify
		v-if="workflowNode.operationType === WorkflowOperationTypes.STRATIFY"
		:node="workflowNode"
		:key="workflowNode.id"
		@open-asset="(asset) => openAssetFromSidebar(asset)"
	/>
	<tera-dataset-transformer
		v-if="workflowNode.operationType === WorkflowOperationTypes.DATASET_TRANSFORMER"
		:project="project"
		:node="workflowNode"
	/>
	<tera-dataset-workflow-wrapper
		v-if="workflowNode.operationType === WorkflowOperationTypes.DATASET"
		:project="project"
		:node="workflowNode"
	/>
	<tera-calibration-julia
		v-if="workflowNode.operationType === WorkflowOperationTypes.CALIBRATION_JULIA"
		:node="workflowNode"
	/>
	<tera-simulate-julia
		v-if="workflowNode.operationType === WorkflowOperationTypes.SIMULATE_JULIA"
		:node="workflowNode"
		:project="project"
	/>
	<tera-calibration-ciemss
		v-if="workflowNode.operationType === WorkflowOperationTypes.CALIBRATION_CIEMSS"
		:node="workflowNode"
	/>
	<tera-calibrate-ensemble-ciemss
		v-if="workflowNode.operationType === WorkflowOperationTypes.CALIBRATE_ENSEMBLE_CIEMSS"
		:node="workflowNode"
		:project="project"
	/>
	<tera-simulate-ciemss
		v-if="workflowNode.operationType === WorkflowOperationTypes.SIMULATE_CIEMSS"
		:node="workflowNode"
		:project="project"
	/>
	<tera-simulate-ensemble-ciemss
		v-if="workflowNode.operationType === WorkflowOperationTypes.SIMULATE_ENSEMBLE_CIEMSS"
		:node="workflowNode"
		:project="project"
	/>
</template>

<script setup lang="ts">
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
</script>
