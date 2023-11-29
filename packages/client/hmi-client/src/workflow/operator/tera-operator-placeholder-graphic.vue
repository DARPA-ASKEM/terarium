<template>
	<section>
		<img :src="placeholderGraphic" alt="Plant icon" />
	</section>
</template>

<script setup lang="ts">
import { WorkflowOperationTypes } from '@/types/workflow';
import plants from '@/assets/svg/plants.svg';
import seed from '@/assets/svg/seed.svg';

const props = defineProps<{
	operationType: WorkflowOperationTypes;
}>();

const operatorGraphics = {
	[WorkflowOperationTypes.CALIBRATE_ENSEMBLE_CIEMSS]: seed,
	[WorkflowOperationTypes.CALIBRATION_CIEMSS]: seed,
	[WorkflowOperationTypes.CALIBRATION_JULIA]: seed
};

const placeholderGraphic = operatorGraphics[props.operationType] ?? plants;
</script>

<style scoped>
section {
	display: flex;
	justify-content: center;
}

img {
	max-width: 50%;
}
</style>
