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
	// @ts-ignore
	// eslint-disable-line
	if (window.AdobeDC) {
		isAdobePdfApiReady.value = true;
	} else {
		// Fallback in the case library isn't loaded
		document.addEventListener('adobe_dc_view_sdk.ready', () => {
			isAdobePdfApiReady.value = true;
		});
	}
});

watch(isAdobePdfApiReady, () => {
	if (isAdobePdfApiReady.value) {
		adobeDCView.value = Object.freeze(
			// @ts-ignore
			// eslint-disable-line
			new window.AdobeDC.View({
				clientId: '0cac1f8fd97c4957b968ebf6a5252223',
				divId: 'adobe-dc-view'
			})
		);

		adobeDCView.value.previewFile({
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
