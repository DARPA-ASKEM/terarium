<template>
	<tera-categorical-facet class="only-body" :selected-data="facets" :base-data="facets" />
</template>

<script setup lang="ts">
import TeraCategoricalFacet from '@/page/data-explorer/components/facets/tera-categorical-facet.vue';

const facets = [
	{ key: 'Overall similarity', value: '75%' },
	{ key: 'Same', value: '40%' },
	{ key: 'Related', value: '40%' },
	{ key: 'Unique', value: '20%' }
];
</script>

<style scoped>
.only-body {
	&:deep(.facet-header-container),
	&:deep(.facet-footer-container) {
		display: none;
	}
}
</style>
