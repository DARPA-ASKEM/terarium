<script setup lang="ts">
import { FacetTimeline } from '@uncharted.software/facets-core';

export interface TimelineData {
	ratio: number;
	label: string;
}

defineProps<{
	data: TimelineData[];
	label?: string;
}>();
</script>

<template>
	<facet-timeline :data="data">
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
