<template>
	<div class="container">
		<h6>{{ props.name }}</h6>
		<p>{{ props.value }}</p>
		<code>{{ props.traceback }}</code>
	</div>
</template>

<script setup lang="ts">
const props = defineProps<{
	name?: string;
	value?: string;
	traceback?: string;
}>();
</script>

<style scoped>
.container {
	background-color: #ffdcdc;
	padding: var(--gap-small);
	border-radius: var(--border-radius);
}
</style>
