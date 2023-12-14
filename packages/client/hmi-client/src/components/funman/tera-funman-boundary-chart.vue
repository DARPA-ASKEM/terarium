<template>
	<div ref="boundaryRef"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import {
	FunmanProcessedData,
	RenderOptions,
	renderFunmanBoundaryChart
} from '@/services/models/funman-service';

const props = defineProps<{
	processedData: FunmanProcessedData;
	param1: string;
	param2: string;
	timestep: number;
	options?: RenderOptions; // Should be optional
}>();

const boundaryRef = ref();
const width = 60;
const height = 60;
const renderOptions = { width, height };

onMounted(async () => {
	renderFunmanBoundaryChart(
		boundaryRef.value as HTMLElement,
		props.processedData,
		props.param1,
		props.param2,
		props.timestep,
		renderOptions
	);
});

watch(
	() => [props.param1, props.param2],
	async () => {
		renderFunmanBoundaryChart(
			boundaryRef.value as HTMLElement,
			props.processedData,
			props.param1,
			props.param2,
			props.timestep,
			renderOptions
		);
	}
);
</script>
