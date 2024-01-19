<template>
	<div class="placeholder-container">
		<img :src="placeholderGraphic" alt="Plant icon" />
		<slot />
	</div>
</template>

<script setup lang="ts">
import { WorkflowOperationTypes } from '@/types/Types';
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
	[WorkflowOperationTypes.CalibrateEnsembleCiemss]: seed,
	[WorkflowOperationTypes.CalibrationCiemss]: seed,
	[WorkflowOperationTypes.CalibrationJulia]: seed,
	[WorkflowOperationTypes.ModelConfig]: plantAndRoot,
	[WorkflowOperationTypes.SimulateCiemss]: plantsSmallToBig,
	[WorkflowOperationTypes.SimulateEnsembleCiemss]: plantsSmallToBig,
	[WorkflowOperationTypes.SimulateJulia]: plantsSmallToBig
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
}

img {
	width: 50%;
	max-height: 8rem;
	margin: 0 auto;
	pointer-events: none;
}
</style>
