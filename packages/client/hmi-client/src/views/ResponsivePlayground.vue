<template>
	<section class="responsive-matrix-demo">
		<p>Responsive Matrix Demo</p>
		<ResponsiveMatrix :data="data" :fillColorFn="fillColorFn" :style="{ flex: '1' }" />
	</section>
</template>

<script lang="ts">
import ResponsiveMatrix from '@/components/responsive-matrix/matrix.vue';
// import demoData from './ResponsiveMatrixDemoData.json';

export default {
	components: {
		ResponsiveMatrix
	},

	computed: {},

	data() {
		return {
			data: [
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
			]
		};
	},

	methods: {
		fillColorFn(datum: any, _paramsMin: any, paramsMax: any): String {
			// get value as fraction of min clamped to 0
			const v = Math.max(datum.I / paramsMax.I, 0);
			// apply gamma: https://en.wikipedia.org/wiki/Gamma_correction
			// v **= 0.1;

			return `#${Math.round(v * 255)
				.toString(16)
				.padStart(2, '0')}0000`;
		}
	}
};
</script>

<style scoped>
section {
	flex: 1;
	display: flex;
	flex-direction: column;
}
</style>
