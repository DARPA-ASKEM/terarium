<template>
	<ul>
		<li v-for="(resource, index) in resources" :key="index">
			{{ resource.name }}
		</li>
	</ul>
</template>

<script setup lang="ts">
export type Resource = {
	name: string;
	type: null;
};

defineProps<{
	resources: Resource[];
}>();
</script>

<style scoped>
ul {
	display: flex;
	flex-direction: column;
}

li {
	border: 1px solid var(--un-color-black-100);
}
</style>
