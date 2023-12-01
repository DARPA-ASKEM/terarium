<template>
	<main>model from code node</main>
	<tera-operator-image-carousel :images="images" />
</template>

<script setup lang="ts">
import TeraOperatorImageCarousel from '@/workflow/operator/tera-operator-image-carousel.vue';
import terarium from '@/assets/svg/terarium-icon.svg';

const images = [
	new URL('@/assets/svg/terarium-icon.svg', import.meta.url).href,
	new URL('@/assets/svg/uncharted-logo-official.svg', import.meta.url).href,
	new URL('@/assets/svg/terarium-logo.svg', import.meta.url).href,
	terarium,
	'data:image/png;base64, iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg=='
];
</script>
