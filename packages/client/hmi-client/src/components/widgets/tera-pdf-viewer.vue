<template>
	<div class="pdf-viewer-container">
		<VuePDF :pdf="pdf" text-layer annotation-layer />
	</div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { VuePDF, usePDF } from '@tato30/vue-pdf';

const props = defineProps<{
	pdfLink?: string;
	title: string;
	filePromise?: Promise<ArrayBuffer | null>;
}>();

const { pdf } = usePDF(computed(() => props.pdfLink));
</script>

<style>
.pdf-viewer-container {
	position: relative;
	width: 100%;
	height: 100%;
}
.viewer-container {
	overflow: auto;
	position: absolute;
	width: 100%;
	height: 100%;
}
</style>
