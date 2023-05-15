<template>
	<div id="adobe-dc-view" />
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import API from '@/api/api';

const props = defineProps<{
	pdfLink: string;
	title: string;
}>();

const adobeDCView = ref();
const isAdobePdfApiReady = ref(false);
let apiKey = null;

onMounted(async () => {
	const apiKeyResponse = await API.get('/adobe');
	apiKey = apiKeyResponse.data;

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
				clientId: apiKey,
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
