<script setup lang="ts">
import { FacetBucket } from '@/types/common';
import { FacetTimeline } from '@uncharted.software/facets-core';
import { computed } from 'vue';

export interface TimelineData {
	ratio: number;
	label: string;
}

const props = defineProps<{
	data: FacetBucket[];
	label?: string;
}>();

const timelineData = computed(() => {
	const initialValue = 0;
	const totalValue = props.data.reduce(
		(accumulator, currentValue) => accumulator + currentValue.value,
		initialValue
	);
	const timeline = props.data.map((entry) => ({
		ratio: entry.value / totalValue,
		label: entry.key
	}));
	return timeline;
});
</script>

<template>
	<facet-timeline :data="timelineData">
		<!-- eslint-disable-next-line vue/no-deprecated-slot-attribute -->
		<div slot="header" class="header">
			<span>{{ label }}</span>
		</div>
		<!-- eslint-disable-next-line vue/no-deprecated-slot-attribute -->
		<div slot="footer" class="footer"></div>
	</facet-timeline>
</template>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.header {
	padding-left: 8px;
	font-weight: bold;
	font-size: medium;
}

.footer {
	min-height: 16px;
}
</style>
