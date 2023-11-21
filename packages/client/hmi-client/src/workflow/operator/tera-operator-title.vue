<template>
	<h5 v-if="title">{{ title }}</h5>
</template>

<script setup lang="ts">
defineProps({
	title: {
		type: String,
		default: null
	}
});
</script>

<style scoped>
h5 {
	margin: 0.5rem;
	display: -webkit-box;
	-webkit-box-orient: vertical;
	text-overflow: ellipsis;
	overflow: hidden;
	-webkit-line-clamp: 3;
}
</style>
