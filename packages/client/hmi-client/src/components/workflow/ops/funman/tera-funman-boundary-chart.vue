<template>
	<div ref="boundaryRef"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { ProcessedFunmanResult, RenderOptions, renderFunmanBoundaryChart } from '@/services/models/funman-service';

const props = defineProps<{
	processedData: ProcessedFunmanResult;
	param1: string;
	param2: string;
	timestep: number;
	selectedBoxId: string;
	options?: RenderOptions;
}>();

const boundaryRef = ref();
const width = 60;
const height = 60;
const renderOptions = props.options || { width, height };

onMounted(async () => {
	renderFunmanBoundaryChart(
		boundaryRef.value as HTMLElement,
		props.processedData,
		props.param1,
		props.param2,
		props.selectedBoxId,
		renderOptions
	);
});

watch(
	() => [props.param1, props.param2, props.selectedBoxId],
	async () => {
		renderFunmanBoundaryChart(
			boundaryRef.value as HTMLElement,
			props.processedData,
			props.param1,
			props.param2,
			props.selectedBoxId,
			renderOptions
		);
	}
);
</script>
