<template>
	<section :class="{ center: isCentered }">
		<i class="pi pi-spin pi-spinner" :style="{ fontSize: fontSize + 'rem' }"></i>
		<p :class="{ inline: isInline }"><slot /></p>
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
	},
	isInline: {
		type: Boolean,
		default: false
	}
});
</script>

<style scoped>
p.inline {
	display: inline;
}

p:not(.inline) {
	margin-top: var(--gap-4);
}

.center {
	display: flex;
	flex: 1;
	justify-content: center;
	align-items: center;
	flex-direction: column;
	margin-left: auto;
	margin-right: auto;

	p {
		text-align: center;
	}
}

i {
	color: var(--primary-color);
}
</style>
