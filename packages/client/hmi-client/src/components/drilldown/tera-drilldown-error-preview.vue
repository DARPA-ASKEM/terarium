<template>
	<div class="container">
		<h6>{{ props.name }}</h6>
		<p>{{ props.value }}</p>
		<p>{{ props.traceback }}</p>
	</div>
</template>

<script setup lang="ts">
const props = defineProps<{
	name: string;
	value: string;
	traceback?: string;
}>();
</script>

<style scoped>
.container {
	background-color: #ffdcdc;
}
</style>
