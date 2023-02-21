<template>
	<facet-timeline :data="timelineData" @facet-element-updated="updateSelection">
		<div slot="header">{{ label }}</div>
	</facet-timeline>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { FacetTimeline } from '@uncharted.software/facets-core';
import useQueryStore from '@/stores/query';
import { FacetBucket } from '@/types/common';

export interface TimelineData {
	ratio: number;
	label: string;
}

const props = defineProps<{
	data: FacetBucket[];
	label?: string;
}>();

const query = useQueryStore();

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

function updateSelection(event) {
	const facet = event.currentTarget;
	if (event.detail.changedProperties.get('selection') !== undefined) {
		if (facet.selection) {
			const values = [props.data[facet.selection[0]].key];
			query.setSearchClause({ field: facet, values });
		}
	}
}
</script>

<style scoped>
.facet-pointer {
	cursor: pointer;
}

.facet-footer-container {
	min-height: 16px;
}
</style>
