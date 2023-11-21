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
