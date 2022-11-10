<script setup lang="ts">
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const props = defineProps({
	id: {
		type: String,
		required: true
	}
});
</script>

<template>
	<section>
		<h3>Project Summary</h3>
	</section>
</template>

<style scoped>
section {
	background: white;
	display: flex;
	width: 100%;
	color: var(--un-color-body-text-secondary);
}

h3 {
	font: var(--un-font-h3);
	margin-left: 1rem;
	margin-top: 1rem;
}
</style>
