<template>
	<div class="placeholder-container">
		<img :src="placeholderGraphic" alt="Plant icon" />
		<slot />
	</div>
</template>

<script setup lang="ts">
import { WorkflowOperationTypes } from '@/types/workflow';
import plants from '@/assets/svg/plants.svg';
import calibrateJulia from '@assets/svg/operator-images/calibrate-deterministic.svg';
import editModel from '@assets/svg/operator-images/edit-model.svg';
import calibrateEnsembleCiemss from '@assets/svg/operator-images/calibrate-ensemble-probabilistic.svg';
import model from '@assets/svg/operator-images/model.svg';
import optimizeModel from '@assets/svg/operator-images/optimize-model.svg';
import calibrateSimulateCiemss from '@assets/svg/operator-images/calibrate-simulate-probabilistic.svg';
import simulateJulia from '@assets/svg/operator-images/simulate-deterministic.svg';
import code from '@assets/svg/operator-images/code.svg';
import simulateEnsembleCiemss from '@assets/svg/operator-images/simulate-ensemble-probabilistic.svg';
import configureModel from '@assets/svg/operator-images/configure-model.svg';
import simulateProbabilistic from '@assets/svg/operator-images/simulate-probabilistic.svg';
import coupleModels from '@assets/svg/operator-images/couple-models.svg';
import stratifyModel from '@assets/svg/operator-images/stratify-model.svg';
import createModelFromCode from '@assets/svg/operator-images/create-model-from-code.svg';
import transformDataset from '@assets/svg/operator-images/transform-dataset.svg';
import createModelFromEquations from '@assets/svg/operator-images/create-model-from-equation.svg';
import transformModel from '@assets/svg/operator-images/transform-model.svg';
import dataset from '@assets/svg/operator-images/dataset.svg';
import document from '@assets/svg/operator-images/document.svg';

const props = defineProps<{
	operationType: WorkflowOperationTypes;
}>();

const operatorGraphics = {
	[WorkflowOperationTypes.CALIBRATE_ENSEMBLE_CIEMSS]: calibrateEnsembleCiemss,
	[WorkflowOperationTypes.CALIBRATION_CIEMSS]: calibrateSimulateCiemss,
	[WorkflowOperationTypes.CALIBRATION_JULIA]: calibrateJulia,
	[WorkflowOperationTypes.CODE]: code,
	[WorkflowOperationTypes.DATASET]: dataset,
	[WorkflowOperationTypes.DATASET_TRANSFORMER]: transformDataset,
	[WorkflowOperationTypes.DOCUMENT]: document,
	[WorkflowOperationTypes.MODEL]: model,
	[WorkflowOperationTypes.MODEL_CONFIG]: configureModel,
	[WorkflowOperationTypes.MODEL_COUPLING]: coupleModels,
	[WorkflowOperationTypes.MODEL_EDIT]: editModel,
	[WorkflowOperationTypes.MODEL_FROM_CODE]: createModelFromCode,
	[WorkflowOperationTypes.MODEL_FROM_EQUATIONS]: createModelFromEquations,
	[WorkflowOperationTypes.OPTIMIZE_CIEMSS]: optimizeModel,
	[WorkflowOperationTypes.MODEL_TRANSFORMER]: transformModel,
	[WorkflowOperationTypes.REGRIDDING]: transformDataset,
	[WorkflowOperationTypes.SIMULATE_CIEMSS]: simulateProbabilistic,
	[WorkflowOperationTypes.SIMULATE_ENSEMBLE_CIEMSS]: simulateEnsembleCiemss,
	[WorkflowOperationTypes.SIMULATE_JULIA]: simulateJulia,
	[WorkflowOperationTypes.STRATIFY_MIRA]: stratifyModel
};

const placeholderGraphic = operatorGraphics[props.operationType] ?? plants;
</script>

<style scoped>
.placeholder-container {
	display: flex;
	flex-direction: column;
	justify-content: center;
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
	text-align: center;
	gap: 0.5rem;
	padding: 0.5rem;
}

img {
	height: 5.5rem;
	max-height: 8rem;
	margin: 0 auto;
	pointer-events: none;
}
</style>
