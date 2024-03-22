<template>
	<Editor
		v-model="editorContent"
		class="h-full"
		:class="{ readonly: !hasEditPermission }"
		:readonly="!hasEditPermission"
	/>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import Editor from 'primevue/editor';
import { update } from '@/services/project';
import { useProjects } from '@/composables/project';

const AUTO_SAVE_DELAY = 3000;

const { activeProject, refresh } = useProjects();
const lastSavedContent = computed(() => atob(activeProject.value?.overviewContent ?? ''));
const hasEditPermission = computed(() =>
	['creator', 'writer'].includes(activeProject.value?.userPermission ?? '')
);
const editorContent = ref('');

let autoSaveIntervalId: number | null = null;
const startAutoSave = () => {
	autoSaveIntervalId = window.setInterval(saveContent, AUTO_SAVE_DELAY);
};
const stopAutoSave = () => {
	if (autoSaveIntervalId === null) return;
	window.clearInterval(autoSaveIntervalId);
	autoSaveIntervalId = null;
};

const isContentSameAsLastSaved = computed(
	() =>
		lastSavedContent.value.length === editorContent.value.length &&
		lastSavedContent.value === editorContent.value
);
const saveContent = async () => {
	if (!activeProject.value) return;
	if (isContentSameAsLastSaved.value) return;
	const res = await update({ ...activeProject.value, overviewContent: editorContent.value });
	// Note that an error has happened when res is null since the `update` function's swallowing the error and returning null instead of throwing it.
	if (!res) {
		stopAutoSave();
		return;
	}
	// This will update the last saved content to the current content
	await refresh();
};

onMounted(() => {
	editorContent.value = lastSavedContent.value;
});

watch(editorContent, () => {
	if (!hasEditPermission.value) return;
	// If the content changes, start the auto-save timer if it's not already running
	if (autoSaveIntervalId !== null) return;
	startAutoSave();
});

onUnmounted(() => {
	stopAutoSave();
});
</script>

<style scoped>
:deep(.ql-editor) {
	font-family: var(--font-family);
	font-size: var(--font-size);
}

/* Editor toolbar formatting */
.readonly :deep(.p-editor-toolbar) {
	display: none;
}

:deep(.p-editor-container .p-editor-toolbar) {
	border-radius: 0px;
	border-color: var(--surface-border-light) !important;
	border-top: none !important;
}

:deep(.ql-picker-label) {
	font-family: var(--font-family);
}

:deep(.ql-active) {
	background-color: var(--surface-highlight) !important;
	color: var(--text-color-subdued) !important;
}

:deep(.ql-active .ql-stroke) {
	stroke: var(--text-color-subdued) !important;
}

:deep(.ql-selected) {
	background-color: var(--surface-highlight) !important;
}
</style>
