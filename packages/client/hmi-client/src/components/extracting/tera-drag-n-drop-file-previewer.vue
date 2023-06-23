<template>
	<div class="file-title-container">
		<div class="file-name-container">
			<p class="file-name">
				{{ props.file?.name }}
			</p>
		</div>
		<div>
			<Button
				@click="emit('remove-file')"
				icon="pi pi-times"
				class="p-button-rounded p-button-text p-button-sm"
				style="max-width: 10px; max-height: 10px"
			/>
		</div>
	</div>
	<div class="file-preview" scrolling="no">
		<template v-if="props.isProcessing">
			<p class="progress-message">Uploading...</p>
			<div class="card">
				<ProgressBar :value="props.progress"></ProgressBar>
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
import { watch } from 'vue';
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
	},
	progress: {
		type: Number,
		default: undefined
	}
});

const getSrc = (file) => URL.createObjectURL(file);

const emit = defineEmits(['remove-file']);

watch(
	() => props.isProcessing,
	() => props.progress
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
	font-size: var(--font-body-small);
	color: var(--text-color-primary);
	border: none;
}

.file-name-container {
	display: flex;
	gap: 1rem;
	flex-direction: row;
}

.progress-message {
	font-size: var(--font-caption);
	color: var(--text-color-secondary);
	margin-top: 0.5rem;
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
