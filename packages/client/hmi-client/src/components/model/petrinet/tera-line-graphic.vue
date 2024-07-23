<template>
	<div :id="graphicId" class="line-graphic-container">
		<svg>
			<line x1="0" :y1="parentHeight / 2" :x2="width" :y2="parentHeight / 2" stroke="black" />
			<rect
				v-if="distribution.type === DistributionType.Uniform"
				:x="rectX"
				:y="parentHeight / 2 - 2"
				:width="rectWidth"
				rx="2"
				height="4"
				stroke="gray"
				fill="gray"
			/>
			<circle v-else :cx="circlePoint" :cy="parentHeight / 2" r="4" stroke="black" fill="black" />
		</svg>
	</div>
</template>

<script setup lang="ts">
import * as d3 from 'd3';
import { onMounted, ref } from 'vue';
import { DistributionType } from '@/services/distribution';

const props = defineProps<{
	id: string;
	maxValue: number;
	minValue: number;
	distribution: any;
}>();
const graphicId = 'graphic-'.concat(props.id);
const parentWidth = ref<number>(0);
const parentHeight = ref<number>(0);
const width = ref('100%');
const rectWidth = ref(0);
const rectX = ref(0);
const circlePoint = ref(0);
const range = ref([1, 100]);

const max = props.maxValue;
const min = props.minValue;

const isUniformType = () => props.distribution.type === DistributionType.Uniform;

onMounted(() => {
	parentWidth.value = document.getElementById(graphicId)?.getBoundingClientRect().width ?? 0;
	parentHeight.value = document.getElementById(graphicId)?.getBoundingClientRect().height ?? 0;
	if (parentWidth.value) {
		range.value = [0, parentWidth.value];
	}

	const scale = d3.scaleLinear().domain([min, max]).range(range.value);

	if (!isUniformType()) {
		const maxScaled = scale(max);
		const cx = scale(props.distribution.parameters.value);
		circlePoint.value = cx > maxScaled ? maxScaled - 5 : cx;
	} else {
		const barMin = scale(props.distribution.parameters.minimum);
		const barMax = scale(props.distribution.parameters.maximum);
		rectWidth.value = barMax - barMin;
		rectX.value = barMin;
	}
});
</script>

<style scoped>
.line-graphic-container {
	width: 100%;
	height: 40px;
}
</style>
