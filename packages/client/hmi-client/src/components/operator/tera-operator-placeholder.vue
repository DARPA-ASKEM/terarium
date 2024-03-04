<template>
	<div class="placeholder-container">
		<img :src="placeholderGraphic" alt="Plant icon" />
		<slot />
	</div>
</template>

<script setup lang="ts">
import { WorkflowOperationTypes } from '@/types/workflow';
import plants from '@/assets/svg/plants.svg';
import seed from '@/assets/svg/seed.svg';
import plantsSmallToBig from '@/assets/svg/plants-small-to-big.svg';
import plantAndRoot from '@/assets/svg/plant-and-root.svg';
import plantAndShovel from '@/assets/svg/plant-and-shovel.svg';
// import clipBamboo from '@/assets/svg/clip-bamboo.svg';
import pottedPlants from '@/assets/svg/potted-plants.svg';
// import talkingPlant from '@/assets/svg/talking-plant.svg'
// import pdfExtract from '@/assets/svg/pdf-extract.svg'
import trimmingPlant from '@assets/svg/trimming-plant.svg';
import equationsToModel from '@assets/svg/equations-to-model.svg';

const props = defineProps<{
	operationType: WorkflowOperationTypes;
}>();

const operatorGraphics = {
	[WorkflowOperationTypes.CALIBRATE_ENSEMBLE_CIEMSS]: seed,
	[WorkflowOperationTypes.CALIBRATION_CIEMSS]: seed,
	[WorkflowOperationTypes.CALIBRATION_JULIA]: seed,
	[WorkflowOperationTypes.MODEL_CONFIG]: plantAndRoot,
	[WorkflowOperationTypes.MODEL_COUPLING]: pottedPlants,
	[WorkflowOperationTypes.OPTIMIZE_CIEMSS]: trimmingPlant,
	[WorkflowOperationTypes.MODEL_TRANSFORMER]: trimmingPlant,
	[WorkflowOperationTypes.MODEL_EDIT]: plantAndShovel,
	[WorkflowOperationTypes.SIMULATE_CIEMSS]: plantsSmallToBig,
	[WorkflowOperationTypes.SIMULATE_ENSEMBLE_CIEMSS]: plantsSmallToBig,
	[WorkflowOperationTypes.SIMULATE_JULIA]: plantsSmallToBig,
	[WorkflowOperationTypes.MODEL_FROM_DOCUMENT]: equationsToModel
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
