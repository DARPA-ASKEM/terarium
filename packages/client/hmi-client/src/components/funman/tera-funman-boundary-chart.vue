<template>
	<div ref="boundaryRef"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import {
	getQueries,
	processFunman,
	RenderOptions,
	renderFunmanBoundaryChart
} from '@/services/models/funman-service';

const props = defineProps<{
	funModelId: string;
	param1: string;
	param2: string;
	timestep: number;
	options?: RenderOptions; // Should be optional
}>();

const boundaryRef = ref();
const width = 200;
const height = 100;
const renderOptions = { width, height };

onMounted(async () => {
	const funModel = await getQueries(props.funModelId);
	const processedData = processFunman(funModel);
	renderFunmanBoundaryChart(
		boundaryRef.value as HTMLElement,
		processedData,
		props.param1,
		props.param2,
		props.timestep,
		renderOptions
	);
});

watch(
	() => [props.param1, props.param2],
	async () => {
		const funModel = await getQueries(props.funModelId);
		const processedData = processFunman(funModel);
		renderFunmanBoundaryChart(
			boundaryRef.value as HTMLElement,
			processedData,
			props.param1,
			props.param2,
			props.timestep,
			renderOptions
		);
	}
);
</script>
