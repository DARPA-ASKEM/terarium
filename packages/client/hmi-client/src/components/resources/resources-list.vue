<script setup lang="ts">
import { Resource } from '@/types/Resource';
import ResourceCard from '@/components/resources/ResourceCard.vue';

defineProps<{
	resources: Resource[];
}>();
</script>

<template>
	<ul>
		<li v-for="(resource, index) in resources" :key="index">
			<ResourceCard :resource="resource" />
		</li>
	</ul>
</template>

<style scoped>
ul {
	display: grid;
	grid-template-columns: 1fr 1fr;
	list-style: none;
	gap: 1rem;
	margin: 1rem 0;
	color: var(--un-color-body-text-secondary);
	width: fit-content;
}
</style>
