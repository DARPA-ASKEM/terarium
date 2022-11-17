<template>
	<section class='responsive-matrix-demo'>
		<p>Responsive Matrix Demo</p>
		<ResponsiveMatrix
			:data="data"
			:fillColorFn="fillColorFn"
			:style="{flex: '1'}"
		/>
	</section>
</template>

<script lang="ts">
	import ResponsiveMatrix from './responsive-matrix/matrix.vue';
	// import demoData from './ResponsiveMatrixDemoData.json';

	export default {
		components: {
			ResponsiveMatrix,
		},

		computed: {

		},

		data() {
			return {
				data: [
					[{S: 98, I: 2, R: 0}, {S: 50, I: 20, R: 30}, {S: 20, I: 30, R: 50}],
					[{S: 98, I: 2, R: 0}, {S: 70, I: 10, R: 20}, {S: 50, I: 10, R: 40}],
					[{S: 98, I: 2, R: 0}, {S: 20, I: 60, R: 20}, {S: 0, I: 30, R: 70}],
				],
				// cellLabelCol: Array(360).fill(0).map((v, i) => i / 2),
				// data: [
				// 	(demoData as any)[0]['kantor_0.0_1.0_1.0_1.0'].reduce((a, v) => {
				// 		a.push({S: v.S, I: v.I, R: v.R});
				// 		return a;
				// 	}, []),
				// 	(demoData as any)[1]['kantor_0.0_1.0_1.0_10.0'].reduce((a, v) => {
				// 		a.push({S: v.S, I: v.I, R: v.R});
				// 		return a;
				// 	}, []),
				// 	(demoData as any)[2]['kantor_0.0_1.0_10.0_1.0'].reduce((a, v) => {
				// 		a.push({S: v.S, I: v.I, R: v.R});
				// 		return a;
				// 	}, []),
				// 	(demoData as any)[3]['kantor_0.0_1.0_10.0_10.0'].reduce((a, v) => {
				// 		a.push({S: v.S, I: v.I, R: v.R});
				// 		return a;
				// 	}, []),
				// 	(demoData as any)[4]['kantor_1.0_1.0_1.0_1.0'].reduce((a, v) => {
				// 		a.push({S: v.S, I: v.I, R: v.R});
				// 		return a;
				// 	}, []),
				// 	(demoData as any)[5]['kantor_1.0_1.0_1.0_10.0'].reduce((a, v) => {
				// 		a.push({S: v.S, I: v.I, R: v.R});
				// 		return a;
				// 	}, []),
				// 	(demoData as any)[6]['kantor_1.0_1.0_10.0_1.0'].reduce((a, v) => {
				// 		a.push({S: v.S, I: v.I, R: v.R});
				// 		return a;
				// 	}, []),
				// 	(demoData as any)[6]['kantor_1.0_1.0_10.0_1.0'].reduce((a, v) => {
				// 		a.push({S: v.S, I: v.I, R: v.R});
				// 		return a;
				// 	}, []),
				// 	(demoData as any)[7]['kantor_1.0_1.0_10.0_10.0'].reduce((a, v) => {
				// 		a.push({S: v.S, I: v.I, R: v.R});
				// 		return a;
				// 	}, []),
				// 	(demoData as any)[8]['kantor_0.0_10.0_1.0_1.0'].reduce((a, v) => {
				// 		a.push({S: v.S, I: v.I, R: v.R});
				// 		return a;
				// 	}, []),
				// 	(demoData as any)[9]['kantor_0.0_10.0_1.0_10.0'].reduce((a, v) => {
				// 		a.push({S: v.S, I: v.I, R: v.R});
				// 		return a;
				// 	}, []),
				// 	(demoData as any)[10]['kantor_0.0_10.0_10.0_1.0'].reduce((a, v) => {
				// 		a.push({S: v.S, I: v.I, R: v.R});
				// 		return a;
				// 	}, []),
				// 	(demoData as any)[11]['kantor_0.0_10.0_10.0_10.0'].reduce((a, v) => {
				// 		a.push({S: v.S, I: v.I, R: v.R});
				// 		return a;
				// 	}, []),
				// 	(demoData as any)[12]['kantor_1.0_10.0_1.0_1.0'].reduce((a, v) => {
				// 		a.push({S: v.S, I: v.I, R: v.R});
				// 		return a;
				// 	}, []),
				// 	(demoData as any)[13]['kantor_1.0_10.0_1.0_10.0'].reduce((a, v) => {
				// 		a.push({S: v.S, I: v.I, R: v.R});
				// 		return a;
				// 	}, []),
				// 	(demoData as any)[14]['kantor_1.0_10.0_10.0_1.0'].reduce((a, v) => {
				// 		a.push({S: v.S, I: v.I, R: v.R});
				// 		return a;
				// 	}, []),
				// 	(demoData as any)[15]['kantor_1.0_10.0_10.0_10.0'].reduce((a, v) => {
				// 		a.push({S: v.S, I: v.I, R: v.R});
				// 		return a;
				// 	}, []),
				// ],
			}
		},

		methods: {
			fillColorFn(datum: any, paramsMin: any, paramsMax: any): String {
				// get value as fraction of min clamped to 0
				const x = Math.max(datum.I / paramsMax.I, 0);
				// apply gamma
				// x **= 0.1;

				return `#${(Math.round(x * 255)).toString(16).padStart(2, '0')}0000`;
			},
		},
	}
</script>

<style scoped>
	.responsive-matrix-demo {
		flex: 1;
		display: flex;
		flex-direction: column;
	}
</style>