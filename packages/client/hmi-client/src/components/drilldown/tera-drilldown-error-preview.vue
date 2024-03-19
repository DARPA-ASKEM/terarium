<template>
	<div class="container">
		{{ props.name }}
		{{ props.value }}
		{{ props.traceback }}
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
