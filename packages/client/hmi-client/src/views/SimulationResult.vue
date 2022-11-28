<script setup lang="ts">
import { ref } from 'vue';
import ResponsiveMatrix from '@/components/responsive-matrix/matrix.vue';

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

const fillColorFn = (datum: any, _paramsMin: any, paramsMax: any): String => {
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
	<div>hello</div>
	<div class="result-container">
		<ResponsiveMatrix :data="data" :fillColorFn="fillColorFn" :style="{ flex: '1' }" />
	</div>
</template>

<style scoped>
.result-container {
	display: flex;
	flex-direction: row;
}
</style>
