<script setup lang="ts">
import { FacetBucket } from '@/types/common';
import { FacetTimeline } from '@uncharted.software/facets-core';
import { computed } from 'vue';
import useQueryStore from '@/stores/query';

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

<template>
	<facet-timeline :data="timelineData" @facet-element-updated="updateSelection">
		<!-- eslint-disable-next-line vue/no-deprecated-slot-attribute -->
		<div slot="header" class="header">
			<span class="facet-font">{{ label }}</span>
		</div>
		<!-- eslint-disable-next-line vue/no-deprecated-slot-attribute -->
		<div slot="footer" class="footer"></div>
	</facet-timeline>
</template>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.facet-font {
	font-family: 'Source Sans Pro', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
		'Helvetica Neue', Arial, sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol';
}

.header {
	padding: 6px 12px 5px;
}

.footer {
	min-height: 16px;
}
</style>
