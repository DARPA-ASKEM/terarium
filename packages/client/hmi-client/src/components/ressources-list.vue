<template>
	<ul>
		<li v-for="(ressource, index) in ressources" :key="index">
			{{ ressource.name }}
		</li>
	</ul>
</template>

<script setup lang="ts">
export type Ressource = {
	name: string;
	type: '';
};

defineProps<{
	ressources: Ressource[];
}>();
</script>

<style scoped>
ul {
	display: flex;
	flex-direction: column;
}
</style>
