<template>
	<Editor v-model="editorContent" class="editor h-full" />
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted, watch } from 'vue';
import Editor from 'primevue/editor';
import { update } from '@/services/project';
import { useProjects } from '@/composables/project';
import { Project } from '@/types/Types';

const AUTO_SAVE_DELAY = 3000;
const DEFAULT_EDITOR_CONTENT = `
	<div>
			<h2>Hey there! </h2>
			<p>This is your project overview page. Use this space however you like. Not sure where to start? Here are some things you can try:</p>
	<br>
			<ul>
					<li><strong>Upload stuff:</strong> Upload documents, models, code or datasets with the green button in the bottom left corner.</li>
					<li><strong>Explore and add:</strong> Use the project selector in the top nav to switch to the Explorer where you can find documents, models and datasets that you can add to your project.</li>
					<li><strong>Build a model:</strong> Create a model that fits just what you need.</li>
					<li><strong>Create a workflow:</strong> Connect resources with operators so you can focus on the science and not the plumbing.</li>
			</ul>
	<br>
			<p>Feel free to erase this text and make it your own.</p>
	</div>
`;
const props = defineProps<{ project: Project | null }>();
const editorContent = ref('');
const lastSavedContent = computed(() => props.project?.overviewText ?? DEFAULT_EDITOR_CONTENT);

let autoSaveIntervalId: NodeJS.Timeout | null = null;
const startAutoSave = () => {
	autoSaveIntervalId = setInterval(saveContent, AUTO_SAVE_DELAY);
};
const stopAutoSave = () => {
	if (autoSaveIntervalId === null) return;
	clearInterval(autoSaveIntervalId);
	autoSaveIntervalId = null;
};

const isContentSameAsLastSaved = computed(
	() =>
		lastSavedContent.value.length === editorContent.value.length &&
		lastSavedContent.value === editorContent.value
);
const saveContent = async () => {
	if (!props.project) return;
	if (isContentSameAsLastSaved.value) return;
	const res = await update({ ...props.project, overviewText: editorContent.value });
	// Note that an error has happened when res is null since the `update` function's swallowing the error and returning null instead of throwing it.
	if (!res) {
		stopAutoSave();
		return;
	}
	// This will update the last saved content to the current content
	await useProjects().refresh();
};

onMounted(() => {
	editorContent.value = lastSavedContent.value;
});

watch(editorContent, () => {
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
