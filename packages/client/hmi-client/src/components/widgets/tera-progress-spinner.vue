<template>
	<section :class="{ center: isCentered }">
		<i class="pi pi-spin pi-spinner" :style="{ fontSize: fontSize + 'rem' }"></i>
		<p><slot /></p>
	</section>
</template>

<script setup lang="ts">
defineProps({
	fontSize: {
		type: Number,
		default: 1
	},
	isCentered: {
		type: Boolean,
		default: false
	}
});
</script>

<style scoped>
.center {
	display: flex;
	flex: 1;
	justify-content: center;
	align-items: center;
	flex-direction: column;
}

i {
	color: var(--primary-color);
}
</style>
