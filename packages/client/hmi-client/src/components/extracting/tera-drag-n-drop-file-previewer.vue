<template>
	<div class="file-title-container">
		<div class="file-name-container">
			<p class="file-name">
				<b>{{ props.file?.name }}</b>
			</p>
		</div>
		<div>
			<Button
				@click="emit('remove-file')"
				icon="pi pi-times"
				class="p-button-square"
				style="max-width: 10px; max-height: 10px"
			/>
		</div>
	</div>
	<div class="file-preview" scrolling="no">
		<template v-if="props.isProcessing">
			<h1>Extracting...</h1>
			<div class="card">
				<ProgressBar :value="progress"></ProgressBar>
			</div>
		</template>
		<template v-else-if="props.showPreview">
			<template v-if="file && file.type === AcceptedTypes.PDF">
				<embed :src="getSrc(file)" />
			</template>
			<template v-else-if="file && imageTypes.includes(file.type?.toString())">
				<img :src="getSrc(file)" alt="" />
			</template>
		</template>
		<template v-else-if="props.showError">
			<div>Error Please Try Again</div>
		</template>
	</div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import Button from 'primevue/button';
import ProgressBar from 'primevue/progressbar';
import { AcceptedTypes } from '@/types/common';

const imageTypes: Array<String> = [AcceptedTypes.JPG, AcceptedTypes.PNG, AcceptedTypes.JPEG];

const props = defineProps({
	file: {
		type: File,
		default: undefined
	},
	showPreview: {
		type: Boolean,
		default: true,
		required: true
	},
	isProcessing: {
		type: Boolean,
		required: true
	},
	showError: {
		type: Boolean
	}
});

const progress = ref(0);
let progressUpdater;

const updateProgress = () => {
	if (progress.value < 98) {
		progress.value += 1;
	}
};

const getSrc = (file) => URL.createObjectURL(file);

const emit = defineEmits(['remove-file']);

watch(
	() => props.isProcessing,
	() => {
		if (props.isProcessing) {
			progress.value = 0;
			progressUpdater = setInterval(updateProgress, 50);
		} else {
			clearInterval(progressUpdater);
		}
	}
);
</script>
<style scoped>
.file-preview {
	overflow: hidden !important;
}

.file-title-container {
	display: flex;
	flex-direction: row;
}

.file-name {
	font-size: 12px;
	background-color: var(--primary-color-text);
	margin-left: 5px;
	margin-right: 5px;
}

.file-name-container {
	margin-bottom: 5px;
	flex-grow: 1;
	min-height: 22px;
}

.file-preview embed {
	width: 100%;
	height: 100%;
}

.file-preview embed html {
	background-color: var(--surface-border-warning);
	height: 100%;
}
</style>
