<template>
	<div class="container">
		<tera-pdf-viewer v-if="pdfLink" :pdf-link="pdfLink" :annotations="annotations" :current-page="currentPage" />
		<div class="annotation-buttons">
			<button @click="annotate1">Annotation1</button>
			<button @click="annotate2">Annotation2</button>
		</div>
	</div>
</template>

<script setup lang="ts">
import teraPdfViewer, { PDFAnnotation } from '@/components/widgets/tera-pdf-viewer.vue';
import { downloadDocumentAsset, getDocumentAsset } from '@/services/document-assets';
import { ref, onMounted } from 'vue';

const pdfLink = ref<string | null>(null);
const currentPage = ref({ page: 1 });

const projectId = '979609fd-bad2-4aa2-9484-1bc4785b41ba'; // Terarium project id
const pdfAssetId = '8b51a0d7-d79e-497d-aa9b-1efd7e52b459'; // PDF document asset id for  SIR paper 1.pdf

onMounted(async () => {
	const document = await getDocumentAsset(pdfAssetId, projectId);
	const filename = document?.fileNames?.[0];
	if (!filename) return;
	pdfLink.value = (await downloadDocumentAsset(pdfAssetId, filename)) ?? null;
});

const annotations = ref<PDFAnnotation[]>([]);

const annotate1 = () => {
	annotations.value = [
		{
			pageNo: 2,
			bbox: {
				left: 0.11865788307510504,
				top: 0.09922614743194214,
				right: 0.8991060144761026,
				bottom: 0.8504185619943215
			},
			color: 'red',
			isHighlight: false
		},
		{
			pageNo: 7,
			bbox: {
				left: 0.11895760928883273,
				top: 0.0502243495044119,
				right: 0.9060322224592958,
				bottom: 0.37100781510957914
			},
			color: 'red',
			isHighlight: false
		}
	];
	currentPage.value = { page: 2 };
};

const annotate2 = () => {
	annotations.value = [
		{
			pageNo: 7,
			bbox: {
				left: 0.11895760928883273,
				top: 0.0502243495044119,
				right: 0.9060322224592963,
				bottom: 0.37100781510957914
			},
			color: 'green',
			isHighlight: false
		},
		{
			pageNo: 7,
			bbox: {
				left: 0.15075629510074606,
				top: 0.05157719714964384,
				right: 0.2286251988310445,
				bottom: 0.06735391923990497
			},
			color: 'yellow',
			isHighlight: true
		},
		{
			pageNo: 7,
			bbox: {
				left: 0.14450419456813987,
				top: 0.24989073634204265,
				right: 0.22966217356992027,
				bottom: 0.26566745843230405
			},
			color: 'yellow',
			isHighlight: true
		},
		{
			pageNo: 7,
			bbox: {
				left: 0.13179831284058535,
				top: 0.27401187648456055,
				right: 0.2459293996599997,
				bottom: 0.29442755344418053
			},
			color: 'yellow',
			isHighlight: true
		},
		{
			pageNo: 7,
			bbox: {
				left: 0.11909243111303083,
				top: 0.47100712589073634,
				right: 0.939287348726026,
				bottom: 0.5605368171021378
			},
			color: 'lightpink',
			isHighlight: true
		}
	];

	currentPage.value = { page: 7 };
};
</script>

<style scoped>
.container {
	height: 100%;
	width: 100%;
	max-width: 1200px;
	margin: auto;
}
.annotation-buttons {
	position: absolute;
	top: 50px;
}
</style>
