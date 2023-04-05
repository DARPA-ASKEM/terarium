<template>
	<div id="adobe-dc-view"></div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';

const props = defineProps<{
	pdfLink: string;
	title: string;
}>();

const adobeDCView = ref();
const isAdobePdfApiReady = ref(false);

onMounted(() => {
	if (window.AdobeDC) isAdobePdfApiReady.value = true;
});

watch(isAdobePdfApiReady, () => {
	if (isAdobePdfApiReady.value) {
		adobeDCView.value = Object.freeze(
			new window.AdobeDC.View({
				clientId: import.meta.env.VITE_ADOBE_API,
				divId: 'adobe-dc-view'
			})
		);

		adobeDCView.value?.previewFile({
			content: {
				location: {
					url: props.pdfLink
				}
			},
			metaData: { fileName: props.title }
		});
	}
});
</script>
