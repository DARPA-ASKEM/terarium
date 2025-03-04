<template>
	<div :id="id" />
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import API from '@/api/api';
import { v4 as uuidv4 } from 'uuid';

const props = defineProps<{
	pdfLink?: string;
	title: string;
	filePromise?: Promise<ArrayBuffer | null>;
}>();

const id = uuidv4();

const adobeDCView = ref();
const adobeApis = ref();
const isAdobePdfApiReady = ref(false);
let apiKey = null;

function goToPage(pageNumber) {
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
				divId: id
			})
		);

		if (props.pdfLink) {
			const pdfFileId = 'dummy-id';
			adobeDCView.value
				.previewFile(
					{
						content: {
							location: {
								url: props.pdfLink
							}
						},
						metaData: { id: pdfFileId, fileName: props.title }
					},
					{
						embedMode: 'FULL_WINDOW',
						showPrintPDF: true,
						showDownloadPDF: true,
						showAnnotationTools: true, // this must be true to use annotation ui fir public version
						enableAnnotationAPIs: true,
						includePDFAnnotations: false,
						viewMode: 'FIT_WIDTH'
					}
				)
				.then((viewer) => {
					viewer.getAPIs().then((apis) => {
						adobeApis.value = apis;
					});
					/// Experiment
					viewer.getAnnotationManager().then((annotationManager) => {
						const { x1, x2, y1, y2 } = {
							x1: 100,
							y1: 500,
							x2: 200,
							y2: 700
						};
						const padding = 5;
						const annotation = {
							'@context': ['https://www.w3.org/ns/anno.jsonld', 'https://comments.acrobat.com/ns/anno.jsonld'],
							type: 'Annotation',
							id: '079d66a4-5ec2-4703-ae9d-30ccbb1aa84c',
							bodyValue: "I can't hide this and annotation UI on the left side when using annotation :(",
							motivation: 'commenting',
							target: {
								source: pdfFileId,
								selector: {
									node: {
										index: 0
									},
									opacity: 1,
									subtype: 'shape',
									boundingBox: [x1 - padding, y1, x2, y2 + padding],
									inkList: [
										[x1, y1, x2, y1],
										[x1, y1, x1, y2],
										[x1, y2, x2, y2],
										[x2, y2, x2, y1]
									],
									strokeColor: '#ff0808',
									strokeWidth: 2,
									type: 'AdobeAnnoSelector'
								}
							},
							creator: {
								type: 'Person',
								name: 'Test User'
							},
							created: '2018-08-02T14:45:37Z',
							modified: '2020-01-20T07:54:10Z'
						};
						annotationManager
							.addAnnotations([annotation])
							.then(() => {
								console.log('Annotation added successfully.');
							})
							.catch((error) => {
								console.error('Error adding annotation:', error);
							});
					});
				})
				.catch((error) => {
					console.error('Error previewing file:', error);
				});
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
