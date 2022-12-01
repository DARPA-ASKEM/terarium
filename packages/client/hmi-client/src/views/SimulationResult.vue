<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import ResponsiveMatrix from '@/components/responsive-matrix/matrix.vue';
import { CellData, ParamMinMax } from '@/types/ResponsiveMatrix';

const route = useRoute();

watch(
	() => route.params.simulationRunId,
	async (simulationRunId) => {
		if (!simulationRunId) return;

		// FIXME: siwtch to different simulation run result
		console.log('simulation run id changed to', simulationRunId);
	},
	{ immediate: true }
);

const data = ref<any>([
	[
		{ S: 98, I: 2, R: 0 },
		{ S: 50, I: 20, R: 30 },
		{ S: 20, I: 30, R: 50 }
	],
	[
		{ S: 98, I: 2, R: 0 },
		{ S: 70, I: 10, R: 20 },
		{ S: 50, I: 10, R: 40 }
	],
	[
		{ S: 98, I: 2, R: 0 },
		{ S: 20, I: 60, R: 20 },
		{ S: 0, I: 30, R: 70 }
	]
]);

const fillColorFn = (datum: CellData, _paramsMin: ParamMinMax, paramsMax: ParamMinMax): String => {
	// get value as fraction of min clamped to 0
	const v = Math.max(datum.I / paramsMax.I, 0);
	// apply gamma: https://en.wikipedia.org/wiki/Gamma_correction
	// v **= 0.1;
	return `#${Math.round(v * 255)
		.toString(16)
		.padStart(2, '0')}0000`;
};
</script>

<template>
	<section>
		<h3>Simulation Results</h3>
		<div class="result-container">
			<ResponsiveMatrix :data="data" :fillColorFn="fillColorFn" :style="{ flex: '1' }" />
		</div>
	</section>
</template>

<style scoped>
.result-container {
	width: 25rem;
	height: 25rem;
	display: flex;
	flex-direction: row;
}

h3 {
	font: var(--un-font-h3);
	margin-bottom: 10px;
}
</style>
