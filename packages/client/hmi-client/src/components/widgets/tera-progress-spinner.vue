<template>
	<i class="pi pi-spin pi-spinner" :style="{ fontSize: fontSize + 'rem' }"></i>
</template>

<script setup lang="ts">
defineProps({
	fontSize: {
		type: Number,
		default: 1
	}
});
</script>

<style scoped></style>
