<template>
	<div class="container">
		<tera-pdf-viewer
			v-if="pdfLink"
			:pdf-link="pdfLink"
			:annotations="annotations"
			:current-page="currentPage"
			fit-to-width
		/>
	</div>
</template>

<script setup lang="ts">
import teraPdfViewer, { PdfAnnotation } from '@/components/widgets/tera-pdf-viewer.vue';
import { downloadDocumentAsset, getDocumentAsset } from '@/services/document-assets';
import { ref, onMounted } from 'vue';

const pdfLink = ref<string | null>(null);
const currentPage = ref(1);

const projectId = '979609fd-bad2-4aa2-9484-1bc4785b41ba'; // Terarium project id
const pdfAssetId = '8b51a0d7-d79e-497d-aa9b-1efd7e52b459'; // PDF document asset id for  SIR paper 1.pdf

onMounted(async () => {
	const document = await getDocumentAsset(pdfAssetId, projectId);
	const filename = document?.fileNames?.[0];
	if (!filename) return;
	pdfLink.value = (await downloadDocumentAsset(pdfAssetId, filename)) ?? null;
});

// TODO: bbox should dome with normalized from from the backend.
const normalizeBBox = (
	bbox: { l: number; t: number; r: number; b: number; coord_origin?: string },
	page: { width: number; height: number }
) => {
	if (bbox.coord_origin === 'TOPLEFT') {
		return {
			l: bbox.l / page.width,
			t: bbox.t / page.height,
			r: bbox.r / page.width,
			b: bbox.b / page.height
		};
	}
	// Else, BottomLeft
	return {
		l: bbox.l / page.width,
		t: 1 - bbox.t / page.height,
		r: bbox.r / page.width,
		b: 1 - bbox.b / page.height
	};
};
const annotations = ref<PdfAnnotation[]>([
	// BBox from extracted items from SIR paper 1.pdf
	{
		pageNo: 2,
		bbox: normalizeBBox(
			{
				l: 70.6014404296875,
				t: 758.4515838623047,
				r: 534.968078613281,
				b: 125.94757080078125
			},
			{ width: 595, height: 842 }
		),
		color: 'red',
		isHighlight: false
	},
	{
		pageNo: 7,
		bbox: normalizeBBox(
			{
				l: 70.77977752685547,
				t: 799.7110977172852,
				r: 539.089172363281,
				b: 529.6114196777344
			},
			{ width: 595, height: 842 }
		),
		color: 'red',
		isHighlight: false
	}
]);

onMounted(() => {
	// Go to page after 2 seconds
	setTimeout(() => {
		currentPage.value = 2;
	}, 2000);
	// update annotations after 5 seconds
	setTimeout(() => {
		annotations.value = [
			{
				pageNo: 7,
				bbox: normalizeBBox(
					{
						l: 70.77977752685547,
						t: 799.7110977172852,
						r: 539.0891723632812,
						b: 529.6114196777344,
						coord_origin: 'BOTTOMLEFT'
					},
					{ width: 595, height: 842 }
				),
				color: 'green',
				isHighlight: false
			},
			{
				pageNo: 7,
				bbox: normalizeBBox(
					{
						l: 89.6999955849439,
						t: 43.42800000000011,
						r: 136.03199330447148,
						b: 56.71199999999999,
						coord_origin: 'TOPLEFT'
					},
					{ width: 595, height: 842 }
				),
				color: 'yellow',
				isHighlight: true
			},
			{
				pageNo: 7,
				bbox: normalizeBBox(
					{
						l: 85.97999576804322,
						t: 210.4079999999999,
						r: 136.64899327410257,
						b: 223.692,
						coord_origin: 'TOPLEFT'
					},
					{ width: 595, height: 842 }
				),
				color: 'yellow',
				isHighlight: true
			},
			{
				pageNo: 7,
				bbox: normalizeBBox(
					{
						l: 78.41999614014829,
						t: 230.71799999999996,
						r: 146.3279927976998,
						b: 247.90800000000002,
						coord_origin: 'TOPLEFT'
					},
					{ width: 595, height: 842 }
				),
				color: 'yellow',
				isHighlight: true
			},
			{
				pageNo: 7,
				bbox: normalizeBBox(
					{
						l: 70.85999651225335,
						t: 445.412,
						r: 558.8759724919855,
						b: 370.028,
						coord_origin: 'BOTTOMLEFT'
					},
					{ width: 595, height: 842 }
				),
				color: 'lightpink',
				isHighlight: true
			}
		];
		currentPage.value = 7;
	}, 5000);
});
</script>

<style scoped>
.container {
	height: 100%;
	width: 50%;
	max-width: 800px;
	margin: auto;
}
</style>
