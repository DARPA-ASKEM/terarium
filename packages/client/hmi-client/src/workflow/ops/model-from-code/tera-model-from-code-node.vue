<template>
	<main>model from code node</main>
	<tera-operator-image-carousel :images="images" />
</template>

<script setup lang="ts">
import TeraOperatorImageCarousel from '@/workflow/operator/tera-operator-image-carousel.vue';

const images = [
	new URL('@/assets/svg/terarium-icon.svg', import.meta.url).href,
	new URL('@/assets/svg/uncharted-logo-official.svg', import.meta.url).href,
	new URL('@/assets/svg/terarium-logo.svg', import.meta.url).href
];
</script>
