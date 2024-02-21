<template>
	<main>
		<tera-carousel>
			<img v-for="(image, index) in images" :src="image" alt="alt" :key="index" />
		</tera-carousel>
		<Button @click="emit('open-drilldown')" label="Edit" severity="secondary" outlined />
	</main>
</template>

<script setup lang="ts">
import TeraCarousel from '@/components/widgets/tera-carousel.vue';
import terarium from '@/assets/svg/terarium-icon.svg';
import Button from 'primevue/button';

const emit = defineEmits(['open-drilldown']);

const images = [
	new URL('@/assets/svg/terarium-icon.svg', import.meta.url).href,
	new URL('@/assets/svg/uncharted-logo-official.svg', import.meta.url).href,
	new URL('@/assets/svg/terarium-logo.svg', import.meta.url).href,
	terarium,
	'data:image/png;base64, iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg=='
];
</script>
