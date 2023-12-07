<template>
	<div class="placeholder-graphic-container">
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
// import plantAndShovel from '@/assets/svg/plant-and-shovel.svg';
// import clipBamboo from '@/assets/svg/clip-bamboo.svg';
// import pottedPlants from '@/assets/svg/potted-plants.svg'
// import talkingPlant from '@/assets/svg/talking-plant.svg'
// import pdfExtract from '@/assets/svg/pdf-extract.svg'

const props = defineProps<{
	operationType: WorkflowOperationTypes;
}>();

const operatorGraphics = {
	[WorkflowOperationTypes.CALIBRATE_ENSEMBLE_CIEMSS]: seed,
	[WorkflowOperationTypes.CALIBRATION_CIEMSS]: seed,
	[WorkflowOperationTypes.CALIBRATION_JULIA]: seed,
	[WorkflowOperationTypes.MODEL_CONFIG]: plantAndRoot,
	[WorkflowOperationTypes.SIMULATE_CIEMSS]: plantsSmallToBig,
	[WorkflowOperationTypes.SIMULATE_ENSEMBLE_CIEMSS]: plantsSmallToBig,
	[WorkflowOperationTypes.SIMULATE_JULIA]: plantsSmallToBig
};

const placeholderGraphic = operatorGraphics[props.operationType] ?? plants;
</script>

<style scoped>
.placeholder-graphic-container {
	display: flex;
	flex-direction: column;
	justify-content: center;
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
	text-align: center;
	gap: 0.5rem;
}

img {
	max-width: 50%;
	margin: 0 auto;
}
</style>
