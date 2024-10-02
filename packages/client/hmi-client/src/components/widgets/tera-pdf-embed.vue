<template>
	<div id="adobe-dc-view" />
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import API from '@/api/api';

const props = defineProps<{
	pdfLink?: string;
	title: string;
	filePromise?: Promise<ArrayBuffer | null>;
}>();

const adobeDCView = ref();
const adobeApis = ref();
const isAdobePdfApiReady = ref(false);
let apiKey = null;

function goToPage(pageNumber) {
	console.log('goToPage', pageNumber);
	if (!adobeApis.value) return;
	adobeApis.value.gotoLocation(pageNumber, 0, 0);
}

defineExpose({
	goToPage
});

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

		if (props.pdfLink) {
			adobeDCView.value
				.previewFile(
					{
						content: {
							location: {
								url: props.pdfLink
							}
						},
						metaData: { fileName: props.title }
					},
					{
						embedMode: 'FULL_WINDOW',
						showPrintPDF: true,
						showDownloadPDF: true,
						showAnnotationTools: false,
						viewMode: 'FIT_WIDTH'
					}
				)
				.then((viewer) =>
					viewer.getAPIs().then((apis) => {
						adobeApis.value = apis;
					})
				);
		} else if (props.filePromise) {
			adobeDCView.value
				.previewFile(
					{
						content: {
							promise: props.filePromise
						},
						metaData: { fileName: props.title }
					},
					{
						embedMode: 'FULL_WINDOW',
						showPrintPDF: true,
						showDownloadPDF: true,
						showAnnotationTools: false,
						viewMode: 'FIT_WIDTH'
					}
				)
				.then((viewer) =>
					viewer.getAPIs().then((apis) => {
						adobeApis.value = apis;
					})
				);
		}
	}
});
</script>
