<script setup lang="ts">
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const props = defineProps({
	name: {
		type: String,
		// required: true,
		default: 'New tab'
	},
	index: {
		type: Number,
		// required: true,
		default: 0
	}
});
</script>

<template>
	<section class="outer-container">
		<header>
			<div>{{ name }}</div>
		</header>
		<section class="content">
			<slot></slot>
		</section>
	</section>
</template>

<style scoped>
div {
	display: inline-flex;
	padding: 0.5rem;
	border-top-left-radius: 1rem;
	border-top-right-radius: 1rem;
	background-color: white;
}

section {
	display: flex;
	flex-direction: column;
}

.outer-container {
	margin: 0.5rem;
}

.content {
	background-color: white;
}
</style>
